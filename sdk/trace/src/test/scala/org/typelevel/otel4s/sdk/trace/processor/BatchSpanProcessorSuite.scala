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
package processor

import cats.Foldable
import cats.effect.IO
import cats.syntax.foldable._
import cats.syntax.traverse._
import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import org.scalacheck.Test
import org.scalacheck.effect.PropF
import org.typelevel.otel4s.sdk.common.Diagnostic
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.sdk.trace.exporter.InMemorySpanExporter
import org.typelevel.otel4s.sdk.trace.exporter.SpanExporter
import org.typelevel.otel4s.sdk.trace.scalacheck.Arbitraries._

class BatchSpanProcessorSuite extends CatsEffectSuite with ScalaCheckEffectSuite {

  private implicit val noopDiagnostic: Diagnostic[IO] = Diagnostic.noop

  test("show details in the name") {
    val exporter = new FailingExporter(
      "error-prone",
      new RuntimeException("something went wrong")
    )

    val expected =
      "BatchSpanProcessor{exporter=error-prone, scheduleDelay=5 seconds, exporterTimeout=30 seconds, maxQueueSize=2048, maxExportBatchSize=512}"

    BatchSpanProcessor.builder(exporter).build.use { processor =>
      IO(assertEquals(processor.name, expected))
    }
  }

  test("do nothing on start") {
    PropF.forAllF { (spans: List[SpanData]) =>
      for {
        exporter <- InMemorySpanExporter.create[IO](None)
        _ <- BatchSpanProcessor.builder(exporter).build.use { p =>
          spans.traverse_(_ => p.onStart(None, null))
        }
        exported <- exporter.finishedSpans
      } yield assert(exported.isEmpty)
    }
  }

  test("export only sampled spans on end") {
    PropF.forAllF { (spans: List[SpanData]) =>
      val sampled = spans.filter(_.spanContext.isSampled)

      for {
        exporter <- InMemorySpanExporter.create[IO](None)
        _ <- BatchSpanProcessor.builder(exporter).build.use { p =>
          spans.traverse_(span => p.onEnd(span))
        }
        exported <- exporter.finishedSpans
      } yield assertEquals(exported, sampled)
    }
  }

  test("do not rethrow export errors") {
    PropF.forAllF { (spans: List[SpanData]) =>
      val error = new RuntimeException("something went wrong")
      val exporter = new FailingExporter("error-prone", error)

      for {
        attempts <- BatchSpanProcessor.builder(exporter).build.use { p =>
          spans.traverse(span => p.onEnd(span).attempt)
        }
      } yield assertEquals(attempts, List.fill(spans.size)(Right(())))
    }
  }

  override protected def scalaCheckTestParameters: Test.Parameters =
    super.scalaCheckTestParameters
      .withMinSuccessfulTests(10)
      .withMaxSize(10)

  private class FailingExporter(
      exporterName: String,
      onExport: Throwable
  ) extends SpanExporter.Unsealed[IO] {
    def name: String = exporterName

    def exportSpans[G[_]: Foldable](spans: G[SpanData]): IO[Unit] =
      IO.raiseError(onExport)

    def flush: IO[Unit] =
      IO.unit
  }

}
