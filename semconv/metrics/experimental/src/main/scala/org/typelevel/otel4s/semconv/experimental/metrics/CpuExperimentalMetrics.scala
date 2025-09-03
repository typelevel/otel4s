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
object CpuExperimentalMetrics {

  @annotation.nowarn("cat=deprecation")
  val specs: List[MetricSpec] = List(
    Frequency,
    Time,
    Utilization,
  )

  /** Deprecated. Use `system.cpu.frequency` instead.
    */
  @deprecated("Replaced by `system.cpu.frequency`.", "")
  object Frequency extends MetricSpec.Unsealed {

    val name: String = "cpu.frequency"
    val description: String = "Deprecated. Use `system.cpu.frequency` instead."
    val unit: String = "{Hz}"
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

  /** Deprecated. Use `system.cpu.time` instead.
    */
  @deprecated("Replaced by `system.cpu.time`.", "")
  object Time extends MetricSpec.Unsealed {

    val name: String = "cpu.time"
    val description: String = "Deprecated. Use `system.cpu.time` instead."
    val unit: String = "s"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The logical CPU number [0..n-1]
        */
      val cpuLogicalNumber: AttributeSpec[Long] =
        AttributeSpec(
          CpuExperimentalAttributes.CpuLogicalNumber,
          List(
            1,
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The mode of the CPU
        *
        * @note
        *   <p> Following states SHOULD be used: `user`, `system`, `nice`, `idle`, `iowait`, `interrupt`, `steal`
        */
      val cpuMode: AttributeSpec[String] =
        AttributeSpec(
          CpuExperimentalAttributes.CpuMode,
          List(
            "user",
            "system",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          cpuLogicalNumber,
          cpuMode,
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

  /** Deprecated. Use `system.cpu.utilization` instead.
    */
  @deprecated("Replaced by `system.cpu.utilization`.", "")
  object Utilization extends MetricSpec.Unsealed {

    val name: String = "cpu.utilization"
    val description: String = "Deprecated. Use `system.cpu.utilization` instead."
    val unit: String = "1"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The logical CPU number [0..n-1]
        */
      val cpuLogicalNumber: AttributeSpec[Long] =
        AttributeSpec(
          CpuExperimentalAttributes.CpuLogicalNumber,
          List(
            1,
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The mode of the CPU
        *
        * @note
        *   <p> Following states SHOULD be used: `user`, `system`, `nice`, `idle`, `iowait`, `interrupt`, `steal`
        */
      val cpuMode: AttributeSpec[String] =
        AttributeSpec(
          CpuExperimentalAttributes.CpuMode,
          List(
            "user",
            "system",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          cpuLogicalNumber,
          cpuMode,
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

}
