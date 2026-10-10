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

import static net.runelite.api.gameval.ItemID.PLANTPOT_EMPTY;
import static no.elg.ii.feature.features.WithdrawFeature.WITHDRAW_PREFIX_OPTION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import net.runelite.api.ItemComposition;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.game.ItemManager;
import no.elg.ii.inventory.slot.InventorySlot;
import no.elg.ii.service.VarService;
import no.elg.ii.service.WidgetService;
import no.elg.ii.test.StatefulFeatureTestMother;
import no.elg.ii.test.TestSetup;
import org.junit.Test;

public class WithdrawFeatureTest extends StatefulFeatureTestMother<WithdrawFeature> {

  private static final int INDEX = 5;
  private static final int BANK_QUANTITY = 137;
  private static final int CHANGE_OPACITY = 63;

  @Override
  public WithdrawFeature createNewInstance() {
    return TestSetup.createNewWithdrawFeature();
  }

  @Test
  public void onMenuOptionClicked_item_withdrawn_into_empty_slot_is_filling() {
    WithdrawFeature feature = createWiredInstance();
    Widget emptySlot = inventorySlot(feature);
    // A slot that has not held an item since the bank was opened has no name
    doReturn("").when(emptySlot).getName();

    feature.onMenuOptionClicked(withdrawOne(bankItem(feature, false)));

    InventorySlot slot = feature.getState().getSlot(INDEX);
    assertEquals(PLANTPOT_EMPTY, slot.getItemId());
    assertTrue(slot.isFilling());
  }

  @Test
  public void onMenuOptionClicked_stack_the_server_has_not_added_stays_filling() {
    WithdrawFeature feature = createWiredInstance();
    Widget stack = inventorySlot(feature);
    doReturn(PLANTPOT_EMPTY).when(stack).getItemId();
    // The withdrawal that started the stack is still on its way
    feature.getState().setSlot(INDEX, PLANTPOT_EMPTY, 1, CHANGE_OPACITY, true);

    feature.onMenuOptionClicked(withdrawOne(bankItem(feature, true)));

    assertTrue(feature.getState().getSlot(INDEX).isFilling());
  }

  @Test
  public void onMenuOptionClicked_stack_already_in_inventory_is_not_filling() {
    WithdrawFeature feature = createWiredInstance();
    Widget stack = inventorySlot(feature);
    doReturn(PLANTPOT_EMPTY).when(stack).getItemId();

    feature.onMenuOptionClicked(withdrawOne(bankItem(feature, true)));

    InventorySlot slot = feature.getState().getSlot(INDEX);
    assertEquals(PLANTPOT_EMPTY, slot.getItemId());
    assertFalse(slot.isFilling());
  }

  private WithdrawFeature createWiredInstance() {
    WithdrawFeature feature = createNewInstance();
    feature.client = TestSetup.mockClient();
    feature.itemManager = mock(ItemManager.class);
    feature.state = TestSetup.newInventoryState(feature.client);
    feature.widgetService = mock(WidgetService.class);
    feature.varService = mock(VarService.class);
    return feature;
  }

  /**
   * @return The only slot of the inventory shown next to the bank
   */
  private static Widget inventorySlot(WithdrawFeature feature) {
    Widget inventory = mock(Widget.class);
    Widget slot = mock(Widget.class);
    doReturn(inventory).when(feature.client).getWidget(InterfaceID.Bankside.ITEMS);
    doReturn(new Widget[]{slot}).when(inventory).getDynamicChildren();
    doReturn(INDEX).when(slot).getIndex();
    return slot;
  }

  private static Widget bankItem(WithdrawFeature feature, boolean stackable) {
    Widget bankItem = mock(Widget.class);
    doReturn(PLANTPOT_EMPTY).when(bankItem).getItemId();
    doReturn(BANK_QUANTITY).when(bankItem).getItemQuantity();
    ItemComposition composition = mock(ItemComposition.class);
    doReturn(composition).when(feature.itemManager).getItemComposition(PLANTPOT_EMPTY);
    if (stackable) {
      doReturn(true).when(composition).isStackable();
    }
    return bankItem;
  }

  private static MenuOptionClicked withdrawOne(Widget bankItem) {
    MenuEntry menuEntry = mock(MenuEntry.class);
    doReturn(bankItem).when(menuEntry).getWidget();
    doReturn(WITHDRAW_PREFIX_OPTION + 1).when(menuEntry).getOption();
    return new MenuOptionClicked(menuEntry);
  }
}
