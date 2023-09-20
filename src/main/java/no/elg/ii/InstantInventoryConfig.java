/*
 * Copyright (c) 2022-2023 Elg
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
import static no.elg.ii.feature.clean.CleanHerbFeature.CLEAN_CONFIG_KEY;
import static no.elg.ii.feature.hide.DepositFeature.DEPOSIT_CONFIG_KEY;
import static no.elg.ii.feature.hide.DropFeature.DROP_CONFIG_KEY;
import static no.elg.ii.feature.replace.EquipFeature.EQUIP_CONFIG_KEY;
import static no.elg.ii.feature.replace.WithdrawFeature.WITHDRAW_CONFIG_KEY;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;
import no.elg.ii.inventory.InventoryState;
import no.elg.ii.util.WidgetUtil;

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


  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////


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
  @Units(Units.TICKS)
  default int maxUnmodifiedTicks() {
    return InventoryState.DEFAULT_MAX_UNMODIFIED_TICKS;
  }

  @ConfigItem(
    keyName = "hideOpacity",
    name = "Hide opacity",
    description = "A number between " + WidgetUtil.FULLY_OPAQUE + " and " + WidgetUtil.FULLY_TRANSPARENT + "." +
      "<p>Where 0 is fully visible and 255 is fully hidden." +
      "<p>" +
      "<p>This controls how visible an items when it will be dropped/removed the next game tick.",
    position = 110
  )
  @Range(max = WidgetUtil.FULLY_TRANSPARENT)
  default int hideOpacity() {
    return WidgetUtil.NEARLY_TRANSPARENT;
  }

  @ConfigItem(
    keyName = "changeOpacity",
    name = "Change opacity",
    description = "A number between " + WidgetUtil.FULLY_OPAQUE + " (fully visible) and " + WidgetUtil.FULLY_TRANSPARENT + "(fully hidden)." +
      "<p>" +
      "<p>This controls how visible an items when it will be replaced with another item the next game tick.",
    position = 111
  )
  @Range(max = WidgetUtil.FULLY_TRANSPARENT)
  default int changeOpacity() {
    return WidgetUtil.HALF_TRANSPARENT;
  }

  @ConfigItem(
    keyName = "allowWidgetForcing",
    name = "Allow Widget Forcing",
    description = "Force the state of widgets to the predicted next state." +
      "<p>This allows for a more seamless experience, but may cause problems as the client may update the widgets after the plugin has done so.",
    warning = "Disabling this will cause the clicked widgets to not be properly updated.<p>Only disable if there is issues with the plugin.",
    position = 120
  )
  default boolean allowWidgetForcing() {
    return true;
  }
}
