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

package no.elg.ii.feature.features;

import static net.runelite.api.gameval.ItemID.BRONZE_2H_SWORD;
import static net.runelite.api.gameval.ItemID.BRONZE_SQ_SHIELD;
import static net.runelite.api.gameval.ItemID.BRONZE_SWORD;
import static net.runelite.api.gameval.ItemID.OBLIGATOR;
import static net.runelite.api.gameval.ItemID.RONDACHE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.widgets.Widget;
import no.elg.ii.inventory.slot.SlotChange;
import no.elg.ii.model.EquipSlot;
import no.elg.ii.service.InventoryService;
import no.elg.ii.service.ItemEquipmentService;
import no.elg.ii.service.WidgetService;
import no.elg.ii.test.StatefulFeatureTestMother;
import no.elg.ii.test.TestSetup;
import org.junit.Test;
import org.mockito.Mockito;

public class EquipFeatureTest extends StatefulFeatureTestMother<EquipFeature> {

  private static final int MAIN_HAND_INDEX = 3;
  private static final int OFF_HAND_INDEX = 7;

  private static final Item TWO_HANDED = new Item(BRONZE_2H_SWORD, 1);
  private static final Item SWORD = new Item(BRONZE_SWORD, 1);
  private static final Item SHIELD = new Item(BRONZE_SQ_SHIELD, 1);

  private static final Item NEW_WEAPON = new Item(OBLIGATOR, 1);

  /**
   * Where the item equipment service says items are worn, by item id
   */
  private final Map<Integer, EquipSlot> equipSlots = new HashMap<>(Map.of(
    BRONZE_2H_SWORD, new EquipSlot(EquipmentInventorySlot.WEAPON.getSlotIdx(), true),
    BRONZE_SWORD, new EquipSlot(EquipmentInventorySlot.WEAPON.getSlotIdx(), false),
    BRONZE_SQ_SHIELD, new EquipSlot(EquipmentInventorySlot.SHIELD.getSlotIdx(), false)
  ));

  /**
   * What the server says is worn, by equipment slot
   */
  private final Map<Integer, Item> worn = new HashMap<>();

  /**
   * The tick the client is at. Not 0, as no slot has been equipped then
   */
  private int tick = 10;

  @Override
  public EquipFeature createNewInstance() {
    return TestSetup.createNewEquipFeature();
  }

  @Test
  public void onMenuOptionClicked_main_hand_then_off_hand_over_two_handed_empties_the_off_hand_slot() {
    EquipFeature feature = createWiredInstance();
    worn.put(EquipmentInventorySlot.WEAPON.getSlotIdx(), TWO_HANDED);
    Widget mainHand = inventoryItem(MAIN_HAND_INDEX, BRONZE_SWORD);
    Widget offHand = inventoryItem(OFF_HAND_INDEX, BRONZE_SQ_SHIELD);

    feature.onMenuOptionClicked(equip(mainHand));
    feature.onMenuOptionClicked(equip(offHand));

    verify(feature.widgetService).setFakeWidgetItem(mainHand, TWO_HANDED, SlotChange.REPLACE);
    assertEquals(SlotChange.REPLACE, feature.getState().getSlot(MAIN_HAND_INDEX).getChange());
    // The main hand has already taken the two-handed weapon's place
    verify(feature.widgetService, never()).setFakeWidgetItem(eq(offHand), any(Item.class), any());
    assertTrue(feature.getState().getSlot(OFF_HAND_INDEX).isEmptying());
  }

  @Test
  public void onMenuOptionClicked_off_hand_then_main_hand_over_two_handed_empties_the_main_hand_slot() {
    EquipFeature feature = createWiredInstance();
    worn.put(EquipmentInventorySlot.WEAPON.getSlotIdx(), TWO_HANDED);
    Widget mainHand = inventoryItem(MAIN_HAND_INDEX, BRONZE_SWORD);
    Widget offHand = inventoryItem(OFF_HAND_INDEX, BRONZE_SQ_SHIELD);

    feature.onMenuOptionClicked(equip(offHand));
    feature.onMenuOptionClicked(equip(mainHand));

    verify(feature.widgetService).setFakeWidgetItem(offHand, TWO_HANDED, SlotChange.REPLACE);
    assertEquals(SlotChange.REPLACE, feature.getState().getSlot(OFF_HAND_INDEX).getChange());
    // The off-hand has already taken the two-handed weapon off
    verify(feature.widgetService, never()).setFakeWidgetItem(eq(mainHand), any(Item.class), any());
    assertTrue(feature.getState().getSlot(MAIN_HAND_INDEX).isEmptying());
  }

