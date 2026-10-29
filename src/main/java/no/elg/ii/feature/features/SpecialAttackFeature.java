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
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
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
  private static final int SPEC_INACTIVE_COLOR = 0x000010;
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
  Client client;

  @Inject
  @VisibleForTesting
  VarService varService;

  @Inject
  @VisibleForTesting
  InstantInventoryConfig config;

  /**
   * Predicted SA_ATTACK after the last spec click, or {@code null} when the server value should be trusted.
   * Needed so multiple clicks within one tick toggle from the predicted state, and so a server update for an earlier
   * click does not flicker the bar and orb back to an outdated state
   */
  @Nullable
  private Integer predictedSpecState;
  private long lastManuallyModified;

  /* (non-javadoc)
   * Uses the ScriptPostFired event because the SA_ATTACK varbit is updated by the TOGGLE_QUICK_SPECIAL_ATTACK_SCRIPT_ID.
   * And we must know the state to update it properly
   */
  @Subscribe
  public void onScriptPostFired(final ScriptPostFired event) {
    assert client.isClientThread();
    if (event.getScriptId() == TOGGLE_QUICK_SPECIAL_ATTACK_SCRIPT_ID) {
      // The orb writes SA_ATTACK clientside, but a server update for an earlier click may still overwrite it
      predict(varService.varpValue(VarPlayerID.SA_ATTACK));
    }
  }

  @Subscribe
  public void onBeforeRender(final BeforeRender event) {
    assert client.isClientThread();
    // The game redraws the bar and orb whenever the server updates SA_ATTACK, so keep re-applying the prediction
    if (predictedSpecState != null) {
      updateSpecBar(predictedSpecState);
      updateSpecOrb(predictedSpecState);
    }
  }

  @Subscribe
  public void onGameTick(final GameTick event) {
    assert client.isClientThread();
    if (predictedSpecState != null && System.currentTimeMillis() - lastManuallyModified >= config.minChangedMs()) {
      // Stop overriding, the game has already drawn what the server says
      predictedSpecState = null;
    }
  }

  private void predict(int newSpecState) {
    predictedSpecState = newSpecState;
    lastManuallyModified = System.currentTimeMillis();
    log.debug("Updating spec state to {}", debugSpecState(newSpecState));
    updateSpecBar(newSpecState);
    updateSpecOrb(newSpecState);
  }

  private int currentSpecState() {
    return predictedSpecState != null ? predictedSpecState : varService.varpValue(VarPlayerID.SA_ATTACK);
  }

  @Subscribe
  public void onMenuOptionClicked(final MenuOptionClicked event) {
    assert client.isClientThread();
    Widget widget = event.getWidget();
    if (widget != null) {
      int hand;
      int id = widget.getId();
      if (id == InterfaceID.CombatInterface.SPECIAL_ATTACK_OFFHAND || (id == InterfaceID.CombatInterface.SPECIAL_ATTACK && hasRondacheEquipped())) {
        hand = SPEC_STATE_OFFHAND;
      } else if (id == InterfaceID.CombatInterface.SPECIAL_ATTACK || id == InterfaceID.CombatInterface.SPECIAL_ATTACK_MAINHAND) {
        hand = SPEC_STATE_MAINHAND;
      } else {
        return;
      }
      log.debug("Bar clicked {}", debugSpecState(hand));
      // Bars are resolved server side, so predict: clicking the active hand turns it off, otherwise switches to it
      predict(currentSpecState() == hand ? SPEC_STATE_OFF : hand);
    }
  }

  private void updateSpecOrb(int newSpecState) {
    assert client.isClientThread();
    Widget specWidget = client.getWidget(InterfaceID.Orbs.SPECENERGY_INDICATOR);
    if (specWidget != null) {
      boolean active = newSpecState == SPEC_STATE_MAINHAND
        || newSpecState == SPEC_STATE_OFFHAND && canUseRondache() && hasRondacheEquipped();
      specWidget.setSpriteId(active ? SPRITE_ID_SPEC_ORB_FILLER_ACTIVE : SPRITE_ID_SPEC_ORB_FILLER_INACTIVE);
    }
  }

  private void updateSpecBar(int newSpecState) {
    assert client.isClientThread();
    Widget parentSpecWidget = client.getWidget(InterfaceID.CombatInterface.SP_ATTACKBARS_CONTAINER);
    if (parentSpecWidget != null && !parentSpecWidget.isHidden()) {
      if (newSpecState == SPEC_STATE_OFF) {
        colorSpecBar(InterfaceID.CombatInterface.SP_INDICATOR, SPEC_INACTIVE_COLOR);
        colorSpecBar(InterfaceID.CombatInterface.SP_INDICATOR_MAINHAND, SPEC_INACTIVE_COLOR);
        colorSpecBar(InterfaceID.CombatInterface.SP_INDICATOR_OFFHAND, SPEC_INACTIVE_COLOR);
      } else if (newSpecState == SPEC_STATE_MAINHAND) {
        colorSpecBar(InterfaceID.CombatInterface.SP_INDICATOR, SPEC_ACTIVE_COLOR);
        colorSpecBar(InterfaceID.CombatInterface.SP_INDICATOR_MAINHAND, SPEC_ACTIVE_COLOR);
        colorSpecBar(InterfaceID.CombatInterface.SP_INDICATOR_OFFHAND, SPEC_INACTIVE_COLOR);
      } else if (newSpecState == SPEC_STATE_OFFHAND) {
        colorSpecBar(InterfaceID.CombatInterface.SP_INDICATOR_MAINHAND, SPEC_INACTIVE_COLOR);
        if (canUseRondache() && hasRondacheEquipped()) {
          colorSpecBar(InterfaceID.CombatInterface.SP_INDICATOR, SPEC_ACTIVE_COLOR);
          colorSpecBar(InterfaceID.CombatInterface.SP_INDICATOR_OFFHAND, SPEC_ACTIVE_COLOR);
        }
      }
    }
  }

  private void colorSpecBar(int interfaceId, int color) {
    Widget specWidget = client.getWidget(interfaceId);
    if (specWidget != null && !specWidget.isHidden()) {
      specWidget.setTextColor(color);
    }
  }

  /**
   * @return whether we have Rondache equipped and have enough changes.
   */
  public boolean canUseRondache() {
    return varService.varbitValue(VarbitID.RONDACHE_CHARGES) >= 10;
  }

  public boolean hasRondacheEquipped() {
    ItemContainer worn = client.getItemContainer(InventoryID.WORN);
    if (worn == null) {
      return false;
    }
    Item shieldEquipped = worn.getItem(EquipmentInventorySlot.SHIELD.getSlotIdx());
    return shieldEquipped != null && shieldEquipped.getId() == ItemID.RONDACHE;
  }

  @Override
  public @NonNull String getConfigKey() {
    return SPEC_CONFIG_KEY;
  }

  @Override
  public boolean isEnabledInConfig() {
    return config.instantSpec();
  }

  @NonNull
  public static String debugSpecState(int specState) {
    switch (specState) {
      case SPEC_STATE_OFF:
        return "Off";
      case SPEC_STATE_MAINHAND:
        return "Mainhand";
      case SPEC_STATE_OFFHAND:
        return "Offhand";
      default:
        return "Unknown";
    }
  }

  private final static int SPEC_STATE_OFF = 0;
  private final static int SPEC_STATE_MAINHAND = 1;
  private final static int SPEC_STATE_OFFHAND = 2;
}
