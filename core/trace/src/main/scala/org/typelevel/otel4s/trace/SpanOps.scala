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

trait SpanOps[F[_]] {
  type Result <: Span[F]

  /** Creates a [[Span]]. The span requires to be ended ''explicitly'' by
    * invoking `end`.
    *
    * This strategy can be used when it's necessary to end a span outside of the
    * scope (e.g. async callback). Make sure the span is ended properly.
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
    *   [[use]], [[use_]], or [[surround]] for a managed lifecycle
    */
  def startUnmanaged(implicit ev: Result =:= Span[F]): F[Span[F]]

  /** Creates and uses a [[Span]]. Unlike [[startUnmanaged]], the lifecycle of
    * the span is fully managed. The span is started and passed to `f` to
    * produce the effect, and ended when the effect completes.
    *
    * The finalization strategy is determined by [[SpanFinalizer.Strategy]]. By
    * default, the abnormal termination (error, cancelation) is recorded.
    *
    * @see
    *   default finalization strategy [[SpanFinalizer.Strategy.reportAbnormal]]
    *
    * @example
    *   {{{
    * val tracer: Tracer[F] = ???
    * val ok: F[Unit] =
    *   tracer.spanBuilder("auto-span").build.use { span =>
    *     span.setStatus(Status.Ok, "all good")
    *   }
    *   }}}
    */
  def use[A](f: Result => F[A]): F[A]

  def use_ : F[Unit]

  def surround[A](fa: F[A]): F[A]
}

object SpanOps {
  type Aux[F[_], A] = SpanOps[F] {
    type Result = A
  }
}
