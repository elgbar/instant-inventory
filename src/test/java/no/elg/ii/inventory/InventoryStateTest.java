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

package no.elg.ii.inventory;

import static no.elg.ii.inventory.slot.InventorySlot.INVALID_ITEM_ID;
import static no.elg.ii.util.InventoryUtil.INVENTORY_SIZE;
import static no.elg.ii.inventory.InventoryState.DEFAULT_MAX_UNMODIFIED_TICKS;
import static no.elg.ii.inventory.slot.InventorySlot.NO_CHANGED_TICK;
import static no.elg.ii.inventory.slot.InventorySlot.UNMODIFIED_SLOT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import net.runelite.api.Client;
import no.elg.ii.InstantInventoryConfig;
import no.elg.ii.inventory.slot.InventorySlot;
import org.junit.Before;
import org.junit.Test;

public class InventoryStateTest {

  private InventoryState inventoryState;
  private InstantInventoryConfig config;
  private Client client;

  @Before
  public void setUp() {
    config = spy(new InstantInventoryConfig() {
    });
    client = mock(Client.class);
    inventoryState = new InventoryState(config, client);
  }

  private void setAll(int itemId) {
    for (int index = 0; index < INVENTORY_SIZE; index++) {
      inventoryState.setItemId(index, itemId);
    }
  }

  @Test
  public void initially_invalid() {
    int index = 0;
    assertSame(inventoryState.getSlot(index), InventorySlot.UNMODIFIED_SLOT);
    assertTrue(inventoryState.isInvalid(index));
    assertFalse(inventoryState.isValid(index));
  }

  @Test
  public void index_out_bound_never_valid() {
    setAll(1);
    assertTrue(inventoryState.isInvalid(-1));
    assertFalse(inventoryState.isValid(-1));
    assertTrue(inventoryState.isInvalid(INVENTORY_SIZE));
    assertFalse(inventoryState.isValid(INVENTORY_SIZE));
  }

  @Test
  public void state_is_valid_when_not_invalid_item_id() {
    inventoryState.setItemId(0, 1);
    assertFalse(inventoryState.isInvalid(0));
    assertTrue(inventoryState.isValid(0));
  }

  @Test
  public void resetAll_makesAllIndexesInvalid() {
    doReturn(123).when(client).getTickCount();
    setAll(1234);

    for (int i = 0; i < INVENTORY_SIZE; i++) {
      InventorySlot slot = inventoryState.getSlot(i);

      assertTrue(slot.hasValidItemId());
      assertTrue(slot.hasChangedTick());
      assertSame(slot, UNMODIFIED_SLOT);
    }

    inventoryState.resetAll();

    for (int i = 0; i < INVENTORY_SIZE; i++) {
      InventorySlot slot = inventoryState.getSlot(i);

      assertFalse(slot.hasValidItemId());
      assertFalse(slot.hasChangedTick());
      assertNotSame(slot, UNMODIFIED_SLOT);
    }
  }

  @Test
  public void validateState_differentItemIdFromCurrent_resets() {
    doReturn(2).when(client).getTickCount();
    int index = 0;
    inventoryState.setItemId(index, 1);

    assertEquals(1, inventoryState.getSlot(index).getItemId());
    assertEquals(2, inventoryState.getSlot(index).getChangedTick());

    inventoryState.validateState(index, 2);

    assertEquals(INVALID_ITEM_ID, inventoryState.getSlot(index).getItemId());
    assertEquals(NO_CHANGED_TICK, inventoryState.getSlot(index).getChangedTick());
  }

  @Test
  public void validateState_timeout_resets_default_is_2() {
    int index = 0;
    inventoryState.setItemId(index, 1);

    assertEquals(1, inventoryState.getSlot(index).getItemId());
    assertEquals(0, inventoryState.getSlot(index).getChangedTick());

    doReturn(DEFAULT_MAX_UNMODIFIED_TICKS).when(client).getTickCount();

    inventoryState.validateState(index, 1);

    assertEquals(INVALID_ITEM_ID, inventoryState.getSlot(index).getItemId());
    assertEquals(NO_CHANGED_TICK, inventoryState.getSlot(index).getChangedTick());
  }

  @Test
  public void validateState_timeout_resets_not_before_configurable_ticks() {
    int index = 0;
    int itemId = 1;
    doReturn(DEFAULT_MAX_UNMODIFIED_TICKS + 1).when(config).maxUnmodifiedTicks();
    inventoryState.setItemId(index, itemId);

    assertEquals(itemId, inventoryState.getSlot(index).getItemId());
    assertEquals(0, inventoryState.getSlot(index).getChangedTick());

    doReturn(DEFAULT_MAX_UNMODIFIED_TICKS).when(client).getTickCount();

    inventoryState.validateState(index, itemId);

    assertEquals(itemId, inventoryState.getSlot(index).getItemId());
    assertEquals(0, inventoryState.getSlot(index).getChangedTick());
  }

  @Test
  public void validateState_timeout_resets_customizable_time() {
    int index = 0;
    int itemId = 1;
    int maxUnmodifiedTicks = DEFAULT_MAX_UNMODIFIED_TICKS + 1;
    doReturn(maxUnmodifiedTicks).when(config).maxUnmodifiedTicks();
    inventoryState.setItemId(index, itemId);

    assertEquals(itemId, inventoryState.getSlot(index).getItemId());
    assertEquals(0, inventoryState.getSlot(index).getChangedTick());

    doReturn(maxUnmodifiedTicks).when(client).getTickCount();

    inventoryState.validateState(index, itemId);

    assertEquals(INVALID_ITEM_ID, inventoryState.getSlot(index).getItemId());
    assertEquals(NO_CHANGED_TICK, inventoryState.getSlot(index).getChangedTick());
  }

  @Test
  public void validateState_new_item_when_invalid_does_not_change_state() {
    int index = 0;

    assertEquals(INVALID_ITEM_ID, inventoryState.getSlot(index).getItemId());
    assertEquals(NO_CHANGED_TICK, inventoryState.getSlot(index).getChangedTick());

    inventoryState.validateState(index, 2);

    assertEquals(INVALID_ITEM_ID, inventoryState.getSlot(index).getItemId());
    assertEquals(NO_CHANGED_TICK, inventoryState.getSlot(index).getChangedTick());
  }

  @Test
  public void validateState_not_timeout_and_same_item_does_not_reset() {
    int index = 0;
    inventoryState.setItemId(0, 1);

    assertEquals(1, inventoryState.getSlot(index).getItemId());
    assertEquals(0, inventoryState.getSlot(index).getChangedTick());

    inventoryState.validateState(0, 1);

    assertEquals(1, inventoryState.getSlot(index).getItemId());
    assertEquals(0, inventoryState.getSlot(index).getChangedTick());
  }
}
