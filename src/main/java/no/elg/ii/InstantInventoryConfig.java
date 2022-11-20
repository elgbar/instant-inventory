package no.elg.ii;

import static no.elg.ii.InstantInventoryConfig.GROUP;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(GROUP)
public interface InstantInventoryConfig extends Config {
  String GROUP = "instant-inventory";
  @ConfigItem(
      keyName = "instantDrop",
      name = "Drop",
      description = "Hide dropped items from the inventory instantly"
  )
  default boolean instantDrop() {
    return true;
  }

	@ConfigItem(
			keyName = "instantClean",
			name = "Clean Herb",
			description = "Show the clean herb instantly"
	)
	default boolean instantClean()
	{
		return true;
	}
}
