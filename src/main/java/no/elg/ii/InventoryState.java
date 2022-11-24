/*
 * Copyright (c) 2022 Elg
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package no.elg.ii;

import java.util.Arrays;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.GameTick;

/**
 * Hold the state of the players inventory. The state is checked every server tick in
 * {@link InstantInventoryPlugin#onGameTick(GameTick)}
 * <p>
 * A {@link Feature} uses this class to handle how to render the changes on the client. Typically, a
 * feature will modify the rendering of a given item in the inventory of the player when the
 * {@link #getItemId(int)} is different to {@link #INVALID_ITEM_ID}.
 */
@EqualsAndHashCode
@Slf4j
public class InventoryState {

  /**
   * Indicate the item has not been modified
   */
  public static final int NOT_MODIFIED = -1;
  /**
   * Maximum number of ticks an item should be displayed as something else
   */
  public static final int MAX_UNMODIFIED_TICKS = 5;

  public static final int INVENTORY_SIZE = 28;
  public static final int INVALID_ITEM_ID = -1;

  private final int[] items = new int[INVENTORY_SIZE];
  private final int[] modified = new int[INVENTORY_SIZE];


  public InventoryState() {
    resetAll();
  }

  /**
   * Update the {@code itemId} at {@code index} will also update which tick the item was modified
   *
   * @param index  The index of the item
   * @param itemId The new itemId, intended to be the current item in the players inventory
   */
  public void setItemId(int index, int itemId) {
    modified[index] = InstantInventoryPlugin.tickCounter.get();
    items[index] = itemId;
  }


  /**
   * @param index The index of the item
   * @return Which tick the item was last modified on
   */
  public int getModifiedTick(int index) {
    return modified[index];
  }

  /**
   * @param index The index of the item
   * @return The last seen real item id at the given index
   */
  public int getItemId(int index) {
    return items[index];
  }

  /**
   * @param index The index of the item to test
   * @return Whether the {@code index} and the item at the given index is invalid
   */
  public boolean isInvalid(int index) {
    return index < 0 || index >= items.length || items[index] == INVALID_ITEM_ID;
  }

  /**
   * @param index The index of the item to test
   * @return Whether the {@code index} and the item at the given index is a valid itemID
   */
  public boolean isValid(int index) {
    return !isInvalid(index);
  }

  /**
   * Reset the state to its inital state
   */
  public void resetAll() {
    Arrays.fill(modified, NOT_MODIFIED);
    Arrays.fill(items, INVALID_ITEM_ID);
  }

  /**
   * Reset a given index to the initial state
   *
   * @param index The index of the item
   */
  public void resetState(int index) {
    modified[index] = NOT_MODIFIED;
    items[index] = INVALID_ITEM_ID;
  }

  /**
   * Validate and modify the state of an item for a given index.
   * <p>
   * The state will be reset when the {@code currentItemId} indicates a different item exists in at
   * the index's inventory slot. Additionally, if too much time have passed without a item change,
   * the state will also be reset to not operate on stale data
   *
   * @param index         The index of the item
   * @param currentItemId The current itemId at the index
   */
  public void validateState(int index, int currentItemId) {
    int itemId = getItemId(index);
    // Item at index changed so we must reset the state
    if (itemId != INVALID_ITEM_ID && itemId != currentItemId) {
      log.debug("Item at index {} changed from item id {} to {}, resetting the item", index, itemId,
          currentItemId);
      resetState(index);
      return;
    }

    // The item at the given index have not changes in some time, we reset to
    int modifiedTick = getModifiedTick(index);
    int ticksSinceModified = InstantInventoryPlugin.tickCounter.get() - modifiedTick;
    log.debug("Item at index {} has not changed in {} tick, resetting the item", index,
        ticksSinceModified);
    if (modifiedTick != NOT_MODIFIED && ticksSinceModified >= MAX_UNMODIFIED_TICKS) {
      log.warn("Item at index {} has not changed in {} tick, resetting the item", index,
          ticksSinceModified);
      resetState(index);
    }
  }
}
