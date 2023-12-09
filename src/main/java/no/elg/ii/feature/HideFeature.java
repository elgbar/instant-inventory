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
package no.elg.ii.feature;

import com.google.common.annotations.VisibleForTesting;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import no.elg.ii.InstantInventoryConfig;
import no.elg.ii.InstantInventoryPlugin;
import no.elg.ii.inventory.InventoryState;
import no.elg.ii.service.EnsureWidgetStateService;
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
  private WidgetService widgetService;

  @Inject
  private EnsureWidgetStateService ensureWidgetStateService;

  protected void hide(Widget widget) {
    log.debug("Hiding widget {}", WidgetUtils.debugWidgetString(widget));
    getState().setSlot(widget); // Will be hidden by onBeforeRender
  }

  @Subscribe
  public void onBeforeRender(BeforeRender event) {
    ensureWidgetStateService.forceWidgetState(getState(), this::isNotHidden, widgetService::setAsHideOpacity);
  }

  protected boolean isNotHidden(Widget widget) {
    return widget.getOpacity() != widgetService.getHideOpacity() && !WidgetUtils.isEmpty(widget);
  }
}
