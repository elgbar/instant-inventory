package no.elg.ii.clean;


import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.OverlayManager;
import no.elg.ii.InstantInventoryComponent;
import no.elg.ii.InstantInventoryConfig;
import no.elg.ii.InventoryState;

@Singleton
public class CleanHerbComponent implements InstantInventoryComponent {

  public static final String CLEAN_OPTION = "Clean";

  @Inject
  private CleanHerbOverlay overlay;
  @Inject
  private OverlayManager overlayManager;
  @Inject
  private InstantInventoryConfig config;

  private final InventoryState state = new InventoryState();

  @Override
  public void startUp() {
    overlayManager.add(overlay);
  }

  @Override
  public void shutDown() {
    overlayManager.remove(overlay);
  }

  @Subscribe
  public void onMenuOptionClicked(final MenuOptionClicked event) {
    Widget widget = event.getWidget();
    if (widget != null) {
      String menuOption = event.getMenuOption();
      if (config.instantDrop() && CLEAN_OPTION.equals(menuOption)) {
        state.setItemId(widget.getIndex(), event.getItemId());
      }
    }
  }

  @Override
  @Nonnull
  public InventoryState getState() {
    return state;
  }
}
