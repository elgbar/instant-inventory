package no.elg.ii;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class LivePluginTest {

  public static void main(String[] args) throws Exception {
    //noinspection unchecked
    ExternalPluginManager.loadBuiltin(InstantInventoryPlugin.class);
    RuneLite.main(args);
  }
}