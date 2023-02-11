package org.typelevel.otel4s

import cats.Applicative

trait ContextPropagators[F[_]] {
  type Context

  def textMapPropagator: TextMapPropagator[F]
}

object ContextPropagators {
  type Aux[F[_], C] = ContextPropagators[F] { type Context = C }

  def noop[F[_]: Applicative, C]: ContextPropagators.Aux[F, C] =
    new ContextPropagators[F] {
      type Context = C

      def textMapPropagator: TextMapPropagator.Aux[F, C] =
        TextMapPropagator.noop
    }
}
