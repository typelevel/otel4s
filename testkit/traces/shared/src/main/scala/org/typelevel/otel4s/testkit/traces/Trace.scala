package org.typelevel.otel4s.testkit
package traces

final class Traces(
  val name: String
  val scope: InstrumentationScope,
  val resource: InstrumentationResource,
)
