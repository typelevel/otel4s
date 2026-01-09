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

package org.typelevel.otel4s.benchmarks

import cats.effect.IO
import cats.effect.Resource
import cats.effect.unsafe.implicits.global
import cats.syntax.foldable._
import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.ThreadParams
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.metrics.Meter
import org.typelevel.otel4s.metrics.MeterProvider

import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit
import scala.util.chaining._

// benchmarks/Jmh/run org.typelevel.otel4s.benchmarks.MetricsBenchmark -prof gc
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(1)
class MetricsBenchmark {
  import MetricsBenchmark._

  @Benchmark
  @Threads(1)
  def recordToMultipleAttributes(state: BenchThreadState): Unit =
    AttributesList.traverse_(attributes => state.op.run(attributes)).unsafeRunSync()

  @Benchmark
  @Threads(1)
  def oneThread(state: BenchThreadState): Unit =
    state.op.run(SharedAttributes).unsafeRunSync()

  @Benchmark
  @Threads(8)
  def eightThreadsCommonLabelSet(state: BenchThreadState): Unit =
    state.op.run(SharedAttributes).unsafeRunSync()

  @Benchmark
  @Threads(8)
  def eightThreadsSeparateLabelSets(state: BenchThreadState): Unit =
    state.op.run(state.threadUniqueLabelSet).unsafeRunSync()

}

object MetricsBenchmark {

  private val AttributesList: List[Attributes] = {
    val keys = 5
    val valuePerKey = 5

    List.tabulate(keys) { key =>
      List
        .tabulate(valuePerKey) { value =>
          Attribute(s"key_$key", s"value_$value")
        }
        .to(Attributes)
    }
  }

  private val SharedAttributes: Attributes =
    Attributes(Attribute("key", "value"))

  trait BenchOperation {
    def run(attributes: Attributes): IO[Unit]
  }

  @State(Scope.Benchmark)
  class BenchThreadState {

    @Param(Array("oteljava", "noop"))
    var backend: String = _

    @Param(Array("noop", "sdk_cumulative", "sdk_delta"))
    var variation: String = _

    @Param(Array("long_counter", "double_counter", "double_histogram", "long_histogram"))
    var operation: String = _

    var op: BenchOperation = _
    var threadUniqueLabelSet: Attributes = _

    private var finalizer: IO[Unit] = _

    @Setup()
    def setup(params: ThreadParams): Unit = {
      threadUniqueLabelSet = Attributes(Attribute("key", params.getThreadIndex.toString))

      backend match {
        case "oteljava" =>
          val (o, release) = otelJavaMeter(variation).evalMap(bench(operation, _)).allocated.unsafeRunSync()

          op = o
          finalizer = release

        case "noop" =>
          op = bench(operation, noopMeter).unsafeRunSync()
          finalizer = IO.unit

        case other =>
          sys.error(s"unknown backend [$other]")
      }
    }

    @TearDown()
    def cleanup(): Unit =
      finalizer.unsafeRunSync()

  }

  private def bench(operation: String, meter: Meter[IO]): IO[BenchOperation] = {
    operation match {
      case "long_counter" =>
        for {
          counter <- meter.counter[Long]("counter.long").create
        } yield new BenchOperation {
          def run(attributes: Attributes): IO[Unit] = counter.add(5L, attributes)
        }

      case "double_counter" =>
        for {
          counter <- meter.counter[Double]("counter.double").create
        } yield new BenchOperation {
          def run(attributes: Attributes): IO[Unit] = counter.add(5.0, attributes)
        }

      case "long_histogram" =>
        for {
          histogram <- meter.histogram[Long]("histogram.long").create
        } yield new BenchOperation {
          def run(attributes: Attributes): IO[Unit] =
            histogram.record(ThreadLocalRandom.current().nextLong(0, 20000), attributes)
        }

      case "double_histogram" =>
        for {
          histogram <- meter.histogram[Double]("histogram.double").create
        } yield new BenchOperation {
          def run(attributes: Attributes): IO[Unit] =
            histogram.record(ThreadLocalRandom.current().nextDouble(0, 20000.0), attributes)
        }

      case other =>
        sys.error(s"unknown operation [$other]")
    }
  }

  private def otelJavaMeter(variation: String): Resource[IO, Meter[IO]] = {
    import io.opentelemetry.sdk.OpenTelemetrySdk
    import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader
    import io.opentelemetry.sdk.metrics.SdkMeterProvider
    import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder
    import org.typelevel.otel4s.oteljava.OtelJava

    val namespace = "otel4s.sdk.metrics"

    def create(
        reader: InMemoryMetricReader,
        customize: SdkMeterProviderBuilder => SdkMeterProviderBuilder = identity,
    ): Resource[IO, Meter[IO]] = {

      def meterProvider = SdkMeterProvider
        .builder()
        .registerMetricReader(reader)
        .pipe(customize)
        .build()

      def otel = OpenTelemetrySdk
        .builder()
        .setMeterProvider(meterProvider)
        .build()

      OtelJava
        .resource[IO](IO(otel))
        .evalMap(_.meterProvider.meter("otel4s.sdk.metrics").get)
    }

    variation match {
      case "noop" =>
        Resource.eval(MeterProvider.noop[IO].get(namespace))

      case "sdk_cumulative" =>
        create(InMemoryMetricReader.create())

      case "sdk_delta" =>
        create(InMemoryMetricReader.createDelta())

      case other =>
        sys.error(s"unknown variation [$other]")
    }
  }

  private def noopMeter: Meter[IO] =
    Meter.noop

}
