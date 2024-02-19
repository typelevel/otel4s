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

  private def lastDistinct(
      keys: Iterable[AttributeKey[_]]
  ): Set[AttributeKey[_]] =
    keys
      .groupMapReduce(_.name)(identity)((_, second) => second)
      .values
      .toSet

  property("Attributes#size is equal to the number of unique key names") {
    forAll(listOfAttributes) { attributes =>
      val keyNames = attributes.map(_.key.name).toSet
      val attrs = attributes.to(Attributes)

      keyNames.size == attrs.size
    }
  }

  property("Attributes#isEmpty is true when there are no attributes") {
    forAll(listOfAttributes) { attributes =>
      val keysSet = attributes.map(_.key.name).toSet
      val attrs = attributes.to(Attributes)

      keysSet.isEmpty == attrs.isEmpty
    }
  }

  property("Attributes#contains is true when the key is present") {
    forAll(listOfAttributes) { attributes =>
      val keysSet = lastDistinct(attributes.view.map(_.key))
      val attrs = attributes.to(Attributes)

      keysSet.forall(attrs.contains)
    }
  }

  property("Attributes#foreach iterates over all attributes") {
    forAll(listOfAttributes) { attributes =>
      val attrs = attributes.to(Attributes)

      var count = 0
      attrs.foreach(_ => count += 1)

      count == attrs.size
    }
  }

  property("Attributes#toList returns a list of all attributes") {
    forAll(listOfAttributes) { attributes =>
      val attrs = attributes.to(Attributes)
      val list = attrs.toList

      list.size == attrs.size && list.forall(a => attrs.contains(a.key))
    }
  }

  property("Attributes#foldLeft folds over all attributes") {
    forAll(listOfAttributes) { attributes =>
      val attrs = attributes.to(Attributes)
      val list = attrs.toList

      val folded = attrs.foldLeft[Int](0) { (acc, _) => acc + 1 }

      folded == list.size
    }
  }

  property(
    "Attributes#forall returns true when all attributes match the predicate"
  ) {
    forAll(listOfAttributes) { attributes =>
      val attrs = attributes.to(Attributes)

      attrs.forall(_ => true)
    }
  }

  property("Attributes#asMap returns a map of all attributes") {
    forAll(listOfAttributes) { attributes =>
      val attrs = attributes.to(Attributes)
      val map = attrs.asMap

      map.size == attrs.size && map.forall { case (_, a) =>
        attrs.contains(a.key) && attrs.get(a.key).contains(a)
      }
    }
  }

  property("Attributes#asUntypedMap returns a map of all attributes") {
    forAll(listOfAttributes) { attributes =>
      val attrs = attributes.to(Attributes)
      val map = attrs.asUntypedMap

      map.size == attrs.size &&
      attrs.forall { a =>
        map.contains(a.key.name) && map.get(a.key.name).contains(a)
      } && map.keys.forall(attrs.containsUntyped)
    }
  }

  property("Attributes#keys returns all of the attributes' keys") {
    forAll(Gens.attributes) { attributes =>
      attributes.keys == attributes.map(_.key).toSet
    }
  }

  property("Attributes#untypedKeys returns all of the attributes' key names") {
    forAll(Gens.attributes) { attributes =>
      attributes.untypedKeys == attributes.map(_.key.name).toSet
    }
  }

  property(
    "Attributes#updated (+) adds attributes and replaces existing ones"
  ) {
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

  property("Attributes#removed (-) removes attributes") {
    forAll { (value: String) =>
      val a = Attribute("key", value)
      val attrs = Attributes(a)

      (attrs - a.key).isEmpty &&
      attrs
        .removed[Long]("key")
        .get[String]("key")
        .contains(a)
    }
  }

  property("Attributes#removedUntyped removes attributes") {
    forAll { (value: String) =>
      val a = Attribute("key", value)
      val attrs = Attributes(a)

      attrs.removedUntyped(a.key.name).isEmpty &&
      attrs.removedUntyped("other").sizeIs == 1
    }
  }

  property("Attributes#concat (++) combines two sets of attributes") {
    forAll(Gens.attributes, Gens.attributes) { (attributes1, attributes2) =>
      val unique = attributes1.untypedKeys ++ attributes2.untypedKeys
      val diff = attributes1.untypedKeys.intersect(attributes2.untypedKeys)

      val combined = attributes1 ++ attributes2
      val sizeIsEqual = combined.size == unique.size

      val secondCollectionOverrodeValues = diff.forall { name =>
        combined.asUntypedMap
          .get(name)
          .exists(attributes2.asUntypedMap.get(name).contains)
      }

      sizeIsEqual && secondCollectionOverrodeValues
    }
  }

  property("Attributes#removedAll (--) removes a set of attributes") {
    forAll(Gens.attributes) { attributes =>
      (attributes -- attributes.keys).isEmpty
    }
  }

  property("Attributes#removedAllUntyped removes a set of attributes") {
    forAll(Gens.attributes) { attributes =>
      attributes.removedAllUntyped(attributes.untypedKeys).isEmpty
    }
  }

  property(
    "Multiple keys with the same name but different types not allowed"
  ) {
    forAll { (longValue: Long, stringValue: String) =>
      val longKey = AttributeKey.long("key")
      val stringKey = AttributeKey.string("key")
      val attrs = Attributes(longKey(longValue), stringKey(stringValue))

      (attrs.sizeIs == 1) &&
      !attrs.contains(longKey) &&
      attrs.get(stringKey).exists(_.value == stringValue)
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
