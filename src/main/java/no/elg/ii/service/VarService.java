/*
 * Copyright (c) 2023-2025 Elg
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

package no.elg.ii.service;

import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.NoArgsConstructor;
import net.runelite.api.Client;
import net.runelite.api.annotations.Varbit;
import net.runelite.api.annotations.Varp;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.game.ItemManager;

/**
 * @see net.runelite.api.gameval.VarbitID
 * @see net.runelite.api.gameval.VarPlayerID
 */
@Singleton
@NoArgsConstructor
public class VarService {

  @Inject
  private Client client;
  @Inject
  private ItemManager itemManager;

  public static final int VAR_VALUE_TRUE = 1;
  public static final int VAR_VALUE_FALSE = 0;

  /**
   * @param varbit the varbit to test
   * @return Whether the value of the varbit is true, i.e., 1
   */
  public boolean isVarbitTrue(@Varbit int varbit) {
    return varbitValue(varbit) == VAR_VALUE_TRUE;
  }

  /**
   * @param varbit the varbit to test
   * @return Whether the value of the varbit is false, i.e., 0
   */
  public boolean isVarbitFalse(@Varbit int varbit) {
    return varbitValue(varbit) == VAR_VALUE_FALSE;
  }

  /**
   * @param varp the varp to test
   * @return Whether the value of the varp is true, i.e., 1
   */
  public boolean isVarpTrue(@Varp int varp) {
    return varpValue(varp) == VAR_VALUE_TRUE;
  }

  /**
   * @param varp the varp to test
   * @return Whether the value of the varp is false, i.e., 0
   */
  public boolean isVarpFalse(@Varp int varp) {
    return varpValue(varp) == VAR_VALUE_FALSE;
  }

  public int varbitValue(@Varbit int varbit) {
    return client.getVarbitValue(varbit);
  }

  public int varpValue(@Varp int varp) {
    return client.getVarpValue(varp);
  }

  public boolean willDropWarningBeShownForItem(int itemId, int quantity) {
    if (isVarbitFalse(VarbitID.OPTION_DROPWARNING_ON)) {
      return false;
    }
    var canonItemId = itemManager.canonicalize(itemId);
    var price = itemManager.getItemPriceWithSource(canonItemId, false);
    return varbitValue(VarbitID.OPTION_DROPWARNING_VALUE) < price * quantity;
  }

  /**
   * @param slotIndex The index of the slot to check
   * @return If this slot is unlocked or bank slot locks are disabled
   */
  public boolean isBankInventorySlotUnlocked(int slotIndex) {
    if (isVarbitTrue(VarbitID.BANK_SIDE_SLOT_IGNOREINVLOCKS)) {
      return true;
    }
    int slotMask = (1 << slotIndex); // Bit that should be set for the slot
    return (slotMask & varbitValue(VarbitID.BANK_SIDE_SLOT_OVERVIEW)) == 0;
  }
}
