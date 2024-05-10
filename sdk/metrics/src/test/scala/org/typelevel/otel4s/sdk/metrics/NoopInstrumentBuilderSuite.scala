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
import org.typelevel.otel4s.sdk.test.InMemoryConsole
import org.typelevel.otel4s.sdk.test.InMemoryConsole._

class NoopInstrumentBuilderSuite extends CatsEffectSuite {

  private val incorrectName = "7metric???"

  test("create a noop Counter") {
    InMemoryConsole.create[IO].flatMap { implicit C: InMemoryConsole[IO] =>
      for {
        counter <- NoopInstrumentBuilder.counter[IO, Long](incorrectName).create
        _ <- C.entries.assertEquals(consoleEntries("Counter"))
      } yield assert(!counter.backend.meta.isEnabled)
    }
  }

  test("create a noop Histogram") {
    InMemoryConsole.create[IO].flatMap { implicit C: InMemoryConsole[IO] =>
      for {
        histogram <- NoopInstrumentBuilder
          .histogram[IO, Long](incorrectName)
          .create
        _ <- C.entries.assertEquals(consoleEntries("Histogram"))
      } yield assert(!histogram.backend.meta.isEnabled)
    }
  }

  test("create a noop UpDownCounter") {
    InMemoryConsole.create[IO].flatMap { implicit C: InMemoryConsole[IO] =>
      for {
        upDownCounter <- NoopInstrumentBuilder
          .upDownCounter[IO, Long](incorrectName)
          .create
        _ <- C.entries.assertEquals(consoleEntries("UpDownCounter"))
      } yield assert(!upDownCounter.backend.meta.isEnabled)
    }
  }

  test("create a noop Gauge") {
    InMemoryConsole.create[IO].flatMap { implicit C: InMemoryConsole[IO] =>
      for {
        _ <- NoopInstrumentBuilder.gauge[IO, Long](incorrectName).create
        _ <- C.entries.assertEquals(consoleEntries("Gauge"))
      } yield ()
    }
  }

  test("create a noop ObservableCounter") {
    InMemoryConsole.create[IO].flatMap { implicit C: InMemoryConsole[IO] =>
      for {
        _ <- NoopInstrumentBuilder
          .observableCounter[IO, Long](incorrectName)
          .createObserver
        _ <- C.entries.assertEquals(consoleEntries("ObservableCounter"))
      } yield ()
    }
  }

  test("create a noop ObservableUpDownCounter") {
    InMemoryConsole.create[IO].flatMap { implicit C: InMemoryConsole[IO] =>
      for {
        _ <- NoopInstrumentBuilder
          .observableUpDownCounter[IO, Long](incorrectName)
          .createObserver
        _ <- C.entries.assertEquals(consoleEntries("ObservableUpDownCounter"))
      } yield ()
    }
  }

  test("create a noop ObservableGauge") {
    InMemoryConsole.create[IO].flatMap { implicit C: InMemoryConsole[IO] =>
      for {
        _ <- NoopInstrumentBuilder
          .observableGauge[IO, Long](incorrectName)
          .createObserver
        _ <- C.entries.assertEquals(consoleEntries("ObservableGauge"))
      } yield ()
    }
  }

  private def consoleEntries(instrument: String): List[Entry] = List(
    Entry(
      Op.Errorln,
      s"SdkMeter: $instrument instrument has invalid name [$incorrectName]. " +
        "Using noop instrument. " +
        "Instrument names must consist of 255 or fewer characters including alphanumeric, _, ., -, and start with a letter."
    )
  )

}
