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

package org.typelevel.otel4s.sdk.exporter.autoconfigure

import cats.effect.IO
import cats.syntax.either._
import munit.CatsEffectSuite
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.exporter.otlp.HttpPayloadEncoding
import org.typelevel.otel4s.sdk.exporter.otlp.Protocol
import org.typelevel.otel4s.sdk.exporter.otlp.autoconfigure.ProtocolAutoConfigure

class ProtocolAutoConfigureSuite extends CatsEffectSuite {

  test("metrics - load from the config - empty config - load default") {
    val config = Config.ofProps(Map.empty)
    val expected = Protocol.Http(HttpPayloadEncoding.Protobuf)

    ProtocolAutoConfigure
      .metrics[IO]
      .configure(config)
      .use(protocol => IO(assertEquals(protocol, expected)))
  }

  test("metrics - load from the config - empty string - load default") {
    val config = Config.ofProps(
      Map(
        "otel.exporter.otlp.protocol" -> "",
        "otel.exporter.otlp.metrics.protocol" -> ""
      )
    )

    val expected = Protocol.Http(HttpPayloadEncoding.Protobuf)

    ProtocolAutoConfigure
      .metrics[IO]
      .configure(config)
      .use(protocol => IO(assertEquals(protocol, expected)))
  }

  test("metrics - load from the config - prioritize 'metrics' properties") {
    val config = Config.ofProps(
      Map(
        "otel.exporter.otlp.protocol" -> "http/protobuf",
        "otel.exporter.otlp.metrics.protocol" -> "http/json"
      )
    )

    val expected = Protocol.Http(HttpPayloadEncoding.Json)

    ProtocolAutoConfigure
      .metrics[IO]
      .configure(config)
      .use(protocol => IO(assertEquals(protocol, expected)))
  }

  test("metrics - load from the config - unknown protocol - fail") {
    val config = Config.ofProps(Map("otel.exporter.otlp.protocol" -> "grpc"))

    ProtocolAutoConfigure
      .metrics[IO]
      .configure(config)
      .use_
      .attempt
      .map(_.leftMap(_.getMessage))
      .assertEquals(
        Left("""Cannot autoconfigure [Protocol].
               |Cause: Unrecognized protocol [grpc]. Supported options [http/json, http/protobuf].
               |Config:
               |1) `otel.exporter.otlp.metrics.protocol` - N/A
               |2) `otel.exporter.otlp.protocol` - grpc""".stripMargin)
      )
  }

  test("traces - load from the config - empty config - load default") {
    val config = Config.ofProps(Map.empty)
    val expected = Protocol.Http(HttpPayloadEncoding.Protobuf)

    ProtocolAutoConfigure
      .traces[IO]
      .configure(config)
      .use(protocol => IO(assertEquals(protocol, expected)))
  }

  test("traces - load from the config - empty string - load default") {
    val config = Config.ofProps(
      Map(
        "otel.exporter.otlp.protocol" -> "",
        "otel.exporter.otlp.traces.protocol" -> ""
      )
    )

    val expected = Protocol.Http(HttpPayloadEncoding.Protobuf)

    ProtocolAutoConfigure
      .traces[IO]
      .configure(config)
      .use(protocol => IO(assertEquals(protocol, expected)))
  }

  test("traces - load from the config - prioritize 'traces' properties") {
    val config = Config.ofProps(
      Map(
        "otel.exporter.otlp.protocol" -> "http/protobuf",
        "otel.exporter.otlp.traces.protocol" -> "http/json"
      )
    )

    val expected = Protocol.Http(HttpPayloadEncoding.Json)

    ProtocolAutoConfigure
      .traces[IO]
      .configure(config)
      .use(protocol => IO(assertEquals(protocol, expected)))
  }

  test("traces - load from the config - unknown protocol - fail") {
    val config = Config.ofProps(Map("otel.exporter.otlp.protocol" -> "grpc"))

    ProtocolAutoConfigure
      .traces[IO]
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
