/*
 * Copyright 2023 Typelevel
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

package org.typelevel
package otel4s
package sdk

import org.scalacheck.Arbitrary
import org.scalacheck.Cogen
import org.scalacheck.Gen

object arbitrary extends ArbitraryInstances
trait ArbitraryInstances extends otel4s.ArbitraryInstances {

  implicit val resource: Arbitrary[Resource] = Arbitrary(for {
    attrs <- attributes.arbitrary
    schemaUrl <- Gen.option(nonEmptyString)
  } yield Resource(attrs, schemaUrl))

  implicit val resourceCogen: Cogen[Resource] =
    Cogen[(Attributes, Option[String])].contramap { r =>
      (r.attributes, r.schemaUrl)
    }

}