  @Test
  public void onMenuOptionClicked_main_hand_then_off_hand_next_tick_over_two_handed_empties_the_off_hand_slot() {
    EquipFeature feature = createWiredInstance();
    worn.put(EquipmentInventorySlot.WEAPON.getSlotIdx(), TWO_HANDED);
    Widget mainHand = inventoryItem(MAIN_HAND_INDEX, BRONZE_SWORD);
    Widget offHand = inventoryItem(OFF_HAND_INDEX, BRONZE_SQ_SHIELD);

    feature.onMenuOptionClicked(equip(mainHand));
    // The main hand was equipped too late in the tick for the server to handle it, so the two-handed weapon is still worn
    tick++;
    feature.onMenuOptionClicked(equip(offHand));

    verify(feature.widgetService, never()).setFakeWidgetItem(eq(offHand), any(Item.class), any());
    assertTrue(feature.getState().getSlot(OFF_HAND_INDEX).isEmptying());
  }

  @Test
  public void onMenuOptionClicked_item_clicked_again_while_being_equipped_keeps_emptying() {
    EquipFeature feature = createWiredInstance();
    worn.put(EquipmentInventorySlot.WEAPON.getSlotIdx(), TWO_HANDED);
    Widget mainHand = inventoryItem(MAIN_HAND_INDEX, BRONZE_SWORD);
    Widget offHand = inventoryItem(OFF_HAND_INDEX, BRONZE_SQ_SHIELD);

    feature.onMenuOptionClicked(equip(mainHand));
    tick++;
    feature.onMenuOptionClicked(equip(offHand));
    // The off-hand has left the slot by the time the server handles this, so it is not equipped again
    tick++;
    feature.onMenuOptionClicked(equip(offHand));

    verify(feature.widgetService, never()).setFakeWidgetItem(eq(offHand), any(Item.class), any());
    assertTrue(feature.getState().getSlot(OFF_HAND_INDEX).isEmptying());
  }

  @Test
  public void onMenuOptionClicked_equip_no_longer_shown_in_the_inventory_is_read_from_what_is_worn() {
    EquipFeature feature = createWiredInstance();
    worn.put(EquipmentInventorySlot.WEAPON.getSlotIdx(), TWO_HANDED);
    Widget mainHand = inventoryItem(MAIN_HAND_INDEX, BRONZE_SWORD);
    Widget offHand = inventoryItem(OFF_HAND_INDEX, BRONZE_SQ_SHIELD);

    feature.onMenuOptionClicked(equip(offHand));
    // The server did not equip the off-hand, so the inventory shows what the server says again
    tick++;
    feature.getState().resetState(OFF_HAND_INDEX);
    feature.onMenuOptionClicked(equip(mainHand));

    verify(feature.widgetService).setFakeWidgetItem(mainHand, TWO_HANDED, SlotChange.REPLACE);
    assertEquals(SlotChange.REPLACE, feature.getState().getSlot(MAIN_HAND_INDEX).getChange());
  }

  @Test
  public void onMenuOptionClicked_item_not_known_where_it_is_worn_is_not_equipped() {
    EquipFeature feature = createWiredInstance();
    worn.put(EquipmentInventorySlot.SHIELD.getSlotIdx(), SHIELD);
    Widget newShield = inventoryItem(OFF_HAND_INDEX, RONDACHE);

    feature.onMenuOptionClicked(equip(newShield));

    // Where it is worn decides what takes its place in the inventory
    verify(feature.widgetService, never()).setFakeWidgetItem(eq(newShield), any(Item.class), any());
    assertFalse(feature.getState().isActiveSlot(OFF_HAND_INDEX));
  }

  @Test
  public void onMenuOptionClicked_shield_not_known_to_be_two_handed_replaces_the_worn_shield() {
    EquipFeature feature = createWiredInstance();
    // Only weapons can be two-handed, so not knowing it does not matter for a shield
    knownOnlyWhereWorn(RONDACHE, EquipmentInventorySlot.SHIELD);
    worn.put(EquipmentInventorySlot.SHIELD.getSlotIdx(), SHIELD);
    Widget newShield = inventoryItem(OFF_HAND_INDEX, RONDACHE);

    feature.onMenuOptionClicked(equip(newShield));

    verify(feature.widgetService).setFakeWidgetItem(newShield, SHIELD, SlotChange.REPLACE);
    assertEquals(SlotChange.REPLACE, feature.getState().getSlot(OFF_HAND_INDEX).getChange());
  }

  @Test
  public void onMenuOptionClicked_weapon_not_known_to_be_two_handed_without_a_shield_replaces_the_wielded_weapon() {
    EquipFeature feature = createWiredInstance();
    knownOnlyWhereWorn(OBLIGATOR, EquipmentInventorySlot.WEAPON);
    worn.put(EquipmentInventorySlot.WEAPON.getSlotIdx(), SWORD);
    Widget mainHand = inventoryItem(MAIN_HAND_INDEX, OBLIGATOR);

    feature.onMenuOptionClicked(equip(mainHand));

    verify(feature.widgetService).setFakeWidgetItem(mainHand, SWORD, SlotChange.REPLACE);
    assertEquals(SlotChange.REPLACE, feature.getState().getSlot(MAIN_HAND_INDEX).getChange());
  }

