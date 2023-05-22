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

import cats.laws.discipline.ExhaustiveCheck
import cats.syntax.all._

import scala.collection.AbstractMap
import scala.collection.Map
import scala.collection.generic.IsMap
import scala.collection.immutable

/** A Map with a cardinality of 25, which is a nice fit for ExhaustiveCheck. For
  * each Boolean key (cardinality 2), the value may be any (Boolean, Boolean)
  * value (cardinality 4) or absent. Each of these is independent. Therefore,
  * the cardinality of the MiniMap is (4+1)^2 == 25.
  */
case class MiniMap(underlying: immutable.Map[Boolean, (Boolean, Boolean)])
    extends AbstractMap[String, String] {
  def iterator =
    underlying.iterator.map { case (k, v) => k.toString -> v.toString }
  def get(key: String) =
    underlying.get(stringToBoolean(key)).map(_.toString)
  def -(key1: String, key2: String, keys: String*): MiniMap =
    keys.foldLeft(this - key1 - key2) { case (m, k) => m - k }
  def -(key: String): MiniMap =
    MiniMap(underlying.removed(stringToBoolean(key)))
  private def stringToBoolean(s: String) =
    s.toBoolean
}

object MiniMap {
  implicit val exhaustiveCheckForMiniMap: ExhaustiveCheck[MiniMap] =
    ExhaustiveCheck.instance(
      ExhaustiveCheck
        .forSet[Boolean]
        .allValues
        .map(_.toList)
        .flatMap(
          _.traverse[List, (Boolean, (Boolean, Boolean))](key =>
            ExhaustiveCheck[(Boolean, Boolean)].allValues
              .map(value => key -> value)
          )
        )
        .map(_.toMap)
        .map(MiniMap(_))
    )

  implicit val isMapForMiniMap
      : IsMap[MiniMap] { type K = String; type V = String } =
    new IsMap[MiniMap] {
      type K = String
      type V = String
      type C = Map[String, String]
      def apply(mm: MiniMap) = mm
    }
}
