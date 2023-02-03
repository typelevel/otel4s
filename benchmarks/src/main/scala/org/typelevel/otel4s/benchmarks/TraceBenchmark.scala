package org.typelevel.otel4s.benchmarks

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.`export`.SimpleSpanProcessor
import org.openjdk.jmh.annotations._
import org.typelevel.otel4s.java.OtelJava
import org.typelevel.otel4s.trace.Tracer

import java.util.concurrent.TimeUnit

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class TraceBenchmark {

  import TraceBenchmark._

  @Benchmark
  def pure: Unit =
    IO.unit.unsafeRunSync()

  @Benchmark
  def noop(ctx: NoopTracer): Unit = {
    import ctx._
    tracer.span("span").use_.unsafeRunSync()
  }

  @Benchmark
  def inMemoryDisabled(ctx: InMemoryTracer): Unit = {
    import ctx._
    tracer.noopScope
      .surround(
        tracer.span("span").use_
      )
      .unsafeRunSync()
  }

  @Benchmark
  def inMemoryEnabled(ctx: InMemoryTracer): Unit = {
    import ctx._
    tracer.rootScope
      .surround(
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

      OtelJava.forSync(otel).flatMap {
        _.tracerProvider.tracer("trace-benchmark").get
      }
    }

    implicit val tracer: Tracer[IO] =
      makeTracer.unsafeRunSync()
  }
}
