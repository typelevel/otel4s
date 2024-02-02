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

package org.typelevel.otel4s.sdk.autoconfigure

import cats.effect.IO
import cats.effect.Resource
import cats.syntax.either._
import munit.CatsEffectSuite

import scala.util.control.NoStackTrace

class AutoConfigureSuite extends CatsEffectSuite {

  test("use hint in the error, ignore empty set of keys") {
    val config = Config.ofProps(Map.empty)

    val io =
      alwaysFailing("Component", Set.empty).configure(config).use_.attempt

    io.map(_.leftMap(_.getMessage))
      .assertEquals(Left(AutoConfigureError("Component", Err).getMessage))
  }

  test("use hint in the error, add keys to the error message") {
    val config = Config.ofProps(Map.empty)
    val keys: Set[Config.Key[_]] = Set(
      Config.Key[String]("a"),
      Config.Key[Set[String]]("b"),
      Config.Key[Map[String, String]]("c"),
      Config.Key[Double]("d")
    )

    val io = alwaysFailing("Component", keys).configure(config).use_.attempt

    io.map(_.leftMap(_.getMessage))
      .assertEquals(
        Left(AutoConfigureError("Component", Err, keys, config).getMessage)
      )
  }

  private def alwaysFailing(
      hint: String,
      configKeys: Set[Config.Key[_]]
  ): AutoConfigure.WithHint[IO, String] = {
    new AutoConfigure.WithHint[IO, String](hint, configKeys) {
      protected def fromConfig(config: Config): Resource[IO, String] =
        Resource.raiseError(Err: Throwable)
    }
  }

  private object Err
      extends RuntimeException("Something went wrong")
      with NoStackTrace

}
