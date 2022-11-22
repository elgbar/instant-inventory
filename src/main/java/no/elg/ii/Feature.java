package no.elg.ii;


import javax.annotation.Nonnull;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;

/**
 * A feature of the instant inventory plugin. Features have a {@link #getState()} and modifies
 * how the inventory is rendered based on that.
 */
public interface Feature {

  /**
   * @return The state of the feature
   */
  @Nonnull
  InventoryState getState();

  /**
   * Method run when this feature is loaded in, either on {@link Plugin#startUp()} or when
   * {@link ConfigChanged} and this feature is enabled in the config and not already loaded.
   */
  default void onEnable() {
  }


  /**
   * Method run when this feature is disabled, either on {@link Plugin#shutDown()} or when
   * {@link ConfigChanged} and this feature is disabled in the config and is loaded.
   */
  default void onDisable() {
  }

  /**
   * Reset the feature to its initial state.
   * <p>
   * In the default implementation {@link InventoryState#resetAll()} is called.
   */
  default void reset() {
    getState().resetAll();
  }
}
