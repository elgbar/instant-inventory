/*
 * Copyright (c) 2022-2023 Elg
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

import static no.elg.ii.util.InventoryUtil.INVENTORY_SIZE;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Provides;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
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
import no.elg.ii.feature.Feature;
import no.elg.ii.inventory.InventoryState;
import no.elg.ii.util.AdditionalWidgetInfo;

@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@PluginDescriptor(name = "Instant Inventory")
public class InstantInventoryPlugin extends Plugin {

  public static final Widget[] EMPTY_WIDGET = new Widget[0];

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

  @Override
  protected void startUp() {
    featureManager.updateAllFeatureStatus();
  }

  @Override
  protected void shutDown() {
    // Disable all features when the plugin shuts down
    featureManager.disableAllFeatures();
  }

  /* (non-javadoc)
   * When an item is different in the inventory, unmark it as being hidden
   */
  @Subscribe
  public void onGameTick(GameTick event) {
    ItemContainer itemContainer = client.getItemContainer(InventoryID.INVENTORY);
    if (itemContainer == null) {
      return;
    }
    for (int index = 0; index < INVENTORY_SIZE; index++) {
      Item item = itemContainer.getItem(index);
      inventoryState.validateState(index, item);
    }
  }

  /* (non-javadoc)
   * Reset features when the state change as we do not want to operate on stale data
   */
  @Subscribe
  public void onGameStateChanged(GameStateChanged event) {
    if (event.getGameState() == GameState.LOGGED_IN) {
      log.debug("Resetting features as the GameState changed to {}", event.getGameState());
      Set<Feature> activeFeatures = featureManager.getActiveFeatures();
      for (Feature feature : activeFeatures) {
        feature.reset();
      }
    }
  }

  @Subscribe
  public void onConfigChanged(ConfigChanged configChanged) {
    if (InstantInventoryConfig.GROUP.equals(configChanged.getGroup())) {
      featureManager.updateAllFeatureStatus();
    }
  }

  /**
   * @return An array of items in the players inventory, or an empty inventory if there is no
   * inventory widget
   */
  @Nonnull
  public Widget[] inventoryItems(@Nonnull WidgetInfo widgetInfo) {
    return inventoryItems(widgetInfo.getGroupId(), widgetInfo.getChildId());
  }

  public Widget[] inventoryItems(@Nonnull AdditionalWidgetInfo widgetInfo) {
    return inventoryItems(widgetInfo.getGroupId(), widgetInfo.getChildId());
  }

  public Widget[] inventoryItems(int groupId, int childId) {
    Widget widget = client.getWidget(groupId, childId);
    if (widget != null) {
      return widget.getDynamicChildren();
    }
    return EMPTY_WIDGET;
  }

  @Provides
  InstantInventoryConfig provideConfig(ConfigManager configManager) {
    return configManager.getConfig(InstantInventoryConfig.class);
  }
}
