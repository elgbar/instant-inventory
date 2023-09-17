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

import static no.elg.ii.util.InventoryUtil.INVENTORY_SIZE;
import static no.elg.ii.util.InventoryUtil.getOpenWidgetItemContainer;
import static no.elg.ii.util.WidgetUtil.updateVisibleWidget;

import com.google.common.annotations.VisibleForTesting;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import no.elg.ii.InstantInventoryConfig;
import no.elg.ii.InstantInventoryPlugin;
import no.elg.ii.feature.Feature;
import no.elg.ii.inventory.slot.IndexedInventorySlot;
import no.elg.ii.inventory.slot.InventorySlot;
import no.elg.ii.inventory.slot.InventorySlotState;

/**
 * Hold the state of the players inventory. The state is checked every server tick in
 * {@link InstantInventoryPlugin#onGameTick(GameTick)}
 * <p>
 * A {@link Feature} uses this class to handle how to render the changes on the client. Typically, a
 * feature will modify the rendering of a given item in the inventory of the player when the
 * {@link #getSlot(int)} is different to {@link InventorySlot#INVALID_ITEM_ID}.
 */
@EqualsAndHashCode
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class InventoryState {

  /**
   * Maximum number of ticks an item should be displayed as something else
   */
  public static final int DEFAULT_MAX_UNMODIFIED_TICKS = 1;

  /**
   * The tick the item was modified
   */
  private final InventorySlot[] slots = new InventorySlot[INVENTORY_SIZE];

  @Inject
  @VisibleForTesting
  InstantInventoryConfig config;

  @Inject
  @VisibleForTesting
  Client client;

  {
    Arrays.fill(slots, InventorySlot.UNMODIFIED_SLOT);
  }

  /**
   * Update the {@code itemId} at {@code index} will also update which tick the item was modified
   */
  public void setSlot(Widget widget) {
    int index = widget.getIndex();
    setSlot(index, new InventorySlotState(client.getTickCount(), widget.getItemId(), index));
  }

  /**
   * Update the {@code itemId} at {@code index} will also update which tick the item was modified
   *
   * @param index  The index of the item
   * @param itemId The new itemId, intended to be the current item in the players inventory
   */
  public void setSlot(int index, int itemId, int quantity) {
    setSlot(index, new InventorySlotState(client.getTickCount(), itemId, quantity));
  }

  public void setSlot(int index, @Nonnull InventorySlot slot) {
    if (isValidIndex(index)) {
      log.trace("Setting index {} to {}", index, slot);
      slots[index] = slot;
    } else {
      log.debug("Tried to set invalid index {} to {}", index, slot);
    }
  }

  @Nullable
  public InventorySlot getSlot(int index) {
    if (isInvalidIndex(index)) {
      log.debug("Tried to get invalid index {}", index);
      return null;
    }
    return slots[index];
  }

  public static boolean isValidIndex(int index) {
    return index >= 0 && index < INVENTORY_SIZE;
  }

  public static boolean isInvalidIndex(int index) {
    return index < 0 || index >= INVENTORY_SIZE;
  }

  /**
   * @return The slots and its index we're currently modifying
   */
  public Stream<IndexedInventorySlot> getActiveSlots() {
    return IntStream.range(0, INVENTORY_SIZE).mapToObj(i -> new IndexedInventorySlot(i, getSlot(i))).filter((iis) -> iis.getSlot().hasValidItemId());
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
    if (isValidIndex(index)) {
      log.trace("Resetting index {}", index);
      slots[index] = InventorySlot.RESET_SLOT;
      resetItemInSlot(index);
    } else {
      log.debug("Tried to reset invalid index {}", index);
    }
  }

  private void resetItemInSlot(int index) {
    ItemContainer inventoryContainer = client.getItemContainer(InventoryID.INVENTORY);
    if (inventoryContainer == null) {
      return;
    }
    Widget inventoryWidget = getOpenWidgetItemContainer(client);
    if (inventoryWidget == null) {
      return;
    }
    Item item = inventoryContainer.getItem(index);
    Widget[] children = inventoryWidget.getDynamicChildren();
    if (item != null && children.length == INVENTORY_SIZE && children[index] != null) {
      updateVisibleWidget(children[index], item);
    }
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
  public void validateState(int index, int actualItemId, int actualQuantity) {
    InventorySlot slot = getSlot(index);
    if (slot == null || slot == InventorySlot.UNMODIFIED_SLOT || slot == InventorySlot.RESET_SLOT) {
      // This item is not modified (or at least not by us) so we do not need to do anything
      return;
    }

    int itemId = slot.getItemId();
    int quantity = slot.getQuantity();
    int modifiedTick = slot.getChangedTick();
    // Item at index changed so we must reset the slot
    if (slot.hasValidItemId() && (itemId != actualItemId || quantity != actualQuantity)) {
      log.debug("Item at index {} changed from item id {} to {} or from quantity {} to {} , resetting the item", index, itemId, actualItemId, quantity, actualItemId);
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
