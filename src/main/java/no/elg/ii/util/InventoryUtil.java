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

package no.elg.ii.util;

import static no.elg.ii.util.WidgetUtil.FULLY_TRANSPARENT;
import static no.elg.ii.util.WidgetUtil.setFakeWidgetItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.runelite.api.Client;
import net.runelite.api.NullItemID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;

public final class InventoryUtil {
  /**
   * Number of items in an inventory
   */
  public static final int INVENTORY_SIZE = 28;

  public static boolean isInvalidInventoryIndex(int index) {
    return index < 0 || index >= INVENTORY_SIZE;
  }

  public interface Filter<T> {
    boolean filter(T t);
  }

  /**
   * Copy a widget from one container to another
   *
   * @param client      The client
   * @param source      The source container
   * @param destination The destination container
   * @param index       The index in the destination container to copy to
   */
  public static void copyWidgetFromContainer(@Nonnull Client client, @Nonnull WidgetInfo source, @Nonnull WidgetInfo destination, int index) {
    Widget srcWidgetContainer = client.getWidget(source);
    Widget dstWidgetContainer = client.getWidget(destination);
    if (srcWidgetContainer == null || dstWidgetContainer == null) {
      return;
    }
    int length = dstWidgetContainer.getDynamicChildren().length;
    if (srcWidgetContainer.getDynamicChildren().length != length || index < 0 || index >= length) {
      return;
    }
    Widget srcWidget = srcWidgetContainer.getChild(index);
    Widget dstWidget = dstWidgetContainer.getChild(index);

    setFakeWidgetItem(dstWidget, srcWidget);
  }

  @Nullable
  public static Widget findFirst(@Nonnull Client client, @Nonnull WidgetInfo widgetInfo, @Nonnull Filter<Widget> filter) {
    Widget invWidget = client.getWidget(widgetInfo);
    if (invWidget == null) {
      return null;
    }
    Widget[] inventoryWidgets = invWidget.getDynamicChildren();
    for (Widget widget : inventoryWidgets) {
      if (filter.filter(widget)) {
        return widget;
      }
    }
    return null;
  }


  /**
   * There is no method to call to check if a slot is not empty, so we just check if they appear to be empty
   */
  public static boolean isEmpty(@Nonnull Widget widget) {
    return widget.isHidden() || widget.getName().isBlank() || widget.getOpacity() == FULLY_TRANSPARENT || widget.getItemId() == NullItemID.NULL_6512;
  }

  @Nullable
  public static Widget findFirstEmptySlot(@Nonnull Client client, @Nonnull WidgetInfo widgetInfo) {
    return findFirst(client, widgetInfo, InventoryUtil::isEmpty);
  }

  private InventoryUtil() {
  }
}
