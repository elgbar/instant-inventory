/*
 * Copyright (c) 2022-2026 Elg
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
import static no.elg.ii.feature.features.SpecialAttackFeature.SPEC_CONFIG_KEY;
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
    name = "Appearance",
    description = "How predicted widget will look",
    position = 0
  )
  String APPEARANCE_SECTION = "instant-inventory-appearance";

  @ConfigSection(
    name = "Features",
    description = "Toggle different features of the plugin",
    position = 10
  )
  String FEATURE_SECTION = "instant-inventory-features";

  @ConfigSection(
    name = "Item Opacity Fix",
    description = "Make what other plugins draw on transparent items just as transparent",
    position = 30,
    closedByDefault = true
  )
  String ITEM_OPACITY_FIX_SECTION = "instant-inventory-item-opacity-fix";

  @ConfigSection(
    name = "Advanced",
    description = "Advanced settings for all features",
    position = 40,
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

  @ConfigItem(
    keyName = SPEC_CONFIG_KEY,
    section = FEATURE_SECTION,
    name = "Highlight Spec Bar Instantly",
    description = "Highlight the special attack bar instantly",
    position = 6
  )
  default boolean instantSpec() {
    return true;
  }

  /// ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  int DEFAULT_ITEM_CHANGE_OPACITY = 75;  //% transparent
  int DEFAULT_ITEM_FILL_OPACITY = 75;  //% transparent
  int DEFAULT_ITEM_HIDE_OPACITY = 20;  //% transparent
  int DEFAULT_PRAYER_DISABLE_OPACITY = 15;  //% transparent
  int DEFAULT_PRAYER_ENABLE_OPACITY = 80;  //% transparent

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
    return DEFAULT_ITEM_HIDE_OPACITY;
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
    return DEFAULT_ITEM_CHANGE_OPACITY;
  }

  @ConfigItem(
    section = APPEARANCE_SECTION,
    keyName = "fillOpacityPercent",
    name = "Added items opacity",
    description = "How transparent items are when added to an empty inventory slot." +
      "<p>A lower value will cause items to be more transparent",
    position = 25
  )
  @Range(max = 100)
  @Units(Units.PERCENT)
  default int fillOpacityPercent() {
    // Added items used the changed items opacity before this setting existed. RuneLite saves this default the first
    // time the plugin loads, so it is copied once rather than following later changes
    return changeOpacityPercent();
  }

  @ConfigItem(
    keyName = "fillOpacityPercent",
    hidden = true,
    name = "",
    description = ""
  )
  default void fillOpacityPercent(int fillOpacityPercent) {
  }

  @ConfigItem(
    section = APPEARANCE_SECTION,
    keyName = "prayerDisableOpacityPercent",
    name = "Disabled prayer opacity",
    description =
      "Temporary background opacity shown <i>after you click to disable</i> a prayer but <i>before the server confirms it is off</i>."
        + "<p>"
        + "<p>Applies only during this short prediction window."
        + "<p>Set to 0 to make it indistinguishable from normal inactive prayers."
        + "<p>"
        + "<p>Lower value makes background more transparent.",
    position = 30
  )
  @Range(max = 100)
  @Units(Units.PERCENT)
  default int prayerDisableOpacityPercent() {
    return DEFAULT_PRAYER_DISABLE_OPACITY;
  }

  @ConfigItem(
    section = APPEARANCE_SECTION,
    keyName = "prayerEnableOpacityPercent",
    name = "Enabled prayer opacity",
    description =
      "Temporary background opacity shown <i>after you click to enable</i> a prayer but <i>before the server confirms it is active</i>."
        + "<p>"
        + "<p>Applies only during this short prediction window."
        + "<p>Set to 100 to make it indistinguishable from normal active prayers."
        + "<p>"
        + "<p>Lower value makes background more transparent.",
    position = 40
  )
  @Range(max = 100)
  @Units(Units.PERCENT)
  default int prayerEnableOpacityPercent() {
    return DEFAULT_PRAYER_ENABLE_OPACITY;
  }


  /// ////////////////////////////////////////////////////////////////////////////////////////////////////////////


  @ConfigItem(
    section = ITEM_OPACITY_FIX_SECTION,
    keyName = "fixItemOpacity",
    name = "Fix item opacity",
    description =
      "Make what other plugins, such as Inventory Tags, Item Charges or Rune Pouch, draw on items this plugin" +
        "<p>makes transparent just as transparent. Otherwise they draw fully opaque on top of them"
        + "<p>"
        + "<p>You should normally leave this enabled unless there is a bug",
    position = 30
  )
  default boolean fixItemOpacity() {
    return true;
  }

  int MAX_MARGIN = 8;

  @ConfigItem(
    section = ITEM_OPACITY_FIX_SECTION,
    keyName = "fixItemOpacityTopMargin",
    name = "Top margin",
    description = "How many pixels above an item other plugins draw",
    position = 31
  )
  @Range(max = MAX_MARGIN)
  default int topMargin() {
    return 1;
  }

  @ConfigItem(
    section = ITEM_OPACITY_FIX_SECTION,
    keyName = "fixItemOpacityLeftMargin",
    name = "Left margin",
    description = "How many pixels to the left of an item other plugins draw",
    position = 32
  )
  @Range(max = MAX_MARGIN)
  default int leftMargin() {
    return 3;
  }

  @ConfigItem(
    section = ITEM_OPACITY_FIX_SECTION,
    keyName = "fixItemOpacityBottomMargin",
    name = "Bottom margin",
    description = "How many pixels below an item other plugins draw",
    position = 33
  )
  @Range(max = MAX_MARGIN)
  default int bottomMargin() {
    return 3;
  }

  @ConfigItem(
    section = ITEM_OPACITY_FIX_SECTION,
    keyName = "fixItemOpacityRightMargin",
    name = "Right margin",
    description = "How many pixels to the right of an item other plugins draw",
    position = 34
  )
  @Range(max = MAX_MARGIN)
  default int rightMargin() {
    return 1;
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
        + "<p>When in PvP or Bossing it is recommended to set this to 1."
        + "<p>"
        + "<p>This setting applies only items and not generally.",
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
        + "<p>This setting is affected by the ping and performance of the server. If you see widgets flicker back into existence, increase this value."
        + "<p>"
        + "<p>This overwrites the \"Max Unmodified Ticks\" setting for inventory actions.",
    position = 20
  )
  @Units(Units.MILLISECONDS)
  default int minChangedMs() {
    return Util.TICK_LENGTH_MS / 2;
  }

  @ConfigItem(
    section = ADVANCED_SECTION,
    keyName = "redrawHiddenInventories",
    name = "Fix Inventory Flicker",
    description =
      "Redraw inventories hidden by another interface, e.g., the bank, when the items in the inventory change."
        + "<p>"
        + "<p>The game does not redraw a hidden inventory, so when closing the bank the inventory briefly shows the"
        + "<p>items it had when the bank was opened.",
    position = 30
  )
  default boolean redrawHiddenInventories() {
    return true;
  }


}
