/*
 * Copyright 2022 Typelevel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.typelevel.otel4s.oteljava.trace

import cats.effect.IO
import io.opentelemetry.api.OpenTelemetry
import munit.CatsEffectSuite
import org.typelevel.otel4s.oteljava.context.Context

class TracesSuite extends CatsEffectSuite {
  test("contextManager shares the tracing module's storage") {
    for {
      traces <- Traces.fromJOpenTelemetry[IO](OpenTelemetry.noop())
      key <- Context.Key.unique[IO, String]("custom")
      context = Context.root.updated(key, "captured")
      restore <- traces.localContext.scope(traces.contextManager.captureScope)(context)
      _ <- restore(traces.localContext.ask[Context]).map(_.get(key)).assertEquals(Some("captured"))
      _ <- traces.contextManager.current.map(_.get(key)).assertEquals(None)
    } yield ()
  }
}