  @Test
  public void onMenuOptionClicked_weapon_not_known_to_be_two_handed_while_wearing_a_shield_is_not_equipped() {
    EquipFeature feature = createWiredInstance();
    knownOnlyWhereWorn(OBLIGATOR, EquipmentInventorySlot.WEAPON);
    worn.put(EquipmentInventorySlot.WEAPON.getSlotIdx(), SWORD);
    worn.put(EquipmentInventorySlot.SHIELD.getSlotIdx(), SHIELD);
    Widget mainHand = inventoryItem(MAIN_HAND_INDEX, OBLIGATOR);

    feature.onMenuOptionClicked(equip(mainHand));

    // A two-handed weapon would also take off the shield
    verify(feature.widgetService, never()).setFakeWidgetItem(eq(mainHand), any(Item.class), any());
    assertFalse(feature.getState().isActiveSlot(MAIN_HAND_INDEX));
  }

  @Test
  public void onMenuOptionClicked_shield_while_wielding_a_weapon_not_known_to_be_two_handed_is_not_equipped() {
    EquipFeature feature = createWiredInstance();
    knownOnlyWhereWorn(OBLIGATOR, EquipmentInventorySlot.WEAPON);
    worn.put(EquipmentInventorySlot.WEAPON.getSlotIdx(), NEW_WEAPON);
    Widget offHand = inventoryItem(OFF_HAND_INDEX, BRONZE_SQ_SHIELD);

    feature.onMenuOptionClicked(equip(offHand));

    // A two-handed weapon would take the shield's place in the inventory
    verify(feature.widgetService, never()).setFakeWidgetItem(eq(offHand), any(Item.class), any());
    assertFalse(feature.getState().isActiveSlot(OFF_HAND_INDEX));
  }

  @Test
  public void onMenuOptionClicked_shield_while_wielding_a_one_handed_weapon_empties_the_slot() {
    EquipFeature feature = createWiredInstance();
    worn.put(EquipmentInventorySlot.WEAPON.getSlotIdx(), SWORD);
    Widget offHand = inventoryItem(OFF_HAND_INDEX, BRONZE_SQ_SHIELD);

    feature.onMenuOptionClicked(equip(offHand));

    verify(feature.widgetService, never()).setFakeWidgetItem(eq(offHand), any(Item.class), any());
    assertTrue(feature.getState().getSlot(OFF_HAND_INDEX).isEmptying());
  }

  @Test
  public void onMenuOptionClicked_shield_while_wielding_a_two_handed_weapon_replaces_it_with_the_weapon() {
    EquipFeature feature = createWiredInstance();
    worn.put(EquipmentInventorySlot.WEAPON.getSlotIdx(), TWO_HANDED);
    Widget offHand = inventoryItem(OFF_HAND_INDEX, BRONZE_SQ_SHIELD);

    feature.onMenuOptionClicked(equip(offHand));

    verify(feature.widgetService).setFakeWidgetItem(offHand, TWO_HANDED, SlotChange.REPLACE);
    assertEquals(SlotChange.REPLACE, feature.getState().getSlot(OFF_HAND_INDEX).getChange());
  }

  private EquipFeature createWiredInstance() {
    EquipFeature feature = createNewInstance();
    feature.client = TestSetup.mockClient(invocation ->
      "getTickCount".equals(invocation.getMethod().getName())
        ? tick
        : Mockito.RETURNS_DEFAULTS.answer(invocation));
    feature.clientThread = TestSetup.inlineClientThread();
    feature.itemEquipmentService = new ItemEquipmentService() {
      @Nullable
      @Override
      public EquipSlot getEquipSlot(int itemId) {
        return equipSlots.get(itemId);
      }
    };
    feature.state = TestSetup.newInventoryState(feature.client);
    feature.widgetService = mock(WidgetService.class);
    feature.inventoryService = mock(InventoryService.class);
    doReturn(mock(ItemContainer.class)).when(feature.inventoryService).getCurrentInventoryContainer();
    ItemContainer wornContainer = mock(ItemContainer.class, withSettings().defaultAnswer(invocation ->
      "getItem".equals(invocation.getMethod().getName())
        ? worn.get(invocation.<Integer>getArgument(0))
        : Mockito.RETURNS_DEFAULTS.answer(invocation)));
    doReturn(wornContainer).when(feature.client).getItemContainer(InventoryID.WORN);
    return feature;
  }

  /**
   * The item equipment service knows where the item is worn, but not whether it is two-handed, as for an item without
   * item stats it has seen worn
   */
  private void knownOnlyWhereWorn(int itemId, EquipmentInventorySlot slot) {
    equipSlots.put(itemId, new EquipSlot(slot.getSlotIdx(), null));
  }

  private static Widget inventoryItem(int index, int itemId) {
    Widget widget = mock(Widget.class);
    doReturn(index).when(widget).getIndex();
    doReturn(itemId).when(widget).getItemId();
    doReturn(1).when(widget).getItemQuantity();
    return widget;
  }

  private static MenuOptionClicked equip(Widget inventoryItem) {
    MenuEntry menuEntry = mock(MenuEntry.class);
    doReturn(inventoryItem).when(menuEntry).getWidget();
    doReturn("Wield").when(menuEntry).getOption();
    return new MenuOptionClicked(menuEntry);
  }
}
