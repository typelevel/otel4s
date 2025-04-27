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
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.trace.SpanBuilder
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.Tracer

import java.util.concurrent.TimeUnit

// benchmarks/Jmh/run org.typelevel.otel4s.benchmarks.SpanBuilderBenchmark -prof gc
@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(2)
@Measurement(iterations = 40, time = 1)
@Warmup(iterations = 5, time = 1)
class SpanBuilderBenchmark {
  import SpanBuilderBenchmark._

  @Param(Array("noop", "oteljava", "sdk"))
  var backend: String = _
  var tracer: Tracer[IO] = _
  var finalizer: IO[Unit] = _

  @Benchmark
  def createSpanBuilder(): SpanBuilder[IO] =
    tracer.spanBuilder("name")

  @Benchmark
  def addParams(): SpanBuilder[IO] =
    tracer
      .spanBuilder("name")
      .addAttribute(Attribute("key", "value"))
      .addAttribute(Attribute("k", "v"))
      .addLink(SpanContext.invalid, Attribute("1", "2"))

  @Benchmark
  def addParamsAndUse(): Unit =
    tracer
      .spanBuilder("name")
      .addAttribute(Attribute("key", "value"))
      .addAttribute(Attribute("k", "v"))
      .addLink(SpanContext.invalid, Attribute("1", "2"))
      .build
      .use_
      .unsafeRunSync()

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

      case "sdk" =>
        val (t, release) = sdkTracer.allocated.unsafeRunSync()

        tracer = t
        finalizer = release

      case other =>
        sys.error(s"unknown backend [$other]")
    }

  @TearDown(Level.Trial)
  def cleanup(): Unit =
    finalizer.unsafeRunSync()

}

object SpanBuilderBenchmark {

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

  private def sdkTracer: Resource[IO, Tracer[IO]] = {
    import cats.effect.std.Random
    import org.typelevel.otel4s.context.LocalProvider
    import org.typelevel.otel4s.sdk.context.Context
    import org.typelevel.otel4s.sdk.testkit.trace.InMemorySpanExporter
    import org.typelevel.otel4s.sdk.trace.SdkTracerProvider
    import org.typelevel.otel4s.sdk.trace.processor.BatchSpanProcessor

    Resource.eval(InMemorySpanExporter.create[IO](None)).flatMap { exporter =>
      BatchSpanProcessor.builder(exporter).build.evalMap { processor =>
        Random.scalaUtilRandom[IO].flatMap { implicit random =>
          LocalProvider[IO, Context].local.flatMap { implicit local =>
            for {
              tracerProvider <- SdkTracerProvider.builder[IO].addSpanProcessor(processor).build
              tracer <- tracerProvider.get("trace-benchmark")
            } yield tracer
          }
        }
      }
    }
  }

  private def noopTracer: Tracer[IO] =
    Tracer.noop

}
