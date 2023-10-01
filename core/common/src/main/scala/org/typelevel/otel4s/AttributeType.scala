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

import cats.Hash
import cats.Show

/** Represents all possible value types for an [[AttributeKey]] and hence the
  * types of values that are allowed for [[Attribute]].
  */
sealed trait AttributeType[A] extends Product with Serializable

object AttributeType {
  case object Boolean extends AttributeType[Boolean]
  case object Double extends AttributeType[Double]
  case object String extends AttributeType[String]
  case object Long extends AttributeType[Long]

  case object BooleanList extends AttributeType[List[Boolean]]
  case object DoubleList extends AttributeType[List[Double]]
  case object StringList extends AttributeType[List[String]]
  case object LongList extends AttributeType[List[Long]]

  implicit def attributeTypeHash[A]: Hash[AttributeType[A]] =
    Hash.fromUniversalHashCode

  implicit def attributeTypeShow[A]: Show[AttributeType[A]] =
    Show.fromToString

  implicit val attributeTypeAnyHash: Hash[AttributeType[_]] =
    Hash.fromUniversalHashCode

}
