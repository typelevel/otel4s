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

import lgbt.princess.platform.Platform
import munit.ScalaCheckSuite
import org.scalacheck.Arbitrary
import org.scalacheck.Prop._

import scala.collection.generic.IsIterable
import scala.collection.mutable
import scala.reflect.ClassTag

class TextMapSetterProps extends ScalaCheckSuite {
  def mkProp[A: Arbitrary](implicit
      tag: ClassTag[A],
      setter: TextMapSetter[A],
      conv: IsIterable[A] { type A = (String, String) }
  ): Unit =
    property(s"TextMapSetter for ${tag.runtimeClass.getName}") {
      forAll { (coll: A, key: String, value: String) =>
        setter.unsafeSet(coll, key, value)
        assert(conv(coll).exists(_ == (key -> value)))
      }
    }

  mkProp[mutable.Map[String, String]]
  mkProp[mutable.HashMap[String, String]]
  mkProp[mutable.CollisionProofHashMap[String, String]]
  mkProp[mutable.SortedMap[String, String]]
  mkProp[mutable.TreeMap[String, String]]
  mkProp[mutable.SeqMap[String, String]]
  mkProp[mutable.LinkedHashMap[String, String]]
  if (Platform.isJVM) mkProp[collection.concurrent.TrieMap[String, String]]

  mkProp[mutable.Buffer[(String, String)]]
  mkProp[mutable.ListBuffer[(String, String)]]
  mkProp[mutable.IndexedBuffer[(String, String)]]
  mkProp[mutable.ArrayBuffer[(String, String)]]
  mkProp[mutable.Queue[(String, String)]]
  mkProp[mutable.Stack[(String, String)]]
  mkProp[mutable.ArrayDeque[(String, String)]]
  mkProp[mutable.UnrolledBuffer[(String, String)]]
}
