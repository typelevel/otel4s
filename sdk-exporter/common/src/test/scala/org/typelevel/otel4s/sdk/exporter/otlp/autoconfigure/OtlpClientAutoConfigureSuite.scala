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
package otlp
package autoconfigure

import cats.effect.IO
import cats.syntax.either._
import cats.syntax.traverse._
import fs2.io.compression._
import munit.CatsEffectSuite
import org.http4s.Headers
import org.http4s.Uri
import org.http4s.syntax.literals._
import org.typelevel.otel4s.sdk.autoconfigure.Config
import scalapb_circe.Printer

import scala.concurrent.duration._

class OtlpClientAutoConfigureSuite extends CatsEffectSuite {

  import OtlpClientAutoConfigure.Defaults

  private sealed trait Payload

  private implicit val protoEncoder: ProtoEncoder.Message[List[Payload]] =
    _ => ???

  private implicit val jsonPrinter: Printer = new Printer()

  private val tracesDefaults = defaults("traces")
  private val metricsDefaults = defaults("metrics")

  //
  // Traces
  //

  test("traces - empty config - load default") {
    val config = Config.ofProps(Map.empty)

    val expected =
      "OtlpClient{" +
        "protocol=http/protobuf, " +
        "endpoint=http://localhost:4318/v1/traces, " +
        "timeout=10 seconds, " +
        "compression=none, " +
        "headers={}}"

    configureTraces(config).assertEquals(expected)
  }

  test("traces - empty string - load default") {
    val config = Config.ofProps(
      Map(
        "otel.exporter.otlp.compression" -> "",
        "otel.exporter.otlp.endpoint" -> "",
        "otel.exporter.otlp.headers" -> "",
        "otel.exporter.otlp.timeout" -> "",
        "otel.exporter.otlp.traces.compression" -> "",
        "otel.exporter.otlp.traces.endpoint" -> "",
        "otel.exporter.otlp.traces.headers" -> "",
        "otel.exporter.otlp.traces.timeout" -> ""
      )
    )

    val expected =
      "OtlpClient{" +
        "protocol=http/protobuf, " +
        "endpoint=http://localhost:4318/v1/traces, " +
        "timeout=10 seconds, " +
        "compression=none, " +
        "headers={}}"

    configureTraces(config).assertEquals(expected)
  }

  test("traces - use global properties") {
    val config = Config.ofProps(
      Map(
        "otel.exporter.otlp.protocol" -> "grpc",
        "otel.exporter.otlp.compression" -> "gzip",
        "otel.exporter.otlp.endpoint" -> "http://localhost:1234/",
        "otel.exporter.otlp.headers" -> "header1=value1",
        "otel.exporter.otlp.timeout" -> "5 seconds"
      )
    )

    val expected =
      "OtlpClient{" +
        "protocol=grpc, " +
        "endpoint=http://localhost:1234/v1/traces, " +
        "timeout=5 seconds, " +
        "compression=gzip, " +
        "headers={header1: value1}}"

    configureTraces(config).assertEquals(expected)
  }

  test("traces - prioritize 'traces' properties") {
    val config = Config.ofProps(
      Map(
        "otel.exporter.otlp.protocol" -> "grpc",
        "otel.exporter.otlp.compression" -> "",
        "otel.exporter.otlp.endpoint" -> "",
        "otel.exporter.otlp.headers" -> "header1=value1",
        "otel.exporter.otlp.timeout" -> "5 seconds",
        "otel.exporter.otlp.traces.protocol" -> "http/json",
        "otel.exporter.otlp.traces.compression" -> "gzip",
        "otel.exporter.otlp.traces.endpoint" -> "http://localhost:1234/v2/traces",
        "otel.exporter.otlp.traces.headers" -> "header2=value2",
        "otel.exporter.otlp.traces.timeout" -> "15 seconds"
      )
    )

    val expected =
      "OtlpClient{" +
        "protocol=http/json, " +
        "endpoint=http://localhost:1234/v2/traces, " +
        "timeout=15 seconds, " +
        "compression=gzip, " +
        "headers={header2: value2}}"

    configureTraces(config).assertEquals(expected)
  }

