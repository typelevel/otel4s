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

import cats.effect.Sync
import cats.effect.std.Env
import cats.syntax.either._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.traverse._

import java.util.Locale
import scala.concurrent.duration._

sealed trait Config {
  def getString(key: String): Option[String]
  def getBoolean(key: String): Either[ConfigurationError, Option[Boolean]]
  def getInt(key: String): Either[ConfigurationError, Option[Int]]
  def getDouble(key: String): Either[ConfigurationError, Option[Double]]
  def getFiniteDuration(
      key: String
  ): Either[ConfigurationError, Option[FiniteDuration]]
  def getStringList(key: String): List[String]
  def getStringSet(key: String): Either[ConfigurationError, Set[String]]
  def getStringMap(key: String): Either[ConfigurationError, Map[String, String]]

  def withOverrides(overrides: Map[String, String]): Config
}

object Config {

  def apply(
      systemProperties: Map[String, String],
      environmentVariables: Map[String, String],
      defaultProperties: Map[String, String]
  ): Config = {
    val default = defaultProperties.map { case (key, value) =>
      (normalizePropertyKey(key), value)
    }

    val env = environmentVariables.map { case (key, value) =>
      (normalizeEnvVariableKey(key), value)
    }

    val props = systemProperties.map { case (key, value) =>
      (normalizePropertyKey(key), value)
    }

    Impl(default ++ env ++ props)
  }

  def load[F[_]: Sync](default: Map[String, String]): F[Config] =
    for {
      envVars <- Env.make[F].entries
      systemProps <- Sync[F].delay(sys.props.toMap)
    } yield apply(systemProps, envVars.toMap, default)

  /** Normalize a property key by converting to lower case and replacing "-"
    * with ".".
    */
  private def normalizePropertyKey(key: String): String =
    key.toLowerCase(Locale.ROOT).replace("-", ".");

  /** Normalize an env variable key by converting to lower case and replacing
    * "_" with ".".
    */
  private def normalizeEnvVariableKey(key: String): String =
    key.toLowerCase(Locale.ROOT).replace("_", ".")

  private final case class Impl[F[_]](properties: Map[String, String])
      extends Config {

    def withOverrides(overrides: Map[String, String]): Config = {
      val normalized = overrides.map { case (key, value) =>
        (normalizePropertyKey(key), value)
      }

      copy(properties = this.properties ++ normalized)
    }

    def getString(key: String): Option[String] =
      properties.get(normalizePropertyKey(key)).map(_.trim).filter(_.nonEmpty)

    def getBoolean(key: String): Either[ConfigurationError, Option[Boolean]] =
      get(key, "Boolean")(_.toBoolean)

    def getInt(key: String): Either[ConfigurationError, Option[Int]] =
      get(key, "Int")(_.toInt)

    def getDouble(key: String): Either[ConfigurationError, Option[Double]] =
      get(key, "Double")(_.toDouble)

    def getFiniteDuration(
        key: String
    ): Either[ConfigurationError, Option[FiniteDuration]] =
      get(key, "FiniteDuration")(value => Duration(value)).flatMap {
        case Some(duration: FiniteDuration) =>
          Right(Some(duration))

        case Some(_) =>
          Left(
            new ConfigurationError(
              s"The duration under $key=${getString(key)} must be finite",
              None
            )
          )

        case None =>
          Right(None)
      }

    def getStringList(key: String): List[String] =
      getString(key) match {
        case Some(value) =>
          value.split(",").map(_.trim).filter(_.nonEmpty).toList

        case None =>
          Nil
      }

    def getStringSet(key: String): Either[ConfigurationError, Set[String]] = {
      val list = getStringList(key)
      val set = list.toSet

      Either.cond(
        test = set.size == list.size,
        right = set,
        left = {
          val duplicates = list
            .groupBy(identity)
            .collect { case (key, value) if value.sizeIs > 1 => key }
            .mkString("[", ",", "]")

          new ConfigurationError(s"$key contains duplicates: $duplicates", None)
        }
      )
    }

    def getStringMap(
        key: String
    ): Either[ConfigurationError, Map[String, String]] = {
      getStringList(key)
        .traverse { entry =>
          val split = entry.split("=", 2)
          Either.cond(
            split.length == 2 && split.head.nonEmpty,
            split.map(_.trim).filter(_.nonEmpty).toList,
            new ConfigurationError(
              s"Invalid map property: $key=${getString(key)}",
              None
            )
          )
        }
        .map { entries =>
          entries
            .collect { case key :: value :: Nil => (key, value) }
            .groupMapReduce(_._1)(_._2)((_, last) => last)
        }
    }

    private def get[A](
        key: String,
        hint: String
    )(f: String => A): Either[ConfigurationError, Option[A]] =
      decode(key) { value =>
        Either
          .catchNonFatal(f(value))
          .leftMap { e =>
            new ConfigurationError(
              s"Invalid value for property $key=$value. Must be a $hint",
              Some(e)
            )
          }
      }

    private def decode[A](key: String)(
        f: String => Either[ConfigurationError, A]
    ): Either[ConfigurationError, Option[A]] =
      getString(key) match {
        case Some(value) =>
          f(value).map(Some(_))

        case None =>
          Right(None)
      }
  }

}
