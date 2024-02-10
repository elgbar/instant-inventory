/*
 * Copyright (c) 2023-2024 Elg
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

import lombok.Data;

@Data
public class InventorySlot {

  /**
   * Indicate that the item is not a real item, but rather a placeholder
   */
  public static final int INVALID_ITEM_ID = -1;
  public static final int RESET_ITEM_ID = -2;

  /**
   * Indicate the item has not been modified
   */
  public static final int NO_CHANGED_TICK = -1;

  public static final int TICK_LENGTH_MS = 600;

  public static final InventorySlot UNMODIFIED_SLOT = new InventorySlot(NO_CHANGED_TICK, INVALID_ITEM_ID, 0);
  public static final InventorySlot RESET_SLOT = new InventorySlot(NO_CHANGED_TICK, RESET_ITEM_ID, 0);

  /**
   * When this slot was modified, or {@link InventorySlot#NO_CHANGED_TICK} if it has not been (or cannot be) modified
   */
  private final int changedTick;
  /**
   * When this slot was modified in milliseconds. This is to remove flickering
   */
  private final long changedMs = System.currentTimeMillis();
  /**
   * The item id of this slot, or {@link InventorySlot#INVALID_ITEM_ID} if this slot is not a real item
   */
  private final int itemId;
  /**
   * How many of the item in this slot
   */
  private final int quantity;

  /**
   * @return Whether this slot is valid, i.e. has an item id
   */
  public boolean hasValidItemId() {
    return getItemId() >= 0;
  }

  /**
   * @return Whether this slot has been modified, if so when it was will be reflected in {@link #getChangedTick()}
   */
  public boolean hasChangedTick() {
    return getChangedTick() >= 0;
  }

  /**
   * A slot shou
   *
   * @return If this inventory slot has still "Invulnerability Frames" left
   */
  public boolean isTooEarlyToReset() {
    return System.currentTimeMillis() - getChangedMs() < TICK_LENGTH_MS;
  }

}
