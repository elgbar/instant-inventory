/*
 * Copyright (c) 2026 Elg
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

import com.google.common.annotations.VisibleForTesting;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemEquipmentStats;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStats;
import no.elg.ii.model.EquipSlot;

@Slf4j
@Singleton
@NoArgsConstructor
public class ItemEquipmentService {

  @Inject
  @VisibleForTesting
  ItemManager itemManager;

  @Inject
  @VisibleForTesting
  Client client;

  /**
   * Where items have been seen worn, for items RuneLite has no item stats for yet, such as items from a recent game update
   */
  private final Map</*itemId*/ Integer, /*slotIdx*/ Integer> seenWornSlot = new HashMap<>(0);

  /**
   * Whether weapons have been seen to be two-handed, for items RuneLite has no item stats for yet
   */
  private final Map</*itemId*/ Integer, /*is2h*/ Boolean> seenTwoHanded = new HashMap<>(0);

  /**
   * Whether a shield was worn the last time the server said what is worn, to see if wielding a weapon took it off
   */
  private boolean lastWoreShield;


  /**
   * RuneLite's item stats only cover items some time after they are released, so for newer items it is what they have
   * been seen worn as
   *
   * @return Where the item is worn, or null if it is not equipable or that is not known
   */
  @Nullable
  public EquipSlot getEquipSlot(int itemId) {
    ItemStats itemStats = itemManager.getItemStats(itemId);
    if (itemStats != null) {
      if (!itemStats.isEquipable()) {
        return null;
      }
      ItemEquipmentStats equipment = itemStats.getEquipment();
      if (equipment != null) {
        return new EquipSlot(equipment.getSlot(), equipment.isTwoHanded());
      }
    }
    Integer slotIdx = seenWornSlot.get(itemId);
    return slotIdx == null ? null : new EquipSlot(slotIdx, seenTwoHanded.get(itemId));
  }


  @Subscribe
  public void onItemContainerChanged(final ItemContainerChanged event) {
    if (event.getContainerId() == InventoryID.WORN) {
      learnEquipSlots(event.getItemContainer());
    }
  }

  /**
   * Learn where items are worn from what the server says is worn, so items RuneLite has no item stats for yet can be
   * equipped
   */
  private void learnEquipSlots(@Nonnull ItemContainer equipmentContainer) {
    assert client.isClientThread();
    for (EquipmentInventorySlot slot : EquipmentInventorySlot.values()) {
      Item item = equipmentContainer.getItem(slot.getSlotIdx());
      if (item != null && missingEquipmentStats(item.getId())) {
        //only record the items we don't know about
        seenWornSlot.put(item.getId(), slot.getSlotIdx());
      }
    }

    Item weaponItem = equipmentContainer.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
    boolean wearsShield = equipmentContainer.getItem(EquipmentInventorySlot.SHIELD.getSlotIdx()) != null;
    if (weaponItem != null && missingEquipmentStats(weaponItem.getId())) {
      if (wearsShield) {
        // A two-handed weapon cannot be worn together with a shield
        seenTwoHanded.put(weaponItem.getId(), false);
      } else if (lastWoreShield) {
        // Wielding the weapon took off the shield, unless the shield was removed the same tick. Either way, a weapon
        // already seen together with a shield is known to be one-handed
        seenTwoHanded.putIfAbsent(weaponItem.getId(), true);
      }
    }
    lastWoreShield = wearsShield;
  }

  /**
   * @return Whether RuneLite's item stats say where the item is worn
   */
  private boolean missingEquipmentStats(int itemId) {
    ItemStats itemStats = itemManager.getItemStats(itemId);
    return itemStats == null || itemStats.getEquipment() == null;
  }

}
