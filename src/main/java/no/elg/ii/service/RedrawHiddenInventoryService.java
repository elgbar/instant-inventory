/*
 * Copyright (c) 2026 Elg
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

import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;
import no.elg.ii.InstantInventoryConfig;

/**
 * Keep inventories hidden by another interface, such as the inventory next to an open bank, drawn with the items in
 * the inventory.
 * <p>
 * The client does not run the inventory transmit listener of a hidden widget, so a hidden inventory keeps the items it
 * had when it was hidden. When shown again, e.g., when closing the bank, these stale items are visible until the
 * client catches up. The client only redraws the inventory itself when switching to the inventory tab, see the
 * {@code toplevel_sidebutton_switch} client script.
 */
@Slf4j
@Singleton
@NoArgsConstructor
public class RedrawHiddenInventoryService {

  @Inject
  Client client;

  @Inject
  InstantInventoryConfig config;

  @Subscribe
  public void onItemContainerChanged(ItemContainerChanged event) {
    if (event.getContainerId() != InventoryID.INV || !config.redrawHiddenInventories()) {
      return;
    }
    for (int componentId : INVENTORY_ITEMS_CONTAINERS) {
      Widget container = client.getWidget(componentId);
      if (container == null || !container.isHidden()) {
        continue;
      }
      Object[] onInvTransmitListener = container.getOnInvTransmitListener();
      if (onInvTransmitListener != null) {
        log.trace("Redrawing hidden inventory {}", componentId);
        client.createScriptEventBuilder(onInvTransmitListener)
          .setSource(container)
          .build()
          .run();
      }
    }
  }
}
