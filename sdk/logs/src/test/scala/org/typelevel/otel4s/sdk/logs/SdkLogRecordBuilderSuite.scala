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

package org.typelevel.otel4s.sdk.logs

import cats.effect.IO
import cats.mtl.Ask
import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import org.scalacheck.Test
import org.scalacheck.effect.PropF
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.logs.LogRecordBuilder
import org.typelevel.otel4s.meta.InstrumentMeta
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.context.TraceContext
import org.typelevel.otel4s.sdk.logs.data.LogRecordData
import org.typelevel.otel4s.sdk.logs.exporter.InMemoryLogRecordExporter
import org.typelevel.otel4s.sdk.logs.processor.SimpleLogRecordProcessor
import org.typelevel.otel4s.sdk.logs.scalacheck.Arbitraries._

import scala.concurrent.duration._
import scala.util.chaining._

class SdkLogRecordBuilderSuite extends CatsEffectSuite with ScalaCheckEffectSuite {

  private implicit val askContext: Ask[IO, Context] = Ask.const(Context.root)

  test("defaults (with explicit observed timestamp)") {
    PropF.forAllF { (scope: InstrumentationScope, resource: TelemetryResource) =>
      for {
        exporter <- InMemoryLogRecordExporter.create[IO](None)
        processor = SimpleLogRecordProcessor(exporter)
        meta = InstrumentMeta.Dynamic.enabled[IO]
        builder = SdkLogRecordBuilder.empty[IO](
          meta = meta,
          processor = processor,
          instrumentationScope = scope,
          resource = resource,
          traceContextLookup = TraceContext.Lookup.noop,
          limits = LogRecordLimits.default
        )
        _ <- builder.emit
        logs <- exporter.finishedLogs
      } yield {
        assertEquals(logs.length, 1)
        val log = logs.head
        assertEquals(log.timestamp, None)
        assert(log.observedTimestamp > Duration.Zero)
        assertEquals(log.traceContext, None)
        assertEquals(log.severity, None)
        assertEquals(log.severityText, None)
        assertEquals(log.body, None)
        assertEquals(log.eventName, None)
        assertEquals(log.attributes.elements, Attributes.empty)
        assertEquals(log.instrumentationScope, scope)
        assertEquals(log.resource, resource)
      }
    }
  }

  test("create a log record with the configured parameters and limits applied") {
    PropF.forAllF { (logRecordData: LogRecordData) =>
      val meta = InstrumentMeta.Dynamic.enabled[IO]
      val lookup = new TraceContext.Lookup {
        def get(ctx: Context): Option[TraceContext] = logRecordData.traceContext
      }

      for {
        exporter <- InMemoryLogRecordExporter.create[IO](None)
        processor = SimpleLogRecordProcessor(exporter)

        builder = SdkLogRecordBuilder.empty[IO](
          meta = meta,
          processor = processor,
          instrumentationScope = logRecordData.instrumentationScope,
          resource = logRecordData.resource,
          traceContextLookup = lookup,
          limits = LogRecordLimits.default
        ): LogRecordBuilder[IO, Context]

        _ <- builder
          .pipe(b => logRecordData.timestamp.fold(b)(b.withTimestamp))
          .pipe(b => logRecordData.severity.fold(b)(b.withSeverity))
          .pipe(b => logRecordData.severityText.fold(b)(b.withSeverityText))
          .pipe(b => logRecordData.body.fold(b)(b.withBody))
          .pipe(b => logRecordData.eventName.fold(b)(b.withEventName))
          .withObservedTimestamp(logRecordData.observedTimestamp)
          .addAttributes(logRecordData.attributes.elements)
          .emit
        logs <- exporter.finishedLogs
      } yield {
        assertEquals(logs.length, 1)
        val log = logs.head

        assertEquals(log.timestamp, logRecordData.timestamp)
        assertEquals(log.observedTimestamp, logRecordData.observedTimestamp)
        assertEquals(log.traceContext, logRecordData.traceContext)
        assertEquals(log.severity, logRecordData.severity)
        assertEquals(log.severityText, logRecordData.severityText)
        assertEquals(log.body, logRecordData.body)
        assertEquals(log.eventName, logRecordData.eventName)
        assertEquals(log.attributes.elements, logRecordData.attributes.elements)

        assertEquals(log.instrumentationScope, logRecordData.instrumentationScope)
        assertEquals(log.resource, logRecordData.resource)
      }
    }
  }

  override protected def scalaCheckTestParameters: Test.Parameters =
    super.scalaCheckTestParameters
      .withMinSuccessfulTests(10)
      .withMaxSize(10)
}
