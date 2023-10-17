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

package org.typelevel.otel4s.java

import cats.effect.Async
import cats.effect.IOLocal
import cats.effect.LiftIO
import cats.effect.Resource
import cats.effect.Sync
import cats.mtl.Local
import cats.syntax.all._
import io.opentelemetry.api.{OpenTelemetry => JOpenTelemetry}
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.context.{Context => JContext}
import io.opentelemetry.sdk.{OpenTelemetrySdk => JOpenTelemetrySdk}
import org.typelevel.otel4s.ContextPropagators
import org.typelevel.otel4s.Otel4s
import org.typelevel.otel4s.java.Conversions.asyncFromCompletableResultCode
import org.typelevel.otel4s.java.context.Context
import org.typelevel.otel4s.java.context.LocalContext
import org.typelevel.otel4s.java.instances._
import org.typelevel.otel4s.java.metrics.Metrics
import org.typelevel.otel4s.java.trace.Traces
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.trace.TracerProvider

import scala.util.Using

sealed abstract class OtelJava[F[_]] private (
    val propagators: ContextPropagators[F, Context],
    val meterProvider: MeterProvider[F],
    val tracerProvider: TracerProvider[F],
) extends Otel4s[F] {
  type Ctx = Context

  /** Runs the given `fa` with the given `JContext`.
    *
    * Can be used to run the effect with the external Open Telemetry Java
    * context.
    *
    * @see
    *   [[useJContextUnsafe]]
    *
    * @param ctx
    *   the Open Telemetry Java Context
    *
    * @param fa
    *   the effect to run
    */
  def withJContext[A](ctx: JContext)(fa: F[A]): F[A]

  /** Extracts the currently active context and sets it into the thread local
    * variable.
    *
    * Can be used to interop with the Java libraries, that rely on the Open
    * Telemetry Java context.
    *
    * @example
    *   {{{
    * import io.opentelemetry.api.trace.{Span => JSpan}
    * import io.opentelemetry.api.trace.{Tracer => JTracer}
    * import io.opentelemetry.context.{Context => JContext}
    *
    * val jTracer: JTracer = ???
    * val dispatcher: Dispatcher[IO] = ???
    * val otel4s: OtelJava[IO] = ???
    * val otel4sTracer: Tracer[IO] = ???
    *
    * val io = otel4sTracer.span("otel4s-span").surround {
    *   otel4s.useJContextUnsafe { _ =>
    *     val span = jTracer.spanBuilder("java-span-inside-io").startSpan()
    *     span.storeInContext(JContext.current()).makeCurrent()
    *     // invoke java code, e.g. instrumented RabbitMQ Java client
    *     span.end()
    *   }
    * }
    *
    * val span = jTracer.span("java-span").startSpan()
    * span.storeInContext(JContext.current()).makeCurrent() // store span in the context
    * otel4s.withJContext(JContext.current())(io).unsafeRunSync() // run IO using the external context
    * span.end()
    *   }}}
    *
    * The hierarchy of spans will be:
    * {{{
    * > java-span
    *   > otel4s-span
    *     > java-span-inside-io
    * }}}
    */
  def useJContextUnsafe[A](fa: JContext => A): F[A]
}

object OtelJava {

  /** Creates an [[org.typelevel.otel4s.Otel4s]] from a Java OpenTelemetry
    * instance.
    *
    * @param jOtel
    *   A Java OpenTelemetry instance. It is the caller's responsibility to shut
    *   this down. Failure to do so may result in lost metrics and traces.
    *
    * @return
    *   An effect of an [[org.typelevel.otel4s.Otel4s]] resource.
    */
  def forAsync[F[_]: LiftIO: Async](jOtel: JOpenTelemetry): F[OtelJava[F]] =
    IOLocal(Context.root)
      .map { implicit ioLocal: IOLocal[Context] =>
        local[F](jOtel)
      }
      .to[F]

  def local[F[_]: Async: LocalContext](
      jOtel: JOpenTelemetry
  ): OtelJava[F] = {
    val contextPropagators = new ContextPropagatorsImpl[F](jOtel.getPropagators)

    val metrics = Metrics.forAsync(jOtel)
    val traces = Traces.local(jOtel, contextPropagators)
    new OtelJava[F](
      contextPropagators,
      metrics.meterProvider,
      traces.tracerProvider,
    ) {
      override def toString: String = jOtel.toString

      def withJContext[A](ctx: JContext)(fa: F[A]): F[A] =
        Local[F, Context].scope(fa)(Context.wrap(ctx))

      def useJContextUnsafe[A](fa: JContext => A): F[A] =
        Local[F, Context].ask.flatMap { ctx =>
          Async[F].fromTry {
            val jContext = ctx.underlying
            Using(jContext.makeCurrent())(_ => fa(jContext))
          }
        }
    }
  }

  /** Lifts the acquisition of a Java OpenTelemetrySdk instance to a Resource.
    *
    * @param acquire
    *   OpenTelemetrySdk resource
    *
    * @return
    *   An [[org.typelevel.otel4s.Otel4s]] resource.
    */
  def resource[F[_]: LiftIO: Async](
      acquire: F[JOpenTelemetrySdk]
  ): Resource[F, OtelJava[F]] =
    Resource
      .make(acquire)(sdk =>
        asyncFromCompletableResultCode(Sync[F].delay(sdk.shutdown()))
      )
      .evalMap(forAsync[F])

  /** Creates an [[org.typelevel.otel4s.Otel4s]] from the global Java
    * OpenTelemetry instance.
    */
  def global[F[_]: LiftIO: Async]: F[OtelJava[F]] =
    Sync[F].delay(GlobalOpenTelemetry.get).flatMap(forAsync[F])
}
