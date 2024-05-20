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

import cats.Applicative
import cats.effect.IO
import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import org.scalacheck.Gen
import org.scalacheck.Test
import org.scalacheck.effect.PropF
import org.typelevel.otel4s.sdk.test.InMemoryConsole
import org.typelevel.otel4s.sdk.test.InMemoryConsole._
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.sdk.trace.scalacheck.Gens

class ConsoleSpanExporterSuite
    extends CatsEffectSuite
    with ScalaCheckEffectSuite {

  test("span data is exported as a log which is printed to the console") {
    PropF.forAllF(Gen.listOf(Gens.spanData)) { spans =>
      val expected = spans.map(span => Entry(Op.Println, expectedLog(span)))

      for {
        inMemConsole <- InMemoryConsole.create[IO]
        consoleExporter = ConsoleSpanExporter[IO](Applicative[IO], inMemConsole)
        _ <- consoleExporter.exportSpans(spans)
        entries <- inMemConsole.entries
      } yield assertEquals(entries, expected)
    }
  }

  private def expectedLog(span: SpanData): String =
    s"ConsoleSpanExporter: '${span.name}' : " +
      s"${span.spanContext.traceIdHex} ${span.spanContext.spanIdHex} ${span.kind} " +
      s"[tracer: ${span.instrumentationScope.name}:${span.instrumentationScope.version
          .getOrElse("")}] " +
      s"${span.attributes}"

  override protected def scalaCheckTestParameters: Test.Parameters =
    super.scalaCheckTestParameters
      .withMinSuccessfulTests(20)
      .withMaxSize(20)

}
