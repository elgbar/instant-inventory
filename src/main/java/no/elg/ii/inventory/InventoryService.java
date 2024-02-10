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

package no.elg.ii.inventory;

import static no.elg.ii.util.IndexedWidget.indexWidget;
import static no.elg.ii.util.InventoryUtil.INVENTORY_ITEMS_CONTAINERS;

import com.google.common.collect.Streams;
import com.google.inject.Singleton;
import java.util.Arrays;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;
import no.elg.ii.inventory.slot.InventorySlot;
import no.elg.ii.service.EnsureWidgetStateService;
import no.elg.ii.util.IndexedWidget;
import no.elg.ii.util.WidgetUtils;

@Singleton
@Slf4j
public class InventoryService {

  @Inject
  private Client client;
  @Inject
  private EnsureWidgetStateService ensureWidgetStateService;

  @Nonnull
  private Stream<Widget> getOpenWidgetItemContainer() {
    return INVENTORY_ITEMS_CONTAINERS.stream()
      .map(componentId -> client.getWidget(componentId))
      .filter(widget -> widget != null && !widget.isHidden());
  }

  @Nonnull
  @SuppressWarnings("UnstableApiUsage")
  public final Stream<IndexedWidget> getAllInventoryWidgets() {
    return getOpenWidgetItemContainer()
      .flatMap(container -> Streams.mapWithIndex(Arrays.stream(container.getDynamicChildren()), indexWidget));
  }

  public boolean hasDifferentOpacity(Widget widget, InventorySlot slot) {
    return slot.hasValidItemId() && widget.getOpacity() != slot.getOpacity() && !WidgetUtils.isEmpty(widget);
  }

  public void setWidgetOpacityFromSlot(Widget widget, InventorySlot slot) {
    widget.setOpacity(slot.getOpacity());
  }

  @Subscribe
  public void onBeforeRender(BeforeRender event) {
    ensureWidgetStateService.forceWidgetState(this::hasDifferentOpacity, this::setWidgetOpacityFromSlot);
  }
}
