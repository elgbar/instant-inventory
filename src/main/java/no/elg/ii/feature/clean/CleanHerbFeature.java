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
package no.elg.ii.feature.clean;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.OverlayManager;
import no.elg.ii.feature.Feature;
import no.elg.ii.InventoryState;

@Singleton
public class CleanHerbFeature implements Feature {

  public static final String CLEAN_OPTION = "Clean";
  public static final String CLEAN_CONFIG_KEY = "instantClean";

  @Inject
  @VisibleForTesting
  public CleanHerbOverlay overlay;
  @Inject
  @VisibleForTesting
  public OverlayManager overlayManager;
  @Inject
  @VisibleForTesting
  public Client client;

  @Inject
  private InventoryState state;

  @Override
  public void onEnable() {
    overlayManager.add(overlay);
  }

  @Override
  public void onDisable() {
    overlayManager.remove(overlay);
  }

  @Override
  public void reset() {
    Feature.super.reset();
    overlay.invalidateCache();
  }

  @Subscribe
  public void onMenuOptionClicked(final MenuOptionClicked event) {
    Widget widget = event.getWidget();
    if (widget != null) {
      String menuOption = event.getMenuOption();
      if (CLEAN_OPTION.equals(menuOption)) {
        int itemId = event.getItemId();
        HerbInfo herbInfo = HerbInfo.HERBS.get(itemId);
        if (herbInfo == null) {
          return;
        }

        int herbloreLevel = client.getBoostedSkillLevel(Skill.HERBLORE);
        if (herbloreLevel >= herbInfo.getMinLevel()) {
          getState().setItemId(widget.getIndex(), itemId);
        }
      }
    }
  }

  @Override
  @Nonnull
  public InventoryState getState() {
    return state;
  }

  @Nonnull
  @Override
  public String getConfigKey() {
    return CLEAN_CONFIG_KEY;
  }
}
