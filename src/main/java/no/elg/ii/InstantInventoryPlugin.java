/*
 * Copyright (c) 2022-2026 Elg
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
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import no.elg.ii.feature.FeatureManager;
import no.elg.ii.feature.state.InventoryState;
import no.elg.ii.service.DisallowModifiedWidgetInteractionService;
import no.elg.ii.service.EnsureWidgetStateService;

@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@PluginDescriptor(
  name = "Instant Inventory"
)
public class InstantInventoryPlugin extends Plugin {

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
  protected FeatureManager featureManager;

  @Inject
  protected InventoryState inventoryState;
  @Inject
  protected ClientThread clientThread;

  //TODO If more services are added which only reacts to events, this should be redesigned to be more generic
  @Inject
  EnsureWidgetStateService ensureWidgetStateService;

  @Inject
  DisallowModifiedWidgetInteractionService disallowModifiedWidgetInteractionService;

  @Override
  protected void startUp() {
    eventBus.register(ensureWidgetStateService);
    eventBus.register(disallowModifiedWidgetInteractionService);
    GameState gameState = client.getGameState();
    if (gameState == GameState.LOGGED_IN) {
      log.debug("Starting up instant inventory. During the game state {}, will update all feature statues", gameState);
      clientThread.invoke(featureManager::updateAllFeatureStatus);
    } else {
      log.debug("Starting up instant inventory. During the game state {}, no reset is done", gameState);
    }
  }

  @Override
  protected void shutDown() {
    log.debug("Shutting down instant inventory plugin");
    // Disable all features when the plugin shuts down
    clientThread.invoke(featureManager::disableAllFeatures);
    eventBus.unregister(ensureWidgetStateService);
    eventBus.unregister(disallowModifiedWidgetInteractionService);
  }

  /* (non-javadoc)
   * When an item is different in the inventory, unmark it as being hidden.
   *
   * This should run after client ticking to prevent flickering of items
   */
  @Subscribe(priority = Integer.MAX_VALUE)
  public void onGameTick(GameTick event) {
    clientThread.invokeLater(inventoryState::validateAll);
  }

  /* (non-javadoc)
   * Reset features when the state change as we do not want to operate on stale data
   */
  @Subscribe
  public void onGameStateChanged(GameStateChanged event) {
    if (event.getGameState() == GameState.LOGGED_IN) {
      log.debug("Resetting features as the GameState changed to {}", event.getGameState());
      featureManager.updateAllFeatureStatus();
    }
  }

  @Subscribe
  public void onConfigChanged(ConfigChanged configChanged) {
    if (InstantInventoryConfig.GROUP.equals(configChanged.getGroup())) {
      clientThread.invoke(featureManager::updateAllFeatureStatus);
    }
  }

  @Provides
  public InstantInventoryConfig provideConfig(ConfigManager configManager) {
    return configManager.getConfig(InstantInventoryConfig.class);
  }
}
