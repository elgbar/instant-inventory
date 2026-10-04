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

package no.elg.ii.feature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import com.google.common.reflect.ClassPath;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;

/**
 * Guards against forgetting to wire up a new {@link StatelessFeature}
 */
public class FeaturesTest {

  /**
   * @return Every field in {@link Features} that holds a {@link StatelessFeature}
   */
  private static List<Field> featureFields() {
    List<Field> fields = Arrays.stream(Features.class.getDeclaredFields())
      .filter(field -> StatelessFeature.class.isAssignableFrom(field.getType()))
      .collect(Collectors.toList());
    fields.forEach(field -> field.setAccessible(true));
    return fields;
  }

  @Test
  public void forEachVisitsEveryFeatureFieldExactlyOnce() throws IllegalAccessException {
    Features features = new Features();
    Set<StatelessFeature> expected = new HashSet<>();
    for (Field field : featureFields()) {
      StatelessFeature feature = (StatelessFeature) mock(field.getType());
      field.set(features, feature);
      expected.add(feature);
    }

    List<StatelessFeature> visited = new ArrayList<>();
    features.forEach(visited::add);

    assertEquals("Features#forEach visited a different number of features than there are feature fields, "
                 + "did you forget to add a new feature to forEach?", expected.size(), visited.size());
    assertEquals("Features#forEach did not visit the same features as the feature fields", expected, new HashSet<>(visited));
  }

  @Test
  public void everyFeatureImplementationHasAFieldInFeatures() throws IOException {
    Set<Class<?>> fieldTypes = featureFields().stream().map(Field::getType).collect(Collectors.toSet());

    List<Class<?>> missing = ClassPath.from(Features.class.getClassLoader())
      .getTopLevelClassesRecursive(Features.class.getPackage().getName())
      .stream()
      .map(ClassPath.ClassInfo::load)
      .filter(StatelessFeature.class::isAssignableFrom)
      .filter(clazz -> !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers()))
      .filter(clazz -> !fieldTypes.contains(clazz))
      .collect(Collectors.toList());

    assertTrue("Found features without a field in Features, did you forget to add a new feature? " + missing, missing.isEmpty());
  }
}
