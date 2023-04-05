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

package no.elg.ii.feature.hide;

import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.*;
import no.elg.ii.*;
import no.elg.ii.test.*;
import org.junit.*;

import static no.elg.ii.InventoryState.INVALID_ITEM_ID;
import static no.elg.ii.InventoryState.MAX_UNMODIFIED_TICKS;
import static no.elg.ii.feature.hide.DropFeature.DROP_CONFIG_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class DropFeatureTest extends FeatureTestMother<DropFeature> {

  @Override
  public DropFeature createNewInstance() {
    return TestSetup.createNewDropFeature();
  }

  @Test
  public void onBeforeRender_calls_updatesHiddenStatus() {
    DropFeature dropFeature = createNewInstance();
    doNothing().when(dropFeature).updateHiddenStatus();

    dropFeature.onBeforeRender(mock(BeforeRender.class));

    verify(dropFeature).updateHiddenStatus();
  }

  @Test
  public void reset_calls_updatesHiddenStatus() {
    DropFeature dropFeature = createNewInstance();
    doNothing().when(dropFeature).updateHiddenStatus();

    dropFeature.reset();

    verify(dropFeature).updateHiddenStatus();
  }

  @Test
  public void onMenuOptionClicked_doNothingWhenWidgetIsNull() {
    int index = 1;
    int itemId = 2;
    DropFeature dropFeature = createNewInstance();

    MenuEntry menuEntry = mock(MenuEntry.class);
    doReturn(DropFeature.DROP_OPTION).when(menuEntry).getOption();
    doReturn(itemId).when(menuEntry).getItemId();
    doReturn(null).when(menuEntry).getWidget();

    MenuOptionClicked event = new MenuOptionClicked(menuEntry);

    assertEquals(INVALID_ITEM_ID, dropFeature.getState().getItemId(index));
    dropFeature.onMenuOptionClicked(event);
    assertEquals(INVALID_ITEM_ID, dropFeature.getState().getItemId(index));
  }

  @Test
  public void onMenuOptionClicked_different_menuEntry_clicked_does_not_update_state() {
    int index = 1;
    int itemId = 2;
    DropFeature dropFeature = createNewInstance();

    Widget widget = mock(Widget.class);
    doReturn(index).when(widget).getIndex();

    MenuEntry menuEntry = mock(MenuEntry.class);
    doReturn("not drop").when(menuEntry).getOption();
    doReturn(itemId).when(menuEntry).getItemId();
    doReturn(widget).when(menuEntry).getWidget();

    MenuOptionClicked event = new MenuOptionClicked(menuEntry);

    assertEquals(INVALID_ITEM_ID, dropFeature.getState().getItemId(index));
    dropFeature.onMenuOptionClicked(event);
    assertEquals(INVALID_ITEM_ID, dropFeature.getState().getItemId(index));
  }

  @Test
  public void stateResetWhenItemNotDropped() {
    int index = 1;
    int itemId = 2;
    DropFeature feature = createNewInstance();

    Widget widget = mock(Widget.class);
    doReturn(index).when(widget).getIndex();

    MenuEntry menuEntry = mock(MenuEntry.class);
    doReturn(DropFeature.DROP_OPTION).when(menuEntry).getOption();
    doReturn(itemId).when(menuEntry).getItemId();
    doReturn(widget).when(menuEntry).getWidget();

    InstantInventoryConfig config = spy(new InstantInventoryConfig() {
    });
    Client client = mock(Client.class);
    InventoryState inventoryState = new InventoryState(config, client);
    doReturn(inventoryState).when(feature).getState();

    MenuOptionClicked event = new MenuOptionClicked(menuEntry);

    assertEquals(INVALID_ITEM_ID, feature.getState().getItemId(index));
    feature.onMenuOptionClicked(event);
    assertEquals(itemId, feature.getState().getItemId(index));

    feature.getState().validateState(index, itemId);
    assertEquals("State was reset when it should not have been", itemId,
      feature.getState().getItemId(index));

    doReturn(MAX_UNMODIFIED_TICKS).when(client).getTickCount();
    feature.getState().validateState(index, itemId);
    assertEquals("State was NOT reset when it should have been", INVALID_ITEM_ID,
      feature.getState().getItemId(index));
  }

  @Test
  public void configKey_is_CLEAN_CONFIG_KEY() {
    DropFeature feature = createNewInstance();
    assertEquals(DROP_CONFIG_KEY, feature.getConfigKey());
  }

  @Test
  public void afterEnablingItWillBeShownOnSomeWidget() {
    HideFeature dropFeature = createNewInstance();
    assertFalse(dropFeature.getWidgets().isEmpty());
  }

  @Test
  public void testUpdateHiddenStatus() {
    testUpdateHiddenStatus("true,true", true, true);
    testUpdateHiddenStatus("true,false", true, false);
    testUpdateHiddenStatus("false,true", false, true);
    testUpdateHiddenStatus("false,false", false, false);
  }

  private void testUpdateHiddenStatus(String name, boolean shouldBeHidden, boolean isSelfHidden) {
    System.out.println(name);
    int index = 0;
    DropFeature dropFeature = createNewInstance();

    InstantInventoryPlugin plugin = dropFeature.plugin;
    Widget widget = mock(Widget.class);
    Widget[] widgets = {widget};
    doReturn(widgets).when(plugin).inventoryItems(any());

    InventoryState state = dropFeature.getState();
    state.setItemId(index, shouldBeHidden ? 1 : INVALID_ITEM_ID);

    assertEquals(name, shouldBeHidden, state.isValid(index));
    doReturn(isSelfHidden).when(widget).isSelfHidden();

    dropFeature.updateHiddenStatus();

    verify(widget, times(shouldBeHidden == isSelfHidden ? 0 : 1)).setHidden(shouldBeHidden);
  }

}
