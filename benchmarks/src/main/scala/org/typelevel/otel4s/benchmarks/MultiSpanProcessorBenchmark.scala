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

import io.opentelemetry.context.Context
import io.opentelemetry.sdk.trace.ReadWriteSpan
import org.openjdk.jmh.annotations._

import java.util.concurrent.TimeUnit

// benchmarks/Jmh/run org.typelevel.otel4s.benchmarks.MultiSpanProcessorBenchmark -prof gc
@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
class MultiSpanProcessorBenchmark {

  import MultiSpanProcessorBenchmark._

  @Param(Array("oteljava"))
  var backend: String = _

  @Param(Array("1", "5", "10"))
  var processorCount: Int = _

  @Param(Array("1"))
  var spanCount: Int = _

  private var processor: Processor = _

  @Benchmark
  def onEnd(): Unit =
    processor.onEnd()

  @Setup(Level.Trial)
  def setup(): Unit =
    backend match {
      case "oteljava" =>
        processor = Processor.otelJava(processorCount)

      case other =>
        sys.error(s"unknown backend [$other]")
    }

}

object MultiSpanProcessorBenchmark {

  trait Processor {
    def onEnd(): Unit
  }

  object Processor {

    def otelJava(processorCount: Int): Processor = {
      import io.opentelemetry.api.trace.Span
      import io.opentelemetry.sdk.trace.{ReadableSpan, SdkTracerProvider}
      import io.opentelemetry.sdk.trace.SpanProcessor
      import scala.jdk.CollectionConverters._

      val tracer = SdkTracerProvider.builder().build().get("benchmarkTracer")

      val span: Span =
        tracer.spanBuilder("span").startSpan()

      val processor = new SpanProcessor {
        def onStart(parentContext: Context, span: ReadWriteSpan): Unit = ()
        def isStartRequired: Boolean = true
        def onEnd(span: ReadableSpan): Unit = ()
        def isEndRequired: Boolean = true
      }

      val proc = SpanProcessor.composite(List.fill(processorCount)(processor).asJava)

      new Processor {
        def onEnd(): Unit =
          proc.onEnd(span.asInstanceOf[ReadableSpan])
      }
    }

  }

}
