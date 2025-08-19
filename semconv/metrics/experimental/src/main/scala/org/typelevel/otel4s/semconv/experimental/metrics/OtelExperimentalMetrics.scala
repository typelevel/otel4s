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
object OtelExperimentalMetrics {

  @annotation.nowarn("cat=deprecation")
  val specs: List[MetricSpec] = List(
    SdkExporterLogExported,
    SdkExporterLogInflight,
    SdkExporterMetricDataPointExported,
    SdkExporterMetricDataPointInflight,
    SdkExporterOperationDuration,
    SdkExporterSpanExported,
    SdkExporterSpanExportedCount,
    SdkExporterSpanInflight,
    SdkExporterSpanInflightCount,
    SdkLogCreated,
    SdkMetricReaderCollectionDuration,
    SdkProcessorLogProcessed,
    SdkProcessorLogQueueCapacity,
    SdkProcessorLogQueueSize,
    SdkProcessorSpanProcessed,
    SdkProcessorSpanProcessedCount,
    SdkProcessorSpanQueueCapacity,
    SdkProcessorSpanQueueSize,
    SdkSpanEnded,
    SdkSpanEndedCount,
    SdkSpanLive,
    SdkSpanLiveCount,
  )

  /** The number of log records for which the export has finished, either successful or failed
    *
    * @note
    *   <p> For successful exports, `error.type` MUST NOT be set. For failed exports, `error.type` MUST contain the
    *   failure cause. For exporters with partial success semantics (e.g. OTLP with `rejected_log_records`), rejected
    *   log records MUST count as failed and only non-rejected log records count as success. If no rejection reason is
    *   available, `rejected` SHOULD be used as value for `error.type`.
    */
  object SdkExporterLogExported extends MetricSpec.Unsealed {

