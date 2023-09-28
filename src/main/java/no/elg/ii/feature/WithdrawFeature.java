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

package no.elg.ii.feature;

import static no.elg.ii.util.InventoryUtil.findFirst;
import static no.elg.ii.util.WidgetUtil.ZERO_QUANTITY_BANK_ITEM_OPACITY;
import static no.elg.ii.util.WidgetUtil.isEmpty;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import no.elg.ii.inventory.InventoryState;
import no.elg.ii.service.WidgetService;
import no.elg.ii.util.Util;
import no.elg.ii.util.VarbitsService;
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
  @Inject
  private WidgetService widgetService;
  @Inject
  private VarbitsService varbitsService;

  @Subscribe
  public void onMenuOptionClicked(final MenuOptionClicked event) {
    Widget bankWidget = event.getWidget();
    if (bankWidget != null) {
      String menuOption = event.getMenuOption();
      if (menuOption != null && menuOption.startsWith(WITHDRAW_PREFIX_OPTION)) {
        int amount = Util.getNumberFromMenuOption(menuOption);
        if (amount == Util.NO_MENU_OPTION_NUMBER) {
          return;
        }
        log.debug("Withdrawing item {}", WidgetUtil.getWidgetInfo(bankWidget));
        withdraw(bankWidget, amount);
      }
    }
  }

  private void withdraw(Widget bankWidget, int amount) {
    int bankWidgetItemId;
    ItemComposition bankWidgetComposition;

    int originalItemId = bankWidget.getItemId();
    ItemComposition originalComposition = itemManager.getItemComposition(originalItemId);

    //If we're withdrawing as a note, we need to get the item id of the note as the banked item is never the noted item
    if (isWithdrawingAsNote() && isItemNotable(originalComposition)) {
      bankWidgetItemId = originalComposition.getLinkedNoteId();
      bankWidgetComposition = itemManager.getItemComposition(bankWidgetItemId);
    } else {
      bankWidgetItemId = originalItemId;
      bankWidgetComposition = originalComposition;
    }

    //Only withdraw the amount that is available
    int quantityToWithdraw = Math.min(bankWidget.getItemQuantity(), amount);

    if (bankWidgetComposition.isStackable()) {
      Widget inventoryWidget = findFirst(client, WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER, w -> w.getItemId() == bankWidgetItemId);
      if (inventoryWidget != null) {
        //There is a matching widget, so we can just update the quantity
        updateBankItem(bankWidget, quantityToWithdraw);
        widgetService.updateQuantity(inventoryWidget, quantityToWithdraw);
        getState().setSlot(inventoryWidget.getIndex(), bankWidgetItemId, inventoryWidget.getItemQuantity());
      } else {
        fillFirstEmpty(bankWidget, bankWidgetItemId, quantityToWithdraw);
      }
    } else {
      //Item is not stackable, so we have to fill the inventory with the item until we run out of space or items
      for (int i = 0; i < quantityToWithdraw; i++) {
        boolean outOfSpace = fillFirstEmpty(bankWidget, bankWidgetItemId, 1);
        if (outOfSpace) {
          break;
        }
      }
    }
  }

  /**
   * @return Whether the item can be noted
   */
  private boolean isItemNotable(ItemComposition itemComposition) {
    return itemComposition.getLinkedNoteId() > 0;
  }

  /**
   * @return {@code false} if there is no more space in the inventory, {@code true} otherwise
   */
  private boolean fillFirstEmpty(Widget bankWidget, int actualItemId, int quantityToWithdraw) {
    var emptyWidget = findFirst(client, WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER, w -> isEmpty(w) && !getState().getSlot(w.getIndex()).hasValidItemId());
    if (emptyWidget != null) {
      widgetService.setFakeWidgetItem(emptyWidget, actualItemId, quantityToWithdraw);
      updateBankItem(bankWidget, quantityToWithdraw);
      getState().setSlot(emptyWidget.getIndex(), bankWidget.getItemId(), quantityToWithdraw);
      return false;
    }
    return true;
  }

  private void updateBankItem(Widget bankWidget, int quantityToWithdraw) {
    int newQuantity = bankWidget.getItemQuantity() - quantityToWithdraw;
    widgetService.updateQuantity(bankWidget, -quantityToWithdraw);
    if (newQuantity == 0) {
      if (isPlaceholdersDisabled()) {
        log.debug("Hiding bank widget, new quantity is 0 and placeholders are disabled");
        widgetService.setAsHideOpacity(bankWidget);
      } else {
        widgetService.setOpacity(bankWidget, ZERO_QUANTITY_BANK_ITEM_OPACITY);
      }
    } else {
      widgetService.setAsChangeOpacity(bankWidget);
    }
  }

  /**
   * @return Whether the bank is set to withdraw as a note
   */
  private boolean isWithdrawingAsNote() {
    return varbitsService.isVarbitTrue(VarbitsService.BOOLEAN_WITHDRAW_AS_NOTE);
  }

  private boolean isPlaceholdersDisabled() {
    return varbitsService.isVarbitFalse(VarbitsService.BOOLEAN_ALWAYS_SET_BANK_PLACEHOLDER);
  }

  @Nonnull
  @Override
  public String getConfigKey() {
    return WITHDRAW_CONFIG_KEY;
  }
}
