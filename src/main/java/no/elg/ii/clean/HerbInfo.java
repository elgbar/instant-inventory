package no.elg.ii.clean;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import net.runelite.api.ItemID;

@Data
public class HerbInfo {

  private final int cleanItemId;
  private final int minLevel;

  /**
   * Map of {@link ItemID} from grimy herbs to cleaned herbs
   */
  public static Map<Integer, HerbInfo> HERBS = new HashMap<>();

  static {
    HERBS.put(ItemID.GRIMY_ROGUES_PURSE, new HerbInfo(ItemID.ROGUES_PURSE, 3));
    HERBS.put(ItemID.GRIMY_SNAKE_WEED, new HerbInfo(ItemID.SNAKE_WEED, 3));
    HERBS.put(ItemID.GRIMY_ARDRIGAL, new HerbInfo(ItemID.ARDRIGAL, 3));
    HERBS.put(ItemID.GRIMY_SITO_FOIL, new HerbInfo(ItemID.SITO_FOIL, 3));
    HERBS.put(ItemID.GRIMY_VOLENCIA_MOSS, new HerbInfo(ItemID.VOLENCIA_MOSS, 3));
    HERBS.put(ItemID.GRIMY_GUAM_LEAF, new HerbInfo(ItemID.GUAM_LEAF, 3));
    HERBS.put(ItemID.GRIMY_MARRENTILL, new HerbInfo(ItemID.MARRENTILL, 5));
    HERBS.put(ItemID.GRIMY_TARROMIN, new HerbInfo(ItemID.TARROMIN, 11));
    HERBS.put(ItemID.GRIMY_HARRALANDER, new HerbInfo(ItemID.HARRALANDER, 20));
    HERBS.put(ItemID.GRIMY_RANARR_WEED, new HerbInfo(ItemID.RANARR_WEED, 25));
    HERBS.put(ItemID.GRIMY_IRIT_LEAF, new HerbInfo(ItemID.IRIT_LEAF, 40));
    HERBS.put(ItemID.GRIMY_AVANTOE, new HerbInfo(ItemID.AVANTOE, 48));
    HERBS.put(ItemID.GRIMY_KWUARM, new HerbInfo(ItemID.KWUARM, 54));
    HERBS.put(ItemID.GRIMY_SNAPDRAGON, new HerbInfo(ItemID.SNAPDRAGON, 59));
    HERBS.put(ItemID.GRIMY_CADANTINE, new HerbInfo(ItemID.CADANTINE, 65));
    HERBS.put(ItemID.GRIMY_DWARF_WEED, new HerbInfo(ItemID.DWARF_WEED, 70));
    HERBS.put(ItemID.GRIMY_TORSTOL, new HerbInfo(ItemID.TORSTOL, 75));
    HERBS.put(ItemID.GRIMY_LANTADYME, new HerbInfo(ItemID.LANTADYME, 67));
    HERBS.put(ItemID.GRIMY_TOADFLAX, new HerbInfo(ItemID.TOADFLAX, 30));

    // Chambers of Xeric herbs
    HERBS.put(ItemID.GRIMY_GOLPAR, new HerbInfo(ItemID.GOLPAR, 47));
    HERBS.put(ItemID.GRIMY_BUCHU_LEAF, new HerbInfo(ItemID.BUCHU_LEAF, 52));
    HERBS.put(ItemID.GRIMY_NOXIFER, new HerbInfo(ItemID.NOXIFER, 60));
  }


}
