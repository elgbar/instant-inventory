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

package no.elg.ii.feature.state;

import static no.elg.ii.model.PrayerInfo.PRAYER_TO_BIT;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import net.runelite.api.Prayer;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import no.elg.ii.InstantInventoryConfig;
import no.elg.ii.test.TestSetup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class PrayerStateTest {

  private static final int PROTECT_FROM_MELEE = PRAYER_TO_BIT.get(Prayer.PROTECT_FROM_MELEE);
  /**
   * Deadeye replaces Eagle Eye, so it is on Eagle Eye's bit in the alternate varp
   */
  private static final int DEADEYE = PRAYER_TO_BIT.get(Prayer.EAGLE_EYE);

  @Test
  public void validateAll_includes_alternate_prayers() {
    PrayerState state = newState(PROTECT_FROM_MELEE, DEADEYE, Integer.MAX_VALUE);

    state.validateAll();

    assertEquals(PROTECT_FROM_MELEE | DEADEYE, state.getServerPrayerState());
    assertEquals(PROTECT_FROM_MELEE | DEADEYE, state.getPrayerState());
  }

  @Test
  public void resetAll_includes_alternate_prayers() {
    PrayerState state = newState(0, DEADEYE, Integer.MAX_VALUE);

    state.resetAll();

    assertEquals(DEADEYE, state.getServerPrayerState());
    assertEquals(DEADEYE, state.getPrayerState());
    assertEquals(DEADEYE, state.getLastPrayerState());
  }

  @Test
  public void alternate_prayers_on_any_bit_are_included() {
    // A future alternate prayer is not limited to the bits of today's prayer book
    int futurePrayer = 1 << 29;
    PrayerState state = newState(0, futurePrayer, Integer.MAX_VALUE);

    state.resetAll();

    assertEquals(futurePrayer, state.getServerPrayerState());
  }

  @Test
  public void validateAll_keeps_a_recent_manual_change() {
    PrayerState state = newState(0, DEADEYE, Integer.MAX_VALUE);
    state.setPrayerState(PROTECT_FROM_MELEE);

    state.validateAll();

    assertEquals(DEADEYE, state.getServerPrayerState());
    assertEquals(PROTECT_FROM_MELEE, state.getPrayerState());
  }

  @Test
  public void validateAll_replaces_an_old_manual_change() {
    PrayerState state = newState(0, DEADEYE, 0);
    state.setPrayerState(PROTECT_FROM_MELEE);

    state.validateAll();

    assertEquals(DEADEYE, state.getPrayerState());
    assertEquals(PROTECT_FROM_MELEE, state.getLastPrayerState());
  }

  private static PrayerState newState(int allActive, int alternateAllActive, int minChangedMs) {
    PrayerState state = new PrayerState();
    state.client = TestSetup.mockClient();
    doReturn(allActive).when(state.client).getServerVarbitValue(VarbitID.PRAYER_ALLACTIVE);
    doReturn(alternateAllActive).when(state.client).getServerVarpValue(VarPlayerID.PRAYER_ALLACTIVE_ALTERNATE_0);
    state.pluginConfig = new InstantInventoryConfig() {
      @Override
      public int minChangedMs() {
        return minChangedMs;
      }
    };
    return state;
  }
}
