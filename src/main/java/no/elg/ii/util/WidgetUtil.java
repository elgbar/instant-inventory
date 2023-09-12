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

import net.runelite.api.Item;
import net.runelite.api.widgets.Widget;

public final class WidgetUtil {
  private WidgetUtil() {
  }

  public static final int FULLY_TRANSPARENT = 255;
  public static final int HALF_TRANSPARENT = FULLY_TRANSPARENT / 2;
  public static final int FULLY_OPAQUE = 0;

  public static String getWidgetInfo(Widget widget) {
    return widget.getName() + " (id: " + widget.getItemId() + ", index: " + widget.getIndex() + ")";
  }

//  public static void updateContainerSlot(@Nullable Widget containerWidget, int index, int itemId, int amount) {
//    if (containerWidget != null && containerWidget.getDynamicChildren().length > index) {
//      var childWidget = containerWidget.getChild(index);
//      setFakeWidgetItem(childWidget, itemId, amount);
//    }
//  }

  public static void setFakeWidgetItem(Widget widget, int itemId, int amount) {
    widget.setHidden(false);
    widget.setOpacity(HALF_TRANSPARENT);

    widget.setItemId(itemId);
    widget.setItemQuantity(amount);
  }

  public static void setFakeWidgetItem(Widget widget, Item fromWidget) {
    setFakeWidgetItem(widget, fromWidget.getId(), fromWidget.getQuantity());
  }

  public static void setFakeWidgetItem(Widget widget, Widget fromWidget) {
    setFakeWidgetItem(widget, fromWidget.getItemId(), fromWidget.getItemQuantity());

    widget.setItemQuantityMode(widget.getItemQuantityMode());
    widget.setName(widget.getName());
  }

  public static void updateQuantity(Widget widget, int delta) {
    widget.setItemQuantity(widget.getItemQuantity() - delta);
    if (widget.getItemQuantity() == 0) {
      widget.setOpacity(HALF_TRANSPARENT);
    }
  }

  public static void setQuantity(Widget widget, int quantity) {
    widget.setItemQuantity(quantity);
    if (widget.getItemQuantity() == 0) {
      widget.setOpacity(HALF_TRANSPARENT);
    }
  }
}
