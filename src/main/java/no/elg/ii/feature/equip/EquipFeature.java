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

package no.elg.ii.feature.equip;

import static no.elg.ii.util.InventoryUtil.INVENTORY_SIZE;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.NullItemID;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.http.api.item.ItemEquipmentStats;
import net.runelite.http.api.item.ItemStats;
import no.elg.ii.feature.Feature;
import no.elg.ii.inventory.InventoryState;
import no.elg.ii.inventory.slot.ReplacementInventorySlot;
import no.elg.ii.util.IndexedItem;
import no.elg.ii.util.WidgetUtil;
import org.apache.commons.lang3.tuple.Pair;

@Singleton
@Slf4j
public class EquipFeature implements Feature {

  public static final String WEAR_OPTION = "Wear";
  public static final String WIELD_OPTION = "Wield";
  public static final String EQUIP_CONFIG_KEY = "instantEquip";

  @Inject
  @VisibleForTesting
  public EquipOverlay overlay;
  @Inject
  @VisibleForTesting
  public OverlayManager overlayManager;

  @Inject
  @Getter
  private InventoryState state;

  @Inject
  @VisibleForTesting
  ItemManager itemManager;

  @Inject
  @VisibleForTesting
  Client client;

  @Override
  public void onEnable() {
    overlayManager.add(overlay);
  }

  @Override
  public void onDisable() {
    overlayManager.remove(overlay);
  }

  @Subscribe
  public void onMenuOptionClicked(final MenuOptionClicked event) {
    Widget widget = event.getWidget();
    if (widget != null) {
      String menuOption = event.getMenuOption();
      if (WIELD_OPTION.equals(menuOption) || WEAR_OPTION.equals(menuOption)) {
        log.debug("Equipped item {}", WidgetUtil.getWidgetInfo(widget));
        hide(widget);
      }
    }
  }

  protected void hide(Widget widget) {
    Pair<Item, Item> itemIds = getEquipmentToReplace(widget);
    ItemContainer inventoryContainer = client.getItemContainer(InventoryID.INVENTORY);
    if (inventoryContainer == null) {
      return;
    }

    @Nullable IndexedItem mainIndexedItem = IndexedItem.of(widget.getIndex(), itemIds.getLeft());
    @Nullable IndexedItem offhandIndexedItem = null;
    if (mainIndexedItem != null) {
      widget.setItemId(mainIndexedItem.getItem().getId());

      Item offhandItem = itemIds.getRight();
      if (offhandItem != null) {
        Widget invWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (invWidget == null) {
          return;
        }
        Widget[] inventoryWidgets = invWidget.getDynamicChildren();
        if (inventoryWidgets.length != INVENTORY_SIZE) {
          log.warn("Inventory widget has {} children, expected {}", inventoryWidgets.length, INVENTORY_SIZE);
          return;
        }
        for (int index = 0; index < inventoryWidgets.length; index++) {
          if (inventoryWidgets[index].getName().isBlank()) {
            offhandIndexedItem = IndexedItem.of(index, offhandItem);
            break;
          }
        }
      }
    } else {
      widget.setItemId(NullItemID.NULL_6512);
    }

    getState().setSlot(widget.getIndex(), new ReplacementInventorySlot(client.getTickCount(), widget.getItemId(), mainIndexedItem, offhandIndexedItem));
  }


  @Nullable
  private Item getItemFromContainer(ItemContainer container, int slotID) {
    return container.getItem(slotID);
  }

  /**
   * @param widget the widget to equip
   * @return The item that was equipped (left) and potentially the off-hand item that was equipped (right) if it will be unequipped
   */
  @Nonnull
  private Pair<Item, Item> getEquipmentToReplace(Widget widget) {
    final ItemStats itemStats = itemManager.getItemStats(widget.getItemId(), false);
    if (itemStats == null || !itemStats.isEquipable()) {
      return Pair.of(null, null);
    }
    Item replaced = null;
    // Used if switching into a 2 handed weapon to store off-hand stats
    Item offHand = null;
    final ItemEquipmentStats currentEquipment = itemStats.getEquipment();

    ItemContainer equipmentContainer = client.getItemContainer(InventoryID.EQUIPMENT);
    if (currentEquipment != null && equipmentContainer != null) {
      final int slot = currentEquipment.getSlot();
      replaced = getItemFromContainer(equipmentContainer, slot);
      if (replaced == null && slot == EquipmentInventorySlot.SHIELD.getSlotIdx()) {
        var weaponItem = getItemFromContainer(equipmentContainer, EquipmentInventorySlot.WEAPON.getSlotIdx());
        if (weaponItem != null) {
          ItemStats weaponStat = itemManager.getItemStats(weaponItem.getId(), false);
          if (weaponStat != null && weaponStat.isEquipable()) {
            replaced = weaponItem;
          }
        }
      } else if (slot == EquipmentInventorySlot.WEAPON.getSlotIdx() && currentEquipment.isTwoHanded()) {
        offHand = getItemFromContainer(equipmentContainer, EquipmentInventorySlot.SHIELD.getSlotIdx());
      }
    }
    return Pair.of(replaced, offHand);
  }

  @Nonnull
  @Override
  public String getConfigKey() {
    return EQUIP_CONFIG_KEY;
  }
}
