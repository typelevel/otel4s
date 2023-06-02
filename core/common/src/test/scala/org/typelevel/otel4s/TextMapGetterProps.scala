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
import org.scalacheck.Prop.forAll

import scala.collection.generic.IsIterable
import scala.collection.immutable
import scala.collection.mutable
import scala.reflect.ClassTag

class TextMapGetterProps extends ScalaCheckSuite {
  def mkProps[A: Arbitrary](implicit
      tag: ClassTag[A],
      getter: TextMapGetter[A],
      conv: IsIterable[A] { type A = (String, String) }
  ): Unit = {
    val className =
      tag.runtimeClass match {
        case cls if cls.isArray => "Array"
        case cls                => cls.getName
      }
    property(s"TextMapSetter#get  for $className") {
      forAll { (coll: A, key: String) =>
        val res = getter.get(coll, key)
        assertEquals(res.isDefined, conv(coll).exists(_._1 == key))
        assert(res.forall(value => conv(coll).exists(_ == (key -> value))))
      }
    }
    property(s"TextMapSetter#keys for $className") {
      forAll { (coll: A) =>
        val keys = getter.keys(coll)
        val collAsKeySet = conv(coll).view.map(_._1).toSet
        assert(keys.sizeCompare(collAsKeySet) == 0)
        assertEquals(keys.toSet, collAsKeySet)
      }
    }
  }

  mkProps[collection.Map[String, String]]
  mkProps[collection.SortedMap[String, String]]
  mkProps[collection.SeqMap[String, String]]
  mkProps[immutable.Map[String, String]]
  mkProps[immutable.HashMap[String, String]]
  mkProps[immutable.SortedMap[String, String]]
  mkProps[immutable.TreeMap[String, String]]
  mkProps[immutable.ListMap[String, String]]
  mkProps[immutable.VectorMap[String, String]]
  mkProps[immutable.TreeSeqMap[String, String]]
  mkProps[mutable.Map[String, String]]
  mkProps[mutable.HashMap[String, String]]
  mkProps[mutable.CollisionProofHashMap[String, String]]
  mkProps[mutable.SortedMap[String, String]]
  mkProps[mutable.TreeMap[String, String]]
  mkProps[mutable.SeqMap[String, String]]
  mkProps[mutable.LinkedHashMap[String, String]]
  if (Platform.isJVM) mkProps[collection.concurrent.TrieMap[String, String]]

  mkProps[collection.Seq[(String, String)]]
  mkProps[collection.LinearSeq[(String, String)]]
  mkProps[collection.IndexedSeq[(String, String)]]
  mkProps[immutable.Seq[(String, String)]]
  mkProps[immutable.LinearSeq[(String, String)]]
  mkProps[immutable.List[(String, String)]]
  mkProps[immutable.LazyList[(String, String)]]
  mkProps[immutable.Queue[(String, String)]]
  mkProps[immutable.IndexedSeq[(String, String)]]
  mkProps[immutable.Vector[(String, String)]]
  mkProps[immutable.ArraySeq[(String, String)]]
  mkProps[mutable.Seq[(String, String)]]
  mkProps[mutable.IndexedSeq[(String, String)]]
  mkProps[mutable.ArraySeq[(String, String)]]
  mkProps[mutable.ArraySeq[(String, String)]]
  mkProps[mutable.Buffer[(String, String)]]
  mkProps[mutable.ListBuffer[(String, String)]]
  mkProps[mutable.IndexedBuffer[(String, String)]]
  mkProps[mutable.ArrayBuffer[(String, String)]]
  mkProps[mutable.Queue[(String, String)]]
  mkProps[mutable.Stack[(String, String)]]
  mkProps[mutable.ArrayDeque[(String, String)]]
  mkProps[mutable.UnrolledBuffer[(String, String)]]

  mkProps[Array[(String, String)]]
}
