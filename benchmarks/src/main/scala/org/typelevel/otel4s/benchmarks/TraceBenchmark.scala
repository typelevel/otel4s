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
import org.openjdk.jmh.annotations._
import org.typelevel.otel4s.trace.Tracer

import java.util.concurrent.TimeUnit

// benchmarks/Jmh/run org.typelevel.otel4s.benchmarks.TraceBenchmark -prof gc
@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(2)
@Measurement(iterations = 40, time = 1)
@Warmup(iterations = 5, time = 1)
class TraceBenchmark {

  import TraceBenchmark._

  @Param(Array("noop", "oteljava", "sdk"))
  var backend: String = _
  var tracer: Tracer[IO] = _
  var finalizer: IO[Unit] = _

  @Benchmark
  def pure(): Unit =
    IO.unit.unsafeRunSync()

  @Benchmark
  def span(): Unit =
    tracer.span("span").use_.unsafeRunSync()

  @Benchmark
  def noopScope(): Unit =
    tracer.noopScope(tracer.span("span").use_).unsafeRunSync()

  @Benchmark
  def rootScope(): Unit =
    tracer.rootScope(tracer.span("span").use_).unsafeRunSync()

  @Setup(Level.Trial)
  def setup(): Unit =
    backend match {
      case "noop" =>
        tracer = noopTracer
        finalizer = IO.unit

      case "oteljava" =>
        val (t, release) = otelJavaTracer.allocated.unsafeRunSync()

        tracer = t
        finalizer = release

      case other =>
        sys.error(s"unknown backend [$other]")
    }

  @TearDown(Level.Trial)
  def cleanup(): Unit =
    finalizer.unsafeRunSync()
}

object TraceBenchmark {

  private def otelJavaTracer: Resource[IO, Tracer[IO]] = {
    import io.opentelemetry.sdk.OpenTelemetrySdk
    import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
    import io.opentelemetry.sdk.trace.SdkTracerProvider
    import io.opentelemetry.sdk.trace.`export`.BatchSpanProcessor
    import org.typelevel.otel4s.oteljava.OtelJava

    def exporter = InMemorySpanExporter.create()

    def builder = SdkTracerProvider
      .builder()
      .addSpanProcessor(BatchSpanProcessor.builder(exporter).build())

    def tracerProvider: SdkTracerProvider =
      builder.build()

    def otel = OpenTelemetrySdk
      .builder()
      .setTracerProvider(tracerProvider)
      .build()

    OtelJava
      .resource[IO](IO(otel))
      .evalMap(_.tracerProvider.tracer("trace-benchmark").get)
  }

  private def noopTracer: Tracer[IO] =
    Tracer.noop

}
