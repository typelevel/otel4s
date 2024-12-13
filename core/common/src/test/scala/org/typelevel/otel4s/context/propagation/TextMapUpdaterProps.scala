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

package org.typelevel.otel4s.context.propagation

import munit.ScalaCheckSuite
import org.scalacheck.Arbitrary
import org.scalacheck.Prop.forAll

import scala.collection.generic.IsIterable
import scala.collection.immutable
import scala.reflect.ClassTag

class TextMapUpdaterProps extends ScalaCheckSuite {
  def mkProp[A: Arbitrary](implicit
      tag: ClassTag[A],
      updater: TextMapUpdater[A],
      conv: IsIterable[A] { type A = (String, String) }
  ): Unit =
    property(s"TextMapSetter for ${tag.runtimeClass.getName}") {
      forAll { (carrier: A, key: String, value: String) =>
        val coll = updater.updated(carrier, key, value)
        assert(conv(coll).exists(_ == (key -> value)))
      }
    }

  mkProp[immutable.Map[String, String]]
  mkProp[immutable.HashMap[String, String]]
  mkProp[immutable.SortedMap[String, String]]
  mkProp[immutable.TreeMap[String, String]]
  mkProp[immutable.ListMap[String, String]]
  mkProp[immutable.VectorMap[String, String]]
  mkProp[immutable.TreeSeqMap[String, String]]

  mkProp[immutable.Seq[(String, String)]]
  mkProp[immutable.LinearSeq[(String, String)]]
  mkProp[immutable.List[(String, String)]]
  mkProp[immutable.LazyList[(String, String)]]
  mkProp[immutable.Queue[(String, String)]]
  mkProp[immutable.IndexedSeq[(String, String)]]
  mkProp[immutable.Vector[(String, String)]]
  mkProp[immutable.ArraySeq[(String, String)]]
}
