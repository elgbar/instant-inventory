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
import no.elg.ii.feature.Feature;
import no.elg.ii.inventory.InventoryState;
import no.elg.ii.service.WidgetService;
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
  @Inject
  private WidgetService widgetService;

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

    //If we're withdrawing as a note, we need to get the item id of the note as the banked item is never the noted item
    boolean isWithdrawingAsNote = isWithdrawingAsNote();
    int originalItemId = bankWidget.getItemId();
    ItemComposition originalComposition = itemManager.getItemComposition(originalItemId);
    if (isWithdrawingAsNote) {
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
        widgetService.updateQuantity(bankWidget, -quantityToWithdraw);
        widgetService.updateQuantity(inventoryWidget, quantityToWithdraw);
        getState().setSlot(inventoryWidget.getIndex(), bankWidgetItemId, inventoryWidget.getItemQuantity());
      } else {
        fillFirstEmpty(bankWidget, quantityToWithdraw);
      }
    } else {
      //Item is not stackable, so we have to fill the inventory with the item until we run out of space or items
      for (int i = 0; i < quantityToWithdraw; i++) {
        boolean outOfSpace = fillFirstEmpty(bankWidget, 1);
        if (outOfSpace) {
          break;
        }
      }
    }
  }

  /**
   * @return {@code false} if there is no more space in the inventory, {@code true} otherwise
   */
  private boolean fillFirstEmpty(Widget bankWidget, int quantityToWithdraw) {
    var emptyWidget = findFirst(client, WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER, w -> isEmpty(w) && !getState().getSlot(w.getIndex()).hasValidItemId());
    if (emptyWidget != null) {
      widgetService.setFakeWidgetItem(emptyWidget, bankWidget);
      widgetService.setQuantity(emptyWidget, quantityToWithdraw);

      widgetService.updateQuantity(bankWidget, -quantityToWithdraw);
      getState().setSlot(emptyWidget.getIndex(), bankWidget.getItemId(), quantityToWithdraw);
      return false;
    }
    return true;
  }

  /**
   * @return Whether the bank is set to withdraw as a note
   */
  private boolean isWithdrawingAsNote() {
    return client.getVarbitValue(VARBIT_WITHDRAW_AS_NOTE) == VARBIT_WITHDRAW_AS_NOTE_VALUE_TRUE;
  }

  public static final int VARBIT_WITHDRAW_AS_NOTE = 3958;
  public static final int VARBIT_WITHDRAW_AS_NOTE_VALUE_TRUE = 1;

  @Nonnull
  @Override
  public String getConfigKey() {
    return WITHDRAW_CONFIG_KEY;
  }
}
