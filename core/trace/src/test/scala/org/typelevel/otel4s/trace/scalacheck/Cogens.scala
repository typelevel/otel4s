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

import org.scalacheck.Cogen
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.SpanKind
import org.typelevel.otel4s.trace.Status
import org.typelevel.otel4s.trace.TraceFlags
import org.typelevel.otel4s.trace.TraceState

trait Cogens extends org.typelevel.otel4s.scalacheck.Cogens {

  implicit val traceFlagsCogen: Cogen[TraceFlags] =
    Cogen[Byte].contramap(_.toByte)

  implicit val traceStateCogen: Cogen[TraceState] =
    Cogen[Map[String, String]].contramap(_.asMap)

  implicit val spanContextCogen: Cogen[SpanContext] =
    Cogen[(String, String, TraceFlags, TraceState, Boolean, Boolean)]
      .contramap { c =>
        (
          c.traceIdHex,
          c.spanIdHex,
          c.traceFlags,
          c.traceState,
          c.isRemote,
          c.isValid
        )
      }

  implicit val spanKindCogen: Cogen[SpanKind] =
    Cogen[String].contramap(_.toString)

  implicit val statusCogen: Cogen[Status] =
    Cogen[String].contramap(_.toString)

}

object Cogens extends Cogens
