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

import com.google.inject.Provides;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import javax.inject.Inject;
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
import no.elg.ii.clean.CleanHerbComponent;
import no.elg.ii.drop.DropComponent;

@PluginDescriptor(name = "Instant Inventory")
public class InstantInventoryPlugin extends Plugin {

  public static final Widget[] EMPTY_WIDGET = new Widget[0];

  /**
   * Statically available and thread-safe tick counter
   */
  public static AtomicInteger tickCounter = new AtomicInteger(0);

  /**
   * The currently loaded components
   */
  public final Set<InstantInventoryComponent> components = new HashSet<>();

  @Inject
  private Client client;

  @Inject
  private DropComponent dropComponent;

  @Inject
  private CleanHerbComponent cleanHerbComponent;

  @Inject
  private EventBus eventBus;

  @Inject
  private InstantInventoryConfig config;

  @Override
  protected void startUp() {
    updateAllComponentStatus();
  }

  @Override
  protected void shutDown() {
    // Disable all components when the plugin shuts down
    for (InstantInventoryComponent component : components) {
      disableComponent(component);
    }
    components.clear();
  }

  /**
   * Make sure all components are in its correct state
   */
  private void updateAllComponentStatus() {
    updateComponentStatus(dropComponent, config.instantDrop());
    updateComponentStatus(cleanHerbComponent, config.instantClean());
  }

  /**
   * Make sure a component is in its correct state, that is disabled when disabled in the config and
   * vice versa
   *
   * @param component         The component to check
   * @param isEnabledInConfig Whether the component is currently enable in the config
   */
  private void updateComponentStatus(InstantInventoryComponent component,
      boolean isEnabledInConfig) {
    boolean wasEnabled = components.contains(component);
    if (!wasEnabled && isEnabledInConfig) {
      enableComponent(component);
    } else if (wasEnabled && !isEnabledInConfig) {
      disableComponent(component);
    }
  }

  /**
   * Enable a component, meaning it is listing to events and generally acting as a mini-plugin
   *
   * @param component The component to enable
   */
  private void enableComponent(InstantInventoryComponent component) {
    eventBus.register(component);
    components.add(component);
    component.onEnable();
    component.reset();
  }

  /**
   * Disable a component, it will no longer receive events
   *
   * @param component The component to disable
   */
  private void disableComponent(InstantInventoryComponent component) {
    eventBus.unregister(component);
    components.remove(component);
    component.onDisable();
    component.reset();
  }

  /* (non-javadoc)
   * When an item is different longer in the inventory, unmark it as being hidden
   */
  @Subscribe
  public void onGameTick(GameTick event) {
    tickCounter.set(client.getTickCount());
    Widget[] inventoryWidgets = inventoryItems();
    for (int index = 0; index < inventoryWidgets.length; index++) {
      int currentItemId = inventoryWidgets[index].getItemId();
      for (InstantInventoryComponent component : components) {
        component.getState().validateState(index, currentItemId);
      }
    }
  }

  /* (non-javadoc)
   * Reset components when the state change as we do not want to operate on stale data
   */
  @Subscribe
  public void onGameStateChanged(GameStateChanged event) {
    for (InstantInventoryComponent component : components) {
      component.reset();
    }
  }

  @Subscribe
  public void onConfigChanged(ConfigChanged configChanged) {
    if (InstantInventoryConfig.GROUP.equals(configChanged.getGroup())) {
      updateAllComponentStatus();
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
