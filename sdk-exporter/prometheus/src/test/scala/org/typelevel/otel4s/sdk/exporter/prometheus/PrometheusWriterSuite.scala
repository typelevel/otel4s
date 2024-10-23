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

package org.typelevel.otel4s.sdk.exporter.prometheus

import cats.data.NonEmptyVector
import cats.effect.IO
import cats.effect.std.Console
import cats.syntax.option._
import munit.CatsEffectSuite
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.metrics.BucketBoundaries
import org.typelevel.otel4s.sdk.BuildInfo
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.exporter.prometheus.PrometheusWriter.Config
import org.typelevel.otel4s.sdk.metrics.data._
import org.typelevel.otel4s.sdk.metrics.scalacheck.Gens
import org.typelevel.otel4s.sdk.test.InMemoryConsole

import java.nio.charset.StandardCharsets

class PrometheusWriterSuite extends CatsEffectSuite {

  test("write MetricData with monotonic MetricPoints.Sum") {
    val expected =
      s"""# HELP foo_milliseconds_total a simple counter
         |# TYPE foo_milliseconds_total counter
         |foo_milliseconds_total{A="B",C="D",E="true",F="42",otel_scope_name="testmeter",otel_scope_version="v0.1.0"} 24.3
         |foo_milliseconds_total{A="D",C="B",E="true",F="42",otel_scope_name="testmeter",otel_scope_version="v0.1.0"} 5
         |# HELP otel_scope_info Instrumentation Scope metadata
         |# TYPE otel_scope_info gauge
         |otel_scope_info{G="H",otel_scope_name="testmeter",otel_scope_version="v0.1.0"} 1
         |# HELP target_info Target metadata
         |# TYPE target_info gauge
         |target_info{service_name="unknown_service:scala",telemetry_sdk_language="scala",telemetry_sdk_name="otel4s",telemetry_sdk_version="${BuildInfo.version}"} 1
         |""".stripMargin

    val metrics = Vector(
      mkSum(
        "foo",
        "a simple counter".some,
        "ms".some,
        NonEmptyVector
          .of(
            (24.3, Attributes(Attribute("A", "B"), Attribute("C", "D"), Attribute("E", true), Attribute("F", 42L))),
            (5.0, Attributes(Attribute("A", "D"), Attribute("C", "B"), Attribute("E", true), Attribute("F", 42L)))
          ),
        scopeAttributes = Attributes(Attribute("G", "H"))
      )
    )

    writeAndCompare(metrics, expected)
  }

  test("write MetricData with monotonic MetricPoints.Sum when type suffixes are disabled") {
    val expected =
      s"""# HELP foo_milliseconds a simple counter without a total suffix
         |# TYPE foo_milliseconds counter
         |foo_milliseconds{A="B",C="D",E="true",F="42",otel_scope_name="testmeter",otel_scope_version="v0.1.0"} 24.3
         |foo_milliseconds{A="D",C="B",E="true",F="42",otel_scope_name="testmeter",otel_scope_version="v0.1.0"} 5
         |# HELP otel_scope_info Instrumentation Scope metadata
         |# TYPE otel_scope_info gauge
         |otel_scope_info{G="H",otel_scope_name="testmeter",otel_scope_version="v0.1.0"} 1
         |# HELP target_info Target metadata
         |# TYPE target_info gauge
         |target_info{service_name="unknown_service:scala",telemetry_sdk_language="scala",telemetry_sdk_name="otel4s",telemetry_sdk_version="${BuildInfo.version}"} 1
         |""".stripMargin

    val metrics = Vector(
      mkSum(
        "foo",
        "a simple counter without a total suffix".some,
        "ms".some,
        NonEmptyVector
          .of(
            (24.3, Attributes(Attribute("A", "B"), Attribute("C", "D"), Attribute("E", true), Attribute("F", 42L))),
            (5.0, Attributes(Attribute("A", "D"), Attribute("C", "B"), Attribute("E", true), Attribute("F", 42L)))
          ),
        scopeAttributes = Attributes(Attribute("G", "H"))
      )
    )

    writeAndCompare(metrics, expected, Config.default.withoutTypeSuffixes)
  }

  test("write MetricData with non-monotonic MetricPoints.Sum") {
    val expected =
      s"""# HELP bar_ratio a fun little gauge
         |# TYPE bar_ratio gauge
         |bar_ratio{A="B",C="D",otel_scope_name="testmeter",otel_scope_version="v0.1.0"} 0.75
         |# HELP otel_scope_info Instrumentation Scope metadata
         |# TYPE otel_scope_info gauge
         |otel_scope_info{E="F",otel_scope_name="testmeter",otel_scope_version="v0.1.0"} 1
         |# HELP target_info Target metadata
         |# TYPE target_info gauge
         |target_info{service_name="unknown_service:scala",telemetry_sdk_language="scala",telemetry_sdk_name="otel4s",telemetry_sdk_version="${BuildInfo.version}"} 1
         |""".stripMargin

    val metrics = Vector(
      mkSum(
        "bar",
        "a fun little gauge".some,
        "1".some,
        NonEmptyVector
          .of(
            (0.75, Attributes(Attribute("A", "B"), Attribute("C", "D")))
          ),
        monotonic = false
      )
    )

    writeAndCompare(metrics, expected)
  }

  test("write MetricData with MetricPoints.Gauge") {
    val expected =
      s"""# HELP bar_ratio a fun little gauge
         |# TYPE bar_ratio gauge
         |bar_ratio{A="B",C="D",otel_scope_name="testmeter",otel_scope_version="v0.1.0"} 0.75
         |# HELP otel_scope_info Instrumentation Scope metadata
         |# TYPE otel_scope_info gauge
         |otel_scope_info{E="F",otel_scope_name="testmeter",otel_scope_version="v0.1.0"} 1
         |# HELP target_info Target metadata
         |# TYPE target_info gauge
         |target_info{service_name="unknown_service:scala",telemetry_sdk_language="scala",telemetry_sdk_name="otel4s",telemetry_sdk_version="${BuildInfo.version}"} 1
         |""".stripMargin

    val metrics = Vector(
      mkGauge(
        "bar",
        "a fun little gauge".some,
        "1".some,
        NonEmptyVector
          .of(
            (0.75, Attributes(Attribute("A", "B"), Attribute("C", "D")))
          )
      )
    )

    writeAndCompare(metrics, expected)
  }

