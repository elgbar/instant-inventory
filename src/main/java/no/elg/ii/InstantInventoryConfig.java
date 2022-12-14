/*
 * Copyright (c) 2022 Elg
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package no.elg.ii;

import static no.elg.ii.InstantInventoryConfig.GROUP;
import static no.elg.ii.clean.CleanHerbFeature.CLEAN_CONFIG_KEY;
import static no.elg.ii.drop.DropFeature.DROP_CONFIG_KEY;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(GROUP)
public interface InstantInventoryConfig extends Config {

  String GROUP = "instant-inventory";

  @ConfigSection(
      name = "Features",
      description = "Toggle different features of the plugin",
      position = 0
  )
  String FEATURE_SECTION = "instant-inventory-features";

  @ConfigItem(
      keyName = DROP_CONFIG_KEY,
      name = "Drop Instantly",
      section = FEATURE_SECTION,
      description = "Drop items from the inventory instantly",
      position = 0
  )
  default boolean instantDrop() {
    return true;
  }

  @ConfigItem(
      keyName = CLEAN_CONFIG_KEY,
      section = FEATURE_SECTION,
      name = "Clean Herbs Instantly",
      description = "Show the cleaned herb instantly",
      position = 1
  )
  default boolean instantClean() {
    return true;
  }

  @ConfigItem(
      keyName = "maxUnmodifiedTicks",
      name = "Max Unmodified Ticks",
      description =
          "How many game ticks (0.6s) an item should be displayed as something else before being reverted back."
              + "<p>"
              + "<p>If this is zero the item will always flicker back into existence,"
              + "<p>when this is 1 items will occasionally flicker back into view when the servers are unstable,"
              + "<p>and when this is 2+ flickering rarely happens (i.e., only when the server lags)."
              + "<p>"
              + "<p>When in PvP or Bossing it is recommended to set this to 1.",
      position = 100
  )
  default int maxUnmodifiedTicks() {
    return InventoryState.MAX_UNMODIFIED_TICKS;
  }
}
