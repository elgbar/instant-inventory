package no.elg.ii;

import static no.elg.ii.InstantInventoryPlugin.INVALID_ITEM_ID;
import static no.elg.ii.InstantInventoryPlugin.INVENTORY_SIZE;

import java.util.Arrays;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class InventoryState {

  public static final int NOT_MODIFIED = -1;
  public static final int MAX_UNMODIFIED_TICKS = 5;

  private final int[] items = new int[INVENTORY_SIZE];
  private final int[] modified = new int[INVENTORY_SIZE];


  public InventoryState() {
    resetAll();
  }

  public void setItemId(int index, int itemId) {
    modified[index] = InstantInventoryPlugin.tickCounter.get();
    items[index] = itemId;
  }

  public int getModifiedTick(int index) {
    return modified[index];
  }

  public int getItemId(int index) {
    return items[index];
  }

  public boolean isInvalid(int index) {
    return index == INVALID_ITEM_ID || items[index] == INVALID_ITEM_ID;
  }

  public void resetAll() {
    Arrays.fill(modified, NOT_MODIFIED);
    Arrays.fill(items, INVALID_ITEM_ID);
  }

  public void resetState(int index) {
    modified[index] = NOT_MODIFIED;
    items[index] = INVALID_ITEM_ID;
  }

  public void validateState(int index, int currentItemId) {
    int itemId = getItemId(index);
    boolean itemChanged = itemId != INVALID_ITEM_ID && itemId != currentItemId;
    if (itemChanged) {
      resetState(index);
    }

    int modifiedTick = getModifiedTick(index);
    boolean timedOut =
        modifiedTick != NOT_MODIFIED
            && InstantInventoryPlugin.tickCounter.get() - modifiedTick >= MAX_UNMODIFIED_TICKS;
    if (timedOut) {
      resetState(index);
    }
  }
}
