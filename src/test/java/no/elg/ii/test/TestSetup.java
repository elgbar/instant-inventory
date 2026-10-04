/*
 * Copyright (c) 2023-2026 Elg
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

package no.elg.ii.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.withSettings;

import java.util.function.BooleanSupplier;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import no.elg.ii.InstantInventoryConfig;
import no.elg.ii.InstantInventoryPlugin;
import no.elg.ii.feature.HideFeature;
import no.elg.ii.feature.features.CleanHerbFeature;
import no.elg.ii.feature.features.DepositFeature;
import no.elg.ii.feature.features.DropFeature;
import no.elg.ii.feature.features.EquipFeature;
import no.elg.ii.feature.features.PrayerFeature;
import no.elg.ii.feature.features.SpecialAttackFeature;
import no.elg.ii.feature.features.WithdrawFeature;
import no.elg.ii.feature.state.InventoryState;
import no.elg.ii.service.InventoryService;
import no.elg.ii.service.WidgetService;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

public class TestSetup {

  public static CleanHerbFeature createNewCleanHerbFeature() {
    CleanHerbFeature feature = spy(new CleanHerbFeature());
    feature.client = mockClient();
    feature.widgetService = mock(WidgetService.class);
    feature.state = newInventoryState(feature.client);
    return feature;
  }

  public static DropFeature createNewDropFeature() {
    DropFeature feature = spy(new DropFeature());
    setupHideFeature(feature);
    return feature;
  }

  public static DepositFeature createNewDepositFeature() {
    DepositFeature feature = spy(new DepositFeature());
    setupHideFeature(feature);
    return feature;
  }

  public static EquipFeature createNewEquipFeature() {
    return spy(new EquipFeature());
  }

  public static WithdrawFeature createNewWithdrawFeature() {
    return spy(new WithdrawFeature());
  }

  public static PrayerFeature createNewInstantPrayer() {
    return spy(new PrayerFeature());
  }

  public static SpecialAttackFeature createNewSpecFeature() {
    return spy(new SpecialAttackFeature());
  }

  /**
   * A mocked {@link Client} for which the thread calling this method, normally the JUnit thread, is
   * the client thread. {@link Client#isClientThread()} is false on every other thread, so
   * {@code assert client.isClientThread()} keeps catching mistakes in tests.
   * <p>
   * This is part of how the mock is defined rather than a stubbing, so strict stubs do not complain
   * in tests that never reach such an assert.
   */
  public static Client mockClient() {
    Thread clientThread = Thread.currentThread();
    Answer<Object> clientThreadAware = invocation -> {
      if ("isClientThread".equals(invocation.getMethod().getName())) {
        return Thread.currentThread() == clientThread;
      }
      return Mockito.RETURNS_DEFAULTS.answer(invocation);
    };
    return mock(Client.class, withSettings().defaultAnswer(clientThreadAware));
  }

  /**
   * A {@link ClientThread} that runs everything immediately. Tests are single threaded and the
   * JUnit thread is the client thread, so later collapses to now.
   */
  public static ClientThread inlineClientThread() {
    return new JunitClientThread();
  }

  public static InventoryState newInventoryState(Client client) {
    InstantInventoryConfig config = spy(new InstantInventoryConfig() {
    });
    return new InventoryState(config, client, mock(InventoryService.class), mock(WidgetService.class));
  }

  private static void setupHideFeature(HideFeature feature) {
    feature.client = mockClient();
    feature.state = newInventoryState(feature.client);
    feature.clientThread = inlineClientThread();
    feature.widgetService = mock(WidgetService.class);
    feature.plugin = mock(InstantInventoryPlugin.class);
  }

  private static class JunitClientThread extends ClientThread {
    @Override
    public void invoke(Runnable r) {
      r.run();
    }

    @Override
    public void invoke(BooleanSupplier r) {
      r.getAsBoolean();
    }

    @Override
    public void invokeLater(Runnable r) {
      r.run();
    }

    @Override
    public void invokeLater(BooleanSupplier r) {
      r.getAsBoolean();
    }

    @Override
    public void invokeAtTickEnd(Runnable r) {
      r.run();
    }
  }
}
