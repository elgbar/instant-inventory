/*
 * Copyright (c) 2018-2023 Elg
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
package no.elg.ii.feature.clean;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.awt.*;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import no.elg.ii.inventory.InventoryState;
import no.elg.ii.inventory.slot.InventorySlot;

import static no.elg.ii.util.InventoryUtil.isInvalidInventoryIndex;
import static no.elg.ii.util.InventoryUtil.isValidInventoryIndex;

@Singleton
public class CleanHerbOverlay extends WidgetItemOverlay {

  @Inject
  @VisibleForTesting
  ItemManager itemManager;
  @Inject
  @VisibleForTesting
  CleanHerbFeature clean;

  {
    showOnBank();
    showOnInventory();
  }

  @Override
  public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem) {
    int index = widgetItem.getWidget().getIndex();
    if(isInvalidInventoryIndex(index)){
      return;
    }
    InventorySlot slot = clean.getState().getSlot(index);
    if (!slot.hasValidItemId()) {
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
}
