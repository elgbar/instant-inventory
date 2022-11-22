package no.elg.ii;


import javax.annotation.Nonnull;

public interface InstantInventoryComponent {

  @Nonnull
  InventoryState getState();

  default void startUp() {
  }

  default void shutDown() {
  }

  default void reset() {
    getState().resetAll();
  }
}
