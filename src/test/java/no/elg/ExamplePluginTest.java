package no.elg;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;
import no.elg.ii.InstantInventoryPlugin;

public class ExamplePluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(InstantInventoryPlugin.class);
		RuneLite.main(args);
	}
}