/*
 * Copyright 2022 Typelevel
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

import cats.Parallel
import cats.effect._
import cats.effect.kernel.Temporal
import cats.effect.std.Console
import cats.effect.std.Random
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.parallel._
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdkBuilder
import io.opentelemetry.sdk.metrics.Aggregation
import io.opentelemetry.sdk.metrics.InstrumentSelector
import io.opentelemetry.sdk.metrics.InstrumentType
import io.opentelemetry.sdk.metrics.View
import org.typelevel.otel4s.metrics.Histogram
import org.typelevel.otel4s.oteljava.OtelJava
import org.typelevel.otel4s.oteljava.context.LocalContextProvider

import java.{util => ju}
import java.util.concurrent.TimeUnit
import scala.concurrent.duration._

object HistogramBucketsExample extends IOApp.Simple {

  def work[F[_]: Temporal: Console](
      histogram: Histogram[F, Double],
      random: Random[F]
  ): F[Unit] =
    for {
      sleepDuration <- random.nextIntBounded(5000)
      _ <- histogram
        .recordDuration(TimeUnit.SECONDS)
        .surround(
          Temporal[F].sleep(sleepDuration.millis) >>
            Console[F].println(s"I'm working after [$sleepDuration ms]")
        )
    } yield ()

  def program[F[_]: Async: LocalContextProvider: Parallel: Console]: F[Unit] =
    OtelJava
      .autoConfigured(configureBuilder)
      .evalMap(_.meterProvider.get("histogram-example"))
      .use { meter =>
        for {
          random <- Random.scalaUtilRandom[F]
          histogram <- meter.histogram("service.work.duration").create
          _ <- work[F](histogram, random).parReplicateA_(50)
        } yield ()
      }

  def run: IO[Unit] =
    program[IO]

  private def configureBuilder(
      builder: AutoConfiguredOpenTelemetrySdkBuilder
  ): AutoConfiguredOpenTelemetrySdkBuilder =
    builder
      .addMeterProviderCustomizer { (meterProviderBuilder, _) =>
        meterProviderBuilder
          .registerView(
            InstrumentSelector
              .builder()
              .setName("service.work.duration")
              .setType(InstrumentType.HISTOGRAM)
              .build(),
            View
              .builder()
              .setName("service.work.duration")
              .setAggregation(
                Aggregation.explicitBucketHistogram(
                  ju.Arrays.asList(.005, .01, .025, .05, .075, .1, .25, .5)
                )
              )
              .build()
          )
      }

}
