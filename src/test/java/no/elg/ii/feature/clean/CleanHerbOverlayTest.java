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

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.cache.Cache;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import net.runelite.api.ItemID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.util.AsyncBufferedImage;
import no.elg.ii.inventory.InventoryState;
import no.elg.ii.inventory.slot.InventorySlot;
import no.elg.ii.test.TestSetup;
import org.junit.Test;

public class CleanHerbOverlayTest {

  @SuppressWarnings("unchecked")
  @Test
  public void invalidateCache_invalidates_fillCache() {
    CleanHerbOverlay overlay = spy(new CleanHerbOverlay());
    Cache<Long, Image> cache = mock(Cache.class);
    overlay.fillCache = cache;

    overlay.invalidateCache();

    verify(cache).invalidateAll();
  }

  @Test
  public void renderItemOverlay_happy_path() {
    renderItemOverlay_test(ItemID.GRIMY_GUAM_LEAF, false, true);
  }

  @Test
  public void renderItemOverlay_not_grimy_herb() {
    renderItemOverlay_test(ItemID.INFERNAL_CAPE, false, false);
  }

  @Test
  public void renderItemOverlay_invalid_state_does_not_draw() {
    renderItemOverlay_test(ItemID.GRIMY_GUAM_LEAF, true, false);
  }

  public void renderItemOverlay_test(int itemId, boolean invalidState, boolean shouldDraw) {
    int index = 1;

    CleanHerbOverlay overlay = spy(new CleanHerbOverlay());
    ItemManager itemManager = overlay.itemManager = mock(ItemManager.class);
    CleanHerbFeature clean = overlay.clean = TestSetup.createNewCleanHerbFeature();
    InventoryState state = mock(InventoryState.class);
    InventorySlot slot = mock(InventorySlot.class);
    doReturn(state).when(clean).getState();
    doReturn(slot).when(state).getSlot(index);
    doReturn(!invalidState).when(slot).hasValidItemId();

    Graphics2D graphics = mock(Graphics2D.class);
    WidgetItem widgetItem = mock(WidgetItem.class);
    Widget widget = mock(Widget.class);
    doReturn(widget).when(widgetItem).getWidget();
    doReturn(index).when(widget).getIndex();

    doReturn(mock(Rectangle.class)).when(widgetItem).getCanvasBounds();
    AsyncBufferedImage image = mock(AsyncBufferedImage.class);
    doReturn(image).when(itemManager).getImage(anyInt(), anyInt(), anyBoolean());

    overlay.renderItemOverlay(graphics, itemId, widgetItem);

    verify(graphics, times(shouldDraw ? 1 : 0)).drawImage(eq(image), anyInt(), anyInt(), eq(null));
  }
}
