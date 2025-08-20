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
object CpythonExperimentalMetrics {

  val specs: List[MetricSpec] = List(
    GcCollectedObjects,
    GcCollections,
    GcUncollectableObjects,
  )

  /** The total number of objects collected inside a generation since interpreter start.
    *
    * @note
    *   <p> This metric reports data from <a
    *   href="https://docs.python.org/3/library/gc.html#gc.get_stats">`gc.stats()`</a>.
    */
  object GcCollectedObjects extends MetricSpec.Unsealed {

    val name: String = "cpython.gc.collected_objects"
    val description: String = "The total number of objects collected inside a generation since interpreter start."
    val unit: String = "{object}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Value of the garbage collector collection generation.
        */
      val cpythonGcGeneration: AttributeSpec[Long] =
        AttributeSpec(
          CpythonExperimentalAttributes.CpythonGcGeneration,
          List(
            0,
            1,
            2,
          ),
          Requirement.required,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          cpythonGcGeneration,
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

  /** The number of times a generation was collected since interpreter start.
    *
    * @note
    *   <p> This metric reports data from <a
    *   href="https://docs.python.org/3/library/gc.html#gc.get_stats">`gc.stats()`</a>.
    */
  object GcCollections extends MetricSpec.Unsealed {

    val name: String = "cpython.gc.collections"
    val description: String = "The number of times a generation was collected since interpreter start."
    val unit: String = "{collection}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Value of the garbage collector collection generation.
        */
      val cpythonGcGeneration: AttributeSpec[Long] =
        AttributeSpec(
          CpythonExperimentalAttributes.CpythonGcGeneration,
          List(
            0,
            1,
            2,
          ),
          Requirement.required,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          cpythonGcGeneration,
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

  /** The total number of objects which were found to be uncollectable inside a generation since interpreter start.
    *
    * @note
    *   <p> This metric reports data from <a
    *   href="https://docs.python.org/3/library/gc.html#gc.get_stats">`gc.stats()`</a>.
    */
  object GcUncollectableObjects extends MetricSpec.Unsealed {

    val name: String = "cpython.gc.uncollectable_objects"
    val description: String =
      "The total number of objects which were found to be uncollectable inside a generation since interpreter start."
    val unit: String = "{object}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Value of the garbage collector collection generation.
        */
      val cpythonGcGeneration: AttributeSpec[Long] =
        AttributeSpec(
          CpythonExperimentalAttributes.CpythonGcGeneration,
          List(
            0,
            1,
            2,
          ),
          Requirement.required,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          cpythonGcGeneration,
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

}
