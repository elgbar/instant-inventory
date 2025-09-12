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

package no.elg.ii.feature.state;

import javax.inject.Inject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.gameval.VarbitID;
import no.elg.ii.InstantInventoryConfig;

@Slf4j
@NoArgsConstructor
public class PrayerState implements FeatureState {

  @Inject
  Client client;

  @Inject
  InstantInventoryConfig pluginConfig;

  /**
   * Last tick server prayer state. Might be modified by client
   */
  @Getter
  private int lastPrayerState;

  /**
   * Current server prayer state.  Might be modified by client
   */
  @Getter
  private int prayerState;

  private long lastManuallyModified;

  public void setPrayerState(int state) {
    this.prayerState = state;
    lastManuallyModified = System.currentTimeMillis();
  }

  @Override
  public void resetAll() {
    prayerState = client.getServerVarbitValue(VarbitID.PRAYER_ALLACTIVE);
    // Keep to record of last state on reset
    lastPrayerState = prayerState;
  }

  @Override
  public void validateAll() {
    if (shouldRevalidate()) {
      lastPrayerState = prayerState;
      prayerState = client.getServerVarbitValue(VarbitID.PRAYER_ALLACTIVE);
    } else {
      // Skip reading server value as it likely does not reflect the clicked state.
      // This will prevent flickering of prayer icons when clicking fast on multiple conflicting prayers
      log.debug("Skipping prayer state revalidation, a manual modification was done ~ {} ms ago. Must wait at least {} ms", System.currentTimeMillis() - lastManuallyModified, pluginConfig.minChangedMs());
    }
  }

  /**
   *
   * @return Whether we should revalidate the state from server. This is to avoid overwriting manual changes too early
   */
  private boolean shouldRevalidate() {
    return System.currentTimeMillis() - lastManuallyModified >= pluginConfig.minChangedMs();
  }

}
