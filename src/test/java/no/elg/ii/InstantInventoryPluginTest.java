/*
 * Copyright (c) 2023 Elg
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

package no.elg.ii;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import net.runelite.api.events.GameStateChanged;
import net.runelite.client.events.ConfigChanged;
import no.elg.ii.test.IntegrationTestHelper;
import org.junit.Test;

public class InstantInventoryPluginTest extends IntegrationTestHelper {

  @Test
  public void startUp_calls_updateAllFeatures() {
    plugin.startUp();
    verify(featureManager).updateAllFeatureStatus();
  }

  @Test
  public void shutDown_disables_all_features() {
    plugin.shutDown();
    verify(featureManager).disableAllFeatures();
  }

  @Test
  public void onGameStateChanged_calls_nothing_on_incorrect_group() {
    plugin.startUp();
    plugin.onGameStateChanged(new GameStateChanged());

    verify(dropFeature, times(2)).reset();
    verify(cleanHerbFeature, times(2)).reset();
  }

  @Test
  public void onConfigChanged_calls_updateAllFeatureStatus_on_correct_group() {
    ConfigChanged configChanged = new ConfigChanged();
    configChanged.setGroup(InstantInventoryConfig.GROUP);
    plugin.onConfigChanged(configChanged);
    verify(featureManager).updateAllFeatureStatus();
  }

  @Test
  public void onConfigChanged_calls_nothing_on_incorrect_group() {
    ConfigChanged configChanged = new ConfigChanged();
    configChanged.setGroup("");
    plugin.onConfigChanged(configChanged);
    verify(featureManager, never()).updateAllFeatureStatus();
  }
}
