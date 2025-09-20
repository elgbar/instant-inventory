/*
 * Copyright (c) 2025 Elg
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

package no.elg.ii.service;


import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
@Singleton
@NoArgsConstructor
public class DisallowModifiedWidgetInteractionService {

  @Inject
  private InventoryService inventoryService;

  @Inject
  private Client client;

  @Subscribe(priority = Integer.MAX_VALUE)
  public void onMenuOptionClicked(final MenuOptionClicked event) {
    Widget widget = event.getWidget();
    if (widget != null && !event.isConsumed() && (event.isItemOp() || isUseItemAction(event.getMenuEntry()))) {
      ItemContainer currentInventoryContainer = inventoryService.getCurrentInventoryContainer();
      if (currentInventoryContainer != null) {
        Item item = currentInventoryContainer.getItem(widget.getIndex());
        if (item != null && item.getId() != widget.getItemId()) {
          log.debug("Widget item in slot {} is not the same as the item in the inventory. Disallowing interaction", widget.getIndex());
          event.consume();
        }
      }
    }
  }

  private boolean isUseItemAction(@Nullable MenuEntry menuEntry) {
    if (menuEntry != null && "Use".equals(menuEntry.getOption())) {
      MenuAction type = menuEntry.getType();
      if (client.isWidgetSelected()) {
        return type == MenuAction.WIDGET_TARGET_ON_WIDGET;
      } else {
        return type == MenuAction.WIDGET_TARGET;
      }
    } else {
      return false;
    }
  }
}
