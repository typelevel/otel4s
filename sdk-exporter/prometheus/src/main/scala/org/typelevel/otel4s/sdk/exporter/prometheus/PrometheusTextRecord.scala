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

import cats.Show
import cats.data.NonEmptyVector
import cats.syntax.foldable._
import cats.syntax.option._
import cats.syntax.show._
import org.typelevel.otel4s.sdk.exporter.prometheus.PrometheusTextRecord.PrometheusTextPoint

import scala.collection.immutable.ListMap

private final case class PrometheusTextRecord(
    helpLine: Option[String],
    typeLine: String,
    points: NonEmptyVector[PrometheusTextPoint]
)

private object PrometheusTextRecord {

  private[prometheus] final case class PrometheusTextPoint(name: String, labels: ListMap[String, String], value: String)

  private[prometheus] object PrometheusTextPoint {
    private def showLabels(labels: Map[String, String]): String = {
      if (labels.nonEmpty) {
        s"{${labels.map { case (k, v) => s"""$k="$v"""" }.mkString(",")}}"
      } else {
        ""
      }
    }

    implicit val show: Show[PrometheusTextPoint] = Show.show { point =>
      s"""${point.name}${showLabels(point.labels)} ${point.value}"""
    }
  }

  implicit val show: Show[PrometheusTextRecord] = Show.show { record =>
    record.helpLine.map(h => s"# HELP $h\n").orEmpty +
      s"""# TYPE ${record.typeLine}
         |${record.points.map(point => point.show).mkString_("\n")}
         |""".stripMargin
  }
}
