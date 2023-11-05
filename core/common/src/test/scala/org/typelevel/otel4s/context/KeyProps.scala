/*
 * Copyright 2022 Typelevel
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

package org.typelevel.otel4s.context

import cats.syntax.show._
import munit.ScalaCheckSuite
import org.scalacheck.Arbitrary
import org.scalacheck.Prop.forAll
import org.typelevel.otel4s.context.vault.VaultContext

class KeyProps extends ScalaCheckSuite with KeyInstances {
  property("Show[Key[String]]") {
    forAll(Arbitrary.arbitrary[Key[String]]) { key =>
      assert(key.show.contains(key.name))
    }
  }

  property("Show[VaultContext.Key[String]]") {
    forAll(Arbitrary.arbitrary[VaultContext.Key[String]]) { key =>
      assert(key.show.contains(key.name))
    }
  }

  property("Key#toString") {
    forAll(Arbitrary.arbitrary[Key[String]]) { key =>
      assert(key.toString.contains(key.name))
    }
  }

  property("VaultContext.Key#toString") {
    forAll(Arbitrary.arbitrary[VaultContext.Key[String]]) { key =>
      assert(key.toString.contains(key.name))
    }
  }
}
