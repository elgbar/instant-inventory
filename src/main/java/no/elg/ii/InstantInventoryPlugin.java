/*
 * Copyright (c) 2022 Elg
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package no.elg.ii;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Provides;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import no.elg.ii.clean.CleanHerbFeature;
import no.elg.ii.drop.DropFeature;

@Slf4j
@PluginDescriptor(name = "Instant Inventory")
public class InstantInventoryPlugin extends Plugin {

  public static final Widget[] EMPTY_WIDGET = new Widget[0];

  /**
   * Statically available and thread-safe tick counter
   */
  public static AtomicInteger tickCounter = new AtomicInteger(0);

  /**
   * The currently loaded features
   */

  @VisibleForTesting
  protected final Set<Feature> features = new HashSet<>();
  @Inject
  @VisibleForTesting
  protected Client client;
  @Inject
  @VisibleForTesting
  protected EventBus eventBus;
  @Inject
  @VisibleForTesting
  protected InstantInventoryConfig config;
  @Inject
  @VisibleForTesting
  protected DropFeature dropFeature;
  @Inject
  @VisibleForTesting
  protected CleanHerbFeature cleanHerbFeature;

  @Override
  protected void startUp() {
    updateAllFeatureStatus();
  }

  @Override
  protected void shutDown() {
    // Disable all features when the plugin shuts down
    HashSet<Feature> copy = new HashSet<>(features);
    for (Feature feature : copy) {
      disableFeature(feature);
    }
  }

  /**
   * Make sure all features are in its correct state
   */
  @VisibleForTesting
  protected void updateAllFeatureStatus() {
    updateFeatureStatus(dropFeature, config.instantDrop());
    updateFeatureStatus(cleanHerbFeature, config.instantClean());
  }

  /**
   * Make sure a feature is in its correct state, that is disabled when disabled in the config and
   * vice versa
   *
   * @param feature           The feature to check
   * @param isEnabledInConfig Whether the feature is currently enable in the config
   */
  @VisibleForTesting
  void updateFeatureStatus(Feature feature, boolean isEnabledInConfig) {
    boolean wasEnabled = features.contains(feature);

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
  void enableFeature(Feature feature) {
    log.info("Enabling " + feature.getConfigKey());
    eventBus.register(feature);
    features.add(feature);
    feature.onEnable();
    feature.reset();
  }

  /**
   * Disable a feature, it will no longer receive events
   *
   * @param feature The feature to disable
   */
  @VisibleForTesting
  void disableFeature(Feature feature) {
    log.info("Disabling " + feature.getConfigKey());
    eventBus.unregister(feature);
    features.remove(feature);
    feature.onDisable();
    feature.reset();
  }

  /* (non-javadoc)
   * When an item is different longer in the inventory, unmark it as being hidden
   */
  @Subscribe
  public void onGameTick(GameTick event) {
    tickCounter.set(client.getTickCount());
    Widget[] inventoryWidgets = inventoryItems();
    HashSet<Feature> copy = new HashSet<>(features);
    for (int index = 0; index < inventoryWidgets.length; index++) {
      int currentItemId = inventoryWidgets[index].getItemId();
      for (Feature feature : copy) {
        feature.getState().validateState(index, currentItemId);
      }
    }
  }

  /* (non-javadoc)
   * Reset features when the state change as we do not want to operate on stale data
   */
  @Subscribe
  public void onGameStateChanged(GameStateChanged event) {
    log.info("Resetting features as the GameState changed to {}", event.getGameState());
    HashSet<Feature> copy = new HashSet<>(features);
    for (Feature feature : copy) {
      feature.reset();
    }
  }

  @Subscribe
  public void onConfigChanged(ConfigChanged configChanged) {
    if (InstantInventoryConfig.GROUP.equals(configChanged.getGroup())) {
      updateAllFeatureStatus();
    }
  }

  /**
   * @return An array of items in the players inventory, or an empty inventory if there is no
   * inventory widget
   */
  @Nonnull
  public Widget[] inventoryItems() {
    Widget inventory = client.getWidget(WidgetInfo.INVENTORY);
    if (inventory != null) {
      return inventory.getDynamicChildren();
    }
    return EMPTY_WIDGET;
  }

  @Provides
  InstantInventoryConfig provideConfig(ConfigManager configManager) {
    return configManager.getConfig(InstantInventoryConfig.class);
  }
}
