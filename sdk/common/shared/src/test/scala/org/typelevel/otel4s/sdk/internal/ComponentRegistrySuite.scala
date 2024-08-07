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

package org.typelevel.otel4s.sdk.internal

import cats.effect.IO
import munit.CatsEffectSuite
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes

class ComponentRegistrySuite extends CatsEffectSuite {

  private val name = "component"
  private val version = "0.0.1"
  private val schemaUrl = "https://otel4s.schema.com"
  private val attributes = Attributes(Attribute("key", "value"))

  registryTest("get cached values (by name only)") { registry =>
    for {
      v1 <- registry.get(name, None, None, Attributes.empty)
      v2 <- registry.get(name, None, None, attributes)
      v3 <- registry.get(name, Some(version), None, attributes)
      v4 <- registry.get(name, Some(version), Some(schemaUrl), attributes)
    } yield {
      assertEquals(v1, v2)
      assertNotEquals(v1, v3)
      assertNotEquals(v2, v3)
      assertNotEquals(v1, v4)
      assertNotEquals(v2, v4)
    }
  }

  registryTest("get cached values (by name and version)") { registry =>
    for {
      v1 <- registry.get(name, Some(version), None, Attributes.empty)
      v2 <- registry.get(name, Some(version), None, attributes)
      v3 <- registry.get(name, Some(version), Some(schemaUrl), attributes)
    } yield {
      assertEquals(v1, v2)
      assertNotEquals(v1, v3)
      assertNotEquals(v2, v3)
    }
  }

  registryTest("get cached values (by name, version, and schema)") { registry =>
    for {
      v1 <- registry.get(name, Some(version), Some(schemaUrl), Attributes.empty)
      v2 <- registry.get(name, Some(version), Some(schemaUrl), attributes)
    } yield assertEquals(v1, v2)
  }

  private def registryTest(
      name: String
  )(body: ComponentRegistry[IO, TestComponent] => IO[Unit]): Unit =
    test(name) {
      for {
        registry <- ComponentRegistry.create(_ => IO.pure(new TestComponent()))
        _ <- body(registry)
      } yield ()
    }

  private class TestComponent

}
