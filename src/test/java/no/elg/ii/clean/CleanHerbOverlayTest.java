package no.elg.ii.clean;

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
import no.elg.ii.InventoryState;
import no.elg.ii.ResetStaticMother;
import no.elg.ii.test.TestSetup;
import org.junit.Test;

public class CleanHerbOverlayTest extends ResetStaticMother {

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
    CleanHerbFeature clean = overlay.clean = spy(TestSetup.createNewCleanHerbFeature());
    InventoryState state = mock(InventoryState.class);
    doReturn(state).when(clean).getState();
    doReturn(invalidState).when(state).isInvalid(index);

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
