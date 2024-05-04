/*
 * Copyright 2024 Typelevel
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

package org.typelevel.otel4s.sdk.metrics.internal

import cats.Hash
import cats.Show
import cats.syntax.foldable._
import org.typelevel.otel4s.metrics.BucketBoundaries

/** Advisory options influencing aggregation configuration parameters.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/metrics/api/#instrument-advisory-parameters]]
  */
private[metrics] sealed trait Advice {

  /** The recommended set of bucket boundaries to use if the selected
    * aggregation is `ExplicitBucketHistogram`.
    */
  def explicitBucketBoundaries: Option[BucketBoundaries]

  override final def hashCode(): Int =
    Hash[Advice].hash(this)

  override final def equals(obj: Any): Boolean =
    obj match {
      case other: Advice => Hash[Advice].eqv(this, other)
      case _             => false
    }

  override final def toString: String =
    Show[Advice].show(this)
}

private[metrics] object Advice {

  /** Creates an [[Advice]] with the given values.
    *
    * @param bucketBoundaries
    *   the recommended set of bucket boundaries to use if the selected
    *   aggregation is `explicit bucket histogram`
    */
  def apply(bucketBoundaries: Option[BucketBoundaries]): Advice =
    Impl(bucketBoundaries)

  implicit val adviceHash: Hash[Advice] =
    Hash.by(_.explicitBucketBoundaries)

  implicit val adviceShow: Show[Advice] =
    Show.show { advice =>
      val boundaries = advice.explicitBucketBoundaries.foldMap { b =>
        s"explicitBucketBoundaries=$b"
      }
      s"Advice{$boundaries}"
    }

  private final case class Impl(
      explicitBucketBoundaries: Option[BucketBoundaries]
  ) extends Advice
}
