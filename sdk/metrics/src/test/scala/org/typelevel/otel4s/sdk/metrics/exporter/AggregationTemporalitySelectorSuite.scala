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

package org.typelevel.otel4s.sdk.metrics.exporter

import munit.FunSuite
import org.typelevel.otel4s.sdk.metrics.InstrumentType
import org.typelevel.otel4s.sdk.metrics.data.AggregationTemporality

class AggregationTemporalitySelectorSuite extends FunSuite {

  test("always cumulative") {
    val selector = AggregationTemporalitySelector.alwaysCumulative
    InstrumentType.values.foreach { tpe =>
      assertEquals(selector.select(tpe), AggregationTemporality.Cumulative)
    }
  }

  test("delta preferred") {
    val selector = AggregationTemporalitySelector.deltaPreferred
    InstrumentType.values.foreach { tpe =>
      val expected = tpe match {
        case InstrumentType.UpDownCounter =>
          AggregationTemporality.Cumulative
        case InstrumentType.ObservableUpDownCounter =>
          AggregationTemporality.Cumulative
        case _ =>
          AggregationTemporality.Delta
      }

      assertEquals(selector.select(tpe), expected)
    }
  }

  test("low memory") {
    val selector = AggregationTemporalitySelector.lowMemory
    InstrumentType.values.foreach { tpe =>
      val expected = tpe match {
        case InstrumentType.UpDownCounter =>
          AggregationTemporality.Cumulative
        case InstrumentType.ObservableUpDownCounter =>
          AggregationTemporality.Cumulative
        case InstrumentType.ObservableCounter =>
          AggregationTemporality.Cumulative
        case _ =>
          AggregationTemporality.Delta
      }

      assertEquals(selector.select(tpe), expected)
    }
  }

}
