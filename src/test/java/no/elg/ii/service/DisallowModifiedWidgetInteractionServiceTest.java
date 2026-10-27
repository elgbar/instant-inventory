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

package no.elg.ii.service;

import static net.runelite.api.gameval.ItemID.BLANKOBJECT;
import static net.runelite.api.gameval.ItemID.BRONZE_DAGGER;
import static net.runelite.api.gameval.ItemID.BRONZE_SQ_SHIELD;
import static net.runelite.api.gameval.ItemID.BRONZE_SWORD;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.WidgetDrag;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.EventBus;
import no.elg.ii.feature.state.InventoryState;
import no.elg.ii.inventory.slot.SlotChange;
import no.elg.ii.model.IndexedWidget;
import no.elg.ii.test.TestSetup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class DisallowModifiedWidgetInteractionServiceTest {

  /**
   * What the server has in each inventory slot
   */
  private final Map<Integer, Item> serverInventory = new HashMap<>();
  /**
   * The inventory slots the game shows
   */
  private final List<Widget> shownSlots = new ArrayList<>();

  /**
   * The mouse button held down, or 0 when none is
   */
  private int mouseButton;
  /**
   * The slot being dragged, or null when none is
   */
  private Widget dragged;
  /**
   * The slot the dragged one is over, or null when there is none
   */
  private Widget draggedOn;

  /**
   * Passes on what a subscriber throws, which the default {@link EventBus} would only log
   */
  private final EventBus eventBus = new EventBus(e -> {
    throw new AssertionError("A subscriber threw", e);
  });

  private InventoryState state;

  @Before
  public void setUp() {
    Client client = TestSetup.mockClient(this::answerLikeTheClient);
    ItemContainer inventoryContainer = mock(ItemContainer.class, withSettings().defaultAnswer(invocation ->
      "getItem".equals(invocation.getMethod().getName())
        ? serverInventory.get(invocation.<Integer>getArgument(0))
        : Mockito.RETURNS_DEFAULTS.answer(invocation)));
    InventoryService inventoryService = mock(InventoryService.class, withSettings().defaultAnswer(invocation -> {
      switch (invocation.getMethod().getName()) {
        case "getCurrentInventoryContainer":
          return inventoryContainer;
        case "getAllOpenInventoryWidgets":
          return shownSlots.stream().map(slot -> new IndexedWidget(slot.getIndex(), slot));
        default:
          return Mockito.RETURNS_DEFAULTS.answer(invocation);
      }
    }));
    state = TestSetup.newInventoryState(client, inventoryService);

    DisallowModifiedWidgetInteractionService service = new DisallowModifiedWidgetInteractionService();
    service.client = client;
    service.inventoryService = inventoryService;
    service.inventoryState = state;
    eventBus.register(service);
  }

  @Test
  public void onMenuOptionClicked_wield_of_item_dragged_to_another_slot_in_the_same_tick_is_allowed() {
    // Issue #34. The server handles the move before the wield, so the wield works just like without the plugin
    Widget firstSlot = itemInSlot(0, BRONZE_SWORD);
    Widget secondSlot = itemInSlot(1, BRONZE_SQ_SHIELD);
    dragInGame(firstSlot, secondSlot);

    MenuOptionClicked wieldSword = click("Wield", secondSlot);
    eventBus.post(wieldSword);

    assertFalse(wieldSword.isConsumed());
  }

  @Test
  public void onMenuOptionClicked_drop_of_what_equipping_takes_off_is_disallowed() {
    // Issue #22. The server handles the wield first, so it has the dagger in the slot by the time it handles the drop.
    // Without the plugin the slot still shows the sword, so the drop would be for the sword
    Widget slot = itemInSlot(0, BRONZE_SWORD);
    showWhatEquippingTakesOff(slot, BRONZE_DAGGER);

    MenuOptionClicked dropDagger = click("Drop", slot);
    eventBus.post(dropDagger);

    assertTrue(dropDagger.isConsumed());
  }

  @Test
  public void onMenuOptionClicked_drop_of_what_equipping_takes_off_dragged_onto_another_item_is_not_sent_for_it() {
    // Issue #22 through a drag. The server handles the wield and then the move, so it has the dagger in the second slot
    // by the time it handles the drop
    Widget firstSlot = itemInSlot(0, BRONZE_SWORD);
    Widget secondSlot = itemInSlot(1, BRONZE_SQ_SHIELD);
    showWhatEquippingTakesOff(firstSlot, BRONZE_DAGGER);
    dragInGame(firstSlot, secondSlot);

    MenuOptionClicked drop = click("Drop", secondSlot);
    eventBus.post(drop);

    boolean droppedDagger = !drop.isConsumed() && drop.getItemId() == BRONZE_DAGGER;
    assertFalse("The dagger would be dropped in the tick it is taken off", droppedDagger);
  }

  @Test
  public void onMenuOptionClicked_drop_of_what_equipping_takes_off_dragged_to_an_empty_slot_is_not_sent_for_it() {
    // Issue #22 through a drag into an empty slot. The second slot stays empty on the server until it has handled the move
    Widget firstSlot = itemInSlot(0, BRONZE_SWORD);
    Widget secondSlot = emptySlot(1);
    showWhatEquippingTakesOff(firstSlot, BRONZE_DAGGER);
    dragInGame(firstSlot, secondSlot);

    MenuOptionClicked drop = click("Drop", secondSlot);
    eventBus.post(drop);

    boolean droppedDagger = !drop.isConsumed() && drop.getItemId() == BRONZE_DAGGER;
    assertFalse("The dagger would be dropped in the tick it is taken off", droppedDagger);
  }

  @Test
  public void onMenuOptionClicked_drop_of_what_equipping_an_item_dragged_to_an_empty_slot_takes_off_is_disallowed() {
    // Issue #22 as reproduced in game: the item is dragged to an empty slot and equipped there in the same tick. The
    // server has that slot empty until it handles the move, and then the wield puts the dagger there
    Widget firstSlot = itemInSlot(0, BRONZE_SWORD);
    Widget secondSlot = emptySlot(1);
    dragInGame(firstSlot, secondSlot);
    showWhatEquippingTakesOff(secondSlot, BRONZE_DAGGER);

    MenuOptionClicked dropDagger = click("Drop", secondSlot);
    eventBus.post(dropDagger);

    assertTrue(dropDagger.isConsumed());
  }

  @Test
  public void onWidgetDrag_between_slots_that_are_not_predicted_does_not_reset_them() {
    // Other plugins may hide slots, which a reset would undo
    Widget firstSlot = itemInSlot(0, BRONZE_SWORD);
    Widget secondSlot = itemInSlot(1, BRONZE_SQ_SHIELD);

    dragInGame(firstSlot, secondSlot);

    verify(firstSlot, never()).setHidden(anyBoolean());
    verify(secondSlot, never()).setHidden(anyBoolean());
  }

  @Test
  public void onWidgetDrag_onto_the_same_slot_keeps_its_prediction() {
    // Nothing moves, so the prediction is still right
    Widget slot = itemInSlot(0, BRONZE_SWORD);
    showWhatEquippingTakesOff(slot, BRONZE_DAGGER);

    dragInGame(slot, slot);

    assertTrue(state.isActiveSlot(0));
  }

  /**
   * Put {@code itemId} in slot {@code index} on the server, and show it there like the game does
   */
  private Widget itemInSlot(int index, int itemId) {
    serverInventory.put(index, new Item(itemId, 1));
    return shownSlot(index, itemId);
  }

  /**
   * Leave slot {@code index} empty on the server, and show it empty like the game does, with the blank item
   */
  private Widget emptySlot(int index) {
    return shownSlot(index, BLANKOBJECT);
  }

  /**
   * A slot of the inventory the game shows with {@code itemId}. It keeps the item it is given, like the game's widgets do
   */
  private Widget shownSlot(int index, int itemId) {
    int[] shownItemId = {itemId};
    Widget slot = mock(Widget.class, withSettings().defaultAnswer(invocation -> {
      switch (invocation.getMethod().getName()) {
        case "getId":
          return InterfaceID.Inventory.ITEMS;
        case "getIndex":
          return index;
        case "getItemId":
          return shownItemId[0];
        case "setItemId":
          shownItemId[0] = invocation.getArgument(0);
          return invocation.getMock();
        default:
          return Mockito.RETURNS_DEFAULTS.answer(invocation);
      }
    }));
    shownSlots.add(slot);
    return slot;
  }

  /**
   * Show in {@code slot} what equipping its item takes off, before the server has done it, like
   * {@link no.elg.ii.feature.features.EquipFeature} does
   */
  private void showWhatEquippingTakesOff(Widget slot, int takenOffItemId) {
    slot.setItemId(takenOffItemId);
    state.setSlot(slot, SlotChange.REPLACE);
  }

  /**
   * Drag the item {@code from} shows onto {@code onto} like the player does in the game.
   * <p>
   * The client posts {@link WidgetDrag} every cycle the mouse button is held, and once more in the cycle it is let go,
   * before it handles the drop (RuneLite's prayer reordering cancels the drop there). The game's
   * {@code inventory_reorder} script then shows each slot's item in the other, reading them from what the slots show.
   * The server only has the move once it has handled it
   */
  private void dragInGame(Widget from, Widget onto) {
    dragged = from;
    draggedOn = onto;
    mouseButton = 1;
    eventBus.post(new WidgetDrag());
    mouseButton = 0;
    eventBus.post(new WidgetDrag());
    if (draggedOn != null) {
      int fromItemId = from.getItemId();
      from.setItemId(draggedOn.getItemId());
      draggedOn.setItemId(fromItemId);
    }
    dragged = null;
    draggedOn = null;
  }

  /**
   * Click {@code option} on {@code slot}. The client sends it for the item the slot shows
   */
  private static MenuOptionClicked click(String option, Widget slot) {
    int itemId = slot.getItemId();
    MenuEntry itemOp = mock(MenuEntry.class, withSettings().defaultAnswer(invocation -> {
      switch (invocation.getMethod().getName()) {
        case "getOption":
          return option;
        case "isItemOp":
          return true;
        case "getItemId":
          return itemId;
        case "getWidget":
          return slot;
        default:
          return Mockito.RETURNS_DEFAULTS.answer(invocation);
      }
    }));
    return new MenuOptionClicked(itemOp);
  }

  private Object answerLikeTheClient(InvocationOnMock invocation) throws Throwable {
    switch (invocation.getMethod().getName()) {
      case "getMouseCurrentButton":
        return mouseButton;
      case "isDraggingWidget":
        return dragged != null;
      case "getDraggedWidget":
        return dragged;
      case "getDraggedOnWidget":
        return draggedOn;
      case "setDraggedOnWidget":
        draggedOn = invocation.getArgument(0);
        return null;
      default:
        return Mockito.RETURNS_DEFAULTS.answer(invocation);
    }
  }
}
