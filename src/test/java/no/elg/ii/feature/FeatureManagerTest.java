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

package no.elg.ii.feature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import no.elg.ii.test.IntegrationTestHelper;
import org.junit.Test;

public class FeatureManagerTest extends IntegrationTestHelper {


  @Test
  public void updateFeatureStatus() {
    featureManager.updateFeatureStatus(dropFeature, false);
    verify(dropFeature, never()).onEnable();
    featureManager.updateFeatureStatus(dropFeature, false);
    verify(dropFeature, never()).onEnable();
    featureManager.updateFeatureStatus(dropFeature, true);
    verify(dropFeature).onEnable();
    featureManager.updateFeatureStatus(dropFeature, true);
    verify(dropFeature).onEnable();

    verify(dropFeature, never()).onDisable();
    featureManager.updateFeatureStatus(dropFeature, false);
    verify(dropFeature).onDisable();
    featureManager.updateFeatureStatus(dropFeature, false);
    verify(dropFeature).onDisable();
  }

  @Test
  public void enableFeature() {
    featureManager.enableFeature(dropFeature);

    verify(eventBus).register(dropFeature);
    verify(dropFeature).onEnable();
    verify(dropFeature).reset();
    assertEquals(1, featureManager.getActiveFeatures().size());
    assertTrue(featureManager.getActiveFeatures().contains(dropFeature));
  }

  @Test
  public void disableFeature() {
    featureManager.enableFeature(dropFeature);
    featureManager.disableFeature(dropFeature);

    verify(eventBus).unregister(dropFeature);
    verify(dropFeature).onDisable();
    //Once to enable, once to disable
    verify(dropFeature, times(2)).reset();
    assertTrue(featureManager.getActiveFeatures().isEmpty());
  }
}
