package no.elg.ii.drop;

import static no.elg.ii.InventoryState.INVALID_ITEM_ID;
import static no.elg.ii.InventoryState.MAX_UNMODIFIED_TICKS;
import static no.elg.ii.drop.DropFeature.DROP_CONFIG_KEY;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import net.runelite.api.MenuEntry;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import no.elg.ii.InstantInventoryPlugin;
import no.elg.ii.InventoryState;
import no.elg.ii.test.FeatureTestMother;
import no.elg.ii.test.TestSetup;
import org.junit.Test;

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
  public void name() {
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
    DropFeature dropFeature = createNewInstance();

    Widget widget = mock(Widget.class);
    doReturn(index).when(widget).getIndex();

    MenuEntry menuEntry = mock(MenuEntry.class);
    doReturn(DropFeature.DROP_OPTION).when(menuEntry).getOption();
    doReturn(itemId).when(menuEntry).getItemId();
    doReturn(widget).when(menuEntry).getWidget();

    MenuOptionClicked event = new MenuOptionClicked(menuEntry);

    assertEquals(0, InstantInventoryPlugin.tickCounter.get());
    assertEquals(INVALID_ITEM_ID, dropFeature.getState().getItemId(index));
    dropFeature.onMenuOptionClicked(event);
    assertEquals(itemId, dropFeature.getState().getItemId(index));

    dropFeature.getState().validateState(index, itemId);
    assertEquals("State was reset when it should not have been", itemId,
        dropFeature.getState().getItemId(index));

    InstantInventoryPlugin.tickCounter.set(MAX_UNMODIFIED_TICKS);
    dropFeature.getState().validateState(index, itemId);
    assertEquals("State was NOT reset when it should have been", INVALID_ITEM_ID,
        dropFeature.getState().getItemId(index));
  }

  @Test
  public void configKey_is_CLEAN_CONFIG_KEY() {
    DropFeature feature = createNewInstance();
    assertEquals(DROP_CONFIG_KEY, feature.getConfigKey());
  }

  @Test
  public void testUpdateHiddenStatus() {
    testUpdateHiddenStatus("true,true", true, true);
    testUpdateHiddenStatus("true,false", true, false);
    testUpdateHiddenStatus("false,true", false, true);
    testUpdateHiddenStatus("false,false", false, false);
  }

  private void testUpdateHiddenStatus(String name, boolean shouldBeHidden, boolean isSelfHidden) {
    int index = 0;
    DropFeature dropFeature = createNewInstance();

    InstantInventoryPlugin plugin = dropFeature.plugin;
    Widget widget = mock(Widget.class);
    Widget[] widgets = {widget};
    doReturn(widgets).when(plugin).inventoryItems();

    InventoryState state = dropFeature.getState();
    state.setItemId(index, shouldBeHidden ? 1 : INVALID_ITEM_ID);

    assertEquals(name, shouldBeHidden, state.isValid(index));
    doReturn(isSelfHidden).when(widget).isSelfHidden();

    dropFeature.updateHiddenStatus();

    System.out.println(name);
    verify(widget, times(shouldBeHidden == isSelfHidden ? 0 : 1)).setHidden(shouldBeHidden);
  }

}
