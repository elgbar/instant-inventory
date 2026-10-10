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

import static org.junit.Assert.assertEquals;

import no.elg.ii.InstantInventoryConfig;
import no.elg.ii.inventory.slot.SlotChange;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class WidgetServiceTest {

  @Spy
  private InstantInventoryConfig config = new InstantInventoryConfig() {
    // Added and changed items have the same opacity by default, so mixing them up would go unnoticed
    @Override
    public int fillOpacityPercent() {
      return 50;
    }
  };
  @InjectMocks
  private WidgetService widgetService;

  @Test
  public void getOpacity_emptying_slot_uses_hide_opacity() {
    assertEquals(widgetService.getHideOpacity(), widgetService.getOpacity(SlotChange.EMPTY));
  }

  @Test
  public void getOpacity_replaced_slot_uses_change_opacity() {
    assertEquals(widgetService.getChangeOpacity(), widgetService.getOpacity(SlotChange.REPLACE));
  }

  @Test
  public void getOpacity_filling_slot_uses_fill_opacity() {
    assertEquals(widgetService.getFillOpacity(), widgetService.getOpacity(SlotChange.FILL));
  }
}
