/*
 * Copyright (c) 2023 Elg
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

import static no.elg.ii.InstantInventoryPlugin.EMPTY_WIDGET;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import net.runelite.api.Client;
import net.runelite.client.eventbus.EventBus;
import no.elg.ii.FeatureManager;
import no.elg.ii.Features;
import no.elg.ii.InstantInventoryConfig;
import no.elg.ii.InstantInventoryPlugin;
import no.elg.ii.feature.clean.CleanHerbFeature;
import no.elg.ii.feature.hide.DepositFeature;
import no.elg.ii.feature.hide.DropFeature;
import no.elg.ii.feature.replace.EquipFeature;
import no.elg.ii.feature.replace.WithdrawFeature;
import no.elg.ii.inventory.InventoryState;
import org.junit.Before;

public class IntegrationTestHelper {

  protected InstantInventoryPlugin plugin;
  protected DropFeature dropFeature;
  protected CleanHerbFeature cleanHerbFeature;
  protected DepositFeature depositFeature;
  protected EquipFeature equipFeature;
  protected WithdrawFeature withdrawFeature;
  protected InstantInventoryConfig instantInventoryConfig;
  protected EventBus eventBus;
  protected Client client;
  protected FeatureManager featureManager;
  protected Features features;

  @Before
  public void setUp() {
    eventBus = mock(EventBus.class);
    client = mock(Client.class);
    instantInventoryConfig = spy(new InstantInventoryConfig() {
    });
    doReturn(true).when(instantInventoryConfig).instantDrop();
    doReturn(true).when(instantInventoryConfig).instantClean();
    doReturn(true).when(instantInventoryConfig).instantDeposit();
    doReturn(true).when(instantInventoryConfig).instantEquip();
    doReturn(true).when(instantInventoryConfig).instantWithdraw();

    features = new Features(
      TestSetup.createNewDropFeature(),
      TestSetup.createNewCleanHerbFeature(),
      TestSetup.createNewDepositFeature(),
      TestSetup.createNewEquipFeature(),
      TestSetup.createNewWithdrawFeature()
    );
    dropFeature = features.getDropFeature();
    cleanHerbFeature = features.getCleanHerbFeature();
    depositFeature = features.getDepositFeature();
    equipFeature = features.getEquipFeature();
    withdrawFeature = features.getWithdrawFeature();

    featureManager = spy(new FeatureManager(eventBus, instantInventoryConfig, features));

    plugin = spy(new InstantInventoryPlugin(client, eventBus, instantInventoryConfig, featureManager));
    doReturn(EMPTY_WIDGET).when(plugin).inventoryItems(any());

    Client client = mock(Client.class);
    InventoryState inventoryState = new InventoryState(instantInventoryConfig, client);
    doReturn(inventoryState).when(dropFeature).getState();
    doReturn(inventoryState).when(cleanHerbFeature).getState();
    doReturn(inventoryState).when(depositFeature).getState();
  }
}
