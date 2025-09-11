/*
 * Copyright (c) 2025 Elg
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

import static net.runelite.api.Prayer.AUGURY;
import static net.runelite.api.Prayer.BURST_OF_STRENGTH;
import static net.runelite.api.Prayer.CHIVALRY;
import static net.runelite.api.Prayer.CLARITY_OF_THOUGHT;
import static net.runelite.api.Prayer.DEADEYE;
import static net.runelite.api.Prayer.EAGLE_EYE;
import static net.runelite.api.Prayer.HAWK_EYE;
import static net.runelite.api.Prayer.IMPROVED_REFLEXES;
import static net.runelite.api.Prayer.INCREDIBLE_REFLEXES;
import static net.runelite.api.Prayer.MYSTIC_LORE;
import static net.runelite.api.Prayer.MYSTIC_MIGHT;
import static net.runelite.api.Prayer.MYSTIC_VIGOUR;
import static net.runelite.api.Prayer.MYSTIC_WILL;
import static net.runelite.api.Prayer.PIETY;
import static net.runelite.api.Prayer.PRESERVE;
import static net.runelite.api.Prayer.PROTECT_FROM_MAGIC;
import static net.runelite.api.Prayer.PROTECT_FROM_MELEE;
import static net.runelite.api.Prayer.PROTECT_FROM_MISSILES;
import static net.runelite.api.Prayer.PROTECT_ITEM;
import static net.runelite.api.Prayer.RAPID_HEAL;
import static net.runelite.api.Prayer.RAPID_RESTORE;
import static net.runelite.api.Prayer.REDEMPTION;
import static net.runelite.api.Prayer.RETRIBUTION;
import static net.runelite.api.Prayer.RIGOUR;
import static net.runelite.api.Prayer.ROCK_SKIN;
import static net.runelite.api.Prayer.SHARP_EYE;
import static net.runelite.api.Prayer.SMITE;
import static net.runelite.api.Prayer.STEEL_SKIN;
import static net.runelite.api.Prayer.SUPERHUMAN_STRENGTH;
import static net.runelite.api.Prayer.THICK_SKIN;
import static net.runelite.api.Prayer.ULTIMATE_STRENGTH;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.NoArgsConstructor;
import net.runelite.api.Prayer;
import net.runelite.api.gameval.InterfaceID;

@NoArgsConstructor
public final class PrayerConflict {

  //  public static Map<Integer, Integer> PRAYER_VARBIT_TO_BIT = new HashMap<>(Prayer.values().length);
  public final static Map<Prayer, Integer> PRAYER_TO_BIT;
  public final static Map<Prayer, Integer> PRAYER_TO_INTERFACE;

  public final static Map<Integer, Prayer> BIT_TO_PRAYER;
  public final static Map<Integer, Prayer> INTERFACE_TO_PRAYER;

//  public static Map<Integer, Integer> PRAYER_VARBIT_TO_CONFLICTS = new HashMap<>(Prayer.values().length);
//  public static Map<Prayer, Integer> PRAYER_TO_CONFLICTS;


  static {
    PRAYER_TO_BIT = setupPrayerToBit();
//    PRAYER_TO_CONFLICTS = setupPrayerToConflicts();
    PRAYER_TO_INTERFACE = setupPrayerToInterface();

    // Create reverse lookup map
    BIT_TO_PRAYER = new HashMap<>(PRAYER_TO_BIT.size());
    for (Map.Entry<Prayer, Integer> entry : PRAYER_TO_BIT.entrySet()) {
      BIT_TO_PRAYER.put(entry.getValue(), entry.getKey());
    }

    INTERFACE_TO_PRAYER = new HashMap<>(PRAYER_TO_INTERFACE.size());
    for (Map.Entry<Prayer, Integer> entry : PRAYER_TO_INTERFACE.entrySet()) {
      INTERFACE_TO_PRAYER.put(entry.getValue(), entry.getKey());
    }
  }

  @SuppressWarnings({"MagicNumber", "PointlessBitwiseExpression"})
  private static Map<Prayer, Integer> setupPrayerToBit() {
    Map<Prayer, Integer> prayerToBit = new EnumMap<>(Prayer.class);
    prayerToBit.put(THICK_SKIN, 1 << 0);
    prayerToBit.put(BURST_OF_STRENGTH, 1 << 1);
    prayerToBit.put(CLARITY_OF_THOUGHT, 1 << 2);
    prayerToBit.put(SHARP_EYE, 1 << 18);
    prayerToBit.put(MYSTIC_WILL, 1 << 19);

    prayerToBit.put(ROCK_SKIN, 1 << 3);
    prayerToBit.put(SUPERHUMAN_STRENGTH, 1 << 4);
    prayerToBit.put(IMPROVED_REFLEXES, 1 << 5);
    prayerToBit.put(RAPID_RESTORE, 1 << 6);
    prayerToBit.put(RAPID_HEAL, 1 << 7);

    prayerToBit.put(PROTECT_ITEM, 1 << 8);
    prayerToBit.put(HAWK_EYE, 1 << 20);
    prayerToBit.put(MYSTIC_LORE, 1 << 21);
    prayerToBit.put(STEEL_SKIN, 1 << 9);
    prayerToBit.put(ULTIMATE_STRENGTH, 1 << 10);

    prayerToBit.put(INCREDIBLE_REFLEXES, 1 << 11);
    prayerToBit.put(PROTECT_FROM_MAGIC, 1 << 12);
    prayerToBit.put(PROTECT_FROM_MISSILES, 1 << 13);
    prayerToBit.put(PROTECT_FROM_MELEE, 1 << 14);
    prayerToBit.put(EAGLE_EYE, 1 << 22);
    prayerToBit.put(DEADEYE, 1 << 22); // Intentional overlap with EAGLE_EYE

    prayerToBit.put(MYSTIC_MIGHT, 1 << 23);
    prayerToBit.put(MYSTIC_VIGOUR, 1 << 23); // Intentional overlap with MYSTIC_MIGHT
    //TODO fill in members only prayers

//    prayerToBit.put(RETRIBUTION, 1L << );
//    prayerToBit.put(REDEMPTION, 1L << );
//    prayerToBit.put(SMITE, 1L << );
//    prayerToBit.put(PRESERVE, 1L << );
//
//    prayerToBit.put(CHIVALRY, 1L << );
//    prayerToBit.put(PIETY, 1L << );
//    prayerToBit.put(RIGOUR, 1L << );
//    prayerToBit.put(AUGURY, 1L << );
    return prayerToBit;
  }

//  private static Map<Prayer, Integer> setupPrayerToConflicts() {
//    Map<Prayer, Integer> prayerToConflicts = new EnumMap<>(Prayer.class);
//    prayerToConflicts.put(THICK_SKIN, toConflictLong(ROCK_SKIN, STEEL_SKIN));
//    prayerToConflicts.put(ROCK_SKIN, toConflictLong(THICK_SKIN, STEEL_SKIN));
//    prayerToConflicts.put(STEEL_SKIN, toConflictLong(ROCK_SKIN, THICK_SKIN));
//
//    prayerToConflicts.put(BURST_OF_STRENGTH, toConflictLong(SHARP_EYE, MYSTIC_WILL, SUPERHUMAN_STRENGTH, HAWK_EYE, MYSTIC_LORE, ULTIMATE_STRENGTH, EAGLE_EYE, MYSTIC_MIGHT));
//    prayerToConflicts.put(SHARP_EYE, toConflictLong(BURST_OF_STRENGTH, MYSTIC_WILL, SUPERHUMAN_STRENGTH, HAWK_EYE, MYSTIC_LORE, ULTIMATE_STRENGTH, EAGLE_EYE, MYSTIC_MIGHT));
//    prayerToConflicts.put(MYSTIC_WILL, toConflictLong(BURST_OF_STRENGTH, SHARP_EYE, SUPERHUMAN_STRENGTH, HAWK_EYE, MYSTIC_LORE, ULTIMATE_STRENGTH, EAGLE_EYE, MYSTIC_MIGHT));
//    prayerToConflicts.put(SUPERHUMAN_STRENGTH, toConflictLong(BURST_OF_STRENGTH, SHARP_EYE, MYSTIC_WILL, HAWK_EYE, MYSTIC_LORE, ULTIMATE_STRENGTH, EAGLE_EYE, MYSTIC_MIGHT));
//    prayerToConflicts.put(HAWK_EYE, toConflictLong(BURST_OF_STRENGTH, SHARP_EYE, MYSTIC_WILL, SUPERHUMAN_STRENGTH, MYSTIC_LORE, ULTIMATE_STRENGTH, EAGLE_EYE, MYSTIC_MIGHT));
//    prayerToConflicts.put(MYSTIC_LORE, toConflictLong(BURST_OF_STRENGTH, SHARP_EYE, MYSTIC_WILL, SUPERHUMAN_STRENGTH, HAWK_EYE, ULTIMATE_STRENGTH, EAGLE_EYE, MYSTIC_MIGHT));
//    prayerToConflicts.put(ULTIMATE_STRENGTH, toConflictLong(BURST_OF_STRENGTH, SHARP_EYE, MYSTIC_WILL, SUPERHUMAN_STRENGTH, HAWK_EYE, MYSTIC_LORE, EAGLE_EYE, MYSTIC_MIGHT));
//    prayerToConflicts.put(EAGLE_EYE, toConflictLong(BURST_OF_STRENGTH, SHARP_EYE, MYSTIC_WILL, SUPERHUMAN_STRENGTH, HAWK_EYE, MYSTIC_LORE, ULTIMATE_STRENGTH, MYSTIC_MIGHT));
//    prayerToConflicts.put(MYSTIC_MIGHT, toConflictLong(BURST_OF_STRENGTH, SHARP_EYE, MYSTIC_WILL, SUPERHUMAN_STRENGTH, HAWK_EYE, MYSTIC_LORE, ULTIMATE_STRENGTH, EAGLE_EYE));
//
//    return prayerToConflicts;
//  }

  private static Map<Prayer, Integer> setupPrayerToInterface() {
    Map<Prayer, Integer> prayerToInterface = new EnumMap<>(Prayer.class);
    prayerToInterface.put(THICK_SKIN, InterfaceID.Prayerbook.PRAYER1);
    prayerToInterface.put(BURST_OF_STRENGTH, InterfaceID.Prayerbook.PRAYER2);
    prayerToInterface.put(CLARITY_OF_THOUGHT, InterfaceID.Prayerbook.PRAYER3);
    prayerToInterface.put(SHARP_EYE, InterfaceID.Prayerbook.PRAYER19);
    prayerToInterface.put(MYSTIC_WILL, InterfaceID.Prayerbook.PRAYER22);

    prayerToInterface.put(ROCK_SKIN, InterfaceID.Prayerbook.PRAYER4);
    prayerToInterface.put(SUPERHUMAN_STRENGTH, InterfaceID.Prayerbook.PRAYER5);
    prayerToInterface.put(IMPROVED_REFLEXES, InterfaceID.Prayerbook.PRAYER6);
    prayerToInterface.put(RAPID_RESTORE, InterfaceID.Prayerbook.PRAYER7);
    prayerToInterface.put(RAPID_HEAL, InterfaceID.Prayerbook.PRAYER8);

    prayerToInterface.put(PROTECT_ITEM, InterfaceID.Prayerbook.PRAYER9);
    prayerToInterface.put(HAWK_EYE, InterfaceID.Prayerbook.PRAYER20);
    prayerToInterface.put(MYSTIC_LORE, InterfaceID.Prayerbook.PRAYER23);
    prayerToInterface.put(STEEL_SKIN, InterfaceID.Prayerbook.PRAYER10);
    prayerToInterface.put(ULTIMATE_STRENGTH, InterfaceID.Prayerbook.PRAYER11);

    prayerToInterface.put(INCREDIBLE_REFLEXES, InterfaceID.Prayerbook.PRAYER12);
    prayerToInterface.put(PROTECT_FROM_MAGIC, InterfaceID.Prayerbook.PRAYER13);
    prayerToInterface.put(PROTECT_FROM_MISSILES, InterfaceID.Prayerbook.PRAYER14);
    prayerToInterface.put(PROTECT_FROM_MELEE, InterfaceID.Prayerbook.PRAYER15);
    prayerToInterface.put(EAGLE_EYE, InterfaceID.Prayerbook.PRAYER21);
    prayerToInterface.put(DEADEYE, InterfaceID.Prayerbook.PRAYER21); // Intentional overlap with EAGLE_EYE

    prayerToInterface.put(MYSTIC_MIGHT, InterfaceID.Prayerbook.PRAYER24);
    prayerToInterface.put(MYSTIC_VIGOUR, InterfaceID.Prayerbook.PRAYER24); // Intentional overlap with MYSTIC_MIGHT

    prayerToInterface.put(RETRIBUTION, InterfaceID.Prayerbook.PRAYER16);
    prayerToInterface.put(REDEMPTION, InterfaceID.Prayerbook.PRAYER17);
    prayerToInterface.put(SMITE, InterfaceID.Prayerbook.PRAYER18);
    prayerToInterface.put(PRESERVE, InterfaceID.Prayerbook.PRAYER29);

    prayerToInterface.put(CHIVALRY, InterfaceID.Prayerbook.PRAYER26);
    prayerToInterface.put(PIETY, InterfaceID.Prayerbook.PRAYER27);
    prayerToInterface.put(RIGOUR, InterfaceID.Prayerbook.PRAYER25);
    prayerToInterface.put(AUGURY, InterfaceID.Prayerbook.PRAYER28);
    return prayerToInterface;
  }

  public static List<Prayer> prayerBitToPrayer(int prayerBits) {
    List<Prayer> prayers = new ArrayList<>(Integer.bitCount(prayerBits));

    for (Map.Entry<Integer, Prayer> entry : BIT_TO_PRAYER.entrySet()) {
      if ((prayerBits & entry.getKey()) != 0) {
        prayers.add(entry.getValue());
      }
    }
    return prayers;
  }

  /**
   *
   * Note: must not be called before {@link PrayerConflict::PRAYER_TO_BIT} is populated
   *
   * @return The bits of each prayer combined with bitwise OR
   */
  public static int toConflictInt(Prayer... prayers) {
    return Stream.of(prayers).mapToInt(PRAYER_TO_BIT::get).reduce(0, (a, b) -> a | b);
  }
}
