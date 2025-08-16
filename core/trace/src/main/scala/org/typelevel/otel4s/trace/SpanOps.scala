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

package org.typelevel.otel4s
package trace

import cats.effect.kernel.MonadCancelThrow
import cats.effect.kernel.Resource
import cats.syntax.functor._
import cats.~>

sealed trait SpanOps[F[_]] {

  /** Creates a [[Span]]. The span requires to be ended '''explicitly''' by invoking `end`.
    *
    * This strategy can be used when it's necessary to end a span outside of the scope (e.g. async callback). Make sure
    * the span is ended properly.
    *
    * If the start timestamp is not configured explicitly in a builder, the `Clock[F].realTime` is used to determine the
    * timestamp.
    *
    * Leaked span:
    * {{{
    * val tracer: Tracer[F] = ???
    * val leaked: F[Unit] =
    *   tracer.spanBuilder("manual-span").build.startUnmanaged.flatMap { span =>
    *     span.setStatus(StatusCode.Ok, "all good")
    *   }
    * }}}
    *
    * Properly ended span:
    * {{{
    * val tracer: Tracer[F] = ???
    * val ok: F[Unit] =
    *   tracer.spanBuilder("manual-span").build.startUnmanaged.flatMap { span =>
    *     span.setStatus(StatusCode.Ok, "all good") >> span.end
    *   }
    * }}}
    *
    * @note
    *   the span started by the [[startUnmanaged]] isn't propagated automatically. Consider the following example:
    *   {{{
    * Tracer[F].span("auto").use { span =>
    *   Tracer[F].span("unmanaged").startUnmanaged.flatMap { unmanagedSpan => // child of the 'auto' span
    *     Tracer[F].span("child-1").use(span => ...) // 'child-1' is the child of the 'auto', not 'unmanaged'
    *   }
    * }
    *   }}}
    *   you must use `Tracer[F].childScope` to explicitly enable automated propagation:
    *   {{{
    * Tracer[F].span("auto").use { span =>
    *   Tracer[F].span("unmanaged").startUnmanaged.flatMap { unmanagedSpan =>
    *     Tracer[F].childScope(unmanagedSpan.context) {
    *       Tracer[F].span("child-1").use(span => ...) // 'child-1' is the child of the 'unmanaged'
    *     }
    *   }
    * }
    *   }}}
    *
    * @see
    *   [[use]], [[use_]], [[surround]], or [[resource]] for a managed lifecycle
    */
  def startUnmanaged: F[Span[F]]

