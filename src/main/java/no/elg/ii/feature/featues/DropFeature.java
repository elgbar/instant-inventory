/*
 * Copyright (c) 2022-2025 Elg
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
package no.elg.ii.feature.featues;

import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import no.elg.ii.feature.HideFeature;
import no.elg.ii.service.VarService;
import no.elg.ii.util.WidgetUtils;

@Slf4j
@Singleton
@NoArgsConstructor
public class DropFeature extends HideFeature {

  public static final String DROP_OPTION = "Drop";
  public static final String DROP_CONFIG_KEY = "instantDrop";

  @Inject
  private ItemManager itemManager;

  @Inject
  private VarService varService;

  @Subscribe
  public void onMenuOptionClicked(final MenuOptionClicked event) {
    Widget widget = event.getWidget();
    if (widget != null) {
      String menuOption = event.getMenuOption();
      if (DROP_OPTION.equals(menuOption)) {
        log.debug("Dropped item {}", WidgetUtils.debugInfo(widget));
        if (willDropWarningBeShownForItem(widget.getItemId(), widget.getItemQuantity())) {
          log.debug("Drop warning will be shown, will not hide item");
        } else {
          hide(widget);
        }
      }
    }
  }

  public boolean willDropWarningBeShownForItem(int itemId, int quantity) {
    if (varService.isVarbitFalse(VarbitID.OPTION_DROPWARNING_ON)) {
      return false;
    }
    var canonItemId = itemManager.canonicalize(itemId);
    var price = itemManager.getItemPriceWithSource(canonItemId, false);
    return varService.varbitValue(VarbitID.OPTION_DROPWARNING_VALUE) < price * quantity;
  }

  @Override
  public @NonNull String getConfigKey() {
    return DROP_CONFIG_KEY;
  }
}
