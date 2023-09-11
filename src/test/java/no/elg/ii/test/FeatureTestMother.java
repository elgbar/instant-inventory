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

package no.elg.ii.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;
import net.runelite.client.config.ConfigItem;
import no.elg.ii.InstantInventoryConfig;
import no.elg.ii.feature.Feature;
import no.elg.ii.inventory.InventoryState;
import org.junit.Test;

public abstract class FeatureTestMother<T extends Feature> {

  /**
   * @return A {@link org.mockito.Spy} instance of this feature
   */
  public abstract T createNewInstance();

  @Test
  public void configTest() {
    Feature feature = createNewInstance();

    try {
      Method method = InstantInventoryConfig.class.getMethod(feature.getConfigKey());
      ConfigItem configItemAnnotation = method.getAnnotation(ConfigItem.class);
      assertNotNull(configItemAnnotation);
      assertEquals("key name in ConfigItem must match feature.getConfigKey()",
        feature.getConfigKey(), configItemAnnotation.keyName());
    } catch (NoSuchMethodException e) {
      fail("Failed to find a field in " + InstantInventoryConfig.class.getSimpleName()
        + " which matches the config key: " + feature.getConfigKey());
    }
  }

  @Test
  public void reset_calls_state_resetAll() {
    Feature feature = createNewInstance();
    InventoryState mockState = mock(InventoryState.class);
    doReturn(mockState).when(feature).getState();
    doNothing().when(mockState).resetAll();

    feature.reset();
    verify(mockState).resetAll();
  }
}
