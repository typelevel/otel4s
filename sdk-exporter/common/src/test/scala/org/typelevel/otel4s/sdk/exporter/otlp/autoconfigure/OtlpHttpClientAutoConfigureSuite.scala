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
package otlp.autoconfigure

import cats.effect.IO
import cats.syntax.either._
import cats.syntax.traverse._
import munit.CatsEffectSuite
import org.http4s.Headers
import org.http4s.Uri
import org.http4s.syntax.literals._
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.exporter.otlp.HttpPayloadEncoding
import org.typelevel.otel4s.sdk.exporter.otlp.ProtoEncoder
import scalapb_circe.Printer

import scala.concurrent.duration._

class OtlpHttpClientAutoConfigureSuite
    extends CatsEffectSuite
    with SuiteRuntimePlatform {

  import OtlpHttpClientAutoConfigure.Defaults

  private sealed trait Payload

  private implicit val protoEncoder: ProtoEncoder.Message[List[Payload]] =
    _ => ???

  private implicit val jsonPrinter: Printer = new Printer()

  private val tracesDefaults = Defaults(
    uri"http://localhost:4318/v1/traces",
    "v1/traces",
    Headers.empty,
    10.seconds,
    HttpPayloadEncoding.Protobuf
  )

  test("traces - empty config - load default") {
    val config = Config.ofProps(Map.empty)

    val expected =
      "OtlpHttpClient{" +
        "encoding=Protobuf, " +
        "endpoint=http://localhost:4318/v1/traces, " +
        "timeout=10 seconds, " +
        "gzipCompression=false, " +
        "headers={}}"

    OtlpHttpClientAutoConfigure
      .traces[IO, Payload](tracesDefaults)
      .configure(config)
      .use(client => IO(assertEquals(client.toString, expected)))
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
      "OtlpHttpClient{" +
        "encoding=Protobuf, " +
        "endpoint=http://localhost:4318/v1/traces, " +
        "timeout=10 seconds, " +
        "gzipCompression=false, " +
        "headers={}}"

    OtlpHttpClientAutoConfigure
      .traces[IO, Payload](tracesDefaults)
      .configure(config)
      .use(client => IO(assertEquals(client.toString, expected)))
  }

  test("traces - use global properties") {
    val config = Config.ofProps(
      Map(
        "otel.exporter.otlp.compression" -> "gzip",
        "otel.exporter.otlp.endpoint" -> "http://localhost:1234/",
        "otel.exporter.otlp.headers" -> "header1=value1",
        "otel.exporter.otlp.timeout" -> "5 seconds"
      )
    )

    val expected =
      "OtlpHttpClient{" +
        "encoding=Protobuf, " +
        "endpoint=http://localhost:1234/v1/traces, " +
        "timeout=5 seconds, " +
        "gzipCompression=true, " +
        "headers={header1: value1}}"

    OtlpHttpClientAutoConfigure
      .traces[IO, Payload](tracesDefaults)
      .configure(config)
      .use(client => IO(assertEquals(client.toString, expected)))
  }

  test("traces - prioritize 'traces' properties") {
    val config = Config.ofProps(
      Map(
        "otel.exporter.otlp.compression" -> "",
        "otel.exporter.otlp.endpoint" -> "",
        "otel.exporter.otlp.headers" -> "header1=value1",
        "otel.exporter.otlp.timeout" -> "5 seconds",
        "otel.exporter.otlp.traces.compression" -> "gzip",
        "otel.exporter.otlp.traces.endpoint" -> "http://localhost:1234/v2/traces",
        "otel.exporter.otlp.traces.headers" -> "header2=value2",
        "otel.exporter.otlp.traces.timeout" -> "15 seconds"
      )
    )

    val expected =
      "OtlpHttpClient{" +
        "encoding=Protobuf, " +
        "endpoint=http://localhost:1234/v2/traces, " +
        "timeout=15 seconds, " +
        "gzipCompression=true, " +
        "headers={header2: value2}}"

    OtlpHttpClientAutoConfigure
      .traces[IO, Payload](tracesDefaults)
      .configure(config)
      .use(client => IO(assertEquals(client.toString, expected)))
  }

  test("traces - invalid property - fail") {
    val input = List(
      "otel.exporter.otlp.compression" -> "unknown",
      "otel.exporter.otlp.endpoint" -> "not\\//-a-url",
      "otel.exporter.otlp.headers" -> "header1",
      "otel.exporter.otlp.timeout" -> "5 hertz",
      "otel.exporter.otlp.traces.compression" -> "gzipped",
      "otel.exporter.otlp.traces.endpoint" -> "aa aa",
      "otel.exporter.otlp.traces.headers" -> "not a header",
      "otel.exporter.otlp.traces.timeout" -> "invalid"
    )

    val empty = Map(
      "otel.exporter.otlp.compression" -> "",
      "otel.exporter.otlp.endpoint" -> "",
      "otel.exporter.otlp.headers" -> "",
      "otel.exporter.otlp.timeout" -> "",
      "otel.exporter.otlp.traces.compression" -> "",
      "otel.exporter.otlp.traces.endpoint" -> "",
      "otel.exporter.otlp.traces.headers" -> "",
      "otel.exporter.otlp.traces.timeout" -> ""
    )

    val errors = Map(
      "otel.exporter.otlp.compression" -> "Unrecognized compression. Supported options [gzip]",
      "otel.exporter.otlp.endpoint" -> Uri
        .fromString("not\\//-a-url")
        .fold(_.message, _ => ""),
      "otel.exporter.otlp.headers" -> "Invalid map property [header1]",
      "otel.exporter.otlp.timeout" -> "Invalid value for property otel.exporter.otlp.timeout=5 hertz. Must be [FiniteDuration]",
      "otel.exporter.otlp.traces.compression" -> "Unrecognized compression. Supported options [gzip]",
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
        s"Cannot autoconfigure [OtlpHttpClient].\nCause: $cause.\nConfig:\n$prettyConfig"

      OtlpHttpClientAutoConfigure
        .traces[IO, Payload](tracesDefaults)
        .configure(config)
        .use_
        .attempt
        .map(_.leftMap(_.getMessage))
        .assertEquals(Left(expected))
    }
  }

  test("traces - unknown protocol - fail") {
    val config =
      Config.ofProps(Map("otel.exporter.otlp.headers" -> "non-header"))

    OtlpHttpClientAutoConfigure
      .traces[IO, Payload](tracesDefaults)
      .configure(config)
      .use_
      .attempt
      .map(_.leftMap(_.getMessage))
      .assertEquals(
        Left("""Cannot autoconfigure [OtlpHttpClient].
               |Cause: Invalid map property [non-header].
               |Config:
               |1) `otel.exporter.otlp.compression` - N/A
               |2) `otel.exporter.otlp.endpoint` - N/A
               |3) `otel.exporter.otlp.headers` - non-header
               |4) `otel.exporter.otlp.timeout` - N/A
               |5) `otel.exporter.otlp.traces.compression` - N/A
               |6) `otel.exporter.otlp.traces.endpoint` - N/A
               |7) `otel.exporter.otlp.traces.headers` - N/A
               |8) `otel.exporter.otlp.traces.timeout` - N/A""".stripMargin)
      )
  }

}
