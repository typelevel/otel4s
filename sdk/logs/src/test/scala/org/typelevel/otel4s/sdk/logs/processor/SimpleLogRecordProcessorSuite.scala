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

package org.typelevel.otel4s.sdk.logs.processor

import cats.Foldable
import cats.effect.IO
import cats.syntax.all._
import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import org.scalacheck.Test
import org.scalacheck.effect.PropF
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.internal.Diagnostic
import org.typelevel.otel4s.sdk.logs.LogRecordRef
import org.typelevel.otel4s.sdk.logs.data.LogRecordData
import org.typelevel.otel4s.sdk.logs.exporter.InMemoryLogRecordExporter
import org.typelevel.otel4s.sdk.logs.exporter.LogRecordExporter
import org.typelevel.otel4s.sdk.logs.scalacheck.Arbitraries._

class SimpleLogRecordProcessorSuite extends CatsEffectSuite with ScalaCheckEffectSuite {

  private implicit val noopDiagnostic: Diagnostic[IO] = Diagnostic.noop

  test("show details in the name") {
    val exporter = new FailingExporter(
      "error-prone",
      new RuntimeException("something went wrong")
    )

    val processor = SimpleLogRecordProcessor(exporter)

    val expected =
      "SimpleLogRecordProcessor{exporter=error-prone}"

    assertEquals(processor.name, expected)
  }

  test("export logs on emit") {
    PropF.forAllF { (logs: List[LogRecordData]) =>
      for {
        exporter <- InMemoryLogRecordExporter.create[IO](None)
        processor = SimpleLogRecordProcessor(exporter)
        _ <- logs.traverse_(log => LogRecordRef.create[IO](log).flatMap(processor.onEmit(Context.root, _)))
        exported <- exporter.finishedLogs
        _ = assertEquals(
          exported.map(_.observedTimestamp),
          logs.map(_.observedTimestamp)
        )
      } yield ()
    }
  }

  test("do not rethrow export errors") {
    PropF.forAllF { (logs: List[LogRecordData]) =>
      val error = new RuntimeException("something went wrong")
      val exporter = new FailingExporter("error-prone", error)
      val processor = SimpleLogRecordProcessor(exporter)

      for {
        attempts <- logs.traverse { log =>
          LogRecordRef.create[IO](log).flatMap(processor.onEmit(Context.root, _)).attempt
        }
        _ = assertEquals(attempts, List.fill(logs.size)(Right(())))
      } yield ()
    }
  }

  override protected def scalaCheckTestParameters: Test.Parameters =
    super.scalaCheckTestParameters
      .withMinSuccessfulTests(10)
      .withMaxSize(10)

  private class FailingExporter(exporterName: String, onExport: Throwable) extends LogRecordExporter.Unsealed[IO] {
    def name: String = exporterName

    def exportLogRecords[G[_]: Foldable](logs: G[LogRecordData]): IO[Unit] =
      IO.raiseError(onExport)

    def flush: IO[Unit] =
      IO.unit
  }

}
