package no.elg.ii;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("instant-inventory")
public interface InstantInventoryConfig extends Config {

  @ConfigItem(
      keyName = "instantDrop",
      name = "Instant Drop",
      description = "Hide dropped items client-side"
  )
  default boolean instantDrop() {
    return true;
  }

//	@ConfigItem(
//			keyName = "instantBank",
//			name = "Instant Bank",
//			description = "Move items in and out the back (seemingly) quicker"
//	)
//	default boolean instantDeposit()
//	{
//		return true;
//	}
}
