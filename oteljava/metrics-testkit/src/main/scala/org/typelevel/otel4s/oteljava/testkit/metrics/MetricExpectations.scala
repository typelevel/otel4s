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

package org.typelevel.otel4s.oteljava.testkit.metrics

import io.opentelemetry.sdk.metrics.data.MetricData

sealed trait MetricMismatch {
  def expectation: MetricExpectation
}

object MetricMismatch {
  final case class NotFound(
      expectation: MetricExpectation,
      metrics: List[MetricData]
  ) extends MetricMismatch
}

object MetricExpectations {

  def exists(
      metrics: List[MetricData],
      expectation: MetricExpectation
  ): Boolean =
    find(metrics, expectation).nonEmpty

  def find(
      metrics: List[MetricData],
      expectation: MetricExpectation
  ): Option[MetricData] =
    metrics.find(expectation.matches)

  def check(
      metrics: List[MetricData],
      expectation: MetricExpectation
  ): Option[MetricMismatch] =
    Option.when(!exists(metrics, expectation)) {
      MetricMismatch.NotFound(expectation, metrics)
    }

  def missing(
      metrics: List[MetricData],
      expectations: List[MetricExpectation]
  ): List[MetricMismatch] =
    expectations.flatMap(check(metrics, _))

  def allMatch(
      metrics: List[MetricData],
      expectations: List[MetricExpectation]
  ): Boolean =
    missing(metrics, expectations).isEmpty
}
