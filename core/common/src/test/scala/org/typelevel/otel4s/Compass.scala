package org.typelevel.otel4s

import cats.laws.discipline.ExhaustiveCheck

// An arbitrary type whose cardinality nicely supports MiniMap
sealed trait Compass
case object North extends Compass
case object South extends Compass
case object East extends Compass
case object West extends Compass

object Compass {
  implicit val exhaustiveCheckForCompass: ExhaustiveCheck[Compass] =
    ExhaustiveCheck.instance(List(North, South, East, West))
}
