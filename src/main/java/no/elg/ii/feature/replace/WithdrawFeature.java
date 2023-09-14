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
import static no.elg.ii.util.WidgetUtil.FULLY_OPAQUE;
import static no.elg.ii.util.WidgetUtil.setFakeWidgetItem;
import static no.elg.ii.util.WidgetUtil.setQuantity;
import static no.elg.ii.util.WidgetUtil.unhide;
import static no.elg.ii.util.WidgetUtil.updateQuantity;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import no.elg.ii.feature.Feature;
import no.elg.ii.inventory.InventoryState;
import no.elg.ii.inventory.slot.InventorySlot;
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
  @Inject
  @Getter
  private ClientThread clientThread;

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

  boolean b = true;
  boolean c = true;


  @Subscribe
  public void onItemContainerChanged(final ItemContainerChanged event) {
    Widget bankInvContainer = client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER);
    if (bankInvContainer == null || bankInvContainer.isHidden()) {
      return;
    }
//    clientThread.invokeAtTickEnd(() -> {
    getState().getActiveSlots().forEach(iis -> {
      var slot = iis.getSlot();
      Widget inventoryWidget = bankInvContainer.getChild(iis.getIndex());
//      if (inventoryWidget == null || !slot.isDifferent(inventoryWidget) || isEmpty(inventoryWidget)) {
//        return;
//      }
//      if (slot.isDifferent(inventoryWidget)) {
      //The item in the inventory is not the same as the one in the bank, so we need to update it

//        if (c)
      setFakeWidgetItem(bankInvContainer, slot.getItemId(), slot.getQuantity());
//        if (b)
      getState().resetState(iis.getIndex());
//      }
    });
//    });


    for (Widget child : bankInvContainer.getDynamicChildren()) {
      unhide(child);
      InventorySlot slot = getState().getSlot(child.getIndex());
      if (slot == null || !slot.hasValidItemId()) {
        child.setOpacity(FULLY_OPAQUE);
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
        updateQuantity(bankWidget, -quantityToWithdraw);
        updateQuantity(inventoryWidget, quantityToWithdraw);
        getState().setSlot(inventoryWidget.getIndex(), bankWidgetItemId, inventoryWidget.getItemQuantity());
        InventoryUtil.copyWidgetFromContainer(client, WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER, WidgetInfo.INVENTORY, inventoryWidget.getIndex());
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
      setFakeWidgetItem(bankWidget, emptyWidget);
      setQuantity(emptyWidget, quantityToWithdraw);
      updateQuantity(bankWidget, -quantityToWithdraw);
      getState().setSlot(emptyWidget.getIndex(), bankWidget.getItemId(), quantityToWithdraw);
      InventoryUtil.copyWidgetFromContainer(client, WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER, WidgetInfo.INVENTORY, emptyWidget.getIndex());
      return false;
    }
    return true;
  }

  /**
   * This is based on observation, so it might not be correct.
   *
   * @return Whether the bank is set to withdraw as a note
   */
  private boolean isWithdrawingAsNote() {
    Widget widget = client.getWidget(WidgetID.BANK_GROUP_ID, BANK_WITHDRAW_AS_ITEM);
    return widget != null && widget.getOnOpListener() != null;
  }

  static final int BANK_WITHDRAW_AS_ITEM = 22;
  static final int BANK_WITHDRAW_AS_NOTE = 24;

  @Nonnull
  @Override
  public String getConfigKey() {
    return WITHDRAW_CONFIG_KEY;
  }
}
