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

import cats._
import cats.laws.discipline._
import cats.syntax.all._

object TestInstances {
  implicit def eqForTextMapGetter[A: ExhaustiveCheck]: Eq[TextMapGetter[A]] =
    (f, g) =>
      ExhaustiveCheck[(A, Boolean)].allValues.forall { case (a, b) =>
        f.get(a, b.toString) === g.get(a, b.toString) &&
        f.keys(a).toSet === g.keys(a).toSet
      }
}
