/*
 * Copyright (c) 2023-2026 Elg
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

package no.elg.ii.feature.features;

import static net.runelite.api.gameval.ItemID.TZHAAR_CAPE_FIRE;
import static net.runelite.api.gameval.ItemID.UNIDENTIFIED_GUAM;
import static no.elg.ii.feature.features.CleanHerbFeature.CLEAN_CONFIG_KEY;
import static no.elg.ii.feature.features.CleanHerbFeature.CLEAN_OPTION;
import static no.elg.ii.inventory.slot.InventorySlot.INVALID_ITEM_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import net.runelite.api.MenuEntry;
import net.runelite.api.Skill;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import no.elg.ii.model.HerbInfo;
import no.elg.ii.test.StatefulFeatureTestMother;
import no.elg.ii.test.TestSetup;
import org.junit.Test;

public class CleanHerbFeatureTest extends StatefulFeatureTestMother<CleanHerbFeature> {

  private static final int INDEX = 1;
  private static final int MAX_LEVEL = 99;

  @Override
  public CleanHerbFeature createNewInstance() {
    return TestSetup.createNewCleanHerbFeature();
  }

  @Test
  public void configKey_is_CLEAN_CONFIG_KEY() {
    CleanHerbFeature feature = createNewInstance();
    assertEquals(CLEAN_CONFIG_KEY, feature.getConfigKey());
  }

  @Test
  public void onMenuOptionClicked_happy_path() {
    CleanHerbFeature feature = createNewInstance();
    Widget widget = mock(Widget.class);
    doReturn(INDEX).when(widget).getIndex();
    MenuEntry menuEntry = menuEntry(CLEAN_OPTION, widget);
    doReturn(UNIDENTIFIED_GUAM).when(menuEntry).getItemId();
    doReturn(MAX_LEVEL).when(feature.client).getBoostedSkillLevel(Skill.HERBLORE);

    click(feature, menuEntry);

    // The slot holds what the widget should show, which is the cleaned herb
    assertEquals(HerbInfo.HERBS.get(UNIDENTIFIED_GUAM).getCleanItemId(), feature.getState().getSlot(INDEX).getItemId());
  }

  @Test
  public void onMenuOptionClicked_no_widget() {
    CleanHerbFeature feature = createNewInstance();
    MenuEntry menuEntry = menuEntry(CLEAN_OPTION, null);

    click(feature, menuEntry);

    assertSlotUntouched(feature);
  }

  @Test
  public void onMenuOptionClicked_not_clean_option() {
    CleanHerbFeature feature = createNewInstance();
    MenuEntry menuEntry = menuEntry("not clean", mock(Widget.class));

    click(feature, menuEntry);

    assertSlotUntouched(feature);
  }

  @Test
  public void onMenuOptionClicked_not_a_herb() {
    CleanHerbFeature feature = createNewInstance();
    MenuEntry menuEntry = menuEntry(CLEAN_OPTION, mock(Widget.class));
    doReturn(TZHAAR_CAPE_FIRE).when(menuEntry).getItemId();

    click(feature, menuEntry);

    assertSlotUntouched(feature);
  }

  @Test
  public void onMenuOptionClicked_too_low_level() {
    CleanHerbFeature feature = createNewInstance();
    MenuEntry menuEntry = menuEntry(CLEAN_OPTION, mock(Widget.class));
    doReturn(UNIDENTIFIED_GUAM).when(menuEntry).getItemId();
    doReturn(1).when(feature.client).getBoostedSkillLevel(Skill.HERBLORE);

    click(feature, menuEntry);

    assertSlotUntouched(feature);
  }

  /**
   * Each test stubs only what its code path reads, as the strict runner requires.
   *
   * @param widget The widget the entry was clicked on, {@code null} when there is none. A mock
   *               returns {@code null} by default, and without a widget the option is never read.
   */
  private static MenuEntry menuEntry(String option, Widget widget) {
    MenuEntry menuEntry = mock(MenuEntry.class);
    if (widget != null) {
      doReturn(widget).when(menuEntry).getWidget();
      doReturn(option).when(menuEntry).getOption();
    }
    return menuEntry;
  }

  private static void click(CleanHerbFeature feature, MenuEntry menuEntry) {
    assertFalse(feature.getState().getSlot(INDEX).hasValidItemId());
    feature.onMenuOptionClicked(new MenuOptionClicked(menuEntry));
  }

  private static void assertSlotUntouched(CleanHerbFeature feature) {
    assertEquals(INVALID_ITEM_ID, feature.getState().getSlot(INDEX).getItemId());
  }
}
