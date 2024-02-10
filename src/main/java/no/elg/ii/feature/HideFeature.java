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

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import no.elg.ii.InstantInventoryConfig;
import no.elg.ii.InstantInventoryPlugin;
import no.elg.ii.inventory.InventoryState;
import no.elg.ii.service.WidgetService;
import no.elg.ii.util.WidgetUtils;

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

  @Inject
  public WidgetService widgetService;

  protected void hide(@Nonnull Widget widget) {
    if (isHiding(widget)) {
      log.debug("Widget {} is already being hidden", WidgetUtils.debugInfo(widget));
    } else {
      log.debug("Hiding widget {}", WidgetUtils.debugInfo(widget));
      getState().setSlotAsHidden(widget); // Will be hidden by onBeforeRender
    }
  }

  protected boolean isHiding(@Nonnull Widget widget) {
    var slot = getState().getSlot(widget.getIndex());
    return slot != null && slot.hasValidItemId();
  }
}
