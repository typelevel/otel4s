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

package org.typelevel.otel4s

import cats.Show
import munit.ScalaCheckSuite
import org.scalacheck.Gen
import org.scalacheck.Prop.forAll
import org.typelevel.otel4s.scalacheck.Gens

class AttributesProps extends ScalaCheckSuite {

  private val listOfAttributes = Gen.listOf(Gens.attribute)

  property("Attributes#size is equal to the number of unique keys") {
    forAll(listOfAttributes) { attributes =>
      val keysSet = attributes.map(_.key).toSet
      val attrs = Attributes.fromSpecific(attributes)

      keysSet.size == attrs.size
    }
  }

  property("Attributes#isEmpty is true when there are no attributes") {
    forAll(listOfAttributes) { attributes =>
      val keysSet = attributes.map(_.key).toSet
      val attrs = Attributes.fromSpecific(attributes)

      keysSet.isEmpty == attrs.isEmpty
    }
  }

  property("Attributes#contains is true when the key is present") {
    forAll(listOfAttributes) { attributes =>
      val keysSet = attributes.map(_.key).toSet
      val attrs = Attributes.fromSpecific(attributes)

      keysSet.forall(attrs.contains)
    }
  }

  property("Attributes#foreach iterates over all attributes") {
    forAll(listOfAttributes) { attributes =>
      val attrs = Attributes.fromSpecific(attributes)

      var count = 0
      attrs.foreach(_ => count += 1)

      count == attrs.size
    }
  }

  property("Attributes#toList returns a list of all attributes") {
    forAll(listOfAttributes) { attributes =>
      val attrs = Attributes.fromSpecific(attributes)
      val list = attrs.toList

      list.size == attrs.size && list.forall(a => attrs.contains(a.key))
    }
  }

  property("Attributes#foldLeft folds over all attributes") {
    forAll(listOfAttributes) { attributes =>
      val attrs = Attributes.fromSpecific(attributes)
      val list = attrs.toList

      val folded = attrs.foldLeft[Int](0) { (acc, _) => acc + 1 }

      folded == list.size
    }
  }

  property(
    "Attributes#forall returns true when all attributes match the predicate"
  ) {
    forAll(listOfAttributes) { attributes =>
      val attrs = Attributes.fromSpecific(attributes)

      attrs.forall(_ => true)
    }
  }

  property("Attributes#toMap returns a map of all attributes") {
    forAll(listOfAttributes) { attributes =>
      val attrs = Attributes.fromSpecific(attributes)
      val map = attrs.toMap

      map.size == attrs.size && map.forall { case (k, v) =>
        attrs.contains(k) && attrs.get(k).contains(v)
      }
    }
  }

  property("Attributes#updated adds attributes and replaces existing ones") {
    forAll { (value1: String, value2: String) =>
      val a1 = Attribute("key", value1)
      val a2 = Attribute("key", value2)
      val attrs1 = Attributes.empty + a1
      val attrs2 = Attributes.empty + a2
      val attrs12 = Attributes.empty + a1 + a2

      val attrs1Contains = attrs1.get[String]("key").exists(_.value == value1)
      val attrs2Contains = attrs2.get[String]("key").exists(_.value == value2)
      val attrs12Checks =
        attrs12.get[String]("key").exists(_.value == value2) &&
          attrs12.sizeIs == 1 &&
          (value1 == value2 ||
            attrs12
              .get[String]("key")
              .forall(_.value != value1))

      attrs1Contains && attrs2Contains && attrs12Checks
    }
  }

  property("Attributes#++ combines two sets of attributes") {
    forAll(listOfAttributes, listOfAttributes) { (attributes1, attributes2) =>
      val keySet1 = attributes1.map(_.key).toSet
      val keySet2 = attributes2.map(_.key).toSet
      val diff = keySet1.intersect(keySet2)

      val attrs1 = Attributes(attributes1: _*)
      val attrs2 = Attributes(attributes2: _*)

      val combined = attrs1 ++ attrs2
      val sizeIsEqual = combined.size == keySet1.size + keySet2.size

      val secondCollectionOverrodeValues = diff.forall { key =>
        combined.get(key).contains(attrs2.get(key).get)
      }

      sizeIsEqual && secondCollectionOverrodeValues
    }
  }

  property("Show[Attributes]") {
    forAll(Gens.attributes) { attributes =>
      val expected = attributes.toList
        .map(Show[Attribute[_]].show)
        .mkString("Attributes(", ", ", ")")

      assertEquals(Show[Attributes].show(attributes), expected)
    }
  }

}
