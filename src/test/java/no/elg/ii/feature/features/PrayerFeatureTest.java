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

import static net.runelite.api.Prayer.BURST_OF_STRENGTH;
import static net.runelite.api.Prayer.CLARITY_OF_THOUGHT;
import static net.runelite.api.Prayer.DEADEYE;
import static net.runelite.api.Prayer.EAGLE_EYE;
import static net.runelite.api.Prayer.INCREDIBLE_REFLEXES;
import static net.runelite.api.Prayer.MYSTIC_MIGHT;
import static net.runelite.api.Prayer.MYSTIC_VIGOUR;
import static net.runelite.api.Prayer.PIETY;
import static net.runelite.api.Prayer.PRESERVE;
import static net.runelite.api.Prayer.PROTECT_FROM_MAGIC;
import static net.runelite.api.Prayer.PROTECT_FROM_MELEE;
import static net.runelite.api.Prayer.PROTECT_FROM_MISSILES;
import static net.runelite.api.Prayer.PROTECT_ITEM;
import static net.runelite.api.Prayer.RAPID_HEAL;
import static net.runelite.api.Prayer.RIGOUR;
import static net.runelite.api.Prayer.ROCK_SKIN;
import static net.runelite.api.Prayer.SMITE;
import static net.runelite.api.Prayer.STEEL_SKIN;
import static net.runelite.api.Prayer.SUPERHUMAN_STRENGTH;
import static net.runelite.api.Prayer.THICK_SKIN;
import static net.runelite.api.Prayer.ULTIMATE_STRENGTH;
import static net.runelite.api.gameval.VarbitID.QUICKPRAYER_ACTIVE;
import static net.runelite.api.gameval.VarbitID.QUICKPRAYER_SELECTED;
import static no.elg.ii.model.PrayerInfo.INTERFACE_TO_BIT;
import static no.elg.ii.model.PrayerInfo.PRAYER_TO_BIT;
import static no.elg.ii.model.PrayerInfo.prayerToBits;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import net.runelite.api.Prayer;
import net.runelite.api.ScriptEvent;
import net.runelite.api.Skill;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.widgets.Widget;
import no.elg.ii.feature.state.PrayerState;
import no.elg.ii.service.VarService;
import no.elg.ii.test.TestSetup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class PrayerFeatureTest {

  private static final int TOGGLE_SINGLE_PRAYER_SCRIPT_ID = 462;
  private static final int TOGGLE_QUICK_PRAYER_SCRIPT_ID = 455;

  @Test
  public void enabling_a_prayer_with_none_active() {
    PrayerFeature feature = newFeature();

    feature.onScriptPreFired(click(PIETY));

    assertActive(feature, PIETY);
  }

  @Test
  public void enabling_an_overhead_replaces_the_active_overhead() {
    PrayerFeature feature = newFeature(PROTECT_FROM_MAGIC, PROTECT_ITEM);

    feature.onScriptPreFired(click(PROTECT_FROM_MELEE));

    assertActive(feature, PROTECT_FROM_MELEE, PROTECT_ITEM);
  }

  @Test
  public void smite_replaces_a_protection_prayer() {
    PrayerFeature feature = newFeature(PROTECT_FROM_MISSILES);

    feature.onScriptPreFired(click(SMITE));

    assertActive(feature, SMITE);
  }

  @Test
  public void enabling_a_skin_prayer_replaces_the_active_skin_prayer() {
    PrayerFeature feature = newFeature(THICK_SKIN, BURST_OF_STRENGTH);

    feature.onScriptPreFired(click(ROCK_SKIN));

    assertActive(feature, ROCK_SKIN, BURST_OF_STRENGTH);
  }

  @Test
  public void strength_and_attack_and_skin_prayers_stack() {
    PrayerFeature feature = newFeature(INCREDIBLE_REFLEXES, ROCK_SKIN);

    feature.onScriptPreFired(click(ULTIMATE_STRENGTH));

    assertActive(feature, ULTIMATE_STRENGTH, INCREDIBLE_REFLEXES, ROCK_SKIN);
  }

  @Test
  public void enabling_a_strength_prayer_replaces_the_active_strength_prayer() {
    PrayerFeature feature = newFeature(BURST_OF_STRENGTH, CLARITY_OF_THOUGHT);

    feature.onScriptPreFired(click(SUPERHUMAN_STRENGTH));

    assertActive(feature, SUPERHUMAN_STRENGTH, CLARITY_OF_THOUGHT);
  }

  @Test
  public void enabling_a_ranged_prayer_replaces_strength_and_attack_but_keeps_skin() {
    PrayerFeature feature = newFeature(STEEL_SKIN, ULTIMATE_STRENGTH, INCREDIBLE_REFLEXES);

    feature.onScriptPreFired(click(EAGLE_EYE));

    assertActive(feature, STEEL_SKIN, EAGLE_EYE);
  }

  @Test
  public void enabling_a_ranged_prayer_replaces_the_active_magic_prayer() {
    PrayerFeature feature = newFeature(MYSTIC_MIGHT, STEEL_SKIN);

    feature.onScriptPreFired(click(EAGLE_EYE));

    assertActive(feature, EAGLE_EYE, STEEL_SKIN);
  }

  @Test
  public void enabling_a_skin_prayer_keeps_the_ranged_prayer() {
    PrayerFeature feature = newFeature(THICK_SKIN, EAGLE_EYE);

    feature.onScriptPreFired(click(STEEL_SKIN));

    assertActive(feature, STEEL_SKIN, EAGLE_EYE);
  }

  @Test
  public void deadeye_does_not_conflict_with_skin_prayers() {
    PrayerFeature feature = newFeature(STEEL_SKIN);

    feature.onScriptPreFired(click(DEADEYE));

    // Deadeye is in Eagle Eye's slot and shares its bit
    assertActive(feature, STEEL_SKIN, EAGLE_EYE);
  }

  @Test
  public void mystic_vigour_does_not_conflict_with_skin_prayers() {
    PrayerFeature feature = newFeature(ROCK_SKIN);

    feature.onScriptPreFired(click(MYSTIC_VIGOUR));

    // Mystic Vigour is in Mystic Might's slot and shares its bit
    assertActive(feature, ROCK_SKIN, MYSTIC_MIGHT);
  }

  @Test
  public void enabling_a_strength_prayer_replaces_the_active_ranged_prayer() {
    PrayerFeature feature = newFeature(EAGLE_EYE);

    feature.onScriptPreFired(click(ULTIMATE_STRENGTH));

    assertActive(feature, ULTIMATE_STRENGTH);
  }

  @Test
  public void enabling_piety_replaces_skin_strength_and_attack_but_keeps_other_prayers() {
    PrayerFeature feature = newFeature(THICK_SKIN, BURST_OF_STRENGTH, CLARITY_OF_THOUGHT, PROTECT_FROM_MELEE, PROTECT_ITEM, RAPID_HEAL, PRESERVE);

    feature.onScriptPreFired(click(PIETY));

    assertActive(feature, PIETY, PROTECT_FROM_MELEE, PROTECT_ITEM, RAPID_HEAL, PRESERVE);
  }

  @Test
  public void enabling_piety_replaces_the_active_ranged_prayer() {
    PrayerFeature feature = newFeature(EAGLE_EYE, STEEL_SKIN);

    feature.onScriptPreFired(click(PIETY));

    assertActive(feature, PIETY);
  }

  @Test
  public void enabling_a_skin_prayer_replaces_piety() {
    PrayerFeature feature = newFeature(PIETY, PROTECT_FROM_MELEE);

    feature.onScriptPreFired(click(STEEL_SKIN));

    assertActive(feature, STEEL_SKIN, PROTECT_FROM_MELEE);
  }

  @Test
  public void enabling_an_endgame_prayer_replaces_the_active_endgame_prayer() {
    PrayerFeature feature = newFeature(PIETY);

    feature.onScriptPreFired(click(RIGOUR));

    assertActive(feature, RIGOUR);
  }

  @Test
  public void disabling_a_prayer_keeps_the_others() {
    PrayerFeature feature = newFeature(PIETY, PROTECT_FROM_MELEE, PROTECT_ITEM);

    feature.onScriptPreFired(click(PIETY));

    assertActive(feature, PROTECT_FROM_MELEE, PROTECT_ITEM);
  }

  @Test
  public void clicking_without_prayer_points_does_nothing() {
    // The mocked client has no prayer points
    PrayerFeature feature = newFeatureWithoutPrayerPoints(PROTECT_FROM_MAGIC);

    // The prayer is never looked up, so the event needs no source widget
    feature.onScriptPreFired(new ScriptPreFired(TOGGLE_SINGLE_PRAYER_SCRIPT_ID));

    assertActive(feature, PROTECT_FROM_MAGIC);
  }

  @Test
  public void quick_prayers_replace_conflicting_active_prayers() {
    PrayerFeature feature = newFeature(EAGLE_EYE, PROTECT_FROM_MAGIC, PROTECT_ITEM);
    quickPrayers(feature, PIETY, PROTECT_FROM_MELEE);

    feature.onScriptPostFired(new ScriptPostFired(TOGGLE_QUICK_PRAYER_SCRIPT_ID));

    assertActive(feature, PIETY, PROTECT_FROM_MELEE, PROTECT_ITEM);
  }

  @Test
  public void quick_prayers_already_active_stay_active() {
    PrayerFeature feature = newFeature(PIETY, PROTECT_FROM_MELEE);
    quickPrayers(feature, PIETY, PROTECT_FROM_MELEE);

    feature.onScriptPostFired(new ScriptPostFired(TOGGLE_QUICK_PRAYER_SCRIPT_ID));

    assertActive(feature, PIETY, PROTECT_FROM_MELEE);
  }

  @Test
  public void turning_quick_prayers_off_disables_every_prayer() {
    PrayerFeature feature = newFeature(PIETY, PROTECT_FROM_MELEE, PROTECT_ITEM);

    // QUICKPRAYER_ACTIVE is already 0, the toggle script has run
    feature.onScriptPostFired(new ScriptPostFired(TOGGLE_QUICK_PRAYER_SCRIPT_ID));

    assertActive(feature);
  }

  /**
   * @param active The prayers that are active before the test
   */
  private static PrayerFeature newFeature(Prayer... active) {
    PrayerFeature feature = newFeatureWithoutPrayerPoints(active);
    doReturn(99).when(feature.client).getBoostedSkillLevel(Skill.PRAYER);
    return feature;
  }

  private static PrayerFeature newFeatureWithoutPrayerPoints(Prayer... active) {
    PrayerFeature feature = TestSetup.createNewInstantPrayer();
    feature.client = TestSetup.mockClient();
    feature.varService = mock(VarService.class);
    // Only the prayer state is used when toggling prayers, never the client
    feature.state = new PrayerState();
    feature.state.setPrayerState(prayerToBits(active));
    return feature;
  }

  /**
   * The quick prayers have been turned on with {@code selected} as the quick prayers
   */
  private static void quickPrayers(PrayerFeature feature, Prayer... selected) {
    doReturn(true).when(feature.varService).isVarbitTrue(QUICKPRAYER_ACTIVE);
    doReturn(prayerToBits(selected)).when(feature.varService).varbitValue(QUICKPRAYER_SELECTED);
  }

  /**
   * @return A click on the prayer in the prayer book
   */
  private static ScriptPreFired click(Prayer prayer) {
    ScriptPreFired event = new ScriptPreFired(TOGGLE_SINGLE_PRAYER_SCRIPT_ID);
    ScriptEvent scriptEvent = mock(ScriptEvent.class);
    Widget prayerWidget = mock(Widget.class);
    doReturn(prayerWidget).when(scriptEvent).getSource();
    doReturn(prayerWidgetId(prayer)).when(prayerWidget).getId();
    event.setScriptEvent(scriptEvent);
    return event;
  }

  private static int prayerWidgetId(Prayer prayer) {
    int bit = PRAYER_TO_BIT.get(prayer);
    return INTERFACE_TO_BIT.entrySet().stream()
      .filter(entry -> entry.getValue() == bit)
      .map(Map.Entry::getKey)
      .findFirst()
      .orElseThrow(() -> new IllegalArgumentException("No prayer book widget for " + prayer));
  }

  private static void assertActive(PrayerFeature feature, Prayer... expected) {
    Set<Prayer> expectedPrayers = expected.length == 0 ? EnumSet.noneOf(Prayer.class) : EnumSet.of(expected[0], expected);
    assertEquals(expectedPrayers, activePrayers(feature.state.getPrayerState()));
  }

  /**
   * @return The base prayers whose bit is set, for readable failures
   */
  private static Set<Prayer> activePrayers(int prayerState) {
    Set<Prayer> active = EnumSet.noneOf(Prayer.class);
    PRAYER_TO_BIT.forEach((prayer, bit) -> {
      // Alternate prayers share the bit of the prayer they replace
      if (prayer != DEADEYE && prayer != MYSTIC_VIGOUR && (prayerState & bit) != 0) {
        active.add(prayer);
      }
    });
    return active;
  }
}
