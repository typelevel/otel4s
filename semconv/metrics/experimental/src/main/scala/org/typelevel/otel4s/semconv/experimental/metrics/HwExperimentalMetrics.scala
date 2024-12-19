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
import org.typelevel.otel4s.semconv.attributes._
import org.typelevel.otel4s.semconv.experimental.attributes._

// DO NOT EDIT, this is an Auto-generated file from buildscripts/templates/registry/otel4s/metrics/SemanticMetrics.scala.j2
object HwExperimentalMetrics {

  val specs: List[MetricSpec] = List(
    Energy,
    Errors,
    Power,
    Status,
  )

  /** Energy consumed by the component
    */
  object Energy extends MetricSpec {

    val name: String = "hw.energy"
    val description: String = "Energy consumed by the component"
    val unit: String = "J"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Type of the component <p>
        * @note
        *   <p> Describes the category of the hardware component for which `hw.state` is being reported. For example,
        *   `hw.type=temperature` along with `hw.state=degraded` would indicate that the temperature of the hardware
        *   component has been reported as `degraded`.
        */
      val hwType: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwType,
          List(
          ),
          Requirement.required,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwId,
          hwName,
          hwParent,
          hwType,
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

  /** Number of errors encountered by the component
    */
  object Errors extends MetricSpec {

    val name: String = "hw.errors"
    val description: String = "Number of errors encountered by the component"
    val unit: String = "{error}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The type of error encountered by the component <p>
        * @note
        *   <p> The `error.type` SHOULD match the error code reported by the component, the canonical name of the error,
        *   or another low-cardinality error identifier. Instrumentations SHOULD document the list of errors they
        *   report.
        */
      val errorType: AttributeSpec[String] =
        AttributeSpec(
          ErrorAttributes.ErrorType,
          List(
            "uncorrected",
            "zero_buffer_credit",
            "crc",
            "bad_sector",
          ),
          Requirement.conditionallyRequired("if and only if an error has occurred"),
          Stability.stable
        )

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Type of the component <p>
        * @note
        *   <p> Describes the category of the hardware component for which `hw.state` is being reported. For example,
        *   `hw.type=temperature` along with `hw.state=degraded` would indicate that the temperature of the hardware
        *   component has been reported as `degraded`.
        */
      val hwType: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwType,
          List(
          ),
          Requirement.required,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          errorType,
          hwId,
          hwName,
          hwParent,
          hwType,
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

  /** Instantaneous power consumed by the component <p>
    * @note
    *   <p> It is recommended to report `hw.energy` instead of `hw.power` when possible.
    */
  object Power extends MetricSpec {

    val name: String = "hw.power"
    val description: String = "Instantaneous power consumed by the component"
    val unit: String = "W"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Type of the component <p>
        * @note
        *   <p> Describes the category of the hardware component for which `hw.state` is being reported. For example,
        *   `hw.type=temperature` along with `hw.state=degraded` would indicate that the temperature of the hardware
        *   component has been reported as `degraded`.
        */
      val hwType: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwType,
          List(
          ),
          Requirement.required,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwId,
          hwName,
          hwParent,
          hwType,
        )
    }

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

  /** Operational status: `1` (true) or `0` (false) for each of the possible states <p>
    * @note
    *   <p> `hw.status` is currently specified as an <em>UpDownCounter</em> but would ideally be represented using a <a
    *   href="https://github.com/OpenObservability/OpenMetrics/blob/main/specification/OpenMetrics.md#stateset"><em>StateSet</em>
    *   as defined in OpenMetrics</a>. This semantic convention will be updated once <em>StateSet</em> is specified in
    *   OpenTelemetry. This planned change is not expected to have any consequence on the way users query their
    *   timeseries backend to retrieve the values of `hw.status` over time.
    */
  object Status extends MetricSpec {

    val name: String = "hw.status"
    val description: String = "Operational status: `1` (true) or `0` (false) for each of the possible states"
    val unit: String = "1"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** An identifier for the hardware component, unique within the monitored host
        */
      val hwId: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwId,
          List(
            "win32battery_battery_testsysa33_1",
          ),
          Requirement.required,
          Stability.development
        )

      /** An easily-recognizable name for the hardware component
        */
      val hwName: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwName,
          List(
            "eth0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Unique identifier of the parent component (typically the `hw.id` attribute of the enclosure, or disk
        * controller)
        */
      val hwParent: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwParent,
          List(
            "dellStorage_perc_0",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The current state of the component
        */
      val hwState: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwState,
          List(
          ),
          Requirement.required,
          Stability.development
        )

      /** Type of the component <p>
        * @note
        *   <p> Describes the category of the hardware component for which `hw.state` is being reported. For example,
        *   `hw.type=temperature` along with `hw.state=degraded` would indicate that the temperature of the hardware
        *   component has been reported as `degraded`.
        */
      val hwType: AttributeSpec[String] =
        AttributeSpec(
          HwExperimentalAttributes.HwType,
          List(
          ),
          Requirement.required,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          hwId,
          hwName,
          hwParent,
          hwState,
          hwType,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[UpDownCounter[F, A]] =
      Meter[F]
        .upDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableUpDownCounter] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

}
