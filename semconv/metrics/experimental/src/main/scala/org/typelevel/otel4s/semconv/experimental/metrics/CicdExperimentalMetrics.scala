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
object CicdExperimentalMetrics {

  val specs: List[MetricSpec] = List(
    PipelineRunActive,
    PipelineRunDuration,
    PipelineRunErrors,
    SystemErrors,
    WorkerCount,
  )

  /** The number of pipeline runs currently active in the system by state.
    */
  object PipelineRunActive extends MetricSpec.Unsealed {

    val name: String = "cicd.pipeline.run.active"
    val description: String = "The number of pipeline runs currently active in the system by state."
    val unit: String = "{run}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The human readable name of the pipeline within a CI/CD system.
        */
      val cicdPipelineName: AttributeSpec[String] =
        AttributeSpec(
          CicdExperimentalAttributes.CicdPipelineName,
          List(
            "Build and Test",
            "Lint",
            "Deploy Go Project",
            "deploy_to_environment",
          ),
          Requirement.required,
          Stability.development
        )

      /** The pipeline run goes through these states during its lifecycle.
        */
      val cicdPipelineRunState: AttributeSpec[String] =
        AttributeSpec(
          CicdExperimentalAttributes.CicdPipelineRunState,
          List(
            "pending",
            "executing",
            "finalizing",
          ),
          Requirement.required,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          cicdPipelineName,
          cicdPipelineRunState,
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

  /** Duration of a pipeline run grouped by pipeline, state and result.
    */
  object PipelineRunDuration extends MetricSpec.Unsealed {

    val name: String = "cicd.pipeline.run.duration"
    val description: String = "Duration of a pipeline run grouped by pipeline, state and result."
    val unit: String = "s"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The human readable name of the pipeline within a CI/CD system.
        */
      val cicdPipelineName: AttributeSpec[String] =
        AttributeSpec(
          CicdExperimentalAttributes.CicdPipelineName,
          List(
            "Build and Test",
            "Lint",
            "Deploy Go Project",
            "deploy_to_environment",
          ),
          Requirement.required,
          Stability.development
        )

      /** The result of a pipeline run.
        */
      val cicdPipelineResult: AttributeSpec[String] =
        AttributeSpec(
          CicdExperimentalAttributes.CicdPipelineResult,
          List(
            "success",
            "failure",
            "timeout",
            "skipped",
          ),
          Requirement.conditionallyRequired("If and only if the pipeline run result has been set during that state."),
          Stability.development
        )

      /** The pipeline run goes through these states during its lifecycle.
        */
      val cicdPipelineRunState: AttributeSpec[String] =
        AttributeSpec(
          CicdExperimentalAttributes.CicdPipelineRunState,
          List(
            "pending",
            "executing",
            "finalizing",
          ),
          Requirement.required,
          Stability.development
        )

      /** Describes a class of error the operation ended with.
        *
        * @note
        *   <p> The `error.type` SHOULD be predictable, and SHOULD have low cardinality. <p> When `error.type` is set to
        *   a type (e.g., an exception type), its canonical class name identifying the type within the artifact SHOULD
        *   be used. <p> Instrumentations SHOULD document the list of errors they report. <p> The cardinality of
        *   `error.type` within one instrumentation library SHOULD be low. Telemetry consumers that aggregate data from
        *   multiple instrumentation libraries and applications should be prepared for `error.type` to have high
        *   cardinality at query time when no additional filters are applied. <p> If the operation has completed
        *   successfully, instrumentations SHOULD NOT set `error.type`. <p> If a specific domain defines its own set of
        *   error identifiers (such as HTTP or gRPC status codes), it's RECOMMENDED to: <ul> <li>Use a domain-specific
        *   attribute <li>Set `error.type` to capture all errors, regardless of whether they are defined within the
        *   domain-specific set or not. </ul>
        */
      val errorType: AttributeSpec[String] =
        AttributeSpec(
          ErrorAttributes.ErrorType,
          List(
            "timeout",
            "java.net.UnknownHostException",
            "server_certificate_invalid",
            "500",
          ),
          Requirement.conditionallyRequired("If and only if the pipeline run failed."),
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          cicdPipelineName,
          cicdPipelineResult,
          cicdPipelineRunState,
          errorType,
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

  /** The number of errors encountered in pipeline runs (eg. compile, test failures).
    *
    * @note
    *   <p> There might be errors in a pipeline run that are non fatal (eg. they are suppressed) or in a parallel stage
    *   multiple stages could have a fatal error. This means that this error count might not be the same as the count of
    *   metric `cicd.pipeline.run.duration` with run result `failure`.
    */
  object PipelineRunErrors extends MetricSpec.Unsealed {

    val name: String = "cicd.pipeline.run.errors"
    val description: String = "The number of errors encountered in pipeline runs (eg. compile, test failures)."
    val unit: String = "{error}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The human readable name of the pipeline within a CI/CD system.
        */
      val cicdPipelineName: AttributeSpec[String] =
        AttributeSpec(
          CicdExperimentalAttributes.CicdPipelineName,
          List(
            "Build and Test",
            "Lint",
            "Deploy Go Project",
            "deploy_to_environment",
          ),
          Requirement.required,
          Stability.development
        )

      /** Describes a class of error the operation ended with.
        *
        * @note
        *   <p> The `error.type` SHOULD be predictable, and SHOULD have low cardinality. <p> When `error.type` is set to
        *   a type (e.g., an exception type), its canonical class name identifying the type within the artifact SHOULD
        *   be used. <p> Instrumentations SHOULD document the list of errors they report. <p> The cardinality of
        *   `error.type` within one instrumentation library SHOULD be low. Telemetry consumers that aggregate data from
        *   multiple instrumentation libraries and applications should be prepared for `error.type` to have high
        *   cardinality at query time when no additional filters are applied. <p> If the operation has completed
        *   successfully, instrumentations SHOULD NOT set `error.type`. <p> If a specific domain defines its own set of
        *   error identifiers (such as HTTP or gRPC status codes), it's RECOMMENDED to: <ul> <li>Use a domain-specific
        *   attribute <li>Set `error.type` to capture all errors, regardless of whether they are defined within the
        *   domain-specific set or not. </ul>
        */
      val errorType: AttributeSpec[String] =
        AttributeSpec(
          ErrorAttributes.ErrorType,
          List(
            "timeout",
            "java.net.UnknownHostException",
            "server_certificate_invalid",
            "500",
          ),
          Requirement.required,
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          cicdPipelineName,
          errorType,
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

  /** The number of errors in a component of the CICD system (eg. controller, scheduler, agent).
    *
    * @note
    *   <p> Errors in pipeline run execution are explicitly excluded. Ie a test failure is not counted in this metric.
    */
  object SystemErrors extends MetricSpec.Unsealed {

    val name: String = "cicd.system.errors"
    val description: String =
      "The number of errors in a component of the CICD system (eg. controller, scheduler, agent)."
    val unit: String = "{error}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The name of a component of the CICD system.
        */
      val cicdSystemComponent: AttributeSpec[String] =
        AttributeSpec(
          CicdExperimentalAttributes.CicdSystemComponent,
          List(
            "controller",
            "scheduler",
            "agent",
          ),
          Requirement.required,
          Stability.development
        )

      /** Describes a class of error the operation ended with.
        *
        * @note
        *   <p> The `error.type` SHOULD be predictable, and SHOULD have low cardinality. <p> When `error.type` is set to
        *   a type (e.g., an exception type), its canonical class name identifying the type within the artifact SHOULD
        *   be used. <p> Instrumentations SHOULD document the list of errors they report. <p> The cardinality of
        *   `error.type` within one instrumentation library SHOULD be low. Telemetry consumers that aggregate data from
        *   multiple instrumentation libraries and applications should be prepared for `error.type` to have high
        *   cardinality at query time when no additional filters are applied. <p> If the operation has completed
        *   successfully, instrumentations SHOULD NOT set `error.type`. <p> If a specific domain defines its own set of
        *   error identifiers (such as HTTP or gRPC status codes), it's RECOMMENDED to: <ul> <li>Use a domain-specific
        *   attribute <li>Set `error.type` to capture all errors, regardless of whether they are defined within the
        *   domain-specific set or not. </ul>
        */
      val errorType: AttributeSpec[String] =
        AttributeSpec(
          ErrorAttributes.ErrorType,
          List(
            "timeout",
            "java.net.UnknownHostException",
            "server_certificate_invalid",
            "500",
          ),
          Requirement.required,
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          cicdSystemComponent,
          errorType,
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

  /** The number of workers on the CICD system by state.
    */
  object WorkerCount extends MetricSpec.Unsealed {

    val name: String = "cicd.worker.count"
    val description: String = "The number of workers on the CICD system by state."
    val unit: String = "{count}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The state of a CICD worker / agent.
        */
      val cicdWorkerState: AttributeSpec[String] =
        AttributeSpec(
          CicdExperimentalAttributes.CicdWorkerState,
          List(
            "idle",
            "busy",
            "down",
          ),
          Requirement.required,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          cicdWorkerState,
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
