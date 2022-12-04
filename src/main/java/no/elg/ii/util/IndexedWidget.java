package no.elg.ii.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.api.widgets.Widget;

@Data
@AllArgsConstructor
public class IndexedWidget implements Comparable<IndexedWidget> {

  int index;
  Widget widget;

  @Override
  public int compareTo(IndexedWidget o) {
    return Integer.compare(index, o.index);
  }
}
