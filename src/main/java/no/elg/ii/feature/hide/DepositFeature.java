/*
 * Copyright (c) 2022-2023 Elg
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
package no.elg.ii.feature.hide;

import java.util.Set;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;
import no.elg.ii.util.IndexedWidget;
import no.elg.ii.util.Util;

@Slf4j
public class DepositFeature extends HideFeature {

  public static final String DEPOSIT_PREFIX_OPTION = "Deposit-";
  public static final String DEPOSIT_ALL_OPTION = "Deposit inventory";
  public static final String DEPOSIT_CONFIG_KEY = "instantDeposit";

  @Subscribe
  public void onMenuOptionClicked(final MenuOptionClicked event) {
    Widget widget = event.getWidget();
    if (widget != null) {
      String menuOption = event.getMenuOption();
      if (DEPOSIT_ALL_OPTION.equals(menuOption)) {
        log.debug("Hiding all items");
        inventoryItems().forEach(indexedWidget -> hide(indexedWidget.getWidget()));
        return;
      }
      int eventItemId = event.getItemId();
      if (menuOption != null && (menuOption.startsWith(DEPOSIT_PREFIX_OPTION) || menuOption.startsWith(ADD_PREFIX_OPTION))) {
        int toTake = Util.getNumberFromMenuOption(menuOption);
        if (toTake == Util.NO_NUMBER) {
          return;
        }
        if (toTake >= widget.getItemQuantity()) {
          log.debug("Hiding " + toTake + " items");
          Set<IndexedWidget> indexedWidgets = inventoryItems();
          indexedWidgets.stream()
            .filter(it -> it.getWidget().getItemId() == eventItemId)
            .sorted()
            .limit(toTake)
            .forEach(indexedWidget -> hide(indexedWidget.getWidget()));
        } else {
          int quantity = widget.getItemQuantity() - toTake;
          log.debug("Updating item quantity from " + widget.getItemQuantity() + " be " + quantity);
          widget.setItemQuantity(quantity);
        }
      }
    }
  }

  //TODO handle WidgetID.BANK_DEPOSIT_INVENTORY click (hide all items in inventory)

  @Nonnull
  @Override
  public String getConfigKey() {
    return DEPOSIT_CONFIG_KEY;
  }
}
