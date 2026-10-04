/*
 * Copyright (c) 2023-2026 Elg
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

package no.elg.ii.feature.features;

import com.google.common.annotations.VisibleForTesting;
import java.awt.Color;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;
import no.elg.ii.InstantInventoryConfig;
import no.elg.ii.feature.StatelessFeature;
import no.elg.ii.service.VarService;


@Slf4j
@Singleton
@NoArgsConstructor
public class SpecialAttackFeature implements StatelessFeature {

  public static final String SPEC_CONFIG_KEY = "instantSpec";
  private static final int SPEC_ACTIVE_COLOR = Color.YELLOW.getRGB();
  private static final int SPEC_INACTIVE_COLOR = Color.BLACK.getRGB();
  /**
   * @see net.runelite.api.SpriteID#MINIMAP_ORB_SPECIAL
   */
  private static final int SPRITE_ID_SPEC_ORB_FILLER_INACTIVE = SpriteID.OrbFiller._9;
  /**
   * @see net.runelite.api.SpriteID#MINIMAP_ORB_SPECIAL_ACTIVATED
   */
  private static final int SPRITE_ID_SPEC_ORB_FILLER_ACTIVE = SpriteID.OrbFiller._10;

  private static final int TOGGLE_QUICK_SPECIAL_ATTACK_SCRIPT_ID = 2793;

  @Inject
  @VisibleForTesting
  private Client client;

  @Inject
  private VarService varService;
  @Inject
  private InstantInventoryConfig config;

  /* (non-javadoc)
   * Uses the ScriptPreFired event because the SA_ATTACK varbit is updated by the TOGGLE_QUICK_PRAYER_SCRIPT_ID
   */
  @Subscribe
  public void onScriptPreFired(final ScriptPreFired event) {
    assert client.isClientThread();
    if (event.getScriptId() == TOGGLE_QUICK_SPECIAL_ATTACK_SCRIPT_ID) {
      //No need to call updateSpecOrb as it is updated clientside from before
      updateSpecBar();
    }
  }

  @Subscribe
  public void onMenuOptionClicked(final MenuOptionClicked event) {
    assert client.isClientThread();
    Widget widget = event.getWidget();
    if (widget != null) {
      String menuOption = event.getMenuOption();
      if (menuOption.contains("Use") && menuOption.contains("Special Attack")) {
        updateSpecBar();
        updateSpecOrb();
      }
    }
  }

  private void updateSpecOrb() {
    assert client.isClientThread();
    Widget specWidget = client.getWidget(InterfaceID.Orbs.SPECENERGY_INDICATOR);
    if (specWidget != null) {
      if (varService.isVarpTrue(VarPlayerID.SA_ATTACK)) {
        // was enabled, mark as disabled
        specWidget.setSpriteId(SPRITE_ID_SPEC_ORB_FILLER_INACTIVE);
      } else {
        // was disabled, mark as enabled
        specWidget.setSpriteId(SPRITE_ID_SPEC_ORB_FILLER_ACTIVE);
      }
    }
  }

  private void updateSpecBar() {
    assert client.isClientThread();
    Widget specWidget = client.getWidget(InterfaceID.CombatInterface.SP_INDICATOR);
    if (specWidget != null) {
      if (varService.isVarpTrue(VarPlayerID.SA_ATTACK)) {
        // was enabled, mark as disabled
        specWidget.setTextColor(SPEC_INACTIVE_COLOR);
      } else {
        // was disabled, mark as enabled
        specWidget.setTextColor(SPEC_ACTIVE_COLOR);
      }
    }
  }

  @Override
  public @NonNull String getConfigKey() {
    return SPEC_CONFIG_KEY;
  }
}