  test("traces - invalid property - fail") {
    val input = List(
      "otel.exporter.otlp.protocol" -> "other",
      "otel.exporter.otlp.compression" -> "unknown",
      "otel.exporter.otlp.endpoint" -> "not\\//-a-url",
      "otel.exporter.otlp.headers" -> "header1",
      "otel.exporter.otlp.timeout" -> "5 hertz",
      "otel.exporter.otlp.traces.protocol" -> "other1",
      "otel.exporter.otlp.traces.compression" -> "gzipped",
      "otel.exporter.otlp.traces.endpoint" -> "aa aa",
      "otel.exporter.otlp.traces.headers" -> "not a header",
      "otel.exporter.otlp.traces.timeout" -> "invalid"
    )

    val empty = Map(
      "otel.exporter.otlp.protocol" -> "",
      "otel.exporter.otlp.compression" -> "",
      "otel.exporter.otlp.endpoint" -> "",
      "otel.exporter.otlp.headers" -> "",
      "otel.exporter.otlp.timeout" -> "",
      "otel.exporter.otlp.traces.protocol" -> "",
      "otel.exporter.otlp.traces.compression" -> "",
      "otel.exporter.otlp.traces.endpoint" -> "",
      "otel.exporter.otlp.traces.headers" -> "",
      "otel.exporter.otlp.traces.timeout" -> ""
    )

    val errors = Map(
      "otel.exporter.otlp.protocol" -> "Unrecognized protocol [other]. Supported options [http/json, http/protobuf, grpc]",
      "otel.exporter.otlp.compression" -> "Unrecognized compression [unknown]. Supported options [gzip, none]",
      "otel.exporter.otlp.endpoint" -> Uri
        .fromString("not\\//-a-url")
        .fold(_.message, _ => ""),
      "otel.exporter.otlp.headers" -> "Invalid map property [header1]",
      "otel.exporter.otlp.timeout" -> "Invalid value for property otel.exporter.otlp.timeout=5 hertz. Must be [FiniteDuration]",
      "otel.exporter.otlp.traces.protocol" -> "Unrecognized protocol [other1]. Supported options [http/json, http/protobuf, grpc]",
      "otel.exporter.otlp.traces.compression" -> "Unrecognized compression [gzipped]. Supported options [gzip, none]",
      "otel.exporter.otlp.traces.endpoint" -> Uri
        .fromString("aa aa")
        .fold(_.message, _ => ""),
      "otel.exporter.otlp.traces.headers" -> "Invalid map property [not a header]",
      "otel.exporter.otlp.traces.timeout" -> "Invalid value for property otel.exporter.otlp.traces.timeout=invalid. Must be [FiniteDuration]"
    )

    input.traverse { case (key, value) =>
      val properties = empty.updated(key, value)
      val config = Config.ofProps(properties)
      val cause = errors(key)

      val prettyConfig = properties.toSeq.sorted.zipWithIndex
        .map { case ((k, v), i) =>
          val idx = i + 1
          val value = if (v.isEmpty) "N/A" else v
          s"$idx) `$k` - $value"
        }
        .mkString("\n")

      val expected =
        s"Cannot autoconfigure [OtlpClient].\nCause: $cause.\nConfig:\n$prettyConfig"

      OtlpClientAutoConfigure
        .traces[IO, Payload](tracesDefaults, None)
        .configure(config)
        .use_
        .attempt
        .map(_.leftMap(_.getMessage))
        .assertEquals(Left(expected))
    }
  }

  //
  // Metrics
  //

  test("metrics - empty config - load default") {
    val config = Config.ofProps(Map.empty)

    val expected =
      "OtlpClient{" +
        "protocol=http/protobuf, " +
        "endpoint=http://localhost:4318/v1/metrics, " +
        "timeout=10 seconds, " +
        "compression=none, " +
        "headers={}}"

    configureMetrics(config).assertEquals(expected)
  }

  test("metrics - empty string - load default") {
    val config = Config.ofProps(
      Map(
        "otel.exporter.otlp.compression" -> "",
        "otel.exporter.otlp.endpoint" -> "",
        "otel.exporter.otlp.headers" -> "",
        "otel.exporter.otlp.timeout" -> "",
        "otel.exporter.otlp.metrics.compression" -> "",
        "otel.exporter.otlp.metrics.endpoint" -> "",
        "otel.exporter.otlp.metrics.headers" -> "",
        "otel.exporter.otlp.metrics.timeout" -> ""
      )
    )

    val expected =
      "OtlpClient{" +
        "protocol=http/protobuf, " +
        "endpoint=http://localhost:4318/v1/metrics, " +
        "timeout=10 seconds, " +
        "compression=none, " +
        "headers={}}"

    configureMetrics(config).assertEquals(expected)
  }

  test("metrics - use global properties") {
    val config = Config.ofProps(
      Map(
        "otel.exporter.otlp.protocol" -> "grpc",
        "otel.exporter.otlp.compression" -> "gzip",
        "otel.exporter.otlp.endpoint" -> "http://localhost:1234/",
        "otel.exporter.otlp.headers" -> "header1=value1",
        "otel.exporter.otlp.timeout" -> "5 seconds"
      )
    )

    val expected =
      "OtlpClient{" +
        "protocol=grpc, " +
        "endpoint=http://localhost:1234/v1/metrics, " +
        "timeout=5 seconds, " +
        "compression=gzip, " +
        "headers={header1: value1}}"

    configureMetrics(config).assertEquals(expected)
  }

