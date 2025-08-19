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
object V8jsExperimentalMetrics {

  val specs: List[MetricSpec] = List(
    GcDuration,
    HeapSpaceAvailableSize,
    HeapSpacePhysicalSize,
    MemoryHeapLimit,
    MemoryHeapUsed,
  )

  /** Garbage collection duration.
    *
    * @note
    *   <p> The values can be retrieved from <a
    *   href="https://nodejs.org/api/perf_hooks.html#performanceobserverobserveoptions">`perf_hooks.PerformanceObserver(...).observe({
    *   entryTypes: ['gc'] })`</a>
    */
  object GcDuration extends MetricSpec.Unsealed {

    val name: String = "v8js.gc.duration"
    val description: String = "Garbage collection duration."
    val unit: String = "s"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The type of garbage collection.
        */
      val v8jsGcType: AttributeSpec[String] =
        AttributeSpec(
          V8jsExperimentalAttributes.V8jsGcType,
          List(
          ),
          Requirement.required,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          v8jsGcType,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue](boundaries: BucketBoundaries): F[Histogram[F, A]] =
      Meter[F]
        .histogram[A](name)
        .withDescription(description)
        .withUnit(unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

  /** Heap space available size.
    *
    * @note
    *   <p> Value can be retrieved from value `space_available_size` of <a
    *   href="https://nodejs.org/api/v8.html#v8getheapspacestatistics">`v8.getHeapSpaceStatistics()`</a>
    */
  object HeapSpaceAvailableSize extends MetricSpec.Unsealed {

    val name: String = "v8js.heap.space.available_size"
    val description: String = "Heap space available size."
    val unit: String = "By"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The name of the space type of heap memory.
        *
        * @note
        *   <p> Value can be retrieved from value `space_name` of <a
        *   href="https://nodejs.org/api/v8.html#v8getheapspacestatistics">`v8.getHeapSpaceStatistics()`</a>
        */
      val v8jsHeapSpaceName: AttributeSpec[String] =
        AttributeSpec(
          V8jsExperimentalAttributes.V8jsHeapSpaceName,
          List(
          ),
          Requirement.required,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          v8jsHeapSpaceName,
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

  /** Committed size of a heap space.
    *
    * @note
    *   <p> Value can be retrieved from value `physical_space_size` of <a
    *   href="https://nodejs.org/api/v8.html#v8getheapspacestatistics">`v8.getHeapSpaceStatistics()`</a>
    */
  object HeapSpacePhysicalSize extends MetricSpec.Unsealed {

    val name: String = "v8js.heap.space.physical_size"
    val description: String = "Committed size of a heap space."
    val unit: String = "By"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The name of the space type of heap memory.
        *
        * @note
        *   <p> Value can be retrieved from value `space_name` of <a
        *   href="https://nodejs.org/api/v8.html#v8getheapspacestatistics">`v8.getHeapSpaceStatistics()`</a>
        */
      val v8jsHeapSpaceName: AttributeSpec[String] =
        AttributeSpec(
          V8jsExperimentalAttributes.V8jsHeapSpaceName,
          List(
          ),
          Requirement.required,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          v8jsHeapSpaceName,
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

  /** Total heap memory size pre-allocated.
    *
    * @note
    *   <p> The value can be retrieved from value `space_size` of <a
    *   href="https://nodejs.org/api/v8.html#v8getheapspacestatistics">`v8.getHeapSpaceStatistics()`</a>
    */
  object MemoryHeapLimit extends MetricSpec.Unsealed {

    val name: String = "v8js.memory.heap.limit"
    val description: String = "Total heap memory size pre-allocated."
    val unit: String = "By"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The name of the space type of heap memory.
        *
        * @note
        *   <p> Value can be retrieved from value `space_name` of <a
        *   href="https://nodejs.org/api/v8.html#v8getheapspacestatistics">`v8.getHeapSpaceStatistics()`</a>
        */
      val v8jsHeapSpaceName: AttributeSpec[String] =
        AttributeSpec(
          V8jsExperimentalAttributes.V8jsHeapSpaceName,
          List(
          ),
          Requirement.required,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          v8jsHeapSpaceName,
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

  /** Heap Memory size allocated.
    *
    * @note
    *   <p> The value can be retrieved from value `space_used_size` of <a
    *   href="https://nodejs.org/api/v8.html#v8getheapspacestatistics">`v8.getHeapSpaceStatistics()`</a>
    */
  object MemoryHeapUsed extends MetricSpec.Unsealed {

    val name: String = "v8js.memory.heap.used"
    val description: String = "Heap Memory size allocated."
    val unit: String = "By"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The name of the space type of heap memory.
        *
        * @note
        *   <p> Value can be retrieved from value `space_name` of <a
        *   href="https://nodejs.org/api/v8.html#v8getheapspacestatistics">`v8.getHeapSpaceStatistics()`</a>
        */
      val v8jsHeapSpaceName: AttributeSpec[String] =
        AttributeSpec(
          V8jsExperimentalAttributes.V8jsHeapSpaceName,
          List(
          ),
          Requirement.required,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          v8jsHeapSpaceName,
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