  test("write MetricData with MetricPoints.Histogram") {
    val expected =
      s"""# HELP histogram_baz_bytes a very nice histogram
         |# TYPE histogram_baz_bytes histogram
         |histogram_baz_bytes_bucket{A="B",C="D",otel_scope_name="testmeter",otel_scope_version="v0.1.0",le="0"} 0
         |histogram_baz_bytes_bucket{A="B",C="D",otel_scope_name="testmeter",otel_scope_version="v0.1.0",le="5"} 0
         |histogram_baz_bytes_bucket{A="B",C="D",otel_scope_name="testmeter",otel_scope_version="v0.1.0",le="10"} 1
         |histogram_baz_bytes_bucket{A="B",C="D",otel_scope_name="testmeter",otel_scope_version="v0.1.0",le="25"} 2
         |histogram_baz_bytes_bucket{A="B",C="D",otel_scope_name="testmeter",otel_scope_version="v0.1.0",le="50"} 2
         |histogram_baz_bytes_bucket{A="B",C="D",otel_scope_name="testmeter",otel_scope_version="v0.1.0",le="75"} 2
         |histogram_baz_bytes_bucket{A="B",C="D",otel_scope_name="testmeter",otel_scope_version="v0.1.0",le="100"} 2
         |histogram_baz_bytes_bucket{A="B",C="D",otel_scope_name="testmeter",otel_scope_version="v0.1.0",le="250"} 4
         |histogram_baz_bytes_bucket{A="B",C="D",otel_scope_name="testmeter",otel_scope_version="v0.1.0",le="500"} 4
         |histogram_baz_bytes_bucket{A="B",C="D",otel_scope_name="testmeter",otel_scope_version="v0.1.0",le="1000"} 4
         |histogram_baz_bytes_bucket{A="B",C="D",otel_scope_name="testmeter",otel_scope_version="v0.1.0",le="+Inf"} 4
         |histogram_baz_bytes_count{A="B",C="D",otel_scope_name="testmeter",otel_scope_version="v0.1.0"} 4
         |histogram_baz_bytes_sum{A="B",C="D",otel_scope_name="testmeter",otel_scope_version="v0.1.0"} 236
         |# HELP otel_scope_info Instrumentation Scope metadata
         |# TYPE otel_scope_info gauge
         |otel_scope_info{E="F",otel_scope_name="testmeter",otel_scope_version="v0.1.0"} 1
         |# HELP target_info Target metadata
         |# TYPE target_info gauge
         |target_info{service_name="unknown_service:scala",telemetry_sdk_language="scala",telemetry_sdk_name="otel4s",telemetry_sdk_version="${BuildInfo.version}"} 1
         |""".stripMargin

    val metrics = Vector(
      mkHistogram(
        "histogram_baz",
        "a very nice histogram".some,
        "By".some,
        NonEmptyVector.of(
          (
            PointData.Histogram
              .Stats(
                sum = 236.0,
                min = 7.0,
                max = 105.0,
                count = 4L
              ),
            Vector(0.0, 5.0, 10.0, 25.0, 50.0, 75.0, 100.0, 250.0, 500.0, 1000.0),
            Vector(0, 0, 1, 1, 0, 0, 0, 2, 0, 0),
            Attributes(Attribute("A", "B"), Attribute("C", "D"))
          )
        )
      )
    )

    writeAndCompare(metrics, expected)
  }

  test("merge or overwrite label values after keys conversion") {
    val expected =
      s"""# HELP foo_total a sanitary counter
         |# TYPE foo_total counter
         |foo_total{A_B="Q",C_D="Y;Z",otel_scope_name="testmeter",otel_scope_version="v0.1.0"} 24.3
         |# HELP otel_scope_info Instrumentation Scope metadata
         |# TYPE otel_scope_info gauge
         |otel_scope_info{E="F",otel_scope_name="testmeter",otel_scope_version="v0.1.0"} 1
         |# HELP target_info Target metadata
         |# TYPE target_info gauge
         |target_info{service_name="unknown_service:scala",telemetry_sdk_language="scala",telemetry_sdk_name="otel4s",telemetry_sdk_version="${BuildInfo.version}"} 1
         |""".stripMargin

    val metrics = Vector(
      mkSum(
        "foo",
        "a sanitary counter".some,
        "By".some,
        NonEmptyVector
          .of(
            (
              24.3,
              // exact match, value should be overwritten
              // unintended match due to sanitization, values should be concatenated
              Attributes(Attribute("A.B", "X"), Attribute("A.B", "Q"), Attribute("C.D", "Y"), Attribute("C/D", "Z"))
            ),
          )
      )
    )

    writeAndCompare(metrics, expected, Config.default.withoutUnits)
  }

