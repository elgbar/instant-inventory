package no.elg;

import com.google.inject.Provides;
import java.awt.Menu;
import java.util.Arrays;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j

@PluginDescriptor(
    name = "Instant Inventory",
    description = "Improve the experience with using the inventory by predicting what an action will do client side.",
    tags = {"inventory", "drop", "improvement", "overlay"}
)
public class InstantInventoryPlugin extends Plugin {

  @Inject
  private Client client;

  @Inject
  private InstantInventoryConfig config;

  @Inject
  private InstantInventoryOverlay overlay;

  @Inject
  private OverlayManager overlayManager;


  @Provides
  InstantInventoryConfig provideConfig(ConfigManager configManager) {
    return configManager.getConfig(InstantInventoryConfig.class);
  }

  @Override
  protected void startUp() {
    overlayManager.add(overlay);
    System.out.println("Start up, adding overlay");
    Arrays.fill(hide,-1);
  }

  @Override
  protected void shutDown() {
    overlayManager.remove(overlay);
  }

  public int[] hide = new int[28];

  @Subscribe
  public void onMenuOptionClicked(final MenuOptionClicked event) {
    if (config.enableDrop() && "Drop".equals(event.getMenuOption())) {
      Widget widget = event.getWidget();
      if (widget != null) {
        System.out.println("hide "+widget.getIndex()+"(item id) "+event.getItemId());
        hide[widget.getIndex()] = event.getItemId();
      }
    }
  }

//  @Subscribe
//  public void onGameTick(final GameTick event) {
//    Arrays.fill(hide, false);
//
//  }
}
