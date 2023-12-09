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

package no.elg.ii.service;

import static no.elg.ii.util.WidgetUtils.FULLY_OPAQUE;
import static no.elg.ii.util.WidgetUtils.FULLY_TRANSPARENT;
import static no.elg.ii.util.WidgetUtils.THE_EMPTY_ITEM_ID;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Item;
import net.runelite.api.widgets.Widget;
import no.elg.ii.InstantInventoryConfig;
import no.elg.ii.inventory.slot.InventorySlot;

@Singleton
public class WidgetService {

  @Inject
  private InstantInventoryConfig config;

  private static final int FULLY_OPAQUE_INT_PERCENT = 100;
  private static final int FULLY_TRANSPARENT_INT_PERCENT = 0;

  public int getChangeOpacity() {
    return (int) ((1d - (config.changeOpacityPercent() / 100d)) * FULLY_TRANSPARENT);
  }

  public int getHideOpacity() {
    return (int) ((1d - (config.hideOpacityPercent() / 100d)) * FULLY_TRANSPARENT);
  }


  /**
   * Set the opacity of the widget to the user specified change opacity
   *
   * @param hideFully If the widget item id can be changed if the opacity is fully transparent
   */
  public void setAsChangeOpacity(@Nonnull Widget widget, boolean hideFully) {
    setOpacity(widget, getChangeOpacity(), hideFully);
  }

  public void setAsChangeOpacity(@Nonnull Widget widget) {
    setOpacity(widget, getChangeOpacity(), true);
  }


  /**
   * Set the opacity of the widget to the user specified hide opacity
   */
  public void setAsHideOpacity(@Nonnull Widget widget) {
    setOpacity(widget, getHideOpacity());
  }

  public void setAsHideOpacity(@Nonnull Widget widget) {
    setOpacity(widget, getHideOpacity());
  }

  /**
   * Make the widget fully visible
   */
  public void setAsFullyOpaque(@Nonnull Widget widget) {
    setOpacity(widget, FULLY_OPAQUE, false);
  }

  public void setOpacity(@Nonnull Widget widget, int opacity) {
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

  public void setFakeWidgetItem(@Nonnull Widget dstWidget, int itemId, int amount) {
    updateVisibleWidget(dstWidget, itemId, amount);
    setAsChangeOpacity(dstWidget);
  }

  public void setFakeWidgetItem(@Nonnull Widget dstWidget, @Nonnull Item srcItem) {
    setFakeWidgetItem(dstWidget, srcItem.getId(), srcItem.getQuantity());
  }

  public void setFakeWidgetItem(@Nonnull Widget dstWidget, @Nonnull InventorySlot srcItem) {
    setFakeWidgetItem(dstWidget, srcItem.getItemId(), srcItem.getQuantity());
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
    if (widget.getItemQuantity() == 0) {
      setAsChangeOpacity(widget);
    }
  }
}