  test("rename invalid metric names") {
    val expected =
      s"""# HELP bar a fun little gauge
         |# TYPE bar gauge
         |bar{A="B",C="D",otel_scope_name="testmeter",otel_scope_version="v0.1.0"} 75
         |# HELP invalid_gauge_name a gauge with an invalid name
         |# TYPE invalid_gauge_name gauge
         |invalid_gauge_name{A="B",C="D",otel_scope_name="testmeter",otel_scope_version="v0.1.0"} 100
         |# HELP _invalid_counter_name_total a counter with an invalid name
         |# TYPE _invalid_counter_name_total counter
         |_invalid_counter_name_total{A="B",C="D",otel_scope_name="testmeter",otel_scope_version="v0.1.0"} 100
         |# HELP invalid_hist_name a histogram with an invalid name
         |# TYPE invalid_hist_name histogram
         |invalid_hist_name_bucket{A="B",C="D",otel_scope_name="testmeter",otel_scope_version="v0.1.0",le="0"} 0
         |invalid_hist_name_bucket{A="B",C="D",otel_scope_name="testmeter",otel_scope_version="v0.1.0",le="5"} 0
         |invalid_hist_name_bucket{A="B",C="D",otel_scope_name="testmeter",otel_scope_version="v0.1.0",le="10"} 0
         |invalid_hist_name_bucket{A="B",C="D",otel_scope_name="testmeter",otel_scope_version="v0.1.0",le="25"} 1
         |invalid_hist_name_bucket{A="B",C="D",otel_scope_name="testmeter",otel_scope_version="v0.1.0",le="50"} 1
         |invalid_hist_name_bucket{A="B",C="D",otel_scope_name="testmeter",otel_scope_version="v0.1.0",le="75"} 1
         |invalid_hist_name_bucket{A="B",C="D",otel_scope_name="testmeter",otel_scope_version="v0.1.0",le="100"} 1
         |invalid_hist_name_bucket{A="B",C="D",otel_scope_name="testmeter",otel_scope_version="v0.1.0",le="250"} 1
         |invalid_hist_name_bucket{A="B",C="D",otel_scope_name="testmeter",otel_scope_version="v0.1.0",le="500"} 1
         |invalid_hist_name_bucket{A="B",C="D",otel_scope_name="testmeter",otel_scope_version="v0.1.0",le="1000"} 1
         |invalid_hist_name_bucket{A="B",C="D",otel_scope_name="testmeter",otel_scope_version="v0.1.0",le="+Inf"} 1
         |invalid_hist_name_count{A="B",C="D",otel_scope_name="testmeter",otel_scope_version="v0.1.0"} 1
         |invalid_hist_name_sum{A="B",C="D",otel_scope_name="testmeter",otel_scope_version="v0.1.0"} 23
         |# HELP otel_scope_info Instrumentation Scope metadata
         |# TYPE otel_scope_info gauge
         |otel_scope_info{E="F",otel_scope_name="testmeter",otel_scope_version="v0.1.0"} 1
         |# HELP target_info Target metadata
         |# TYPE target_info gauge
         |target_info{service_name="unknown_service:scala",telemetry_sdk_language="scala",telemetry_sdk_name="otel4s",telemetry_sdk_version="${BuildInfo.version}"} 1
         |""".stripMargin

    val metrics = Vector(
      mkGauge(
        "bar",
        "a fun little gauge".some,
        none,
        NonEmptyVector
          .of(
            (75.0, Attributes(Attribute("A", "B"), Attribute("C", "D")))
          )
      ),
      mkGauge(
        "invalid.gauge.name",
        "a gauge with an invalid name".some,
        none,
        NonEmptyVector
          .of(
            (100.0, Attributes(Attribute("A", "B"), Attribute("C", "D")))
          )
      ),
      mkSum(
        "0invalid.counter.name",
        "a counter with an invalid name".some,
        none,
        NonEmptyVector
          .of(
            (100.0, Attributes(Attribute("A", "B"), Attribute("C", "D")))
          )
      ),
      mkHistogram(
        "invalid.hist.name",
        "a histogram with an invalid name".some,
        none,
        NonEmptyVector.of(
          (
            PointData.Histogram
              .Stats(
                sum = 23.0,
                min = 23.0,
                max = 23.0,
                count = 1L
              ),
            Vector(0.0, 5.0, 10.0, 25.0, 50.0, 75.0, 100.0, 250.0, 500.0, 1000.0),
            Vector(0, 0, 0, 1, 0, 0, 0, 0, 0, 0),
            Attributes(Attribute("A", "B"), Attribute("C", "D"))
          )
        )
      )
    )

    writeAndCompare(metrics, expected)
  }

  test("write custom resource") {
    val expected =
      """# HELP foo_total a simple counter
         |# TYPE foo_total counter
         |foo_total{A="B",C="D",E="true",F="42",otel_scope_name="testmeter",otel_scope_version="v0.1.0"} 24.3
         |# HELP otel_scope_info Instrumentation Scope metadata
         |# TYPE otel_scope_info gauge
         |otel_scope_info{G="H",otel_scope_name="testmeter",otel_scope_version="v0.1.0"} 1
         |# HELP target_info Target metadata
         |# TYPE target_info gauge
         |target_info{A="B",C="D"} 1
         |""".stripMargin

    val metrics = Vector(
      mkSum(
        "foo",
        "a simple counter".some,
        none,
        NonEmptyVector
          .of(
            (24.3, Attributes(Attribute("A", "B"), Attribute("C", "D"), Attribute("E", true), Attribute("F", 42L))),
          ),
        scopeAttributes = Attributes(Attribute("G", "H")),
        resource = TelemetryResource(Attributes(Attribute("A", "B"), Attribute("C", "D")))
      )
    )

    writeAndCompare(metrics, expected)
  }

  test("write result without target_info when it is disabled") {
    val expected =
      """# HELP foo_total a simple counter
         |# TYPE foo_total counter
         |foo_total{A="B",C="D",E="true",F="42",otel_scope_name="testmeter",otel_scope_version="v0.1.0"} 24.3
         |# HELP otel_scope_info Instrumentation Scope metadata
         |# TYPE otel_scope_info gauge
         |otel_scope_info{G="H",otel_scope_name="testmeter",otel_scope_version="v0.1.0"} 1
         |""".stripMargin

    val metrics = Vector(
      mkSum(
        "foo",
        "a simple counter".some,
        none,
        NonEmptyVector
          .of(
            (24.3, Attributes(Attribute("A", "B"), Attribute("C", "D"), Attribute("E", true), Attribute("F", 42L))),
          ),
        scopeAttributes = Attributes(Attribute("G", "H"))
      )
    )

    writeAndCompare(metrics, expected, Config.default.disableTargetInfo)
  }

