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
import cats.kernel.laws.discipline.HashTests
import cats.syntax.show._
import munit._
import org.scalacheck.Arbitrary
import org.scalacheck.Cogen
import org.scalacheck.Gen
import org.scalacheck.Prop

import scala.concurrent.duration._

class RetryPolicySuite extends DisciplineSuite {

  private val retryPolicyGen: Gen[RetryPolicy] =
    for {
      maxAttempts <- Gen.chooseNum(2, 5)
      initialBackoff <- Gen.chooseNum(1, 100)
      maxBackoff <- Gen.chooseNum(1, 100)
      multiplier <- Gen.chooseNum(0.1, 100.1)
    } yield RetryPolicy.builder
      .withMaxAttempts(maxAttempts)
      .withInitialBackoff(initialBackoff.seconds)
      .withMaxBackoff(maxBackoff.seconds)
      .withBackoffMultiplier(multiplier)
      .build

  private implicit val retryPolicyArbitrary: Arbitrary[RetryPolicy] =
    Arbitrary(retryPolicyGen)

  private implicit val retryPolicyCogen: Cogen[RetryPolicy] =
    Cogen[(Int, FiniteDuration, FiniteDuration, Double)].contramap { p =>
      (p.maxAttempts, p.initialBackoff, p.maxBackoff, p.backoffMultiplier)
    }

  checkAll("RetryPolicy.HashLaws", HashTests[RetryPolicy].hash)

  property("Show[RetryPolicy]") {
    Prop.forAll(retryPolicyGen) { policy =>
      val expected = "RetryPolicy{" +
        show"maxAttempts=${policy.maxAttempts}, " +
        show"initialBackoff=${policy.initialBackoff}, " +
        show"maxBackoff=${policy.maxBackoff}, " +
        show"backoffMultiplier=${policy.backoffMultiplier}}"

      assertEquals(Show[RetryPolicy].show(policy), expected)
    }
  }

}
