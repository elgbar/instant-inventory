package no.elg;

import com.google.inject.Provides;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j

@PluginDescriptor(
    name = "Instant Inventory",
    description = "Improve the experience with using the inventory by predicting what an action will do client side.",
    tags = {"inventory", "drop", "improvement", "overlay"}
)
public class InstantInventoryPlugin extends Plugin {

  public static final int INVENTORY_SIZE = 28;
  public static final int INVALID_ITEM_ID = -1;

  private static final Widget[] EMPTY_WIDGET = new Widget[0];
  public int[] hide = new int[INVENTORY_SIZE];

  @Inject
  private Client client;

  @Override
  protected void startUp() {
    Arrays.fill(hide, INVALID_ITEM_ID);
  }


  @Subscribe
  public void onMenuOptionClicked(final MenuOptionClicked event) {
    if ("Drop".equals(event.getMenuOption())) {
      Widget widget = event.getWidget();
      if (widget != null) {
        hide[widget.getIndex()] = event.getItemId();
      }
    }
  }

  /* (non-javadoc)
   * Make sure the item in the slot is hidden, the client sets it as non-hidden each tick (?)
   *  or so. This must be done before the client is rendered otherwise (such as if we were to use
   *  the ClientTick event) the item would be visible for a single frame.
   */
  @Subscribe
  public void onBeforeRender(BeforeRender beforeRender){
    Widget[] inventoryWidgetItem = inventoryItems();
    for (int index = 0; index < inventoryWidgetItem.length; index++) {
      int hideIndex = hide[index];
      if (hideIndex == INVALID_ITEM_ID) {
        continue;
      }
      Widget widget = inventoryWidgetItem[index];
      if(!widget.isSelfHidden()){
        widget.setHidden(true);
      }
    }
  }

  /* (non-javadoc)
   * When a dropped item is no longer in the inventory, unmark it as hidden
   */
  @Subscribe
  public void onGameTick(GameTick tick) {
    Widget[] inventoryWidgets = inventoryItems();
    for (int index = 0; index < inventoryWidgets.length; index++) {
      int hideIndex = hide[index];
      if (hideIndex != INVALID_ITEM_ID && hideIndex != inventoryWidgets[index].getItemId()) {
        hide[index] = INVALID_ITEM_ID;
      }
    }
  }

  /**
   *
   * @return An array of items in the players inventory, or an empty inventory if there is no inventory widget
   */
  @Nonnull
  private Widget[] inventoryItems() {
    Widget inventory = client.getWidget(WidgetInfo.INVENTORY);
    if (inventory != null) {
      return inventory.getDynamicChildren();
    }
    return EMPTY_WIDGET;
  }

  @Provides
  InstantInventoryConfig provideConfig(ConfigManager configManager) {
    return configManager.getConfig(InstantInventoryConfig.class);
  }

}
