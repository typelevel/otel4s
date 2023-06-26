package org.typelevel.otel4s.testkit
package traces

import cats.Hash
import cats.Show
import cats.syntax.show._

final class Trace(
  val name: String,
  val scope: InstrumentationScope,
  val resource: InstrumentationResource,
) {

  override def hashCode(): Int =
    Hash[Trace].hash(this)

  override def toString: String =
    Show[Trace].show(this)

  override def equals(obj: Any): Boolean =
    obj match {
      case other: Trace =>
        Hash[Trace].eqv(this, other)
      case _ =>
        false
    }

}

object Trace {

  implicit val traceHash: Hash[Trace] =
    Hash.by(t => (t.name, t.scope, t.resource))

  implicit val traceShow: Show[Trace] =
    Show.show(t =>
        show"Trace(${t.name}, ${t.scope}, ${t.resource})"
    )

}
