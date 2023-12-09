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

import com.google.common.annotations.VisibleForTesting;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import no.elg.ii.feature.Feature;

@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeatureManager {

  /**
   * The currently loaded features
   */
  @VisibleForTesting
  protected final Set<Feature> activeFeatures = ConcurrentHashMap.newKeySet();
  private final Set<Feature> activeFeaturesView = Collections.unmodifiableSet(activeFeatures);


  @Inject
  @VisibleForTesting
  protected EventBus eventBus;

  @Inject
  @VisibleForTesting
  protected InstantInventoryConfig config;

  @Inject
  protected Features featureInstances;

  @Inject
  protected ClientThread clientThread;

  /**
   * Make sure all features are in its correct state
   */
  @VisibleForTesting
  protected void updateAllFeatureStatus() {
    updateFeatureStatus(featureInstances.getDropFeature(), config.instantDrop());
    updateFeatureStatus(featureInstances.getCleanHerbFeature(), config.instantClean());
    updateFeatureStatus(featureInstances.getDepositFeature(), config.instantDeposit());
    updateFeatureStatus(featureInstances.getEquipFeature(), config.instantEquip());
    updateFeatureStatus(featureInstances.getWithdrawFeature(), config.instantWithdraw());
  }

  public void disableAllFeatures() {
    HashSet<Feature> copy = new HashSet<>(activeFeatures);
    for (Feature feature : copy) {
      disableFeature(feature);
    }
  }

  /**
   * @return Thread safe view of the currently active features
   */
  public Set<Feature> getActiveFeatures() {
    return activeFeaturesView;
  }

  /**
   * Make sure a feature is in its correct state, that is disabled when disabled in the config and
   * vice versa
   *
   * @param feature           The feature to check
   * @param isEnabledInConfig Whether the feature is currently enable in the config
   */
  @VisibleForTesting
  void updateFeatureStatus(@Nonnull Feature feature, boolean isEnabledInConfig) {
    boolean wasEnabled = activeFeatures.contains(feature);

    if (!wasEnabled && isEnabledInConfig) {
      enableFeature(feature);
    } else if (wasEnabled && !isEnabledInConfig) {
      disableFeature(feature);
    }
  }

  /**
   * Enable a feature, meaning it is listing to events and generally acting as a mini-plugin
   *
   * @param feature The feature to enable
   */
  @VisibleForTesting
  void enableFeature(@Nonnull Feature feature) {
    clientThread.invoke(() -> {
      log.debug("Enabling " + feature.getConfigKey());
      eventBus.register(feature);
      activeFeatures.add(feature);
      feature.onEnable();
      feature.reset();
    });
  }

  /**
   * Disable a feature, it will no longer receive events
   *
   * @param feature The feature to disable
   */
  @VisibleForTesting
  void disableFeature(@Nonnull Feature feature) {
    clientThread.invoke(() -> {
      log.debug("Disabling " + feature.getConfigKey());
      eventBus.unregister(feature);
      activeFeatures.remove(feature);
      feature.onDisable();
      feature.reset();
    });
  }
}
