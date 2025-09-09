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
import org.scalacheck.effect.PropF
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.context.AskContext
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.logs.data.LogRecordData
import org.typelevel.otel4s.sdk.logs.exporter.InMemoryLogRecordExporter
import org.typelevel.otel4s.sdk.logs.processor.LogRecordProcessor
import org.typelevel.otel4s.sdk.logs.processor.SimpleLogRecordProcessor
import org.typelevel.otel4s.sdk.logs.scalacheck.Arbitraries._
import org.typelevel.otel4s.sdk.scalacheck.Gens

import scala.util.chaining._

class SdkLoggerProviderSuite extends CatsEffectSuite with ScalaCheckEffectSuite {

  private implicit val askContext: AskContext[IO] = Ask.const(Context.root)

  test("empty builder - return noop instance") {
    for {
      provider <- SdkLoggerProvider.builder[IO].build
    } yield assertEquals(provider.toString, "LoggerProvider.Noop")
  }

  test("reflect parameters in the toString") {
    PropF.forAllF(Gens.telemetryResource) { resource =>
      val processor = LogRecordProcessor.noop[IO]
      val limits = LogRecordLimits.builder
        .withMaxNumberOfAttributes(64)
        .withMaxAttributeValueLength(128)
        .build

      val expected =
        s"SdkLoggerProvider{resource=$resource, logRecordLimits=$limits, logRecordProcessor=$processor}"

      for {
        provider <- SdkLoggerProvider
          .builder[IO]
          .withResource(resource)
          .withLogRecordLimits(limits)
          .addLogRecordProcessor(processor)
          .build
      } yield assertEquals(provider.toString, expected)
    }
  }

  test("create a log record with the configured parameters and limits applied") {
    PropF.forAllF { (logRecordData: LogRecordData) =>
      for {
        exporter <- InMemoryLogRecordExporter.create[IO](None)

        provider <- SdkLoggerProvider
          .builder[IO]
          .addLogRecordProcessor(SimpleLogRecordProcessor(exporter))
          .withTraceContextLookup(_ => logRecordData.traceContext)
          .withResource(logRecordData.resource)
          .build

        logger <- provider
          .logger(logRecordData.instrumentationScope.name)
          .pipe(b => logRecordData.instrumentationScope.version.fold(b)(b.withVersion))
          .pipe(b => logRecordData.instrumentationScope.schemaUrl.fold(b)(b.withSchemaUrl))
          .get

        _ <- logger.logRecordBuilder
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

        assertEquals(
          log.instrumentationScope,
          InstrumentationScope(
            name = logRecordData.instrumentationScope.name,
            version = logRecordData.instrumentationScope.version,
            schemaUrl = logRecordData.instrumentationScope.schemaUrl,
            attributes = Attributes.empty
          )
        )

        assertEquals(log.resource, logRecordData.resource)
      }
    }
  }

}
