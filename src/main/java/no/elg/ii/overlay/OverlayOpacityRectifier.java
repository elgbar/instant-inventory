/*
 * Copyright (c) 2026 Elg
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

package no.elg.ii.overlay;

import static no.elg.ii.util.InventoryUtil.INVENTORY_ITEMS_CONTAINERS;
import static no.elg.ii.util.WidgetUtils.FULLY_OPAQUE;
import static no.elg.ii.util.WidgetUtils.FULLY_TRANSPARENT;

import com.google.common.annotations.VisibleForTesting;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import net.runelite.api.BufferProvider;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.SpritePixels;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.api.widgets.WidgetUtil;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import no.elg.ii.InstantInventoryConfig;
import no.elg.ii.feature.state.InventoryState;
import no.elg.ii.model.IndexedWidget;
import no.elg.ii.service.InventoryService;
import no.elg.ii.util.Util;

/**
 * Make what other plugins draw on a faded inventory item, such as the tags of the Inventory Tags plugin, as transparent
 * as the item itself.
 * <p>
 * Other plugins draw their overlays at full opacity and cannot be told otherwise. Instead, two overlays wrap the
 * overlays drawn after an inventory interface: {@link CaptureItemPixelsOverlay} copies the pixels of each faded item
 * and draws the item opaque over them before any other overlay draws on it, and {@link BlendItemPixelsOverlay} adds
 * what the other overlays changed on the opaque item to the copies after all of them have drawn. What the other
 * overlays drew on the item thereby ends up with the same opacity as the item, and covers the item as much as it
 * covers the item when it is not faded.
 */
@Singleton
@AllArgsConstructor
@NoArgsConstructor
public class OverlayOpacityRectifier {

  /**
   * The shadow the {@code inventory_init} client script gives inventory items
   */
  private static final int INVENTORY_ITEM_SHADOW = 0x333333;

  @Inject
  private Client client;

  @Inject
  private OverlayManager overlayManager;

  @Inject
  private InventoryService inventoryService;

  @Inject
  private InventoryState state;

  @Inject
  private InstantInventoryConfig config;


  /**
   * How far outside an item other overlays draw. Inventory Tags draws its underline two pixels below the item, from
   * its left edge to one pixel past its right edge
   */
  @NonNull
  private final Insets overlayMargin = new Insets(0, 0, 0, 0);

  private final List<Snapshot> snapshots = new ArrayList<>();
  private final Overlay captureOverlay = new CaptureItemPixelsOverlay();
  private final Overlay blendOverlay = new BlendItemPixelsOverlay();

  public void recreateOverlayMargin() {
    overlayMargin.set(config.topMargin(), config.leftMargin(), config.bottomMargin(), config.rightMargin());
  }

  public void startUp() {
    recreateOverlayMargin();
    overlayManager.add(captureOverlay);
    overlayManager.add(blendOverlay);
  }

  public void shutDown() {
    overlayManager.remove(captureOverlay);
    overlayManager.remove(blendOverlay);
  }

  public void onConfigChange() {
    if (config.fixItemOpacity()) {
      startUp();
    } else {
      shutDown();
    }
  }

  /**
   * Copy the pixels of each faded item, then draw the item opaque, before other overlays draw on them
   */
  private void capture(@Nonnull Collection<WidgetItem> widgetItems) {
    snapshots.clear();
    if (widgetItems.isEmpty()) {
      return;
    }
    List<Widget> predictedWidgets = getPredictedWidgets();
    if (predictedWidgets.isEmpty()) {
      return;
    }

    BufferProvider buffer = client.getBufferProvider();
    Rectangle bufferBounds = new Rectangle(buffer.getWidth(), buffer.getHeight());
    for (WidgetItem widgetItem : widgetItems) {
      Widget widget = widgetItem.getWidget();
      if (widget.getOpacity() == FULLY_OPAQUE || !predictedWidgets.contains(widget)) {
        continue;
      }
      Rectangle area = withOverlayMargin(widgetItem.getCanvasBounds()).intersection(bufferBounds);
      if (!area.isEmpty()) {
        int[] faded = copyPixels(buffer, area);
        drawOpaque(widgetItem);
        snapshots.add(new Snapshot(area, faded, copyPixels(buffer, area), widget.getOpacity()));
      }
    }
  }

  /**
   * Draw an item over its faded self the way the client draws an opaque item, so other overlays draw on the item as
   * they do when it is not faded. Otherwise, the faded item would show through what they draw over it.
   */
  private void drawOpaque(@Nonnull WidgetItem widgetItem) {
    Widget widget = widgetItem.getWidget();
    SpritePixels sprite = client.createItemSprite(widgetItem.getId(), widgetItem.getQuantity(), widget.getBorderType(),
      INVENTORY_ITEM_SHADOW, widget.getItemQuantityMode(), false, Constants.CLIENT_DEFAULT_ZOOM);
    if (sprite != null) {
      Rectangle bounds = widgetItem.getCanvasBounds();
      sprite.drawAt(bounds.x, bounds.y);
    }
  }

