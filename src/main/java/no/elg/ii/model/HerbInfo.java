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
package no.elg.ii.model;

import java.util.HashMap;
import java.util.Map;
import lombok.Value;
import net.runelite.api.ItemID;

@Value
@SuppressWarnings("MagicNumber")
public class HerbInfo {

  /**
   * Map of {@link ItemID} from grimy herbs to cleaned herbs
   */
  public static Map<Integer, HerbInfo> HERBS = new HashMap<>();

  static {
    HERBS.put(ItemID.GRIMY_ROGUES_PURSE, new HerbInfo(ItemID.ROGUES_PURSE, 3));
    HERBS.put(ItemID.GRIMY_SNAKE_WEED, new HerbInfo(ItemID.SNAKE_WEED, 3));
    HERBS.put(ItemID.GRIMY_ARDRIGAL, new HerbInfo(ItemID.ARDRIGAL, 3));
    HERBS.put(ItemID.GRIMY_SITO_FOIL, new HerbInfo(ItemID.SITO_FOIL, 3));
    HERBS.put(ItemID.GRIMY_VOLENCIA_MOSS, new HerbInfo(ItemID.VOLENCIA_MOSS, 3));
    HERBS.put(ItemID.GRIMY_GUAM_LEAF, new HerbInfo(ItemID.GUAM_LEAF, 3));
    HERBS.put(ItemID.GRIMY_MARRENTILL, new HerbInfo(ItemID.MARRENTILL, 5));
    HERBS.put(ItemID.GRIMY_TARROMIN, new HerbInfo(ItemID.TARROMIN, 11));
    HERBS.put(ItemID.GRIMY_HARRALANDER, new HerbInfo(ItemID.HARRALANDER, 20));
    HERBS.put(ItemID.GRIMY_RANARR_WEED, new HerbInfo(ItemID.RANARR_WEED, 25));
    HERBS.put(ItemID.GRIMY_IRIT_LEAF, new HerbInfo(ItemID.IRIT_LEAF, 40));
    HERBS.put(ItemID.GRIMY_AVANTOE, new HerbInfo(ItemID.AVANTOE, 48));
    HERBS.put(ItemID.GRIMY_KWUARM, new HerbInfo(ItemID.KWUARM, 54));
    HERBS.put(ItemID.GRIMY_SNAPDRAGON, new HerbInfo(ItemID.SNAPDRAGON, 59));
    HERBS.put(ItemID.GRIMY_CADANTINE, new HerbInfo(ItemID.CADANTINE, 65));
    HERBS.put(ItemID.GRIMY_DWARF_WEED, new HerbInfo(ItemID.DWARF_WEED, 70));
    HERBS.put(ItemID.GRIMY_TORSTOL, new HerbInfo(ItemID.TORSTOL, 75));
    HERBS.put(ItemID.GRIMY_LANTADYME, new HerbInfo(ItemID.LANTADYME, 67));
    HERBS.put(ItemID.GRIMY_TOADFLAX, new HerbInfo(ItemID.TOADFLAX, 30));

    // Chambers of Xeric herbs
    HERBS.put(ItemID.GRIMY_GOLPAR, new HerbInfo(ItemID.GOLPAR, 47));
    HERBS.put(ItemID.GRIMY_BUCHU_LEAF, new HerbInfo(ItemID.BUCHU_LEAF, 52));
    HERBS.put(ItemID.GRIMY_NOXIFER, new HerbInfo(ItemID.NOXIFER, 60));
  }

  int cleanItemId;
  int minLevel;

}
