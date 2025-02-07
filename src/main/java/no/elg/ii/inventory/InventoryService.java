/*
 * Copyright (c) 2023-2025 Elg
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
import static no.elg.ii.util.InventoryUtil.GROUP_ITEM_CONTAINER;
import static no.elg.ii.util.InventoryUtil.INVENTORY_ITEMS_CONTAINERS;

import com.google.common.collect.Streams;
import com.google.inject.Singleton;
import java.util.Arrays;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;
import no.elg.ii.inventory.slot.InventorySlot;
import no.elg.ii.service.EnsureWidgetStateService;
import no.elg.ii.service.WidgetService;
import no.elg.ii.util.IndexedWidget;
import no.elg.ii.util.WidgetUtils;

@Singleton
@Slf4j
public class InventoryService {

  @Inject
  private Client client;
  @Inject
  private EnsureWidgetStateService ensureWidgetStateService;
  @Inject
  private WidgetService widgetService;

  @Nullable
  public ItemContainer getCurrentInventoryContainer() {
    Widget widget = client.getWidget(GROUP_ITEM_CONTAINER);
    InventoryID itemContainer;
    if (widget != null && !widget.isHidden()) {
      itemContainer = InventoryID.GROUP_STORAGE_INV;
    } else {
      itemContainer = InventoryID.INVENTORY;
    }
    return client.getItemContainer(itemContainer);
  }

  @Nonnull
  private Stream<Widget> getOpenWidgetItemContainer() {
    return INVENTORY_ITEMS_CONTAINERS.stream()
      .map(componentId -> client.getWidget(componentId))
      .filter(widget -> widget != null && !widget.isHidden());
  }

  /**
   * @return A stream of indexed widgets in the current open inventory
   */
  @Nonnull
  @SuppressWarnings("UnstableApiUsage")
  public final Stream<IndexedWidget> getAllOpenInventoryWidgets() {
    return getOpenWidgetItemContainer()
      .flatMap(container -> Streams.mapWithIndex(Arrays.stream(container.getDynamicChildren()), indexWidget));
  }

  public boolean isDifferent(Widget widget, InventorySlot slot) {
    return slot.hasValidItemId() && !WidgetUtils.isEmpty(widget)
      && (widget.getItemId() != slot.getItemId()
      || widget.getItemQuantity() != slot.getQuantity()
      || widget.getOpacity() != slot.getOpacity());
  }

  public void setWidgetFromSlot(Widget widget, InventorySlot slot) {
    widgetService.updateVisibleWidget(widget, slot.getItemId(), slot.getQuantity());
    widgetService.setOpacity(widget, slot.getOpacity(), true);
  }

  @Subscribe
  public void onBeforeRender(BeforeRender event) {
    ensureWidgetStateService.forceWidgetState(this::isDifferent, this::setWidgetFromSlot);
  }
}
