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

package org.typelevel.otel4s.sdk.exporter.prometheus.autoconfigure

import cats.effect.IO
import cats.syntax.either._
import fs2.io.compression._
import munit.CatsEffectSuite
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.test.InMemoryDiagnostic

class PrometheusMetricExporterAutoConfigureSuite extends CatsEffectSuite {

  test("load from an empty config - load default") {
    val config = Config.ofProps(Map.empty)

    val expectedConsoleEntries = {
      import org.typelevel.otel4s.sdk.test.InMemoryDiagnostic._

      List(
        Entry.Info(
          "PrometheusMetricsExporter: launched Prometheus server at localhost:9464/metrics, " +
            "writer options: PrometheusWriter.Config{" +
            "unitSuffixDisabled=false, " +
            "typeSuffixDisabled=false, " +
            "scopeInfoDisabled=false, " +
            "targetInfoDisabled=false}"
        )
      )
    }

    InMemoryDiagnostic.create[IO].flatMap { implicit C: InMemoryDiagnostic[IO] =>
      for {
        _ <- PrometheusMetricExporterAutoConfigure[IO]
          .configure(config)
          .use(_ => IO.unit)
        _ <- C.entries.assertEquals(expectedConsoleEntries)
      } yield ()
    }
  }

  test("load from the config (empty string) - load default") {
    val config = Config.ofProps(
      Map(
        "otel.exporter.prometheus.host" -> "",
        "otel.exporter.prometheus.port" -> "",
        "otel.exporter.prometheus.shutdown.timeout" -> "",
        "otel.exporter.prometheus.default.aggregation" -> "",
        "otel.exporter.prometheus.without.units" -> "",
        "otel.exporter.prometheus.without.type.suffix" -> "",
        "otel.exporter.prometheus.without.scope.info" -> "",
        "otel.exporter.prometheus.without.target.info" -> ""
      )
    )

    val expectedConsoleEntries = {
      import org.typelevel.otel4s.sdk.test.InMemoryDiagnostic._

      List(
        Entry.Info(
          "PrometheusMetricsExporter: launched Prometheus server at localhost:9464/metrics, " +
            "writer options: PrometheusWriter.Config{" +
            "unitSuffixDisabled=false, " +
            "typeSuffixDisabled=false, " +
            "scopeInfoDisabled=false, " +
            "targetInfoDisabled=false}"
        )
      )
    }

    InMemoryDiagnostic.create[IO].flatMap { implicit C: InMemoryDiagnostic[IO] =>
      for {
        _ <- PrometheusMetricExporterAutoConfigure[IO]
          .configure(config)
          .use(_ => IO.unit)
        _ <- C.entries.assertEquals(expectedConsoleEntries)
      } yield ()
    }
  }

  test("load from the config - use given value") {
    val config = Config.ofProps(
      Map(
        "otel.exporter.prometheus.host" -> "127.0.0.2",
        "otel.exporter.prometheus.port" -> "9465",
        "otel.exporter.prometheus.shutdown.timeout" -> "10000",
        "otel.exporter.prometheus.without.units" -> "true",
        "otel.exporter.prometheus.without.type.suffix" -> "true",
        "otel.exporter.prometheus.without.scope.info" -> "true",
        "otel.exporter.prometheus.without.target.info" -> "true"
      )
    )

    val expectedConsoleEntries = {
      import org.typelevel.otel4s.sdk.test.InMemoryDiagnostic._

      List(
        Entry.Info(
          "PrometheusMetricsExporter: launched Prometheus server at 127.0.0.2:9465/metrics, " +
            "writer options: PrometheusWriter.Config{" +
            "unitSuffixDisabled=true, " +
            "typeSuffixDisabled=true, " +
            "scopeInfoDisabled=true, " +
            "targetInfoDisabled=true}"
        )
      )
    }

    InMemoryDiagnostic.create[IO].flatMap { implicit C: InMemoryDiagnostic[IO] =>
      for {
        _ <- PrometheusMetricExporterAutoConfigure[IO].configure(config).use_
        _ <- C.entries.assertEquals(expectedConsoleEntries)
      } yield ()
    }
  }

  test("invalid config value - fail") {
    val config =
      Config.ofProps(Map("otel.exporter.prometheus.default.aggregation" -> "unspecified"))
    val error =
      "Unrecognized default aggregation [unspecified]. Supported options [default]"

    PrometheusMetricExporterAutoConfigure[IO]
      .configure(config)
      .use_
      .attempt
      .map(_.leftMap(_.getMessage))
      .assertEquals(
        Left(
          s"""Cannot autoconfigure [PrometheusMetricExporter].
             |Cause: $error.
             |Config:
             |1) `otel.exporter.prometheus.default.aggregation` - unspecified
             |2) `otel.exporter.prometheus.host` - N/A
             |3) `otel.exporter.prometheus.port` - N/A
             |4) `otel.exporter.prometheus.shutdown.timeout` - N/A
             |5) `otel.exporter.prometheus.without.scope.info` - N/A
             |6) `otel.exporter.prometheus.without.target.info` - N/A
             |7) `otel.exporter.prometheus.without.type.suffix` - N/A
             |8) `otel.exporter.prometheus.without.units` - N/A""".stripMargin
        )
      )
  }

}
