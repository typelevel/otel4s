package org.typelevel.otel4s.trace

import munit._

class SamplingDecisionSuite extends FunSuite {

  test("Drop should have isSampled = false") {
    assertEquals(SamplingDecision.Drop.isSampled, false)
  }

  test("RecordOnly should have isSampled = false") {
    assertEquals(SamplingDecision.RecordOnly.isSampled, false)
  }

  test("RecordAndSample should have isSampled = true") {
    assertEquals(SamplingDecision.RecordAndSample.isSampled, true)
  }

}