  /** Creates a [[Span]] and a [[cats.effect.kernel.Resource Resource]] for using it. Unlike [[startUnmanaged]], the
    * lifecycle of the span is fully managed.
    *
    * The finalization strategy is determined by [[SpanFinalizer.Strategy]]. By default, the abnormal termination
    * (error, cancelation) is recorded.
    *
    * If the start timestamp is not configured explicitly in a builder, the `Clock[F].realTime` is used to determine the
    * timestamp.
    *
    * `Clock[F].realTime` is always used as the end timestamp.
    *
    * @note
    *   the name of the method can create a false perception of the behavior. However, the span started by the
    *   [[resource]] '''isn't propagated automatically''' to the resource closure. The propagation doesn't work because
    *   `Resource` abstraction is leaky regarding the fiber context propagation. The context:
    *   [[https://github.com/typelevel/otel4s/issues/194]].
    *
    * Consider the following example:
    * {{{
    * Tracer[F].span("my-resource-span").resource.use { case SpanOps.Res(span, trace) =>
    *   Tracer[F].currentSpanContext // returns `None`
    * }
    * }}}
    * you must evaluate the inner effect within the `trace` to propagate span details:
    * {{{
    * Tracer[F].span("my-resource-span").resource.use { case SpanOps.Res(span, trace) =>
    *   trace(Tracer[F].currentSpanContext) // returns `Some(SpanContext{traceId="...", spanId="...", ...})`
    * }
    * }}}
    *
    * `res.trace` encloses its contents within the "resource-span" span; anything not applied to `res.trace` will not
    * end up in the span.
    *
    * @see
    *   default finalization strategy [[SpanFinalizer.Strategy.reportAbnormal]]
    *
    * @see
    *   [[SpanOps.Res]] for the semantics and usage of the resource's value
    *
    * @see
    *   Tracing documentation [[https://typelevel.org/otel4s/instrumentation/tracing.html#tracing-a-resource]]
    *
    * @example
    *   Resource tracing:
    *   {{{
    * class Connection[F[_]]
    *
    * def createConnection[F[_]: Async: Tracer]: Resource[F, Connection[F]] =
    *   Resource.make(
    *     Tracer[F].span("connection#acquire").surround(Async[F].pure(new Connection[F]))
    *   )(_ => Tracer[F].span("connection#release").surround(Async[F].unit))
    *
    * def useConnection[F[_]: Async: Tracer](c: Connection[F]): F[Unit] =
    *   Tracer[F].span("use").surround(Async[F].sleep(5.seconds))
    *
    * (for {
    *   r <- Tracer[F].span("resource").resource
    *   c <- createConnection[F].mapK(r.trace)
    * } yield (r, c)).use { case (res, connection) =>
    *   res.trace(Tracer[F].span("connection#use").surround(useConnection(connection)))
    * }
    *   }}}
    *   The result spans:
    *   {{{
    * > resource
    *   > connection#acquire
    *   > connection#use
    *     > use
    *   > connection#release
    *   }}}
    *
    * @example
    *   Trace acquire and release steps:
    *   {{{
    * class Transactor[F[_]]
    * class Redis[F[_]]
    *
    * def createTransactor[F[_]: Async: Tracer]: Resource[F, Transactor[F]] =
    *   Resource.make(
    *     Tracer[F].span("transactor#acquire").surround(Async[F].pure(new Transactor[F]))
    *   )(_ => Tracer[F].span("transactor#release").surround(Async[F].sleep(300.millis)))
    *
    * def createRedis[F[_]: Async: Tracer]: Resource[F, Redis[F]] =
    *   Resource.make(
    *     Tracer[F].span("redis#acquire").surround(Async[F].pure(new Redis[F]).delayBy(200.millis))
    *   )(_ => Tracer[F].span("redis#release").surround(Async[F].sleep(100.millis)))
    *
    * def components[F[_]: Async: Tracer]: Resource[F, (Transactor[F], Redis[F])] =
    *   for {
    *     r <- Tracer[F].span("app_lifecycle").resource
    *     tx <- createTransactor[F].mapK(r.trace)
    *     redis <- createRedis[F].mapK(r.trace)
    *   } yield (tx, redis)
    *
    * def run[F[_]: Async: Tracer]: F[Unit] =
    *   components[F].use { case (transactor, redis) =>
    *     Tracer[F].span("app_run").surround(Async[F].sleep(200.millis))
    *   }
    *   }}}
    *   `app_run` isn't linked to the `app_lifecycle` span in any way. The result spans:
    *   {{{
    * > app_lifecycle
    *   > transactor#acquire
    *   > redis#acquire
    *   > redis#release
    *   > transactor#release
    * > app_run
    *   }}}
    */
  def resource: Resource[F, SpanOps.Res[F]]

  /** Creates and uses a [[Span]]. Unlike [[startUnmanaged]], the lifecycle of the span is fully managed. The span is
    * started and passed to `f` to produce the effect, and ended when the effect completes.
    *
    * The finalization strategy is determined by [[SpanFinalizer.Strategy]]. By default, the abnormal termination
    * (error, cancelation) is recorded.
    *
    * If the start timestamp is not configured explicitly in a builder, the `Clock[F].realTime` is used to determine the
    * timestamp.
    *
    * `Clock[F].realTime` is always used as the end timestamp.
    *
    * @see
    *   default finalization strategy [[SpanFinalizer.Strategy.reportAbnormal]]
    *
    * @see
    *   Tracing documentation [[https://typelevel.org/otel4s/instrumentation/tracing.html#creating-a-span]]
    *
    * @example
    *   {{{
    * val tracer: Tracer[F] = ???
    * val ok: F[Unit] =
    *   tracer.spanBuilder("auto-span").build.use { span =>
    *     span.setStatus(StatusCode.Ok, "all good")
    *   }
    *   }}}
    */
  def use[A](f: Span[F] => F[A]): F[A]

