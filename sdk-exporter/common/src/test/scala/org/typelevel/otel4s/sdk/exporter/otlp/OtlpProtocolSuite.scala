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

package org.typelevel.otel4s.sdk.exporter.otlp

import cats.Show
import munit.ScalaCheckSuite
import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalacheck.Prop

class OtlpProtocolSuite extends ScalaCheckSuite {

  private implicit val protocolArbitrary: Arbitrary[OtlpProtocol] =
    Arbitrary(
      Gen.oneOf(
        OtlpProtocol.grpc,
        OtlpProtocol.httpJson,
        OtlpProtocol.httpProtobuf
      )
    )

  test("Show[OtlpProtocol]") {
    Prop.forAll { (protocol: OtlpProtocol) =>
      val expected = protocol match {
        case OtlpProtocol.Http(HttpPayloadEncoding.Json)     => "http/json"
        case OtlpProtocol.Http(HttpPayloadEncoding.Protobuf) => "http/protobuf"
        case OtlpProtocol.Grpc                               => "grpc"
      }

      assertEquals(Show[OtlpProtocol].show(protocol), expected)
      assertEquals(protocol.toString, expected)
    }
  }

}
