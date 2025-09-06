/*
 * Copyright 2025 Typelevel
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

package org.typelevel.otel4s.sdk.exporter.otlp.logs.autoconfigure

import cats.effect.IO
import cats.syntax.either._
import cats.syntax.foldable._
import fs2.io.compression._
import munit.CatsEffectSuite
import org.typelevel.otel4s.sdk.autoconfigure.Config

class OtlpLogRecordExportersAutoConfigureSuite extends CatsEffectSuite {

  test("load from the config - empty config - load default") {
    val config = Config.ofProps(Map.empty)

    val expected =
      "OtlpLogRecordExporter{client=OtlpClient{" +
        "protocol=http/protobuf, " +
        "endpoint=http://localhost:4318/v1/logs, " +
        "timeout=10 seconds, " +
        "compression=none, " +
        "headers={}}}"

    OtlpLogRecordExporterAutoConfigure[IO]
      .configure(config)
      .use(exporter => IO(assertEquals(exporter.name, expected)))
  }

  test("load from the config - empty string - load default") {
    val config = Config.ofProps(
      Map(
        "otel.exporter.otlp.protocol" -> "",
        "otel.exporter.otlp.logs.protocol" -> ""
      )
    )

    val expected =
      "OtlpLogRecordExporter{client=OtlpClient{" +
        "protocol=http/protobuf, " +
        "endpoint=http://localhost:4318/v1/logs, " +
        "timeout=10 seconds, " +
        "compression=none, " +
        "headers={}}}"

    OtlpLogRecordExporterAutoConfigure[IO]
      .configure(config)
      .use(exporter => IO(assertEquals(exporter.name, expected)))
  }

  test("load from the config - prioritize 'logs' properties") {
    val config = Config.ofProps(
      Map(
        "otel.exporter.otlp.protocol" -> "http/protobuf",
        "otel.exporter.otlp.logs.protocol" -> "http/json"
      )
    )

    val expected =
      "OtlpLogRecordExporter{client=OtlpClient{" +
        "protocol=http/json, " +
        "endpoint=http://localhost:4318/v1/logs, " +
        "timeout=10 seconds, " +
        "compression=none, " +
        "headers={}}}"

    OtlpLogRecordExporterAutoConfigure[IO]
      .configure(config)
      .use(exporter => IO(assertEquals(exporter.name, expected)))
  }

  test("load from the config - generate a valid endpoint") {
    val endpoints = List(
      "http://localhost:4318",
      "http://localhost:4318/"
    )

    val expected =
      "OtlpLogRecordExporter{client=OtlpClient{" +
        "protocol=http/protobuf, " +
        "endpoint=http://localhost:4318/v1/logs, " +
        "timeout=10 seconds, " +
        "compression=none, " +
        "headers={}}}"

    endpoints.traverse_ { endpoint =>
      val config = Config.ofProps(
        Map("otel.exporter.otlp.endpoint" -> endpoint)
      )

      OtlpLogRecordExporterAutoConfigure[IO]
        .configure(config)
        .use(exporter => IO(assertEquals(exporter.name, expected)))
    }
  }

  test("load from the config - unknown protocol - fail") {
    val config = Config.ofProps(Map("otel.exporter.otlp.protocol" -> "mqtt"))

    OtlpLogRecordExporterAutoConfigure[IO]
      .configure(config)
      .use_
      .attempt
      .map(_.leftMap(_.getMessage))
      .assertEquals(
        Left("""Cannot autoconfigure [OtlpClient].
               |Cause: Unrecognized protocol [mqtt]. Supported options [http/json, http/protobuf, grpc].
               |Config:
               |1) `otel.exporter.otlp.compression` - N/A
               |2) `otel.exporter.otlp.endpoint` - N/A
               |3) `otel.exporter.otlp.headers` - N/A
               |4) `otel.exporter.otlp.logs.compression` - N/A
               |5) `otel.exporter.otlp.logs.endpoint` - N/A
               |6) `otel.exporter.otlp.logs.headers` - N/A
               |7) `otel.exporter.otlp.logs.protocol` - N/A
               |8) `otel.exporter.otlp.logs.timeout` - N/A
               |9) `otel.exporter.otlp.protocol` - mqtt
               |10) `otel.exporter.otlp.timeout` - N/A""".stripMargin)
      )
  }

}