  test("metrics - prioritize 'metrics' properties") {
    val config = Config.ofProps(
      Map(
        "otel.exporter.otlp.protocol" -> "grpc",
        "otel.exporter.otlp.compression" -> "",
        "otel.exporter.otlp.endpoint" -> "",
        "otel.exporter.otlp.headers" -> "header1=value1",
        "otel.exporter.otlp.timeout" -> "5 seconds",
        "otel.exporter.otlp.metrics.protocol" -> "http/json",
        "otel.exporter.otlp.metrics.compression" -> "gzip",
        "otel.exporter.otlp.metrics.endpoint" -> "http://localhost:1234/v2/metrics",
        "otel.exporter.otlp.metrics.headers" -> "header2=value2",
        "otel.exporter.otlp.metrics.timeout" -> "15 seconds"
      )
    )

    val expected =
      "OtlpClient{" +
        "protocol=http/json, " +
        "endpoint=http://localhost:1234/v2/metrics, " +
        "timeout=15 seconds, " +
        "compression=gzip, " +
        "headers={header2: value2}}"

    configureMetrics(config).assertEquals(expected)
  }

  test("metrics - invalid property - fail") {
    val input = List(
      "otel.exporter.otlp.protocol" -> "other",
      "otel.exporter.otlp.compression" -> "unknown",
      "otel.exporter.otlp.endpoint" -> "not\\//-a-url",
      "otel.exporter.otlp.headers" -> "header1",
      "otel.exporter.otlp.timeout" -> "5 hertz",
      "otel.exporter.otlp.metrics.protocol" -> "other1",
      "otel.exporter.otlp.metrics.compression" -> "gzipped",
      "otel.exporter.otlp.metrics.endpoint" -> "aa aa",
      "otel.exporter.otlp.metrics.headers" -> "not a header",
      "otel.exporter.otlp.metrics.timeout" -> "invalid"
    )

    val empty = Map(
      "otel.exporter.otlp.protocol" -> "",
      "otel.exporter.otlp.compression" -> "",
      "otel.exporter.otlp.endpoint" -> "",
      "otel.exporter.otlp.headers" -> "",
      "otel.exporter.otlp.timeout" -> "",
      "otel.exporter.otlp.metrics.protocol" -> "",
      "otel.exporter.otlp.metrics.compression" -> "",
      "otel.exporter.otlp.metrics.endpoint" -> "",
      "otel.exporter.otlp.metrics.headers" -> "",
      "otel.exporter.otlp.metrics.timeout" -> ""
    )

    val errors = Map(
      "otel.exporter.otlp.protocol" -> "Unrecognized protocol [other]. Supported options [http/json, http/protobuf, grpc]",
      "otel.exporter.otlp.compression" -> "Unrecognized compression [unknown]. Supported options [gzip, none]",
      "otel.exporter.otlp.endpoint" -> Uri
        .fromString("not\\//-a-url")
        .fold(_.message, _ => ""),
      "otel.exporter.otlp.headers" -> "Invalid map property [header1]",
      "otel.exporter.otlp.timeout" -> "Invalid value for property otel.exporter.otlp.timeout=5 hertz. Must be [FiniteDuration]",
      "otel.exporter.otlp.metrics.protocol" -> "Unrecognized protocol [other1]. Supported options [http/json, http/protobuf, grpc]",
      "otel.exporter.otlp.metrics.compression" -> "Unrecognized compression [gzipped]. Supported options [gzip, none]",
      "otel.exporter.otlp.metrics.endpoint" -> Uri
        .fromString("aa aa")
        .fold(_.message, _ => ""),
      "otel.exporter.otlp.metrics.headers" -> "Invalid map property [not a header]",
      "otel.exporter.otlp.metrics.timeout" -> "Invalid value for property otel.exporter.otlp.metrics.timeout=invalid. Must be [FiniteDuration]"
    )

    input.traverse { case (key, value) =>
      val properties = empty.updated(key, value)
      val config = Config.ofProps(properties)
      val cause = errors(key)

      val prettyConfig = properties.toSeq.sorted.zipWithIndex
        .map { case ((k, v), i) =>
          val idx = i + 1
          val value = if (v.isEmpty) "N/A" else v
          s"$idx) `$k` - $value"
        }
        .mkString("\n")

      val expected =
        s"Cannot autoconfigure [OtlpClient].\nCause: $cause.\nConfig:\n$prettyConfig"

      OtlpClientAutoConfigure
        .metrics[IO, Payload](metricsDefaults, None)
        .configure(config)
        .use_
        .attempt
        .map(_.leftMap(_.getMessage))
        .assertEquals(Left(expected))
    }
  }

  private def configureMetrics(config: Config): IO[String] =
    OtlpClientAutoConfigure
      .metrics[IO, Payload](metricsDefaults, None)
      .configure(config)
      .use(client => IO.pure(client.toString))

  private def configureTraces(config: Config): IO[String] =
    OtlpClientAutoConfigure
      .traces[IO, Payload](tracesDefaults, None)
      .configure(config)
      .use(client => IO.pure(client.toString))

  private def defaults(path: String) =
    Defaults(
      protocol = OtlpProtocol.httpProtobuf,
      endpoint = uri"http://localhost:4318" / "v1" / path,
      apiPath = s"v1/$path",
      headers = Headers.empty,
      timeout = 10.seconds,
      compression = PayloadCompression.none
    )

}