  test("write result without scope_info when it is disabled") {
    val expected =
      s"""# HELP bar_ratio a fun little gauge
         |# TYPE bar_ratio gauge
         |bar_ratio{A="B",C="D"} 1
         |# HELP target_info Target metadata
         |# TYPE target_info gauge
         |target_info{service_name="unknown_service:scala",telemetry_sdk_language="scala",telemetry_sdk_name="otel4s",telemetry_sdk_version="${BuildInfo.version}"} 1
         |""".stripMargin

    val metrics = Vector(
      mkGauge(
        "bar",
        "a fun little gauge".some,
        "1".some,
        NonEmptyVector
          .of(
            (1.0, Attributes(Attribute("A", "B"), Attribute("C", "D")))
          )
      )
    )

    writeAndCompare(metrics, expected, Config.default.disableScopeInfo)
  }

  test("write result without scope_info and target_info when they are disabled") {
    val expected =
      """# HELP bar_bytes_total a fun little counter
         |# TYPE bar_bytes_total counter
         |bar_bytes_total{A="B",C="D"} 3
         |""".stripMargin

    val metrics = Vector(
      mkSum(
        "bar",
        "a fun little counter".some,
        "By".some,
        NonEmptyVector
          .of(
            (3.0, Attributes(Attribute("A", "B"), Attribute("C", "D"))),
          ),
        scopeAttributes = Attributes(Attribute("G", "H"))
      )
    )

    writeAndCompare(metrics, expected, Config.default.disableScopeInfo.disableTargetInfo)
  }

  test("write multiple scopes") {
    val expected =
      s"""# HELP foo_milliseconds_total meter foo counter
         |# TYPE foo_milliseconds_total counter
         |foo_milliseconds_total{type="foo",otel_scope_name="meterfoo",otel_scope_version="v0.1.0"} 100
         |# HELP bar_milliseconds_total meter bar counter
         |# TYPE bar_milliseconds_total counter
         |bar_milliseconds_total{type="bar",otel_scope_name="meterbar",otel_scope_version="v0.1.0"} 200
         |# HELP otel_scope_info Instrumentation Scope metadata
         |# TYPE otel_scope_info gauge
         |otel_scope_info{E="F",otel_scope_name="meterfoo",otel_scope_version="v0.1.0"} 1
         |otel_scope_info{E="F",otel_scope_name="meterbar",otel_scope_version="v0.1.0"} 1
         |# HELP target_info Target metadata
         |# TYPE target_info gauge
         |target_info{service_name="unknown_service:scala",telemetry_sdk_language="scala",telemetry_sdk_name="otel4s",telemetry_sdk_version="${BuildInfo.version}"} 1
         |""".stripMargin

    val metrics = Vector(
      mkSum(
        "foo",
        "meter foo counter".some,
        "ms".some,
        NonEmptyVector
          .of(
            (100.0, Attributes(Attribute("type", "foo")))
          ),
        scopeName = "meterfoo"
      ),
      mkSum(
        "bar",
        "meter bar counter".some,
        "ms".some,
        NonEmptyVector
          .of(
            (200.0, Attributes(Attribute("type", "bar")))
          ),
        scopeName = "meterbar"
      )
    )

    writeAndCompare(metrics, expected)
  }

  test("group together counters with the same name") {
    val expected =
      s"""# HELP foo_bytes_total meter counter foo
         |# TYPE foo_bytes_total counter
         |foo_bytes_total{A="B",otel_scope_name="ma",otel_scope_version="v0.1.0"} 100
         |foo_bytes_total{A="B",otel_scope_name="mb",otel_scope_version="v0.1.0"} 100
         |# HELP otel_scope_info Instrumentation Scope metadata
         |# TYPE otel_scope_info gauge
         |otel_scope_info{E="F",otel_scope_name="ma",otel_scope_version="v0.1.0"} 1
         |otel_scope_info{E="F",otel_scope_name="mb",otel_scope_version="v0.1.0"} 1
         |# HELP target_info Target metadata
         |# TYPE target_info gauge
         |target_info{service_name="unknown_service:scala",telemetry_sdk_language="scala",telemetry_sdk_name="otel4s",telemetry_sdk_version="${BuildInfo.version}"} 1
         |""".stripMargin

    val metrics = Vector(
      mkSum(
        "foo",
        "meter counter foo".some,
        "By".some,
        NonEmptyVector
          .of(
            (100.0, Attributes(Attribute("A", "B")))
          ),
        scopeName = "ma"
      ),
      mkSum(
        "foo",
        "meter counter foo".some,
        "By".some,
        NonEmptyVector
          .of(
            (100.0, Attributes(Attribute("A", "B")))
          ),
        scopeName = "mb"
      )
    )

    writeAndCompare(metrics, expected)
  }

  test("group together gauges with the same name") {
    val expected =
      s"""# HELP foo_bytes meter gauge foo
         |# TYPE foo_bytes gauge
         |foo_bytes{A="B",otel_scope_name="ma",otel_scope_version="v0.1.0"} 100
         |foo_bytes{A="B",otel_scope_name="mb",otel_scope_version="v0.1.0"} 100
         |# HELP otel_scope_info Instrumentation Scope metadata
         |# TYPE otel_scope_info gauge
         |otel_scope_info{E="F",otel_scope_name="ma",otel_scope_version="v0.1.0"} 1
         |otel_scope_info{E="F",otel_scope_name="mb",otel_scope_version="v0.1.0"} 1
         |# HELP target_info Target metadata
         |# TYPE target_info gauge
         |target_info{service_name="unknown_service:scala",telemetry_sdk_language="scala",telemetry_sdk_name="otel4s",telemetry_sdk_version="${BuildInfo.version}"} 1
         |""".stripMargin

    val metrics = Vector(
      mkGauge(
        "foo",
        "meter gauge foo".some,
        "By".some,
        NonEmptyVector
          .of(
            (100.0, Attributes(Attribute("A", "B")))
          ),
        scopeName = "ma"
      ),
      mkGauge(
        "foo",
        "meter gauge foo".some,
        "By".some,
        NonEmptyVector
          .of(
            (100.0, Attributes(Attribute("A", "B")))
          ),
        scopeName = "mb"
      )
    )

    writeAndCompare(metrics, expected)
  }

