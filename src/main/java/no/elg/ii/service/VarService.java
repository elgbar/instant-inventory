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
import net.runelite.api.annotations.VarCInt;
import net.runelite.api.annotations.VarCStr;
import net.runelite.api.annotations.Varbit;
import net.runelite.api.annotations.Varp;

/**
 * @see net.runelite.api.gameval.VarbitID
 * @see net.runelite.api.gameval.VarPlayerID
 */
@Singleton
@NoArgsConstructor
public class VarService {

  @Inject
  private Client client;

  public static final int VAR_VALUE_TRUE = 1;
  public static final int VAR_VALUE_FALSE = 0;


  public int varbitValue(@Varbit int varbit) {
    return client.getVarbitValue(varbit);
  }

  public int varpValue(@Varp int varp) {
    return client.getVarpValue(varp);
  }

  public int varCIntValue(@VarCInt int varc) {
    return client.getVarcIntValue(varc);
  }

  public String varCStrValue(@VarCStr int varc) {
    return client.getVarcStrValue(varc);
  }

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


  /**
   * @param varc the varc to test
   * @return Whether the value of the varc is true, i.e., 1
   */
  public boolean isVarcTrue(@VarCInt int varc) {
    return varCIntValue(varc) == VAR_VALUE_TRUE;
  }

  /**
   * @param varc the varc to test
   * @return Whether the value of the varc is false, i.e., 0
   */
  public boolean isVarcFalse(@VarCInt int varc) {
    return varCIntValue(varc) == VAR_VALUE_FALSE;
  }
}
