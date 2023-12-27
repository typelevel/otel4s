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
import cats.effect.unsafe.implicits.global
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.`export`.SimpleSpanProcessor
import org.openjdk.jmh.annotations._
import org.typelevel.otel4s.oteljava.OtelJava
import org.typelevel.otel4s.trace.Tracer

import java.util.concurrent.TimeUnit

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class TraceBenchmark {

  import TraceBenchmark._

  @Benchmark
  def pure(): Unit =
    IO.unit.unsafeRunSync()

  @Benchmark
  def noop(ctx: NoopTracer): Unit = {
    import ctx._
    tracer.span("span").use_.unsafeRunSync()
  }

  @Benchmark
  def inMemoryDisabled(ctx: InMemoryTracer): Unit = {
    import ctx._
    tracer
      .noopScope(
        tracer.span("span").use_
      )
      .unsafeRunSync()
  }

  @Benchmark
  def inMemoryEnabled(ctx: InMemoryTracer): Unit = {
    import ctx._
    tracer
      .rootScope(
        tracer.span("span").use_
      )
      .unsafeRunSync()
  }
}

object TraceBenchmark {
  @State(Scope.Benchmark)
  class NoopTracer {
    implicit val tracer: Tracer[IO] = Tracer.noop
  }

  @State(Scope.Benchmark)
  class InMemoryTracer {
    private def makeTracer: IO[Tracer[IO]] = {
      val exporter = InMemorySpanExporter.create()

      val builder = SdkTracerProvider
        .builder()
        .addSpanProcessor(SimpleSpanProcessor.create(exporter))

      val tracerProvider: SdkTracerProvider =
        builder.build()

      val otel = OpenTelemetrySdk
        .builder()
        .setTracerProvider(tracerProvider)
        .build()

      OtelJava.forAsync(otel).flatMap {
        _.tracerProvider.tracer("trace-benchmark").get
      }
    }

    implicit val tracer: Tracer[IO] =
      makeTracer.unsafeRunSync()
  }
}