  test("group together histograms with the same name") {
    val expected =
      s"""# HELP foo_bytes meter histogram foo
         |# TYPE foo_bytes histogram
         |foo_bytes_bucket{A="B",otel_scope_name="ma",otel_scope_version="v0.1.0",le="0"} 0
         |foo_bytes_bucket{A="B",otel_scope_name="ma",otel_scope_version="v0.1.0",le="5"} 0
         |foo_bytes_bucket{A="B",otel_scope_name="ma",otel_scope_version="v0.1.0",le="10"} 0
         |foo_bytes_bucket{A="B",otel_scope_name="ma",otel_scope_version="v0.1.0",le="25"} 0
         |foo_bytes_bucket{A="B",otel_scope_name="ma",otel_scope_version="v0.1.0",le="50"} 0
         |foo_bytes_bucket{A="B",otel_scope_name="ma",otel_scope_version="v0.1.0",le="75"} 0
         |foo_bytes_bucket{A="B",otel_scope_name="ma",otel_scope_version="v0.1.0",le="100"} 1
         |foo_bytes_bucket{A="B",otel_scope_name="ma",otel_scope_version="v0.1.0",le="250"} 1
         |foo_bytes_bucket{A="B",otel_scope_name="ma",otel_scope_version="v0.1.0",le="500"} 1
         |foo_bytes_bucket{A="B",otel_scope_name="ma",otel_scope_version="v0.1.0",le="1000"} 1
         |foo_bytes_bucket{A="B",otel_scope_name="ma",otel_scope_version="v0.1.0",le="+Inf"} 1
         |foo_bytes_count{A="B",otel_scope_name="ma",otel_scope_version="v0.1.0"} 1
         |foo_bytes_sum{A="B",otel_scope_name="ma",otel_scope_version="v0.1.0"} 100
         |foo_bytes_bucket{A="B",otel_scope_name="mb",otel_scope_version="v0.1.0",le="0"} 0
         |foo_bytes_bucket{A="B",otel_scope_name="mb",otel_scope_version="v0.1.0",le="5"} 0
         |foo_bytes_bucket{A="B",otel_scope_name="mb",otel_scope_version="v0.1.0",le="10"} 0
         |foo_bytes_bucket{A="B",otel_scope_name="mb",otel_scope_version="v0.1.0",le="25"} 0
         |foo_bytes_bucket{A="B",otel_scope_name="mb",otel_scope_version="v0.1.0",le="50"} 0
         |foo_bytes_bucket{A="B",otel_scope_name="mb",otel_scope_version="v0.1.0",le="75"} 0
         |foo_bytes_bucket{A="B",otel_scope_name="mb",otel_scope_version="v0.1.0",le="100"} 1
         |foo_bytes_bucket{A="B",otel_scope_name="mb",otel_scope_version="v0.1.0",le="250"} 1
         |foo_bytes_bucket{A="B",otel_scope_name="mb",otel_scope_version="v0.1.0",le="500"} 1
         |foo_bytes_bucket{A="B",otel_scope_name="mb",otel_scope_version="v0.1.0",le="1000"} 1
         |foo_bytes_bucket{A="B",otel_scope_name="mb",otel_scope_version="v0.1.0",le="+Inf"} 1
         |foo_bytes_count{A="B",otel_scope_name="mb",otel_scope_version="v0.1.0"} 1
         |foo_bytes_sum{A="B",otel_scope_name="mb",otel_scope_version="v0.1.0"} 100
         |# HELP otel_scope_info Instrumentation Scope metadata
         |# TYPE otel_scope_info gauge
         |otel_scope_info{E="F",otel_scope_name="ma",otel_scope_version="v0.1.0"} 1
         |otel_scope_info{E="F",otel_scope_name="mb",otel_scope_version="v0.1.0"} 1
         |# HELP target_info Target metadata
         |# TYPE target_info gauge
         |target_info{service_name="unknown_service:scala",telemetry_sdk_language="scala",telemetry_sdk_name="otel4s",telemetry_sdk_version="${BuildInfo.version}"} 1
         |""".stripMargin

    val metrics = Vector(
      mkHistogram(
        "foo",
        "meter histogram foo".some,
        "By".some,
        NonEmptyVector.of(
          (
            PointData.Histogram
              .Stats(
                sum = 100.0,
                min = 100.0,
                max = 100.0,
                count = 1L
              ),
            Vector(0.0, 5.0, 10.0, 25.0, 50.0, 75.0, 100.0, 250.0, 500.0, 1000.0),
            Vector(0, 0, 0, 0, 0, 0, 1, 0, 0, 0),
            Attributes(Attribute("A", "B"))
          )
        ),
        scopeName = "ma"
      ),
      mkHistogram(
        "foo",
        "meter histogram foo".some,
        "By".some,
        NonEmptyVector.of(
          (
            PointData.Histogram
              .Stats(
                sum = 100.0,
                min = 100.0,
                max = 100.0,
                count = 1L
              ),
            Vector(0.0, 5.0, 10.0, 25.0, 50.0, 75.0, 100.0, 250.0, 500.0, 1000.0),
            Vector(0, 0, 0, 0, 0, 0, 1, 0, 0, 0),
            Attributes(Attribute("A", "B"))
          )
        ),
        scopeName = "mb"
      )
    )

    writeAndCompare(metrics, expected)
  }

