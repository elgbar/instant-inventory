package no.elg;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import javax.inject.Inject;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.ImageUtil;

public class InstantInventoryOverlay extends WidgetItemOverlay {

  private final ItemManager itemManager;
  private final InstantInventoryPlugin plugin;
  private final Cache<Long, Image> fillCache;

  @Inject
  public InstantInventoryOverlay(ItemManager itemManager, InstantInventoryPlugin plugin) {
    this.itemManager = itemManager;
    this.plugin = plugin;
    showOnInventory();
    fillCache = CacheBuilder.newBuilder()
        .concurrencyLevel(1)
        .maximumSize(32)
        .build();
    System.out.println("overlay created");
  }

  @Override
  public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem) {
    int index = widgetItem.getWidget().getIndex();
    if (index >= 0 && index < 28 && plugin.hide[index]) {
//    System.out.println("test te2st");
      Rectangle bounds = widgetItem.getCanvasBounds();
//      final Image image = getFillImage(Color.GRAY, widgetItem.getId(), widgetItem.getQuantity());
//      graphics.drawImage(image, (int) bounds.getX(), (int) bounds.getY(), null);
      graphics.setColor(new Color(0,0,0,1));
      graphics.clearRect(bounds.x, bounds.y, bounds.width, bounds.height);
      graphics.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
      graphics.setBackground(new Color(0,0,0,1));
    }
  }

  private Image getFillImage(Color color, int itemId, int qty) {
    long key = (((long) itemId) << 32) | qty;
    Image image = fillCache.getIfPresent(key);
    if (image == null) {
      final Color fillColor = ColorUtil.colorWithAlpha(color, 255);
      image = ImageUtil.fillImage(itemManager.getImage(itemId, qty, false), fillColor);
      fillCache.put(key, image);
    }
    return image;
  }

}
