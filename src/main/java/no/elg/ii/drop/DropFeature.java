package no.elg.ii.drop;

import static no.elg.ii.InventoryState.INVALID_ITEM_ID;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;
import no.elg.ii.Feature;
import no.elg.ii.InstantInventoryConfig;
import no.elg.ii.InstantInventoryPlugin;
import no.elg.ii.InventoryState;

@Singleton
public class DropFeature implements Feature {

  public static final String DROP_OPTION = "Drop";
  private final InventoryState state = new InventoryState();

  @Inject
  private InstantInventoryConfig config;

  @Inject
  private InstantInventoryPlugin plugin;

  /* (non-javadoc)
   * Make sure the item in the slot is hidden, the client sets it as non-hidden each tick (?)
   *  or so. This must be done before the client is rendered otherwise (such as if we were to use
   *  the ClientTick event) the item would be visible for a single frame.
   */
  @Subscribe
  public void onBeforeRender(BeforeRender beforeRender) {
    if (config.instantDrop()) {
      Widget[] inventoryWidgetItem = plugin.inventoryItems();
      for (int index = 0; index < inventoryWidgetItem.length; index++) {
        int hideIndex = state.getItemId(index);
        if (hideIndex == INVALID_ITEM_ID) {
          continue;
        }
        Widget widget = inventoryWidgetItem[index];
        if (!widget.isSelfHidden()) {
          widget.setHidden(true);
        }
      }
    }
  }


  @Subscribe
  public void onMenuOptionClicked(final MenuOptionClicked event) {
    Widget widget = event.getWidget();
    if (widget != null) {
      String menuOption = event.getMenuOption();
      if (config.instantDrop() && DROP_OPTION.equals(menuOption)) {
        state.setItemId(widget.getIndex(), event.getItemId());
      }
    }
  }

  @Nonnull
  @Override
  public InventoryState getState() {
    return state;
  }
}