  test("group together counters with the same name and drop conflicting help") {
    val expected =
      s"""# HELP bar_bytes_total meter a bar
         |# TYPE bar_bytes_total counter
         |bar_bytes_total{type="bar",otel_scope_name="ma",otel_scope_version="v0.1.0"} 100
         |bar_bytes_total{type="bar",otel_scope_name="mb",otel_scope_version="v0.1.0"} 100
         |# HELP otel_scope_info Instrumentation Scope metadata
         |# TYPE otel_scope_info gauge
         |otel_scope_info{E="F",otel_scope_name="ma",otel_scope_version="v0.1.0"} 1
         |otel_scope_info{E="F",otel_scope_name="mb",otel_scope_version="v0.1.0"} 1
         |# HELP target_info Target metadata
         |# TYPE target_info gauge
         |target_info{service_name="unknown_service:scala",telemetry_sdk_language="scala",telemetry_sdk_name="otel4s",telemetry_sdk_version="${BuildInfo.version}"} 1
         |""".stripMargin

    val metrics = Vector(
      mkSum(
        "bar",
        "meter a bar".some,
        "By".some,
        NonEmptyVector
          .of(
            (100.0, Attributes(Attribute("type", "bar")))
          ),
        scopeName = "ma"
      ),
      mkSum(
        "bar",
        "meter b bar".some,
        "By".some,
        NonEmptyVector
          .of(
            (100.0, Attributes(Attribute("type", "bar")))
          ),
        scopeName = "mb"
      )
    )

    writeAndCompare(metrics, expected)
  }

  test("group together gauges with the same name and drop conflicting help") {
    val expected =
      s"""# HELP bar_bytes meter a bar
         |# TYPE bar_bytes gauge
         |bar_bytes{type="bar",otel_scope_name="ma",otel_scope_version="v0.1.0"} 100
         |bar_bytes{type="bar",otel_scope_name="mb",otel_scope_version="v0.1.0"} 100
         |# HELP otel_scope_info Instrumentation Scope metadata
         |# TYPE otel_scope_info gauge
         |otel_scope_info{E="F",otel_scope_name="ma",otel_scope_version="v0.1.0"} 1
         |otel_scope_info{E="F",otel_scope_name="mb",otel_scope_version="v0.1.0"} 1
         |# HELP target_info Target metadata
         |# TYPE target_info gauge
         |target_info{service_name="unknown_service:scala",telemetry_sdk_language="scala",telemetry_sdk_name="otel4s",telemetry_sdk_version="${BuildInfo.version}"} 1
         |""".stripMargin

    val metrics = Vector(
      mkGauge(
        "bar",
        "meter a bar".some,
        "By".some,
        NonEmptyVector
          .of(
            (100.0, Attributes(Attribute("type", "bar")))
          ),
        scopeName = "ma"
      ),
      mkGauge(
        "bar",
        "meter b bar".some,
        "By".some,
        NonEmptyVector
          .of(
            (100.0, Attributes(Attribute("type", "bar")))
          ),
        scopeName = "mb"
      )
    )

    writeAndCompare(metrics, expected)
  }

