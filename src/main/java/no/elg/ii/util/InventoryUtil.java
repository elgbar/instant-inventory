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

import static no.elg.ii.util.WidgetUtil.setFakeWidgetItem;

import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
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

  @Nullable
  public static Widget findFirstEmptySlot(@Nonnull Client client, @Nonnull WidgetInfo widgetInfo) {
    return findFirst(client, widgetInfo, WidgetUtil::isEmpty);
  }

  public static final AdditionalWidgetInfo GROUP_ITEM_CONTAINER = new AdditionalWidgetInfo(WidgetID.GROUP_STORAGE_INVENTORY_GROUP_ID, 0);
  public static final AdditionalWidgetInfo DEPOSIT_BOX_ITEM_CONTAINER = new AdditionalWidgetInfo(268, 0);

  public static final List<AdditionalWidgetInfo> INVENTORY_ITEMS_CONTAINERS = List.of(
    AdditionalWidgetInfo.fromWidgetInfo(WidgetInfo.INVENTORY),
    AdditionalWidgetInfo.fromWidgetInfo(WidgetInfo.EQUIPMENT_INVENTORY_ITEMS_CONTAINER),
    AdditionalWidgetInfo.fromWidgetInfo(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER),
    AdditionalWidgetInfo.fromWidgetInfo(WidgetInfo.GRAND_EXCHANGE_INVENTORY_ITEMS_CONTAINER),
    AdditionalWidgetInfo.fromWidgetInfo(WidgetInfo.DEPOSIT_BOX_INVENTORY_ITEMS_CONTAINER),
    AdditionalWidgetInfo.fromWidgetInfo(WidgetInfo.SHOP_INVENTORY_ITEMS_CONTAINER),
    AdditionalWidgetInfo.fromWidgetInfo(WidgetInfo.SMITHING_INVENTORY_ITEMS_CONTAINER),
    AdditionalWidgetInfo.fromWidgetInfo(WidgetInfo.GUIDE_PRICES_INVENTORY_ITEMS_CONTAINER),
    AdditionalWidgetInfo.fromWidgetInfo(WidgetInfo.SEED_VAULT_INVENTORY_ITEMS_CONTAINER),
    GROUP_ITEM_CONTAINER,
    DEPOSIT_BOX_ITEM_CONTAINER
  );

  @Nonnull
  public static Stream<Widget> getOpenWidgetItemContainer(Client client) {
    return INVENTORY_ITEMS_CONTAINERS.stream()
      .map(widgetInfo -> client.getWidget(widgetInfo.getGroupId(), widgetInfo.getChildId()))
      .filter(widget -> widget != null && !widget.isHidden());
  }

  /**
   * @param inventoryId {@link net.runelite.api.InventoryID}
   * @return The widget info for the given inventory id or {@code null} if there is no such widget
   * @see net.runelite.api.InventoryID
   */
  @Nullable
  public static WidgetInfo inventoryIdToWidget(int inventoryId) {
    if (inventoryId == InventoryID.INVENTORY.getId()) {
      return WidgetInfo.INVENTORY;
    } else if (inventoryId == InventoryID.BANK.getId()) {
      return WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER;
    } else {
      return null;
    }
  }

  private InventoryUtil() {
  }
}
