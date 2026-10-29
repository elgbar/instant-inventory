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

import static net.runelite.api.gameval.ItemID.BRONZE_SWORD;
import static net.runelite.api.gameval.ItemID.DRAGON_DAGGER;
import static net.runelite.api.gameval.ItemID.RONDACHE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.EnumComposition;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.gameval.InterfaceID.CombatInterface;
import net.runelite.api.gameval.InterfaceID.Orbs;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
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
   * Item id to its special attack cost, as read by script3648
   */
  private static final int SPEC_COST_ENUM_ID = 906;
  private static final int RONDACHE_SPEC_COST = 10;
  private static final Map<Integer, Integer> SPEC_COSTS = Map.of(DRAGON_DAGGER, 250, RONDACHE, RONDACHE_SPEC_COST);

  /**
   * The widgets the client knows about, looked up by {@link net.runelite.api.Client#getWidget(int)}
   */
  private final Map<Integer, Widget> widgets = new HashMap<>();
  /**
   * What the server says is worn, by equipment slot
   */
  private final Map<Integer, Item> worn = new HashMap<>();
  /**
   * The varps the client knows about, unknown ones are zero as in the game
   */
  private final Map<Integer, Integer> varps = new HashMap<>();
  /**
   * The varbits the client knows about, unknown ones are zero as in the game
   */
  private final Map<Integer, Integer> varbits = new HashMap<>();

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
  public void clicking_the_bar_with_only_a_rondache_activates_it() {
    SpecialAttackFeature feature = rondacheBarFeature(OFF, RONDACHE_SPEC_COST);

    feature.onMenuOptionClicked(click(CombatInterface.SPECIAL_ATTACK));

    assertEquals(ACTIVE, lastColor(CombatInterface.SP_INDICATOR));
    verify(widgets.get(Orbs.SPECENERGY_INDICATOR)).setSpriteId(SpriteID.OrbFiller._10);
  }

  @Test
  public void clicking_the_bar_with_only_an_active_rondache_deactivates_it() {
    // With only a spec offhand the plain bar toggles the offhand
    SpecialAttackFeature feature = rondacheBarFeature(OFFHAND, RONDACHE_SPEC_COST);

    feature.onMenuOptionClicked(click(CombatInterface.SPECIAL_ATTACK));

    assertEquals(INACTIVE, lastColor(CombatInterface.SP_INDICATOR));
    verify(widgets.get(Orbs.SPECENERGY_INDICATOR)).setSpriteId(SpriteID.OrbFiller._9);
  }

  @Test
  public void clicking_the_bar_with_only_a_rondache_without_enough_charges_leaves_it_inactive() {
    SpecialAttackFeature feature = rondacheBarFeature(OFF, RONDACHE_SPEC_COST - 1);

    feature.onMenuOptionClicked(click(CombatInterface.SPECIAL_ATTACK));

    verify(widgets.get(CombatInterface.SP_INDICATOR), never()).setTextColor(ACTIVE);
  }

  @Test
  public void clicking_the_bar_with_only_a_rondache_without_enough_charges_leaves_the_orb_inactive() {
    SpecialAttackFeature feature = rondacheBarFeature(OFF, RONDACHE_SPEC_COST - 1);

    feature.onMenuOptionClicked(click(CombatInterface.SPECIAL_ATTACK));

    verify(widgets.get(Orbs.SPECENERGY_INDICATOR), never()).setSpriteId(SpriteID.OrbFiller._10);
  }

  @Test
  public void orb_click_with_only_a_rondache_colors_the_bar_from_the_clientside_value() {
    // The orb script has already written SA_ATTACK clientside, the offhand as it is the only spec item
    SpecialAttackFeature feature = rondacheBarFeature(OFFHAND, RONDACHE_SPEC_COST);

    feature.onScriptPostFired(new ScriptPostFired(TOGGLE_SPEC_ORB_SCRIPT_ID));

    assertEquals(ACTIVE, lastColor(CombatInterface.SP_INDICATOR));
  }

  @Test
  public void clicking_the_offhand_bar_without_enough_rondache_charges_leaves_it_inactive() {
    SpecialAttackFeature feature = dualBarFeature(OFF, RONDACHE_SPEC_COST - 1);

    feature.onMenuOptionClicked(click(CombatInterface.SPECIAL_ATTACK_OFFHAND));

    verify(widgets.get(CombatInterface.SP_INDICATOR_OFFHAND), never()).setTextColor(ACTIVE);
  }

  @Test
  public void clicking_the_mainhand_bar_without_enough_rondache_charges_activates_it() {
    SpecialAttackFeature feature = dualBarFeature(OFF, RONDACHE_SPEC_COST - 1);

    feature.onMenuOptionClicked(click(CombatInterface.SPECIAL_ATTACK_MAINHAND));

    assertEquals(ACTIVE, lastColor(CombatInterface.SP_INDICATOR_MAINHAND));
    verify(widgets.get(Orbs.SPECENERGY_INDICATOR)).setSpriteId(SpriteID.OrbFiller._10);
  }

  @Test
  public void clicking_the_offhand_bar_switches_from_the_mainhand() {
    SpecialAttackFeature feature = dualBarFeature(MAINHAND, RONDACHE_SPEC_COST);

    feature.onMenuOptionClicked(click(CombatInterface.SPECIAL_ATTACK_OFFHAND));

    assertEquals(INACTIVE, lastColor(CombatInterface.SP_INDICATOR_MAINHAND));
    assertEquals(ACTIVE, lastColor(CombatInterface.SP_INDICATOR_OFFHAND));
  }

  @Test
  public void clicking_the_active_offhand_bar_deactivates_both() {
    SpecialAttackFeature feature = dualBarFeature(OFFHAND, RONDACHE_SPEC_COST);

    feature.onMenuOptionClicked(click(CombatInterface.SPECIAL_ATTACK_OFFHAND));

    assertEquals(INACTIVE, lastColor(CombatInterface.SP_INDICATOR_MAINHAND));
    assertEquals(INACTIVE, lastColor(CombatInterface.SP_INDICATOR_OFFHAND));
    verify(widgets.get(Orbs.SPECENERGY_INDICATOR)).setSpriteId(SpriteID.OrbFiller._9);
  }

  @Test
  public void clicking_the_mainhand_bar_switches_from_the_offhand() {
    SpecialAttackFeature feature = dualBarFeature(OFFHAND, RONDACHE_SPEC_COST);

    feature.onMenuOptionClicked(click(CombatInterface.SPECIAL_ATTACK_MAINHAND));

    assertEquals(ACTIVE, lastColor(CombatInterface.SP_INDICATOR_MAINHAND));
    assertEquals(INACTIVE, lastColor(CombatInterface.SP_INDICATOR_OFFHAND));
  }

  @Test
  public void clicking_the_offhand_bar_moves_the_active_orb_from_the_mainhand() {
    SpecialAttackFeature feature = dualBarFeature(MAINHAND, RONDACHE_SPEC_COST);

    feature.onMenuOptionClicked(click(CombatInterface.SPECIAL_ATTACK_OFFHAND));

    assertEquals(SpriteID.OrbFiller._9, sprite(CombatInterface.SP_ORB_MAINHAND));
    assertEquals(SpriteID.OrbFiller._10, sprite(CombatInterface.SP_ORB_OFFHAND));
  }

  @Test
  public void clicking_the_active_offhand_bar_deactivates_its_orb() {
    SpecialAttackFeature feature = dualBarFeature(OFFHAND, RONDACHE_SPEC_COST);

    feature.onMenuOptionClicked(click(CombatInterface.SPECIAL_ATTACK_OFFHAND));

    assertEquals(SpriteID.OrbFiller._9, sprite(CombatInterface.SP_ORB_MAINHAND));
    assertEquals(SpriteID.OrbFiller._9, sprite(CombatInterface.SP_ORB_OFFHAND));
  }

  @Test
  public void clicking_the_offhand_bar_without_enough_rondache_charges_keeps_its_empty_orb() {
    SpecialAttackFeature feature = dualBarFeature(OFF, RONDACHE_SPEC_COST - 1);

    feature.onMenuOptionClicked(click(CombatInterface.SPECIAL_ATTACK_OFFHAND));

    assertEquals(SpriteID.OrbFiller._16, sprite(CombatInterface.SP_ORB_OFFHAND));
  }

  @Test
  public void clicking_the_mainhand_bar_keeps_the_empty_orb_of_a_rondache_without_enough_charges() {
    SpecialAttackFeature feature = dualBarFeature(OFF, RONDACHE_SPEC_COST - 1);

    feature.onMenuOptionClicked(click(CombatInterface.SPECIAL_ATTACK_MAINHAND));

    assertEquals(SpriteID.OrbFiller._10, sprite(CombatInterface.SP_ORB_MAINHAND));
    // The game only redraws it when a var changes, so the inactive sprite could stick
    assertEquals(SpriteID.OrbFiller._16, sprite(CombatInterface.SP_ORB_OFFHAND));
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
    varps.put(VarPlayerID.SA_ATTACK, saAttack);
    worn.put(EquipmentInventorySlot.WEAPON.getSlotIdx(), new Item(DRAGON_DAGGER, 1));
    plainBarWidgets();
    return feature;
  }

  /**
   * A spec offhand without a spec weapon: the plain bar is shown for the rondache, the mainhand and offhand bars are
   * hidden
   */
  private SpecialAttackFeature rondacheBarFeature(int saAttack, int rondacheCharges) {
    SpecialAttackFeature feature = newFeature(Integer.MAX_VALUE);
    varps.put(VarPlayerID.SA_ATTACK, saAttack);
    varbits.put(VarbitID.RONDACHE_CHARGES, rondacheCharges);
    worn.put(EquipmentInventorySlot.WEAPON.getSlotIdx(), new Item(BRONZE_SWORD, 1));
    worn.put(EquipmentInventorySlot.SHIELD.getSlotIdx(), new Item(RONDACHE, 1));
    plainBarWidgets();
    return feature;
  }

  /**
   * A spec weapon and a spec offhand, e.g., a rondache: the plain bar is hidden, the mainhand and offhand bars are shown
   */
  private SpecialAttackFeature dualBarFeature(int saAttack, int rondacheCharges) {
    SpecialAttackFeature feature = newFeature(Integer.MAX_VALUE);
    varps.put(VarPlayerID.SA_ATTACK, saAttack);
    varbits.put(VarbitID.RONDACHE_CHARGES, rondacheCharges);
    worn.put(EquipmentInventorySlot.WEAPON.getSlotIdx(), new Item(DRAGON_DAGGER, 1));
    worn.put(EquipmentInventorySlot.SHIELD.getSlotIdx(), new Item(RONDACHE, 1));
    widget(CombatInterface.SP_ATTACKBARS_CONTAINER);
    hiddenWidget(CombatInterface.SP_INDICATOR);
    widget(CombatInterface.SP_INDICATOR_MAINHAND);
    widget(CombatInterface.SP_INDICATOR_OFFHAND);
    // As script10148 draws them, with full special attack energy
    orbWidget(CombatInterface.SP_ORB_MAINHAND, saAttack == MAINHAND ? SpriteID.OrbFiller._10 : SpriteID.OrbFiller._9);
    orbWidget(CombatInterface.SP_ORB_OFFHAND, rondacheCharges < RONDACHE_SPEC_COST
      ? SpriteID.OrbFiller._16
      : saAttack == OFFHAND ? SpriteID.OrbFiller._10 : SpriteID.OrbFiller._9);
    widget(Orbs.SPECENERGY_INDICATOR);
    return feature;
  }

  private void plainBarWidgets() {
    widget(CombatInterface.SP_ATTACKBARS_CONTAINER);
    widget(CombatInterface.SP_INDICATOR);
    hiddenWidget(CombatInterface.SP_INDICATOR_MAINHAND);
    hiddenWidget(CombatInterface.SP_INDICATOR_OFFHAND);
    hiddenWidget(CombatInterface.SP_ORB_MAINHAND);
    hiddenWidget(CombatInterface.SP_ORB_OFFHAND);
    widget(Orbs.SPECENERGY_INDICATOR);
  }

  private SpecialAttackFeature newFeature(int minChangedMs) {
    SpecialAttackFeature feature = TestSetup.createNewSpecFeature();
    ItemContainer wornContainer = mock(ItemContainer.class, withSettings().defaultAnswer(invocation ->
      "getItem".equals(invocation.getMethod().getName())
        ? worn.get(invocation.<Integer>getArgument(0))
        : Mockito.RETURNS_DEFAULTS.answer(invocation)));
    // script3648 clamps the negative default of an item without a special attack to zero
    EnumComposition specCosts = mock(EnumComposition.class, withSettings().defaultAnswer(invocation ->
      "getIntValue".equals(invocation.getMethod().getName())
        ? SPEC_COSTS.getOrDefault(invocation.<Integer>getArgument(0), -1)
        : Mockito.RETURNS_DEFAULTS.answer(invocation)));
    feature.client = TestSetup.mockClient(invocation -> {
      String method = invocation.getMethod().getName();
      if ("getWidget".equals(method) && invocation.getArguments().length == 1) {
        return widgets.get(invocation.<Integer>getArgument(0));
      }
      if ("getItemContainer".equals(method) && invocation.getArgument(0).equals(InventoryID.WORN)) {
        return wornContainer;
      }
      if ("getEnum".equals(method) && invocation.<Integer>getArgument(0) == SPEC_COST_ENUM_ID) {
        return specCosts;
      }
      return Mockito.RETURNS_DEFAULTS.answer(invocation);
    });
    feature.varService = new VarService() {
      @Override
      public int varpValue(int varp) {
        return varps.getOrDefault(varp, 0);
      }

      @Override
      public int varbitValue(int varbit) {
        return varbits.getOrDefault(varbit, 0);
      }
    };
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

  /**
   * Hidden in the double itself, as which bars get colored depends on the state and charges
   */
  private void hiddenWidget(int id) {
    widgets.put(id, mock(Widget.class, withSettings().defaultAnswer(invocation ->
      "isHidden".equals(invocation.getMethod().getName()) ? true : Mockito.RETURNS_DEFAULTS.answer(invocation))));
  }

  /**
   * An orb of the split bar, which keeps the sprite it was last given as a widget does
   */
  private void orbWidget(int id, int spriteId) {
    int[] sprite = {spriteId};
    widgets.put(id, mock(Widget.class, withSettings().defaultAnswer(invocation -> {
      switch (invocation.getMethod().getName()) {
        case "getSpriteId":
          return sprite[0];
        case "setSpriteId":
          sprite[0] = invocation.getArgument(0);
          return invocation.getMock();
        default:
          return Mockito.RETURNS_DEFAULTS.answer(invocation);
      }
    })));
  }

  private int sprite(int id) {
    return widgets.get(id).getSpriteId();
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