  test("group together histograms with the same name and drop conflicting help") {
    val expected =
      s"""# HELP bar_bytes meter a bar
         |# TYPE bar_bytes histogram
         |bar_bytes_bucket{A="B",otel_scope_name="ma",otel_scope_version="v0.1.0",le="0"} 0
         |bar_bytes_bucket{A="B",otel_scope_name="ma",otel_scope_version="v0.1.0",le="5"} 0
         |bar_bytes_bucket{A="B",otel_scope_name="ma",otel_scope_version="v0.1.0",le="10"} 0
         |bar_bytes_bucket{A="B",otel_scope_name="ma",otel_scope_version="v0.1.0",le="25"} 0
         |bar_bytes_bucket{A="B",otel_scope_name="ma",otel_scope_version="v0.1.0",le="50"} 0
         |bar_bytes_bucket{A="B",otel_scope_name="ma",otel_scope_version="v0.1.0",le="75"} 0
         |bar_bytes_bucket{A="B",otel_scope_name="ma",otel_scope_version="v0.1.0",le="100"} 1
         |bar_bytes_bucket{A="B",otel_scope_name="ma",otel_scope_version="v0.1.0",le="250"} 1
         |bar_bytes_bucket{A="B",otel_scope_name="ma",otel_scope_version="v0.1.0",le="500"} 1
         |bar_bytes_bucket{A="B",otel_scope_name="ma",otel_scope_version="v0.1.0",le="1000"} 1
         |bar_bytes_bucket{A="B",otel_scope_name="ma",otel_scope_version="v0.1.0",le="+Inf"} 1
         |bar_bytes_count{A="B",otel_scope_name="ma",otel_scope_version="v0.1.0"} 1
         |bar_bytes_sum{A="B",otel_scope_name="ma",otel_scope_version="v0.1.0"} 100
         |bar_bytes_bucket{A="B",otel_scope_name="mb",otel_scope_version="v0.1.0",le="0"} 0
         |bar_bytes_bucket{A="B",otel_scope_name="mb",otel_scope_version="v0.1.0",le="5"} 0
         |bar_bytes_bucket{A="B",otel_scope_name="mb",otel_scope_version="v0.1.0",le="10"} 0
         |bar_bytes_bucket{A="B",otel_scope_name="mb",otel_scope_version="v0.1.0",le="25"} 0
         |bar_bytes_bucket{A="B",otel_scope_name="mb",otel_scope_version="v0.1.0",le="50"} 0
         |bar_bytes_bucket{A="B",otel_scope_name="mb",otel_scope_version="v0.1.0",le="75"} 0
         |bar_bytes_bucket{A="B",otel_scope_name="mb",otel_scope_version="v0.1.0",le="100"} 1
         |bar_bytes_bucket{A="B",otel_scope_name="mb",otel_scope_version="v0.1.0",le="250"} 1
         |bar_bytes_bucket{A="B",otel_scope_name="mb",otel_scope_version="v0.1.0",le="500"} 1
         |bar_bytes_bucket{A="B",otel_scope_name="mb",otel_scope_version="v0.1.0",le="1000"} 1
         |bar_bytes_bucket{A="B",otel_scope_name="mb",otel_scope_version="v0.1.0",le="+Inf"} 1
         |bar_bytes_count{A="B",otel_scope_name="mb",otel_scope_version="v0.1.0"} 1
         |bar_bytes_sum{A="B",otel_scope_name="mb",otel_scope_version="v0.1.0"} 100
         |# HELP otel_scope_info Instrumentation Scope metadata
         |# TYPE otel_scope_info gauge
         |otel_scope_info{E="F",otel_scope_name="ma",otel_scope_version="v0.1.0"} 1
         |otel_scope_info{E="F",otel_scope_name="mb",otel_scope_version="v0.1.0"} 1
         |# HELP target_info Target metadata
         |# TYPE target_info gauge
         |target_info{service_name="unknown_service:scala",telemetry_sdk_language="scala",telemetry_sdk_name="otel4s",telemetry_sdk_version="${BuildInfo.version}"} 1
         |""".stripMargin

    val metrics = Vector(
      mkHistogram(
        "bar",
        "meter a bar".some,
        "By".some,
        NonEmptyVector.of(
          (
            PointData.Histogram
              .Stats(
                sum = 100.0,
                min = 100.0,
                max = 100.0,
                count = 1L
              ),
            Vector(0.0, 5.0, 10.0, 25.0, 50.0, 75.0, 100.0, 250.0, 500.0, 1000.0),
            Vector(0, 0, 0, 0, 0, 0, 1, 0, 0, 0),
            Attributes(Attribute("A", "B"))
          )
        ),
        scopeName = "ma"
      ),
      mkHistogram(
        "bar",
        "meter b bar".some,
        "By".some,
        NonEmptyVector.of(
          (
            PointData.Histogram
              .Stats(
                sum = 100.0,
                min = 100.0,
                max = 100.0,
                count = 1L
              ),
            Vector(0.0, 5.0, 10.0, 25.0, 50.0, 75.0, 100.0, 250.0, 500.0, 1000.0),
            Vector(0, 0, 0, 0, 0, 0, 1, 0, 0, 0),
            Attributes(Attribute("A", "B"))
          )
        ),
        scopeName = "mb"
      )
    )

    writeAndCompare(metrics, expected)
  }

  test("drop gauge conflicting with counter with the same name") {
    val expectedText =
      s"""# HELP foo_total meter foo
         |# TYPE foo_total counter
         |foo_total{type="foo",otel_scope_name="ma",otel_scope_version="v0.1.0"} 100
         |# HELP otel_scope_info Instrumentation Scope metadata
         |# TYPE otel_scope_info gauge
         |otel_scope_info{E="F",otel_scope_name="ma",otel_scope_version="v0.1.0"} 1
         |# HELP target_info Target metadata
         |# TYPE target_info gauge
         |target_info{service_name="unknown_service:scala",telemetry_sdk_language="scala",telemetry_sdk_name="otel4s",telemetry_sdk_version="${BuildInfo.version}"} 1
         |""".stripMargin

    val expectedConsoleEntries = {
      import org.typelevel.otel4s.sdk.test.InMemoryConsole._

      List(
        Entry(
          Op.Errorln,
          "Conflicting metric name [foo]. Existing metric type [counter], dropped metric type [gauge]"
        )
      )
    }

    val metrics = Vector(
      mkSum(
        "foo",
        "meter foo".some,
        "By".some,
        NonEmptyVector
          .of(
            (100.0, Attributes(Attribute("type", "foo")))
          ),
        scopeName = "ma"
      ),
      mkGauge(
        "foo_total",
        "meter foo".some,
        "By".some,
        NonEmptyVector
          .of(
            (200.0, Attributes(Attribute("type", "foo")))
          ),
        scopeName = "mb"
      )
    )

    InMemoryConsole.create[IO].flatMap { implicit C: InMemoryConsole[IO] =>
      for {
        _ <- writeAndCompare(metrics, expectedText, Config.default.withoutUnits)
        _ <- C.entries.assertEquals(expectedConsoleEntries)
      } yield ()
    }
  }

  test("drop histogram conflicting with gauge with the same name") {
    val expectedText =
      s"""# HELP foo_bytes meter gauge foo
         |# TYPE foo_bytes gauge
         |foo_bytes{A="B",otel_scope_name="ma",otel_scope_version="v0.1.0"} 100
         |# HELP otel_scope_info Instrumentation Scope metadata
         |# TYPE otel_scope_info gauge
         |otel_scope_info{E="F",otel_scope_name="ma",otel_scope_version="v0.1.0"} 1
         |# HELP target_info Target metadata
         |# TYPE target_info gauge
         |target_info{service_name="unknown_service:scala",telemetry_sdk_language="scala",telemetry_sdk_name="otel4s",telemetry_sdk_version="${BuildInfo.version}"} 1
         |""".stripMargin

    val expectedConsoleEntries = {
      import org.typelevel.otel4s.sdk.test.InMemoryConsole._

      List(
        Entry(
          Op.Errorln,
          "Conflicting metric name [foo_bytes]. Existing metric type [gauge], dropped metric type [histogram]"
        )
      )
    }

    val metrics = Vector(
      mkGauge(
        "foo",
        "meter gauge foo".some,
        "By".some,
        NonEmptyVector
          .of(
            (100.0, Attributes(Attribute("A", "B")))
          ),
        scopeName = "ma"
      ),
      mkHistogram(
        "foo",
        "meter histogram foo".some,
        "By".some,
        NonEmptyVector.of(
          (
            PointData.Histogram
              .Stats(
                sum = 100.0,
                min = 100.0,
                max = 100.0,
                count = 1L
              ),
            Vector(0.0, 5.0, 10.0, 25.0, 50.0, 75.0, 100.0, 250.0, 500.0, 1000.0),
            Vector(0, 0, 0, 0, 0, 0, 1, 0, 0, 0),
            Attributes(Attribute("A", "B"))
          )
        ),
        scopeName = "mb"
      )
    )

    InMemoryConsole.create[IO].flatMap { implicit C: InMemoryConsole[IO] =>
      for {
        _ <- writeAndCompare(metrics, expectedText)
        _ <- C.entries.assertEquals(expectedConsoleEntries)
      } yield ()
    }
  }

