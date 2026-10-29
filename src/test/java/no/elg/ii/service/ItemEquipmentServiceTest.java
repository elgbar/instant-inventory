/*
 * Copyright (c) 2026 Elg
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

import static net.runelite.api.gameval.ItemID.BRONZE_2H_SWORD;
import static net.runelite.api.gameval.ItemID.BRONZE_SQ_SHIELD;
import static net.runelite.api.gameval.ItemID.BRONZE_SWORD;
import static net.runelite.api.gameval.ItemID.OBLIGATOR;
import static net.runelite.api.gameval.ItemID.RONDACHE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.client.game.ItemEquipmentStats;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStats;
import no.elg.ii.model.EquipSlot;
import no.elg.ii.test.TestSetup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ItemEquipmentServiceTest {

  private static final int WEAPON_SLOT = EquipmentInventorySlot.WEAPON.getSlotIdx();
  private static final int SHIELD_SLOT = EquipmentInventorySlot.SHIELD.getSlotIdx();

  private static final Item SWORD = new Item(BRONZE_SWORD, 1);
  private static final Item SHIELD = new Item(BRONZE_SQ_SHIELD, 1);

  /*
   * Items without item stats, as when RuneLite has not yet updated them after a game update
   */
  private static final Item NEW_WEAPON = new Item(OBLIGATOR, 1);
  private static final Item NEW_SHIELD = new Item(RONDACHE, 1);

  private static final Map<Integer, ItemStats> ITEM_STATS = Map.of(
    BRONZE_2H_SWORD, equipable(EquipmentInventorySlot.WEAPON, true),
    BRONZE_SWORD, equipable(EquipmentInventorySlot.WEAPON, false),
    BRONZE_SQ_SHIELD, equipable(EquipmentInventorySlot.SHIELD, false)
  );

  /**
   * What the server says is worn, by equipment slot
   */
  private final Map<Integer, Item> worn = new HashMap<>();

  private ItemEquipmentService service;
  private ItemContainer wornContainer;

  @Before
  public void setUp() {
    service = new ItemEquipmentService();
    service.client = TestSetup.mockClient();
    service.itemManager = mock(ItemManager.class, withSettings().defaultAnswer(invocation ->
      "getItemStats".equals(invocation.getMethod().getName())
        ? ITEM_STATS.get(invocation.<Integer>getArgument(0))
        : RETURNS_DEFAULTS.answer(invocation)));
    wornContainer = mock(ItemContainer.class, withSettings().defaultAnswer(invocation ->
      "getItem".equals(invocation.getMethod().getName())
        ? worn.get(invocation.<Integer>getArgument(0))
        : RETURNS_DEFAULTS.answer(invocation)));
  }

  @Test
  public void getEquipSlot_item_with_stats_is_where_its_stats_say() {
    assertEquals(new EquipSlot(WEAPON_SLOT, true), service.getEquipSlot(BRONZE_2H_SWORD));
  }

  @Test
  public void getEquipSlot_item_without_stats_never_seen_worn_is_not_known() {
    assertNull(service.getEquipSlot(RONDACHE));
  }

  @Test
  public void getEquipSlot_item_without_stats_seen_worn_is_where_it_was_worn() {
    serverWears(null, NEW_SHIELD);
    // It is still known after being taken off
    serverWears(null, SHIELD);

    assertEquals(new EquipSlot(SHIELD_SLOT, null), service.getEquipSlot(RONDACHE));
  }

  @Test
  public void getEquipSlot_weapon_without_stats_seen_without_a_shield_is_not_known_to_be_two_handed() {
    serverWears(NEW_WEAPON, null);

    assertEquals(new EquipSlot(WEAPON_SLOT, null), service.getEquipSlot(OBLIGATOR));
  }

  @Test
  public void getEquipSlot_weapon_without_stats_seen_with_a_shield_is_one_handed() {
    // Worn together with a shield, so it is one-handed
    serverWears(NEW_WEAPON, SHIELD);
    serverWears(NEW_WEAPON, null);

    assertEquals(new EquipSlot(WEAPON_SLOT, false), service.getEquipSlot(OBLIGATOR));
  }

  @Test
  public void getEquipSlot_weapon_without_stats_seen_taking_off_a_shield_is_two_handed() {
    serverWears(SWORD, SHIELD);
    // Wielding it took off the shield, so it is two-handed
    serverWears(NEW_WEAPON, null);

    assertEquals(new EquipSlot(WEAPON_SLOT, true), service.getEquipSlot(OBLIGATOR));
  }

  @Test
  public void getEquipSlot_weapon_without_stats_seen_with_a_shield_stays_one_handed_after_the_shield_was_removed_as_it_was_wielded() {
    serverWears(NEW_WEAPON, SHIELD);
    serverWears(SWORD, SHIELD);
    // Removing the shield the same tick the weapon is wielded looks like wielding it took off the shield
    serverWears(NEW_WEAPON, null);

    assertEquals(new EquipSlot(WEAPON_SLOT, false), service.getEquipSlot(OBLIGATOR));
  }

  /**
   * The server tells the client what is worn, as it does when logging in and after equipping
   */
  private void serverWears(@Nullable Item weapon, @Nullable Item shield) {
    worn.clear();
    if (weapon != null) {
      worn.put(WEAPON_SLOT, weapon);
    }
    if (shield != null) {
      worn.put(SHIELD_SLOT, shield);
    }
    service.learnEquipSlots(wornContainer);
  }

  private static ItemStats equipable(EquipmentInventorySlot slot, boolean twoHanded) {
    return new ItemStats(true, 0, 0, ItemEquipmentStats.builder().slot(slot.getSlotIdx()).isTwoHanded(twoHanded).build());
  }
}
