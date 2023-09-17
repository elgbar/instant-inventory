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

import javax.annotation.Nonnull;
import net.runelite.api.Item;
import net.runelite.api.NullItemID;
import net.runelite.api.widgets.Widget;

public final class WidgetUtil {
  private WidgetUtil() {
  }

  /**
   * Item is fully transparent, not visible
   */
  public static final int FULLY_TRANSPARENT = 255;
  public static final int HALF_TRANSPARENT = FULLY_TRANSPARENT / 2;
  /**
   * Item is fully opaque, visible
   */
  public static final int FULLY_OPAQUE = 0;

  @Nonnull
  public static String getWidgetInfo(@Nonnull Widget widget) {
    return widget.getName() + " (id: " + widget.getItemId() + ", index: " + widget.getIndex() + ")";
  }

  public static void unhide(@Nonnull Widget widget) {
    widget.setHidden(false);
    widget.setOpacity(HALF_TRANSPARENT);
  }

  public static void updateVisibleWidget(@Nonnull Widget dstWidget, int itemId, int amount) {
    dstWidget.setItemId(itemId);
    dstWidget.setItemQuantity(amount);

  }

  public static void updateVisibleWidget(@Nonnull Widget dstWidget, @Nonnull Item srcItem) {
    updateVisibleWidget(dstWidget, srcItem.getId(), srcItem.getQuantity());
  }

  public static void setFakeWidgetItem(@Nonnull Widget dstWidget, int itemId, int amount) {
    unhide(dstWidget);
    updateVisibleWidget(dstWidget, itemId, amount);
  }

  public static void setFakeWidgetItem(@Nonnull Widget dstWidget, @Nonnull Item srcItem) {
    setFakeWidgetItem(dstWidget, srcItem.getId(), srcItem.getQuantity());
  }

  public static void setFakeWidgetItem(@Nonnull Widget dstWidget, @Nonnull Widget srcWidget) {
    setFakeWidgetItem(dstWidget, srcWidget.getItemId(), srcWidget.getItemQuantity());

    dstWidget.setItemQuantityMode(dstWidget.getItemQuantityMode());
    dstWidget.setName(dstWidget.getName());
  }

  /**
   * Add {@code delta} from the quantity of {@code widget}
   *
   * @param widget the widget to update
   * @param delta  the amount to add
   */
  public static void updateQuantity(@Nonnull Widget widget, int delta) {
    setQuantity(widget, widget.getItemQuantity() + delta);
  }

  public static void setQuantity(@Nonnull Widget widget, int quantity) {
    widget.setItemQuantity(quantity);
    if (widget.getItemQuantity() == 0) {
      widget.setOpacity(HALF_TRANSPARENT);
    }
  }

  /**
   * There is no method to call to check if a slot is not empty, so we just check if they appear to be empty
   */
  public static boolean isEmpty(@Nonnull Widget widget) {
    return widget.isHidden() || widget.getName().isEmpty() || widget.getOpacity() == FULLY_TRANSPARENT || widget.getItemId() == NullItemID.NULL_6512;
  }
}
