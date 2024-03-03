/*
 * Copyright (c) 2023-2024 Elg
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

import javax.annotation.Nonnull;
import net.runelite.api.widgets.Widget;

public class Util {
  public static final int NO_MENU_OPTION_NUMBER = -1;
  public static final int TICK_LENGTH_MS = 600;

  public static int getNumberFromMenuOption(String text, @Nonnull Widget widget) {
    try {
      String substring = text.substring(text.indexOf('-') + 1);
      int quantity;
      if ("All".equals(substring)) {
        quantity = Integer.MAX_VALUE;
      } else if ("All-but-1".equalsIgnoreCase(substring)) {
        quantity = widget.getItemQuantity() - 1;
      } else {
        quantity = Integer.parseInt(substring);
      }
      assert quantity > 0;
      return quantity;
    } catch (NumberFormatException | IndexOutOfBoundsException e) {
      return NO_MENU_OPTION_NUMBER;
    }
  }

  public static int coerceIn(int value, int min, int max) {
    return Math.max(min, Math.min(max, value));
  }

  private Util() {
  }
}
