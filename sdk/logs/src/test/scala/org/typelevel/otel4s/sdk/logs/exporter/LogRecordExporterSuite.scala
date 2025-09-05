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

import cats.Foldable
import cats.data.NonEmptyList
import cats.effect.IO
import munit.CatsEffectSuite
import org.typelevel.otel4s.sdk.logs.data.LogRecordData
import org.typelevel.otel4s.sdk.logs.scalacheck.Gens

class LogRecordExporterSuite extends CatsEffectSuite {

  test("create a no-op instance") {
    val exporter = LogRecordExporter.noop[IO]

    assertEquals(exporter.toString, "LogRecordExporter.Noop")
  }

  test("of (empty input) - use noop") {
    val exporter = LogRecordExporter.of[IO]()

    assertEquals(exporter.toString, "LogRecordExporter.Noop")
  }

  test("of (single input) - use this input") {
    val data: LogRecordData = getLogRecordData

    for {
      inMemory <- InMemoryLogRecordExporter.create[IO](None)
      exporter <- IO.pure(LogRecordExporter.of(inMemory))
      _ <- exporter.exportLogRecords(List(data))
      logs <- inMemory.finishedLogs
    } yield {
      assertEquals(exporter.toString, "InMemoryLogRecordExporter")
      assertEquals(logs, List(data))
    }
  }

  test("of (multiple) - create a multi instance") {
    val data: LogRecordData = getLogRecordData

    for {
      inMemoryA <- InMemoryLogRecordExporter.create[IO](None)
      inMemoryB <- InMemoryLogRecordExporter.create[IO](None)
      exporter <- IO.pure(LogRecordExporter.of(inMemoryA, inMemoryB))
      _ <- exporter.exportLogRecords(List(data))
      logsA <- inMemoryA.finishedLogs
      logsB <- inMemoryB.finishedLogs
    } yield {
      assertEquals(logsA, List(data))
      assertEquals(logsB, List(data))

      assertEquals(
        exporter.toString,
        "LogRecordExporter.Multi(InMemoryLogRecordExporter, InMemoryLogRecordExporter)"
      )
    }
  }

  test("of (multiple) - flatten out nested multi instances") {
    val data: LogRecordData = getLogRecordData

    for {
      inMemoryA <- InMemoryLogRecordExporter.create[IO](None)
      inMemoryB <- InMemoryLogRecordExporter.create[IO](None)

      multi1 <- IO.pure(LogRecordExporter.of(inMemoryA, inMemoryB))
      multi2 <- IO.pure(LogRecordExporter.of(inMemoryA, inMemoryB))

      exporter <- IO.pure(LogRecordExporter.of(multi1, multi2))

      _ <- exporter.exportLogRecords(List(data))
      logsA <- inMemoryA.finishedLogs
      logsB <- inMemoryB.finishedLogs
    } yield {
      assertEquals(logsA, List(data, data))
      assertEquals(logsB, List(data, data))

      assertEquals(
        exporter.toString,
        "LogRecordExporter.Multi(InMemoryLogRecordExporter, InMemoryLogRecordExporter, InMemoryLogRecordExporter, InMemoryLogRecordExporter)"
      )
    }
  }

  test("of (multiple) - single failure - rethrow a single failure") {
    val data: LogRecordData = getLogRecordData

    val onExport = new RuntimeException("cannot export logs")
    val onFlush = new RuntimeException("cannot flush")

    val failing = new FailingExporter("error-prone", onExport, onFlush)

    def expected(e: Throwable) =
      LogRecordExporter.ExporterFailure("error-prone", e)

    for {
      inMemoryA <- InMemoryLogRecordExporter.create[IO](None)
      inMemoryB <- InMemoryLogRecordExporter.create[IO](None)
      exporter <- IO.pure(LogRecordExporter.of(inMemoryA, failing, inMemoryB))
      exportAttempt <- exporter.exportLogRecords(List(data)).attempt
      flushAttempt <- exporter.flush.attempt
      logsA <- inMemoryA.finishedLogs
      logsB <- inMemoryB.finishedLogs
    } yield {
      assertEquals(logsA, List(data))
      assertEquals(logsB, List(data))
      assertEquals(exportAttempt, Left(expected(onExport)))
      assertEquals(flushAttempt, Left(expected(onFlush)))
    }
  }

  test("of (multiple) - multiple failures - rethrow a composite failure") {
    val onExport = new RuntimeException("cannot export logs")
    val onFlush = new RuntimeException("cannot flush")

    val exporter1 = new FailingExporter("exporter1", onExport, onFlush)
    val exporter2 = new FailingExporter("exporter2", onExport, onFlush)

    val exporter = LogRecordExporter.of(exporter1, exporter2)

    def expected(e: Throwable) =
      LogRecordExporter.CompositeExporterFailure(
        LogRecordExporter.ExporterFailure("exporter1", e),
        NonEmptyList.of(LogRecordExporter.ExporterFailure("exporter2", e))
      )

    for {
      exportAttempt <- exporter.exportLogRecords(List.empty[LogRecordData]).attempt
      flushAttempt <- exporter.flush.attempt
    } yield {
      assertEquals(exportAttempt, Left(expected(onExport)))
      assertEquals(flushAttempt, Left(expected(onFlush)))
    }
  }

  private def getLogRecordData: LogRecordData =
    Gens.logRecordData.sample.getOrElse(getLogRecordData)

  private class FailingExporter(
      exporterName: String,
      onExport: Throwable,
      onFlush: Throwable
  ) extends LogRecordExporter.Unsealed[IO] {
    def name: String = exporterName

    def exportLogRecords[G[_]: Foldable](logs: G[LogRecordData]): IO[Unit] =
      IO.raiseError(onExport)

    def flush: IO[Unit] =
      IO.raiseError(onFlush)
  }

}
