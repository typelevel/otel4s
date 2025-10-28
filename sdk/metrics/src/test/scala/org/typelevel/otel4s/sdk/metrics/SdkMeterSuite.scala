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
import cats.mtl.Ask
import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import org.scalacheck.Gen
import org.scalacheck.effect.PropF
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.common.Diagnostic
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.context.AskContext
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.metrics.scalacheck.Gens
import org.typelevel.otel4s.sdk.metrics.test.InMemoryMeterSharedState
import org.typelevel.otel4s.sdk.test.InMemoryDiagnostic
import org.typelevel.otel4s.sdk.test.InMemoryDiagnostic.Entry

import scala.concurrent.duration._

class SdkMeterSuite extends CatsEffectSuite with ScalaCheckEffectSuite {

  private val invalidNameGen: Gen[String] =
    for {
      first <- Gen.oneOf(Gen.numChar, Gen.oneOf('_', '.', '-', '?', '='))
      rest <- Gen.alphaNumStr
    } yield first +: rest

  test("create a noop Counter when the name is invalid") {
    PropF.forAllF(
      Gens.telemetryResource,
      Gens.instrumentationScope,
      invalidNameGen
    ) { (resource, scope, name) =>
      InMemoryDiagnostic.create[IO].flatMap { implicit C: InMemoryDiagnostic[IO] =>
        for {
          meter <- createMeter(resource, scope)
          counter <- meter.counter[Long](name).create
          _ <- C.entries.assertEquals(consoleEntries("Counter", name))
          enabled <- counter.backend.meta.isEnabled
        } yield assert(!enabled)
      }
    }
  }

  test("create a noop UpDownCounter when the name is invalid") {
    PropF.forAllF(
      Gens.telemetryResource,
      Gens.instrumentationScope,
      invalidNameGen
    ) { (resource, scope, name) =>
      InMemoryDiagnostic.create[IO].flatMap { implicit C: InMemoryDiagnostic[IO] =>
        for {
          meter <- createMeter(resource, scope)
          counter <- meter.upDownCounter[Long](name).create
          _ <- C.entries.assertEquals(consoleEntries("UpDownCounter", name))
          enabled <- counter.backend.meta.isEnabled
        } yield assert(!enabled)
      }
    }
  }

  test("create a noop Histogram when the name is invalid") {
    PropF.forAllF(
      Gens.telemetryResource,
      Gens.instrumentationScope,
      invalidNameGen
    ) { (resource, scope, name) =>
      InMemoryDiagnostic.create[IO].flatMap { implicit C: InMemoryDiagnostic[IO] =>
        for {
          meter <- createMeter(resource, scope)
          histogram <- meter.histogram[Long](name).create
          _ <- C.entries.assertEquals(consoleEntries("Histogram", name))
          enabled <- histogram.backend.meta.isEnabled
        } yield assert(!enabled)
      }
    }
  }

  test("create a noop Gauge when the name is invalid") {
    PropF.forAllF(
      Gens.telemetryResource,
      Gens.instrumentationScope,
      invalidNameGen
    ) { (resource, scope, name) =>
      InMemoryDiagnostic.create[IO].flatMap { implicit C: InMemoryDiagnostic[IO] =>
        for {
          meter <- createMeter(resource, scope)
          gauge <- meter.gauge[Long](name).create
          _ <- C.entries.assertEquals(consoleEntries("Gauge", name))
          enabled <- gauge.backend.meta.isEnabled
        } yield assert(!enabled)
      }
    }
  }

  test("create a noop ObservableCounter when the name is invalid") {
    PropF.forAllF(
      Gens.telemetryResource,
      Gens.instrumentationScope,
      invalidNameGen
    ) { (resource, scope, name) =>
      InMemoryDiagnostic.create[IO].flatMap { implicit C: InMemoryDiagnostic[IO] =>
        for {
          meter <- createMeter(resource, scope)
          _ <- meter.observableCounter[Long](name).createObserver
          _ <- C.entries.assertEquals(consoleEntries("ObservableCounter", name))
        } yield ()
      }
    }
  }

  test("create a noop ObservableUpDownCounter when the name is invalid") {
    PropF.forAllF(
      Gens.telemetryResource,
      Gens.instrumentationScope,
      invalidNameGen
    ) { (resource, scope, name) =>
      InMemoryDiagnostic.create[IO].flatMap { implicit C: InMemoryDiagnostic[IO] =>
        for {
          meter <- createMeter(resource, scope)
          _ <- meter.observableUpDownCounter[Long](name).createObserver
          _ <- C.entries.assertEquals(
            consoleEntries("ObservableUpDownCounter", name)
          )
        } yield ()
      }
    }
  }

  test("create a noop ObservableGauge when the name is invalid") {
    PropF.forAllF(
      Gens.telemetryResource,
      Gens.instrumentationScope,
      invalidNameGen
    ) { (resource, scope, name) =>
      InMemoryDiagnostic.create[IO].flatMap { implicit C: InMemoryDiagnostic[IO] =>
        for {
          meter <- createMeter(resource, scope)
          _ <- meter.observableGauge[Long](name).createObserver
          _ <- C.entries.assertEquals(consoleEntries("ObservableGauge", name))
        } yield ()
      }
    }
  }

  private def createMeter(
      resource: TelemetryResource,
      scope: InstrumentationScope,
      start: FiniteDuration = Duration.Zero
  )(implicit C: Diagnostic[IO]): IO[SdkMeter[IO]] = {
    implicit val askContext: AskContext[IO] = Ask.const(Context.root)

    for {
      state <- InMemoryMeterSharedState.create[IO](resource, scope, start)
    } yield new SdkMeter[IO](state.state)
  }

  private def consoleEntries(instrument: String, name: String): List[Entry] =
    List(
      Entry.Error(
        s"SdkMeter: $instrument instrument has invalid name [$name]. " +
          "Using noop instrument. " +
          "Instrument names must consist of 255 or fewer characters including alphanumeric, _, ., -, and start with a letter.",
        None
      )
    )

}
