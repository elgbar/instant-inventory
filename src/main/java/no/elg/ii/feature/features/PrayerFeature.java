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

import static net.runelite.api.gameval.VarbitID.QUICKPRAYER_ACTIVE;
import static net.runelite.api.gameval.VarbitID.QUICKPRAYER_SELECTED;
import static no.elg.ii.model.PrayerInfo.INTERFACE_TO_PRAYER;
import static no.elg.ii.model.PrayerInfo.PRAYER_TO_BIT;

import java.util.Map;
import java.util.function.IntBinaryOperator;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Prayer;
import net.runelite.api.ScriptEvent;
import net.runelite.api.Skill;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;
import no.elg.ii.feature.Feature;
import no.elg.ii.feature.state.PrayerState;
import no.elg.ii.model.PrayerInfo;
import no.elg.ii.service.VarService;

@Slf4j
@Singleton
@NoArgsConstructor
public class PrayerFeature implements Feature {

  public static final String PRAYER_CONFIG_KEY = "instantPrayer";

  /**
   * Index of the background/highlight widget for a prayer
   */
  private static final int BACKGROUND_PRAYER_INDEX = 0;

  /**
   * Indicates that no prayer change was made during an update.
   * <p>
   * Should be safe to use as there should never be a state where all bits are on.
   */
  private static final int UNCHANGED_PRAYER_STATE = Integer.MAX_VALUE;

  private static final int TOGGLE_PRAYER_SCRIPT_ID = 462;
  private static final int TOGGLE_QUICK_PRAYER_SCRIPT_ID = 455;

  @Inject
  @Getter
  private PrayerState state;

  @Inject
  private Client client;

  @Inject
  private VarService varService;

  @Subscribe
  public void onBeforeRender(BeforeRender event) {
    render();
  }

  @Subscribe
  public void onGameTick(final GameTick event) {
    state.validateAll();
  }

  private static final IntBinaryOperator TOGGLE_OP = (prayerState, bit) -> prayerState ^ bit;
  private static final IntBinaryOperator ENABLE_OP = (prayerState, bit) -> prayerState | bit;

  @Subscribe
  public void onScriptPreFired(final ScriptPreFired event) {
    if (event.getScriptId() == TOGGLE_PRAYER_SCRIPT_ID || event.getScriptId() == 455) { //TODO why does adding 455 help (but only a bit) with quick prayers?
      ScriptEvent scriptEvent = event.getScriptEvent();
      Widget src = scriptEvent.getSource();
      if (src != null && hasPrayer()) {
        var prayer = INTERFACE_TO_PRAYER.get(src.getId());
        if (prayer != null) {
          int prayerBit = PRAYER_TO_BIT.getOrDefault(prayer, 0);
          if (prayerBit != 0) {
            updateBit(prayerBit, TOGGLE_OP);
          }
        }
      }
    }
  }

  @Subscribe
  public void onScriptPostFired(final ScriptPostFired event) {
    if (event.getScriptId() == TOGGLE_QUICK_PRAYER_SCRIPT_ID && canUsePrayers()) {
      if (varService.isVarbitTrue(QUICKPRAYER_ACTIVE)) {
        int quickPrayerBits = varService.varbitValue(QUICKPRAYER_SELECTED);
        // turn on quick prayers, conflicting prayers will be automatically turned off by update
        log.debug("[{}] Quick prayer toggled on: quick prayers {}", client.getTickCount(), Integer.toBinaryString(quickPrayerBits));
        enableAllBits(quickPrayerBits);
      } else {
        log.debug("[{}] Quick prayer toggled off. Will disable all prayers", client.getTickCount());
        // Quick prayer will turn off all prayers when toggled off, not just the configured prayers
        state.setPrayerState(0);
      }
    }
  }

  /**
   * Update all bits by {@code op}, one at a time so that conflicts, and the ui updated are resolved correctly
   */
  private void updateAllBits(int prayerBits, @NonNull IntBinaryOperator op) {
    var prayers = PrayerInfo.prayerBitsToPrayers(prayerBits);
    for (Prayer prayer : prayers) {
      int prayerBit = PRAYER_TO_BIT.getOrDefault(prayer, 0);
      if (prayerBit != 0) {
        updateBit(prayerBit, op);
      }
    }
  }

  /**
   * Update a single prayer bit using the provided operator.
   *
   * @param prayerBit The prayer bit to update. Must only have one bit set.
   * @param op        How to update {@code prayerBit}
   * @see PrayerInfo#PRAYER_TO_BIT
   */
  private void updateBit(int prayerBit, @NonNull IntBinaryOperator op) {
    assert Integer.bitCount(prayerBit) == 1;
    int prayerState = state.getPrayerState();
    int newValue = op.applyAsInt(prayerState, prayerBit);
    int updateValue = update(newValue);
    state.setPrayerState(updateValue);
    log.debug("[{}] Toggled prayer: old state {}, new value {}, updated value {}", client.getTickCount(), Integer.toBinaryString(prayerState), Integer.toBinaryString(newValue), Integer.toBinaryString(updateValue));
  }

