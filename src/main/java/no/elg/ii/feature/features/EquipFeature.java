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

package no.elg.ii.feature.features;

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
import lombok.Value;
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
import no.elg.ii.InstantInventoryConfig;
import no.elg.ii.feature.StatefulFeature;
import no.elg.ii.feature.state.InventoryState;
import no.elg.ii.inventory.slot.InventorySlot;
import no.elg.ii.inventory.slot.SlotChange;
import no.elg.ii.model.EquipSlot;
import no.elg.ii.model.IndexedWidget;
import no.elg.ii.service.InventoryService;
import no.elg.ii.service.ItemEquipmentService;
import no.elg.ii.service.WidgetService;
import no.elg.ii.util.WidgetUtils;
import org.apache.commons.lang3.tuple.Pair;


@Slf4j
@Singleton
@NoArgsConstructor
public class EquipFeature implements StatefulFeature {

  public static final Set<String> EQUIP_OPTIONS = Set.of("Wear", "Wield", "Equip");
  public static final String EQUIP_CONFIG_KEY = "instantEquip";
  private static final String TOO_LOW_LEVEL_MESSAGE = "You are not a high enough level to use this item.";

  @Inject
  @VisibleForTesting
  Client client;

  @Inject
  @VisibleForTesting
  ClientThread clientThread;

  @Inject
  @Getter
  @VisibleForTesting
  InventoryState state;

  @Inject
  @VisibleForTesting
  WidgetService widgetService;

  @Inject
  @VisibleForTesting
  InventoryService inventoryService;

  @Inject
  private InstantInventoryConfig config;

  @Inject
  private ItemEquipmentService itemEquipmentService;

  /**
   * The last tick each slot was equipped
   */
  private final Map</*slotIdx*/ Integer, /*last tick count changed*/ Integer> lastEquipped = new HashMap<>(EquipmentInventorySlot.values().length);

  /**
   * What equipping will put in each worn slot, as the worn container does not show it until the server has handled it
   */
  private final Map</*slotIdx*/ Integer, WornPrediction> predictedWorn = new HashMap<>(EquipmentInventorySlot.values().length);

  @Subscribe
  public void onMenuOptionClicked(final MenuOptionClicked event) {
    Widget widget = event.getWidget();
    if (widget != null && !event.isConsumed()) {
      String menuOption = event.getMenuOption();
      if (EQUIP_OPTIONS.contains(menuOption)) {
        log.debug("'{}' item {}", menuOption, WidgetUtils.debugInfo(widget));
        clientThread.invokeAtTickEnd(() -> equip(widget));
      }
    }
  }

  @Subscribe
  public void onChatMessage(ChatMessage event) {
    if (event.getType() == ChatMessageType.GAMEMESSAGE && TOO_LOW_LEVEL_MESSAGE.equals(event.getMessage())) {
      log.debug("Failed to equip item");
      state.getActiveSlots().filter(is -> is.getSlot().getChangedTick() == client.getTickCount()).forEach(is -> state.resetState(is.getIndex()));
    }
  }

  protected void equip(@Nonnull Widget widget) {
    if (state.isEmptying(widget)) {
      log.debug("The item is already leaving the slot, so the server will not equip it again");
      return;
    }
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
    if (toReplaceItem != null) {
      log.trace("An item was equipped in the slot (to replace: {}), will replace it with {}", WidgetUtils.debugInfo(toReplaceItem), WidgetUtils.debugInfo(widget));
      Item extraItem = itemIds.getRight();
      if (extraItem != null) {
        log.trace("There is also something in the off-slot ({}), will replace that too", WidgetUtils.debugInfo(extraItem));
        Optional<Widget> maybeOffhandWidget = inventoryService.getAllOpenInventoryWidgets().filter(WidgetUtils::isEmpty).findFirst().map(IndexedWidget::getWidget);
        if (maybeOffhandWidget.isPresent()) {
          Widget offhandWidget = maybeOffhandWidget.get();
          widgetService.setFakeWidgetItem(widget, toReplaceItem, SlotChange.REPLACE);
          widgetService.setFakeWidgetItem(offhandWidget, extraItem, SlotChange.FILL);
          state.setSlot(widget, SlotChange.REPLACE);
          state.setSlot(offhandWidget.getIndex(), extraItem.getId(), extraItem.getQuantity(), SlotChange.FILL);
        } else {
          //There was no slot to put the offhand item in, so the items will not be equipped
          log.debug("Will not equip two-handed item, as there is no slot to put the offhand item in");
        }
      } else {
        log.trace("No off-hand item to replace, will only change the clicked slot");
        widgetService.setFakeWidgetItem(widget, toReplaceItem, SlotChange.REPLACE);
        state.setSlot(widget, SlotChange.REPLACE);
      }
    } else {
      log.trace("No item to replace, expecting the item to be emptied");
      state.setSlot(widget, SlotChange.EMPTY);
    }
  }

