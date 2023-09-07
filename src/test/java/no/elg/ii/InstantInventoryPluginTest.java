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

package no.elg.ii;

import static no.elg.ii.InstantInventoryPlugin.EMPTY_WIDGET;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import net.runelite.api.Client;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.events.ConfigChanged;
import no.elg.ii.feature.Feature;
import no.elg.ii.feature.clean.CleanHerbFeature;
import no.elg.ii.feature.hide.DepositFeature;
import no.elg.ii.feature.hide.DropFeature;
import no.elg.ii.inventory.InventoryState;
import no.elg.ii.test.IntegrationTestHelper;
import no.elg.ii.test.TestSetup;
import org.junit.Before;
import org.junit.Test;

public class InstantInventoryPluginTest extends IntegrationTestHelper {

  @Test
  public void startUp_calls_updateAllFeatures() {
    plugin.startUp();
    verify(featureManager).updateAllFeatureStatus();
  }

  @Test
  public void shutDown_disables_all_features() {
    plugin.shutDown();
    verify(featureManager).disableAllFeatures();
  }

  @Test
  public void onGameStateChanged_calls_nothing_on_incorrect_group() {
    plugin.startUp();
    assertEquals(3, featureManager.getActiveFeatures().size());
    plugin.onGameStateChanged(new GameStateChanged());

    verify(dropFeature, times(2)).reset();
    verify(cleanHerbFeature, times(2)).reset();
  }

  @Test
  public void onConfigChanged_calls_updateAllFeatureStatus_on_correct_group() {
    ConfigChanged configChanged = new ConfigChanged();
    configChanged.setGroup(InstantInventoryConfig.GROUP);
    plugin.onConfigChanged(configChanged);
    verify(featureManager).updateAllFeatureStatus();
  }

  @Test
  public void onConfigChanged_calls_nothing_on_incorrect_group() {
    ConfigChanged configChanged = new ConfigChanged();
    configChanged.setGroup("");
    plugin.onConfigChanged(configChanged);
    verify(featureManager, never()).updateAllFeatureStatus();
  }

  @Test
  public void inventoryItems_returns_widgets_list_on_inventory_exists() {
    Widget inventoryWidget = mock(Widget.class);
    Widget[] widgets = {mock(Widget.class)};
    doReturn(widgets).when(inventoryWidget).getDynamicChildren();
    doReturn(inventoryWidget).when(client).getWidget(WidgetInfo.INVENTORY);
    doCallRealMethod().when(plugin).inventoryItems(WidgetInfo.INVENTORY);

    assertArrayEquals(widgets, plugin.inventoryItems(WidgetInfo.INVENTORY));
    assertSame(widgets, plugin.inventoryItems(WidgetInfo.INVENTORY));
  }

  @Test
  public void inventoryItems_returns_empty_list_on_no_inventory() {
    doReturn(null).when(client).getWidget(any());
    doCallRealMethod().when(plugin).inventoryItems(any());
    assertSame(EMPTY_WIDGET, plugin.inventoryItems(any()));
  }
}
