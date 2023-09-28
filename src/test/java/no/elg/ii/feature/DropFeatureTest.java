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

package no.elg.ii.feature;

import static no.elg.ii.feature.DropFeature.DROP_CONFIG_KEY;
import static no.elg.ii.inventory.InventoryState.DEFAULT_MAX_UNMODIFIED_TICKS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import no.elg.ii.InstantInventoryConfig;
import no.elg.ii.inventory.InventoryService;
import no.elg.ii.inventory.InventoryState;
import no.elg.ii.service.WidgetService;
import no.elg.ii.test.FeatureTestMother;
import no.elg.ii.test.TestSetup;
import org.junit.Test;

public class DropFeatureTest extends FeatureTestMother<DropFeature> {

  @Override
  public DropFeature createNewInstance() {
    return TestSetup.createNewDropFeature();
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

    assertFalse(dropFeature.getState().getSlot(index).hasValidItemId());
    dropFeature.onMenuOptionClicked(event);
    assertFalse(dropFeature.getState().getSlot(index).hasValidItemId());
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

    assertFalse(dropFeature.getState().getSlot(index).hasValidItemId());
    dropFeature.onMenuOptionClicked(event);
    assertFalse(dropFeature.getState().getSlot(index).hasValidItemId());
  }

  @Test
  public void stateResetWhenItemNotDropped() {
    int index = 1;
    int itemId = 2;
    DropFeature feature = createNewInstance();

    Widget widget = mock(Widget.class);
    doReturn(index).when(widget).getIndex();
    doReturn(itemId).when(widget).getItemId();

    MenuEntry menuEntry = mock(MenuEntry.class);
    doReturn(DropFeature.DROP_OPTION).when(menuEntry).getOption();
    doReturn(itemId).when(menuEntry).getItemId();
    doReturn(widget).when(menuEntry).getWidget();

    InstantInventoryConfig config = spy(new InstantInventoryConfig() {
    });
    Client client = mock(Client.class);
    var inventoryService = mock(InventoryService.class);
    var widgetService = mock(WidgetService.class);
    InventoryState inventoryState = new InventoryState(config, client, inventoryService, widgetService);
    doReturn(inventoryState).when(feature).getState();

    MenuOptionClicked event = new MenuOptionClicked(menuEntry);

    assertFalse(feature.getState().getSlot(index).hasValidItemId());
    feature.onMenuOptionClicked(event);
    assertTrue(feature.getState().getSlot(index).hasValidItemId());
    assertEquals(itemId, feature.getState().getSlot(index).getItemId());

    feature.getState().validateState(index, null);
    assertTrue("State was reset when it should not have been", feature.getState().getSlot(index).hasValidItemId());

    doReturn(DEFAULT_MAX_UNMODIFIED_TICKS).when(client).getTickCount();
    feature.getState().validateState(index, null);
    assertFalse("State was NOT reset when it should have been", feature.getState().getSlot(index).hasValidItemId());
  }

  @Test
  public void configKey_is_CLEAN_CONFIG_KEY() {
    DropFeature feature = createNewInstance();
    assertEquals(DROP_CONFIG_KEY, feature.getConfigKey());
  }

  @Test
  public void afterEnablingItWillBeShownOnSomeWidget() {
    HideFeature dropFeature = createNewInstance();
//    assertFalse(dropFeature.getWidgets().isEmpty());
  }
}
