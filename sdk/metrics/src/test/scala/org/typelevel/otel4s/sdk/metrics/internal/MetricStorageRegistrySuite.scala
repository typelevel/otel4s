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

package org.typelevel.otel4s.sdk.metrics.internal

import cats.effect.IO
import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import org.scalacheck.effect.PropF
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.data.TimeWindow
import org.typelevel.otel4s.sdk.metrics.internal.storage.MetricStorage
import org.typelevel.otel4s.sdk.metrics.scalacheck.Gens
import org.typelevel.otel4s.sdk.metrics.view.View
import org.typelevel.otel4s.sdk.test.InMemoryDiagnostic

class MetricStorageRegistrySuite extends CatsEffectSuite with ScalaCheckEffectSuite {

  test("register a storage") {
    PropF.forAllF(Gens.instrumentDescriptor) { descriptor =>
      val metricDescriptor = MetricDescriptor(None, descriptor)
      val storage = metricStorage(metricDescriptor)

      for {
        registry <- MetricStorageRegistry.create[IO]
        _ <- registry.register(storage)
        storages <- registry.storages
      } yield assertEquals(storages, Vector(storage))
    }
  }

  test("warn about duplicates") {
    PropF.forAllF(Gens.instrumentDescriptor) { descriptor =>
      InMemoryDiagnostic.create[IO].flatMap { implicit C: InMemoryDiagnostic[IO] =>
        val first = MetricDescriptor(None, descriptor)
        val second = MetricDescriptor(
          Some(View.builder.withDescription("desc").build),
          descriptor
        )

        val consoleEntries = {
          import org.typelevel.otel4s.sdk.test.InMemoryDiagnostic._

          List(
            Entry.Error(
              s"MetricStorageRegistry: found a duplicate. The $second has similar descriptors in the storage: $first.",
              None
            )
          )
        }

        for {
          registry <- MetricStorageRegistry.create[IO]
          source <- registry.register(metricStorage(first))
          duplicate <- registry.register(metricStorage(second))
          storages <- registry.storages
          _ <- C.entries.assertEquals(consoleEntries)
        } yield assertEquals(storages, Vector(source, duplicate))
      }
    }
  }

  private def metricStorage(descriptor: MetricDescriptor): MetricStorage[IO] =
    new MetricStorage[IO] {
      def metricDescriptor: MetricDescriptor =
        descriptor

      def collect(
          resource: TelemetryResource,
          scope: InstrumentationScope,
          timeWindow: TimeWindow
      ): IO[Option[MetricData]] =
        IO.pure(None)
    }

}