  /**
   * @param widget the widget to equip
   * @return The item that was equipped (left) and potentially the off-hand item that was equipped (right) if it will be unequipped
   */
  @VisibleForTesting
  @Nullable
  public Pair<Item, Item> getEquipmentToReplace(Widget widget) {
    final EquipSlot clickedEquipment = itemEquipmentService.getEquipSlot(widget.getItemId());
    if (clickedEquipment == null) {
      log.debug("Not known where the item is worn, will not equip it: {}", WidgetUtils.debugInfo(widget));
      return null;
    }
    Item toReplace = null;
    Item extra = null;

    ItemContainer equipmentContainer = client.getItemContainer(InventoryID.WORN);
    if (equipmentContainer != null) {
      if (lastEquipped.getOrDefault(clickedEquipment.getSlot(), 0) == client.getTickCount()) {
        log.debug("We have already equipped an item in the same slot this tick, will not replace it");
        return null;
      }
      final int slotOfClickedItem = clickedEquipment.getSlot();
      toReplace = getWornItem(equipmentContainer, slotOfClickedItem);

      if (clickedEquipment.isWeaponSlot()) {
        Item shieldItem = getWornItem(equipmentContainer, EquipmentInventorySlot.SHIELD.getSlotIdx());
        Boolean twoHanded = clickedEquipment.getTwoHanded();
        if (twoHanded == null && shieldItem != null) {
          log.debug("Not known whether the weapon is two-handed and takes off the shield, will not equip it: {}", WidgetUtils.debugInfo(shieldItem));
          return null;
        } else if (Boolean.TRUE.equals(twoHanded)) {
          extra = shieldItem;
          predictWorn(EquipmentInventorySlot.SHIELD.getSlotIdx(), null, widget);
        }
      } else if (clickedEquipment.isShieldSlot()) {
        var weaponItem = getWornItem(equipmentContainer, EquipmentInventorySlot.WEAPON.getSlotIdx());
        if (weaponItem != null) {
          EquipSlot weaponEquipment = itemEquipmentService.getEquipSlot(weaponItem.getId());
          if (weaponEquipment == null) {
            log.debug("The wielded weapon has no item stats and has not been seen worn, will not equip the shield: {}", WidgetUtils.debugInfo(weaponItem));
            return null;
          }
          Boolean weaponTwoHanded = weaponEquipment.getTwoHanded();
          if (weaponTwoHanded == null) {
            log.debug("Not known whether the wielded weapon is two-handed and is taken off, will not equip the shield: {}", WidgetUtils.debugInfo(weaponItem));
            return null;
          } else if (weaponTwoHanded) {
            //If we click a shield while have a two-handed weapon equipped, the weapon get unequipped
            extra = weaponItem;
            predictWorn(EquipmentInventorySlot.WEAPON.getSlotIdx(), null, widget);
          }
        }
      }
      predictWorn(slotOfClickedItem, new Item(widget.getItemId(), widget.getItemQuantity()), widget);
      lastEquipped.put(slotOfClickedItem, client.getTickCount());
    }
    if (extra != null && toReplace == null) {
      return Pair.of(extra, null);
    }
    return Pair.of(toReplace, extra);
  }

  /**
   * @param item   What will be worn in the slot, or null if it will be empty
   * @param widget The inventory item being equipped
   */
  private void predictWorn(int slotIdx, @Nullable Item item, @Nonnull Widget widget) {
    predictedWorn.put(slotIdx, new WornPrediction(item, widget.getIndex(), client.getTickCount()));
  }

  /**
   * A prediction is used for as long as the inventory slot the item was equipped from shows the equip, so it lasts until
   * the server has handled it, see {@link InventoryState#validateState(int, Item)}
   *
   * @return What will be worn in the slot once the server has handled what was equipped
   */
  @Nullable
  private Item getWornItem(@Nonnull ItemContainer equipmentContainer, int slotIdx) {
    WornPrediction prediction = predictedWorn.get(slotIdx);
    if (prediction != null) {
      InventorySlot equippedFrom = state.getSlot(prediction.getInventoryIndex());
      if (equippedFrom != null && equippedFrom.getChangedTick() == prediction.getEquippedTick()) {
        return prediction.getItem();
      }
    }
    return equipmentContainer.getItem(slotIdx);
  }

  @NonNull
  @Override
  public String getConfigKey() {
    return EQUIP_CONFIG_KEY;
  }

  @Override
  public boolean isEnabledInConfig() {
    return config.instantEquip();
  }


  @Value
  private static class WornPrediction {
    /**
     * What will be worn, or null if the slot will be empty
     */
    @Nullable
    Item item;
    /**
     * The inventory slot the item was equipped from
     */
    int inventoryIndex;
    /**
     * The tick the item was equipped
     */
    int equippedTick;
  }
}
