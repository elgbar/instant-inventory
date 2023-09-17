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

package no.elg.ii.inventory.slot;

public interface InventorySlot {

  /**
   * Indicate that the item is not a real item, but rather a placeholder
   */
  int INVALID_ITEM_ID = -1;
  int RESET_ITEM_ID = -2;

  /**
   * Indicate the item has not been modified
   */
  int NO_CHANGED_TICK = -1;

  InventorySlot UNMODIFIED_SLOT = new InventorySlotState(NO_CHANGED_TICK, INVALID_ITEM_ID, 0);
  InventorySlot RESET_SLOT = new InventorySlotState(NO_CHANGED_TICK, RESET_ITEM_ID, 0);

  /**
   * @return When this slot was modified, or {@link InventorySlot#NO_CHANGED_TICK} if it has not been (or cannot be) modified
   */
  int getChangedTick();

  /**
   * @return The item id of this slot, or {@link InventorySlot#INVALID_ITEM_ID} if this slot is not a real item
   */
  int getItemId();

  int getQuantity();


  /**
   * @return Whether this slot is valid, i.e. has an item id
   */
  default boolean hasValidItemId() {
    return getItemId() >= 0;
  }

  /**
   * @return Whether this slot has been modified
   */
  default boolean hasChangedTick() {
    return getChangedTick() >= 0;
  }

}
