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
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import no.elg.ii.InstantInventoryConfig;
import no.elg.ii.InstantInventoryPlugin;
import no.elg.ii.feature.Feature;
import no.elg.ii.inventory.InventoryState;
import no.elg.ii.util.IndexedWidget;
import no.elg.ii.util.InventoryUtil;
import no.elg.ii.util.WidgetUtil;

@Slf4j
public abstract class HideFeature implements Feature {

  @Inject
  public InstantInventoryPlugin plugin;
  @Inject
  @VisibleForTesting
  protected InstantInventoryConfig config;
  @Inject
  public ClientThread clientThread;
  @Inject
  @Getter
  private InventoryState state;

  @Inject
  public Client client;

  protected void hide(Widget widget) {
    clientThread.invokeAtTickEnd(() -> {
      log.debug("Hiding widget {}", WidgetUtil.getWidgetInfo(widget));
      widget.setOpacity(config.hideOpacity());
      getState().setSlot(widget);
    });
  }

  @Nonnull
  @SuppressWarnings("UnstableApiUsage")
  protected Set<IndexedWidget> inventoryItems() {
    Widget openWidgetItemContainer = InventoryUtil.getOpenWidgetItemContainer(client);
    if (openWidgetItemContainer == null) {
      return Set.of();
    }
    Widget[] children = openWidgetItemContainer.getDynamicChildren();
    return Streams.mapWithIndex(Arrays.stream(children), (from, index) -> new IndexedWidget((int) index, from)).collect(Collectors.toSet());
  }

  protected boolean isHidden(Widget widget) {
    return widget.getOpacity() == config.hideOpacity() || WidgetUtil.isEmpty(widget);
  }
}
