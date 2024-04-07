/*
 * Copyright 2024 Typelevel
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

package org.typelevel.otel4s.sdk.metrics.view

import munit.ScalaCheckSuite
import org.scalacheck.Prop
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.metrics.scalacheck.Gens

class AttributesProcessorSuite extends ScalaCheckSuite {

  private val context = Context.root

  test("noop - return attributes as is") {
    val processor = AttributesProcessor.noop
    Prop.forAll(Gens.attributes) { attributes =>
      assertEquals(processor.process(attributes, context), attributes)
    }
  }

  test("retain - empty set - return empty attributes") {
    val processor = AttributesProcessor.retain(Set.empty)
    Prop.forAll(Gens.attributes) { attributes =>
      assertEquals(processor.process(attributes, context), Attributes.empty)
    }
  }

  test("retain - non empty set - return matching attributes") {
    Prop.forAll(Gens.attributes) { attributes =>
      val keys = attributes.headOption.map(_.key.name).toSet
      val processor = AttributesProcessor.retain(keys)
      assertEquals(processor.process(attributes, context), attributes.take(1))
    }
  }

  test("attributePredicate - always false - return empty attributes") {
    val processor = AttributesProcessor.attributePredicate(_ => false)
    Prop.forAll(Gens.attributes) { attributes =>
      assertEquals(processor.process(attributes, context), Attributes.empty)
    }
  }

  test("attributePredicate - return matching attributes") {
    Prop.forAll(Gens.attributes) { attributes =>
      val first = attributes.headOption
      val processor = AttributesProcessor.attributePredicate(first.contains)
      assertEquals(processor.process(attributes, context), attributes.take(1))
    }
  }

}
