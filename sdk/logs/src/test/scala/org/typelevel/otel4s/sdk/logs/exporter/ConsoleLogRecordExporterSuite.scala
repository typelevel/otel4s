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
package exporter

import cats.Monad
import cats.effect.IO
import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import org.scalacheck.Gen
import org.scalacheck.Test
import org.scalacheck.effect.PropF
import org.typelevel.otel4s.sdk.logs.data.LogRecordData
import org.typelevel.otel4s.sdk.logs.scalacheck.Gens
import org.typelevel.otel4s.sdk.test.InMemoryConsole
import org.typelevel.otel4s.sdk.test.InMemoryConsole._

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class ConsoleLogRecordExporterSuite extends CatsEffectSuite with ScalaCheckEffectSuite {
  private val Formatter = DateTimeFormatter.ISO_DATE_TIME

  test("log record data is printed to the console") {
    PropF.forAllF(Gen.listOf(Gens.logRecordData)) { logs =>
      val expected = logs.map(log => Entry(Op.Println, expectedLog(log)))

      for {
        inMemConsole <- InMemoryConsole.create[IO]
        consoleExporter = ConsoleLogRecordExporter[IO](Monad[IO], inMemConsole)
        _ <- consoleExporter.exportLogRecords(logs)
        entries <- inMemConsole.entries
      } yield assertEquals(entries, expected)
    }
  }

  private def expectedLog(data: LogRecordData): String = {
    val scope = data.instrumentationScope

    val timestamp =
      Instant
        .ofEpochMilli(data.timestamp.getOrElse(data.observedTimestamp).toMillis)
        .atZone(ZoneOffset.UTC)

    val date = Formatter.format(timestamp)

    val content =
      s"$date ${data.severity.map(_.toString).getOrElse("")} '${data.body.getOrElse("")}' : " +
        s"${data.traceContext.map(_.traceId.toHex).getOrElse("")} ${data.traceContext.map(_.spanId.toHex).getOrElse("")} " +
        s"[scopeInfo: ${scope.name}:${scope.version.getOrElse("")}] " +
        s"${data.attributes}"

    s"ConsoleLogRecordExporter: $content"
  }

  override protected def scalaCheckTestParameters: Test.Parameters =
    super.scalaCheckTestParameters
      .withMinSuccessfulTests(20)
      .withMaxSize(20)

}
