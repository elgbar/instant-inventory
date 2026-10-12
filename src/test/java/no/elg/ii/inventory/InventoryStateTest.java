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

package no.elg.ii.inventory;

import static no.elg.ii.feature.state.InventoryState.DEFAULT_MAX_UNMODIFIED_TICKS;
import static no.elg.ii.inventory.slot.InventorySlot.INVALID_ITEM_ID;
import static no.elg.ii.inventory.slot.InventorySlot.NO_CHANGED_TICK;
import static no.elg.ii.inventory.slot.InventorySlot.UNMODIFIED_SLOT;
import static no.elg.ii.util.InventoryUtil.INVENTORY_SIZE;
import static no.elg.ii.util.WidgetUtils.FULLY_OPAQUE;
import static no.elg.ii.util.WidgetUtils.FULLY_TRANSPARENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.stream.Stream;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.widgets.Widget;
import no.elg.ii.InstantInventoryConfig;
import no.elg.ii.feature.state.InventoryState;
import no.elg.ii.inventory.slot.InventorySlot;
import no.elg.ii.inventory.slot.SlotChange;
import no.elg.ii.model.IndexedWidget;
import no.elg.ii.service.InventoryService;
import no.elg.ii.test.TestSetup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class InventoryStateTest {

  private InventoryState inventoryState;
  private InstantInventoryConfig config;
  private Client client;
  private InventoryService inventoryService;

  private final int index = 3;
  private final int itemId = 1;
  private final int quantity = 2;
  private final Item item = new Item(itemId, quantity);

  @Before
  public void setUp() {
    // These tests drive time through the tick count, so the wall clock guard is off
    config = spy(new InstantInventoryConfig() {
      @Override
      public int minChangedMs() {
        return 0;
      }
    });
    client = TestSetup.mockClient();
    inventoryService = mock(InventoryService.class);

    inventoryState = new InventoryState(config, client, inventoryService);
  }

  private void setAll(int itemId) {
    for (int index = 0; index < INVENTORY_SIZE; index++) {
      inventoryState.setSlot(index, itemId, 0);
    }
  }

  @Test
  public void initially_invalid() {
    int index = 0;
    assertSame(inventoryState.getSlot(index), InventorySlot.UNMODIFIED_SLOT);
  }

  @Test
  public void resetAll_makesAllIndexesInvalid() {
    doReturn(123).when(client).getTickCount();
    setAll(1234);

    for (int i = 0; i < INVENTORY_SIZE; i++) {
      InventorySlot slot = inventoryState.getSlot(i);

      assertTrue(slot.hasValidItemId());
      assertTrue(slot.hasChangedTick());
    }

    inventoryState.resetAll();

    for (int i = 0; i < INVENTORY_SIZE; i++) {
      InventorySlot slot = inventoryState.getSlot(i);

      assertFalse(slot.hasValidItemId());
      assertFalse(slot.hasChangedTick());
      assertSame(slot, UNMODIFIED_SLOT);
    }
  }

  @Test
  public void validateState_differentItemIdFromCurrent_resets() {
    doReturn(2).when(client).getTickCount();
    inventoryState.setSlot(index, itemId, 0);

    assertEquals(itemId, inventoryState.getSlot(index).getItemId());
    assertEquals(2, inventoryState.getSlot(index).getChangedTick());

    inventoryState.validateState(index, item);

    assertEquals(INVALID_ITEM_ID, inventoryState.getSlot(index).getItemId());
    assertEquals(NO_CHANGED_TICK, inventoryState.getSlot(index).getChangedTick());
  }

  @Test
  public void validateState_timeout_resets_default_is_2() {
    inventoryState.setSlot(index, itemId, quantity);

    assertEquals(1, inventoryState.getSlot(index).getItemId());
    assertEquals(0, inventoryState.getSlot(index).getChangedTick());

    doReturn(DEFAULT_MAX_UNMODIFIED_TICKS).when(client).getTickCount();

    inventoryState.validateState(index, item);

    assertEquals(INVALID_ITEM_ID, inventoryState.getSlot(index).getItemId());
    assertEquals(NO_CHANGED_TICK, inventoryState.getSlot(index).getChangedTick());
  }

  @Test
  public void validateState_timeout_resets_not_before_configurable_ticks() {

    doReturn(DEFAULT_MAX_UNMODIFIED_TICKS + 1).when(config).maxUnmodifiedTicks();
    inventoryState.setSlot(index, itemId, quantity);

    assertEquals(itemId, inventoryState.getSlot(index).getItemId());
    assertEquals(0, inventoryState.getSlot(index).getChangedTick());

    doReturn(DEFAULT_MAX_UNMODIFIED_TICKS).when(client).getTickCount();

    inventoryState.validateState(index, item);

    assertEquals(itemId, inventoryState.getSlot(index).getItemId());
    assertEquals(0, inventoryState.getSlot(index).getChangedTick());
  }

  @Test
  public void validateState_timeout_resets_customizable_time() {
    int maxUnmodifiedTicks = DEFAULT_MAX_UNMODIFIED_TICKS + 1;
    doReturn(maxUnmodifiedTicks).when(config).maxUnmodifiedTicks();
    inventoryState.setSlot(index, itemId, quantity);

    assertEquals(itemId, inventoryState.getSlot(index).getItemId());
    assertEquals(0, inventoryState.getSlot(index).getChangedTick());

    doReturn(maxUnmodifiedTicks).when(client).getTickCount();

    inventoryState.validateState(index, item);

    assertEquals(INVALID_ITEM_ID, inventoryState.getSlot(index).getItemId());
    assertEquals(NO_CHANGED_TICK, inventoryState.getSlot(index).getChangedTick());
  }

  @Test
  public void validateState_new_item_when_invalid_does_not_change_state() {
    int index = 0;

    assertEquals(INVALID_ITEM_ID, inventoryState.getSlot(index).getItemId());
    assertEquals(NO_CHANGED_TICK, inventoryState.getSlot(index).getChangedTick());

    inventoryState.validateState(index, item);

    assertEquals(INVALID_ITEM_ID, inventoryState.getSlot(index).getItemId());
    assertEquals(NO_CHANGED_TICK, inventoryState.getSlot(index).getChangedTick());
  }

  @Test
  public void validateState_not_timeout_and_same_item_does_not_reset() {
    inventoryState.setSlot(index, itemId, quantity);

    assertEquals(itemId, inventoryState.getSlot(index).getItemId());
    assertEquals(quantity, inventoryState.getSlot(index).getQuantity());
    assertEquals(0, inventoryState.getSlot(index).getChangedTick());

    inventoryState.validateState(0, item);

    assertEquals(itemId, inventoryState.getSlot(index).getItemId());
    assertEquals(quantity, inventoryState.getSlot(index).getQuantity());
    assertEquals(0, inventoryState.getSlot(index).getChangedTick());
  }

  @Test
  public void validateState_filling_slot_left_empty_by_server_draws_widget_empty() {
    Widget widget = mock(Widget.class);
    showInInventory(widget);
    inventoryState.setSlot(index, itemId, quantity, SlotChange.FILL);

    // The server did not put the withdrawn item in this slot, so only the predicted item is drawn there
    inventoryState.validateState(index, null);

    assertSame(UNMODIFIED_SLOT, inventoryState.getSlot(index));
    // Hidden would be permanent, as the inventory next to the bank never unhides a slot when the item arrives late
    verify(widget).setHidden(false);
    verify(widget).setOpacity(FULLY_TRANSPARENT);
  }

  @Test
  public void validateState_other_slot_left_empty_by_server_makes_widget_opaque() {
    Widget widget = mock(Widget.class);
    showInInventory(widget);
    inventoryState.setSlot(index, itemId, quantity);

    // The server removed the item, and the client has already drawn the slot as empty
    inventoryState.validateState(index, null);

    assertSame(UNMODIFIED_SLOT, inventoryState.getSlot(index));
    verify(widget).setHidden(false);
    verify(widget).setOpacity(FULLY_OPAQUE);
  }

  private void showInInventory(Widget widget) {
    doAnswer(invocation -> Stream.of(new IndexedWidget(index, widget))).when(inventoryService).getAllOpenInventoryWidgets();
  }
}
