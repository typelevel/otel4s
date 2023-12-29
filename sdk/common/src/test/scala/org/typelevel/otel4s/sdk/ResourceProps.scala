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

package org.typelevel.otel4s.sdk

import cats.Show
import cats.syntax.show._
import munit.ScalaCheckSuite
import org.scalacheck.Prop.forAll
import org.typelevel.otel4s.sdk.arbitrary.resource

class ResourceProps extends ScalaCheckSuite {

  property("Attributes#merge merges attributes") {
    forAll(resource.arbitrary, resource.arbitrary) { (resource1, resource2) =>
      val mergedEither = resource1.merge(resource2)
      mergedEither match {
        case Right(merged) =>
          val mergedAttrs = merged.attributes
          val keys =
            resource1.attributes.toMap.keySet ++ resource2.attributes.toMap.keySet

          mergedAttrs.size == keys.size && mergedAttrs.forall { a =>
            resource2.attributes
              .get(a.key)
              .orElse(resource1.attributes.get(a.key))
              .contains(a)
          }
        case Left(_) => true
      }

    }
  }

  property("Show[Resource]") {
    forAll(resource.arbitrary) { resource =>
      val expected =
        show"Resource{attributes=${resource.attributes}, schemaUrl=${resource.schemaUrl}}"

      assertEquals(Show[Resource].show(resource), expected)
    }
  }

}
