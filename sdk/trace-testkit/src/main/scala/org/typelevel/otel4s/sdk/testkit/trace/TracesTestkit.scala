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

package org.typelevel.otel4s.sdk.testkit.trace

import cats.FlatMap
import cats.Parallel
import cats.effect.Async
import cats.effect.Resource
import cats.effect.std.Console
import cats.effect.std.Random
import cats.syntax.flatMap._
import org.typelevel.otel4s.context.LocalProvider
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.context.LocalContext
import org.typelevel.otel4s.sdk.context.LocalContextProvider
import org.typelevel.otel4s.sdk.trace.SdkTracerProvider
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.sdk.trace.processor.SimpleSpanProcessor
import org.typelevel.otel4s.sdk.trace.processor.SpanProcessor
import org.typelevel.otel4s.trace.TracerProvider

trait TracesTestkit[F[_]] {

  /** The [[org.typelevel.otel4s.trace.TracerProvider TracerProvider]].
    */
  def tracerProvider: TracerProvider[F]

  /** The list of finished spans.
    *
    * @note
    *   each invocation cleans up the internal buffer.
    */
  def finishedSpans: F[List[SpanData]]
}

object TracesTestkit {

  /** Creates [[TracesTestkit]] that keeps spans in-memory.
    *
    * @param customize
    *   the customization of the builder
    */
  def inMemory[F[_]: Async: Parallel: Console: LocalContextProvider](
      customize: SdkTracerProvider.Builder[F] => SdkTracerProvider.Builder[F] = (b: SdkTracerProvider.Builder[F]) => b
  ): Resource[F, TracesTestkit[F]] = {
    def createTracerProvider(
        processor: SpanProcessor[F]
    )(implicit local: LocalContext[F]): F[TracerProvider[F]] =
      Random.scalaUtilRandom[F].flatMap { implicit random =>
        val builder = SdkTracerProvider.builder[F].addSpanProcessor(processor)
        customize(builder).build
      }

    for {
      local <- Resource.eval(LocalProvider[F, Context].local)
      exporter <- Resource.eval(InMemorySpanExporter.create[F](None))
      processor <- Resource.pure(SimpleSpanProcessor(exporter))
      tracerProvider <- Resource.eval(createTracerProvider(processor)(local))
    } yield new Impl(tracerProvider, processor, exporter)
  }

  private final class Impl[F[_]: FlatMap](
      val tracerProvider: TracerProvider[F],
      processor: SpanProcessor[F],
      exporter: InMemorySpanExporter[F]
  ) extends TracesTestkit[F] {
    def finishedSpans: F[List[SpanData]] =
      processor.forceFlush >> exporter.finishedSpans
  }

}
