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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import javax.inject.Inject;
import javax.inject.Singleton;
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
    HerbInfo cleanItemId = HerbInfo.HERBS.getOrDefault(itemId, null);
    if (cleanItemId == null) {
      return;
    }

    Image item = itemManager.getImage(cleanItemId.getCleanItemId(), widgetItem.getQuantity(),
        false);
    graphics.drawImage(item, (int) bounds.getX(), (int) bounds.getY(), null);
  }

  public void invalidateCache() {
    fillCache.invalidateAll();
  }

}
