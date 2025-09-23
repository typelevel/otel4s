/*
 * Copyright 2024 Typelevel
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

package org.typelevel.otel4s.sdk.exporter.prometheus

import scala.annotation.tailrec

private object PrometheusConverter {

  private val ReservedSuffixes = List("total", "created", "bucket", "info")

  private val UnitMapping = Map(
    // Time
    "a" -> "years",
    "mo" -> "months",
    "wk" -> "weeks",
    "d" -> "days",
    "h" -> "hours",
    "min" -> "minutes",
    "s" -> "seconds",
    "ms" -> "milliseconds",
    "us" -> "microseconds",
    "ns" -> "nanoseconds",
    // Bytes
    "By" -> "bytes",
    "KiBy" -> "kibibytes",
    "MiBy" -> "mebibytes",
    "GiBy" -> "gibibytes",
    "TiBy" -> "tibibytes",
    "KBy" -> "kilobytes",
    "MBy" -> "megabytes",
    "GBy" -> "gigabytes",
    "TBy" -> "terabytes",
    // SI
    "m" -> "meters",
    "V" -> "volts",
    "A" -> "amperes",
    "J" -> "joules",
    "W" -> "watts",
    "g" -> "grams",
    // Misc
    "Cel" -> "celsius",
    "Hz" -> "hertz",
    "%" -> "percent",
    "1" -> "ratio"
  )

  private val PerMapping = Map(
    "s" -> "second",
    "min" -> "minute",
    "h" -> "hour",
    "d" -> "day",
    "wk" -> "week",
    "mo" -> "month",
    "a" -> "year"
  )

  private val NameIllegalFirstCharRegex = "[^a-zA-Z_:.]"
  private val NameIllegalCharsRegex = "[^a-zA-Z0-9_:.]"
  private val LabelIllegalFirstCharRegex = "[^a-zA-Z_.]"
  private val LabelIllegalCharsRegex = "[^a-zA-Z0-9_.]"
  private val UnitIllegalCharsRegex = "[^a-zA-Z0-9_:.]"

  private val Replacement = "_"

  /** Converts an arbitrary string to Prometheus metric name.
    */
  def convertName(name: String): Either[Throwable, String] = {
    @tailrec
    def removeReservedSuffixes(s: String): String = {
      val (updatedS, modified) = ReservedSuffixes.foldLeft((s, false)) { case ((str, modified), suffix) =>
        if (str == s"_$suffix" || str == s".$suffix") {
          (suffix, modified)
        } else if (str.endsWith(s"_$suffix") || str.endsWith(s".$suffix")) {
          (str.substring(0, str.length - suffix.length - 1), true)
        } else {
          (str, modified)
        }
      }

      if (modified) {
        removeReservedSuffixes(updatedS)
      } else {
        updatedS
      }
    }

    errorOnEmpty(name, "Empty string is not a valid metric name").map { _ =>
      val (firstChar, rest) = (name.substring(0, 1), name.substring(1))
      val nameWithoutIllegalChars =
        firstChar.replaceAll(NameIllegalFirstCharRegex, Replacement) + rest.replaceAll(
          NameIllegalCharsRegex,
          Replacement
        )

      val nameWithoutReservedSuffixes = removeReservedSuffixes(nameWithoutIllegalChars)
      asPrometheusName(nameWithoutReservedSuffixes)
    }
  }

  /** Converts an arbitrary string and OpenTelemetry unit to Prometheus metric name.
    */
  def convertName(name: String, unit: String): Either[Throwable, String] = {
    convertName(name).map { convertedName =>
      val nameWithUnit = if (convertedName.endsWith(s"_$unit")) {
        convertedName
      } else {
        s"${convertedName}_$unit"
      }

      asPrometheusName(nameWithUnit)
    }
  }

  /** Converts an arbitrary string to Prometheus label name.
    */
  def convertLabelName(label: String): Either[Throwable, String] = {
    errorOnEmpty(label, "Empty string is not a valid label name").map { _ =>
      val (firstChar, rest) = (label.substring(0, 1), label.substring(1))
      val labelWithoutIllegalChars =
        firstChar.replaceAll(LabelIllegalFirstCharRegex, Replacement) + rest.replaceAll(
          LabelIllegalCharsRegex,
          Replacement
        )

      asPrometheusName(labelWithoutIllegalChars)
    }
  }

  /** Converts OpenTelemetry unit names to Prometheus units.
    */
  def convertUnitName(unit: String): Option[String] = {
    val unitWithoutBraces: String = if (unit.contains("{")) {
      unit.replaceAll("\\{[^}]*}", "").trim()
    } else {
      unit
    }
    if (unitWithoutBraces.isEmpty) {
      None
    } else {
      Option(
        UnitMapping
          .getOrElse(
            unitWithoutBraces, {
              unitWithoutBraces.split("/", 2).map(_.trim) match {
                case Array(unitFirstPart, unitSecondPart) =>
                  val firstPartPlural = UnitMapping.getOrElse(unitFirstPart, unitFirstPart)
                  val secondPartSingular = PerMapping.getOrElse(unitSecondPart, unitSecondPart)
                  if (firstPartPlural.isEmpty) {
                    refineUnitName(s"per_$secondPartSingular")
                  } else {
                    refineUnitName(s"${firstPartPlural}_per_$secondPartSingular")
                  }
                case _ => refineUnitName(unitWithoutBraces)
              }
            }
          )
      )
    }
  }

  private def refineUnitName(unit: String): String = {
    def trim(s: String) = s.replaceAll("^[_.]+", "").replaceAll("[_.]+$", "")

    @tailrec
    def removeReservedSuffixes(s: String): String = {
      val (updatedS, modified) = ReservedSuffixes.foldLeft((s, false)) { case ((str, modified), suffix) =>
        if (str.endsWith(suffix)) {
          (trim(str.substring(0, str.length - suffix.length)), true)
        } else {
          (str, modified)
        }
      }

      if (modified) {
        removeReservedSuffixes(updatedS)
      } else {
        updatedS
      }
    }

    val unitWithoutIllegalChars = unit.replaceAll(UnitIllegalCharsRegex, Replacement)
    removeReservedSuffixes(trim(unitWithoutIllegalChars))
  }

  private def asPrometheusName(name: String): String = {
    name.replace(".", Replacement).replaceAll("_{2,}", Replacement)
  }

  private def errorOnEmpty(name: String, error: String): Either[IllegalArgumentException, String] = {
    Either.cond(name.nonEmpty, name, new IllegalArgumentException(error))
  }

}
