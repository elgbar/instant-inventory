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

package no.elg.ii.overlay;

import static no.elg.ii.overlay.ItemOpacityOverlays.lerpChannels;
import static no.elg.ii.util.WidgetUtils.FULLY_OPAQUE;
import static no.elg.ii.util.WidgetUtils.FULLY_TRANSPARENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import net.runelite.api.BufferProvider;
import net.runelite.api.Client;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPosition;
import no.elg.ii.feature.state.InventoryState;
import no.elg.ii.model.IndexedWidget;
import no.elg.ii.service.InventoryService;
import no.elg.ii.test.TestSetup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ItemOpacityOverlaysTest {

  private static final int BACKGROUND = 0xFF102030;
  private static final int TAG = 0xFFFF0000;
  private static final int FADED_OPACITY = 204;
  private static final int INDEX = 5;
  private static final int ITEM_ID = 1;

  /**
   * An item with room around it in {@link #frame} for overlays drawn outside it
   */
  private static final Rectangle ITEM_BOUNDS = new Rectangle(2, 2, 4, 3);

  /**
   * {@link #ITEM_BOUNDS} and the row below it Inventory Tags draws its underline on, from the left edge of the item to
   * one pixel past its right edge
   */
  private static final Rectangle ITEM_AND_UNDERLINE = new Rectangle(2, 2, 5, 6);

  private final FrameBuffer frame = new FrameBuffer(10, 10);

  @Mock
  private InventoryService inventoryService;
  @Mock
  private OverlayManager overlayManager;

  private Client client;
  private InventoryState state;
  private ItemOpacityOverlays itemOpacityOverlays;
  private Overlay captureOverlay;
  private Overlay blendOverlay;

  @Before
  public void setUp() {
    client = TestSetup.mockClient();
    state = TestSetup.newInventoryState(client);
    itemOpacityOverlays = new ItemOpacityOverlays(client, overlayManager, inventoryService, state);

    itemOpacityOverlays.startUp();
    ArgumentCaptor<Overlay> added = ArgumentCaptor.forClass(Overlay.class);
    verify(overlayManager, times(2)).add(added.capture());
    captureOverlay = added.getAllValues().get(0);
    blendOverlay = added.getAllValues().get(1);
  }

  /**
   * Render a frame where another plugin draws {@link #TAG} over every pixel
   */
  private void renderFrame(WidgetItem widgetItem) {
    doReturn(List.of(widgetItem)).when(overlayManager).getWidgetItems();
    captureOverlay.render(null);
    frame.fill(TAG);
    blendOverlay.render(null);
  }

  private void openInventory(IndexedWidget... slotWidgets) {
    doAnswer(invocation -> Stream.of(slotWidgets)).when(inventoryService).getAllOpenInventoryWidgets();
  }

  private Widget predictedWidget(int opacity) {
    Widget widget = mock(Widget.class);
    doReturn(opacity).when(widget).getOpacity();
    state.setSlot(INDEX, ITEM_ID, 1);
    openInventory(new IndexedWidget(INDEX, widget));
    doReturn(frame).when(client).getBufferProvider();
    return widget;
  }

  private void assertFrame(Rectangle fadedArea, int fadedPixel) {
    for (int y = 0; y < frame.getHeight(); y++) {
      for (int x = 0; x < frame.getWidth(); x++) {
        int expected = fadedArea.contains(x, y) ? fadedPixel : TAG;
        assertEquals("Pixel at (" + x + ", " + y + ")", Integer.toHexString(expected), Integer.toHexString(frame.get(x, y)));
      }
    }
  }

  @Test
  public void overlays_wrap_the_item_overlays_drawn_after_the_inventory() {
    int afterInventory = InterfaceID.INVENTORY << 16 | 0xffff;
    for (Overlay overlay : List.of(captureOverlay, blendOverlay)) {
      assertTrue(overlay.getDrawHooks().contains(afterInventory));
      assertEquals(OverlayLayer.MANUAL, overlay.getLayer());
      assertEquals(OverlayPosition.DYNAMIC, overlay.getPosition());
    }
    assertTrue(captureOverlay.getPriority() < Overlay.PRIORITY_LOW);
    assertTrue(blendOverlay.getPriority() > Overlay.PRIORITY_HIGHEST);
  }

  @Test
  public void overlays_drawn_on_a_faded_item_get_its_opacity() {
    Widget widget = predictedWidget(FADED_OPACITY);

    renderFrame(new WidgetItem(ITEM_ID, 1, ITEM_BOUNDS, widget, null));

    assertFrame(ITEM_AND_UNDERLINE, lerpChannels(BACKGROUND, TAG, FULLY_TRANSPARENT - FADED_OPACITY));
  }

  @Test
  public void overlays_drawn_on_a_fully_transparent_item_are_erased() {
    Widget widget = predictedWidget(FULLY_TRANSPARENT);

    renderFrame(new WidgetItem(ITEM_ID, 1, ITEM_BOUNDS, widget, null));

    assertFrame(ITEM_AND_UNDERLINE, BACKGROUND);
  }

  @Test
  public void overlays_drawn_on_a_fully_opaque_item_are_kept() {
    Widget widget = predictedWidget(FULLY_OPAQUE);

    renderFrame(new WidgetItem(ITEM_ID, 1, ITEM_BOUNDS, widget, null));

    assertFrame(new Rectangle(), TAG);
  }

  @Test
  public void overlays_drawn_on_a_faded_item_not_predicted_by_this_plugin_are_kept() {
    Widget predicted = mock(Widget.class);
    Widget fadedByTheGame = mock(Widget.class);
    doReturn(FADED_OPACITY).when(fadedByTheGame).getOpacity();
    state.setSlot(INDEX, ITEM_ID, 1);
    openInventory(new IndexedWidget(INDEX, predicted), new IndexedWidget(INDEX + 1, fadedByTheGame));
    doReturn(frame).when(client).getBufferProvider();

    renderFrame(new WidgetItem(ITEM_ID, 1, ITEM_BOUNDS, fadedByTheGame, null));

    assertFrame(new Rectangle(), TAG);
  }

  @Test
  public void frame_is_not_read_when_nothing_is_predicted() {
    // The client has no frame buffer, so reading it would throw
    renderFrame(new WidgetItem(ITEM_ID, 1, ITEM_BOUNDS, mock(Widget.class), null));

    assertFrame(new Rectangle(), TAG);
  }

  @Test
  public void item_partly_outside_the_frame_fades_the_part_inside() {
    Widget widget = predictedWidget(FADED_OPACITY);

    renderFrame(new WidgetItem(ITEM_ID, 1, new Rectangle(8, 8, 4, 3), widget, null));

    assertFrame(new Rectangle(8, 8, 2, 2), lerpChannels(BACKGROUND, TAG, FULLY_TRANSPARENT - FADED_OPACITY));
  }

  @Test
  public void shutDown_removes_both_overlays() {
    itemOpacityOverlays.shutDown();

    verify(overlayManager).remove(captureOverlay);
    verify(overlayManager).remove(blendOverlay);
  }

  @Test
  public void lerpChannels_with_no_fraction_is_from() {
    assertEquals(BACKGROUND, lerpChannels(BACKGROUND, TAG, 0));
  }

  @Test
  public void lerpChannels_with_full_fraction_is_to() {
    assertEquals(TAG, lerpChannels(BACKGROUND, TAG, 0xFF));
  }

  @Test
  public void lerpChannels_interpolates_each_channel_on_its_own() {
    assertEquals(0x80808080, lerpChannels(0x00000000, 0xFFFFFFFF, 0x80));
    assertEquals(0x7F7F7F7F, lerpChannels(0xFFFFFFFF, 0x00000000, 0x80));
  }

  /**
   * Pixels in rows, like the frame buffer of the client
   */
  private static class FrameBuffer implements BufferProvider {
    private final int width;
    private final int height;
    private final int[] pixels;

    FrameBuffer(int width, int height) {
      this.width = width;
      this.height = height;
      this.pixels = new int[width * height];
      fill(BACKGROUND);
    }

    void fill(int pixel) {
      Arrays.fill(pixels, pixel);
    }

    int get(int x, int y) {
      return pixels[y * width + x];
    }

    @Override
    public int[] getPixels() {
      return pixels;
    }

    @Override
    public int getWidth() {
      return width;
    }

    @Override
    public int getHeight() {
      return height;
    }
  }
}
