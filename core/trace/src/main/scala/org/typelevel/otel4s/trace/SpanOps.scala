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

import cats.data.OptionT
import cats.effect.MonadCancel
import cats.effect.Resource
import cats.effect.kernel.CancelScope
import cats.effect.kernel.MonadCancelThrow
import cats.effect.kernel.Poll
import cats.syntax.functor._
import cats.~>

trait SpanOps[F[_]] {

  /** Creates a [[Span]]. The span requires to be ended '''explicitly''' by
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
    *       // `res.trace` encloses its contents within the "resource-span"
    *       // span; anything not applied to `res.include` will not end up in
    *       // the span
    *       res.trace(res.span.setStatus(Status.Ok, "all good"))
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

    implicit final class SpanOpsResSyntax[F[_]](
        private val res: Res[F]
    ) extends AnyVal {

      def translate[G[_]](fk: F ~> G, gk: G ~> F): Res[G] =
        new Res[G] {
          def span: Span[G] = res.span.mapK(fk)
          def trace: G ~> G = res.trace.andThen(fk).compose(gk)
        }

    }
  }

  implicit final class SpanOpsSyntax[F[_]](
      private val ops: SpanOps[F]
  ) extends AnyVal {

    def translate[G[_]](fk: F ~> G, gk: G ~> F)(implicit
        F: MonadCancelThrow[F]
    ): SpanOps[G] =
      new SpanOps[G] {
        private implicit val G: MonadCancelThrow[G] =
          liftMonadCancelThrow(F, fk, gk)

        def startUnmanaged: G[Span[G]] =
          fk(ops.startUnmanaged).map(span => span.mapK(fk))

        def resource: Resource[G, Res[G]] =
          ops.resource.map(res => res.translate(fk, gk)).mapK(fk)

        def use[A](f: Span[G] => G[A]): G[A] =
          fk(ops.use(span => gk(f(span.mapK(fk)))))

        def use_ : G[Unit] =
          fk(ops.use_)
      }

  }

  def liftOptionT[F[_]](ops: SpanOps[F])(implicit
      F: MonadCancel[F, _]
  ): SpanOps[OptionT[F, *]] = {
    new SpanOps[OptionT[F, *]] {

      def startUnmanaged: OptionT[F, Span[OptionT[F, *]]] =
        OptionT.liftF(ops.startUnmanaged).map(span => span.mapK(OptionT.liftK))

      def resource: Resource[OptionT[F, *], Res[OptionT[F, *]]] =
        ops.resource
          .map { res =>
            new Res[OptionT[F, *]] {
              def span: Span[OptionT[F, *]] =
                res.span.mapK(OptionT.liftK)

              def trace: OptionT[F, *] ~> OptionT[F, *] =
                new (OptionT[F, *] ~> OptionT[F, *]) {
                  def apply[A](fa: OptionT[F, A]): OptionT[F, A] =
                    OptionT(res.trace.apply(fa.value))
                }
            }
          }
          .mapK(OptionT.liftK)

      def use[A](f: Span[OptionT[F, *]] => OptionT[F, A]): OptionT[F, A] =
        OptionT(ops.use(span => f(span.mapK(OptionT.liftK)).value))

      def use_ : OptionT[F, Unit] =
        OptionT.liftF(ops.use_)
    }
  }

  private def liftMonadCancelThrow[F[_], G[_]](
      F: MonadCancelThrow[F],
      fk: F ~> G,
      gk: G ~> F
  ): MonadCancelThrow[G] =
    new MonadCancelThrow[G] {
      def pure[A](x: A): G[A] = fk(F.pure(x))

      // Members declared in cats.ApplicativeError
      def handleErrorWith[A](ga: G[A])(f: Throwable => G[A]): G[A] =
        fk(F.handleErrorWith(gk(ga))(ex => gk(f(ex))))

      def raiseError[A](e: Throwable): G[A] = fk(F.raiseError[A](e))

      // Members declared in cats.FlatMap
      def flatMap[A, B](ga: G[A])(f: A => G[B]): G[B] =
        fk(F.flatMap(gk(ga))(a => gk(f(a))))

      def tailRecM[A, B](a: A)(f: A => G[Either[A, B]]): G[B] =
        fk(F.tailRecM(a)(a => gk(f(a))))

      // Members declared in cats.effect.kernel.MonadCancel
      def canceled: G[Unit] = fk(F.canceled)

      def forceR[A, B](ga: G[A])(gb: G[B]): G[B] =
        fk(F.forceR(gk(ga))(gk(gb)))

      def onCancel[A](ga: G[A], fin: G[Unit]): G[A] =
        fk(F.onCancel(gk(ga), gk(fin)))

      def rootCancelScope: CancelScope = F.rootCancelScope

      def uncancelable[A](body: Poll[G] => G[A]): G[A] =
        fk(F.uncancelable { pollF =>
          gk(body(new Poll[G] {
            def apply[B](gb: G[B]): G[B] = fk(pollF(gk(gb)))
          }))
        })
    }
}
