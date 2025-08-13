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

import cats.Foldable
import cats.data.NonEmptyList
import cats.effect.IO
import munit.CatsEffectSuite
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.sdk.trace.scalacheck.Gens

class SpanExporterSuite extends CatsEffectSuite {

  test("create a no-op instance") {
    val exporter = SpanExporter.noop[IO]

    assertEquals(exporter.toString, "SpanExporter.Noop")
  }

  test("of (empty input) - use noop") {
    val exporter = SpanExporter.of[IO]()

    assertEquals(exporter.toString, "SpanExporter.Noop")
  }

  test("of (single input) - use this input") {
    val data: SpanData = getSpanData

    for {
      inMemory <- InMemorySpanExporter.create[IO](None)
      exporter <- IO.pure(SpanExporter.of(inMemory))
      _ <- exporter.exportSpans(List(data))
      spans <- inMemory.finishedSpans
    } yield {
      assertEquals(exporter.toString, "InMemorySpanExporter")
      assertEquals(spans, List(data))
    }
  }

  test("of (multiple) - create a multi instance") {
    val data: SpanData = getSpanData

    for {
      inMemoryA <- InMemorySpanExporter.create[IO](None)
      inMemoryB <- InMemorySpanExporter.create[IO](None)
      exporter <- IO.pure(SpanExporter.of(inMemoryA, inMemoryB))
      _ <- exporter.exportSpans(List(data))
      spansA <- inMemoryA.finishedSpans
      spansB <- inMemoryB.finishedSpans
    } yield {
      assertEquals(spansA, List(data))
      assertEquals(spansB, List(data))

      assertEquals(
        exporter.toString,
        "SpanExporter.Multi(InMemorySpanExporter, InMemorySpanExporter)"
      )
    }
  }

  test("of (multiple) - flatten out nested multi instances") {
    val data: SpanData = getSpanData

    for {
      inMemoryA <- InMemorySpanExporter.create[IO](None)
      inMemoryB <- InMemorySpanExporter.create[IO](None)

      multi1 <- IO.pure(SpanExporter.of(inMemoryA, inMemoryB))
      multi2 <- IO.pure(SpanExporter.of(inMemoryA, inMemoryB))

      exporter <- IO.pure(SpanExporter.of(multi1, multi2))

      _ <- exporter.exportSpans(List(data))
      spansA <- inMemoryA.finishedSpans
      spansB <- inMemoryB.finishedSpans
    } yield {
      assertEquals(spansA, List(data, data))
      assertEquals(spansB, List(data, data))

      assertEquals(
        exporter.toString,
        "SpanExporter.Multi(InMemorySpanExporter, InMemorySpanExporter, InMemorySpanExporter, InMemorySpanExporter)"
      )
    }
  }

  test("of (multiple) - single failure - rethrow a single failure") {
    val data: SpanData = getSpanData

    val onExport = new RuntimeException("cannot export spans")
    val onFlush = new RuntimeException("cannot flush")

    val failing = new FailingExporter("error-prone", onExport, onFlush)

    def expected(e: Throwable) =
      SpanExporter.ExporterFailure("error-prone", e)

    for {
      inMemoryA <- InMemorySpanExporter.create[IO](None)
      inMemoryB <- InMemorySpanExporter.create[IO](None)
      exporter <- IO.pure(SpanExporter.of(inMemoryA, failing, inMemoryB))
      exportAttempt <- exporter.exportSpans(List(data)).attempt
      flushAttempt <- exporter.flush.attempt
      spansA <- inMemoryA.finishedSpans
      spansB <- inMemoryB.finishedSpans
    } yield {
      assertEquals(spansA, List(data))
      assertEquals(spansB, List(data))
      assertEquals(exportAttempt, Left(expected(onExport)))
      assertEquals(flushAttempt, Left(expected(onFlush)))
    }
  }

  test("of (multiple) - multiple failures - rethrow a composite failure") {
    val onExport = new RuntimeException("cannot export spans")
    val onFlush = new RuntimeException("cannot flush")

    val exporter1 = new FailingExporter("exporter1", onExport, onFlush)
    val exporter2 = new FailingExporter("exporter2", onExport, onFlush)

    val exporter = SpanExporter.of(exporter1, exporter2)

    def expected(e: Throwable) =
      SpanExporter.CompositeExporterFailure(
        SpanExporter.ExporterFailure("exporter1", e),
        NonEmptyList.of(SpanExporter.ExporterFailure("exporter2", e))
      )

    for {
      exportAttempt <- exporter.exportSpans(List.empty[SpanData]).attempt
      flushAttempt <- exporter.flush.attempt
    } yield {
      assertEquals(exportAttempt, Left(expected(onExport)))
      assertEquals(flushAttempt, Left(expected(onFlush)))
    }
  }

  private def getSpanData: SpanData =
    Gens.spanData.sample.getOrElse(getSpanData)

  private class FailingExporter(
      exporterName: String,
      onExport: Throwable,
      onFlush: Throwable
  ) extends SpanExporter.Unsealed[IO] {
    def name: String = exporterName

    def exportSpans[G[_]: Foldable](spans: G[SpanData]): IO[Unit] =
      IO.raiseError(onExport)

    def flush: IO[Unit] =
      IO.raiseError(onFlush)
  }

}
