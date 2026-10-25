/*
 * Copyright (c) 2025-2026 Elg
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


import static no.elg.ii.util.InventoryUtil.INVENTORY_ITEMS_CONTAINERS;

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
import net.runelite.api.events.WidgetDrag;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;
import no.elg.ii.feature.state.InventoryState;

@Slf4j
@Singleton
@NoArgsConstructor
public class DisallowModifiedWidgetInteractionService {

  @Inject
  InventoryService inventoryService;

  @Inject
  Client client;

  @Inject
  InventoryState inventoryState;

  @Subscribe(priority = Integer.MAX_VALUE)
  public void onMenuOptionClicked(final MenuOptionClicked event) {
    Widget widget = event.getWidget();
    if (widget != null && !event.isConsumed() && (event.isItemOp() || isUseItemAction(event.getMenuEntry())) && inventoryState.isActiveSlot(widget.getIndex())) {
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

  /**
   * When an item is dragged onto another slot, the game shows each slot's item in the other, taking the items from what
   * the slots show. A predicted item would then end up in a slot that is not tracked, where it can be used before the
   * server has it (issue #22). So when either slot is predicted, both are reset to what the server has
   */
  @Subscribe
  public void onWidgetDrag(WidgetDrag event) {
    // Posted every cycle of a drag. The mouse button is let go in the last one, before the client handles the drop
    if (client.getMouseCurrentButton() != 0) {
      return;
    }
    Widget dragged = client.getDraggedWidget();
    Widget draggedOn = client.getDraggedOnWidget();
    if (dragged == null || draggedOn == null || dragged.getId() != draggedOn.getId()
      || !INVENTORY_ITEMS_CONTAINERS.contains(dragged.getId())) {
      return;
    }
    int from = dragged.getIndex();
    int onto = draggedOn.getIndex();
    if (from != onto && (inventoryState.isActiveSlot(from) || inventoryState.isActiveSlot(onto))) {
      log.debug("Resetting slots {} and {}, as dragging them would move a predicted item", from, onto);
      inventoryState.resetState(from);
      inventoryState.resetState(onto);
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
