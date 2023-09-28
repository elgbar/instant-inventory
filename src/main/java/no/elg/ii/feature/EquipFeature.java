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

package no.elg.ii.feature;

import static no.elg.ii.util.InventoryUtil.findFirstEmptySlot;

import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.http.api.item.ItemEquipmentStats;
import net.runelite.http.api.item.ItemStats;
import no.elg.ii.inventory.InventoryState;
import no.elg.ii.service.EnsureWidgetStateService;
import no.elg.ii.service.WidgetService;
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
  ItemManager itemManager;

  @Inject
  @VisibleForTesting
  Client client;

  @Inject
  @Getter
  private InventoryState state;
  @Inject
  private WidgetService widgetService;
  @Inject
  private EnsureWidgetStateService ensureWidgetStateService;


  @Subscribe
  public void onBeforeRender(BeforeRender event) {
    ensureWidgetStateService.forceWidgetState(getState(), WidgetUtil::isNotEmpty, widgetService::setAsChangeOpacity);
  }

  @Subscribe
  public void onMenuOptionClicked(final MenuOptionClicked event) {
    Widget widget = event.getWidget();
    if (widget != null) {
      String menuOption = event.getMenuOption();
      if (WIELD_OPTION.equals(menuOption) || WEAR_OPTION.equals(menuOption)) {
        log.debug("Equipped item {}", WidgetUtil.getWidgetInfo(widget));
        equip(widget);
      }
    }
  }

  @Subscribe
  public void onChatMessage(ChatMessage event) {
    if (event.getType() == ChatMessageType.GAMEMESSAGE && Objects.equals(event.getMessage(), "You are not a high enough level to use this item.")) {
      log.debug("Failed to equip item?");
      getState().getActiveSlots().filter(is -> is.getSlot().getChangedTick() == client.getTickCount()).forEach(is -> getState().resetState(is.getIndex()));
    }
  }

  protected void equip(Widget widget) {
    Pair<Item, Item> itemIds = getEquipmentToReplace(widget);
    ItemContainer inventoryContainer = client.getItemContainer(InventoryID.INVENTORY);
    if (inventoryContainer == null) {
      return;
    }

    @Nullable Item toReplaceItem = itemIds.getLeft();
    if (toReplaceItem != null) {
      Item extraItem = itemIds.getRight();
      if (extraItem != null) {
        var offhandWidget = findFirstEmptySlot(client, WidgetInfo.INVENTORY);
        if (offhandWidget != null) {
          widgetService.setFakeWidgetItem(widget, toReplaceItem);
          widgetService.setFakeWidgetItem(offhandWidget, extraItem);
        } else {
          //There was no slot to put the offhand item in, so the items will not be equipped
          log.debug("Will not equip two-handed item, as there is no slot to put the offhand item in");
          return;
        }
      } else {
        widgetService.setFakeWidgetItem(widget, toReplaceItem);
      }
    } else {
      widgetService.setAsHideOpacity(widget);
    }
    getState().setSlot(widget.getIndex(), widget.getItemId(), widget.getItemQuantity());
  }

  /**
   * @param widget the widget to equip
   * @return The item that was equipped (left) and potentially the off-hand item that was equipped (right) if it will be unequipped
   */
  @VisibleForTesting
  @Nonnull
  public Pair<Item, Item> getEquipmentToReplace(Widget widget) {
    final ItemStats itemStats = itemManager.getItemStats(widget.getItemId(), false);
    if (itemStats == null || !itemStats.isEquipable()) {
      return Pair.of(null, null);
    }
    Item toReplace = null;
    Item extra = null;

    final ItemEquipmentStats clickedEquipment = itemStats.getEquipment();

    ItemContainer equipmentContainer = client.getItemContainer(InventoryID.EQUIPMENT);
    if (clickedEquipment != null && equipmentContainer != null) {
      final int slotOfClickedItem = clickedEquipment.getSlot();
      toReplace = equipmentContainer.getItem(slotOfClickedItem);

      if (isWeaponSlot(slotOfClickedItem)) {
        if (clickedEquipment.isTwoHanded()) {
          extra = equipmentContainer.getItem(EquipmentInventorySlot.SHIELD.getSlotIdx());
        }
      } else if (isShieldSlot(slotOfClickedItem)) {
        var weaponItem = equipmentContainer.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
        if (weaponItem != null) {
          ItemStats weaponStat = itemManager.getItemStats(weaponItem.getId(), false);
          if (weaponStat != null && weaponStat.isEquipable()) {
            ItemEquipmentStats weaponStatEquipment = weaponStat.getEquipment();
            if (weaponStatEquipment != null && weaponStatEquipment.isTwoHanded()) {
              //If we click a shield while have a two-handed weapon equipped, the weapon get unequipped
              extra = weaponItem;
            }
          }
        }
      }
    }
    if (extra != null && toReplace == null) {
      //
      return Pair.of(extra, null);
    }
    return Pair.of(toReplace, extra);
  }

  private static boolean isShieldSlot(int index) {
    return index == EquipmentInventorySlot.SHIELD.getSlotIdx();
  }

  private static boolean isWeaponSlot(int index) {
    return index == EquipmentInventorySlot.WEAPON.getSlotIdx();
  }

  @Nonnull
  @Override
  public String getConfigKey() {
    return EQUIP_CONFIG_KEY;
  }
}
