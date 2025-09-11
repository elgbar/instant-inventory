/*
 * Copyright (c) 2025 Elg
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
import static net.runelite.api.Prayer.EAGLE_EYE;
import static net.runelite.api.Prayer.HAWK_EYE;
import static net.runelite.api.Prayer.IMPROVED_REFLEXES;
import static net.runelite.api.Prayer.INCREDIBLE_REFLEXES;
import static net.runelite.api.Prayer.MYSTIC_LORE;
import static net.runelite.api.Prayer.MYSTIC_MIGHT;
import static net.runelite.api.Prayer.MYSTIC_WILL;
import static net.runelite.api.Prayer.PROTECT_FROM_MAGIC;
import static net.runelite.api.Prayer.PROTECT_FROM_MELEE;
import static net.runelite.api.Prayer.PROTECT_FROM_MISSILES;
import static net.runelite.api.Prayer.ROCK_SKIN;
import static net.runelite.api.Prayer.SHARP_EYE;
import static net.runelite.api.Prayer.STEEL_SKIN;
import static net.runelite.api.Prayer.SUPERHUMAN_STRENGTH;
import static net.runelite.api.Prayer.THICK_SKIN;
import static net.runelite.api.Prayer.ULTIMATE_STRENGTH;
import static no.elg.ii.model.PrayerConflict.INTERFACE_TO_PRAYER;
import static no.elg.ii.model.PrayerConflict.PRAYER_TO_BIT;
import static no.elg.ii.model.PrayerConflict.PRAYER_TO_INTERFACE;

import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ScriptEvent;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;
import no.elg.ii.feature.Feature;
import no.elg.ii.feature.state.PrayerState;
import no.elg.ii.model.PrayerConflict;
import no.elg.ii.service.VarService;

@Slf4j
@Singleton
@NoArgsConstructor
public class InstantPrayer implements Feature {

  public static final String PRAYER_CONFIG_KEY = "instantPrayer";

  @Inject
  @Getter
  private PrayerState state;

  @Inject
  VarService varService;

  @Inject
  Client client;

  public int[] conflicting = {
    PrayerConflict.toConflictInt(THICK_SKIN, ROCK_SKIN, STEEL_SKIN),
    PrayerConflict.toConflictInt(BURST_OF_STRENGTH, SUPERHUMAN_STRENGTH, ULTIMATE_STRENGTH, SHARP_EYE, MYSTIC_WILL, HAWK_EYE, MYSTIC_LORE, EAGLE_EYE, MYSTIC_MIGHT),
    PrayerConflict.toConflictInt(CLARITY_OF_THOUGHT, IMPROVED_REFLEXES, INCREDIBLE_REFLEXES, SHARP_EYE, MYSTIC_WILL, HAWK_EYE, MYSTIC_LORE, EAGLE_EYE, MYSTIC_MIGHT),
    PrayerConflict.toConflictInt(SHARP_EYE, MYSTIC_WILL, HAWK_EYE, MYSTIC_LORE, EAGLE_EYE, MYSTIC_MIGHT),
    PrayerConflict.toConflictInt(PROTECT_FROM_MAGIC, PROTECT_FROM_MISSILES, PROTECT_FROM_MELEE),
  };

  @Subscribe
  public void onScriptPreFired(final ScriptPreFired event) {
    if (event.getScriptId() == 462) {
      ScriptEvent scriptEvent = event.getScriptEvent();
      Widget src = scriptEvent.getSource();
      if (src != null) {
        var prayer = INTERFACE_TO_PRAYER.get(src.getId());
        if (prayer != null) {
          int prayerBit = PRAYER_TO_BIT.getOrDefault(prayer, 0);
          if (prayerBit != 0) {
            //Toggle the prayer
            int newValue = state.prayerState ^ prayerBit;
            int updateValue = update(newValue);
            log.warn("[{}] Toggled prayer {}, old value {}, new value {}", client.getTickCount(), prayer, Integer.toBinaryString(updateValue), Integer.toBinaryString(newValue));
            state.prayerState = updateValue;
          }
        }
      }
    }
  }

  //FIXMe still buggy when (on the same tick) switching between conflicting prayers
//  @Subscribe(priority = Integer.MAX_VALUE - 1)
//  public void onVarbitChanged(final VarbitChanged event) {
//    int varbitId = event.getVarbitId();
//    int newValue = event.getValue();
//    if (varbitId == VarbitID.PRAYER_ALLACTIVE && state.prayerState != newValue) {
//      update(newValue);
//    }
//  }

  int update(int newValue) {
    int active = state.prayerState;
    for (int conflictMask : conflicting) {
      int maskedNewValue = newValue & conflictMask;
      if (Integer.bitCount(maskedNewValue) > 1) {
        //There are some conflicts
        //remove the conflicting prayers by using the old value
        int maskedOldValue = active & conflictMask;
        togglePrayerUI(maskedOldValue, true);
        int correctedValue = maskedNewValue & ~maskedOldValue;
        togglePrayerUI(correctedValue, false);
        active = (active & ~conflictMask) | correctedValue;
      }
    }
    return active;
  }

  public void togglePrayerUI(int mask, boolean hidden) {
    var prayers = PrayerConflict.prayerBitToPrayer(mask);
    for (var prayer : prayers) {
      int interfaceid = PRAYER_TO_INTERFACE.getOrDefault(prayer, 0);
      if (interfaceid != 0) {
        Widget widget = client.getWidget(interfaceid);
        if (widget != null) {
          Widget child = widget.getChild(0);
          if (child != null) {
            child.setHidden(hidden);
          }
        }
      }
    }
  }

  @Override
  public @NonNull String getConfigKey() {
    return PRAYER_CONFIG_KEY;
  }
}
