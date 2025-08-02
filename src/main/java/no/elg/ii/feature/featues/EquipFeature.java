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

package no.elg.ii.feature.featues;

import com.google.common.annotations.VisibleForTesting;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemEquipmentStats;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStats;
import no.elg.ii.feature.Feature;
import no.elg.ii.feature.state.InventoryState;
import no.elg.ii.model.IndexedWidget;
import no.elg.ii.service.InventoryService;
import no.elg.ii.service.WidgetService;
import no.elg.ii.util.WidgetUtils;
import org.apache.commons.lang3.tuple.Pair;


@Slf4j
@Singleton
@NoArgsConstructor
public class EquipFeature implements Feature {

  public static final Set<String> EQUIP_OPTIONS = Set.of("Wear", "Wield", "Equip");
  public static final String EQUIP_CONFIG_KEY = "instantEquip";

  @Inject
  @VisibleForTesting
  ItemManager itemManager;

  @Inject
  @VisibleForTesting
  Client client;
  @Inject
  @VisibleForTesting
  ClientThread clientThread;

  @Inject
  @Getter
  private InventoryState state;
  @Inject
  private WidgetService widgetService;
  @Inject
  private InventoryService inventoryService;

  /**
   * The last tick each slot was equipped
   */
  private final Map</*slotIdx*/ Integer, /*last tick count changed*/ Integer> lastEquipped = new HashMap<>(EquipmentInventorySlot.values().length);

  @Subscribe
  public void onMenuOptionClicked(final MenuOptionClicked event) {
    Widget widget = event.getWidget();
    if (widget != null) {
      String menuOption = event.getMenuOption();
      if (EQUIP_OPTIONS.contains(menuOption)) {
        log.debug("'{}' item {}", menuOption, WidgetUtils.debugInfo(widget));
        clientThread.invokeAtTickEnd(() -> equip(widget));
      }
    }
  }

  private static final String TOO_LOW_LEVEL_MESSAGE = "You are not a high enough level to use this item.";

  @Subscribe
  public void onChatMessage(ChatMessage event) {
    if (event.getType() == ChatMessageType.GAMEMESSAGE && TOO_LOW_LEVEL_MESSAGE.equals(event.getMessage())) {
      log.debug("Failed to equip item");
      state.getActiveSlots().filter(is -> is.getSlot().getChangedTick() == client.getTickCount()).forEach(is -> state.resetState(is.getIndex()));
    }
  }

  protected void equip(@Nonnull Widget widget) {
    ItemContainer inventoryContainer = inventoryService.getCurrentInventoryContainer();
    if (inventoryContainer == null) {
      log.debug("Failed to find the inventory container");
      return;
    }
    @Nullable Pair<Item, Item> itemIds = getEquipmentToReplace(widget);
    if (itemIds == null) {
      return;
    }

    @Nullable Item toReplaceItem = itemIds.getLeft();
    int opacity;
    if (toReplaceItem != null) {
      log.trace("An item was equipped in the slot (to replace: {}), will replace it with {}", WidgetUtils.debugInfo(toReplaceItem), WidgetUtils.debugInfo(widget));
      Item extraItem = itemIds.getRight();
      if (extraItem != null) {
        log.trace("There is also something in the off-slot ({}), will replace that too", WidgetUtils.debugInfo(extraItem));
        Optional<Widget> offhandWidget = inventoryService.getAllOpenInventoryWidgets().filter(WidgetUtils::isEmpty).findFirst().map(IndexedWidget::getWidget);
        if (offhandWidget.isPresent()) {
          widgetService.setFakeWidgetItem(widget, toReplaceItem);
          widgetService.setFakeWidgetItem(offhandWidget.get(), extraItem);
        } else {
          //There was no slot to put the offhand item in, so the items will not be equipped
          log.debug("Will not equip two-handed item, as there is no slot to put the offhand item in");
          return;
        }
      } else {
        log.trace("No off-hand item to replace, will only change the clicked slot");
        widgetService.setFakeWidgetItem(widget, toReplaceItem);
      }
      opacity = widgetService.getChangeOpacity();
    } else {
      log.trace("No other item to replace, will show the slot as empty");
      widgetService.setEmptyItem(widget);
      opacity = widgetService.getHideOpacity();
    }
    state.setSlot(widget, opacity);
  }

  /**
   * @param widget the widget to equip
   * @return The item that was equipped (left) and potentially the off-hand item that was equipped (right) if it will be unequipped
   */
  @VisibleForTesting
  @Nullable
  public Pair<Item, Item> getEquipmentToReplace(Widget widget) {
    final ItemStats itemStats = itemManager.getItemStats(widget.getItemId());
    if (itemStats == null || !itemStats.isEquipable()) {
      log.debug("Item has not status or is not equipable, will not equip it: {}", itemStats);
      return null;
    }
    Item toReplace = null;
    Item extra = null;

    final ItemEquipmentStats clickedEquipment = itemStats.getEquipment();

    ItemContainer equipmentContainer = client.getItemContainer(InventoryID.WORN);
    if (clickedEquipment != null && equipmentContainer != null) {
      if (lastEquipped.getOrDefault(clickedEquipment.getSlot(), 0) == client.getTickCount()) {
        log.debug("We have already equipped an item in the same slot this tick, will not replace it");
        return null;
      }
      final int slotOfClickedItem = clickedEquipment.getSlot();
      toReplace = equipmentContainer.getItem(slotOfClickedItem);

      if (isWeaponSlot(slotOfClickedItem)) {
        if (clickedEquipment.isTwoHanded()) {
          extra = equipmentContainer.getItem(EquipmentInventorySlot.SHIELD.getSlotIdx());
        }
      } else if (isShieldSlot(slotOfClickedItem)) {
        var weaponItem = equipmentContainer.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
        if (weaponItem != null) {
          ItemStats weaponStat = itemManager.getItemStats(weaponItem.getId());
          if (weaponStat != null && weaponStat.isEquipable()) {
            ItemEquipmentStats weaponStatEquipment = weaponStat.getEquipment();
            if (weaponStatEquipment != null && weaponStatEquipment.isTwoHanded()) {
              //If we click a shield while have a two-handed weapon equipped, the weapon get unequipped
              extra = weaponItem;
            }
          }
        }
      }
      lastEquipped.put(clickedEquipment.getSlot(), client.getTickCount());
    }
    if (extra != null && toReplace == null) {
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

  @Override
  public @NonNull String getConfigKey() {
    return EQUIP_CONFIG_KEY;
  }
}
