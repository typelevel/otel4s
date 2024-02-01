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

class TelemetryResourceAutoConfigureSuite extends CatsEffectSuite {

  test("load from an empty config") {
    val config = Config(Map.empty, Map.empty, Map.empty)
    TelemetryResourceAutoConfigure[IO].configure(config).use { resource =>
      IO(assertEquals(resource, TelemetryResource.empty))
    }
  }

  test("load from the config") {
    val props = Map(
      "otel.service.name" -> "some-service",
      "otel.resource.attributes" -> "key1=val1,key2=val2,key3=val3",
      "otel.experimental.resource.disabled-keys" -> "key1,val3,test,key3"
    )

    val config = Config(props, Map.empty, Map.empty)

    val expectedAttributes = Attributes(
      Attribute("key2", "val2"),
      Attribute("service.name", "some-service")
    )

    TelemetryResourceAutoConfigure[IO].configure(config).use { resource =>
      IO(assertEquals(resource, TelemetryResource(expectedAttributes)))
    }
  }
}
