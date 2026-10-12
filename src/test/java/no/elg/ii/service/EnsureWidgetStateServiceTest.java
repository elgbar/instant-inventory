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
import static net.runelite.api.gameval.ItemID.PLANTPOT_EMPTY;
import static no.elg.ii.util.WidgetUtils.FULLY_TRANSPARENT;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.stream.Stream;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.widgets.Widget;
import no.elg.ii.inventory.slot.SlotChange;
import no.elg.ii.model.IndexedWidget;
import no.elg.ii.test.TestSetup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class EnsureWidgetStateServiceTest {

  private static final int INDEX = 3;
  private static final int FILL_OPACITY = 63;

  @Mock
  private InventoryService inventoryService;
  @Mock
  private WidgetService widgetService;
  @Mock
  private Widget widget;

  private EnsureWidgetStateService service;

  @Before
  public void setUp() {
    service = new EnsureWidgetStateService();
    service.inventoryService = inventoryService;
    service.widgetService = widgetService;
    service.state = TestSetup.newInventoryState(TestSetup.mockClient());
  }

  @Test
  public void onBeforeRender_filling_slot_drawn_empty_by_the_client_is_redrawn() {
    // A withdrawal clicked just before the tick ended, so the server handles it a tick later
    service.state.setSlot(INDEX, PLANTPOT_EMPTY, 1, SlotChange.FILL);
    showInInventory();
    doReturn(FILL_OPACITY).when(widgetService).getOpacity(SlotChange.FILL);
    // The inventory update for an earlier withdrawal redraws every slot, and an empty slot gets the blank item
    doReturn(BLANKOBJECT).when(widget).getItemId();

    service.onBeforeRender(new BeforeRender());

    verify(widget).setItemId(PLANTPOT_EMPTY);
    verify(widget).setItemQuantity(1);
    verify(widget, atLeastOnce()).setHidden(false);
    verify(widget, never()).setHidden(true);
    verify(widget).setOpacity(FILL_OPACITY);
  }

  @Test
  public void onBeforeRender_filling_slot_already_drawn_is_left_alone() {
    service.state.setSlot(INDEX, PLANTPOT_EMPTY, 1, SlotChange.FILL);
    showInInventory();
    doReturn(FILL_OPACITY).when(widgetService).getOpacity(SlotChange.FILL);
    doReturn(PLANTPOT_EMPTY).when(widget).getItemId();
    doReturn(1).when(widget).getItemQuantity();
    doReturn(FILL_OPACITY).when(widget).getOpacity();

    service.onBeforeRender(new BeforeRender());

    verify(widget, never()).setItemId(anyInt());
    verify(widget, never()).setItemQuantity(anyInt());
    verify(widget, never()).setOpacity(anyInt());
  }

  @Test
  public void onBeforeRender_hidden_slot_drawn_empty_by_the_client_stays_empty() {
    // A dropped or deposited item
    service.state.setSlot(INDEX, PLANTPOT_EMPTY, 1, SlotChange.EMPTY);
    showInInventory();
    // The server removed the item and the client drew the slot as a fully transparent blank item. The name of the old
    // item may still be there
    doReturn("<col=ff9040>Empty plant pot</col>").when(widget).getName();
    doReturn(FULLY_TRANSPARENT).when(widget).getOpacity();

    service.onBeforeRender(new BeforeRender());

    verifyNoInteractions(widgetService);
  }

  private void showInInventory() {
    doAnswer(invocation -> Stream.of(new IndexedWidget(INDEX, widget))).when(inventoryService).getAllOpenInventoryWidgets();
  }
}
