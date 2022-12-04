package no.elg.ii;

import static no.elg.ii.InstantInventoryPlugin.EMPTY_WIDGET;
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
import no.elg.ii.clean.CleanHerbFeature;
import no.elg.ii.drop.DropFeature;
import no.elg.ii.test.TestSetup;
import org.junit.Before;
import org.junit.Test;

public class InstantInventoryPluginTest {

  private InstantInventoryPlugin plugin;
  private DropFeature dropFeature;
  private CleanHerbFeature cleanHerbFeature;
  private InstantInventoryConfig instantInventoryConfig;
  private EventBus eventBus;
  private Client client;

  @Before
  public void setUp() {
    plugin = spy(new InstantInventoryPlugin());
    doReturn(EMPTY_WIDGET).when(plugin).inventoryItems(any());
    plugin.eventBus = eventBus = mock(EventBus.class);
    plugin.client = client = mock(Client.class);
    instantInventoryConfig = spy(new InstantInventoryConfig() {
    });
    doReturn(true).when(instantInventoryConfig).instantDrop();
    doReturn(true).when(instantInventoryConfig).instantClean();
    plugin.config = instantInventoryConfig;

    plugin.dropFeature = dropFeature = TestSetup.createNewDropFeature();
    dropFeature.plugin = plugin;

    plugin.cleanHerbFeature = cleanHerbFeature = TestSetup.createNewCleanHerbFeature();

    Client client = mock(Client.class);
    InventoryState inventoryState = new InventoryState(instantInventoryConfig, client);
    doReturn(inventoryState).when(dropFeature).getState();
    doReturn(inventoryState).when(cleanHerbFeature).getState();
  }

  @Test
  public void startUp_calls_updateAllFeatures() {
    plugin.startUp();
    verify(plugin).updateAllFeatureStatus();
    assertEquals(2, plugin.features.size());
    verify(dropFeature).onEnable();
    verify(cleanHerbFeature).onEnable();
  }

  @Test
  public void shutDown_disables_all_features() {
    plugin.startUp();
    assertFalse(plugin.features.isEmpty());

    plugin.shutDown();
    assertTrue(plugin.features.isEmpty());
    verify(dropFeature).onDisable();
    verify(cleanHerbFeature).onDisable();
  }

  @Test
  public void updateFeatureStatus() {
    plugin.updateFeatureStatus(dropFeature, false);
    verify(dropFeature, never()).onEnable();
    plugin.updateFeatureStatus(dropFeature, false);
    verify(dropFeature, never()).onEnable();
    plugin.updateFeatureStatus(dropFeature, true);
    verify(dropFeature).onEnable();
    plugin.updateFeatureStatus(dropFeature, true);
    verify(dropFeature).onEnable();

    verify(dropFeature, never()).onDisable();
    plugin.updateFeatureStatus(dropFeature, false);
    verify(dropFeature).onDisable();
    plugin.updateFeatureStatus(dropFeature, false);
    verify(dropFeature).onDisable();
  }

  @Test
  public void enableFeature() {
    plugin.enableFeature(dropFeature);

    verify(eventBus).register(dropFeature);
    verify(dropFeature).onEnable();
    verify(dropFeature).reset();
    assertEquals(1, plugin.features.size());
    assertTrue(plugin.features.contains(dropFeature));
  }

  @Test
  public void disableFeature() {
    plugin.enableFeature(dropFeature);
    plugin.disableFeature(dropFeature);

    verify(eventBus).unregister(dropFeature);
    verify(dropFeature).onDisable();
    //Once to enable, once to disable
    verify(dropFeature, times(2)).reset();
    assertTrue(plugin.features.isEmpty());
  }

  @Test
  public void onGameStateChanged_calls_nothing_on_incorrect_group() {
    plugin.startUp();
    assertEquals(2, plugin.features.size());
    plugin.onGameStateChanged(new GameStateChanged());

    verify(dropFeature, times(2)).reset();
    verify(cleanHerbFeature, times(2)).reset();
  }

  @Test
  public void onConfigChanged_calls_updateAllFeatureStatus_on_correct_group() {
    ConfigChanged configChanged = new ConfigChanged();
    configChanged.setGroup(InstantInventoryConfig.GROUP);
    plugin.onConfigChanged(configChanged);
    verify(plugin).updateAllFeatureStatus();
  }

  @Test
  public void onConfigChanged_calls_nothing_on_incorrect_group() {
    ConfigChanged configChanged = new ConfigChanged();
    configChanged.setGroup("");
    plugin.onConfigChanged(configChanged);
    verify(plugin, never()).updateAllFeatureStatus();
  }

  @Test
  public void inventoryItems_returns_widgets_list_on_inventory_exists() {
    Widget inventoryWidget = mock(Widget.class);
    Widget[] widgets = {mock(Widget.class)};
    doReturn(widgets).when(inventoryWidget).getDynamicChildren();
    doReturn(inventoryWidget).when(client).getWidget(WidgetInfo.INVENTORY);
    doCallRealMethod().when(plugin).inventoryItems(any());

    assertSame(widgets, plugin.inventoryItems(any()));
  }

  @Test
  public void inventoryItems_returns_empty_list_on_no_inventory() {
    doReturn(null).when(client).getWidget(any());
    doCallRealMethod().when(plugin).inventoryItems(any());
    assertSame(EMPTY_WIDGET, plugin.inventoryItems(any()));
  }

  @Test
  public void onGameTick_validatesEachItem() {
    Widget widget = mock(Widget.class);
    Widget[] widgets = {widget, widget, widget};
    doReturn(widgets).when(plugin).inventoryItems(any());

    InventoryState mockedState = mock(InventoryState.class);
    Feature a = mock(Feature.class);
    Feature b = mock(Feature.class);

    doReturn(mockedState).when(a).getState();
    doReturn(mockedState).when(b).getState();

    plugin.enableFeature(a);
    plugin.enableFeature(b);

    plugin.onGameTick(new GameTick());

    assertEquals(3, widgets.length);
    assertEquals(2, plugin.features.size());

    verify(mockedState, times(widgets.length * plugin.features.size())).validateState(anyInt(),
        anyInt());

  }
}