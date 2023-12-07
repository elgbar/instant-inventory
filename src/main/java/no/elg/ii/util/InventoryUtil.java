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

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.runelite.api.Client;
import net.runelite.api.annotations.Component;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetUtil;

public final class InventoryUtil {
  /**
   * Number of items in an inventory
   */
  public static final int INVENTORY_SIZE = 28;

  public interface Filter<T> {
    boolean filter(T t);
  }

  @Nullable
  public static Widget findFirst(@Nonnull Client client, @Component int componentId, @Nonnull Filter<Widget> filter) {
    Widget invWidget = client.getWidget(componentId);
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
  public static Widget findFirstEmptySlot(@Nonnull Client client, @Component int componentId) {
    return findFirst(client, componentId, WidgetUtils::isEmpty);
  }

  public static final int GROUP_ITEM_CONTAINER = WidgetUtil.packComponentId(InterfaceID.GROUP_STORAGE_INVENTORY, 0);
  public static final int DEPOSIT_BOX_ITEM_CONTAINER = WidgetUtil.packComponentId(268, 0);
  public static final int BANK_WITHDRAW_AS_ITEM = WidgetUtil.packComponentId(InterfaceID.BANK, 22);
  public static final int BANK_WITHDRAW_AS_NOTE = WidgetUtil.packComponentId(InterfaceID.BANK, 24);

  public static final List<Integer> INVENTORY_ITEMS_CONTAINERS = List.of(
    InterfaceID.INVENTORY,
    ComponentID.EQUIPMENT_INVENTORY_ITEM_CONTAINER,
    ComponentID.BANK_INVENTORY_ITEM_CONTAINER,
    ComponentID.GRAND_EXCHANGE_INVENTORY_INVENTORY_ITEM_CONTAINER,
    ComponentID.DEPOSIT_BOX_INVENTORY_ITEM_CONTAINER,
    ComponentID.SHOP_INVENTORY_ITEM_CONTAINER,
    ComponentID.SMITHING_INVENTORY_ITEM_CONTAINER,
    ComponentID.GUIDE_PRICES_INVENTORY_ITEM_CONTAINER,
    ComponentID.SEED_VAULT_INVENTORY_ITEM_CONTAINER,
    GROUP_ITEM_CONTAINER,
    DEPOSIT_BOX_ITEM_CONTAINER
  );

  private InventoryUtil() {
  }
}
