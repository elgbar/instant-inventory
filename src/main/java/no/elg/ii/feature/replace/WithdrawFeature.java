/*
 * Copyright (c) 2023 Elg
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package no.elg.ii.feature.replace;

import static no.elg.ii.util.InventoryUtil.IS_EMPTY_FILTER;
import static no.elg.ii.util.InventoryUtil.findFirst;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;
import no.elg.ii.inventory.slot.ReplacementInventorySlot;
import no.elg.ii.util.IndexedItem;
import no.elg.ii.util.Util;
import no.elg.ii.util.WidgetUtil;

@Slf4j
public class WithdrawFeature extends ReplacedItemFeature {

  public static final String WITHDRAW_PREFIX_OPTION = "Withdraw-";
  public static final String WITHDRAW_CONFIG_KEY = "instantWithdraw";

  @Inject
  @VisibleForTesting
  Client client;

  @Subscribe
  public void onMenuOptionClicked(final MenuOptionClicked event) {
    Widget widget = event.getWidget();
    if (widget != null) {
      String menuOption = event.getMenuOption();
      if (menuOption != null && menuOption.startsWith(WITHDRAW_PREFIX_OPTION)) {
        int amount = Util.getNumber(menuOption);
        if (amount == Util.NO_NUMBER) {
          return;
        }
        log.debug("Withdrawing item {}", WidgetUtil.getWidgetInfo(widget));
        withdraw(widget, amount);
      }
    }
  }

  private void setSlot(int index, int itemId, int amount) {
    updateSlot(client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER), index, itemId, amount);
    updateSlot(client.getWidget(WidgetInfo.INVENTORY), index, itemId, amount);
  }

  private void updateSlot(@Nullable Widget parentWidget, int index, int itemId, int amount) {
    if (parentWidget != null) {
      var widget = parentWidget.getChild(index);
      changeWidgetItem(widget, itemId, amount);
    }
  }

  private void changeWidgetItem(Widget widget, int itemId, int amount) {
    widget.setHidden(false);
    widget.setOpacity(0);
    widget.setItemId(itemId);
    widget.setItemQuantity(amount);
  }

  private void withdraw(Widget widget, int amount) {
    int itemId = widget.getItemId();
    ItemComposition itemComposition = itemManager.getItemComposition(itemId);

    Widget matchingWidget = findFirst(client, WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER, w -> w.getItemId() == itemId);
    if (itemComposition.isStackable() && matchingWidget != null) {
      widget.setItemQuantity(widget.getItemQuantity() - amount);
      matchingWidget.setItemQuantity(matchingWidget.getItemQuantity() + amount);
      IndexedItem indexedItem = IndexedItem.of(matchingWidget.getIndex(), new Item(itemId, matchingWidget.getItemQuantity()));
      getState().setSlot(matchingWidget.getIndex(), new ReplacementInventorySlot(client.getTickCount(), itemId, indexedItem, null));
    } else {
      var emptyWidget = findFirst(client, WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER, w -> IS_EMPTY_FILTER.filter(w) && !getState().getSlot(w.getIndex()).hasValidItemId());
      if (emptyWidget != null) {
        //FIXME if not  stackable then find N empty slots, not just set the quantity :p
        int index = emptyWidget.getIndex();
        setSlot(index, itemId, amount);
        changeWidgetItem(emptyWidget, itemId, amount);

        IndexedItem indexedItem = IndexedItem.of(index, new Item(itemId, amount));
        getState().setSlot(index, new ReplacementInventorySlot(client.getTickCount(), itemId, indexedItem, null));
      }
    }
  }

  @Nonnull
  @Override
  public String getConfigKey() {
    return WITHDRAW_CONFIG_KEY;
  }

}
