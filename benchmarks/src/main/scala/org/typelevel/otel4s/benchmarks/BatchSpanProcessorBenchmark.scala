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

  @Param(Array("oteljava"))
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

  }

}
