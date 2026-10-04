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

package no.elg.ii.feature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import no.elg.ii.test.IntegrationTestHelper;
import org.junit.Test;

public class FeatureManagerTest extends IntegrationTestHelper {

  @Test
  public void updateFeatureStatus() {
    // Disabled in config and not active: nothing happens
    doReturn(false).when(dropFeature).isEnabledInConfig();
    featureManager.updateFeatureStatus(dropFeature);
    verify(dropFeature, never()).onEnable();
    featureManager.updateFeatureStatus(dropFeature);
    verify(dropFeature, never()).onEnable();
    assertFalse(featureManager.getActiveFeatures().contains(dropFeature));

    // Enabled in config: enabled exactly once, even when updated again
    doReturn(true).when(dropFeature).isEnabledInConfig();
    featureManager.updateFeatureStatus(dropFeature);
    verify(dropFeature).onEnable();
    featureManager.updateFeatureStatus(dropFeature);
    verify(dropFeature).onEnable();
    assertTrue(featureManager.getActiveFeatures().contains(dropFeature));

    // Disabled in config again: disabled exactly once, even when updated again
    verify(dropFeature, never()).onDisable();
    doReturn(false).when(dropFeature).isEnabledInConfig();
    featureManager.updateFeatureStatus(dropFeature);
    verify(dropFeature).onDisable();
    featureManager.updateFeatureStatus(dropFeature);
    verify(dropFeature).onDisable();
    assertFalse(featureManager.getActiveFeatures().contains(dropFeature));
  }

  @Test
  public void updateAllFeatureStatus_checksEveryFeature() {
    features.forEach(feature -> doReturn(false).when(feature).isEnabledInConfig());

    featureManager.updateAllFeatureStatus();

    features.forEach(feature -> verify(featureManager).updateFeatureStatus(feature));
  }

  @Test
  public void enableFeature_offTheClientThread_failsTheClientThreadAssertion() throws Exception {
    Throwable[] thrown = new Throwable[1];
    Thread otherThread = new Thread(() -> {
      try {
        featureManager.enableFeature(dropFeature);
      } catch (Throwable t) {
        thrown[0] = t;
      }
    });
    otherThread.start();
    otherThread.join();

    assertTrue("Expected the client thread assertion to fail, got " + thrown[0], thrown[0] instanceof AssertionError);
    assertTrue(featureManager.getActiveFeatures().isEmpty());
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
