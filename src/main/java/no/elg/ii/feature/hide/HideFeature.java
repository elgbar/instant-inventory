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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Streams;

import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import no.elg.ii.InstantInventoryPlugin;
import no.elg.ii.InventoryState;
import no.elg.ii.feature.Feature;
import no.elg.ii.util.IndexedWidget;

@Slf4j
public abstract class HideFeature implements Feature {

  @Inject
  private InventoryState state;

  @Inject
  public InstantInventoryPlugin plugin;

  @Inject
  public ClientThread clientThread;

  private final Set<WidgetInfo> widgets = new HashSet<>();

  /* (non-javadoc)
   * Make sure the item in the slot is hidden, the client sets it as non-hidden each tick (?)
   *  or so. This must be done before the client is rendered otherwise (such as if we were to use
   *  the ClientTick event) the item would be visible for a single frame.
   */
  @Subscribe
  public void onBeforeRender(BeforeRender beforeRender) {
    updateHiddenStatus();
  }

  @Override
  public void reset() {
    Feature.super.reset();
    clientThread.invoke(this::updateHiddenStatus);
  }

  @VisibleForTesting
  public void updateHiddenStatus() {
    for (IndexedWidget indexedWidget : inventoryItems()) {
      Widget widget = indexedWidget.getWidget();
      int index = indexedWidget.getIndex();
      //Only hide the item when the state has been set to a valid item id
      boolean shouldBeHidden = getState().isValid(index);

      if (shouldBeHidden && !widget.isSelfHidden()) {
        log.debug("Hiding item at index {}", index);
        widget.setHidden(true);
      } else if (!shouldBeHidden && widget.isSelfHidden()) {
        log.debug("Showing item at index {}", index);
        widget.setHidden(false);
      }
    }
  }

  @SuppressWarnings("UnstableApiUsage")
  protected Set<IndexedWidget> inventoryItems() {
    if (widgets.isEmpty()) {
      log.error("No widget added to hide feature " + this.getClass().getName());
      return Collections.emptySet();
    }
    return widgets.stream()
      .map(it -> plugin.inventoryItems(it))
      .flatMap(ws ->
        Streams.mapWithIndex(
          Arrays.stream(ws),
          (from, index) -> new IndexedWidget((int) index, from)
        )
      )
      .collect(Collectors.toSet());
  }

  @Nonnull
  @Override
  public InventoryState getState() {
    return state;
  }

  public void showOnWidgets(WidgetInfo widget) {
    this.widgets.add(widget);
  }

  public void showOnWidgets(WidgetInfo... widgets) {
    this.widgets.addAll(Arrays.asList(widgets));
  }

  @VisibleForTesting
  public Set<WidgetInfo> getWidgets() {
    return widgets;
  }
}
