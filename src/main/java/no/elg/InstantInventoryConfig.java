package no.elg;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("inventory")
public interface InstantInventoryConfig extends Config
{
	@ConfigItem(
		keyName = "enableDrop",
		name = "Enable Instant Drop",
		description = "En"
	)
	default boolean enableDrop()
	{
		return true;
	}
}