  private boolean hasPrayer() {
    return client.getBoostedSkillLevel(Skill.PRAYER) > 0;
  }

  /**
   * Update the prayer state, making sure no conflicting prayers are active at the same time.
   *
   * @param tweakedState The new prayer state. Must be only bit different from the current state.
   * @return The corrected prayer state
   */
  private int update(int tweakedState) {
    assert client.isClientThread();
    int initState = state.getPrayerState();
    //Only one bit should be different
    assert Math.abs(Integer.bitCount(tweakedState) - Integer.bitCount(initState)) <= 1;

    int[] conflictResolvedStatus = new int[PrayerInfo.CONFLICTING_PRAYERS.length];
    for (int i = 0, conflictingLength = PrayerInfo.CONFLICTING_PRAYERS.length; i < conflictingLength; i++) {
      int conflictMask = PrayerInfo.CONFLICTING_PRAYERS[i];
      int maskedTweakedValue = tweakedState & conflictMask;
      if (Integer.bitCount(maskedTweakedValue) > 1) {
        //There are at least two conflicts, remove the conflicting prayers by using the old value
        // Keep only those active that are in the conflict mask
        int maskedInitState = initState & conflictMask;
        // Show only the one that was not active before
        int correctedValue = maskedTweakedValue & ~maskedInitState;
        // Disable all conflicting prayers in the group and enable the corrected value
        conflictResolvedStatus[i] = (initState & ~conflictMask) | correctedValue;
      }
    }

    int nextState = UNCHANGED_PRAYER_STATE;
    for (int resolvedStatus : conflictResolvedStatus) {
      // If no resolvedStatus is set it will be 0, so skip it
      if (resolvedStatus != 0) {
        if (nextState == UNCHANGED_PRAYER_STATE) {
          // First conflicting group will be the initial status.
          // This cannot be `initState` because a bit might be enabled and that would be removed by &-ing the `resolvedStatus` with `initState`
          nextState = resolvedStatus;
        } else {
          // Handles multiple conflict groups, e.g. if you enable BURST_OF_STRENGTH + CLARITY_OF_THOUGHT are enabled, and you enable SHARP_EYE
          // Both the BURST_OF_STRENGTH group and CLARITY_OF_THOUGHT should be disabled, while SHARP_EYE should be enabled
          // This &-ing will disable multiple prayers at once
          nextState &= resolvedStatus;
        }
        log.debug("[{} | update] current state {}, diff state {}", client.getTickCount(), Integer.toBinaryString(initState), Integer.toBinaryString(resolvedStatus));
      }
    }
    if (nextState == UNCHANGED_PRAYER_STATE) {
      log.debug("[{} | update] no conflicts detected, final state will be input", client.getTickCount());
      // No conflicts found, assume the new state is correct
      // This is needed when turning on the first prayer in a conflict group
      nextState = tweakedState;
    }
    log.debug("[{} | update] init state {}, final state {}", client.getTickCount(), Integer.toBinaryString(initState), Integer.toBinaryString(nextState));
    return nextState;
  }

  /**
   * Modify whether the prayer book is active based on the internal prayer state.
   */
  private void render() {
    assert client.isClientThread();
    //Only update background widget when prayers was or is active
    // The lastPrayerState is needed to make sure we disable the prayers when they are turned off
    if ((state.getPrayerState() != 0 || state.getLastPrayerState() != 0)) {
      Widget prayerContainer = client.getWidget(InterfaceID.Prayerbook.CONTAINER);
      if (prayerContainer != null && !prayerContainer.isHidden()) {
        for (Map.Entry<Integer, Prayer> entry : INTERFACE_TO_PRAYER.entrySet()) {
          int prayerBit = PRAYER_TO_BIT.getOrDefault(entry.getValue(), 0);
          if (prayerBit != 0) {
            int prayerWidgetId = entry.getKey();
            Widget prayerWidget = client.getWidget(prayerWidgetId);
            if (prayerWidget != null) {
              Widget backgroundWidget = prayerWidget.getChild(BACKGROUND_PRAYER_INDEX);
              if (backgroundWidget != null) {
                // prayer is hidden when the bit is not set in the prayer state
                boolean hidden = (prayerBit & state.getPrayerState()) == 0;
                backgroundWidget.setHidden(hidden);
              }
            }
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
