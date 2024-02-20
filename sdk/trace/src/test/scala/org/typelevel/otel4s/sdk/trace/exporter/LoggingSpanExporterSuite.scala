/*
 * Copyright 2023 Typelevel
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

package org.typelevel.otel4s.sdk.trace
package exporter

import munit.CatsEffectSuite
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.sdk.trace.scalacheck.Gens
import cats.effect.IO
import org.typelevel.otel4s.sdk.trace.exporter.InMemoryConsole._
import cats.Applicative

class LoggingSpanExporterSuite extends CatsEffectSuite {

  test("span data is exported as log") {
    val span1: SpanData = getSpanData
    val span2: SpanData = getSpanData

    for {
      inMemConsole <- InMemoryConsole.create[IO]
      loggingExporter = LoggingSpanExporter[IO](Applicative[IO], inMemConsole)
      _ <- loggingExporter.exportSpans(List(span1, span2))
      entries <- inMemConsole.entries
      expectedLogs = List(expectedLog(span1), expectedLog(span2)).map { msg =>
        Entry(Op.Println, msg)
      }
    } yield {
      assertEquals(entries, expectedLogs)
    }
  }

  private def getSpanData: SpanData =
    Gens.spanData.sample.getOrElse(getSpanData)

  private def expectedLog(span: SpanData): String =
    s"'${span.name}' : " +
      s"${span.spanContext.traceIdHex} ${span.spanContext.spanIdHex} ${span.kind} " +
      s"[tracer: ${span.instrumentationScope.name}:${span.instrumentationScope.version
          .getOrElse("")}] " +
      s"${span.attributes}"

}
