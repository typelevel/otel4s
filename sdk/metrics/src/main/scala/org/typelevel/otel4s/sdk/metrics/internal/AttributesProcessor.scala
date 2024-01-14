package org.typelevel.otel4s.sdk.metrics.internal

import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.sdk.context.Context

trait AttributesProcessor {
  def process(incoming: Attributes, context: Context): Attributes
}

object AttributesProcessor {
  def noop: AttributesProcessor =
    (attributes, _) => attributes

  def filterByKeyName(keepWhen: String => Boolean): AttributesProcessor =
    (attributes, _) =>
      attributes.filter(attribute => keepWhen(attribute.key.name))
}
