/*
 * Copyright (c) 2018 kulers, 2022 Elg
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
package no.elg.ii.clean;

import static no.elg.ii.InventoryState.INVALID_ITEM_ID;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.ItemID;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import no.elg.ii.InstantInventoryConfig;

@Singleton
public class CleanHerbOverlay extends WidgetItemOverlay {

  @Inject
  private ItemManager itemManager;
  @Inject
  private InstantInventoryConfig config;
  @Inject
  private CleanHerbComponent clean;

  private final Cache<Long, Image> fillCache;

  {
    showOnInterfaces(WidgetID.INVENTORY_GROUP_ID);
    fillCache = CacheBuilder.newBuilder()
        .concurrencyLevel(1)
        .maximumSize(32)
        .build();
  }

  @Override
  public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem) {
    if (!config.instantClean()) {
      return;
    }
    int index = widgetItem.getWidget().getIndex();
    if (clean.getState().isInvalid(index)) {
      return;
    }

    Rectangle bounds = widgetItem.getCanvasBounds();
    int cleanItemId = GRIMY_CONVERTER.getOrDefault(itemId, INVALID_ITEM_ID);
    if (cleanItemId == INVALID_ITEM_ID) {
      return;
    }

    Image item = itemManager.getImage(cleanItemId, widgetItem.getQuantity(), false);
    graphics.drawImage(item, (int) bounds.getX(), (int) bounds.getY(), null);
  }

  public void invalidateCache() {
    fillCache.invalidateAll();
  }

  /**
   * Map of {@link ItemID} from grimy herbs to cleaned herbs
   */
  public static Map<Integer, Integer> GRIMY_CONVERTER = new HashMap<>();

  static {
    GRIMY_CONVERTER.put(ItemID.GRIMY_GUAM_LEAF, ItemID.GUAM_LEAF);
    GRIMY_CONVERTER.put(ItemID.GRIMY_MARRENTILL, ItemID.MARRENTILL);
    GRIMY_CONVERTER.put(ItemID.GRIMY_TARROMIN, ItemID.TARROMIN);
    GRIMY_CONVERTER.put(ItemID.GRIMY_HARRALANDER, ItemID.HARRALANDER);
    GRIMY_CONVERTER.put(ItemID.GRIMY_RANARR_WEED, ItemID.RANARR_WEED);
    GRIMY_CONVERTER.put(ItemID.GRIMY_IRIT_LEAF, ItemID.IRIT_LEAF);
    GRIMY_CONVERTER.put(ItemID.GRIMY_AVANTOE, ItemID.AVANTOE);
    GRIMY_CONVERTER.put(ItemID.GRIMY_KWUARM, ItemID.KWUARM);
    GRIMY_CONVERTER.put(ItemID.GRIMY_CADANTINE, ItemID.CADANTINE);
    GRIMY_CONVERTER.put(ItemID.GRIMY_DWARF_WEED, ItemID.DWARF_WEED);
    GRIMY_CONVERTER.put(ItemID.GRIMY_TORSTOL, ItemID.TORSTOL);
    GRIMY_CONVERTER.put(ItemID.GRIMY_SNAKE_WEED, ItemID.SNAKE_WEED);
    GRIMY_CONVERTER.put(ItemID.GRIMY_ARDRIGAL, ItemID.ARDRIGAL);
    GRIMY_CONVERTER.put(ItemID.GRIMY_SITO_FOIL, ItemID.SITO_FOIL);
    GRIMY_CONVERTER.put(ItemID.GRIMY_VOLENCIA_MOSS, ItemID.VOLENCIA_MOSS);
    GRIMY_CONVERTER.put(ItemID.GRIMY_ROGUES_PURSE, ItemID.ROGUES_PURSE);
    GRIMY_CONVERTER.put(ItemID.GRIMY_LANTADYME, ItemID.LANTADYME);
    GRIMY_CONVERTER.put(ItemID.GRIMY_TOADFLAX, ItemID.TOADFLAX);
    GRIMY_CONVERTER.put(ItemID.GRIMY_SNAPDRAGON, ItemID.SNAPDRAGON);
    GRIMY_CONVERTER.put(ItemID.GRIMY_NOXIFER, ItemID.NOXIFER);
    GRIMY_CONVERTER.put(ItemID.GRIMY_GOLPAR, ItemID.GOLPAR);
    GRIMY_CONVERTER.put(ItemID.GRIMY_BUCHU_LEAF, ItemID.BUCHU_LEAF);
  }
}
