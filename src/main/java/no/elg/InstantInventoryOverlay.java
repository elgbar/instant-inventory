package no.elg;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.awt.*;
import java.util.Collection;
import javax.inject.Inject;

import lombok.AccessLevel;
import lombok.Setter;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.ImageUtil;

import static net.runelite.api.widgets.WidgetID.INVENTORY_GROUP_ID;

public class InstantInventoryOverlay extends WidgetItemOverlay {

  @Setter(AccessLevel.PACKAGE)
  private OverlayManager overlayManager;

  private final ItemManager itemManager;
  private final InstantInventoryPlugin plugin;
  private final Cache<Long, Image> fillCache;

  @Inject
  public InstantInventoryOverlay(ItemManager itemManager, InstantInventoryPlugin plugin) {
    this.itemManager = itemManager;
    this.plugin = plugin;
    showOnInterfaces(INVENTORY_GROUP_ID);
    fillCache = CacheBuilder.newBuilder()
        .concurrencyLevel(1)
        .maximumSize(32)
        .build();
    System.out.println("overlay created");
  }

  @Override
  public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem) {
    int index = widgetItem.getWidget().getIndex();
    if(index < 0 || index >= 28){
      return;
    }

    int hiddenItemId = plugin.hide[index];
    if(hiddenItemId < 0){
      return;
    }
    System.out.println("hidden "+index+"(item id) "+hiddenItemId+" curr item id "+itemId);

    if (hiddenItemId == itemId) {
      Rectangle bounds = widgetItem.getCanvasBounds();
      graphics.setColor(new Color(0,0,0,155));
      graphics.clearRect(bounds.x, bounds.y, bounds.width, bounds.height);
      graphics.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
      graphics.setBackground(new Color(0,0,0,255));
    }else {
      plugin.hide[index] = -1;
    }
  }

  @Override
  public Dimension render(Graphics2D graphics)
  {
    final Collection<WidgetItem> widgetItems = overlayManager.getWidgetItems();
    final Rectangle originalClipBounds = graphics.getClipBounds();
    Widget curClipParent = null;
    for (WidgetItem widgetItem : widgetItems)
    {
      Widget widget = widgetItem.getWidget();
      Widget parent = widget.getParent();
      Rectangle parentBounds = parent.getBounds();
      Rectangle itemCanvasBounds = widgetItem.getCanvasBounds();
      boolean dragging = widgetItem.getDraggingCanvasBounds() != null;

      boolean shouldClip;
      if (dragging)
      {
        // If dragging, clip if the dragged item is outside of the parent bounds
        shouldClip = itemCanvasBounds.x < parentBounds.x;
        shouldClip |= itemCanvasBounds.x + itemCanvasBounds.width >= parentBounds.x + parentBounds.width;
        shouldClip |= itemCanvasBounds.y < parentBounds.y;
        shouldClip |= itemCanvasBounds.y + itemCanvasBounds.height >= parentBounds.y + parentBounds.height;
      }
      else
      {
        // Otherwise, we only need to clip the overlay if it intersects the parent bounds,
        // since items completely outside of the parent bounds are not drawn
        shouldClip = itemCanvasBounds.y < parentBounds.y && itemCanvasBounds.y + itemCanvasBounds.height >= parentBounds.y;
        shouldClip |= itemCanvasBounds.y < parentBounds.y + parentBounds.height && itemCanvasBounds.y + itemCanvasBounds.height >= parentBounds.y + parentBounds.height;
        shouldClip |= itemCanvasBounds.x < parentBounds.x && itemCanvasBounds.x + itemCanvasBounds.width >= parentBounds.x;
        shouldClip |= itemCanvasBounds.x < parentBounds.x + parentBounds.width && itemCanvasBounds.x + itemCanvasBounds.width >= parentBounds.x + parentBounds.width;
      }
      if (shouldClip)
      {
        if (curClipParent != parent)
        {
          graphics.setClip(parentBounds);
          curClipParent = parent;
        }
      }
      else if (curClipParent != null && curClipParent != parent)
      {
        graphics.setClip(originalClipBounds);
        curClipParent = null;
      }

      renderItemOverlay(graphics, widgetItem.getId(), widgetItem);
    }
    return null;
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
