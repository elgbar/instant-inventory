package no.elg.ii.clean;


import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Skill;
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
  @Inject
  private Client client;

  private final InventoryState state = new InventoryState();

  @Override
  public void onEnable() {
    overlayManager.add(overlay);
  }

  @Override
  public void onDisable() {
    overlayManager.remove(overlay);
    overlay.invalidateCache();
  }

  @Subscribe
  public void onMenuOptionClicked(final MenuOptionClicked event) {
    Widget widget = event.getWidget();
    if (widget != null) {
      String menuOption = event.getMenuOption();
      if (config.instantDrop() && CLEAN_OPTION.equals(menuOption)) {
        int itemId = event.getItemId();
        HerbInfo herbInfo = HerbInfo.HERBS.get(itemId);
        if (herbInfo == null) {
          return;
        }

        //TODO test with spicy stew (brown) by reducing the boosted level
        int herbloreLevel = client.getBoostedSkillLevel(Skill.HERBLORE);
        if (herbloreLevel >= herbInfo.getMinLevel()) {
          state.setItemId(widget.getIndex(), itemId);
        }
      }
    }
  }

  @Override
  @Nonnull
  public InventoryState getState() {
    return state;
  }
}
