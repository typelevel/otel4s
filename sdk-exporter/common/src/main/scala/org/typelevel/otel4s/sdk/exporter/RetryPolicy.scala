/*
 * Copyright 2023 Typelevel
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

package org.typelevel.otel4s.sdk.exporter

import cats.Show
import cats.kernel.Hash
import cats.syntax.show._

import scala.concurrent.duration._

/** The exponential retry policy. Used by the exporters.
  */
sealed trait RetryPolicy {

  /** The max number of attempts, including the original request.
    */
  def maxAttempts: Int

  /** The initial backoff duration.
    */
  def initialBackoff: FiniteDuration

  /** The max backoff duration.
    */
  def maxBackoff: FiniteDuration

  /** The backoff multiplier.
    */
  def backoffMultiplier: Double

  override final lazy val hashCode: Int =
    Hash[RetryPolicy].hash(this)

  override final def equals(obj: Any): Boolean =
    obj match {
      case other: RetryPolicy => Hash[RetryPolicy].eqv(this, other)
      case _                  => false
    }

  override final def toString: String =
    Show[RetryPolicy].show(this)
}

object RetryPolicy {

  private object Defaults {
    val MaxAttempts: Int = 5
    val InitialBackoff: FiniteDuration = 1.second
    val MaxBackoff: FiniteDuration = 5.seconds
    val BackoffMultiplier: Double = 1.5
  }

  private val Default = Impl(
    Defaults.MaxAttempts,
    Defaults.InitialBackoff,
    Defaults.MaxBackoff,
    Defaults.BackoffMultiplier
  )

  /** A builder of [[RetryPolicy]].
    */
  sealed trait Builder {

    /** Sets the number of maximum attempts.
      *
      * The default value is `5`.
      *
      * @note
      *   must be greater than `1` and less than `6`.
      *
      * @param max
      *   the number of maximum attempts to use
      */
    def withMaxAttempts(max: Int): Builder

    /** Sets the initial backoff duration.
      *
      * The default value is `1 second`.
      *
      * @note
      *   must be greater than `0`.
      *
      * @param duration
      *   the initial backoff duration to use
      */
    def withInitialBackoff(duration: FiniteDuration): Builder

    /** Sets the max backoff duration.
      *
      * The default value is `5 seconds`.
      *
      * @note
      *   must be greater than `0`.
      *
      * @param duration
      *   the max backoff duration to use
      */
    def withMaxBackoff(duration: FiniteDuration): Builder

    /** Sets the backoff multiplier.
      *
      * The default value is `1.5`.
      *
      * @note
      *   must be greater than `0.0`.
      *
      * @param multiplier
      *   the backoff multiplier to use
      */
    def withBackoffMultiplier(multiplier: Double): Builder

    /** Creates a [[RetryPolicy]] using the configuration of this builder.
      */
    def build: RetryPolicy
  }

  /** A [[RetryPolicy]] with the default configuration.
    */
  def default: RetryPolicy = Default

  /** A [[Builder]] of [[RetryPolicy]] with the default configuration.
    */
  def builder: Builder = Default

  implicit val retryPolicyShow: Show[RetryPolicy] =
    Show.show { policy =>
      "RetryPolicy{" +
        show"maxAttempts=${policy.maxAttempts}, " +
        show"initialBackoff=${policy.initialBackoff}, " +
        show"maxBackoff=${policy.maxBackoff}, " +
        show"backoffMultiplier=${policy.backoffMultiplier}}"
    }

  implicit val retryPolicyHash: Hash[RetryPolicy] =
    Hash.by { p =>
      (p.maxAttempts, p.initialBackoff, p.maxBackoff, p.backoffMultiplier)
    }

  private final case class Impl(
      maxAttempts: Int,
      initialBackoff: FiniteDuration,
      maxBackoff: FiniteDuration,
      backoffMultiplier: Double
  ) extends RetryPolicy
      with Builder {

    def withMaxAttempts(max: Int): Builder = {
      require(
        max > 1 && max < 6,
        "maxAttempts must be greater than 1 and less than 6"
      )

      copy(maxAttempts = max)
    }

    def withInitialBackoff(duration: FiniteDuration): Builder = {
      require(duration > Duration.Zero, "initialBackoff must be greater than 0")
      copy(initialBackoff = duration)
    }

    def withMaxBackoff(duration: FiniteDuration): Builder = {
      require(duration > Duration.Zero, "maxBackoff must be greater than 0")
      copy(maxBackoff = duration)
    }

    def withBackoffMultiplier(multiplier: Double): Builder = {
      require(multiplier > 0, "backoffMultiplier must be greater than 0")
      copy(backoffMultiplier = multiplier)
    }

    def build: RetryPolicy = this
  }

}
