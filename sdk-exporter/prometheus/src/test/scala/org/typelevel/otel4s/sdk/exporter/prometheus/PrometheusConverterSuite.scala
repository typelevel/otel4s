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

import cats.syntax.either._
import munit.FunSuite
import org.typelevel.otel4s.sdk.exporter.prometheus.PrometheusConverter.convertLabelName
import org.typelevel.otel4s.sdk.exporter.prometheus.PrometheusConverter.convertName
import org.typelevel.otel4s.sdk.exporter.prometheus.PrometheusConverter.convertUnitName

class PrometheusConverterSuite extends FunSuite {

  test("convert a metric name") {
    List(
      ("0abc.def", "_abc_def"),
      ("___ab.:c0", "_ab_:c0"),
      ("my_prefix/my_metric", "my_prefix_my_metric"),
      ("my_counter_total", "my_counter"),
      ("jvm.info", "jvm"),
      ("jvm_info", "jvm"),
      ("_total", "total"),
      ("total", "total")
    ).foreach { case (name, expected) =>
      assertEquals(convertName(name), Right(expected))
    }
  }

  test("fail to convert an empty metric name") {
    assertEquals(convertName("").leftMap(_.getMessage), "Empty string is not a valid metric name".asLeft[String])
  }

  test("convert a metric name with unit") {
    List(
      (("0abc.def", "ratio"), "_abc_def_ratio"),
      (("___ab.:c0", "ratio"), "_ab_:c0_ratio"),
      (("my_prefix/my_metric", "ratio"), "my_prefix_my_metric_ratio"),
      (("my_counter_total", "ratio"), "my_counter_ratio"),
      (("jvm.info", "ratio"), "jvm_ratio"),
      (("jvm_info", "ratio"), "jvm_ratio"),
      (("a.b", "ratio"), "a_b_ratio"),
      (("_total", "ratio"), "total_ratio"),
      (("total", "ratio"), "total_ratio")
    ).foreach { case ((name, unit), expected) =>
      assertEquals(convertName(name, unit), Right(expected))
    }
  }

  test("convert a label name") {
    List(
      ("0abc.def", "_abc_def"),
      ("_abc", "_abc"),
      ("__abc", "_abc"),
      ("___abc", "_abc"),
      ("_.abc", "_abc"),
      ("abc.def", "abc_def"),
      ("abc.def2", "abc_def2")
    ).foreach { case (label, expected) =>
      assertEquals(convertLabelName(label), Right(expected))
    }
  }

  test("fail to convert an empty label name") {
    assertEquals(convertLabelName("").leftMap(_.getMessage), "Empty string is not a valid label name".asLeft[String])
  }

  test("convert a unit name") {
    List(
      ("seconds", "seconds"),
      ("seconds_total", "seconds"),
      ("seconds_total_total", "seconds"),
      ("secondstotal", "seconds"),
      ("2", "2"),
      ("By", "bytes"),
      ("KBy", "kilobytes"),
      ("MBy", "megabytes"),
      ("GBy", "gigabytes"),
      ("TBy", "terabytes"),
      ("KiBy", "kibibytes"),
      ("MiBy", "mebibytes"),
      ("GiBy", "gibibytes"),
      ("TiBy", "tibibytes"),
      ("d", "days"),
      ("h", "hours"),
      ("s", "seconds"),
      ("ms", "milliseconds"),
      ("us", "microseconds"),
      ("ns", "nanoseconds"),
      ("min", "minutes"),
      ("%", "percent"),
      ("Hz", "hertz"),
      ("Cel", "celsius"),
      ("S", "S"),
      ("1", "ratio"),
      ("{packets}V", "volts"),
      ("{objects}/s", "per_second"),
      ("m/s", "meters_per_second"),
      ("m/min", "meters_per_minute"),
      ("A/d", "amperes_per_day"),
      ("W/wk", "watts_per_week"),
      ("J/mo", "joules_per_month"),
      ("TBy/a", "terabytes_per_year"),
      ("v/v", "v_per_v"),
      ("km/h", "km_per_hour"),
      ("g/x", "grams_per_x"),
      ("watts_W", "watts_W")
    ).foreach { case (unit, expected) =>
      assertEquals(convertUnitName(unit), Some(expected))
    }
  }

  test("return None when convert an empty or only annotation unit name") {
    assertEquals(convertUnitName(""), None)
    assertEquals(convertUnitName("{thread}"), None)
  }

}
