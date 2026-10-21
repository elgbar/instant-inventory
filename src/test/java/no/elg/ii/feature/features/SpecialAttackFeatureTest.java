/*
 * Copyright (c) 2026 Elg
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.gameval.InterfaceID.CombatInterface;
import net.runelite.api.gameval.InterfaceID.Orbs;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.widgets.Widget;
import no.elg.ii.InstantInventoryConfig;
import no.elg.ii.service.VarService;
import no.elg.ii.test.TestSetup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class SpecialAttackFeatureTest {

  private static final int ACTIVE = Color.YELLOW.getRGB();
  private static final int INACTIVE = 0x000010;
  private static final int OFF = 0;
  private static final int MAINHAND = 1;
  private static final int OFFHAND = 2;
  private static final int TOGGLE_SPEC_ORB_SCRIPT_ID = 2793;

  /**
   * The widgets the client knows about, looked up by {@link net.runelite.api.Client#getWidget(int)}
   */
  private final Map<Integer, Widget> widgets = new HashMap<>();

  @Test
  public void clicking_the_bar_when_off_activates_it() {
    SpecialAttackFeature feature = singleBarFeature(OFF, Integer.MAX_VALUE);

    feature.onMenuOptionClicked(click(CombatInterface.SPECIAL_ATTACK));

    assertEquals(ACTIVE, lastColor(CombatInterface.SP_INDICATOR));
    verify(widgets.get(Orbs.SPECENERGY_INDICATOR)).setSpriteId(SpriteID.OrbFiller._10);
  }

  @Test
  public void clicking_the_bar_when_on_deactivates_it() {
    SpecialAttackFeature feature = singleBarFeature(MAINHAND, Integer.MAX_VALUE);

    feature.onMenuOptionClicked(click(CombatInterface.SPECIAL_ATTACK));

    assertEquals(INACTIVE, lastColor(CombatInterface.SP_INDICATOR));
    verify(widgets.get(Orbs.SPECENERGY_INDICATOR)).setSpriteId(SpriteID.OrbFiller._9);
  }

  @Test
  public void clicking_the_bar_twice_in_a_tick_toggles_from_the_prediction() {
    SpecialAttackFeature feature = singleBarFeature(OFF, Integer.MAX_VALUE);

    feature.onMenuOptionClicked(click(CombatInterface.SPECIAL_ATTACK));
    feature.onMenuOptionClicked(click(CombatInterface.SPECIAL_ATTACK));

    assertEquals(List.of(ACTIVE, INACTIVE), colors(CombatInterface.SP_INDICATOR));
  }

  @Test
  public void clicking_the_bar_three_times_in_a_tick_ends_active() {
    SpecialAttackFeature feature = singleBarFeature(OFF, Integer.MAX_VALUE);

    feature.onMenuOptionClicked(click(CombatInterface.SPECIAL_ATTACK));
    feature.onMenuOptionClicked(click(CombatInterface.SPECIAL_ATTACK));
    feature.onMenuOptionClicked(click(CombatInterface.SPECIAL_ATTACK));

    assertEquals(List.of(ACTIVE, INACTIVE, ACTIVE), colors(CombatInterface.SP_INDICATOR));
  }

  @Test
  public void clicking_the_offhand_bar_switches_from_the_mainhand() {
    SpecialAttackFeature feature = dualBarFeature(MAINHAND);

    feature.onMenuOptionClicked(click(CombatInterface.SPECIAL_ATTACK_OFFHAND));

    assertEquals(INACTIVE, lastColor(CombatInterface.SP_INDICATOR_MAINHAND));
    assertEquals(ACTIVE, lastColor(CombatInterface.SP_INDICATOR_OFFHAND));
  }

  @Test
  public void clicking_the_active_offhand_bar_deactivates_both() {
    SpecialAttackFeature feature = dualBarFeature(OFFHAND);

    feature.onMenuOptionClicked(click(CombatInterface.SPECIAL_ATTACK_OFFHAND));

    assertEquals(INACTIVE, lastColor(CombatInterface.SP_INDICATOR_MAINHAND));
    assertEquals(INACTIVE, lastColor(CombatInterface.SP_INDICATOR_OFFHAND));
    verify(widgets.get(Orbs.SPECENERGY_INDICATOR)).setSpriteId(SpriteID.OrbFiller._9);
  }

  @Test
  public void clicking_the_mainhand_bar_switches_from_the_offhand() {
    SpecialAttackFeature feature = dualBarFeature(OFFHAND);

    feature.onMenuOptionClicked(click(CombatInterface.SPECIAL_ATTACK_MAINHAND));

    assertEquals(ACTIVE, lastColor(CombatInterface.SP_INDICATOR_MAINHAND));
    assertEquals(INACTIVE, lastColor(CombatInterface.SP_INDICATOR_OFFHAND));
  }

  @Test
  public void clicking_another_widget_does_nothing() {
    SpecialAttackFeature feature = newFeature(Integer.MAX_VALUE);
    Widget bar = widget(CombatInterface.SP_INDICATOR);

    feature.onMenuOptionClicked(click(CombatInterface.RETALIATE));

    verify(bar, never()).setTextColor(Mockito.anyInt());
  }

  @Test
  public void prediction_is_reapplied_after_the_game_redraws() {
    SpecialAttackFeature feature = singleBarFeature(OFF, Integer.MAX_VALUE);
    feature.onMenuOptionClicked(click(CombatInterface.SPECIAL_ATTACK));

    // Every frame, as the game may have redrawn the server state in between, e.g., when it answers an earlier click
    feature.onBeforeRender(new BeforeRender());
    feature.onBeforeRender(new BeforeRender());

    assertEquals(List.of(ACTIVE, ACTIVE, ACTIVE), colors(CombatInterface.SP_INDICATOR));
  }

  @Test
  public void prediction_is_kept_on_a_tick_before_min_changed_ms() {
    SpecialAttackFeature feature = singleBarFeature(OFF, Integer.MAX_VALUE);
    feature.onMenuOptionClicked(click(CombatInterface.SPECIAL_ATTACK));

    feature.onGameTick(new GameTick());
    feature.onBeforeRender(new BeforeRender());

    assertEquals(List.of(ACTIVE, ACTIVE), colors(CombatInterface.SP_INDICATOR));
  }

  @Test
  public void prediction_is_dropped_on_a_tick_after_min_changed_ms() {
    SpecialAttackFeature feature = singleBarFeature(OFF, 0);
    feature.onMenuOptionClicked(click(CombatInterface.SPECIAL_ATTACK));

    feature.onGameTick(new GameTick());
    feature.onBeforeRender(new BeforeRender());

    // Only the click colored the bar, after the tick the game's own drawing is left alone
    assertEquals(List.of(ACTIVE), colors(CombatInterface.SP_INDICATOR));
  }

  @Test
  public void click_after_the_prediction_is_dropped_toggles_from_the_server_state() {
    SpecialAttackFeature feature = singleBarFeature(OFF, 0);
    feature.onMenuOptionClicked(click(CombatInterface.SPECIAL_ATTACK));
    feature.onGameTick(new GameTick());

    // The server rejected the click, so it is still off
    feature.onMenuOptionClicked(click(CombatInterface.SPECIAL_ATTACK));

    assertEquals(List.of(ACTIVE, ACTIVE), colors(CombatInterface.SP_INDICATOR));
  }

  @Test
  public void orb_click_colors_the_bar_from_the_clientside_value() {
    SpecialAttackFeature feature = singleBarFeature(MAINHAND, Integer.MAX_VALUE);

    // The orb script has already written SA_ATTACK clientside
    feature.onScriptPostFired(new ScriptPostFired(TOGGLE_SPEC_ORB_SCRIPT_ID));

    assertEquals(ACTIVE, lastColor(CombatInterface.SP_INDICATOR));
  }

  @Test
  public void bar_click_after_orb_click_toggles_from_the_orb_state() {
    SpecialAttackFeature feature = singleBarFeature(MAINHAND, Integer.MAX_VALUE);
    feature.onScriptPostFired(new ScriptPostFired(TOGGLE_SPEC_ORB_SCRIPT_ID));

    feature.onMenuOptionClicked(click(CombatInterface.SPECIAL_ATTACK));

    assertEquals(INACTIVE, lastColor(CombatInterface.SP_INDICATOR));
  }

  @Test
  public void other_scripts_are_ignored() {
    SpecialAttackFeature feature = newFeature(Integer.MAX_VALUE);
    Widget bar = widget(CombatInterface.SP_INDICATOR);

    feature.onScriptPostFired(new ScriptPostFired(TOGGLE_SPEC_ORB_SCRIPT_ID + 1));

    verify(bar, never()).setTextColor(Mockito.anyInt());
  }

  /**
   * One spec weapon: the plain bar is shown, the mainhand and offhand bars are hidden
   */
  private SpecialAttackFeature singleBarFeature(int saAttack, int minChangedMs) {
    SpecialAttackFeature feature = newFeature(minChangedMs);
    doReturn(saAttack).when(feature.varService).varpValue(VarPlayerID.SA_ATTACK);
    widget(CombatInterface.SP_ATTACKBARS_CONTAINER);
    widget(CombatInterface.SP_INDICATOR);
    hiddenWidget(CombatInterface.SP_INDICATOR_MAINHAND);
    hiddenWidget(CombatInterface.SP_INDICATOR_OFFHAND);
    widget(Orbs.SPECENERGY_INDICATOR);
    return feature;
  }

  /**
   * A spec weapon and a spec offhand, e.g., a rondache: the plain bar is hidden, the mainhand and offhand bars are shown
   */
  private SpecialAttackFeature dualBarFeature(int saAttack) {
    SpecialAttackFeature feature = newFeature(Integer.MAX_VALUE);
    doReturn(saAttack).when(feature.varService).varpValue(VarPlayerID.SA_ATTACK);
    widget(CombatInterface.SP_ATTACKBARS_CONTAINER);
    hiddenWidget(CombatInterface.SP_INDICATOR);
    widget(CombatInterface.SP_INDICATOR_MAINHAND);
    widget(CombatInterface.SP_INDICATOR_OFFHAND);
    widget(Orbs.SPECENERGY_INDICATOR);
    return feature;
  }

  private SpecialAttackFeature newFeature(int minChangedMs) {
    SpecialAttackFeature feature = TestSetup.createNewSpecFeature();
    feature.client = TestSetup.mockClient(invocation -> {
      if ("getWidget".equals(invocation.getMethod().getName()) && invocation.getArguments().length == 1) {
        return widgets.get(invocation.<Integer>getArgument(0));
      }
      return Mockito.RETURNS_DEFAULTS.answer(invocation);
    });
    feature.varService = mock(VarService.class);
    feature.config = new InstantInventoryConfig() {
      @Override
      public int minChangedMs() {
        return minChangedMs;
      }
    };
    return feature;
  }

  private Widget widget(int id) {
    Widget widget = mock(Widget.class);
    widgets.put(id, widget);
    return widget;
  }

  private void hiddenWidget(int id) {
    doReturn(true).when(widget(id)).isHidden();
  }

  private static MenuOptionClicked click(int widgetId) {
    Widget clicked = mock(Widget.class);
    doReturn(widgetId).when(clicked).getId();
    MenuEntry menuEntry = mock(MenuEntry.class);
    doReturn(clicked).when(menuEntry).getWidget();
    return new MenuOptionClicked(menuEntry);
  }

  /**
   * @return Every color the plugin gave the widget, in order
   */
  private List<Integer> colors(int id) {
    ArgumentCaptor<Integer> color = ArgumentCaptor.forClass(Integer.class);
    verify(widgets.get(id), atLeastOnce()).setTextColor(color.capture());
    return color.getAllValues();
  }

  private int lastColor(int id) {
    List<Integer> colors = colors(id);
    return colors.get(colors.size() - 1);
  }
}