  test("escape help and label values") {
    val expected =
      """# HELP foo_total some text \\ and \n some \" escaping
        |# TYPE foo_total counter
        |foo_total{A="B\n",C="\\D\"",E="true",F="42",G="[\"H\",\"I\\\"\"]",J="[true,false]",otel_scope_name="testmeter",otel_scope_version="v0.1.0"} 24.3
        |# HELP otel_scope_info Instrumentation Scope metadata
        |# TYPE otel_scope_info gauge
        |otel_scope_info{G="H\n",L="\\M\"",otel_scope_name="testmeter",otel_scope_version="v0.1.0"} 1
        |# HELP target_info Target metadata
        |# TYPE target_info gauge
        |target_info{A="B\n",C="\\D\""} 1
        |""".stripMargin

    val metrics = Vector(
      mkSum(
        "foo",
        "some text \\ and \n some \" escaping".some,
        none,
        NonEmptyVector
          .of(
            (
              24.3,
              Attributes(
                Attribute("A", "B\n"),
                Attribute("C", "\\D\""),
                Attribute("E", true),
                Attribute("F", 42L),
                Attribute("G", Seq("H", "I\"")),
                Attribute("J", Seq(true, false))
              )
            ),
          ),
        scopeAttributes = Attributes(Attribute("G", "H\n"), Attribute("L", "\\M\"")),
        resource = TelemetryResource(Attributes(Attribute("A", "B\n"), Attribute("C", "\\D\"")))
      )
    )

    writeAndCompare(metrics, expected)
  }

  private def getTimeWindow: TimeWindow =
    Gens.timeWindow.sample.getOrElse(getTimeWindow)

  private def mkSum(
      metricName: String,
      metricDescription: Option[String],
      metricUnit: Option[String],
      points: NonEmptyVector[(Double, Attributes)],
      resource: TelemetryResource = TelemetryResource.default,
      scopeName: String = "testmeter",
      scopeVersion: String = "v0.1.0",
      scopeAttributes: Attributes = Attributes(Attribute("E", "F")),
      temporality: AggregationTemporality = AggregationTemporality.Cumulative,
      monotonic: Boolean = true
  ) = {
    MetricData(
      resource,
      InstrumentationScope
        .builder(scopeName)
        .withVersion(scopeVersion)
        .withAttributes(scopeAttributes)
        .build,
      metricName,
      metricDescription,
      metricUnit,
      MetricPoints.sum(
        points
          .map { case (value, attrs) =>
            PointData.doubleNumber(
              getTimeWindow,
              attrs,
              Vector.empty,
              value
            )
          },
        monotonic,
        temporality
      )
    )
  }

  private def mkGauge(
      metricName: String,
      metricDescription: Option[String],
      metricUnit: Option[String],
      points: NonEmptyVector[(Double, Attributes)],
      resource: TelemetryResource = TelemetryResource.default,
      scopeName: String = "testmeter",
      scopeVersion: String = "v0.1.0",
      scopeAttributes: Attributes = Attributes(Attribute("E", "F"))
  ) = {
    MetricData(
      resource,
      InstrumentationScope
        .builder(scopeName)
        .withVersion(scopeVersion)
        .withAttributes(scopeAttributes)
        .build,
      metricName,
      metricDescription,
      metricUnit,
      MetricPoints.gauge(
        points
          .map { case (value, attrs) =>
            PointData.doubleNumber(
              getTimeWindow,
              attrs,
              Vector.empty,
              value
            )
          }
      )
    )
  }

  private def mkHistogram(
      metricName: String,
      metricDescription: Option[String],
      metricUnit: Option[String],
      points: NonEmptyVector[(PointData.Histogram.Stats, Vector[Double], Vector[Long], Attributes)],
      resource: TelemetryResource = TelemetryResource.default,
      scopeName: String = "testmeter",
      scopeVersion: String = "v0.1.0",
      scopeAttributes: Attributes = Attributes(Attribute("E", "F")),
      temporality: AggregationTemporality = AggregationTemporality.Cumulative
  ) = {
    MetricData(
      resource,
      InstrumentationScope
        .builder(scopeName)
        .withVersion(scopeVersion)
        .withAttributes(scopeAttributes)
        .build,
      metricName,
      metricDescription,
      metricUnit,
      MetricPoints.histogram(
        points.map { case (stats, boundaries, counts, attrs) =>
          PointData.histogram(
            getTimeWindow,
            attrs,
            Vector.empty,
            stats.some,
            BucketBoundaries(boundaries),
            counts
          )
        },
        temporality
      )
    )
  }

  private def writeAndCompare(
      metrics: Vector[MetricData],
      expected: String,
      writerConfig: PrometheusWriter.Config = PrometheusWriter.Config.default
  )(implicit C: Console[IO]) = {
    PrometheusWriter
      .text[IO](writerConfig)
      .write(metrics)
      .compile
      .to(Array)
      .map(new String(_, StandardCharsets.UTF_8))
      .flatMap { result =>
        IO(assertEquals(result, expected))
      }
  }
}
