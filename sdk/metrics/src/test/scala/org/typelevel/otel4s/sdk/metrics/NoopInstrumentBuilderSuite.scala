/*
 * Copyright 2024 Typelevel
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

package org.typelevel.otel4s.sdk.metrics

import cats.effect.IO
import munit.CatsEffectSuite
import org.typelevel.otel4s.sdk.test.InMemoryDiagnostic
import org.typelevel.otel4s.sdk.test.InMemoryDiagnostic._

class NoopInstrumentBuilderSuite extends CatsEffectSuite {

  private val incorrectName = "7metric???"

  test("create a noop Counter") {
    InMemoryDiagnostic.create[IO].flatMap { implicit C: InMemoryDiagnostic[IO] =>
      for {
        counter <- NoopInstrumentBuilder.counter[IO, Long](incorrectName).create
        _ <- C.entries.assertEquals(consoleEntries("Counter"))
        enabled <- counter.backend.meta.isEnabled
      } yield assert(!enabled)
    }
  }

  test("create a noop Histogram") {
    InMemoryDiagnostic.create[IO].flatMap { implicit C: InMemoryDiagnostic[IO] =>
      for {
        histogram <- NoopInstrumentBuilder
          .histogram[IO, Long](incorrectName)
          .create
        _ <- C.entries.assertEquals(consoleEntries("Histogram"))
        enabled <- histogram.backend.meta.isEnabled
      } yield assert(!enabled)
    }
  }

  test("create a noop UpDownCounter") {
    InMemoryDiagnostic.create[IO].flatMap { implicit C: InMemoryDiagnostic[IO] =>
      for {
        upDownCounter <- NoopInstrumentBuilder
          .upDownCounter[IO, Long](incorrectName)
          .create
        _ <- C.entries.assertEquals(consoleEntries("UpDownCounter"))
        enabled <- upDownCounter.backend.meta.isEnabled
      } yield assert(!enabled)
    }
  }

  test("create a noop Gauge") {
    InMemoryDiagnostic.create[IO].flatMap { implicit C: InMemoryDiagnostic[IO] =>
      for {
        gauge <- NoopInstrumentBuilder.gauge[IO, Long](incorrectName).create
        _ <- C.entries.assertEquals(consoleEntries("Gauge"))
        enabled <- gauge.backend.meta.isEnabled
      } yield assert(!enabled)
    }
  }

  test("create a noop ObservableCounter") {
    InMemoryDiagnostic.create[IO].flatMap { implicit C: InMemoryDiagnostic[IO] =>
      for {
        _ <- NoopInstrumentBuilder
          .observableCounter[IO, Long](incorrectName)
          .createObserver
        _ <- C.entries.assertEquals(consoleEntries("ObservableCounter"))
      } yield ()
    }
  }

  test("create a noop ObservableUpDownCounter") {
    InMemoryDiagnostic.create[IO].flatMap { implicit C: InMemoryDiagnostic[IO] =>
      for {
        _ <- NoopInstrumentBuilder
          .observableUpDownCounter[IO, Long](incorrectName)
          .createObserver
        _ <- C.entries.assertEquals(consoleEntries("ObservableUpDownCounter"))
      } yield ()
    }
  }

  test("create a noop ObservableGauge") {
    InMemoryDiagnostic.create[IO].flatMap { implicit C: InMemoryDiagnostic[IO] =>
      for {
        _ <- NoopInstrumentBuilder
          .observableGauge[IO, Long](incorrectName)
          .createObserver
        _ <- C.entries.assertEquals(consoleEntries("ObservableGauge"))
      } yield ()
    }
  }

  private def consoleEntries(instrument: String): List[Entry] = List(
    Entry.Error(
      s"SdkMeter: $instrument instrument has invalid name [$incorrectName]. " +
        "Using noop instrument. " +
        "Instrument names must consist of 255 or fewer characters including alphanumeric, _, ., -, and start with a letter.",
      None
    )
  )

}
