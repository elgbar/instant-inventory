/*
 * Copyright (c) 2022-2023 Elg
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
package no.elg.ii.inventory;

import com.google.common.annotations.VisibleForTesting;
import java.util.Arrays;
import javax.inject.Inject;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import no.elg.ii.InstantInventoryConfig;
import no.elg.ii.InstantInventoryPlugin;
import no.elg.ii.feature.Feature;
import no.elg.ii.inventory.slot.InventorySlot;
import no.elg.ii.inventory.slot.InventorySlotState;

/**
 * Hold the state of the players inventory. The state is checked every server tick in
 * {@link InstantInventoryPlugin#onGameTick(GameTick)}
 * <p>
 * A {@link Feature} uses this class to handle how to render the changes on the client. Typically, a
 * feature will modify the rendering of a given item in the inventory of the player when the
 * {@link #getItemId(int)} is different to {@link InventorySlot#INVALID_ITEM_ID}.
 */
@EqualsAndHashCode
@Slf4j
public class InventoryState {

  /**
   * Maximum number of ticks an item should be displayed as something else
   */
  public static final int DEFAULT_MAX_UNMODIFIED_TICKS = 1;

  /**
   * Number of items in an inventory
   */
  public static final int INVENTORY_SIZE = 28;

  /**
   * The tick the item was modified
   */
  private final InventorySlot[] slots = new InventorySlot[INVENTORY_SIZE];

  @VisibleForTesting
  InstantInventoryConfig config;

  @VisibleForTesting
  Client client;

  @Inject
  public InventoryState(InstantInventoryConfig config, Client client) {
    this.config = config;
    this.client = client;
    resetAll();
  }

  /**
   * Update the {@code itemId} at {@code index} will also update which tick the item was modified
   *
   * @param index  The index of the item
   * @param itemId The new itemId, intended to be the current item in the players inventory
   */
  public void setItemId(int index, int itemId) {
//    log.debug("Setting index {} to {}", index, itemId);
    slots[index] = new InventorySlotState(client.getTickCount(), itemId);
  }

  public void setSlot(int index, InventorySlot slot) {
//    log.debug("Setting index {} to {}", index, slot);
    slots[index] = slot;
  }

  /**
   * @param index The index of the item
   * @return Which tick the item was last modified on
   * @deprecated Use {@link #getSlot(int)} instead
   */
  @Deprecated(since = "1.1.2", forRemoval = true)
  public int getModifiedTick(int index) {
    return slots[index].getChangedTick();
  }

  /**
   * @param index The index of the item
   * @return The last seen real item id at the given index
   * @deprecated Use {@link #getSlot(int)} instead
   */
  @Deprecated(since = "1.1.2", forRemoval = true)
  public int getItemId(int index) {
    return slots[index].getItemId();
  }

  public InventorySlot getSlot(int index) {
    return slots[index];
  }

  /**
   * @param index The index of the item to test
   * @return Whether the {@code index} and the item at the given index is invalid
   */
  @Deprecated(since = "1.1.2", forRemoval = true)
  public boolean isInvalid(int index) {
    return index < 0 || index >= slots.length || !getSlot(index).hasValidItemId();
  }

  /**
   * @param index The index of the item to test
   * @return Whether the {@code index} and the item at the given index is a valid itemID
   */
  @Deprecated(since = "1.1.2", forRemoval = true)
  public boolean isValid(int index) {
    return !isInvalid(index);
  }

  /**
   * Reset the state to its initial state
   */
  public void resetAll() {
    Arrays.fill(slots, InventorySlot.RESET_SLOT);
  }

  /**
   * Reset a given index to the initial state
   *
   * @param index The index of the item
   */
  public void resetState(int index) {
    slots[index] = InventorySlot.RESET_SLOT;
  }

  /**
   * Validate and modify the state of an item for a given index.
   * <p>
   * The state will be reset when the {@code actualItemId} indicates a different item exists in at
   * the index's inventory slot. Additionally, if too much time have passed without an item change,
   * the state will also be reset to not operate on stale data
   *
   * @param index        The index of the item
   * @param actualItemId The actual item which is in the inventory
   */
  public void validateState(int index, int actualItemId) {
    InventorySlot slot = getSlot(index);
    if (slot == InventorySlot.UNMODIFIED_SLOT || slot == InventorySlot.RESET_SLOT) {
      // This item is not modified (or at least not by us) so we do not need to do anything
      return;
    }

    int itemId = slot.getItemId();
    int modifiedTick = slot.getChangedTick();
    // Item at index changed so we must reset the slot
    if (slot.hasValidItemId() && itemId != actualItemId) {
      log.debug("Item at index {} changed from item id {} to {}, resetting the item", index, itemId, actualItemId);
      resetState(index);
      return;
    }

    // The item at the given index have not changes in some time, we reset to
    int ticksSinceModified = client.getTickCount() - modifiedTick;
    if (slot.hasChangedTick() && ticksSinceModified >= config.maxUnmodifiedTicks()) {
      log.debug("Item at index {} has not changed in {} tick, resetting the item", index, ticksSinceModified);
      resetState(index);
    }
  }
}
