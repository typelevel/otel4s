/*
 * Copyright 2025 Typelevel
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

package org.typelevel.otel4s.oteljava.testkit.logs

import cats.effect.IO
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanContext
import io.opentelemetry.api.trace.TraceFlags
import io.opentelemetry.api.trace.TraceState
import io.opentelemetry.context.{Context => JContext}
import munit.CatsEffectSuite
import org.typelevel.otel4s.oteljava.context.Context

class LogsTestkitSuite extends CatsEffectSuite {
  test("deferred log records use captured trace context") {
    LogsTestkit.inMemory[IO]().use { testkit =>
      val spanContext = SpanContext.create(
        "0af7651916cd43dd8448eb211c80319c",
        "b7ad6b7169203331",
        TraceFlags.getSampled,
        TraceState.getDefault
      )
      val context = Context.wrap(Span.wrap(spanContext).storeInContext(JContext.root()))
      val manager = testkit.contextManager

      for {
        logger <- testkit.loggerProvider.get("context-manager")
        restore <- manager.scope(context)(manager.captureScope)
        _ <- restore(logger.logRecordBuilder.emit)
        ambient <- manager.current
        logs <- testkit.finishedLogs
      } yield {
        assertEquals(logs.map(_.getSpanContext), List(spanContext))
        assert(!Span.fromContext(ambient.underlying).getSpanContext.isValid)
      }
    }
  }
}
