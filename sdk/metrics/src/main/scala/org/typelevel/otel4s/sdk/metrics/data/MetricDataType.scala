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

package org.typelevel.otel4s.sdk.metrics.data

sealed trait MetricDataType

object MetricDataType {

  case object LongGauge extends MetricDataType
  case object DoubleGauge extends MetricDataType
  case object LongSum extends MetricDataType
  case object DoubleSum extends MetricDataType
  case object Summary extends MetricDataType
  case object Histogram extends MetricDataType
  case object ExponentialHistogram extends MetricDataType

}
