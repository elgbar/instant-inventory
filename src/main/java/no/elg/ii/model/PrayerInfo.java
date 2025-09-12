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
import net.runelite.api.Client;
import net.runelite.api.Prayer;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarbitID;

@NoArgsConstructor
public final class PrayerInfo {

  /**
   * Mapping to find the bit for a prayer
   */
  public final static Map<Prayer, Integer> PRAYER_TO_BIT;
  /**
   * Mapping to find the prayer given a single bit
   */
  public final static Map<Integer, Prayer> BIT_TO_PRAYER;

  /**
   * Mapping to find the interface ID for a prayer
   *
   * @see Client#getWidget(int)
   * @see InterfaceID.Prayerbook
   */
  public final static Map<Prayer, Integer> PRAYER_TO_INTERFACE;
  /**
   * Mapping to find the prayer given an interface ID
   *
   * @see Client#getWidget(int)
   * @see InterfaceID.Prayerbook
   */
  public final static Map<Integer, Prayer> INTERFACE_TO_PRAYER;

  /**
   * Groups of conflicting prayers
   */
  public final static int[] CONFLICTING_PRAYERS;

  static {
    PRAYER_TO_BIT = setupPrayerToBit();
    PRAYER_TO_INTERFACE = setupPrayerToInterface();
    CONFLICTING_PRAYERS = new int[]{ //
      prayerToBits(THICK_SKIN, ROCK_SKIN, STEEL_SKIN, CHIVALRY, PIETY, RIGOUR, AUGURY), //
      prayerToBits(BURST_OF_STRENGTH, SUPERHUMAN_STRENGTH, ULTIMATE_STRENGTH, SHARP_EYE, MYSTIC_WILL, HAWK_EYE, MYSTIC_LORE, EAGLE_EYE, MYSTIC_MIGHT, CHIVALRY, PIETY, RIGOUR, AUGURY), //
      prayerToBits(CLARITY_OF_THOUGHT, IMPROVED_REFLEXES, INCREDIBLE_REFLEXES, SHARP_EYE, MYSTIC_WILL, HAWK_EYE, MYSTIC_LORE, EAGLE_EYE, MYSTIC_MIGHT, CHIVALRY, PIETY, RIGOUR, AUGURY), //
      prayerToBits(SHARP_EYE, MYSTIC_WILL, HAWK_EYE, MYSTIC_LORE, EAGLE_EYE, MYSTIC_MIGHT, CHIVALRY, PIETY, RIGOUR, AUGURY), //
      prayerToBits(PROTECT_FROM_MAGIC, PROTECT_FROM_MISSILES, PROTECT_FROM_MELEE, RETRIBUTION, REDEMPTION, SMITE), //
      prayerToBits(CHIVALRY, PIETY, RIGOUR, AUGURY), //
    };

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

  /**
   * Manual setup of prayer to a bit. These bits reflect what bit the server sets in {@link VarbitID#PRAYER_ALLACTIVE}.
   *
   * @see VarbitID#PRAYER_ALLACTIVE
   */
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

    prayerToBit.put(RETRIBUTION, 1 << 15);
    prayerToBit.put(REDEMPTION, 1 << 16);
    prayerToBit.put(SMITE, 1 << 17);
    prayerToBit.put(PRESERVE, 1 << 28);

    prayerToBit.put(CHIVALRY, 1 << 25);
    prayerToBit.put(PIETY, 1 << 26);
    prayerToBit.put(RIGOUR, 1 << 24);
    prayerToBit.put(AUGURY, 1 << 27);
    return prayerToBit;
  }

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

  public static List<Prayer> prayerBitsToPrayers(int prayerBits) {
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
   * Note: must not be called before {@link PrayerInfo#PRAYER_TO_BIT} is populated
   *
   * @return The bits of each prayer combined with bitwise OR
   */
  public static int prayerToBits(Prayer... prayers) {
    return Stream.of(prayers).mapToInt(PRAYER_TO_BIT::get).reduce(0, (a, b) -> a | b);
  }
}
