package org.typelevel.otel4s.java

import munit.CatsEffectSuite
import io.opentelemetry.api.{OpenTelemetry => JOpenTelemetry}

class OtelJavaSuite extends CatsEffectSuite {

  test("toString implementation") {
    assertEquals(JOpenTelemetry.noop().toString(), "noop?")
  }
}
