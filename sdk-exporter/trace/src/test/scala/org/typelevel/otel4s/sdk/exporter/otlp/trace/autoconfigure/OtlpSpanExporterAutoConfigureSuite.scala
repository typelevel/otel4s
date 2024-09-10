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
package otlp.trace.autoconfigure

import cats.effect.IO
import cats.syntax.either._
import cats.syntax.foldable._
import munit.CatsEffectSuite
import org.typelevel.otel4s.sdk.autoconfigure.Config

class OtlpSpanExporterAutoConfigureSuite extends CatsEffectSuite with SuiteRuntimePlatform {

  test("load from the config - empty config - load default") {
    val config = Config.ofProps(Map.empty)

    val expected =
      "OtlpHttpSpanExporter{client=OtlpHttpClient{" +
        "encoding=Protobuf, " +
        "endpoint=http://localhost:4318/v1/traces, " +
        "timeout=10 seconds, " +
        "gzipCompression=false, " +
        "headers={}}}"

    OtlpSpanExporterAutoConfigure[IO]
      .configure(config)
      .use(exporter => IO(assertEquals(exporter.name, expected)))
  }

  test("load from the config - empty string - load default") {
    val config = Config.ofProps(
      Map(
        "otel.exporter.otlp.protocol" -> "",
        "otel.exporter.otlp.traces.protocol" -> ""
      )
    )

    val expected =
      "OtlpHttpSpanExporter{client=OtlpHttpClient{" +
        "encoding=Protobuf, " +
        "endpoint=http://localhost:4318/v1/traces, " +
        "timeout=10 seconds, " +
        "gzipCompression=false, " +
        "headers={}}}"

    OtlpSpanExporterAutoConfigure[IO]
      .configure(config)
      .use(exporter => IO(assertEquals(exporter.name, expected)))
  }

  test("load from the config - prioritize 'traces' properties") {
    val config = Config.ofProps(
      Map(
        "otel.exporter.otlp.protocol" -> "http/protobuf",
        "otel.exporter.otlp.traces.protocol" -> "http/json"
      )
    )

    val expected =
      "OtlpHttpSpanExporter{client=OtlpHttpClient{" +
        "encoding=Json, " +
        "endpoint=http://localhost:4318/v1/traces, " +
        "timeout=10 seconds, " +
        "gzipCompression=false, " +
        "headers={}}}"

    OtlpSpanExporterAutoConfigure[IO]
      .configure(config)
      .use(exporter => IO(assertEquals(exporter.name, expected)))
  }

  test("load from the config - generate a valid endpoint") {
    val endpoints = List(
      "http://localhost:4318",
      "http://localhost:4318/"
    )

    val expected =
      "OtlpHttpSpanExporter{client=OtlpHttpClient{" +
        "encoding=Protobuf, " +
        "endpoint=http://localhost:4318/v1/traces, " +
        "timeout=10 seconds, " +
        "gzipCompression=false, " +
        "headers={}}}"

    endpoints.traverse_ { endpoint =>
      val config = Config.ofProps(
        Map("otel.exporter.otlp.endpoint" -> endpoint)
      )

      OtlpSpanExporterAutoConfigure[IO]
        .configure(config)
        .use(exporter => IO(assertEquals(exporter.name, expected)))
    }
  }

  test("load from the config - unknown protocol - fail") {
    val config = Config.ofProps(Map("otel.exporter.otlp.protocol" -> "grpc"))

    OtlpSpanExporterAutoConfigure[IO]
      .configure(config)
      .use_
      .attempt
      .map(_.leftMap(_.getMessage))
      .assertEquals(
        Left("""Cannot autoconfigure [Protocol].
               |Cause: Unrecognized protocol [grpc]. Supported options [http/json, http/protobuf].
               |Config:
               |1) `otel.exporter.otlp.protocol` - grpc
               |2) `otel.exporter.otlp.traces.protocol` - N/A""".stripMargin)
      )
  }

}
