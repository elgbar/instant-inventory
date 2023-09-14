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

import static no.elg.ii.util.InventoryUtil.findFirst;
import static no.elg.ii.util.InventoryUtil.isEmpty;
import static no.elg.ii.util.WidgetUtil.setFakeWidgetItem;
import static no.elg.ii.util.WidgetUtil.setQuantity;
import static no.elg.ii.util.WidgetUtil.updateQuantity;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import no.elg.ii.feature.Feature;
import no.elg.ii.inventory.InventoryState;
import no.elg.ii.util.InventoryUtil;
import no.elg.ii.util.Util;
import no.elg.ii.util.WidgetUtil;

@Slf4j
public class WithdrawFeature implements Feature {

  public static final String WITHDRAW_PREFIX_OPTION = "Withdraw-";
  public static final String WITHDRAW_CONFIG_KEY = "instantWithdraw";

  @Inject
  @VisibleForTesting
  Client client;

  @Inject
  @VisibleForTesting
  public ItemManager itemManager;
  
  @Inject
  @Getter
  private InventoryState state;

  @Subscribe
  public void onMenuOptionClicked(final MenuOptionClicked event) {
    Widget bankWidget = event.getWidget();
    if (bankWidget != null) {
      String menuOption = event.getMenuOption();
      if (menuOption != null && menuOption.startsWith(WITHDRAW_PREFIX_OPTION)) {
        int amount = Util.getNumberFromMenuOption(menuOption);
        if (amount == Util.NO_NUMBER) {
          return;
        }
        log.debug("Withdrawing item {}", WidgetUtil.getWidgetInfo(bankWidget));
        withdraw(bankWidget, amount);
      }
    }
  }

//  private void setSlot(int index, int itemId, int amount) {
//    updateSlot(client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER), index, itemId, amount);
////    updateSlot(client.getWidget(WidgetInfo.INVENTORY), index, itemId, amount);
//  }

  private void withdraw(Widget widget, int amount) {
    int itemId = widget.getItemId();
    ItemComposition itemComposition = itemManager.getItemComposition(itemManager.canonicalize(itemId));

    //Only withdraw the amount that is available
    int quantityToWithdraw = Math.min(widget.getItemQuantity(), amount);

    if (itemComposition.isStackable()) {
      Widget matchingWidget = findFirst(client, WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER, w -> w.getItemId() == itemId);
      if (matchingWidget != null) {
        //There is a matching widget, so we can just update the quantity
        int updatedQuantity = matchingWidget.getItemQuantity() + quantityToWithdraw;
        setQuantity(widget, updatedQuantity);
        setQuantity(matchingWidget, updatedQuantity);
        getState().setSlot(matchingWidget.getIndex(), itemId, updatedQuantity);
      } else {
        fillFirstEmpty(widget, quantityToWithdraw);
      }
    } else {
      //Item is not stackable, so we have to fill the inventory with the item until we run out of space or items
      for (int i = 0; i < quantityToWithdraw; i++) {
        boolean outOfSpace = fillFirstEmpty(widget, 1);
        if (outOfSpace) {
          break;
        }
      }
    }
  }

  /**
   * @param widget
   * @param quantityToWithdraw
   * @return If there is no more space in the inventory
   */
  private boolean fillFirstEmpty(Widget widget, int quantityToWithdraw) {
    var emptyWidget = findFirst(client, WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER, w -> IS_EMPTY_FILTER.filter(w) && !getState().getSlot(w.getIndex()).hasValidItemId());
    if (emptyWidget != null) {
      setFakeWidgetItem(emptyWidget, widget);
      setQuantity(emptyWidget, quantityToWithdraw);
      getState().setSlot(emptyWidget.getIndex(), widget.getItemId(), quantityToWithdraw);
      return false;
    }
    return true;
  }

  @Nonnull
  @Override
  public String getConfigKey() {
    return WITHDRAW_CONFIG_KEY;
  }
}
