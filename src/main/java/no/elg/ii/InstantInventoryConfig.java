/*
 * Copyright (c) 2022-2025 Elg
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package no.elg.ii;

import static no.elg.ii.InstantInventoryConfig.GROUP;
import static no.elg.ii.feature.features.CleanHerbFeature.CLEAN_CONFIG_KEY;
import static no.elg.ii.feature.features.DepositFeature.DEPOSIT_CONFIG_KEY;
import static no.elg.ii.feature.features.DropFeature.DROP_CONFIG_KEY;
import static no.elg.ii.feature.features.EquipFeature.EQUIP_CONFIG_KEY;
import static no.elg.ii.feature.features.PrayerFeature.PRAYER_CONFIG_KEY;
import static no.elg.ii.feature.features.WithdrawFeature.WITHDRAW_CONFIG_KEY;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;
import no.elg.ii.feature.state.InventoryState;
import no.elg.ii.util.Util;

@ConfigGroup(GROUP)
public interface InstantInventoryConfig extends Config {

  String GROUP = "instant-inventory";

  @ConfigSection(
    name = "Features",
    description = "Toggle different features of the plugin",
    position = 10
  )
  String FEATURE_SECTION = "instant-inventory-features";

  @ConfigSection(
    name = "Appearance",
    description = "How predicted widget will look",
    position = 0
  )
  String APPEARANCE_SECTION = "instant-inventory-appearance";

  @ConfigSection(
    name = "Advanced",
    description = "Advanced settings for all features",
    position = 20,
    closedByDefault = true
  )
  String ADVANCED_SECTION = "instant-inventory-advanced";

  /// ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @ConfigItem(
    keyName = DROP_CONFIG_KEY,
    name = "Drop Items Instantly",
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
    keyName = DEPOSIT_CONFIG_KEY,
    section = FEATURE_SECTION,
    name = "Deposit Items Instantly",
    description = "Deposit items into your bank instantly."
      + "<p>"
      + "<p>Note that how many items that can be deposited at a time is limited by how osrs works. It appears"
      + "<p>that only four items can be deposited in a single game tick, so if you deposit more than four items at "
      + "<p>once, you have to wait for the next game tick before the rest of the items are deposited.",
    position = 2
  )
  default boolean instantDeposit() {
    return true;
  }

  @ConfigItem(
    keyName = EQUIP_CONFIG_KEY,
    section = FEATURE_SECTION,
    name = "Equip Items Instantly",
    description = "Equip wearable/wieldable items instantly",
    position = 3
  )
  default boolean instantEquip() {
    return true;
  }

  @ConfigItem(
    keyName = WITHDRAW_CONFIG_KEY,
    section = FEATURE_SECTION,
    name = "Withdraw Items Instantly",
    description = "Withdraw items from your bank instantly",
    position = 4
  )
  default boolean instantWithdraw() {
    return true;
  }

  @ConfigItem(
    keyName = PRAYER_CONFIG_KEY,
    section = FEATURE_SECTION,
    name = "Prayer Switch Instantly",
    description = "Switch prayers instantly",
    position = 5
  )
  default boolean instantPrayer() {
    return true;
  }


  /// ////////////////////////////////////////////////////////////////////////////////////////////////////////////


  @ConfigItem(
    section = APPEARANCE_SECTION,
    keyName = "hideOpacityPercent",
    name = "Hidden items opacity",
    description = "How transparent items are when removed from the inventory or bank" +
      "<p>A lower value will cause items to be more transparent",
    position = 10
  )
  @Range(max = 100)
  @Units(Units.PERCENT)
  default int hideOpacityPercent() {
    return 20; //% transparent
  }

  @ConfigItem(
    section = APPEARANCE_SECTION,
    keyName = "changeOpacityPercent",
    name = "Changed items opacity",
    description = "How transparent items are when replaced with another item." +
      "<p>A lower value will cause items to be more transparent",
    position = 20
  )
  @Range(max = 100)
  @Units(Units.PERCENT)
  default int changeOpacityPercent() {
    return 75; //% transparent
  }


  /// ////////////////////////////////////////////////////////////////////////////////////////////////////////////


  @ConfigItem(
    section = ADVANCED_SECTION,
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
    position = 10
  )
  @Units(Units.TICKS)
  default int maxUnmodifiedTicks() {
    return InventoryState.DEFAULT_MAX_UNMODIFIED_TICKS;
  }

  @ConfigItem(
    section = ADVANCED_SECTION,
    keyName = "minChangedMs",
    name = "Min Changed Millis",
    description =
      "How many milliseconds an action should minimum stay changed before it can be reverted back to what the server says it is."
        + "<p>The default value is half a game tick, i.e, 300ms."
        + "<p>This setting is affected by the ping and performance of the server. If you see items flicker back into existence, increase this value."
        + "<p>"
        + "<p>This overwrites the \"Max Unmodified Ticks\" setting for inventory actions.",
    position = 20
  )
  @Units(Units.MILLISECONDS)
  default int minChangedMs() {
    return Util.TICK_LENGTH_MS / 2;
  }

}
