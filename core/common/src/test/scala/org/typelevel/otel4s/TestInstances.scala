package org.typelevel.otel4s

import cats._
import cats.laws.discipline._
import cats.syntax.all._

object TestInstances {
  implicit def eqForTextMapGetter[A: ExhaustiveCheck]: Eq[TextMapGetter[A]] =
    (f, g) =>
      ExhaustiveCheck[(A, Boolean)].allValues.forall { case (a, b) =>
        f.get(a, b.toString) === g.get(a, b.toString)
      }
}
