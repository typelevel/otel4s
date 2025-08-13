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

import cats.Foldable
import cats.effect.IO
import cats.effect.Resource
import cats.effect.std.Random
import cats.effect.unsafe.implicits.global
import cats.syntax.foldable._
import org.openjdk.jmh.annotations._

import java.util.concurrent.TimeUnit
import scala.concurrent.duration._

// benchmarks/Jmh/run org.typelevel.otel4s.benchmarks.BatchSpanExporterBenchmark -prof gc
@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
@Threads(5)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
class BatchSpanProcessorBenchmark {

  import BatchSpanProcessorBenchmark._

  @Param(Array("oteljava", "sdk"))
  var backend: String = _

  @Param(Array("0", "1", "5"))
  var delayMs: Int = _

  @Param(Array("1000", "2000", "5000"))
  var spanCount: Int = _

  private var processor: Processor = _
  private var finalizer: IO[Unit] = _

  @Benchmark
  def doExport(): Unit =
    processor.doExport()

  @Setup(Level.Trial)
  def setup(): Unit =
    backend match {
      case "oteljava" =>
        val (proc, release) = Processor.otelJava(delayMs.millis, spanCount).allocated.unsafeRunSync()

        processor = proc
        finalizer = release

      case "sdk" =>
        val (proc, release) = Processor.sdk(delayMs.millis, spanCount).allocated.unsafeRunSync()

        processor = proc
        finalizer = release

      case other =>
        sys.error(s"unknown backend [$other]")
    }

  @TearDown(Level.Trial)
  def cleanup(): Unit =
    finalizer.unsafeRunSync()
}

object BatchSpanProcessorBenchmark {

  trait Processor {
    def doExport(): Unit
  }

  object Processor {

    def otelJava(delay: FiniteDuration, spanCount: Int): Resource[IO, Processor] = {
      import io.opentelemetry.api.trace.Span
      import io.opentelemetry.sdk.common.CompletableResultCode
      import io.opentelemetry.sdk.trace.{ReadableSpan, SdkTracerProvider}
      import io.opentelemetry.sdk.trace.data.SpanData
      import io.opentelemetry.sdk.trace.`export`.{BatchSpanProcessor, SpanExporter}
      import java.util.concurrent.Executors
      import java.util.concurrent.ScheduledExecutorService

      def toIO(codeIO: IO[CompletableResultCode]): IO[Unit] =
        codeIO.flatMap { code =>
          IO.async[Unit] { cb =>
            IO.delay {
              code.whenComplete { () =>
                cb(Either.cond(code.isSuccess, (), new RuntimeException("OpenTelemetry SDK async operation failed")))
              }
              None
            }
          }
        }

      def exporter(executor: ScheduledExecutorService): SpanExporter = new SpanExporter {
        def `export`(spans: java.util.Collection[SpanData]): CompletableResultCode = {
          val result = new CompletableResultCode()
          executor.schedule(() => result.succeed(), delay.toMillis, TimeUnit.MILLISECONDS)
          result
        }

        def flush(): CompletableResultCode =
          CompletableResultCode.ofSuccess()

        def shutdown(): CompletableResultCode =
          CompletableResultCode.ofSuccess()
      }

      val tracer = SdkTracerProvider.builder().build().get("benchmarkTracer")

      val spans: Vector[Span] =
        Vector.fill(spanCount)(tracer.spanBuilder("span").startSpan())

      def makeBsp(executor: ScheduledExecutorService) =
        BatchSpanProcessor
          .builder(exporter(executor))
          .setMaxQueueSize(spanCount * 2)
          .build

      for {
        executor <- Resource.make(IO.delay(Executors.newScheduledThreadPool(5)))(e => IO.delay(e.shutdown()))
        bsp <- Resource.make(IO.delay(makeBsp(executor)))(r => toIO(IO.delay(r.shutdown())))
      } yield new Processor {
        def doExport(): Unit = {
          spans.foreach(span => bsp.onEnd(span.asInstanceOf[ReadableSpan]))
          val _ = bsp.forceFlush().join(10, TimeUnit.MINUTES)
          ()
        }
      }
    }

    def sdk(delay: FiniteDuration, spanCount: Int): Resource[IO, Processor] = {
      import org.typelevel.otel4s.trace.{TraceFlags, TraceState}
      import org.typelevel.otel4s.trace.{SpanContext, SpanKind}
      import org.typelevel.otel4s.sdk.TelemetryResource
      import org.typelevel.otel4s.sdk.common.InstrumentationScope
      import org.typelevel.otel4s.sdk.trace.IdGenerator
      import org.typelevel.otel4s.sdk.data.LimitedData
      import org.typelevel.otel4s.sdk.trace.data.{SpanData, StatusData}
      import org.typelevel.otel4s.sdk.trace.exporter.SpanExporter
      import org.typelevel.otel4s.sdk.trace.processor.BatchSpanProcessor

      val exporter: SpanExporter[IO] = new SpanExporter.Unsealed[IO] {
        def name: String = s"DelayExporter($delay)"
        def exportSpans[G[_]: Foldable](spans: G[SpanData]): IO[Unit] = IO.sleep(delay)
        def flush: IO[Unit] = IO.unit
      }

      def mkSpanData(idGenerator: IdGenerator[IO], random: Random[IO]): IO[SpanData] =
        for {
          name <- random.nextString(20)
          traceId <- idGenerator.generateTraceId
          spanId <- idGenerator.generateSpanId
        } yield SpanData(
          name = name,
          spanContext = SpanContext(traceId, spanId, TraceFlags.Sampled, TraceState.empty, remote = false),
          parentSpanContext = None,
          kind = SpanKind.Internal,
          startTimestamp = Duration.Zero,
          endTimestamp = None,
          status = StatusData.Ok,
          attributes = LimitedData.attributes(Int.MaxValue, 1024),
          events = LimitedData.vector(Int.MaxValue),
          links = LimitedData.vector(Int.MaxValue),
          instrumentationScope = InstrumentationScope.empty,
          resource = TelemetryResource.empty
        )

      for {
        bsp <- BatchSpanProcessor.builder[IO](exporter).withMaxQueueSize(spanCount * 2).build
        spans <- Resource.eval(
          Random.scalaUtilRandom[IO].flatMap { implicit random =>
            val generator = IdGenerator.random[IO]
            mkSpanData(generator, random).replicateA(spanCount)
          }
        )
      } yield new Processor {
        def doExport(): Unit =
          (spans.traverse_(span => bsp.onEnd(span)) >> bsp.forceFlush).unsafeRunSync()
      }
    }

  }

}
