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

import org.scalacheck.Arbitrary
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.SpanKind
import org.typelevel.otel4s.trace.StatusCode

trait Arbitraries extends org.typelevel.otel4s.scalacheck.Arbitraries {

  implicit val spanContextArbitrary: Arbitrary[SpanContext] =
    Arbitrary(Gens.spanContext)

  implicit val spanKindArbitrary: Arbitrary[SpanKind] =
    Arbitrary(Gens.spanKind)

  implicit val statusCodeArbitrary: Arbitrary[StatusCode] =
    Arbitrary(Gens.statusCode)

}

object Arbitraries extends Arbitraries
