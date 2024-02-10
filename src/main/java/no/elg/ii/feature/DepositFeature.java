/*
 * Copyright (c) 2022-2024 Elg
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

import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import no.elg.ii.inventory.InventoryService;
import no.elg.ii.inventory.InventoryState;
import no.elg.ii.inventory.slot.InventorySlot;
import no.elg.ii.util.IndexedWidget;
import no.elg.ii.util.Util;
import no.elg.ii.util.WidgetUtils;

@Slf4j
public class DepositFeature extends HideFeature {

  public static final String DEPOSIT_PREFIX_OPTION = "Deposit-";
  public static final String DEPOSIT_ALL_OPTION = "Deposit inventory";
  public static final String ADD_ALL_OPTION = "Add all";
  public static final String DEPOSIT_CONFIG_KEY = "instantDeposit";

  @Inject
  private InventoryService inventoryService;

  @Inject
  private ItemManager itemManager;
  @Inject
  private InventoryState inventoryState;

  @Subscribe
  public void onMenuOptionClicked(final MenuOptionClicked event) {
    Widget widget = event.getWidget();
    if (widget != null) {
      String menuOption = event.getMenuOption();
      if (DEPOSIT_ALL_OPTION.equals(menuOption) || ADD_ALL_OPTION.equals(menuOption)) {
        log.debug("Hiding all items");
        inventoryService.getAllInventoryWidgets()
          .filter(iw -> WidgetUtils.isNotEmpty(iw.getWidget()))
          .forEach(indexedWidget -> hide(indexedWidget.getWidget()));
        return;
      }
      int eventItemId = event.getItemId();
      int clickedIndex = widget.getIndex();
      if (menuOption != null && (menuOption.startsWith(DEPOSIT_PREFIX_OPTION))) {
        int toTake = Util.getNumberFromMenuOption(menuOption);
        if (toTake == Util.NO_MENU_OPTION_NUMBER) {
          return;
        }
        int actualTaken;
        if (toTake >= widget.getItemQuantity()) {
          log.debug("Hiding " + toTake + " items");

          Set<IndexedWidget> itemToTake = inventoryService.getAllInventoryWidgets()
            .filter(it -> {
              InventorySlot slot = inventoryState.getSlot(it.getWidget().getIndex());
              return it.getIndex() == clickedIndex || slot != null && !slot.hasValidItemId() && it.getWidget().getItemId() == eventItemId;
            })
            .sorted()
            .limit(toTake)
            .collect(Collectors.toUnmodifiableSet());
          itemToTake.forEach(indexedWidget -> hide(indexedWidget.getWidget()));
          actualTaken = itemToTake.stream().mapToInt(indexedWidget1 -> indexedWidget1.getWidget().getItemQuantity()).sum();
        } else {
          int ui = widget.getItemQuantity() - toTake;
          log.debug("Updating item quantity from " + widget.getItemQuantity() + " be " + ui);
          widget.setItemQuantity(ui);
          actualTaken = toTake;
        }

        int canonItemId = itemManager.canonicalize(eventItemId);
        //Update widget in bank
        Widget bankInventoryContainer = client.getWidget(ComponentID.BANK_ITEM_CONTAINER);
        if (bankInventoryContainer != null) {
          for (Widget bankWidget : bankInventoryContainer.getDynamicChildren()) {
            if (itemManager.canonicalize(bankWidget.getItemId()) == canonItemId) {
              bankWidget.setItemQuantity(bankWidget.getItemQuantity() + actualTaken);
              return;
            }
          }
        }
      }
    }
  }

  @Nonnull
  @Override
  public String getConfigKey() {
    return DEPOSIT_CONFIG_KEY;
  }
}
