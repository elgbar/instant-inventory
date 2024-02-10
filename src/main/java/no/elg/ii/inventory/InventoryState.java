/*
 * Copyright (c) 2022-2024 Elg
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

import static no.elg.ii.inventory.slot.InventorySlot.INVALID_ITEM_ID;
import static no.elg.ii.util.InventoryUtil.INVENTORY_SIZE;

import com.google.common.annotations.VisibleForTesting;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
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
import no.elg.ii.service.WidgetService;
import no.elg.ii.util.IndexedWidget;

/**
 * Hold the state of the players inventory. The state is checked every server tick in
 * {@link InstantInventoryPlugin#onGameTick(GameTick)}
 * <p>
 * A {@link Feature} uses this class to handle how to render the changes on the client. Typically, a
 * feature will modify the rendering of a given item in the inventory of the player when the
 * {@link #getSlot(int)} is different to {@link InventorySlot#INVALID_ITEM_ID}.
 * <p>
 * The state should only be modified by the client thread
 */
@EqualsAndHashCode
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@Singleton
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

  @Inject
  private InventoryService inventoryService;

  @Inject
  private WidgetService widgetService;

  {
    Arrays.fill(slots, InventorySlot.UNMODIFIED_SLOT);
  }

  /**
   * Update the {@code itemId} at {@code index} will also update which tick the item was modified
   */
  public void setSlotAsHidden(Widget widget) {
    setSlot(widget, widgetService.getHideOpacity());
  }

  /**
   * Update the {@code itemId} at {@code index} will also update which tick the item was modified
   */
  public void setSlotAsChanged(Widget widget) {
    setSlot(widget, widgetService.getChangeOpacity());
  }

  /**
   * Update the {@code itemId} at {@code index} will also update which tick the item was modified
   */
  public void setSlot(Widget widget, int opacity) {
    int index = widget.getIndex();
    setSlot(index, new InventorySlot(client.getTickCount(), widget.getItemId(), widget.getItemQuantity(), opacity));
  }

  /**
   * Update the {@code itemId} at {@code index} will also update which tick the item was modified
   *
   * @param index  The index of the item
   * @param itemId The new itemId, intended to be the current item in the players inventory
   */
  public void setSlot(int index, int itemId, int quantity, int opacity) {
    setSlot(index, new InventorySlot(client.getTickCount(), itemId, quantity, opacity));
  }

  private void setSlot(int index, @Nonnull InventorySlot slot) {
    assert this.client.isClientThread();
    if (isValidIndex(index)) {
      log.trace("Setting index {} to {}", index, slot);
      slots[index] = slot;
    } else {
      log.debug("Tried to set invalid index {} to {}", index, slot);
    }
  }

  @Nullable
  public InventorySlot getSlot(int index) {
    assert this.client.isClientThread();
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
    assert this.client.isClientThread();
    //noinspection DataFlowIssue Safe as we are always within the size of the inventory
    return IntStream.range(0, slots.length).mapToObj(i -> new IndexedInventorySlot(i, getSlot(i))).filter((iis) -> iis.getSlot().hasValidItemId());
  }

  /**
   * Reset the state to its initial state
   */
  public void resetAll() {
    for (int i = 0; i < INVENTORY_SIZE; i++) {
      resetState(i);
    }
  }

  /**
   * Reset a given index to the initial state
   *
   * @param index The index of the item
   */
  public void resetState(int index) {
    resetState(index, null, false);
  }

  /**
   * Reset a given index to the initial state
   *
   * @param index The index of the item
   */
  private void resetState(int index, @Nullable Item item, boolean hasItem) {
    assert this.client.isClientThread();
    if (isValidIndex(index)) {
      log.trace("Resetting index {}", index);
      slots[index] = InventorySlot.RESET_SLOT;
      resetWidgetInSlot(index, item, hasItem);
    } else {
      log.debug("Tried to reset invalid index {}", index);
    }
  }

  /**
   * Update all inventory widgets to reflect the actual state of the inventory
   */
  private void resetWidgetInSlot(int index, @Nullable Item maybeItem, boolean hasItem) {
    assert this.client.isClientThread();
    Item item;
    if (hasItem) {
      item = maybeItem;
    } else {
      ItemContainer inventoryContainer = client.getItemContainer(InventoryID.INVENTORY);
      if (inventoryContainer == null) {
        return;
      }
      item = inventoryContainer.getItem(index);
    }

    Stream<IndexedWidget> indexedWidgetStream = inventoryService.getAllInventoryWidgets().filter(it -> it.getIndex() == index);
    if (item == null || item.getId() < 0) {
      //There is no item at this index, so we hide the widget
      //Make sure items that are not in the inventory are hidden
      indexedWidgetStream.forEach(it -> widgetService.setAsFullyOpaque(it.getWidget()));
    } else {
      //Update the item to the actual item
      indexedWidgetStream.forEach(it -> {
        widgetService.setAsFullyOpaque(it.getWidget());
        widgetService.updateVisibleWidget(it.getWidget(), item);
      });
    }
  }

  /**
   * Validate and modify the state of an item for a given index.
   * <p>
   * The state will be reset when the {@code actualItemId} indicates a different item exists in at
   * the index's inventory slot. Additionally, if too much time have passed without an item change,
   * the state will also be reset to not operate on stale data
   *
   * @param index The index of the item
   * @param item  The actual item which is in the inventory
   */
  public void validateState(int index, @Nullable Item item) {
    assert this.client.isClientThread();
    InventorySlot slot = getSlot(index);
    if (slot == null || slot == InventorySlot.UNMODIFIED_SLOT || slot == InventorySlot.RESET_SLOT) {
      // This item is not modified (or at least not by us) so we do not need to do anything
      return;
    }
    if (slot.isTooEarlyToReset()) {
      log.debug("Not resetting slot {} as it is too early", index);
      return;
    }
    int actualItemId = item == null ? INVALID_ITEM_ID : item.getId();
    int actualQuantity = item == null ? INVALID_ITEM_ID : item.getQuantity();

    int itemId = slot.getItemId();
    int quantity = slot.getQuantity();
    int modifiedTick = slot.getChangedTick();
    // Item at index changed so we must reset the slot
    if (slot.hasValidItemId() && (itemId != actualItemId || quantity != actualQuantity)) {
      log.debug("Item at index {} changed from item id {} to {} or from quantity {} to {}, resetting the item", index, itemId, actualItemId, quantity, actualQuantity);
      resetState(index, item, true);
      return;
    }

    // The item at the given index have not changes in some time, we reset to
    int ticksSinceModified = client.getTickCount() - modifiedTick;
    if (slot.hasChangedTick() && ticksSinceModified >= config.maxUnmodifiedTicks()) {
      log.debug("Item at index {} has not changed in {} tick, resetting the item", index, ticksSinceModified);
      resetState(index, item, true);
    }
  }
}
