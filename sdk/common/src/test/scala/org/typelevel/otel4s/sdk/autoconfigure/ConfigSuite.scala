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

import cats.syntax.either._
import munit.FunSuite

import scala.concurrent.duration._

class ConfigSuite extends FunSuite {

  private val properties = Map(
    "test.string" -> "str",
    "test.int" -> "10",
    "test.double" -> "5.4",
    "test.boolean" -> "true",
    "test.list" -> "a,b,c",
    "test.map" -> "a=1,b=2,c=3,d=",
    "test.duration" -> "1s",
  )

  test("get - using dots") {
    val config = makeConfig(properties)

    assertEquals(config.get[String]("test.string"), Right(Some("str")))
    assertEquals(config.get[Int]("test.int"), Right(Some(10)))
    assertEquals(config.get[Double]("test.double"), Right(Some(5.4)))
    assertEquals(config.get[Boolean]("test.boolean"), Right(Some(true)))
    assertEquals(
      config.get[List[String]]("test.list"),
      Right(Some(List("a", "b", "c")))
    )
    assertEquals(
      config.get[Set[String]]("test.list"),
      Right(Some(Set("a", "b", "c")))
    )
    assertEquals(
      config.get[Map[String, String]]("test.map"),
      Right(Some(Map("a" -> "1", "b" -> "2", "c" -> "3", "d" -> "")))
    )
    assertEquals(
      config.get[FiniteDuration]("test.duration"),
      Right(Some(1.second))
    )
  }

  test("get - using hyphens") {
    val config = makeConfig(properties)

    assertEquals(config.get[String]("test-string"), Right(Some("str")))
    assertEquals(config.get[Int]("test-int"), Right(Some(10)))
    assertEquals(config.get[Double]("test-double"), Right(Some(5.4)))
    assertEquals(config.get[Boolean]("test-boolean"), Right(Some(true)))
    assertEquals(
      config.get[List[String]]("test-list"),
      Right(Some(List("a", "b", "c")))
    )
    assertEquals(
      config.get[Set[String]]("test-list"),
      Right(Some(Set("a", "b", "c")))
    )
    assertEquals(
      config.get[Map[String, String]]("test-map"),
      Right(Some(Map("a" -> "1", "b" -> "2", "c" -> "3", "d" -> "")))
    )
    assertEquals(
      config.get[FiniteDuration]("test-duration"),
      Right(Some(1.second))
    )
  }

  test("get - missing entries") {
    val config = makeConfig(Map.empty)

    assertEquals(config.get[String]("test.string"), Right(None))
    assertEquals(config.get[Int]("test.int"), Right(None))
    assertEquals(config.get[Double]("test.double"), Right(None))
    assertEquals(config.get[Boolean]("test.boolean"), Right(None))
    assertEquals(config.get[List[String]]("test.list"), Right(None))
    assertEquals(config.get[Map[String, String]]("test.map"), Right(None))
    assertEquals(config.get[FiniteDuration]("test.duration"), Right(None))
  }

  test("get - empty values") {
    val config = makeConfig(properties.map { case (k, _) => (k, "") })

    assertEquals(config.get[String]("test.string"), Right(None))
    assertEquals(config.get[Int]("test.int"), Right(None))
    assertEquals(config.get[Double]("test.double"), Right(None))
    assertEquals(config.get[Boolean]("test.boolean"), Right(None))
    assertEquals(config.get[List[String]]("test.list"), Right(None))
    assertEquals(config.get[Map[String, String]]("test.map"), Right(None))
    assertEquals(config.get[FiniteDuration]("test.duration"), Right(None))
  }

  test("get - non-trivial string list") {
    val config = makeConfig(Map("key" -> "   a,  b,c   ,d,, e,, d ,  ,"))

    assertEquals(
      config.get[List[String]]("key"),
      Right(Some(List("a", "b", "c", "d", "e", "d")))
    )
  }

  test("get - non-trivial string set") {
    val config = makeConfig(Map("key" -> "   a,  b,c   ,d,, e,, f ,  ,"))

    assertEquals(
      config.get[Set[String]]("key"),
      Right(Some(Set("a", "b", "c", "d", "e", "f")))
    )
  }

  test("get - non-trivial string map") {
    val config = makeConfig(Map("key" -> "   a=1,  b=2,c =  3   ,   d= 4,, ,"))

    assertEquals(
      config.get[Map[String, String]]("key"),
      Right(Some(Map("a" -> "1", "b" -> "2", "c" -> "3", "d" -> "4")))
    )
  }

  test("get - invalid int") {
    assertEquals(
      makeConfig(Map("key" -> "value")).get[Int]("key").leftMap(_.getMessage),
      Left("Invalid value for property key=value. Must be [Int]")
    )

    assertEquals(
      makeConfig(Map("key" -> Long.MaxValue.toString))
        .get[Int]("key")
        .leftMap(_.getMessage),
      Left("Invalid value for property key=9223372036854775807. Must be [Int]")
    )
  }

  test("get - invalid double") {
    val config = makeConfig(Map("key" -> "value"))

    assertEquals(
      config.get[Double]("key").leftMap(_.getMessage),
      Left("Invalid value for property key=value. Must be [Double]")
    )
  }

  test("get - invalid boolean") {
    val config = makeConfig(Map("key" -> "value"))

    assertEquals(
      config.get[Boolean]("key").leftMap(_.getMessage),
      Left("Invalid value for property key=value. Must be [Boolean]")
    )
  }

  test("get - invalid string map") {
    val config = makeConfig(Map("key" -> "a=1,b"))

    assertEquals(
      config.get[Map[String, String]]("key").leftMap(_.getMessage),
      Left("Invalid map property [b]")
    )
  }

  test("get - invalid string set (duplicates)") {
    val config = makeConfig(Map("key" -> "   a,  b,c   ,d,, b,, e ,  c,"))

    assertEquals(
      config.get[Set[String]]("key").leftMap(_.getMessage),
      Left("The string set contains duplicates: [b, c]")
    )
  }

  test("get - invalid duration (non parsable)") {
    val config = makeConfig(Map("key" -> "12secz"))

    assertEquals(
      config.get[FiniteDuration]("key").leftMap(_.getMessage),
      Left("Invalid value for property key=12secz. Must be [FiniteDuration]")
    )
  }

  test("get - invalid duration (infinite)") {
    val config = makeConfig(Map("key" -> "Inf"))

    assertEquals(
      config.get[FiniteDuration]("key").leftMap(_.getMessage),
      Left("The duration must be finite")
    )
  }

  private def makeConfig(props: Map[String, String]): Config =
    Config.ofProps(props)

}
