/*
 * Copyright (c) 2023-2024 Elg
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

public final class WidgetUtils {
  private WidgetUtils() {
  }

  /**
   * Item is fully transparent, i.e., not visible
   */
  public static final int FULLY_TRANSPARENT = 255;
  public static final int NEARLY_TRANSPARENT = 200;
  /**
   * Semi transparent item, same opacity as when entering bank pin
   */
  public static final int HALF_TRANSPARENT = 100;

  /**
   * The opacity of a bank item with a quantity of zero
   */
  public static final int ZERO_QUANTITY_BANK_ITEM_OPACITY = 120;
  /**
   * Item is fully opaque, i.e., visible
   */
  public static final int FULLY_OPAQUE = 0;

  public static final int THE_EMPTY_ITEM_ID = NullItemID.NULL_6512;

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
  public static boolean isEmpty(@Nonnull Widget widget) {
    return widget.isHidden()
      || widget.getName().isEmpty()
      || widget.getOpacity() == FULLY_TRANSPARENT
      || widget.getItemId() == THE_EMPTY_ITEM_ID;
  }

  public static boolean isNotEmpty(@Nonnull Widget widget) {
    return !isEmpty(widget);
  }
}
