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
package experimental
package metrics

import cats.effect.Resource
import org.typelevel.otel4s.metrics._
import org.typelevel.otel4s.semconv.experimental.attributes._

// DO NOT EDIT, this is an Auto-generated file from buildscripts/templates/registry/otel4s/metrics/SemanticMetrics.scala.j2
object NodejsExperimentalMetrics {

  val specs: List[MetricSpec] = List(
    EventloopDelayMax,
    EventloopDelayMean,
    EventloopDelayMin,
    EventloopDelayP50,
    EventloopDelayP90,
    EventloopDelayP99,
    EventloopDelayStddev,
    EventloopTime,
    EventloopUtilization,
  )

  /** Event loop maximum delay. <p>
    * @note
    *   <p> Value can be retrieved from value `histogram.max` of <a
    *   href="https://nodejs.org/api/perf_hooks.html#perf_hooksmonitoreventloopdelayoptions">`perf_hooks.monitorEventLoopDelay([options])`</a>
    */
  object EventloopDelayMax extends MetricSpec {

    val name: String = "nodejs.eventloop.delay.max"
    val description: String = "Event loop maximum delay."
    val unit: String = "s"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = Nil

    def create[F[_]: Meter, A: MeasurementValue]: F[Gauge[F, A]] =
      Meter[F]
        .gauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Event loop mean delay. <p>
    * @note
    *   <p> Value can be retrieved from value `histogram.mean` of <a
    *   href="https://nodejs.org/api/perf_hooks.html#perf_hooksmonitoreventloopdelayoptions">`perf_hooks.monitorEventLoopDelay([options])`</a>
    */
  object EventloopDelayMean extends MetricSpec {

    val name: String = "nodejs.eventloop.delay.mean"
    val description: String = "Event loop mean delay."
    val unit: String = "s"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = Nil

    def create[F[_]: Meter, A: MeasurementValue]: F[Gauge[F, A]] =
      Meter[F]
        .gauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Event loop minimum delay. <p>
    * @note
    *   <p> Value can be retrieved from value `histogram.min` of <a
    *   href="https://nodejs.org/api/perf_hooks.html#perf_hooksmonitoreventloopdelayoptions">`perf_hooks.monitorEventLoopDelay([options])`</a>
    */
  object EventloopDelayMin extends MetricSpec {

    val name: String = "nodejs.eventloop.delay.min"
    val description: String = "Event loop minimum delay."
    val unit: String = "s"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = Nil

    def create[F[_]: Meter, A: MeasurementValue]: F[Gauge[F, A]] =
      Meter[F]
        .gauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Event loop 50 percentile delay. <p>
    * @note
    *   <p> Value can be retrieved from value `histogram.percentile(50)` of <a
    *   href="https://nodejs.org/api/perf_hooks.html#perf_hooksmonitoreventloopdelayoptions">`perf_hooks.monitorEventLoopDelay([options])`</a>
    */
  object EventloopDelayP50 extends MetricSpec {

    val name: String = "nodejs.eventloop.delay.p50"
    val description: String = "Event loop 50 percentile delay."
    val unit: String = "s"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = Nil

    def create[F[_]: Meter, A: MeasurementValue]: F[Gauge[F, A]] =
      Meter[F]
        .gauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Event loop 90 percentile delay. <p>
    * @note
    *   <p> Value can be retrieved from value `histogram.percentile(90)` of <a
    *   href="https://nodejs.org/api/perf_hooks.html#perf_hooksmonitoreventloopdelayoptions">`perf_hooks.monitorEventLoopDelay([options])`</a>
    */
  object EventloopDelayP90 extends MetricSpec {

    val name: String = "nodejs.eventloop.delay.p90"
    val description: String = "Event loop 90 percentile delay."
    val unit: String = "s"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = Nil

    def create[F[_]: Meter, A: MeasurementValue]: F[Gauge[F, A]] =
      Meter[F]
        .gauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Event loop 99 percentile delay. <p>
    * @note
    *   <p> Value can be retrieved from value `histogram.percentile(99)` of <a
    *   href="https://nodejs.org/api/perf_hooks.html#perf_hooksmonitoreventloopdelayoptions">`perf_hooks.monitorEventLoopDelay([options])`</a>
    */
  object EventloopDelayP99 extends MetricSpec {

    val name: String = "nodejs.eventloop.delay.p99"
    val description: String = "Event loop 99 percentile delay."
    val unit: String = "s"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = Nil

    def create[F[_]: Meter, A: MeasurementValue]: F[Gauge[F, A]] =
      Meter[F]
        .gauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Event loop standard deviation delay. <p>
    * @note
    *   <p> Value can be retrieved from value `histogram.stddev` of <a
    *   href="https://nodejs.org/api/perf_hooks.html#perf_hooksmonitoreventloopdelayoptions">`perf_hooks.monitorEventLoopDelay([options])`</a>
    */
  object EventloopDelayStddev extends MetricSpec {

    val name: String = "nodejs.eventloop.delay.stddev"
    val description: String = "Event loop standard deviation delay."
    val unit: String = "s"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = Nil

    def create[F[_]: Meter, A: MeasurementValue]: F[Gauge[F, A]] =
      Meter[F]
        .gauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Cumulative duration of time the event loop has been in each state. <p>
    * @note
    *   <p> Value can be retrieved from <a
    *   href="https://nodejs.org/api/perf_hooks.html#performanceeventlooputilizationutilization1-utilization2">`performance.eventLoopUtilization([utilization1[,
    *   utilization2]])`</a>
    */
  object EventloopTime extends MetricSpec {

    val name: String = "nodejs.eventloop.time"
    val description: String = "Cumulative duration of time the event loop has been in each state."
    val unit: String = "s"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The state of event loop time.
        */
      val nodejsEventloopState: AttributeSpec[String] =
        AttributeSpec(
          NodejsExperimentalAttributes.NodejsEventloopState,
          List(
          ),
          Requirement.required,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          nodejsEventloopState,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Counter[F, A]] =
      Meter[F]
        .counter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableCounter] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Event loop utilization. <p>
    * @note
    *   <p> The value range is [0.0, 1.0] and can be retrieved from <a
    *   href="https://nodejs.org/api/perf_hooks.html#performanceeventlooputilizationutilization1-utilization2">`performance.eventLoopUtilization([utilization1[,
    *   utilization2]])`</a>
    */
  object EventloopUtilization extends MetricSpec {

    val name: String = "nodejs.eventloop.utilization"
    val description: String = "Event loop utilization."
    val unit: String = "1"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = Nil

    def create[F[_]: Meter, A: MeasurementValue]: F[Gauge[F, A]] =
      Meter[F]
        .gauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

}
