/*
 * Copyright (c) 2023-2026 Elg
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

import static net.runelite.api.gameval.ItemID.BLANKOBJECT;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import net.runelite.api.Item;
import net.runelite.api.widgets.Widget;
import no.elg.ii.model.IndexedWidget;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class WidgetUtils {

  /**
   * Item is fully transparent, i.e., not visible
   */
  public static final int FULLY_TRANSPARENT = 255;

  /**
   * The opacity of a bank item with a quantity of zero
   */
  public static final int ZERO_QUANTITY_BANK_ITEM_OPACITY = 120;

  /**
   * Item is fully opaque, i.e., visible
   */
  public static final int FULLY_OPAQUE = 0;

  @Nonnull
  public static String debugInfo(@Nonnull Widget widget) {
    return widget.getName() + " id: " + widget.getItemId() + ", index: " + widget.getIndex() + ", quantity: " + widget.getItemQuantity() + " opacity: " + widget.getOpacity();
  }

  @Nonnull
  public static String debugInfo(@Nonnull Item item) {
    return "id: " + item.getId() + ", quantity: " + item.getQuantity();
  }

  /**
   * There is no method to call to check if a slot is not empty, so we just check if they appear to be empty
   */
  public static boolean isEmpty(@Nonnull IndexedWidget indexedWidget) {
    return isEmpty(indexedWidget.getWidget());
  }

  /**
   * There is no method to call to check if a slot is not empty, so we just check if they appear to be empty
   */
  public static boolean isEmpty(@Nonnull Widget widget) {
    return widget.isHidden()
      || widget.getName().isEmpty()
      || widget.getOpacity() == FULLY_TRANSPARENT
      || widget.getItemId() == BLANKOBJECT;
  }

  public static boolean isNotEmpty(@Nonnull Widget widget) {
    return !isEmpty(widget);
  }

  /**
   * Change the opacity of the widget to {@code opacity}
   */
  public static void setOpacity(@Nonnull Widget widget, int opacity) {
    setOpacityAndUnhide(widget, opacity);
    widget.setHidden(opacity == FULLY_TRANSPARENT);
  }

  /**
   * Used if we reset the state to an empty item, then we don't track it anymore so it must be not hidden by us.
   * <p>
   * Change the opacity of the widget to {@code opacity} and unhide it.
   */
  public static void setOpacityAndUnhide(@Nonnull Widget widget, int opacity) {
    assert 0 <= opacity && opacity <= FULLY_TRANSPARENT;
    widget.setOpacity(opacity);
    widget.setHidden(false);
  }

  /**
   * Set the itemId and quantity of a widget without changing the opacity or any other attributes
   */
  public static void updateVisibleWidget(@Nonnull Widget dstWidget, int itemId, int amount) {
    dstWidget.setItemId(itemId);
    dstWidget.setItemQuantity(amount);
  }

  public static void updateVisibleWidget(@Nonnull Widget dstWidget, @Nonnull Item srcItem) {
    updateVisibleWidget(dstWidget, srcItem.getId(), srcItem.getQuantity());
  }

  /**
   * Set the name of the widget if the name is not null
   */
  public static void setWidgetNameIfPresent(@Nonnull Widget widget, @Nullable String maybeName) {
    if (maybeName != null) {
      widget.setName(maybeName);
    }
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

  /**
   * Set the quantity of {@code widget} to {@code quantity}
   */
  public static void setQuantity(@Nonnull Widget widget, int quantity) {
    widget.setItemQuantity(quantity);
  }
}
