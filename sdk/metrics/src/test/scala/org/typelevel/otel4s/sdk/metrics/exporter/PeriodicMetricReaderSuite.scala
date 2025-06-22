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

package org.typelevel.otel4s.sdk.metrics.exporter

import cats.data.NonEmptyVector
import cats.effect.IO
import cats.effect.Ref
import cats.effect.Resource
import cats.effect.std.Console
import cats.effect.testkit.TestControl
import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import org.scalacheck.Test
import org.scalacheck.effect.PropF
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.scalacheck.Arbitraries._
import org.typelevel.otel4s.sdk.test.InMemoryConsole

import scala.concurrent.duration._
import scala.util.control.NoStackTrace

class PeriodicMetricReaderSuite extends CatsEffectSuite with ScalaCheckEffectSuite {

  test("export metrics with a fixed interval") {
    PropF.forAllF { (metrics: List[MetricData]) =>
      val producer = constProducer(metrics)

      TestControl.executeEmbed {
        for {
          exporter <- InMemoryMetricExporter.create[IO](None)
          _ <- makeReader(exporter).use { reader =>
            for {
              _ <- reader.register(NonEmptyVector.one(producer))

              // should be empty
              _ <- exporter.exportedMetrics.assertEquals(Nil)

              // first export
              _ <- IO.sleep(40.seconds)
              _ <- exporter.exportedMetrics.assertEquals(metrics)

              // in-between export interval, should export nothing
              _ <- IO.sleep(10.seconds)
              _ <- exporter.exportedMetrics.assertEquals(Nil)

              // second exporter
              _ <- IO.sleep(15.seconds)
              _ <- exporter.exportedMetrics.assertEquals(metrics)
            } yield ()
          }

          // should export metrics upon finalization
          _ <- exporter.exportedMetrics.assertEquals(metrics)

          // outside of the periodic reader lifecycle, should be empty
          _ <- IO.sleep(15.seconds)
          _ <- exporter.exportedMetrics.assertEquals(Nil)
        } yield ()
      }
    }
  }

  test("accept first register and ignore any subsequent one") {
    PropF.forAllF { (metrics: List[MetricData]) =>
      val producer = constProducer(metrics)

      TestControl.executeEmbed {
        InMemoryConsole.create[IO].flatMap { implicit C: InMemoryConsole[IO] =>
          val consoleEntries = {
            import org.typelevel.otel4s.sdk.test.InMemoryConsole._

            List(
              Entry(
                Op.Errorln,
                "MetricProducers are already registered at this periodic metric reader"
              )
            )
          }

          for {
            exporter <- InMemoryMetricExporter.create[IO](None)
            _ <- makeReader(exporter).use { reader =>
              for {
                _ <- reader.register(NonEmptyVector.one(producer))
                _ <- reader.register(NonEmptyVector.one(producer))

                _ <- C.entries.assertEquals(consoleEntries)

                // if both producers are registered, there will be 'metrics ++ metrics'
                _ <- IO.sleep(40.seconds)
                _ <- exporter.exportedMetrics.assertEquals(metrics)
              } yield ()
            }
          } yield ()
        }
      }
    }
  }

  test("keep running when a producer fails to produce metrics") {
    PropF.forAllF { (metrics: List[MetricData]) =>
      val e = new RuntimeException("Something went wrong") with NoStackTrace

      def producer(throwError: Ref[IO, Boolean]): MetricProducer[IO] =
        new MetricProducer[IO] {
          def produce: IO[Vector[MetricData]] =
            throwError.get.ifM(
              IO.raiseError(e),
              IO.pure(metrics.toVector)
            )
        }

      TestControl.executeEmbed {
        InMemoryConsole.create[IO].flatMap { implicit C: InMemoryConsole[IO] =>
          val consoleEntries = {
            import org.typelevel.otel4s.sdk.test.InMemoryConsole._

            List(
              Entry(
                Op.Errorln,
                s"PeriodicMetricReader: the export has failed: ${e.getMessage}\n${e.getStackTrace.mkString("\n")}\n"
              )
            )
          }

          for {
            exporter <- InMemoryMetricExporter.create[IO](None)
            _ <- makeReader(exporter).use { reader =>
              for {
                throwError <- IO.ref(false)

                _ <- reader.register(NonEmptyVector.one(producer(throwError)))

                // first successful export should happen
                _ <- IO.sleep(31.seconds)
                _ <- exporter.exportedMetrics.assertEquals(metrics)

                _ <- throwError.set(true)

                // second export will fail
                _ <- IO.sleep(31.seconds)
                _ <- exporter.exportedMetrics.assertEquals(Nil)
                _ <- C.entries.assertEquals(consoleEntries)

                _ <- throwError.set(false)

                // third successful export should happen
                _ <- IO.sleep(31.seconds)
                _ <- exporter.exportedMetrics.assertEquals(metrics)
              } yield ()
            }
          } yield ()
        }
      }
    }
  }

  test("terminate export task by the timeout") {
    val producer: MetricProducer[IO] =
      new MetricProducer[IO] {
        def produce: IO[Vector[MetricData]] =
          IO.never.as(Vector.empty)
      }

    TestControl.executeEmbed {
      InMemoryConsole.create[IO].flatMap { implicit C: InMemoryConsole[IO] =>
        val consoleEntries = {
          import org.typelevel.otel4s.sdk.test.InMemoryConsole._

          List(
            Entry(
              Op.Errorln,
              "PeriodicMetricReader: the export attempt has been canceled after [5 seconds]"
            )
          )
        }

        for {
          exporter <- InMemoryMetricExporter.create[IO](None)
          _ <- makeReader(exporter).use { reader =>
            for {
              _ <- reader.register(NonEmptyVector.one(producer))

              // nothing should be exported
              _ <- IO.sleep(60.seconds)
              _ <- exporter.exportedMetrics.assertEquals(Nil)
              _ <- C.entries.assertEquals(consoleEntries)
            } yield ()
          }
        } yield ()
      }
    }
  }

  private def makeReader(
      exporter: MetricExporter.Push[IO]
  )(implicit console: Console[IO]): Resource[IO, MetricReader[IO]] =
    MetricReader.periodic(exporter, 30.seconds, 5.seconds)

  private def constProducer(metrics: List[MetricData]): MetricProducer[IO] =
    new MetricProducer[IO] {
      def produce: IO[Vector[MetricData]] = IO.pure(metrics.toVector)
    }

  override protected def scalaCheckTestParameters: Test.Parameters =
    super.scalaCheckTestParameters
      .withMinSuccessfulTests(20)
      .withMaxSize(20)

}