    val name: String = "otel.sdk.exporter.log.exported"
    val description: String = "The number of log records for which the export has finished, either successful or failed"
    val unit: String = "{log_record}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

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
            "rejected",
            "timeout",
            "500",
            "java.net.UnknownHostException",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** A name uniquely identifying the instance of the OpenTelemetry component within its containing SDK instance.
        *
        * @note
        *   <p> Implementations SHOULD ensure a low cardinality for this attribute, even across application or SDK
        *   restarts. E.g. implementations MUST NOT use UUIDs as values for this attribute. <p> Implementations MAY
        *   achieve these goals by following a `<otel.component.type>/<instance-counter>` pattern, e.g.
        *   `batching_span_processor/0`. Hereby `otel.component.type` refers to the corresponding attribute value of the
        *   component. <p> The value of `instance-counter` MAY be automatically assigned by the component and uniqueness
        *   within the enclosing SDK instance MUST be guaranteed. For example, `<instance-counter>` MAY be implemented
        *   by using a monotonically increasing counter (starting with `0`), which is incremented every time an instance
        *   of the given component type is started. <p> With this implementation, for example the first Batching Span
        *   Processor would have `batching_span_processor/0` as `otel.component.name`, the second one
        *   `batching_span_processor/1` and so on. These values will therefore be reused in the case of an application
        *   restart.
        */
      val otelComponentName: AttributeSpec[String] =
        AttributeSpec(
          OtelExperimentalAttributes.OtelComponentName,
          List(
            "otlp_grpc_span_exporter/0",
            "custom-name",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** A name identifying the type of the OpenTelemetry component.
        *
        * @note
        *   <p> If none of the standardized values apply, implementations SHOULD use the language-defined name of the
        *   type. E.g. for Java the fully qualified classname SHOULD be used in this case.
        */
      val otelComponentType: AttributeSpec[String] =
        AttributeSpec(
          OtelExperimentalAttributes.OtelComponentType,
          List(
            "batching_span_processor",
            "com.example.MySpanExporter",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Server domain name if available without reverse DNS lookup; otherwise, IP address or Unix domain socket name.
        *
        * @note
        *   <p> When observed from the client side, and when communicating through an intermediary, `server.address`
        *   SHOULD represent the server address behind any intermediaries, for example proxies, if it's available.
        */
      val serverAddress: AttributeSpec[String] =
        AttributeSpec(
          ServerAttributes.ServerAddress,
          List(
            "example.com",
            "10.1.2.80",
            "/tmp/my.sock",
          ),
          Requirement.recommended("when applicable"),
          Stability.stable
        )

      /** Server port number.
        *
        * @note
        *   <p> When observed from the client side, and when communicating through an intermediary, `server.port` SHOULD
        *   represent the server port behind any intermediaries, for example proxies, if it's available.
        */
      val serverPort: AttributeSpec[Long] =
        AttributeSpec(
          ServerAttributes.ServerPort,
          List(
            80,
            8080,
            443,
          ),
          Requirement.recommended("when applicable"),
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          errorType,
          otelComponentName,
          otelComponentType,
          serverAddress,
          serverPort,
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

  /** The number of log records which were passed to the exporter, but that have not been exported yet (neither
    * successful, nor failed)
    *
    * @note
    *   <p> For successful exports, `error.type` MUST NOT be set. For failed exports, `error.type` MUST contain the
    *   failure cause.
    */
  object SdkExporterLogInflight extends MetricSpec.Unsealed {

    val name: String = "otel.sdk.exporter.log.inflight"
    val description: String =
      "The number of log records which were passed to the exporter, but that have not been exported yet (neither successful, nor failed)"
    val unit: String = "{log_record}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** A name uniquely identifying the instance of the OpenTelemetry component within its containing SDK instance.
        *
        * @note
        *   <p> Implementations SHOULD ensure a low cardinality for this attribute, even across application or SDK
        *   restarts. E.g. implementations MUST NOT use UUIDs as values for this attribute. <p> Implementations MAY
        *   achieve these goals by following a `<otel.component.type>/<instance-counter>` pattern, e.g.
        *   `batching_span_processor/0`. Hereby `otel.component.type` refers to the corresponding attribute value of the
        *   component. <p> The value of `instance-counter` MAY be automatically assigned by the component and uniqueness
        *   within the enclosing SDK instance MUST be guaranteed. For example, `<instance-counter>` MAY be implemented
        *   by using a monotonically increasing counter (starting with `0`), which is incremented every time an instance
        *   of the given component type is started. <p> With this implementation, for example the first Batching Span
        *   Processor would have `batching_span_processor/0` as `otel.component.name`, the second one
        *   `batching_span_processor/1` and so on. These values will therefore be reused in the case of an application
        *   restart.
        */
      val otelComponentName: AttributeSpec[String] =
        AttributeSpec(
          OtelExperimentalAttributes.OtelComponentName,
          List(
            "otlp_grpc_span_exporter/0",
            "custom-name",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** A name identifying the type of the OpenTelemetry component.
        *
        * @note
        *   <p> If none of the standardized values apply, implementations SHOULD use the language-defined name of the
        *   type. E.g. for Java the fully qualified classname SHOULD be used in this case.
        */
      val otelComponentType: AttributeSpec[String] =
        AttributeSpec(
          OtelExperimentalAttributes.OtelComponentType,
          List(
            "batching_span_processor",
            "com.example.MySpanExporter",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Server domain name if available without reverse DNS lookup; otherwise, IP address or Unix domain socket name.
        *
        * @note
        *   <p> When observed from the client side, and when communicating through an intermediary, `server.address`
        *   SHOULD represent the server address behind any intermediaries, for example proxies, if it's available.
        */
      val serverAddress: AttributeSpec[String] =
        AttributeSpec(
          ServerAttributes.ServerAddress,
          List(
            "example.com",
            "10.1.2.80",
            "/tmp/my.sock",
          ),
          Requirement.recommended("when applicable"),
          Stability.stable
        )

      /** Server port number.
        *
        * @note
        *   <p> When observed from the client side, and when communicating through an intermediary, `server.port` SHOULD
        *   represent the server port behind any intermediaries, for example proxies, if it's available.
        */
      val serverPort: AttributeSpec[Long] =
        AttributeSpec(
          ServerAttributes.ServerPort,
          List(
            80,
            8080,
            443,
          ),
          Requirement.recommended("when applicable"),
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          otelComponentName,
          otelComponentType,
          serverAddress,
          serverPort,
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

  /** The number of metric data points for which the export has finished, either successful or failed
    *
    * @note
    *   <p> For successful exports, `error.type` MUST NOT be set. For failed exports, `error.type` MUST contain the
    *   failure cause. For exporters with partial success semantics (e.g. OTLP with `rejected_data_points`), rejected
    *   data points MUST count as failed and only non-rejected data points count as success. If no rejection reason is
    *   available, `rejected` SHOULD be used as value for `error.type`.
    */
  object SdkExporterMetricDataPointExported extends MetricSpec.Unsealed {

    val name: String = "otel.sdk.exporter.metric_data_point.exported"
    val description: String =
      "The number of metric data points for which the export has finished, either successful or failed"
    val unit: String = "{data_point}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

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
            "rejected",
            "timeout",
            "500",
            "java.net.UnknownHostException",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** A name uniquely identifying the instance of the OpenTelemetry component within its containing SDK instance.
        *
        * @note
        *   <p> Implementations SHOULD ensure a low cardinality for this attribute, even across application or SDK
        *   restarts. E.g. implementations MUST NOT use UUIDs as values for this attribute. <p> Implementations MAY
        *   achieve these goals by following a `<otel.component.type>/<instance-counter>` pattern, e.g.
        *   `batching_span_processor/0`. Hereby `otel.component.type` refers to the corresponding attribute value of the
        *   component. <p> The value of `instance-counter` MAY be automatically assigned by the component and uniqueness
        *   within the enclosing SDK instance MUST be guaranteed. For example, `<instance-counter>` MAY be implemented
        *   by using a monotonically increasing counter (starting with `0`), which is incremented every time an instance
        *   of the given component type is started. <p> With this implementation, for example the first Batching Span
        *   Processor would have `batching_span_processor/0` as `otel.component.name`, the second one
        *   `batching_span_processor/1` and so on. These values will therefore be reused in the case of an application
        *   restart.
        */
      val otelComponentName: AttributeSpec[String] =
        AttributeSpec(
          OtelExperimentalAttributes.OtelComponentName,
          List(
            "otlp_grpc_span_exporter/0",
            "custom-name",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** A name identifying the type of the OpenTelemetry component.
        *
        * @note
        *   <p> If none of the standardized values apply, implementations SHOULD use the language-defined name of the
        *   type. E.g. for Java the fully qualified classname SHOULD be used in this case.
        */
      val otelComponentType: AttributeSpec[String] =
        AttributeSpec(
          OtelExperimentalAttributes.OtelComponentType,
          List(
            "batching_span_processor",
            "com.example.MySpanExporter",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Server domain name if available without reverse DNS lookup; otherwise, IP address or Unix domain socket name.
        *
        * @note
        *   <p> When observed from the client side, and when communicating through an intermediary, `server.address`
        *   SHOULD represent the server address behind any intermediaries, for example proxies, if it's available.
        */
      val serverAddress: AttributeSpec[String] =
        AttributeSpec(
          ServerAttributes.ServerAddress,
          List(
            "example.com",
            "10.1.2.80",
            "/tmp/my.sock",
          ),
          Requirement.recommended("when applicable"),
          Stability.stable
        )

      /** Server port number.
        *
        * @note
        *   <p> When observed from the client side, and when communicating through an intermediary, `server.port` SHOULD
        *   represent the server port behind any intermediaries, for example proxies, if it's available.
        */
      val serverPort: AttributeSpec[Long] =
        AttributeSpec(
          ServerAttributes.ServerPort,
          List(
            80,
            8080,
            443,
          ),
          Requirement.recommended("when applicable"),
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          errorType,
          otelComponentName,
          otelComponentType,
          serverAddress,
          serverPort,
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

  /** The number of metric data points which were passed to the exporter, but that have not been exported yet (neither
    * successful, nor failed)
    *
    * @note
    *   <p> For successful exports, `error.type` MUST NOT be set. For failed exports, `error.type` MUST contain the
    *   failure cause.
    */
  object SdkExporterMetricDataPointInflight extends MetricSpec.Unsealed {

    val name: String = "otel.sdk.exporter.metric_data_point.inflight"
    val description: String =
      "The number of metric data points which were passed to the exporter, but that have not been exported yet (neither successful, nor failed)"
    val unit: String = "{data_point}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** A name uniquely identifying the instance of the OpenTelemetry component within its containing SDK instance.
        *
        * @note
        *   <p> Implementations SHOULD ensure a low cardinality for this attribute, even across application or SDK
        *   restarts. E.g. implementations MUST NOT use UUIDs as values for this attribute. <p> Implementations MAY
        *   achieve these goals by following a `<otel.component.type>/<instance-counter>` pattern, e.g.
        *   `batching_span_processor/0`. Hereby `otel.component.type` refers to the corresponding attribute value of the
        *   component. <p> The value of `instance-counter` MAY be automatically assigned by the component and uniqueness
        *   within the enclosing SDK instance MUST be guaranteed. For example, `<instance-counter>` MAY be implemented
        *   by using a monotonically increasing counter (starting with `0`), which is incremented every time an instance
        *   of the given component type is started. <p> With this implementation, for example the first Batching Span
        *   Processor would have `batching_span_processor/0` as `otel.component.name`, the second one
        *   `batching_span_processor/1` and so on. These values will therefore be reused in the case of an application
        *   restart.
        */
      val otelComponentName: AttributeSpec[String] =
        AttributeSpec(
          OtelExperimentalAttributes.OtelComponentName,
          List(
            "otlp_grpc_span_exporter/0",
            "custom-name",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** A name identifying the type of the OpenTelemetry component.
        *
        * @note
        *   <p> If none of the standardized values apply, implementations SHOULD use the language-defined name of the
        *   type. E.g. for Java the fully qualified classname SHOULD be used in this case.
        */
      val otelComponentType: AttributeSpec[String] =
        AttributeSpec(
          OtelExperimentalAttributes.OtelComponentType,
          List(
            "batching_span_processor",
            "com.example.MySpanExporter",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Server domain name if available without reverse DNS lookup; otherwise, IP address or Unix domain socket name.
        *
        * @note
        *   <p> When observed from the client side, and when communicating through an intermediary, `server.address`
        *   SHOULD represent the server address behind any intermediaries, for example proxies, if it's available.
        */
      val serverAddress: AttributeSpec[String] =
        AttributeSpec(
          ServerAttributes.ServerAddress,
          List(
            "example.com",
            "10.1.2.80",
            "/tmp/my.sock",
          ),
          Requirement.recommended("when applicable"),
          Stability.stable
        )

      /** Server port number.
        *
        * @note
        *   <p> When observed from the client side, and when communicating through an intermediary, `server.port` SHOULD
        *   represent the server port behind any intermediaries, for example proxies, if it's available.
        */
      val serverPort: AttributeSpec[Long] =
        AttributeSpec(
          ServerAttributes.ServerPort,
          List(
            80,
            8080,
            443,
          ),
          Requirement.recommended("when applicable"),
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          otelComponentName,
          otelComponentType,
          serverAddress,
          serverPort,
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

  /** The duration of exporting a batch of telemetry records.
    *
    * @note
    *   <p> This metric defines successful operations using the full success definitions for <a
    *   href="https://github.com/open-telemetry/opentelemetry-proto/blob/v1.5.0/docs/specification.md#full-success-1">http</a>
    *   and <a
    *   href="https://github.com/open-telemetry/opentelemetry-proto/blob/v1.5.0/docs/specification.md#full-success">grpc</a>.
    *   Anything else is defined as an unsuccessful operation. For successful operations, `error.type` MUST NOT be set.
    *   For unsuccessful export operations, `error.type` MUST contain a relevant failure cause.
    */
  object SdkExporterOperationDuration extends MetricSpec.Unsealed {

    val name: String = "otel.sdk.exporter.operation.duration"
    val description: String = "The duration of exporting a batch of telemetry records."
    val unit: String = "s"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

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
            "rejected",
            "timeout",
            "500",
            "java.net.UnknownHostException",
          ),
          Requirement.conditionallyRequired("If operation has ended with an error"),
          Stability.stable
        )

      /** The HTTP status code of the last HTTP request performed in scope of this export call.
        */
      val httpResponseStatusCode: AttributeSpec[Long] =
        AttributeSpec(
          HttpAttributes.HttpResponseStatusCode,
          List(
            200,
          ),
          Requirement.recommended("when applicable"),
          Stability.stable
        )

      /** A name uniquely identifying the instance of the OpenTelemetry component within its containing SDK instance.
        *
        * @note
        *   <p> Implementations SHOULD ensure a low cardinality for this attribute, even across application or SDK
        *   restarts. E.g. implementations MUST NOT use UUIDs as values for this attribute. <p> Implementations MAY
        *   achieve these goals by following a `<otel.component.type>/<instance-counter>` pattern, e.g.
        *   `batching_span_processor/0`. Hereby `otel.component.type` refers to the corresponding attribute value of the
        *   component. <p> The value of `instance-counter` MAY be automatically assigned by the component and uniqueness
        *   within the enclosing SDK instance MUST be guaranteed. For example, `<instance-counter>` MAY be implemented
        *   by using a monotonically increasing counter (starting with `0`), which is incremented every time an instance
        *   of the given component type is started. <p> With this implementation, for example the first Batching Span
        *   Processor would have `batching_span_processor/0` as `otel.component.name`, the second one
        *   `batching_span_processor/1` and so on. These values will therefore be reused in the case of an application
        *   restart.
        */
      val otelComponentName: AttributeSpec[String] =
        AttributeSpec(
          OtelExperimentalAttributes.OtelComponentName,
          List(
            "otlp_grpc_span_exporter/0",
            "custom-name",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** A name identifying the type of the OpenTelemetry component.
        *
        * @note
        *   <p> If none of the standardized values apply, implementations SHOULD use the language-defined name of the
        *   type. E.g. for Java the fully qualified classname SHOULD be used in this case.
        */
      val otelComponentType: AttributeSpec[String] =
        AttributeSpec(
          OtelExperimentalAttributes.OtelComponentType,
          List(
            "otlp_grpc_span_exporter",
            "com.example.MySpanExporter",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The gRPC status code of the last gRPC requests performed in scope of this export call.
        */
      val rpcGrpcStatusCode: AttributeSpec[Long] =
        AttributeSpec(
          RpcExperimentalAttributes.RpcGrpcStatusCode,
          List(
          ),
          Requirement.recommended("when applicable"),
          Stability.development
        )

      /** Server domain name if available without reverse DNS lookup; otherwise, IP address or Unix domain socket name.
        *
        * @note
        *   <p> When observed from the client side, and when communicating through an intermediary, `server.address`
        *   SHOULD represent the server address behind any intermediaries, for example proxies, if it's available.
        */
      val serverAddress: AttributeSpec[String] =
        AttributeSpec(
          ServerAttributes.ServerAddress,
          List(
            "example.com",
            "10.1.2.80",
            "/tmp/my.sock",
          ),
          Requirement.recommended("when applicable"),
          Stability.stable
        )

      /** Server port number.
        *
        * @note
        *   <p> When observed from the client side, and when communicating through an intermediary, `server.port` SHOULD
        *   represent the server port behind any intermediaries, for example proxies, if it's available.
        */
      val serverPort: AttributeSpec[Long] =
        AttributeSpec(
          ServerAttributes.ServerPort,
          List(
            80,
            8080,
            443,
          ),
          Requirement.recommended("when applicable"),
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          errorType,
          httpResponseStatusCode,
          otelComponentName,
          otelComponentType,
          rpcGrpcStatusCode,
          serverAddress,
          serverPort,
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

  /** The number of spans for which the export has finished, either successful or failed
    *
    * @note
    *   <p> For successful exports, `error.type` MUST NOT be set. For failed exports, `error.type` MUST contain the
    *   failure cause. For exporters with partial success semantics (e.g. OTLP with `rejected_spans`), rejected spans
    *   MUST count as failed and only non-rejected spans count as success. If no rejection reason is available,
    *   `rejected` SHOULD be used as value for `error.type`.
    */
  object SdkExporterSpanExported extends MetricSpec.Unsealed {

    val name: String = "otel.sdk.exporter.span.exported"
    val description: String = "The number of spans for which the export has finished, either successful or failed"
    val unit: String = "{span}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

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
            "rejected",
            "timeout",
            "500",
            "java.net.UnknownHostException",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** A name uniquely identifying the instance of the OpenTelemetry component within its containing SDK instance.
        *
        * @note
        *   <p> Implementations SHOULD ensure a low cardinality for this attribute, even across application or SDK
        *   restarts. E.g. implementations MUST NOT use UUIDs as values for this attribute. <p> Implementations MAY
        *   achieve these goals by following a `<otel.component.type>/<instance-counter>` pattern, e.g.
        *   `batching_span_processor/0`. Hereby `otel.component.type` refers to the corresponding attribute value of the
        *   component. <p> The value of `instance-counter` MAY be automatically assigned by the component and uniqueness
        *   within the enclosing SDK instance MUST be guaranteed. For example, `<instance-counter>` MAY be implemented
        *   by using a monotonically increasing counter (starting with `0`), which is incremented every time an instance
        *   of the given component type is started. <p> With this implementation, for example the first Batching Span
        *   Processor would have `batching_span_processor/0` as `otel.component.name`, the second one
        *   `batching_span_processor/1` and so on. These values will therefore be reused in the case of an application
        *   restart.
        */
      val otelComponentName: AttributeSpec[String] =
        AttributeSpec(
          OtelExperimentalAttributes.OtelComponentName,
          List(
            "otlp_grpc_span_exporter/0",
            "custom-name",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** A name identifying the type of the OpenTelemetry component.
        *
        * @note
        *   <p> If none of the standardized values apply, implementations SHOULD use the language-defined name of the
        *   type. E.g. for Java the fully qualified classname SHOULD be used in this case.
        */
      val otelComponentType: AttributeSpec[String] =
        AttributeSpec(
          OtelExperimentalAttributes.OtelComponentType,
          List(
            "batching_span_processor",
            "com.example.MySpanExporter",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Server domain name if available without reverse DNS lookup; otherwise, IP address or Unix domain socket name.
        *
        * @note
        *   <p> When observed from the client side, and when communicating through an intermediary, `server.address`
        *   SHOULD represent the server address behind any intermediaries, for example proxies, if it's available.
        */
      val serverAddress: AttributeSpec[String] =
        AttributeSpec(
          ServerAttributes.ServerAddress,
          List(
            "example.com",
            "10.1.2.80",
            "/tmp/my.sock",
          ),
          Requirement.recommended("when applicable"),
          Stability.stable
        )

      /** Server port number.
        *
        * @note
        *   <p> When observed from the client side, and when communicating through an intermediary, `server.port` SHOULD
        *   represent the server port behind any intermediaries, for example proxies, if it's available.
        */
      val serverPort: AttributeSpec[Long] =
        AttributeSpec(
          ServerAttributes.ServerPort,
          List(
            80,
            8080,
            443,
          ),
          Requirement.recommended("when applicable"),
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          errorType,
          otelComponentName,
          otelComponentType,
          serverAddress,
          serverPort,
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

  /** Deprecated, use `otel.sdk.exporter.span.exported` instead.
    */
  @deprecated("Replaced by `otel.sdk.exporter.span.exported`.", "")
  object SdkExporterSpanExportedCount extends MetricSpec.Unsealed {

    val name: String = "otel.sdk.exporter.span.exported.count"
    val description: String = "Deprecated, use `otel.sdk.exporter.span.exported` instead."
    val unit: String = "{span}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = Nil

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

  /** The number of spans which were passed to the exporter, but that have not been exported yet (neither successful,
    * nor failed)
    *
    * @note
    *   <p> For successful exports, `error.type` MUST NOT be set. For failed exports, `error.type` MUST contain the
    *   failure cause.
    */
  object SdkExporterSpanInflight extends MetricSpec.Unsealed {

    val name: String = "otel.sdk.exporter.span.inflight"
    val description: String =
      "The number of spans which were passed to the exporter, but that have not been exported yet (neither successful, nor failed)"
    val unit: String = "{span}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** A name uniquely identifying the instance of the OpenTelemetry component within its containing SDK instance.
        *
        * @note
        *   <p> Implementations SHOULD ensure a low cardinality for this attribute, even across application or SDK
        *   restarts. E.g. implementations MUST NOT use UUIDs as values for this attribute. <p> Implementations MAY
        *   achieve these goals by following a `<otel.component.type>/<instance-counter>` pattern, e.g.
        *   `batching_span_processor/0`. Hereby `otel.component.type` refers to the corresponding attribute value of the
        *   component. <p> The value of `instance-counter` MAY be automatically assigned by the component and uniqueness
        *   within the enclosing SDK instance MUST be guaranteed. For example, `<instance-counter>` MAY be implemented
        *   by using a monotonically increasing counter (starting with `0`), which is incremented every time an instance
        *   of the given component type is started. <p> With this implementation, for example the first Batching Span
        *   Processor would have `batching_span_processor/0` as `otel.component.name`, the second one
        *   `batching_span_processor/1` and so on. These values will therefore be reused in the case of an application
        *   restart.
        */
      val otelComponentName: AttributeSpec[String] =
        AttributeSpec(
          OtelExperimentalAttributes.OtelComponentName,
          List(
            "otlp_grpc_span_exporter/0",
            "custom-name",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** A name identifying the type of the OpenTelemetry component.
        *
        * @note
        *   <p> If none of the standardized values apply, implementations SHOULD use the language-defined name of the
        *   type. E.g. for Java the fully qualified classname SHOULD be used in this case.
        */
      val otelComponentType: AttributeSpec[String] =
        AttributeSpec(
          OtelExperimentalAttributes.OtelComponentType,
          List(
            "batching_span_processor",
            "com.example.MySpanExporter",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** Server domain name if available without reverse DNS lookup; otherwise, IP address or Unix domain socket name.
        *
        * @note
        *   <p> When observed from the client side, and when communicating through an intermediary, `server.address`
        *   SHOULD represent the server address behind any intermediaries, for example proxies, if it's available.
        */
      val serverAddress: AttributeSpec[String] =
        AttributeSpec(
          ServerAttributes.ServerAddress,
          List(
            "example.com",
            "10.1.2.80",
            "/tmp/my.sock",
          ),
          Requirement.recommended("when applicable"),
          Stability.stable
        )

      /** Server port number.
        *
        * @note
        *   <p> When observed from the client side, and when communicating through an intermediary, `server.port` SHOULD
        *   represent the server port behind any intermediaries, for example proxies, if it's available.
        */
      val serverPort: AttributeSpec[Long] =
        AttributeSpec(
          ServerAttributes.ServerPort,
          List(
            80,
            8080,
            443,
          ),
          Requirement.recommended("when applicable"),
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          otelComponentName,
          otelComponentType,
          serverAddress,
          serverPort,
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

  /** Deprecated, use `otel.sdk.exporter.span.inflight` instead.
    */
  @deprecated("Replaced by `otel.sdk.exporter.span.inflight`.", "")
  object SdkExporterSpanInflightCount extends MetricSpec.Unsealed {

    val name: String = "otel.sdk.exporter.span.inflight.count"
    val description: String = "Deprecated, use `otel.sdk.exporter.span.inflight` instead."
    val unit: String = "{span}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = Nil

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

  /** The number of logs submitted to enabled SDK Loggers
    */
  object SdkLogCreated extends MetricSpec.Unsealed {

    val name: String = "otel.sdk.log.created"
    val description: String = "The number of logs submitted to enabled SDK Loggers"
    val unit: String = "{log_record}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = Nil

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

  /** The duration of the collect operation of the metric reader.
    *
    * @note
    *   <p> For successful collections, `error.type` MUST NOT be set. For failed collections, `error.type` SHOULD
    *   contain the failure cause. It can happen that metrics collection is successful for some MetricProducers, while
    *   others fail. In that case `error.type` SHOULD be set to any of the failure causes.
    */
  object SdkMetricReaderCollectionDuration extends MetricSpec.Unsealed {

    val name: String = "otel.sdk.metric_reader.collection.duration"
    val description: String = "The duration of the collect operation of the metric reader."
    val unit: String = "s"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

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
          Requirement.recommended,
          Stability.stable
        )

      /** A name uniquely identifying the instance of the OpenTelemetry component within its containing SDK instance.
        *
        * @note
        *   <p> Implementations SHOULD ensure a low cardinality for this attribute, even across application or SDK
        *   restarts. E.g. implementations MUST NOT use UUIDs as values for this attribute. <p> Implementations MAY
        *   achieve these goals by following a `<otel.component.type>/<instance-counter>` pattern, e.g.
        *   `batching_span_processor/0`. Hereby `otel.component.type` refers to the corresponding attribute value of the
        *   component. <p> The value of `instance-counter` MAY be automatically assigned by the component and uniqueness
        *   within the enclosing SDK instance MUST be guaranteed. For example, `<instance-counter>` MAY be implemented
        *   by using a monotonically increasing counter (starting with `0`), which is incremented every time an instance
        *   of the given component type is started. <p> With this implementation, for example the first Batching Span
        *   Processor would have `batching_span_processor/0` as `otel.component.name`, the second one
        *   `batching_span_processor/1` and so on. These values will therefore be reused in the case of an application
        *   restart.
        */
      val otelComponentName: AttributeSpec[String] =
        AttributeSpec(
          OtelExperimentalAttributes.OtelComponentName,
          List(
            "otlp_grpc_span_exporter/0",
            "custom-name",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** A name identifying the type of the OpenTelemetry component.
        *
        * @note
        *   <p> If none of the standardized values apply, implementations SHOULD use the language-defined name of the
        *   type. E.g. for Java the fully qualified classname SHOULD be used in this case.
        */
      val otelComponentType: AttributeSpec[String] =
        AttributeSpec(
          OtelExperimentalAttributes.OtelComponentType,
          List(
            "batching_span_processor",
            "com.example.MySpanExporter",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          errorType,
          otelComponentName,
          otelComponentType,
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

  /** The number of log records for which the processing has finished, either successful or failed
    *
    * @note
    *   <p> For successful processing, `error.type` MUST NOT be set. For failed processing, `error.type` MUST contain
    *   the failure cause. For the SDK Simple and Batching Log Record Processor a log record is considered to be
    *   processed already when it has been submitted to the exporter, not when the corresponding export call has
    *   finished.
    */
  object SdkProcessorLogProcessed extends MetricSpec.Unsealed {

    val name: String = "otel.sdk.processor.log.processed"
    val description: String =
      "The number of log records for which the processing has finished, either successful or failed"
    val unit: String = "{log_record}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** A low-cardinality description of the failure reason. SDK Batching Log Record Processors MUST use `queue_full`
        * for log records dropped due to a full queue.
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
            "queue_full",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** A name uniquely identifying the instance of the OpenTelemetry component within its containing SDK instance.
        *
        * @note
        *   <p> Implementations SHOULD ensure a low cardinality for this attribute, even across application or SDK
        *   restarts. E.g. implementations MUST NOT use UUIDs as values for this attribute. <p> Implementations MAY
        *   achieve these goals by following a `<otel.component.type>/<instance-counter>` pattern, e.g.
        *   `batching_span_processor/0`. Hereby `otel.component.type` refers to the corresponding attribute value of the
        *   component. <p> The value of `instance-counter` MAY be automatically assigned by the component and uniqueness
        *   within the enclosing SDK instance MUST be guaranteed. For example, `<instance-counter>` MAY be implemented
        *   by using a monotonically increasing counter (starting with `0`), which is incremented every time an instance
        *   of the given component type is started. <p> With this implementation, for example the first Batching Span
        *   Processor would have `batching_span_processor/0` as `otel.component.name`, the second one
        *   `batching_span_processor/1` and so on. These values will therefore be reused in the case of an application
        *   restart.
        */
      val otelComponentName: AttributeSpec[String] =
        AttributeSpec(
          OtelExperimentalAttributes.OtelComponentName,
          List(
            "otlp_grpc_span_exporter/0",
            "custom-name",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** A name identifying the type of the OpenTelemetry component.
        *
        * @note
        *   <p> If none of the standardized values apply, implementations SHOULD use the language-defined name of the
        *   type. E.g. for Java the fully qualified classname SHOULD be used in this case.
        */
      val otelComponentType: AttributeSpec[String] =
        AttributeSpec(
          OtelExperimentalAttributes.OtelComponentType,
          List(
            "batching_span_processor",
            "com.example.MySpanExporter",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          errorType,
          otelComponentName,
          otelComponentType,
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

  /** The maximum number of log records the queue of a given instance of an SDK Log Record processor can hold
    *
    * @note
    *   <p> Only applies to Log Record processors which use a queue, e.g. the SDK Batching Log Record Processor.
    */
  object SdkProcessorLogQueueCapacity extends MetricSpec.Unsealed {

    val name: String = "otel.sdk.processor.log.queue.capacity"
    val description: String =
      "The maximum number of log records the queue of a given instance of an SDK Log Record processor can hold"
    val unit: String = "{log_record}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** A name uniquely identifying the instance of the OpenTelemetry component within its containing SDK instance.
        *
        * @note
        *   <p> Implementations SHOULD ensure a low cardinality for this attribute, even across application or SDK
        *   restarts. E.g. implementations MUST NOT use UUIDs as values for this attribute. <p> Implementations MAY
        *   achieve these goals by following a `<otel.component.type>/<instance-counter>` pattern, e.g.
        *   `batching_span_processor/0`. Hereby `otel.component.type` refers to the corresponding attribute value of the
        *   component. <p> The value of `instance-counter` MAY be automatically assigned by the component and uniqueness
        *   within the enclosing SDK instance MUST be guaranteed. For example, `<instance-counter>` MAY be implemented
        *   by using a monotonically increasing counter (starting with `0`), which is incremented every time an instance
        *   of the given component type is started. <p> With this implementation, for example the first Batching Span
        *   Processor would have `batching_span_processor/0` as `otel.component.name`, the second one
        *   `batching_span_processor/1` and so on. These values will therefore be reused in the case of an application
        *   restart.
        */
      val otelComponentName: AttributeSpec[String] =
        AttributeSpec(
          OtelExperimentalAttributes.OtelComponentName,
          List(
            "otlp_grpc_span_exporter/0",
            "custom-name",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** A name identifying the type of the OpenTelemetry component.
        *
        * @note
        *   <p> If none of the standardized values apply, implementations SHOULD use the language-defined name of the
        *   type. E.g. for Java the fully qualified classname SHOULD be used in this case.
        */
      val otelComponentType: AttributeSpec[String] =
        AttributeSpec(
          OtelExperimentalAttributes.OtelComponentType,
          List(
            "batching_span_processor",
            "com.example.MySpanExporter",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          otelComponentName,
          otelComponentType,
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

  /** The number of log records in the queue of a given instance of an SDK log processor
    *
    * @note
    *   <p> Only applies to log record processors which use a queue, e.g. the SDK Batching Log Record Processor.
    */
  object SdkProcessorLogQueueSize extends MetricSpec.Unsealed {

    val name: String = "otel.sdk.processor.log.queue.size"
    val description: String = "The number of log records in the queue of a given instance of an SDK log processor"
    val unit: String = "{log_record}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** A name uniquely identifying the instance of the OpenTelemetry component within its containing SDK instance.
        *
        * @note
        *   <p> Implementations SHOULD ensure a low cardinality for this attribute, even across application or SDK
        *   restarts. E.g. implementations MUST NOT use UUIDs as values for this attribute. <p> Implementations MAY
        *   achieve these goals by following a `<otel.component.type>/<instance-counter>` pattern, e.g.
        *   `batching_span_processor/0`. Hereby `otel.component.type` refers to the corresponding attribute value of the
        *   component. <p> The value of `instance-counter` MAY be automatically assigned by the component and uniqueness
        *   within the enclosing SDK instance MUST be guaranteed. For example, `<instance-counter>` MAY be implemented
        *   by using a monotonically increasing counter (starting with `0`), which is incremented every time an instance
        *   of the given component type is started. <p> With this implementation, for example the first Batching Span
        *   Processor would have `batching_span_processor/0` as `otel.component.name`, the second one
        *   `batching_span_processor/1` and so on. These values will therefore be reused in the case of an application
        *   restart.
        */
      val otelComponentName: AttributeSpec[String] =
        AttributeSpec(
          OtelExperimentalAttributes.OtelComponentName,
          List(
            "otlp_grpc_span_exporter/0",
            "custom-name",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** A name identifying the type of the OpenTelemetry component.
        *
        * @note
        *   <p> If none of the standardized values apply, implementations SHOULD use the language-defined name of the
        *   type. E.g. for Java the fully qualified classname SHOULD be used in this case.
        */
      val otelComponentType: AttributeSpec[String] =
        AttributeSpec(
          OtelExperimentalAttributes.OtelComponentType,
          List(
            "batching_span_processor",
            "com.example.MySpanExporter",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          otelComponentName,
          otelComponentType,
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

  /** The number of spans for which the processing has finished, either successful or failed
    *
    * @note
    *   <p> For successful processing, `error.type` MUST NOT be set. For failed processing, `error.type` MUST contain
    *   the failure cause. For the SDK Simple and Batching Span Processor a span is considered to be processed already
    *   when it has been submitted to the exporter, not when the corresponding export call has finished.
    */
  object SdkProcessorSpanProcessed extends MetricSpec.Unsealed {

    val name: String = "otel.sdk.processor.span.processed"
    val description: String = "The number of spans for which the processing has finished, either successful or failed"
    val unit: String = "{span}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** A low-cardinality description of the failure reason. SDK Batching Span Processors MUST use `queue_full` for
        * spans dropped due to a full queue.
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
            "queue_full",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** A name uniquely identifying the instance of the OpenTelemetry component within its containing SDK instance.
        *
        * @note
        *   <p> Implementations SHOULD ensure a low cardinality for this attribute, even across application or SDK
        *   restarts. E.g. implementations MUST NOT use UUIDs as values for this attribute. <p> Implementations MAY
        *   achieve these goals by following a `<otel.component.type>/<instance-counter>` pattern, e.g.
        *   `batching_span_processor/0`. Hereby `otel.component.type` refers to the corresponding attribute value of the
        *   component. <p> The value of `instance-counter` MAY be automatically assigned by the component and uniqueness
        *   within the enclosing SDK instance MUST be guaranteed. For example, `<instance-counter>` MAY be implemented
        *   by using a monotonically increasing counter (starting with `0`), which is incremented every time an instance
        *   of the given component type is started. <p> With this implementation, for example the first Batching Span
        *   Processor would have `batching_span_processor/0` as `otel.component.name`, the second one
        *   `batching_span_processor/1` and so on. These values will therefore be reused in the case of an application
        *   restart.
        */
      val otelComponentName: AttributeSpec[String] =
        AttributeSpec(
          OtelExperimentalAttributes.OtelComponentName,
          List(
            "otlp_grpc_span_exporter/0",
            "custom-name",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** A name identifying the type of the OpenTelemetry component.
        *
        * @note
        *   <p> If none of the standardized values apply, implementations SHOULD use the language-defined name of the
        *   type. E.g. for Java the fully qualified classname SHOULD be used in this case.
        */
      val otelComponentType: AttributeSpec[String] =
        AttributeSpec(
          OtelExperimentalAttributes.OtelComponentType,
          List(
            "batching_span_processor",
            "com.example.MySpanExporter",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          errorType,
          otelComponentName,
          otelComponentType,
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

  /** Deprecated, use `otel.sdk.processor.span.processed` instead.
    */
  @deprecated("Replaced by `otel.sdk.processor.span.processed`.", "")
  object SdkProcessorSpanProcessedCount extends MetricSpec.Unsealed {

    val name: String = "otel.sdk.processor.span.processed.count"
    val description: String = "Deprecated, use `otel.sdk.processor.span.processed` instead."
    val unit: String = "{span}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = Nil

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

  /** The maximum number of spans the queue of a given instance of an SDK span processor can hold
    *
    * @note
    *   <p> Only applies to span processors which use a queue, e.g. the SDK Batching Span Processor.
    */
  object SdkProcessorSpanQueueCapacity extends MetricSpec.Unsealed {

    val name: String = "otel.sdk.processor.span.queue.capacity"
    val description: String =
      "The maximum number of spans the queue of a given instance of an SDK span processor can hold"
    val unit: String = "{span}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** A name uniquely identifying the instance of the OpenTelemetry component within its containing SDK instance.
        *
        * @note
        *   <p> Implementations SHOULD ensure a low cardinality for this attribute, even across application or SDK
        *   restarts. E.g. implementations MUST NOT use UUIDs as values for this attribute. <p> Implementations MAY
        *   achieve these goals by following a `<otel.component.type>/<instance-counter>` pattern, e.g.
        *   `batching_span_processor/0`. Hereby `otel.component.type` refers to the corresponding attribute value of the
        *   component. <p> The value of `instance-counter` MAY be automatically assigned by the component and uniqueness
        *   within the enclosing SDK instance MUST be guaranteed. For example, `<instance-counter>` MAY be implemented
        *   by using a monotonically increasing counter (starting with `0`), which is incremented every time an instance
        *   of the given component type is started. <p> With this implementation, for example the first Batching Span
        *   Processor would have `batching_span_processor/0` as `otel.component.name`, the second one
        *   `batching_span_processor/1` and so on. These values will therefore be reused in the case of an application
        *   restart.
        */
      val otelComponentName: AttributeSpec[String] =
        AttributeSpec(
          OtelExperimentalAttributes.OtelComponentName,
          List(
            "otlp_grpc_span_exporter/0",
            "custom-name",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** A name identifying the type of the OpenTelemetry component.
        *
        * @note
        *   <p> If none of the standardized values apply, implementations SHOULD use the language-defined name of the
        *   type. E.g. for Java the fully qualified classname SHOULD be used in this case.
        */
      val otelComponentType: AttributeSpec[String] =
        AttributeSpec(
          OtelExperimentalAttributes.OtelComponentType,
          List(
            "batching_span_processor",
            "com.example.MySpanExporter",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          otelComponentName,
          otelComponentType,
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

  /** The number of spans in the queue of a given instance of an SDK span processor
    *
    * @note
    *   <p> Only applies to span processors which use a queue, e.g. the SDK Batching Span Processor.
    */
  object SdkProcessorSpanQueueSize extends MetricSpec.Unsealed {

    val name: String = "otel.sdk.processor.span.queue.size"
    val description: String = "The number of spans in the queue of a given instance of an SDK span processor"
    val unit: String = "{span}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** A name uniquely identifying the instance of the OpenTelemetry component within its containing SDK instance.
        *
        * @note
        *   <p> Implementations SHOULD ensure a low cardinality for this attribute, even across application or SDK
        *   restarts. E.g. implementations MUST NOT use UUIDs as values for this attribute. <p> Implementations MAY
        *   achieve these goals by following a `<otel.component.type>/<instance-counter>` pattern, e.g.
        *   `batching_span_processor/0`. Hereby `otel.component.type` refers to the corresponding attribute value of the
        *   component. <p> The value of `instance-counter` MAY be automatically assigned by the component and uniqueness
        *   within the enclosing SDK instance MUST be guaranteed. For example, `<instance-counter>` MAY be implemented
        *   by using a monotonically increasing counter (starting with `0`), which is incremented every time an instance
        *   of the given component type is started. <p> With this implementation, for example the first Batching Span
        *   Processor would have `batching_span_processor/0` as `otel.component.name`, the second one
        *   `batching_span_processor/1` and so on. These values will therefore be reused in the case of an application
        *   restart.
        */
      val otelComponentName: AttributeSpec[String] =
        AttributeSpec(
          OtelExperimentalAttributes.OtelComponentName,
          List(
            "otlp_grpc_span_exporter/0",
            "custom-name",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** A name identifying the type of the OpenTelemetry component.
        *
        * @note
        *   <p> If none of the standardized values apply, implementations SHOULD use the language-defined name of the
        *   type. E.g. for Java the fully qualified classname SHOULD be used in this case.
        */
      val otelComponentType: AttributeSpec[String] =
        AttributeSpec(
          OtelExperimentalAttributes.OtelComponentType,
          List(
            "batching_span_processor",
            "com.example.MySpanExporter",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          otelComponentName,
          otelComponentType,
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

  /** The number of created spans for which the end operation was called
    *
    * @note
    *   <p> For spans with `recording=true`: Implementations MUST record both `otel.sdk.span.live` and
    *   `otel.sdk.span.ended`. For spans with `recording=false`: If implementations decide to record this metric, they
    *   MUST also record `otel.sdk.span.live`.
    */
  object SdkSpanEnded extends MetricSpec.Unsealed {

    val name: String = "otel.sdk.span.ended"
    val description: String = "The number of created spans for which the end operation was called"
    val unit: String = "{span}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The result value of the sampler for this span
        */
      val otelSpanSamplingResult: AttributeSpec[String] =
        AttributeSpec(
          OtelExperimentalAttributes.OtelSpanSamplingResult,
          List(
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          otelSpanSamplingResult,
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

  /** Deprecated, use `otel.sdk.span.ended` instead.
    */
  @deprecated("Replaced by `otel.sdk.span.ended`.", "")
  object SdkSpanEndedCount extends MetricSpec.Unsealed {

    val name: String = "otel.sdk.span.ended.count"
    val description: String = "Deprecated, use `otel.sdk.span.ended` instead."
    val unit: String = "{span}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = Nil

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

  /** The number of created spans for which the end operation has not been called yet
    *
    * @note
    *   <p> For spans with `recording=true`: Implementations MUST record both `otel.sdk.span.live` and
    *   `otel.sdk.span.ended`. For spans with `recording=false`: If implementations decide to record this metric, they
    *   MUST also record `otel.sdk.span.ended`.
    */
  object SdkSpanLive extends MetricSpec.Unsealed {

    val name: String = "otel.sdk.span.live"
    val description: String = "The number of created spans for which the end operation has not been called yet"
    val unit: String = "{span}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The result value of the sampler for this span
        */
      val otelSpanSamplingResult: AttributeSpec[String] =
        AttributeSpec(
          OtelExperimentalAttributes.OtelSpanSamplingResult,
          List(
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          otelSpanSamplingResult,
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

  /** Deprecated, use `otel.sdk.span.live` instead.
    */
  @deprecated("Replaced by `otel.sdk.span.live`.", "")
  object SdkSpanLiveCount extends MetricSpec.Unsealed {

    val name: String = "otel.sdk.span.live.count"
    val description: String = "Deprecated, use `otel.sdk.span.live` instead."
    val unit: String = "{span}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = Nil

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
