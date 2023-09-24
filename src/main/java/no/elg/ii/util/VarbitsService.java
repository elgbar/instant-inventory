/*
 * Copyright (c) 2023 Elg
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

package no.elg.ii.util;

import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.client.game.ItemManager;

/**
 * Holds various varbits used by this plugin which are not documented in the RuneLite API.
 * <p></p>
 * Each varbit is written in the format <code>VARBIT_{type}_{name}</code>. Where <code>{type}</code> is the type the
 * varbit stores and <code>{name}</code> is the name of the varbit.
 *
 * @see net.runelite.api.Varbits
 */
@Singleton
public class VarbitsService {

  @Inject
  private Client client;
  @Inject
  private ItemManager itemManager;

  /**
   * When enabled, if you go to drop an item that has a higher value than the threshold you set, a warning will appear.
   * <p>
   * Can be toggled in <i>All Settings</i> under the <i>Warnings</i> tab with the name <i>Drop item warning</i>
   *
   * @see #INT_MINIMUM_ITEM_VALUE_FOR_DROP_WARNING
   */
  public static final int BOOLEAN_DROP_ITEM_WARNING = 5411;

  /**
   * If you  go to drop an item that is worth more than this value, a warning will appear.
   * <p>
   * Can be toggled in <i>All Settings</i> under the <i>Warnings</i> tab with the name <i>Minimum item value needed for drop item warning</i>
   *
   * @see #BOOLEAN_DROP_ITEM_WARNING
   */
  public static final int INT_MINIMUM_ITEM_VALUE_FOR_DROP_WARNING = 5412;
  public static final int INT_MINIMUM_ITEM_VALUE_FOR_ALCH_WARNING = 6091;
  public static final int BOOLEAN_ALWAYS_WARN_WHEN_ALCHING_UNTRADEABLE = 6092;

  /**
   * Whether items from the bank will be withdrawn as notes
   */
  public static final int BOOLEAN_WITHDRAW_AS_NOTE = 3958;

  /**
   * How much will be withdrawn from the bank by default (i.e., left clicking and item)
   *
   * <p>0 = 1 at a time
   * <p>1 = 5 at a time
   * <p>2 = 10 at a time
   * <p>3 = X at a time
   * <p>4 = All items
   */
  public static final int ENUM_WITHDRAW_DEFAULT_QUANTITY = 8354;

  /**
   * The quantity of items to withdraw when <code>X</code> is selected
   */
  public static final int INT_DEFAULT_X_QUANTITY = 3960;


  /**
   * Whether placeholders will be shown in the bank
   */
  public static final int BOOLEAN_ALWAYS_SET_BANK_PLACEHOLDER = 3755;


  public static final int VARBIT_VALUE_TRUE = 1;
  public static final int VARBIT_VALUE_FALSE = 0;

  /**
   * @param varbit the varbit to test
   * @return Whether the value of the varbit is true, i.e., 1
   */
  public boolean isVarbitTrue(int varbit) {
    return client.getVarbitValue(varbit) == VARBIT_VALUE_TRUE;
  }

  /**
   * @param varbit the varbit to test
   * @return Whether the value of the varbit is false, i.e., 0
   */
  public boolean isVarbitFalse(int varbit) {
    return client.getVarbitValue(varbit) == VARBIT_VALUE_FALSE;
  }

  public int varbitValue(int varbit) {
    return client.getVarbitValue(varbit);
  }

  public boolean willDropWarningBeShownForItem(int itemId, int quantity) {
    if (isVarbitFalse(BOOLEAN_DROP_ITEM_WARNING)) {
      return false;
    }
    var canonItemId = itemManager.canonicalize(itemId);
    var price = itemManager.getItemPrice(canonItemId);
    return varbitValue(INT_MINIMUM_ITEM_VALUE_FOR_DROP_WARNING) < price * quantity;
  }
}
