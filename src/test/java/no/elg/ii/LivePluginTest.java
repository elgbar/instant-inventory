/*
 * Copyright (c) 2023-2024 Elg
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

package no.elg.ii;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import java.util.Arrays;
import java.util.List;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;
import org.slf4j.LoggerFactory;

public class LivePluginTest {

  private final static List<String> SPAMMING_LOGGERS = List.of(
    "client-patch",
    "net.runelite.client.task.Scheduler",
    "net.runelite.api.geometry.RectangleUnion",
    "net.runelite.client.game.LootManager",
    "net.runelite.client.input.KeyManager",
    "net.runelite.client.config.ConfigInvocationHandler"
  );

  @SuppressWarnings("unchecked")
  public static void main(String[] args) throws Exception {
    ExternalPluginManager.loadBuiltin(InstantInventoryPlugin.class);
    var argsList = Arrays.asList(args);
    if (argsList.contains("--trace")) {

      final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
      logger.setLevel(Level.TRACE);

      //Spams too much
      for (String loggerName : SPAMMING_LOGGERS) {
        final Logger taskScheduler = (Logger) LoggerFactory.getLogger(loggerName);
        taskScheduler.setLevel(Level.INFO);
      }

      String[] argsNoTrace = Arrays.stream(args).filter(s -> !s.equalsIgnoreCase("--trace")).toArray(String[]::new);
      RuneLite.main(argsNoTrace);
    } else {
      RuneLite.main(args);
    }
  }
}