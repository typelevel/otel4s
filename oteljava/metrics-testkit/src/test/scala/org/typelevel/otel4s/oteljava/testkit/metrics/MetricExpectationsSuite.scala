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

import io.opentelemetry.sdk.common.InstrumentationScopeInfo
import io.opentelemetry.sdk.metrics.data.AggregationTemporality
import io.opentelemetry.sdk.metrics.data.MetricData
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongPointData
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSumData
import io.opentelemetry.sdk.resources.Resource
import munit.FunSuite
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.oteljava.AttributeConverters._

import scala.jdk.CollectionConverters._

class MetricExpectationsSuite extends FunSuite {

  test("match by name only") {
    val metrics = List(longSum("service.counter", 1L))

    assert(MetricExpectations.exists(metrics, MetricExpectation.name("service.counter")))
  }

  test("match by kind and value") {
    val metrics = List(longSum("service.counter", 1L))

    assert(
      MetricExpectations.exists(
        metrics,
        MetricExpectation.sum[Long]("service.counter").withValue(1L)
      )
    )
  }

  test("match by exact attributes") {
    val metrics = List(
      longSum(
        "service.counter",
        1L,
        Attributes(Attribute("http.method", "GET"))
      )
    )

    assert(
      MetricExpectations.exists(
        metrics,
        MetricExpectation
          .sum[Long]("service.counter")
          .withAnyPoint(
            PointExpectation
              .value(1L)
              .withAttributes(Attributes(Attribute("http.method", "GET")))
          )
      )
    )
  }

  test("match by attribute subset") {
    val metrics = List(
      longSum(
        "service.counter",
        1L,
        Attributes(
          Attribute("http.method", "GET"),
          Attribute("http.route", "/users")
        )
      )
    )

    assert(
      MetricExpectations.exists(
        metrics,
        MetricExpectation
          .sum[Long]("service.counter")
          .withAnyPoint(
            PointExpectation
              .value(1L)
              .withAttributesSubset(Attributes(Attribute("http.method", "GET")))
          )
      )
    )
  }

  test("missing returns unmatched expectations") {
    val metrics = List(longSum("service.counter", 1L))

    val missing = MetricExpectations.missing(
      metrics,
      List(
        MetricExpectation.sum[Long]("service.counter").withValue(1L),
        MetricExpectation.gauge[Long]("service.gauge")
      )
    )

    assertEquals(missing.size, 1)
    assert(missing.head.expectation == MetricExpectation.gauge[Long]("service.gauge"))
  }

  test("withAllPoints requires every point to match") {
    val metrics = List(
      metricWithPoints(
        "service.counter",
        List(
          point(1L, Attributes(Attribute("region", "eu"))),
          point(2L, Attributes(Attribute("region", "us")))
        )
      )
    )

    assert(
      !MetricExpectations.exists(
        metrics,
        MetricExpectation
          .sum[Long]("service.counter")
          .withAllPoints(
            PointExpectation
              .value(1L)
              .withAttributesSubset(Attributes(Attribute("region", "eu")))
          )
      )
    )
  }

  private def longSum(
      name: String,
      value: Long,
      attributes: Attributes = Attributes.empty
  ): MetricData =
    ImmutableMetricData.createLongSum(
      resource,
      scope,
      name,
      "",
      "",
      ImmutableSumData.create(
        true,
        AggregationTemporality.CUMULATIVE,
        List(point(value, attributes)).asJava
      )
    )

  private def metricWithPoints(
      name: String,
      points: List[io.opentelemetry.sdk.metrics.data.LongPointData]
  ): MetricData =
    ImmutableMetricData.createLongSum(
      resource,
      scope,
      name,
      "",
      "",
      ImmutableSumData.create(
        true,
        AggregationTemporality.CUMULATIVE,
        points.asJava
      )
    )

  private def point(
      value: Long,
      attributes: Attributes
  ): io.opentelemetry.sdk.metrics.data.LongPointData =
    ImmutableLongPointData.create(0L, 1000000000L, attributes.toJava, value)

  private val scope: InstrumentationScopeInfo =
    InstrumentationScopeInfo.create("scope")

  private val resource: Resource =
    Resource.empty()
}