  /**
   * Add what other overlays changed on the opaque items to the copied faded pixels, at the opacity of the items
   */
  private void blend() {
    if (snapshots.isEmpty()) {
      return;
    }
    BufferProvider buffer = client.getBufferProvider();
    int[] pixels = buffer.getPixels();
    int width = buffer.getWidth();
    for (Snapshot snapshot : snapshots) {
      Rectangle area = snapshot.getArea();
      int visibility = FULLY_TRANSPARENT - snapshot.getOpacity();
      for (int row = 0; row < area.height; row++) {
        for (int column = 0; column < area.width; column++) {
          int index = (area.y + row) * width + area.x + column;
          int snapshotIndex = row * area.width + column;
          pixels[index] = blendChannels(snapshot.getFaded()[snapshotIndex], snapshot.getOpaque()[snapshotIndex], pixels[index], visibility);
        }
      }
    }
    snapshots.clear();
  }

  /**
   * Add the change from {@code opaque} to {@code drawn} to {@code faded}, scaled by {@code visibility}, in each 8-bit
   * channel of a pixel. When {@code opaque} is {@code faded}, this moves from {@code faded} towards {@code drawn}.
   *
   * @param visibility How much of the change to add, from 0 (none, stay at {@code faded}) to 255 (all of it)
   */
  @VisibleForTesting
  static int blendChannels(int faded, int opaque, int drawn, int visibility) {
    int result = 0;
    for (int shift = 0; shift < Integer.SIZE; shift += Byte.SIZE) {
      int fadedChannel = (faded >>> shift) & 0xFF;
      int change = ((drawn >>> shift) & 0xFF) - ((opaque >>> shift) & 0xFF);
      result |= Util.coerceIn(fadedChannel + change * visibility / 0xFF, 0, 0xFF) << shift;
    }
    return result;
  }

  /**
   * @return The inventory widgets showing a slot this plugin is predicting
   */
  @Nonnull
  private List<Widget> getPredictedWidgets() {
    return state.getActiveSlots()
      .flatMap(iis -> inventoryService.getAllOpenInventoryWidgets().filter(slotWidget -> slotWidget.getIndex() == iis.getIndex()))
      .map(IndexedWidget::getWidget)
      .collect(Collectors.toList());
  }

  @Nonnull
  private Rectangle withOverlayMargin(@Nonnull Rectangle bounds) {
    return new Rectangle(
      bounds.x - overlayMargin.left,
      bounds.y - overlayMargin.top,
      bounds.width + overlayMargin.left + overlayMargin.right,
      bounds.height + overlayMargin.top + overlayMargin.bottom
    );
  }

  @Nonnull
  private static int[] copyPixels(@Nonnull BufferProvider buffer, @Nonnull Rectangle area) {
    int[] pixels = new int[area.width * area.height];
    for (int row = 0; row < area.height; row++) {
      System.arraycopy(buffer.getPixels(), (area.y + row) * buffer.getWidth() + area.x, pixels, row * area.width, area.width);
    }
    return pixels;
  }

  @Value
  private static class Snapshot {
    @NonNull
    Rectangle area;
    /**
     * The pixels of the faded item, as the client drew them
     */
    int[] faded;
    /**
     * The pixels of the item drawn opaque over {@link #faded}, before other overlays drew on it
     */
    int[] opaque;
    int opacity;
  }

  /**
   * Draws after the same interfaces as a {@link WidgetItemOverlay} showing on the inventory, with a priority deciding
   * whether it is drawn before or after them
   */
  private abstract static class InventoryInterfaceOverlay extends Overlay {
    InventoryInterfaceOverlay(float priority) {
      setPosition(OverlayPosition.DYNAMIC);
      setLayer(OverlayLayer.MANUAL);
      setPriority(priority);
      INVENTORY_ITEMS_CONTAINERS.stream()
        .map(WidgetUtil::componentToInterface)
        .distinct()
        .forEach(this::drawAfterInterface);
    }
  }

  private class CaptureItemPixelsOverlay extends InventoryInterfaceOverlay {
    CaptureItemPixelsOverlay() {
      super(Float.NEGATIVE_INFINITY);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
      capture(overlayManager.getWidgetItems());
      return null;
    }
  }

  private class BlendItemPixelsOverlay extends InventoryInterfaceOverlay {
    BlendItemPixelsOverlay() {
      super(Float.POSITIVE_INFINITY);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
      blend();
      return null;
    }
  }
}
