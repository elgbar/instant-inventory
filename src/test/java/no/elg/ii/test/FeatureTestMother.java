package no.elg.ii.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;
import net.runelite.client.config.ConfigItem;
import no.elg.ii.Feature;
import no.elg.ii.InstantInventoryConfig;
import no.elg.ii.InventoryState;
import no.elg.ii.ResetStaticMother;
import org.junit.Test;

public abstract class FeatureTestMother<T extends Feature> extends ResetStaticMother {


  public abstract T createNewInstance();

  @Test
  public void configTest() {
    Feature feature = createNewInstance();

    try {
      Method method = InstantInventoryConfig.class.getMethod(feature.getConfigKey());
      ConfigItem configItemAnnotation = method.getAnnotation(ConfigItem.class);
      assertNotNull(configItemAnnotation);
      assertEquals("key name in ConfigItem must match feature.getConfigKey()",
          feature.getConfigKey(), configItemAnnotation.keyName());
    } catch (NoSuchMethodException e) {
      fail("Failed to find a field in " + InstantInventoryConfig.class.getSimpleName()
          + " which matches the config key: " + feature.getConfigKey());
    }
  }

  @Test
  public void reset_calls_state_resetAll() {
    Feature feature = spy(createNewInstance());
    InventoryState mockState = mock(InventoryState.class);
    doReturn(mockState).when(feature).getState();
    doNothing().when(mockState).resetAll();

    feature.reset();
    verify(mockState).resetAll();
  }
}
