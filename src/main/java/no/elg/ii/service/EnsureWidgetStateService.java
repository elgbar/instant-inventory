/*
 * Copyright (c) 2023-2024 Elg
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

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import javax.inject.Inject;
import net.runelite.api.widgets.Widget;
import no.elg.ii.inventory.InventoryService;
import no.elg.ii.inventory.InventoryState;
import no.elg.ii.inventory.slot.InventorySlot;

public class EnsureWidgetStateService {
  @Inject
  InventoryService inventoryService;

  @Inject
  InventoryState state;

  /**
   * Force widget to look a certain way. Sometimes the widgets get updated by client code, but this will override
   * any other (clientside) changes to inventory widgets.
   * <p>
   * If this is not actively corrected the item will be fully visible.
   * This only applies to the clicked item, but it is not known <b>when</b> the item was clicked.
   * So this is a brute-force method to ensure that the item is hidden.
   */
  public void forceWidgetState(BiPredicate<Widget, InventorySlot> widgetFilter, BiConsumer<Widget, InventorySlot> force) {
    state.getActiveSlots()
      .forEach(iis -> inventoryService.getAllInventoryWidgets()
        .filter(slotWidget -> slotWidget.getIndex() == iis.getIndex() && widgetFilter.test(slotWidget.getWidget(), iis.getSlot()))
        .forEach(slotWidget -> force.accept(slotWidget.getWidget(), iis.getSlot())));
  }

}
