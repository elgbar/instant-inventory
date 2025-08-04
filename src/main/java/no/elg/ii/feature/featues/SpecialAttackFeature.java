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

package no.elg.ii.feature.featues;

import com.google.common.annotations.VisibleForTesting;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import no.elg.ii.feature.Feature;
import no.elg.ii.feature.state.InventoryState;
import no.elg.ii.service.VarService;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.Color;


@Slf4j
@Singleton
@NoArgsConstructor
public class SpecialAttackFeature implements Feature {

  public static final String SPEC_CONFIG_KEY = "instantSpec";
  private static final int SPEC_ACTIVE_COLOR = Color.YELLOW.getRGB();
  private static final int SPEC_INACTIVE_COLOR = Color.BLACK.getRGB();

  @Inject
  @VisibleForTesting
  Client client;
  @Inject
  @VisibleForTesting
  ClientThread clientThread;

  @Inject
  private VarService varService;

  @Inject
  @Getter
  private InventoryState state;

  private int weaponSpec;

  @Subscribe
  public void onScriptPostFired(ScriptPostFired event) {
    if (event.getScriptId() == 3648) {
      weaponSpec = client.getIntStack()[client.getIntStackSize()-1];
    }
  }

  @Subscribe
  public void onMenuOptionClicked(final MenuOptionClicked event) {
    Widget widget = event.getWidget();
    if (widget != null) {
      String menuOption = event.getMenuOption();
      if (menuOption.contains("Use") && menuOption.contains("Special Attack")) {
        clientThread.invokeAtTickEnd(this::highlightSpec);
      }
    }
  }

  public void highlightSpec() {
    Widget specWidget = client.getWidget(InterfaceID.CombatInterface.SP_INDICATOR);
    if (specWidget != null) {
      if (varService.isVarpTrue(VarPlayerID.SA_ATTACK)) {
        // was enabled, mark as disabled
        specWidget.setTextColor(SPEC_INACTIVE_COLOR);
      } else {
        // was disabled, mark as enabled
        int currentSpec = varService.varpValue(VarPlayerID.SA_ENERGY);
        if (currentSpec >= weaponSpec) {
          specWidget.setTextColor(SPEC_ACTIVE_COLOR);
        }
      }
    }
  }

  @Override
  public @NonNull String getConfigKey() {
    return SPEC_CONFIG_KEY;
  }
}
