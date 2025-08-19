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
object FaasExperimentalMetrics {

  val specs: List[MetricSpec] = List(
    Coldstarts,
    CpuUsage,
    Errors,
    InitDuration,
    Invocations,
    InvokeDuration,
    MemUsage,
    NetIo,
    Timeouts,
  )

  /** Number of invocation cold starts
    */
  object Coldstarts extends MetricSpec.Unsealed {

    val name: String = "faas.coldstarts"
    val description: String = "Number of invocation cold starts"
    val unit: String = "{coldstart}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Type of the trigger which caused this function invocation.
        */
      val faasTrigger: AttributeSpec[String] =
        AttributeSpec(
          FaasExperimentalAttributes.FaasTrigger,
          List(
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          faasTrigger,
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

  /** Distribution of CPU usage per invocation
    */
  object CpuUsage extends MetricSpec.Unsealed {

    val name: String = "faas.cpu_usage"
    val description: String = "Distribution of CPU usage per invocation"
    val unit: String = "s"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Type of the trigger which caused this function invocation.
        */
      val faasTrigger: AttributeSpec[String] =
        AttributeSpec(
          FaasExperimentalAttributes.FaasTrigger,
          List(
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          faasTrigger,
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

  /** Number of invocation errors
    */
  object Errors extends MetricSpec.Unsealed {

    val name: String = "faas.errors"
    val description: String = "Number of invocation errors"
    val unit: String = "{error}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Type of the trigger which caused this function invocation.
        */
      val faasTrigger: AttributeSpec[String] =
        AttributeSpec(
          FaasExperimentalAttributes.FaasTrigger,
          List(
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          faasTrigger,
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

  /** Measures the duration of the function's initialization, such as a cold start
    */
  object InitDuration extends MetricSpec.Unsealed {

    val name: String = "faas.init_duration"
    val description: String = "Measures the duration of the function's initialization, such as a cold start"
    val unit: String = "s"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Type of the trigger which caused this function invocation.
        */
      val faasTrigger: AttributeSpec[String] =
        AttributeSpec(
          FaasExperimentalAttributes.FaasTrigger,
          List(
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          faasTrigger,
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

  /** Number of successful invocations
    */
  object Invocations extends MetricSpec.Unsealed {

    val name: String = "faas.invocations"
    val description: String = "Number of successful invocations"
    val unit: String = "{invocation}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Type of the trigger which caused this function invocation.
        */
      val faasTrigger: AttributeSpec[String] =
        AttributeSpec(
          FaasExperimentalAttributes.FaasTrigger,
          List(
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          faasTrigger,
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

  /** Measures the duration of the function's logic execution
    */
  object InvokeDuration extends MetricSpec.Unsealed {

    val name: String = "faas.invoke_duration"
    val description: String = "Measures the duration of the function's logic execution"
    val unit: String = "s"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Type of the trigger which caused this function invocation.
        */
      val faasTrigger: AttributeSpec[String] =
        AttributeSpec(
          FaasExperimentalAttributes.FaasTrigger,
          List(
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          faasTrigger,
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

  /** Distribution of max memory usage per invocation
    */
  object MemUsage extends MetricSpec.Unsealed {

    val name: String = "faas.mem_usage"
    val description: String = "Distribution of max memory usage per invocation"
    val unit: String = "By"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Type of the trigger which caused this function invocation.
        */
      val faasTrigger: AttributeSpec[String] =
        AttributeSpec(
          FaasExperimentalAttributes.FaasTrigger,
          List(
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          faasTrigger,
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

  /** Distribution of net I/O usage per invocation
    */
  object NetIo extends MetricSpec.Unsealed {

    val name: String = "faas.net_io"
    val description: String = "Distribution of net I/O usage per invocation"
    val unit: String = "By"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Type of the trigger which caused this function invocation.
        */
      val faasTrigger: AttributeSpec[String] =
        AttributeSpec(
          FaasExperimentalAttributes.FaasTrigger,
          List(
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          faasTrigger,
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

  /** Number of invocation timeouts
    */
  object Timeouts extends MetricSpec.Unsealed {

    val name: String = "faas.timeouts"
    val description: String = "Number of invocation timeouts"
    val unit: String = "{timeout}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Type of the trigger which caused this function invocation.
        */
      val faasTrigger: AttributeSpec[String] =
        AttributeSpec(
          FaasExperimentalAttributes.FaasTrigger,
          List(
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          faasTrigger,
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
