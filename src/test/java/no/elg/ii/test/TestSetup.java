package no.elg.ii.test;

import static no.elg.ii.InstantInventoryPlugin.EMPTY_WIDGET;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.function.BooleanSupplier;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.ui.overlay.OverlayManager;
import no.elg.ii.InstantInventoryConfig;
import no.elg.ii.InstantInventoryPlugin;
import no.elg.ii.InventoryState;
import no.elg.ii.clean.CleanHerbFeature;
import no.elg.ii.clean.CleanHerbOverlay;
import no.elg.ii.drop.DropFeature;
import org.mockito.stubbing.Answer;

public class TestSetup {

  public static DropFeature createNewDropFeature() {
    DropFeature feature = spy(new DropFeature());
    feature.clientThread = TestSetup.mockedClientThread();
    InstantInventoryPlugin plugin = feature.plugin = mock(InstantInventoryPlugin.class);
    doReturn(EMPTY_WIDGET).when(plugin).inventoryItems();
    InventoryState inventoryState = new InventoryState(spy(new InstantInventoryConfig() {
    }),
        mock(Client.class));
    doReturn(inventoryState).when(feature).getState();
    return feature;
  }

  public static CleanHerbFeature createNewCleanHerbFeature() {
    CleanHerbFeature feature = spy(new CleanHerbFeature());
    feature.overlayManager = mock(OverlayManager.class);
    feature.client = mock(Client.class);
    feature.overlay = mock(CleanHerbOverlay.class);
    InstantInventoryConfig config = spy(new InstantInventoryConfig() {
    });

    InventoryState inventoryState = new InventoryState(config, feature.client);
    doReturn(inventoryState).when(feature).getState();
    return feature;
  }

  public static ClientThread mockedClientThread() {
    ClientThread clientThread = mock(ClientThread.class);

    Answer<Void> runnableAnswer = invocation -> {
      invocation.getArgument(0, Runnable.class).run();
      return null;
    };
    doAnswer(runnableAnswer).when(clientThread).invoke(any(Runnable.class));
    doAnswer(runnableAnswer).when(clientThread).invokeLater(any(Runnable.class));

    doAnswer(it -> it.getArgument(0, BooleanSupplier.class).getAsBoolean()).when(clientThread)
        .invoke(any(BooleanSupplier.class));
    doAnswer(it -> it.getArgument(0, BooleanSupplier.class).getAsBoolean()).when(clientThread)
        .invokeLater(any(BooleanSupplier.class));

    return clientThread;
  }

}
