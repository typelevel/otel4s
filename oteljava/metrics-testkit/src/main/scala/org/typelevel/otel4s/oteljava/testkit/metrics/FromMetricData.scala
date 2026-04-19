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
import org.typelevel.otel4s.oteljava.testkit.metrics.data.Metric

import scala.annotation.nowarn

/** Transforms OpenTelemetry's MetricData into arbitrary type `A`.
  */
@deprecated(
  "Use `collectMetrics` without a type parameter to work with OpenTelemetry Java `MetricData`, or use the expectation API for assertions.",
  "1.0.0-RC1"
)
sealed trait FromMetricData[A] {
  def from(metricData: MetricData): A
}

@nowarn("cat=deprecation")
object FromMetricData {

  @deprecated(
    "Use `collectMetrics` without a type parameter to work with OpenTelemetry Java `MetricData`, or use the expectation API for assertions.",
    "1.0.0-RC1"
  )
  def apply[A](implicit ev: FromMetricData[A]): FromMetricData[A] = ev

  implicit val toOtelJavaMetricData: FromMetricData[MetricData] =
    new FromMetricData[MetricData] {
      def from(metricData: MetricData): MetricData = metricData
    }

  @deprecated(
    "Use OpenTelemetry Java `MetricData` directly or the expectation API instead of the deprecated testkit projection model.",
    "1.0.0-RC1"
  )
  implicit val toOtel4sMetric: FromMetricData[Metric] =
    new FromMetricData[Metric] {
      def from(metricData: MetricData): Metric = Metric(metricData)
    }

}
