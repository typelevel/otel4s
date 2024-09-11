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

package org.typelevel.otel4s
package semconv
package metrics

import org.typelevel.otel4s.metrics._

// DO NOT EDIT, this is an Auto-generated file from buildscripts/templates/registry/otel4s/metrics/SemanticMetrics.scala.j2
object NodejsExperimentalMetrics {

  /** Event loop maximum delay. <p>
    * @note
    *   <p> Value can be retrieved from value `histogram.max` of <a
    *   href="https://nodejs.org/api/perf_hooks.html#perf_hooksmonitoreventloopdelayoptions">`perf_hooks.monitorEventLoopDelay([options])`</a>
    */
  object EventloopDelayMax {

    val Name = "nodejs.eventloop.delay.max"
    val Description = "Event loop maximum delay."
    val Unit = "s"

    def create[F[_]: Meter]: F[Gauge[F, Long]] =
      Meter[F]
        .gauge[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Event loop mean delay. <p>
    * @note
    *   <p> Value can be retrieved from value `histogram.mean` of <a
    *   href="https://nodejs.org/api/perf_hooks.html#perf_hooksmonitoreventloopdelayoptions">`perf_hooks.monitorEventLoopDelay([options])`</a>
    */
  object EventloopDelayMean {

    val Name = "nodejs.eventloop.delay.mean"
    val Description = "Event loop mean delay."
    val Unit = "s"

    def create[F[_]: Meter]: F[Gauge[F, Long]] =
      Meter[F]
        .gauge[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Event loop minimum delay. <p>
    * @note
    *   <p> Value can be retrieved from value `histogram.min` of <a
    *   href="https://nodejs.org/api/perf_hooks.html#perf_hooksmonitoreventloopdelayoptions">`perf_hooks.monitorEventLoopDelay([options])`</a>
    */
  object EventloopDelayMin {

    val Name = "nodejs.eventloop.delay.min"
    val Description = "Event loop minimum delay."
    val Unit = "s"

    def create[F[_]: Meter]: F[Gauge[F, Long]] =
      Meter[F]
        .gauge[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Event loop 50 percentile delay. <p>
    * @note
    *   <p> Value can be retrieved from value `histogram.percentile(50)` of <a
    *   href="https://nodejs.org/api/perf_hooks.html#perf_hooksmonitoreventloopdelayoptions">`perf_hooks.monitorEventLoopDelay([options])`</a>
    */
  object EventloopDelayP50 {

    val Name = "nodejs.eventloop.delay.p50"
    val Description = "Event loop 50 percentile delay."
    val Unit = "s"

    def create[F[_]: Meter]: F[Gauge[F, Long]] =
      Meter[F]
        .gauge[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Event loop 90 percentile delay. <p>
    * @note
    *   <p> Value can be retrieved from value `histogram.percentile(90)` of <a
    *   href="https://nodejs.org/api/perf_hooks.html#perf_hooksmonitoreventloopdelayoptions">`perf_hooks.monitorEventLoopDelay([options])`</a>
    */
  object EventloopDelayP90 {

    val Name = "nodejs.eventloop.delay.p90"
    val Description = "Event loop 90 percentile delay."
    val Unit = "s"

    def create[F[_]: Meter]: F[Gauge[F, Long]] =
      Meter[F]
        .gauge[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Event loop 99 percentile delay. <p>
    * @note
    *   <p> Value can be retrieved from value `histogram.percentile(99)` of <a
    *   href="https://nodejs.org/api/perf_hooks.html#perf_hooksmonitoreventloopdelayoptions">`perf_hooks.monitorEventLoopDelay([options])`</a>
    */
  object EventloopDelayP99 {

    val Name = "nodejs.eventloop.delay.p99"
    val Description = "Event loop 99 percentile delay."
    val Unit = "s"

    def create[F[_]: Meter]: F[Gauge[F, Long]] =
      Meter[F]
        .gauge[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Event loop standard deviation delay. <p>
    * @note
    *   <p> Value can be retrieved from value `histogram.stddev` of <a
    *   href="https://nodejs.org/api/perf_hooks.html#perf_hooksmonitoreventloopdelayoptions">`perf_hooks.monitorEventLoopDelay([options])`</a>
    */
  object EventloopDelayStddev {

    val Name = "nodejs.eventloop.delay.stddev"
    val Description = "Event loop standard deviation delay."
    val Unit = "s"

    def create[F[_]: Meter]: F[Gauge[F, Long]] =
      Meter[F]
        .gauge[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Event loop utilization. <p>
    * @note
    *   <p> The value range is [0.0,1.0] and can be retrieved from value <a
    *   href="https://nodejs.org/api/perf_hooks.html#performanceeventlooputilizationutilization1-utilization2">`performance.eventLoopUtilization([utilization1[,
    *   utilization2]])`</a>
    */
  object EventloopUtilization {

    val Name = "nodejs.eventloop.utilization"
    val Description = "Event loop utilization."
    val Unit = "1"

    def create[F[_]: Meter]: F[Gauge[F, Long]] =
      Meter[F]
        .gauge[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

}
