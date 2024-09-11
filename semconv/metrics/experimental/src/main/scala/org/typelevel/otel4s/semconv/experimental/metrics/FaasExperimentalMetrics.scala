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

import org.typelevel.otel4s.metrics._
import org.typelevel.otel4s.semconv.experimental.attributes._

// DO NOT EDIT, this is an Auto-generated file from buildscripts/templates/registry/otel4s/metrics/SemanticMetrics.scala.j2
object FaasExperimentalMetrics {

  /** Number of invocation cold starts
    */
  object Coldstarts {

    val Name = "faas.coldstarts"
    val Description = "Number of invocation cold starts"
    val Unit = "{coldstart}"

    object AttributeSpecs {

      /** Type of the trigger which caused this function invocation.
        */
      val faasTrigger: AttributeSpec[String] =
        AttributeSpec(
          FaasExperimentalAttributes.FaasTrigger,
          List(
          ),
          Requirement.recommended,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          faasTrigger,
        )
    }

    def create[F[_]: Meter]: F[Counter[F, Long]] =
      Meter[F]
        .counter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Distribution of CPU usage per invocation
    */
  object CpuUsage {

    val Name = "faas.cpu_usage"
    val Description = "Distribution of CPU usage per invocation"
    val Unit = "s"

    object AttributeSpecs {

      /** Type of the trigger which caused this function invocation.
        */
      val faasTrigger: AttributeSpec[String] =
        AttributeSpec(
          FaasExperimentalAttributes.FaasTrigger,
          List(
          ),
          Requirement.recommended,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          faasTrigger,
        )
    }

    def create[F[_]: Meter](boundaries: BucketBoundaries): F[Histogram[F, Double]] =
      Meter[F]
        .histogram[Double](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

  /** Number of invocation errors
    */
  object Errors {

    val Name = "faas.errors"
    val Description = "Number of invocation errors"
    val Unit = "{error}"

    object AttributeSpecs {

      /** Type of the trigger which caused this function invocation.
        */
      val faasTrigger: AttributeSpec[String] =
        AttributeSpec(
          FaasExperimentalAttributes.FaasTrigger,
          List(
          ),
          Requirement.recommended,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          faasTrigger,
        )
    }

    def create[F[_]: Meter]: F[Counter[F, Long]] =
      Meter[F]
        .counter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Measures the duration of the function's initialization, such as a cold start
    */
  object InitDuration {

    val Name = "faas.init_duration"
    val Description = "Measures the duration of the function's initialization, such as a cold start"
    val Unit = "s"

    object AttributeSpecs {

      /** Type of the trigger which caused this function invocation.
        */
      val faasTrigger: AttributeSpec[String] =
        AttributeSpec(
          FaasExperimentalAttributes.FaasTrigger,
          List(
          ),
          Requirement.recommended,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          faasTrigger,
        )
    }

    def create[F[_]: Meter](boundaries: BucketBoundaries): F[Histogram[F, Double]] =
      Meter[F]
        .histogram[Double](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

  /** Number of successful invocations
    */
  object Invocations {

    val Name = "faas.invocations"
    val Description = "Number of successful invocations"
    val Unit = "{invocation}"

    object AttributeSpecs {

      /** Type of the trigger which caused this function invocation.
        */
      val faasTrigger: AttributeSpec[String] =
        AttributeSpec(
          FaasExperimentalAttributes.FaasTrigger,
          List(
          ),
          Requirement.recommended,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          faasTrigger,
        )
    }

    def create[F[_]: Meter]: F[Counter[F, Long]] =
      Meter[F]
        .counter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Measures the duration of the function's logic execution
    */
  object InvokeDuration {

    val Name = "faas.invoke_duration"
    val Description = "Measures the duration of the function's logic execution"
    val Unit = "s"

    object AttributeSpecs {

      /** Type of the trigger which caused this function invocation.
        */
      val faasTrigger: AttributeSpec[String] =
        AttributeSpec(
          FaasExperimentalAttributes.FaasTrigger,
          List(
          ),
          Requirement.recommended,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          faasTrigger,
        )
    }

    def create[F[_]: Meter](boundaries: BucketBoundaries): F[Histogram[F, Double]] =
      Meter[F]
        .histogram[Double](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

  /** Distribution of max memory usage per invocation
    */
  object MemUsage {

    val Name = "faas.mem_usage"
    val Description = "Distribution of max memory usage per invocation"
    val Unit = "By"

    object AttributeSpecs {

      /** Type of the trigger which caused this function invocation.
        */
      val faasTrigger: AttributeSpec[String] =
        AttributeSpec(
          FaasExperimentalAttributes.FaasTrigger,
          List(
          ),
          Requirement.recommended,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          faasTrigger,
        )
    }

    def create[F[_]: Meter](boundaries: BucketBoundaries): F[Histogram[F, Double]] =
      Meter[F]
        .histogram[Double](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

  /** Distribution of net I/O usage per invocation
    */
  object NetIo {

    val Name = "faas.net_io"
    val Description = "Distribution of net I/O usage per invocation"
    val Unit = "By"

    object AttributeSpecs {

      /** Type of the trigger which caused this function invocation.
        */
      val faasTrigger: AttributeSpec[String] =
        AttributeSpec(
          FaasExperimentalAttributes.FaasTrigger,
          List(
          ),
          Requirement.recommended,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          faasTrigger,
        )
    }

    def create[F[_]: Meter](boundaries: BucketBoundaries): F[Histogram[F, Double]] =
      Meter[F]
        .histogram[Double](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

  /** Number of invocation timeouts
    */
  object Timeouts {

    val Name = "faas.timeouts"
    val Description = "Number of invocation timeouts"
    val Unit = "{timeout}"

    object AttributeSpecs {

      /** Type of the trigger which caused this function invocation.
        */
      val faasTrigger: AttributeSpec[String] =
        AttributeSpec(
          FaasExperimentalAttributes.FaasTrigger,
          List(
          ),
          Requirement.recommended,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          faasTrigger,
        )
    }

    def create[F[_]: Meter]: F[Counter[F, Long]] =
      Meter[F]
        .counter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

}
