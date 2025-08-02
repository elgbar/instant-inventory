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
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;

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

  public static final Set<Integer> INVENTORY_ITEMS_CONTAINERS = Set.of(
    InterfaceID.Inventory.ITEMS, // Normal inventory
    InterfaceID.Bankside.ITEMS, // Normal inventory when the bank is open
    InterfaceID.Bankside.WORNOPS, // Inventory when equipping items in the bank
    InterfaceID.GeOffersSide.ITEMS, // Inventory when inside the Grand Exchange interface
    InterfaceID.BankDepositbox.INVENTORY, // When using the deposit box
    InterfaceID.GePricecheckerSide.ITEMS, // When price checking items ('View guide prices' in the 'Worn Equipment' tab)
    InterfaceID.EquipmentSide.ITEMS, //When showing 'View equipment stats' in the 'Worn Equipment' tab
    InterfaceID.SharedBankSide.ITEMS, // Inventory when in group storage
    InterfaceID.InvoverlayNoops.ITEMS, // When the inventory is open, but it's not interactable (e.g. in the deposit box)
    InterfaceID.InventoryNoops.ITEMS // TODO unknown, thought it was InvoverlayNoops.ITEMS

    //Might be used in the future or broken:
//    InterfaceID.SeedVaultDeposit.INV, // When using the seed vault in the Farming Guild (currently buggy, when trying to deposit non-seed items)
//    InterfaceID.Shopside.ITEMS // When in a shop interface
  );
}