  /** Starts a span and ends it immediately.
    *
    * A shortcut for:
    * {{{
    * val tracer: Tracer[F] = ???
    * val ops: SpanOps[F] = tracer.spanBuilder("auto-span").build
    * ops.use(_ => F.unit) <-> ops.use_
    * }}}
    *
    * @see
    *   See [[use]] for more details regarding lifecycle strategy
    */
  def use_ : F[Unit]

  /** Starts a span, runs `fa` and ends the span once `fa` terminates, fails or gets interrupted.
    *
    * A shortcut for:
    * {{{
    * val tracer: Tracer[F] = ???
    * val ops: SpanOps[F] = tracer.spanBuilder("auto-span").build
    * ops.use(_ => fa) <-> ops.surround(fa)
    * }}}
    *
    * @see
    *   See [[use]] for more details regarding lifecycle strategy
    */
  final def surround[A](fa: F[A]): F[A] = use(_ => fa)

  /** Modify the context `F` using an implicit [[KindTransformer]] from `F` to `G`.
    */
  def mapK[G[_]: MonadCancelThrow](implicit
      F: MonadCancelThrow[F],
      kt: KindTransformer[F, G]
  ): SpanOps[G] =
    new SpanOps.MappedK(this)
}

object SpanOps {
  private[otel4s] trait Unsealed[F[_]] extends SpanOps[F]

  /** The span and associated natural transformation [[`trace`]] provided and managed by a call to [[SpanOps.resource]].
    * In order to trace something in the span, it must be applied to [[`trace`]].
    */
  sealed trait Res[F[_]] {

    /** The managed span. */
    def span: Span[F]

    /** A natural transformation that traces everything applied to it in the span.
      *
      * Consider the following example:
      * {{{
      * Tracer[F].span("my-resource-span").resource.use { case SpanOps.Res(span, trace) =>
      *   Tracer[F].currentSpanContext // returns `None`
      * }
      * }}}
      * you must evaluate the inner effect within the `trace` to propagate span details:
      * {{{
      * Tracer[F].span("my-resource-span").resource.use { case SpanOps.Res(span, trace) =>
      *   trace(Tracer[F].currentSpanContext) // returns `Some(SpanContext{traceId="...", spanId="...", ...})`
      * }
      * }}}
      * `trace` encloses its contents within the "resource-span" span.
      *
      * @note
      *   anything not applied to this [[cats.arrow.FunctionK FunctionK]] will not be traced.
      */
    def trace: F ~> F

    /** Modify the context `F` using an implicit [[KindTransformer]] from `F` to `G`.
      */
    def mapK[G[_]](implicit kt: KindTransformer[F, G]): Res[G] =
      Res(span.mapK[G], kt.liftFunctionK(trace))
  }

  object Res {
    private[this] final case class Impl[F[_]](span: Span[F], trace: F ~> F) extends Res[F]

    /** Creates a [[Res]] from a managed span and a natural transformation for tracing operations in the span.
      */
    def apply[F[_]](span: Span[F], trace: F ~> F): Res[F] =
      Impl(span, trace)

    /** Extracts [[Res.span `span`]] and [[Res.trace `trace`]] as a tuple. */
    def unapply[F[_]](res: Res[F]): Some[(Span[F], F ~> F)] =
      Some((res.span, res.trace))
  }

  /** Implementation for [[SpanOps.mapK]]. */
  private class MappedK[F[_]: MonadCancelThrow, G[_]: MonadCancelThrow](
      ops: SpanOps[F]
  )(implicit kt: KindTransformer[F, G])
      extends SpanOps[G] {
    def startUnmanaged: G[Span[G]] =
      kt.liftK(ops.startUnmanaged).map(_.mapK[G])

    def resource: Resource[G, Res[G]] =
      ops.resource.mapK(kt.liftK).map(res => res.mapK[G])

    def use[A](f: Span[G] => G[A]): G[A] =
      resource.use { res => res.trace(f(res.span)) }

    def use_ : G[Unit] = kt.liftK(ops.use_)
  }
}
