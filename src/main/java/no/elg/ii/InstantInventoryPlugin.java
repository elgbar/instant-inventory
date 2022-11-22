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
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(name = "Instant Inventory")
public class InstantInventoryPlugin extends Plugin {

  public static final String DROP_OPTION = "Drop";
  public static final String CLEAN_OPTION = "Clean";
  public static final int INVENTORY_SIZE = 28;
  public static final int INVALID_ITEM_ID = -1;
  public static final Widget[] EMPTY_WIDGET = new Widget[0];
  public int[] dropped = new int[INVENTORY_SIZE];
  public int[] cleaned = new int[INVENTORY_SIZE];

  @Inject
  private Client client;

  @Inject
  private InstantInventoryConfig config;

  @Inject
  private InstantInventoryOverlay overlay;

  @Inject
  private OverlayManager overlayManager;

  @Override
  protected void startUp() {
    Arrays.fill(dropped, INVALID_ITEM_ID);
    overlayManager.add(overlay);
  }

  @Override
  protected void shutDown()
  {
    overlayManager.remove(overlay);
  }

  @Subscribe
  public void onMenuOptionClicked(final MenuOptionClicked event) {
    Widget widget = event.getWidget();
    if (widget != null) {
      String menuOption = event.getMenuOption();
      int index = widget.getIndex();
      int itemId = event.getItemId();

      if (config.instantDrop() && DROP_OPTION.equals(menuOption)) {
        dropped[index] = itemId;
      } else if (config.instantClean() && CLEAN_OPTION.equals(menuOption)) {
        cleaned[index] = itemId;
      }
    }
  }

  /* (non-javadoc)
   * Make sure the item in the slot is hidden, the client sets it as non-hidden each tick (?)
   *  or so. This must be done before the client is rendered otherwise (such as if we were to use
   *  the ClientTick event) the item would be visible for a single frame.
   */
  @Subscribe
  public void onBeforeRender(BeforeRender beforeRender) {
    Widget[] inventoryWidgetItem = inventoryItems();
    for (int index = 0; index < inventoryWidgetItem.length; index++) {
      int hideIndex = dropped[index];
      if (hideIndex == INVALID_ITEM_ID) {
        continue;
      }
      Widget widget = inventoryWidgetItem[index];
      if (!widget.isSelfHidden()) {
        widget.setHidden(true);
      }
    }
  }

  /* (non-javadoc)
   * When a dropped item is no longer in the inventory, unmark it as being hidden
   */
  @Subscribe
  public void onGameTick(GameTick tick) {
    Widget[] inventoryWidgets = inventoryItems();
    for (int index = 0; index < inventoryWidgets.length; index++) {
      int currentItemId = inventoryWidgets[index].getItemId();
      testAndReset(dropped, index, currentItemId);
      testAndReset(cleaned, index, currentItemId);
    }
  }

  @Subscribe
  public void onConfigChanged(ConfigChanged configChanged)
  {
    if (configChanged.getGroup().equals(InstantInventoryConfig.GROUP))
    {
      overlay.invalidateCache();
    }
  }

  private static void testAndReset(int[] items, int index, int currentItemId) {
    if (items[index] != INVALID_ITEM_ID && items[index] != currentItemId) {
      items[index] = INVALID_ITEM_ID;
    }
  }

  /**
   * @return An array of items in the players inventory, or an empty inventory if there is no
   * inventory widget
   */
  @Nonnull
  private Widget[] inventoryItems() {
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
