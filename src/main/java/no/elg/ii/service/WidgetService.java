/*
 * Copyright (c) 2023-2025 Elg
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

package no.elg.ii.service;

import static no.elg.ii.util.WidgetUtils.FULLY_OPAQUE;
import static no.elg.ii.util.WidgetUtils.FULLY_TRANSPARENT;
import static no.elg.ii.util.WidgetUtils.THE_EMPTY_ITEM_ID;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.NoArgsConstructor;
import net.runelite.api.Item;
import net.runelite.api.widgets.ItemQuantityMode;
import net.runelite.api.widgets.Widget;
import no.elg.ii.InstantInventoryConfig;
import no.elg.ii.util.Util;
import no.elg.ii.util.WidgetUtils;


@Singleton
@NoArgsConstructor
public class WidgetService {

  @Inject
  private InstantInventoryConfig config;

  private static final int FULLY_OPAQUE_INT_PERCENT = 100;
  private static final int FULLY_TRANSPARENT_INT_PERCENT = 0;

  /**
   * Convert a number between {@link #FULLY_TRANSPARENT_INT_PERCENT} and {@link #FULLY_OPAQUE_INT_PERCENT} to the
   * corresponding opacity value between {@link WidgetUtils#FULLY_OPAQUE} and {@link WidgetUtils#FULLY_TRANSPARENT}
   */
  private static int intPercentToOpacityValue(int opacityPercent) {
    int validOpacityIntPercent = Util.coerceIn(opacityPercent, FULLY_TRANSPARENT_INT_PERCENT, FULLY_OPAQUE_INT_PERCENT);
    double percent = validOpacityIntPercent / (double) FULLY_OPAQUE_INT_PERCENT;
    return (int) ((1d - percent) * FULLY_TRANSPARENT);
  }

  public int getChangeOpacity() {
    return intPercentToOpacityValue(config.changeOpacityPercent());
  }

  public int getHideOpacity() {
    return intPercentToOpacityValue(config.hideOpacityPercent());
  }

  /**
   * Set the opacity of the widget to the user specified change opacity
   *
   * @param hideFully If the widget item id can be changed if the opacity is fully transparent
   */
  public void setAsChangeOpacity(@Nonnull Widget widget, boolean hideFully) {
    setOpacity(widget, getChangeOpacity(), hideFully);
  }

  /**
   * Set the opacity of the widget to the user specified hide opacity
   *
   * @param hideFully If the widget item id can be changed if the opacity is fully transparent
   */
  public void setAsHideOpacity(@Nonnull Widget widget, boolean hideFully) {
    setOpacity(widget, getHideOpacity(), hideFully);
  }

  /**
   * Make the widget fully visible
   */
  public void setAsFullyOpaque(@Nonnull Widget widget) {
    setOpacity(widget, FULLY_OPAQUE, false);
  }

  /**
   * Change the opacity of the widget to {@code opacity}
   *
   * @param hideFully If the widget item id can be changed if the opacity is fully transparent
   */
  public void setOpacity(@Nonnull Widget widget, int opacity, boolean hideFully) {
    if (hideFully && opacity == FULLY_TRANSPARENT) {
      //When the opacity should be fully transparent we want to change the widget to not display overlay for the given item
      setEmptyItem(widget);
    }
    widget.setHidden(false);
    widget.setOpacity(opacity);
  }

  /**
   * Set the itemId and quantity of a widget without changing the opacity or any other attributes
   */
  public void updateVisibleWidget(@Nonnull Widget dstWidget, int itemId, int amount) {
    dstWidget.setItemId(itemId);
    dstWidget.setItemQuantity(amount);
  }

  public void updateVisibleWidget(@Nonnull Widget dstWidget, @Nonnull Item srcItem) {
    updateVisibleWidget(dstWidget, srcItem.getId(), srcItem.getQuantity());
  }

  public void setEmptyItem(@Nonnull Widget widget) {
    widget.setItemQuantityMode(ItemQuantityMode.NEVER);
    setFakeWidgetItem(widget, THE_EMPTY_ITEM_ID, 1);
  }

  public void setFakeWidgetItem(@Nonnull Widget dstWidget, int itemId, int amount) {
    updateVisibleWidget(dstWidget, itemId, amount);
    setAsChangeOpacity(dstWidget, false); //this can never be true as it will cause an infinite loop
  }

  public void setFakeWidgetItem(@Nonnull Widget dstWidget, @Nonnull Item srcItem) {
    setFakeWidgetItem(dstWidget, srcItem.getId(), srcItem.getQuantity());
  }

  /**
   * Add {@code delta} from the quantity of {@code widget}
   *
   * @param widget the widget to update
   * @param delta  the amount to add
   */
  public void updateQuantity(@Nonnull Widget widget, int delta) {
    setQuantity(widget, widget.getItemQuantity() + delta);
  }

  /**
   * Set the quantity of {@code widget} to {@code quantity}
   */
  public void setQuantity(@Nonnull Widget widget, int quantity) {
    widget.setItemQuantity(quantity);
  }
}
