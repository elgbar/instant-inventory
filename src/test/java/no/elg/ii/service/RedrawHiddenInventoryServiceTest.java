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

package no.elg.ii.service;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.runelite.api.ItemContainer;
import net.runelite.api.ScriptEvent;
import net.runelite.api.ScriptEventBuilder;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.widgets.Widget;
import no.elg.ii.InstantInventoryConfig;
import no.elg.ii.test.TestSetup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class RedrawHiddenInventoryServiceTest {

  /**
   * The listener {@code inventory_init} gives the inventory, i.e., {@code inventory_build(inventory:items, 28, 1, null)}
   */
  private static final Object[] INVENTORY_BUILD_LISTENER = {6009, InterfaceID.Inventory.ITEMS, 28, 1, -1};

  /**
   * The widgets of the interfaces the client has loaded, by component id
   */
  private final Map<Integer, Widget> loadedWidgets = new HashMap<>();
  private final List<ScriptEvent> ranScripts = new ArrayList<>();
  private boolean redrawHiddenInventories = true;

  private RedrawHiddenInventoryService service;

  @Before
  public void setUp() {
    service = new RedrawHiddenInventoryService();
    service.client = TestSetup.mockClient(this::answerAsClient);
    service.config = new InstantInventoryConfig() {
      @Override
      public boolean redrawHiddenInventories() {
        return redrawHiddenInventories;
      }
    };
  }

  @Test
  public void onItemContainerChanged_redraws_the_inventory_hidden_by_the_bank() {
    Widget inventory = inventoryWidget(true, INVENTORY_BUILD_LISTENER);
    loadedWidgets.put(InterfaceID.Inventory.ITEMS, inventory);

    service.onItemContainerChanged(inventoryChanged(InventoryID.INV));

    assertEquals(1, ranScripts.size());
    assertArrayEquals(INVENTORY_BUILD_LISTENER, ranScripts.get(0).getArguments());
    assertSame(inventory, ranScripts.get(0).getSource());
  }

  @Test
  public void onItemContainerChanged_leaves_a_visible_inventory_to_the_client() {
    loadedWidgets.put(InterfaceID.Inventory.ITEMS, inventoryWidget(false, INVENTORY_BUILD_LISTENER));

    service.onItemContainerChanged(inventoryChanged(InventoryID.INV));

    assertTrue(ranScripts.isEmpty());
  }

  @Test
  public void onItemContainerChanged_ignores_other_item_containers() {
    loadedWidgets.put(InterfaceID.Inventory.ITEMS, inventoryWidget(true, INVENTORY_BUILD_LISTENER));

    service.onItemContainerChanged(inventoryChanged(InventoryID.BANK));

    assertTrue(ranScripts.isEmpty());
  }

  @Test
  public void onItemContainerChanged_skips_hidden_inventory_without_transmit_listener() {
    loadedWidgets.put(InterfaceID.Inventory.ITEMS, inventoryWidget(true, null));

    service.onItemContainerChanged(inventoryChanged(InventoryID.INV));

    assertTrue(ranScripts.isEmpty());
  }

  @Test
  public void onItemContainerChanged_does_nothing_when_disabled() {
    redrawHiddenInventories = false;
    loadedWidgets.put(InterfaceID.Inventory.ITEMS, inventoryWidget(true, INVENTORY_BUILD_LISTENER));

    service.onItemContainerChanged(inventoryChanged(InventoryID.INV));

    assertTrue(ranScripts.isEmpty());
  }

  private static ItemContainerChanged inventoryChanged(int containerId) {
    return new ItemContainerChanged(containerId, mock(ItemContainer.class));
  }

  private static Widget inventoryWidget(boolean hidden, @Nullable Object[] onInvTransmitListener) {
    return mock(Widget.class, withSettings().defaultAnswer(invocation -> {
      switch (invocation.getMethod().getName()) {
        case "isHidden":
          return hidden;
        case "getOnInvTransmitListener":
          return onInvTransmitListener;
        default:
          return RETURNS_DEFAULTS.answer(invocation);
      }
    }));
  }

  /**
   * Answers like the client for the widgets in {@link #loadedWidgets}, and records the scripts run in
   * {@link #ranScripts}
   */
  private Object answerAsClient(InvocationOnMock invocation) throws Throwable {
    String method = invocation.getMethod().getName();
    if ("getWidget".equals(method) && invocation.getArguments().length == 1) {
      return loadedWidgets.get(invocation.<Integer>getArgument(0));
    } else if ("createScriptEventBuilder".equals(method)) {
      return new FakeScriptEventBuilder(invocation.getArguments());
    }
    return RETURNS_DEFAULTS.answer(invocation);
  }

  private class FakeScriptEventBuilder implements ScriptEventBuilder {
    private Object[] arguments;
    private Widget source;
    private Widget target;
    private int op;

    FakeScriptEventBuilder(Object[] arguments) {
      this.arguments = arguments;
    }

    @Override
    public Object[] getArguments() {
      return arguments;
    }

    @Override
    public ScriptEventBuilder setArguments(Object[] arguments) {
      this.arguments = arguments;
      return this;
    }

    @Override
    public Widget getSource() {
      return source;
    }

    @Override
    public ScriptEventBuilder setSource(Widget widget) {
      this.source = widget;
      return this;
    }

    @Nullable
    @Override
    public Widget getTarget() {
      return target;
    }

    @Override
    public ScriptEventBuilder setTarget(Widget target) {
      this.target = target;
      return this;
    }

    @Override
    public int getOp() {
      return op;
    }

    @Override
    public ScriptEventBuilder setOp(int op) {
      this.op = op;
      return this;
    }

    @Override
    public ScriptEvent build() {
      return new FakeScriptEvent(arguments, source, target, op);
    }
  }

  private class FakeScriptEvent implements ScriptEvent {
    private final Object[] arguments;
    private final Widget source;
    private final Widget target;
    private final int op;

    FakeScriptEvent(Object[] arguments, Widget source, Widget target, int op) {
      this.arguments = arguments;
      this.source = source;
      this.target = target;
      this.op = op;
    }

    @Override
    public Widget getSource() {
      return source;
    }

    @Nullable
    @Override
    public Widget getTarget() {
      return target;
    }

    @Override
    public Object[] getArguments() {
      return arguments;
    }

    @Override
    public int getOp() {
      return op;
    }

    @Override
    public String getOpbase() {
      return "";
    }

    @Override
    public int getMouseX() {
      return 0;
    }

    @Override
    public int getMouseY() {
      return 0;
    }

    @Override
    public int getTypedKeyCode() {
      return 0;
    }

    @Override
    public int getTypedKeyChar() {
      return 0;
    }

    @Override
    public ScriptEvent setCanSendPackets(boolean canSendPackets) {
      return this;
    }

    @Override
    public void run() {
      ranScripts.add(this);
    }
  }
}
