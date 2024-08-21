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

package org.typelevel.otel4s
package sdk
package autoconfigure

import cats.effect.IO
import munit.CatsEffectSuite
import munit.internal.PlatformCompat
import org.typelevel.otel4s.sdk.resource.TelemetryResourceDetector

class TelemetryResourceAutoConfigureSuite extends CatsEffectSuite {

  test("load from an empty config - use default as a fallback") {
    val props = Map("otel.otel4s.resource.detectors" -> "none")
    val config = Config.ofProps(props)

    TelemetryResourceAutoConfigure[IO](Set.empty)
      .configure(config)
      .use { resource =>
        IO(assertEquals(resource, TelemetryResource.default))
      }
  }

  test("load from the config - use default as an initial resource") {
    val props = Map(
      "otel.service.name" -> "some-service",
      "otel.resource.attributes" -> "key1=val1,key2=val2,key3=val3",
      "otel.experimental.resource.disabled-keys" -> "key1,val3,test,key3",
      "otel.otel4s.resource.detectors" -> "none"
    )

    val config = Config.ofProps(props)

    val expectedAttributes = Attributes(
      Attribute("key2", "val2"),
      Attribute("service.name", "some-service")
    )

    val expected = TelemetryResource.default.mergeUnsafe(
      TelemetryResource(expectedAttributes)
    )

    TelemetryResourceAutoConfigure[IO](Set.empty)
      .configure(config)
      .use { resource =>
        IO(assertEquals(resource, expected))
      }
  }

  test("use default detectors") {
    val config = Config(Map.empty, Map.empty, Map.empty)
    TelemetryResourceAutoConfigure[IO](Set.empty)
      .configure(config)
      .use { resource =>
        val service = Set("service.name")
        val host = Set("host.arch", "host.name")
        val os = Set("os.type", "os.description")

        val runtime = {
          val name = "process.runtime.name"
          val version = "process.runtime.version"
          val description = "process.runtime.description"

          if (PlatformCompat.isNative) Set(name, description)
          else Set(name, version, description)
        }

        val telemetry = Set(
          "telemetry.sdk.language",
          "telemetry.sdk.name",
          "telemetry.sdk.version"
        )

        val all =
          host ++ os ++ runtime ++ service ++ telemetry

        IO(assertEquals(resource.attributes.map(_.key.name).toSet, all))
      }
  }

  test("use extra detectors") {
    val props = Map("otel.otel4s.resource.detectors" -> "custom")
    val config = Config(props, Map.empty, Map.empty)

    val customResource =
      TelemetryResource(Attributes(Attribute("custom", true)))

    val detector = new TelemetryResourceDetector[IO] {
      def name: String = "custom"
      def detect: IO[Option[TelemetryResource]] = IO.pure(Some(customResource))
    }

    TelemetryResourceAutoConfigure[IO](Set(detector))
      .configure(config)
      .use { resource =>
        IO(
          assertEquals(
            resource,
            TelemetryResource.default.mergeUnsafe(customResource)
          )
        )
      }
  }
}
