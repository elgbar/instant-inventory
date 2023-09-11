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

import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

  @Nullable
  public static Widget findFirst(@Nonnull Client client, WidgetInfo widgetInfo, @Nonnull Filter<Widget> filter) {
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

  public static final Filter<Widget> IS_EMPTY_FILTER = w -> w.getName().isBlank();

  @Nullable
  public static Widget firstEmptyWidget(@Nonnull Client client) {
    return findFirst(client, WidgetInfo.INVENTORY, IS_EMPTY_FILTER);
  }

  private InventoryUtil() {
  }
}
