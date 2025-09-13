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

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.runelite.api.Prayer;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarbitID;

@NoArgsConstructor
public final class PrayerInfo {

  /**
   * Mapping to find the bit for a prayer
   */
  public final static EnumMap<Prayer, Integer> PRAYER_TO_BIT;


  /**
   * Key is the interface id in the prayer book. value is the bit for the prayer.
   *
   * @see #PRAYER_TO_BIT
   *
   */
  public static final Map<Integer, Integer> INTERFACE_TO_BIT;

  /**
   * Groups of conflicting prayers
   */
  public final static int[] CONFLICTING_PRAYERS;

  static {
    PRAYER_TO_BIT = setupPrayerToBit();
    INTERFACE_TO_BIT = setupInterfaceToBit();
    // Endgame prayers conflict with (almost) everything else
    int endgame = prayerToBits(CHIVALRY, PIETY, RIGOUR, AUGURY);
    // Ranged and magic prayers
    int rangedAndMagic = prayerToBits(SHARP_EYE, MYSTIC_WILL, HAWK_EYE, MYSTIC_LORE, EAGLE_EYE, MYSTIC_MIGHT);
    CONFLICTING_PRAYERS = new int[]{
      // Protection / overheads
      prayerToBits(PROTECT_FROM_MAGIC, PROTECT_FROM_MISSILES, PROTECT_FROM_MELEE, RETRIBUTION, REDEMPTION, SMITE),
      // Defence prayers (can be used with ranged/magic prayers!)
      prayerToBits(THICK_SKIN, ROCK_SKIN, STEEL_SKIN) | endgame,
      // Strength prayers
      prayerToBits(BURST_OF_STRENGTH, SUPERHUMAN_STRENGTH, ULTIMATE_STRENGTH) | rangedAndMagic | endgame,
      // Attack prayers
      prayerToBits(CLARITY_OF_THOUGHT, IMPROVED_REFLEXES, INCREDIBLE_REFLEXES) | rangedAndMagic | endgame,
      // Ranged and magic prayers
      rangedAndMagic | endgame, //
      endgame,//
    };
  }

  /**
   * Manual setup of prayer to a bit. These bits reflect what bit the server sets in {@link VarbitID#PRAYER_ALLACTIVE}.
   *
   * @see VarbitID#PRAYER_ALLACTIVE
   */
  @SuppressWarnings({"MagicNumber", "PointlessBitwiseExpression"})
  private static EnumMap<Prayer, Integer> setupPrayerToBit() {
    EnumMap<Prayer, Integer> prayerToBit = new EnumMap<>(Prayer.class);
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

  private static Map<Integer, Integer> setupInterfaceToBit() {
    Map<Integer, Integer> interfaceTobit = new HashMap<>(PRAYER_TO_BIT.size());

    interfaceTobit.put(InterfaceID.Prayerbook.PRAYER1, PRAYER_TO_BIT.get(THICK_SKIN));
    interfaceTobit.put(InterfaceID.Prayerbook.PRAYER2, PRAYER_TO_BIT.get(BURST_OF_STRENGTH));
    interfaceTobit.put(InterfaceID.Prayerbook.PRAYER3, PRAYER_TO_BIT.get(CLARITY_OF_THOUGHT));
    interfaceTobit.put(InterfaceID.Prayerbook.PRAYER19, PRAYER_TO_BIT.get(SHARP_EYE));
    interfaceTobit.put(InterfaceID.Prayerbook.PRAYER22, PRAYER_TO_BIT.get(MYSTIC_WILL));

    interfaceTobit.put(InterfaceID.Prayerbook.PRAYER4, PRAYER_TO_BIT.get(ROCK_SKIN));
    interfaceTobit.put(InterfaceID.Prayerbook.PRAYER5, PRAYER_TO_BIT.get(SUPERHUMAN_STRENGTH));
    interfaceTobit.put(InterfaceID.Prayerbook.PRAYER6, PRAYER_TO_BIT.get(IMPROVED_REFLEXES));
    interfaceTobit.put(InterfaceID.Prayerbook.PRAYER7, PRAYER_TO_BIT.get(RAPID_RESTORE));
    interfaceTobit.put(InterfaceID.Prayerbook.PRAYER8, PRAYER_TO_BIT.get(RAPID_HEAL));

    interfaceTobit.put(InterfaceID.Prayerbook.PRAYER9, PRAYER_TO_BIT.get(PROTECT_ITEM));
    interfaceTobit.put(InterfaceID.Prayerbook.PRAYER20, PRAYER_TO_BIT.get(HAWK_EYE));
    interfaceTobit.put(InterfaceID.Prayerbook.PRAYER23, PRAYER_TO_BIT.get(MYSTIC_LORE));
    interfaceTobit.put(InterfaceID.Prayerbook.PRAYER10, PRAYER_TO_BIT.get(STEEL_SKIN));
    interfaceTobit.put(InterfaceID.Prayerbook.PRAYER11, PRAYER_TO_BIT.get(ULTIMATE_STRENGTH));

    interfaceTobit.put(InterfaceID.Prayerbook.PRAYER12, PRAYER_TO_BIT.get(INCREDIBLE_REFLEXES));
    interfaceTobit.put(InterfaceID.Prayerbook.PRAYER13, PRAYER_TO_BIT.get(PROTECT_FROM_MAGIC));
    interfaceTobit.put(InterfaceID.Prayerbook.PRAYER14, PRAYER_TO_BIT.get(PROTECT_FROM_MISSILES));
    interfaceTobit.put(InterfaceID.Prayerbook.PRAYER15, PRAYER_TO_BIT.get(PROTECT_FROM_MELEE));
    interfaceTobit.put(InterfaceID.Prayerbook.PRAYER21, PRAYER_TO_BIT.get(EAGLE_EYE)); // Uses same slot as DEADEYE

    interfaceTobit.put(InterfaceID.Prayerbook.PRAYER24, PRAYER_TO_BIT.get(MYSTIC_MIGHT)); // uses same slot as MYSTIC_VIGOUR
    interfaceTobit.put(InterfaceID.Prayerbook.PRAYER16, PRAYER_TO_BIT.get(RETRIBUTION));
    interfaceTobit.put(InterfaceID.Prayerbook.PRAYER17, PRAYER_TO_BIT.get(REDEMPTION));
    interfaceTobit.put(InterfaceID.Prayerbook.PRAYER18, PRAYER_TO_BIT.get(SMITE));
    interfaceTobit.put(InterfaceID.Prayerbook.PRAYER29, PRAYER_TO_BIT.get(PRESERVE));

    interfaceTobit.put(InterfaceID.Prayerbook.PRAYER26, PRAYER_TO_BIT.get(CHIVALRY));
    interfaceTobit.put(InterfaceID.Prayerbook.PRAYER27, PRAYER_TO_BIT.get(PIETY));
    interfaceTobit.put(InterfaceID.Prayerbook.PRAYER25, PRAYER_TO_BIT.get(RIGOUR));
    interfaceTobit.put(InterfaceID.Prayerbook.PRAYER28, PRAYER_TO_BIT.get(AUGURY));
    return interfaceTobit;
  }

  /**
   *
   * Note: must not be called before {@link PrayerInfo#PRAYER_TO_BIT} is populated
   *
   * @return The bits of each prayer combined with bitwise OR
   */
  public static int prayerToBits(@NonNull Prayer... prayers) {
    return Stream.of(prayers).mapToInt(PRAYER_TO_BIT::get).reduce(0, (a, b) -> a | b);
  }
}
