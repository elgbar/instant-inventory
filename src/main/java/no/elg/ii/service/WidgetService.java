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

package no.elg.ii.service;

import static no.elg.ii.util.WidgetUtils.FULLY_TRANSPARENT;
import static no.elg.ii.util.WidgetUtils.setOpacity;
import static no.elg.ii.util.WidgetUtils.setWidgetNameIfPresent;
import static no.elg.ii.util.WidgetUtils.updateVisibleWidget;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.NoArgsConstructor;
import net.runelite.api.Item;
import net.runelite.api.widgets.Widget;
import no.elg.ii.InstantInventoryConfig;
import no.elg.ii.inventory.slot.SlotChange;
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

  public int getFillOpacity() {
    return intPercentToOpacityValue(config.fillOpacityPercent());
  }

  /**
   * @return The opacity of an inventory slot predicted to change like {@code change}
   */
  public int getOpacity(@Nonnull SlotChange change) {
    switch (change) {
      case EMPTY:
        return getHideOpacity();
      case FILL:
        return getFillOpacity();
      default:
        return getChangeOpacity();
    }
  }

  public int getPrayerEnableOpacity() {
    return intPercentToOpacityValue(config.prayerEnableOpacityPercent());
  }

  public int getPrayerDisableOpacity() {
    return intPercentToOpacityValue(config.prayerDisableOpacityPercent());
  }

  /**
   * Set the opacity of the widget to the user specified change opacity
   *
   */
  public void setAsChangeOpacity(@Nonnull Widget widget) {
    setOpacity(widget, getChangeOpacity());
  }

  /**
   * Set the opacity of the widget to the user specified hide opacity
   *
   */
  public void setAsHideOpacity(@Nonnull Widget widget) {
    setOpacity(widget, getHideOpacity());
  }

  /**
   * @param change How the slot of {@code dstWidget} is predicted to change, which decides the opacity of the item
   */
  public void setFakeWidgetItem(@Nonnull Widget dstWidget, int itemId, int amount, @Nullable String maybeName, @Nonnull SlotChange change) {
    setWidgetNameIfPresent(dstWidget, maybeName);
    updateVisibleWidget(dstWidget, itemId, amount);
    setOpacity(dstWidget, getOpacity(change));
  }

  public void setFakeWidgetItem(@Nonnull Widget dstWidget, @Nonnull Item srcItem, @Nonnull SlotChange change) {
    setFakeWidgetItem(dstWidget, srcItem.getId(), srcItem.getQuantity(), null, change);
  }
}
