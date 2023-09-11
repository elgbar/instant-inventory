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

package no.elg.ii.feature.equip;

import com.google.common.annotations.VisibleForTesting;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import javax.inject.Inject;
import net.runelite.api.Item;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import no.elg.ii.inventory.slot.InventorySlot;
import no.elg.ii.inventory.slot.ReplacementInventorySlot;
import no.elg.ii.util.IndexedItem;

public class EquipOverlay extends WidgetItemOverlay {

  @Inject
  @VisibleForTesting
  ItemManager itemManager;
  @Inject
  @VisibleForTesting
  EquipFeature feature;

  {
    showOnInventory();
  }

  @Override
  public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem) {
    Widget widget = widgetItem.getWidget();
    if (widget.isHidden() || widget.getName().isBlank()) {
      int index = widget.getIndex();
      feature.getState().getActiveSlots().forEach(indexedSlot -> {
        InventorySlot slot = indexedSlot.getSlot();
        if (slot instanceof ReplacementInventorySlot) {
          ReplacementInventorySlot replacementInventorySlot = (ReplacementInventorySlot) slot;
          Rectangle bounds = widgetItem.getCanvasBounds();

          IndexedItem primaryHand = replacementInventorySlot.getReplacedItem();
          if (primaryHand != null && index == primaryHand.getIndex()) {
            renderItem(graphics, bounds, primaryHand.getItem());
          }

          IndexedItem offhand = replacementInventorySlot.getOffhandReplacedItem();
          if (offhand != null && index == offhand.getIndex()) {
            renderItem(graphics, bounds, offhand.getItem());
          }
        }
      });
    }
  }

  private void renderItem(Graphics2D graphics, Rectangle bounds, Item item) {
    Image image = itemManager.getImage(item.getId(), item.getQuantity(), false);
    graphics.drawImage(image, (int) bounds.getX(), (int) bounds.getY(), null);
  }
}
