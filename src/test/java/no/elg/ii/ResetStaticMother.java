package no.elg.ii;

import org.junit.Before;

public class ResetStaticMother {

  @Before
  public void resetTickCounter() {
    InstantInventoryPlugin.tickCounter.set(0);
  }
}
