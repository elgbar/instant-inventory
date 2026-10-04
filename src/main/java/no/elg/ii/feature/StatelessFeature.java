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
package no.elg.ii.feature;


import lombok.NonNull;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import no.elg.ii.InstantInventoryConfig;

/**
 * A feature of the instant inventory plugin.
 * Usually each feature implements {@link StatefulFeature} when they need a state.
 */
public interface StatelessFeature {

  /**
   * @return Config key in {@link InstantInventoryConfig} for this feature
   */
  @NonNull
  String getConfigKey();

  /**
   * @return If the relevant {@link InstantInventoryConfig} toggle is enabled
   */
  boolean isEnabledInConfig();

  /**
   * Method run when this feature is loaded in, either on {@link Plugin#startUp()} or when
   * {@link ConfigChanged} and this feature is enabled in the config and not already loaded.
   */
  default void onEnable() {
  }

  /**
   * Method run when this feature is disabled, either on {@link Plugin#shutDown()} or when
   * {@link ConfigChanged} and this feature is disabled in the config and is loaded.
   */
  default void onDisable() {
  }
}
