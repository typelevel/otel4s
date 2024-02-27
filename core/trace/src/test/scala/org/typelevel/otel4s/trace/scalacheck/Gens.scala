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

package org.typelevel.otel4s.trace.scalacheck

import org.scalacheck.Gen
import org.typelevel.otel4s.baggage.Baggage
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.SpanKind
import org.typelevel.otel4s.trace.StatusCode
import org.typelevel.otel4s.trace.TraceFlags
import org.typelevel.otel4s.trace.TraceState
import scodec.bits.ByteVector

trait Gens extends org.typelevel.otel4s.scalacheck.Gens {

  val traceId: Gen[ByteVector] =
    for {
      hi <- Gen.long
      lo <- Gen.long.suchThat(_ != 0)
    } yield SpanContext.TraceId.fromLongs(hi, lo)

  val spanId: Gen[ByteVector] =
    for {
      value <- Gen.long.suchThat(_ != 0)
    } yield SpanContext.SpanId.fromLong(value)

  val traceFlags: Gen[TraceFlags] =
    Gen.oneOf(TraceFlags.Sampled, TraceFlags.Default)

  val traceState: Gen[TraceState] =
    for {
      k1 <- nonEmptyString
      v1 <- nonEmptyString
      k2 <- nonEmptyString
      v2 <- nonEmptyString
      k3 <- nonEmptyString
      v3 <- nonEmptyString
    } yield TraceState.empty.updated(k1, v1).updated(k2, v2).updated(k3, v3)

  val baggage: Gen[Baggage] =
    for {
      k1 <- nonEmptyString
      v1 <- nonEmptyString
      k2 <- nonEmptyString
      v2 <- nonEmptyString
      k3 <- nonEmptyString
      v3 <- nonEmptyString
    } yield Baggage.empty.updated(k1, v1).updated(k2, v2).updated(k3, v3)

  val spanContext: Gen[SpanContext] =
    for {
      traceId <- Gens.traceId
      spanId <- Gens.spanId
      traceFlags <- Gens.traceFlags
      remote <- Gen.oneOf(true, false)
    } yield SpanContext(traceId, spanId, traceFlags, TraceState.empty, remote)

  val spanKind: Gen[SpanKind] =
    Gen.oneOf(
      SpanKind.Internal,
      SpanKind.Server,
      SpanKind.Client,
      SpanKind.Producer,
      SpanKind.Consumer
    )

  val statusCode: Gen[StatusCode] =
    Gen.oneOf(StatusCode.Unset, StatusCode.Ok, StatusCode.Error)

}

object Gens extends Gens
