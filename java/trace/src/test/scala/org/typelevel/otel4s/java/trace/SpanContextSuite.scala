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

package org.typelevel.otel4s.java.trace

import io.opentelemetry.api.trace.{SpanContext => JSpanContext}
import munit.CatsEffectSuite
import org.typelevel.otel4s.trace.SpanContext

class SpanContextSuite extends CatsEffectSuite {

  test("SpanContext.invalid satisfies the specification") {
    val context = SpanContext.invalid
    val jContext = JSpanContext.getInvalid
    assert(context.traceId.toArray.sameElements(jContext.getTraceIdBytes))
    assertEquals(context.traceIdHex, jContext.getTraceId)
    assert(context.spanId.toArray.sameElements(jContext.getSpanIdBytes))
    assertEquals(context.spanIdHex, jContext.getSpanId)
    assertEquals(context.samplingDecision.isSampled, jContext.isSampled)
    assertEquals(context.isValid, jContext.isValid)
    assertEquals(context.isRemote, jContext.isRemote)
  }

}
