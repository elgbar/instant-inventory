package no.elg.ii;

import static no.elg.ii.InventoryState.INVALID_ITEM_ID;
import static no.elg.ii.InventoryState.INVENTORY_SIZE;
import static no.elg.ii.InventoryState.MAX_UNMODIFIED_TICKS;
import static no.elg.ii.InventoryState.NOT_MODIFIED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import net.runelite.api.Client;
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

  private void setAllIndexes(int itemId) {
    for (int i = 0; i < INVENTORY_SIZE; i++) {
      inventoryState.setItemId(i, itemId);
    }
  }

  @Test
  public void initially_invalid() {
    int index = 0;
    assertEquals(INVALID_ITEM_ID, inventoryState.getItemId(0));
    assertEquals(NOT_MODIFIED, inventoryState.getModifiedTick(0));
    assertTrue(inventoryState.isInvalid(index));
    assertFalse(inventoryState.isValid(index));
  }

  @Test
  public void index_out_bound_never_valid() {
    setAllIndexes(1);
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
    doReturn(1).when(client).getTickCount();
    setAllIndexes(2);

    for (int i = 0; i < INVENTORY_SIZE; i++) {
      assertEquals(2, inventoryState.getItemId(i));
      assertEquals(1, inventoryState.getModifiedTick(i));
    }

    inventoryState.resetAll();

    for (int i = 0; i < INVENTORY_SIZE; i++) {
      assertEquals(INVALID_ITEM_ID, inventoryState.getItemId(i));
      assertEquals(NOT_MODIFIED, inventoryState.getModifiedTick(i));
    }
  }

  @Test
  public void validateState_differentItemIdFromCurrent_resets() {
    doReturn(2).when(client).getTickCount();
    int index = 0;
    inventoryState.setItemId(index, 1);

    assertEquals(1, inventoryState.getItemId(0));
    assertEquals(2, inventoryState.getModifiedTick(0));

    inventoryState.validateState(index, 2);

    assertEquals(INVALID_ITEM_ID, inventoryState.getItemId(0));
    assertEquals(NOT_MODIFIED, inventoryState.getModifiedTick(0));
  }

  @Test
  public void validateState_timeout_resets_default_is_2() {
    int index = 0;
    inventoryState.setItemId(index, 1);

    assertEquals(1, inventoryState.getItemId(0));
    assertEquals(0, inventoryState.getModifiedTick(0));

    doReturn(MAX_UNMODIFIED_TICKS).when(client).getTickCount();

    inventoryState.validateState(index, 1);

    assertEquals(INVALID_ITEM_ID, inventoryState.getItemId(0));
    assertEquals(NOT_MODIFIED, inventoryState.getModifiedTick(0));
  }

  @Test
  public void validateState_timeout_resets_not_before_configurable_ticks() {
    int index = 0;
    int itemId = 1;
    doReturn(MAX_UNMODIFIED_TICKS + 1).when(config).maxUnmodifiedTicks();
    inventoryState.setItemId(index, itemId);

    assertEquals(itemId, inventoryState.getItemId(0));
    assertEquals(0, inventoryState.getModifiedTick(0));

    doReturn(MAX_UNMODIFIED_TICKS).when(client).getTickCount();

    inventoryState.validateState(index, itemId);

    assertEquals(itemId, inventoryState.getItemId(0));
    assertEquals(0, inventoryState.getModifiedTick(0));
  }

  @Test
  public void validateState_timeout_resets_customizable_time() {
    int index = 0;
    int itemId = 1;
    int maxUnmodifiedTicks = MAX_UNMODIFIED_TICKS + 1;
    doReturn(maxUnmodifiedTicks).when(config).maxUnmodifiedTicks();
    inventoryState.setItemId(index, itemId);

    assertEquals(itemId, inventoryState.getItemId(0));
    assertEquals(0, inventoryState.getModifiedTick(0));

    doReturn(maxUnmodifiedTicks).when(client).getTickCount();

    inventoryState.validateState(index, itemId);

    assertEquals(INVALID_ITEM_ID, inventoryState.getItemId(0));
    assertEquals(NOT_MODIFIED, inventoryState.getModifiedTick(0));
  }

  @Test
  public void validateState_new_item_when_invalid_does_not_change_state() {
    int index = 0;

    assertEquals(INVALID_ITEM_ID, inventoryState.getItemId(0));
    assertEquals(NOT_MODIFIED, inventoryState.getModifiedTick(0));

    inventoryState.validateState(index, 2);

    assertEquals(INVALID_ITEM_ID, inventoryState.getItemId(0));
    assertEquals(NOT_MODIFIED, inventoryState.getModifiedTick(0));
  }

  @Test
  public void validateState_not_timeout_and_same_item_does_not_reset() {
    inventoryState.setItemId(0, 1);

    assertEquals(1, inventoryState.getItemId(0));
    assertEquals(0, inventoryState.getModifiedTick(0));

    inventoryState.validateState(0, 1);

    assertEquals(1, inventoryState.getItemId(0));
    assertEquals(0, inventoryState.getModifiedTick(0));
  }
}