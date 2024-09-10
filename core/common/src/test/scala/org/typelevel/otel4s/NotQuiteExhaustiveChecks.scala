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
import cats.syntax.traverse._
import org.scalacheck.util.Buildable

/** [[cats.laws.discipline.ExhaustiveCheck]] instances that aren't actually exhaustive but are needed for laws testing
  * of `TextMap` types.
  */
object NotQuiteExhaustiveChecks {
  implicit val notQuiteExhaustiveCheckString: ExhaustiveCheck[String] =
    ExhaustiveCheck.instance(List("foo", "bar"))

  implicit def notQuiteExhaustiveCheckCollection[C](implicit
      b: Buildable[(String, String), C]
  ): ExhaustiveCheck[C] =
    ExhaustiveCheck.instance {
      ExhaustiveCheck
        .forSet[String]
        .allValues
        .flatMap {
          _.toList.traverse[List, (String, String)] { key =>
            ExhaustiveCheck[String].allValues.map(value => key -> value)
          }
        }
        .map(b.builder.addAll(_).result())
    }
}
