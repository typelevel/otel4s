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

package org.typelevel.otel4s.context

import cats.mtl.LiftKind
import cats.mtl.Local
import cats.~>

/** A utility for capturing and restoring the OpenTelemetry context associated with an effect.
  *
  * A captured context is the immutable context value itself. It can be retained and later passed to [[scope]] to
  * restore the complete context, including values that are not part of tracing.
  *
  * @tparam F
  *   the higher-kinded type of a polymorphic effect
  *
  * @see
  *   How-to guide [[https://typelevel.org/otel4s/how-to-tracing/preserve-context-for-deferred-work.html]]
  */
sealed trait ContextManager[F[_]] { self =>

  /** The backend-specific context type. */
  type Ctx

  /** Reads the current context when the effect runs. */
  def current: F[Ctx]

  /** Returns a natural transformation that runs effects with `context` as the complete current context.
    *
    * The previous context is restored after the transformed effect succeeds, fails, or is canceled.
    */
  def scope(context: Ctx): F ~> F

  /** Captures the current context and returns a reusable natural transformation that restores it.
    *
    * The context is captured when this effect is evaluated, not when the returned transformation is applied.
    */
  def captureScope: F[F ~> F]

  /** Lifts this manager from `F` to `G` while preserving the backend-specific [[Ctx]] type. */
  def liftTo[G[_]](implicit lift: LiftKind[F, G]): ContextManager.Aux[G, Ctx] =
    new ContextManager.Lifted[F, G, Ctx](self)
}

object ContextManager {

  /** A [[ContextManager]] with its context type made explicit. */
  type Aux[F[_], C] = ContextManager[F] { type Ctx = C }

  /** Summons an implicit [[ContextManager]]. */
  def apply[F[_]](implicit manager: ContextManager[F]): ContextManager[F] =
    manager

  /** Creates a [[ContextManager]] backed by the given [[Local]]. */
  private[otel4s] def fromLocal[F[_], C](implicit local: Local[F, C]): Aux[F, C] =
    new FromLocal(local)

  private final class FromLocal[F[_], C](local: Local[F, C]) extends ContextManager[F] {
    type Ctx = C

    def current: F[C] =
      local.ask

    def scope(context: C): F ~> F =
      new (F ~> F) {
        def apply[A](fa: F[A]): F[A] =
          local.scope(fa)(context)
      }

    def captureScope: F[F ~> F] =
      local.applicative.map(current)(scope)

    override def toString: String =
      "ContextManager.fromLocal"
  }

  private final class Lifted[F[_], G[_], C](
      manager: Aux[F, C]
  )(implicit lift: LiftKind[F, G])
      extends ContextManager[G] {
    type Ctx = C

    def current: G[C] =
      lift(manager.current)

    def scope(context: C): G ~> G =
      lift.liftScope(manager.scope(context))

    def captureScope: G[G ~> G] =
      lift.applicativeG.map(current)(scope)

    override def toString: String =
      s"ContextManager.Lifted($manager)"
  }
}
