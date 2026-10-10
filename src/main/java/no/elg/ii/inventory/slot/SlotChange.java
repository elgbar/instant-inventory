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

package no.elg.ii.inventory.slot;

/**
 * What the server is predicted to do with an inventory slot. An {@link InventorySlot} says how to draw the slot until
 * the server catches up, this says what the slot holds on the server once it has.
 * <p>
 * The server handles clicks in the order they were made, which is what makes predicting several clicks ahead possible.
 */
public enum SlotChange {
  /**
   * The item leaves the slot, e.g., when it is dropped or deposited.
   * <p>
   * The slot is empty on the server once it has handled the action, so an item withdrawn after it can land in this slot
   */
  EMPTY,
  /**
   * The slot keeps an item, but a different one or another quantity, e.g., a cleaned herb
   */
  REPLACE,
  /**
   * An item appears in a slot that is empty on the server, e.g., when it is withdrawn.
   * <p>
   * The client draws such a slot as empty until the server has added the item, so an empty widget does not mean the
   * prediction is over
   */
  FILL
}
