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

package org.typelevel.otel4s.trace

import munit._
import org.typelevel.otel4s.trace.SpanContext.SpanId
import org.typelevel.otel4s.trace.SpanContext.TraceId

class SpanContextSuite extends FunSuite {

  test("invalid span context") {
    assertEquals(SpanContext.invalid.traceId, TraceId.Invalid)
    assertEquals(SpanContext.invalid.traceIdHex, TraceId.InvalidHex)
    assertEquals(SpanContext.invalid.spanId, SpanId.Invalid)
    assertEquals(SpanContext.invalid.spanIdHex, SpanId.InvalidHex)
    assertEquals(SpanContext.invalid.traceFlags, TraceFlags.Default)
    assertEquals(SpanContext.invalid.isValid, false)
    assertEquals(SpanContext.invalid.isRemote, false)
  }

}
