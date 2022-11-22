package no.elg.ii;

import java.util.Arrays;
import lombok.EqualsAndHashCode;
import net.runelite.api.events.GameTick;

/**
 * Hold the state of the players inventory. The state is checked every server tick in
 * {@link InstantInventoryPlugin#onGameTick(GameTick)}
 * <p>
 * A {@link InstantInventoryComponent} uses this class to handle how to render the changes on the
 * client. Typically, a component will modify the rendering of a given item in the inventory of the
 * player when the {@link #getItemId(int)} is different to {@link #INVALID_ITEM_ID}.
 */
@EqualsAndHashCode
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
   * @return Whether the {@code index} and the item at the given index is valid
   */
  public boolean isInvalid(int index) {
    return index == INVALID_ITEM_ID || items[index] == INVALID_ITEM_ID;
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
