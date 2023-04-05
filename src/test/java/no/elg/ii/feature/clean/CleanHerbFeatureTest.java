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

package no.elg.ii.feature.clean;

import static net.runelite.api.ItemID.FIRE_CAPE;
import static net.runelite.api.ItemID.GRIMY_GUAM_LEAF;
import static no.elg.ii.InventoryState.INVALID_ITEM_ID;
import static no.elg.ii.feature.clean.CleanHerbFeature.CLEAN_CONFIG_KEY;
import static no.elg.ii.feature.clean.CleanHerbFeature.CLEAN_OPTION;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.OverlayManager;
import no.elg.ii.feature.clean.CleanHerbFeature;
import no.elg.ii.feature.clean.CleanHerbOverlay;
import no.elg.ii.test.FeatureTestMother;
import no.elg.ii.test.TestSetup;
import org.junit.Test;

public class CleanHerbFeatureTest extends FeatureTestMother<CleanHerbFeature> {

  @Override
  public CleanHerbFeature createNewInstance() {
    return TestSetup.createNewCleanHerbFeature();
  }

  @Test
  public void addsOverlayOnEnable() {
    CleanHerbFeature feature = createNewInstance();
    OverlayManager overlayManager = feature.overlayManager;
    CleanHerbOverlay overlay = feature.overlay;

    feature.onEnable();

    verify(overlayManager).add(overlay);
  }

  @Test
  public void removesOverOnDisable() {
    CleanHerbFeature feature = createNewInstance();
    OverlayManager overlayManager = feature.overlayManager;
    CleanHerbOverlay overlay = feature.overlay;

    feature.onDisable();

    verify(overlayManager).remove(overlay);
  }

  @Test
  public void invalidateCacheOnReset() {
    CleanHerbFeature feature = createNewInstance();
    CleanHerbOverlay overlay = feature.overlay;

    feature.reset();

    verify(overlay).invalidateCache();
  }

  @Test
  public void configKey_is_CLEAN_CONFIG_KEY() {
    CleanHerbFeature feature = createNewInstance();
    assertEquals(CLEAN_CONFIG_KEY, feature.getConfigKey());
  }

  @Test
  public void onMenuOptionClicked_happy_path() {
    onMenuOptionClicked_test(GRIMY_GUAM_LEAF, GRIMY_GUAM_LEAF, true, CLEAN_OPTION, 99);
  }

  @Test
  public void onMenuOptionClicked_no_widget() {
    onMenuOptionClicked_test(INVALID_ITEM_ID, GRIMY_GUAM_LEAF, false, CLEAN_OPTION, 99);
  }

  @Test
  public void onMenuOptionClicked_not_clean_option() {
    onMenuOptionClicked_test(INVALID_ITEM_ID, GRIMY_GUAM_LEAF, true, "not clean", 99);
  }

  @Test
  public void onMenuOptionClicked_not_a_herb() {
    onMenuOptionClicked_test(INVALID_ITEM_ID, FIRE_CAPE, true, CLEAN_OPTION, 99);
  }

  @Test
  public void onMenuOptionClicked_too_low_level() {
    onMenuOptionClicked_test(INVALID_ITEM_ID, GRIMY_GUAM_LEAF, true, CLEAN_OPTION, 1);
  }

  private void onMenuOptionClicked_test(int stateItemId, int itemId, boolean hasWidget,
      String menuEntryOption, int level) {
    int index = 1;
    CleanHerbFeature feature = createNewInstance();

    Widget widget = mock(Widget.class);
    doReturn(index).when(widget).getIndex();

    MenuEntry menuEntry = mock(MenuEntry.class);
    doReturn(menuEntryOption).when(menuEntry).getOption();
    doReturn(itemId).when(menuEntry).getItemId();
    doReturn(hasWidget ? widget : null).when(menuEntry).getWidget();
    Client client = feature.client;
    doReturn(level).when(client).getBoostedSkillLevel(any());

    MenuOptionClicked event = new MenuOptionClicked(menuEntry);

    assertEquals(INVALID_ITEM_ID, feature.getState().getItemId(index));
    feature.onMenuOptionClicked(event);
    assertEquals(stateItemId, feature.getState().getItemId(index));
  }
}