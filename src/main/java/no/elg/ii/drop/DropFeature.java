/*
 * Copyright (c) 2022 Elg
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package no.elg.ii.drop;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import no.elg.ii.Feature;
import no.elg.ii.InstantInventoryPlugin;
import no.elg.ii.InventoryState;

@Singleton
@Slf4j
public class DropFeature implements Feature {

  public static final String DROP_OPTION = "Drop";
  public static final String DROP_CONFIG_KEY = "instantDrop";
  private final InventoryState state = new InventoryState();

  @Inject
  @VisibleForTesting
  public InstantInventoryPlugin plugin;

  @Inject
  @VisibleForTesting
  public ClientThread clientThread;

  /* (non-javadoc)
   * Make sure the item in the slot is hidden, the client sets it as non-hidden each tick (?)
   *  or so. This must be done before the client is rendered otherwise (such as if we were to use
   *  the ClientTick event) the item would be visible for a single frame.
   */
  @Subscribe
  public void onBeforeRender(BeforeRender beforeRender) {
    updateHiddenStatus();
  }

  @Override
  public void reset() {
    Feature.super.reset();
    clientThread.invoke(this::updateHiddenStatus);
  }

  @Subscribe
  public void onMenuOptionClicked(final MenuOptionClicked event) {
    Widget widget = event.getWidget();
    if (widget != null) {
      String menuOption = event.getMenuOption();
      if (DROP_OPTION.equals(menuOption)) {
        log.debug("Dropped item at index {}", widget.getIndex());
        state.setItemId(widget.getIndex(), event.getItemId());
      }
    }
  }

  @VisibleForTesting
  protected void updateHiddenStatus() {
    Widget[] inventoryWidgetItem = plugin.inventoryItems();
    for (int index = 0; index < inventoryWidgetItem.length; index++) {

      //Only hide the item when the state has been set to a valid item id
      boolean shouldBeHidden = state.isValid(index);

      Widget widget = inventoryWidgetItem[index];
      if (shouldBeHidden && !widget.isSelfHidden()) {
        log.debug("Hiding item at index {}", index);
        widget.setHidden(true);
      } else if (!shouldBeHidden && widget.isSelfHidden()) {
        log.debug("Showing item at index {}", index);
        widget.setHidden(false);
      }
    }
  }

  @Nonnull
  @Override
  public String getConfigKey() {
    return DROP_CONFIG_KEY;
  }

  @Nonnull
  @Override
  public InventoryState getState() {
    return state;
  }
}
