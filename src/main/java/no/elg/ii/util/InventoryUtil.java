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

package no.elg.ii.util;

import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.annotations.Component;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetUtil;

@Slf4j
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class InventoryUtil {
  /**
   * Number of items in an inventory
   */
  public static final int INVENTORY_SIZE = 28;

  @Nullable
  public static Widget findFirst(@Nonnull Client client, @Component int componentId, @Nonnull Predicate<Widget> filter) {
    Widget invWidget = client.getWidget(componentId);
    if (invWidget == null) {
      return null;
    }
    Widget[] inventoryWidgets = invWidget.getDynamicChildren();
    for (Widget widget : inventoryWidgets) {
      if (filter.test(widget)) {
        return widget;
      }
    }
    return null;
  }

  public static final int BANK_WITHDRAW_AS_ITEM = WidgetUtil.packComponentId(InterfaceID.BANK, 22);
  public static final int BANK_WITHDRAW_AS_NOTE = WidgetUtil.packComponentId(InterfaceID.BANK, 24);

  public static final int GROUP_ITEM_CONTAINER = WidgetUtil.packComponentId(InterfaceID.GROUP_STORAGE_INVENTORY, 0);
  public static final int DEPOSIT_BOX_ITEM_CONTAINER = WidgetUtil.packComponentId(268, 0);
  public static final int RESIZABLE_GUIDE_PRICES_INVENTORY_ITEM_CONTAINER = WidgetUtil.packComponentId(238, 0);
  public static final int RESIZABLE_VIEW_EQUIPMENT_STATUS_INVENTORY_ITEM_CONTAINER = WidgetUtil.packComponentId(85, 0);

  public static final Set<Integer> INVENTORY_ITEMS_CONTAINERS = Set.of(
    ComponentID.INVENTORY_CONTAINER,
    ComponentID.EQUIPMENT_INVENTORY_ITEM_CONTAINER,
    ComponentID.BANK_INVENTORY_ITEM_CONTAINER,
    ComponentID.GRAND_EXCHANGE_INVENTORY_INVENTORY_ITEM_CONTAINER,
    ComponentID.DEPOSIT_BOX_INVENTORY_ITEM_CONTAINER,
    ComponentID.SHOP_INVENTORY_ITEM_CONTAINER,
    ComponentID.SMITHING_INVENTORY_ITEM_CONTAINER,
    ComponentID.GUIDE_PRICES_INVENTORY_ITEM_CONTAINER,
    RESIZABLE_GUIDE_PRICES_INVENTORY_ITEM_CONTAINER,
    ComponentID.SEED_VAULT_INVENTORY_ITEM_CONTAINER,
    ComponentID.BANK_INVENTORY_EQUIPMENT_ITEM_CONTAINER,
    ComponentID.FIXED_VIEWPORT_INVENTORY_CONTAINER,
    ComponentID.FIXED_VIEWPORT_BANK_CONTAINER,
    GROUP_ITEM_CONTAINER,
    DEPOSIT_BOX_ITEM_CONTAINER,
    RESIZABLE_VIEW_EQUIPMENT_STATUS_INVENTORY_ITEM_CONTAINER
  );
}
