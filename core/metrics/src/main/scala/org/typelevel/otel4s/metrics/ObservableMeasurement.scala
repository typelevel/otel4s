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

package org.typelevel.otel4s.metrics

import cats.Applicative
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes

trait ObservableMeasurement[F[_], A] {

  /** Records a value with a set of attributes.
    *
    * @param value
    *   the value to record
    *
    * @param attributes
    *   the set of attributes to associate with the value
    */
  final def record(value: A, attributes: Attribute[_]*): F[Unit] =
    record(value, attributes.to(Attributes))

  /** Records a value with a set of attributes.
    *
    * @param value
    *   the value to record
    *
    * @param attributes
    *   the set of attributes to associate with the value
    */
  def record(value: A, attributes: Attributes): F[Unit]
}

object ObservableMeasurement {

  def noop[F[_]: Applicative, A]: ObservableMeasurement[F, A] =
    new ObservableMeasurement[F, A] {
      def record(value: A, attributes: Attributes): F[Unit] =
        Applicative[F].unit
    }

}
