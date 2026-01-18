/*
 * Copyright 2023 Typelevel
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
package experimental.attributes

// DO NOT EDIT, this is an Auto-generated file from buildscripts/templates/registry/otel4s/attributes/SemanticAttributes.scala.j2
object OtelExperimentalAttributes {

  /** A name uniquely identifying the instance of the OpenTelemetry component within its containing SDK instance.
    *
    * @note
    *   <p> Implementations SHOULD ensure a low cardinality for this attribute, even across application or SDK restarts.
    *   E.g. implementations MUST NOT use UUIDs as values for this attribute. <p> Implementations MAY achieve these
    *   goals by following a `<otel.component.type>/<instance-counter>` pattern, e.g. `batching_span_processor/0`.
    *   Hereby `otel.component.type` refers to the corresponding attribute value of the component. <p> The value of
    *   `instance-counter` MAY be automatically assigned by the component and uniqueness within the enclosing SDK
    *   instance MUST be guaranteed. For example, `<instance-counter>` MAY be implemented by using a monotonically
    *   increasing counter (starting with `0`), which is incremented every time an instance of the given component type
    *   is started. <p> With this implementation, for example the first Batching Span Processor would have
    *   `batching_span_processor/0` as `otel.component.name`, the second one `batching_span_processor/1` and so on.
    *   These values will therefore be reused in the case of an application restart.
    */
  val OtelComponentName: AttributeKey[String] =
    AttributeKey("otel.component.name")

  /** A name identifying the type of the OpenTelemetry component.
    *
    * @note
    *   <p> If none of the standardized values apply, implementations SHOULD use the language-defined name of the type.
    *   E.g. for Java the fully qualified classname SHOULD be used in this case.
    */
  val OtelComponentType: AttributeKey[String] =
    AttributeKey("otel.component.type")

  /** Deprecated. Use the `otel.scope.name` attribute
    */
  @deprecated("Replaced by `otel.scope.name`.", "")
  val OtelLibraryName: AttributeKey[String] =
    AttributeKey("otel.library.name")

  /** Deprecated. Use the `otel.scope.version` attribute.
    */
  @deprecated("Replaced by `otel.scope.version`.", "")
  val OtelLibraryVersion: AttributeKey[String] =
    AttributeKey("otel.library.version")

  /** The name of the instrumentation scope - (`InstrumentationScope.Name` in OTLP).
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.OtelAttributes.OtelScopeName` instead.",
    ""
  )
  val OtelScopeName: AttributeKey[String] =
    AttributeKey("otel.scope.name")

  /** The schema URL of the instrumentation scope.
    */
  val OtelScopeSchemaUrl: AttributeKey[String] =
    AttributeKey("otel.scope.schema_url")

  /** The version of the instrumentation scope - (`InstrumentationScope.Version` in OTLP).
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.OtelAttributes.OtelScopeVersion` instead.",
    ""
  )
  val OtelScopeVersion: AttributeKey[String] =
    AttributeKey("otel.scope.version")

  /** Determines whether the span has a parent span, and if so, <a
    * href="https://opentelemetry.io/docs/specs/otel/trace/api/#isremote">whether it is a remote parent</a>
    */
  val OtelSpanParentOrigin: AttributeKey[String] =
    AttributeKey("otel.span.parent.origin")

  /** The result value of the sampler for this span
    */
  val OtelSpanSamplingResult: AttributeKey[String] =
    AttributeKey("otel.span.sampling_result")

  /** Name of the code, either "OK" or "ERROR". MUST NOT be set if the status code is UNSET.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.OtelAttributes.OtelStatusCode` instead.",
    ""
  )
  val OtelStatusCode: AttributeKey[String] =
    AttributeKey("otel.status_code")

  /** Description of the Status if it has a value, otherwise not set.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.OtelAttributes.OtelStatusDescription` instead.",
    ""
  )
  val OtelStatusDescription: AttributeKey[String] =
    AttributeKey("otel.status_description")

  /** Values for [[OtelComponentType]].
    */
  abstract class OtelComponentTypeValue(val value: String)
  object OtelComponentTypeValue {
    implicit val attributeFromOtelComponentTypeValue: Attribute.From[OtelComponentTypeValue, String] = _.value

    /** The builtin SDK batching span processor
      */
    case object BatchingSpanProcessor extends OtelComponentTypeValue("batching_span_processor")

    /** The builtin SDK simple span processor
      */
    case object SimpleSpanProcessor extends OtelComponentTypeValue("simple_span_processor")

    /** The builtin SDK batching log record processor
      */
    case object BatchingLogProcessor extends OtelComponentTypeValue("batching_log_processor")

    /** The builtin SDK simple log record processor
      */
    case object SimpleLogProcessor extends OtelComponentTypeValue("simple_log_processor")

    /** OTLP span exporter over gRPC with protobuf serialization
      */
    case object OtlpGrpcSpanExporter extends OtelComponentTypeValue("otlp_grpc_span_exporter")

    /** OTLP span exporter over HTTP with protobuf serialization
      */
    case object OtlpHttpSpanExporter extends OtelComponentTypeValue("otlp_http_span_exporter")

    /** OTLP span exporter over HTTP with JSON serialization
      */
    case object OtlpHttpJsonSpanExporter extends OtelComponentTypeValue("otlp_http_json_span_exporter")

    /** Zipkin span exporter over HTTP
      */
    case object ZipkinHttpSpanExporter extends OtelComponentTypeValue("zipkin_http_span_exporter")

    /** OTLP log record exporter over gRPC with protobuf serialization
      */
    case object OtlpGrpcLogExporter extends OtelComponentTypeValue("otlp_grpc_log_exporter")

    /** OTLP log record exporter over HTTP with protobuf serialization
      */
    case object OtlpHttpLogExporter extends OtelComponentTypeValue("otlp_http_log_exporter")

    /** OTLP log record exporter over HTTP with JSON serialization
      */
    case object OtlpHttpJsonLogExporter extends OtelComponentTypeValue("otlp_http_json_log_exporter")

    /** The builtin SDK periodically exporting metric reader
      */
    case object PeriodicMetricReader extends OtelComponentTypeValue("periodic_metric_reader")

    /** OTLP metric exporter over gRPC with protobuf serialization
      */
    case object OtlpGrpcMetricExporter extends OtelComponentTypeValue("otlp_grpc_metric_exporter")

    /** OTLP metric exporter over HTTP with protobuf serialization
      */
    case object OtlpHttpMetricExporter extends OtelComponentTypeValue("otlp_http_metric_exporter")

    /** OTLP metric exporter over HTTP with JSON serialization
      */
    case object OtlpHttpJsonMetricExporter extends OtelComponentTypeValue("otlp_http_json_metric_exporter")

    /** Prometheus metric exporter over HTTP with the default text-based format
      */
    case object PrometheusHttpTextMetricExporter extends OtelComponentTypeValue("prometheus_http_text_metric_exporter")
  }

  /** Values for [[OtelSpanParentOrigin]].
    */
  abstract class OtelSpanParentOriginValue(val value: String)
  object OtelSpanParentOriginValue {
    implicit val attributeFromOtelSpanParentOriginValue: Attribute.From[OtelSpanParentOriginValue, String] = _.value

    /** The span does not have a parent, it is a root span
      */
    case object None extends OtelSpanParentOriginValue("none")

    /** The span has a parent and the parent's span context <a
      * href="https://opentelemetry.io/docs/specs/otel/trace/api/#isremote">isRemote()</a> is false
      */
    case object Local extends OtelSpanParentOriginValue("local")

    /** The span has a parent and the parent's span context <a
      * href="https://opentelemetry.io/docs/specs/otel/trace/api/#isremote">isRemote()</a> is true
      */
    case object Remote extends OtelSpanParentOriginValue("remote")
  }

  /** Values for [[OtelSpanSamplingResult]].
    */
  abstract class OtelSpanSamplingResultValue(val value: String)
  object OtelSpanSamplingResultValue {
    implicit val attributeFromOtelSpanSamplingResultValue: Attribute.From[OtelSpanSamplingResultValue, String] = _.value

    /** The span is not sampled and not recording
      */
    case object Drop extends OtelSpanSamplingResultValue("DROP")

    /** The span is not sampled, but recording
      */
    case object RecordOnly extends OtelSpanSamplingResultValue("RECORD_ONLY")

    /** The span is sampled and recording
      */
    case object RecordAndSample extends OtelSpanSamplingResultValue("RECORD_AND_SAMPLE")
  }

  /** Values for [[OtelStatusCode]].
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.OtelAttributes.OtelStatusCode` instead.",
    ""
  )
  abstract class OtelStatusCodeValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object OtelStatusCodeValue {
    implicit val attributeFromOtelStatusCodeValue: Attribute.From[OtelStatusCodeValue, String] = _.value

    /** The operation has been validated by an Application developer or Operator to have completed successfully.
      */
    case object Ok extends OtelStatusCodeValue("OK")

    /** The operation contains an error.
      */
    case object Error extends OtelStatusCodeValue("ERROR")
  }

}
