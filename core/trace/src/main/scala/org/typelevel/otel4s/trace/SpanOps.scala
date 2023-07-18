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

package org.typelevel.otel4s.trace

import cats.effect.Resource
import cats.~>

trait SpanOps[F[_]] {

  /** Creates a [[Span]]. The span requires to be ended ''explicitly'' by
    * invoking `end`.
    *
    * This strategy can be used when it's necessary to end a span outside of the
    * scope (e.g. async callback). Make sure the span is ended properly.
    *
    * If the start timestamp is not configured explicitly in a builder, the
    * `Clock[F].realTime` is used to determine the timestamp.
    *
    * Leaked span:
    * {{{
    * val tracer: Tracer[F] = ???
    * val leaked: F[Unit] =
    *   tracer.spanBuilder("manual-span").build.startUnmanaged.flatMap { span =>
    *     span.setStatus(Status.Ok, "all good")
    *   }
    * }}}
    *
    * Properly ended span:
    * {{{
    * val tracer: Tracer[F] = ???
    * val ok: F[Unit] =
    *   tracer.spanBuilder("manual-span").build.startUnmanaged.flatMap { span =>
    *     span.setStatus(Status.Ok, "all good") >> span.end
    *   }
    * }}}
    *
    * @see
    *   [[use]], [[use_]], [[surround]], or [[resource]] for a managed lifecycle
    */
  def startUnmanaged: F[Span[F]]

  /** Creates a [[Span]] and a [[cats.effect.Resource Resource]] for using it.
    * Unlike [[startUnmanaged]], the lifecycle of the span is fully managed.
    *
    * The finalization strategy is determined by [[SpanFinalizer.Strategy]]. By
    * default, the abnormal termination (error, cancelation) is recorded.
    *
    * If the start timestamp is not configured explicitly in a builder, the
    * `Clock[F].realTime` is used to determine the timestamp.
    *
    * `Clock[F].realTime` is always used as the end timestamp.
    *
    * @see
    *   default finalization strategy [[SpanFinalizer.Strategy.reportAbnormal]]
    * @see
    *   [[SpanOps.Res]] for the semantics and usage of the resource's value
    * @example
    *   {{{
    * val tracer: Tracer[F] = ???
    * val ok: F[Unit] =
    *   tracer.spanBuilder("resource-span")
    *     .build
    *     .resource
    *     .use { res =>
    *       // `res.include` encloses its contents within the "resource-span"
    *       // span; anything not applied to `res.include` will not end up in
    *       // the span
    *       res.include(res.span.setStatus(Status.Ok, "all good"))
    *     }
    *   }}}
    */
  def resource: Resource[F, SpanOps.Res[F]]

  /** Creates and uses a [[Span]]. Unlike [[startUnmanaged]], the lifecycle of
    * the span is fully managed. The span is started and passed to `f` to
    * produce the effect, and ended when the effect completes.
    *
    * The finalization strategy is determined by [[SpanFinalizer.Strategy]]. By
    * default, the abnormal termination (error, cancelation) is recorded.
    *
    * If the start timestamp is not configured explicitly in a builder, the
    * `Clock[F].realTime` is used to determine the timestamp.
    *
    * `Clock[F].realTime` is always used as the end timestamp.
    *
    * @see
    *   default finalization strategy [[SpanFinalizer.Strategy.reportAbnormal]]
    * @example
    *   {{{
    * val tracer: Tracer[F] = ???
    * val ok: F[Unit] =
    *   tracer.spanBuilder("auto-span").build.use { span =>
    *     span.setStatus(Status.Ok, "all good")
    *   }
    *   }}}
    */
  def use[A](f: Span[F] => F[A]): F[A]

  /** Starts a span and ends it immediately.
    *
    * A shortcut for:
    * {{{
    * val tracer: Tracer[F] = ???
    * val ops: SpanOps.Aux[F, Span[F]] = tracer.spanBuilder("auto-span").build
    * ops.use(_ => F.unit) <-> ops.use_
    * }}}
    *
    * @see
    *   See [[use]] for more details regarding lifecycle strategy
    */
  def use_ : F[Unit]

  /** Starts a span, runs `fa` and ends the span once `fa` terminates, fails or
    * gets interrupted.
    *
    * A shortcut for:
    * {{{
    * val tracer: Tracer[F] = ???
    * val ops: SpanOps.Aux[F, Span[F]] = tracer.spanBuilder("auto-span").build
    * ops.use(_ => fa) <-> ops.surround(fa)
    * }}}
    *
    * @see
    *   See [[use]] for more details regarding lifecycle strategy
    */
  final def surround[A](fa: F[A]): F[A] = use(_ => fa)
}

object SpanOps {

  /** The span and associated natural transformation [[`trace`]] provided and
    * managed by a call to [[SpanOps.resource]]. In order to trace something in
    * the span, it must be applied to [[`trace`]].
    */
  sealed trait Res[F[_]] {

    /** The managed span. */
    def span: Span[F]

    /** A natural transformation that traces everything applied to it in the
      * span. Note: anything not applied to this
      * [[cats.arrow.FunctionK FunctionK]] will not be traced.
      */
    def trace: F ~> F
  }

  object Res {
    private[this] final case class Impl[F[_]](span: Span[F], trace: F ~> F)
        extends Res[F]

    /** Creates a [[Res]] from a managed span and a natural transformation for
      * tracing operations in the span.
      */
    def apply[F[_]](span: Span[F], trace: F ~> F): Res[F] =
      Impl(span, trace)
  }
}
