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
import org.typelevel.otel4s.semconv.attributes._
import org.typelevel.otel4s.semconv.experimental.attributes._

// DO NOT EDIT, this is an Auto-generated file from buildscripts/templates/registry/otel4s/metrics/SemanticMetrics.scala.j2
object RpcExperimentalMetrics {

  @annotation.nowarn("cat=deprecation")
  val specs: List[MetricSpec] = List(
    ClientCallDuration,
    ClientDuration,
    ClientRequestSize,
    ClientRequestsPerRpc,
    ClientResponseSize,
    ClientResponsesPerRpc,
    ServerCallDuration,
    ServerDuration,
    ServerRequestSize,
    ServerRequestsPerRpc,
    ServerResponseSize,
    ServerResponsesPerRpc,
  )

  /** Measures the duration of outbound remote procedure calls (RPC).
    *
    * @note
    *   <p> When this metric is reported alongside an RPC client span, the metric value SHOULD be the same as the RPC
    *   client span duration.
    */
  object ClientCallDuration extends MetricSpec.Unsealed {

    val name: String = "rpc.client.call.duration"
    val description: String = "Measures the duration of outbound remote procedure calls (RPC)."
    val unit: String = "s"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Describes a class of error the operation ended with.
        *
        * @note
        *   <p> If the RPC fails with an error before status code is returned, `error.type` SHOULD be set to the
        *   exception type (its fully-qualified class name, if applicable) or a component-specific, low cardinality
        *   error identifier. <p> If a response status code is returned and status indicates an error, `error.type`
        *   SHOULD be set to that status code. Check system-specific conventions for the details on which values of
        *   `rpc.response.status_code` are considered errors. <p> The `error.type` value SHOULD be predictable and
        *   SHOULD have low cardinality. Instrumentations SHOULD document the list of errors they report. <p> If the
        *   request has completed successfully, instrumentations SHOULD NOT set `error.type`.
        */
      val errorType: AttributeSpec[String] =
        AttributeSpec(
          ErrorAttributes.ErrorType,
          List(
            "DEADLINE_EXCEEDED",
            "java.net.UnknownHostException",
            "-32602",
          ),
          Requirement.conditionallyRequired("If and only if the operation failed."),
          Stability.stable
        )

      /** <a href="https://wikipedia.org/wiki/Application_layer">OSI application layer</a> or non-OSI equivalent.
        *
        * @note
        *   <p> The value SHOULD be normalized to lowercase.
        */
      val networkProtocolName: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkProtocolName,
          List(
            "http",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** The actual version of the protocol used for network communication.
        *
        * @note
        *   <p> If protocol version is subject to negotiation (for example using <a
        *   href="https://www.rfc-editor.org/rfc/rfc7301.html">ALPN</a>), this attribute SHOULD be set to the negotiated
        *   version. If the actual protocol version is not known, this attribute SHOULD NOT be set.
        */
      val networkProtocolVersion: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkProtocolVersion,
          List(
            "1.1",
            "2",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** <a href="https://wikipedia.org/wiki/Transport_layer">OSI transport layer</a> or <a
        * href="https://wikipedia.org/wiki/Inter-process_communication">inter-process communication method</a>.
        *
        * @note
        *   <p> The value SHOULD be normalized to lowercase. <p> Consider always setting the transport when setting a
        *   port number, since a port number is ambiguous without knowing the transport. For example different processes
        *   could be listening on TCP port 12345 and UDP port 12345.
        */
      val networkTransport: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkTransport,
          List(
            "tcp",
            "udp",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** The fully-qualified logical name of the method from the RPC interface perspective.
        *
        * @note
        *   <p> The method name MAY have unbounded cardinality in edge or error cases. <p> Some RPC frameworks or
        *   libraries provide a fixed set of recognized methods for client stubs and server implementations.
        *   Instrumentations for such frameworks MUST set this attribute to the original method name only when the
        *   method is recognized by the framework or library. <p> When the method is not recognized, for example, when
        *   the server receives a request for a method that is not predefined on the server, or when instrumentation is
        *   not able to reliably detect if the method is predefined, the attribute MUST be set to `_OTHER`. In such
        *   cases, tracing instrumentations MUST also set `rpc.method_original` attribute to the original method value.
        *   <p> If the RPC instrumentation could end up converting valid RPC methods to `_OTHER`, then it SHOULD provide
        *   a way to configure the list of recognized RPC methods. <p> The `rpc.method` can be different from the name
        *   of any implementing method/function. The `code.function.name` attribute may be used to record the
        *   fully-qualified method actually executing the call on the server side, or the RPC client stub method on the
        *   client side.
        */
      val rpcMethod: AttributeSpec[String] =
        AttributeSpec(
          RpcExperimentalAttributes.RpcMethod,
          List(
            "com.example.ExampleService/exampleMethod",
            "EchoService/Echo",
            "_OTHER",
          ),
          Requirement.conditionallyRequired("if available."),
          Stability.development
        )

      /** Status code of the RPC returned by the RPC server or generated by the client
        *
        * @note
        *   <p> Usually it represents an error code, but may also represent partial success, warning, or differentiate
        *   between various types of successful outcomes. Semantic conventions for individual RPC frameworks SHOULD
        *   document what `rpc.response.status_code` means in the context of that system and which values are considered
        *   to represent errors.
        */
      val rpcResponseStatusCode: AttributeSpec[String] =
        AttributeSpec(
          RpcExperimentalAttributes.RpcResponseStatusCode,
          List(
            "OK",
            "DEADLINE_EXCEEDED",
            "-32602",
          ),
          Requirement.conditionallyRequired("if available."),
          Stability.development
        )

      /** The Remote Procedure Call (RPC) system.
        *
        * @note
        *   <p> The client and server RPC systems may differ for the same RPC interaction. For example, a client may use
        *   Apache Dubbo or Connect RPC to communicate with a server that uses gRPC since both protocols provide
        *   compatibility with gRPC.
        */
      val rpcSystemName: AttributeSpec[String] =
        AttributeSpec(
          RpcExperimentalAttributes.RpcSystemName,
          List(
          ),
          Requirement.required,
          Stability.development
        )

      /** RPC server <a href="https://grpc.github.io/grpc/core/md_doc_naming.html">host name</a>.
        *
        * @note
        *   <p> May contain server IP address, DNS name, or local socket name. When host component is an IP address,
        *   instrumentations SHOULD NOT do a reverse proxy lookup to obtain DNS name and SHOULD set `server.address` to
        *   the IP address provided in the host component.
        */
      val serverAddress: AttributeSpec[String] =
        AttributeSpec(
          ServerAttributes.ServerAddress,
          List(
            "example.com",
            "10.1.2.80",
            "/tmp/my.sock",
          ),
          Requirement.conditionallyRequired("If available."),
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
          Requirement.conditionallyRequired(
            "if `server.address` is set and if the port is supported by the network transport used for communication."
          ),
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          errorType,
          networkProtocolName,
          networkProtocolVersion,
          networkTransport,
          rpcMethod,
          rpcResponseStatusCode,
          rpcSystemName,
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

  /** Deprecated, use `rpc.client.call.duration` instead. Note: the unit also changed from `ms` to `s`.
    *
    * @note
    *   <p> While streaming RPCs may record this metric as start-of-batch to end-of-batch, it's hard to interpret in
    *   practice. <p> <strong>Streaming</strong>: N/A.
    */
  @deprecated("Replaced by `rpc.client.call.duration` with unit `s`.", "")
  object ClientDuration extends MetricSpec.Unsealed {

    val name: String = "rpc.client.duration"
    val description: String =
      "Deprecated, use `rpc.client.call.duration` instead. Note: the unit also changed from `ms` to `s`."
    val unit: String = "ms"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Describes a class of error the operation ended with.
        *
        * @note
        *   <p> If the RPC fails with an error before status code is returned, `error.type` SHOULD be set to the
        *   exception type (its fully-qualified class name, if applicable) or a component-specific, low cardinality
        *   error identifier. <p> If a response status code is returned and status indicates an error, `error.type`
        *   SHOULD be set to that status code. Check system-specific conventions for the details on which values of
        *   `rpc.response.status_code` are considered errors. <p> The `error.type` value SHOULD be predictable and
        *   SHOULD have low cardinality. Instrumentations SHOULD document the list of errors they report. <p> If the
        *   request has completed successfully, instrumentations SHOULD NOT set `error.type`.
        */
      val errorType: AttributeSpec[String] =
        AttributeSpec(
          ErrorAttributes.ErrorType,
          List(
            "DEADLINE_EXCEEDED",
            "java.net.UnknownHostException",
            "-32602",
          ),
          Requirement.conditionallyRequired("If and only if the operation failed."),
          Stability.stable
        )

      /** <a href="https://wikipedia.org/wiki/Application_layer">OSI application layer</a> or non-OSI equivalent.
        *
        * @note
        *   <p> The value SHOULD be normalized to lowercase.
        */
      val networkProtocolName: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkProtocolName,
          List(
            "http",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** The actual version of the protocol used for network communication.
        *
        * @note
        *   <p> If protocol version is subject to negotiation (for example using <a
        *   href="https://www.rfc-editor.org/rfc/rfc7301.html">ALPN</a>), this attribute SHOULD be set to the negotiated
        *   version. If the actual protocol version is not known, this attribute SHOULD NOT be set.
        */
      val networkProtocolVersion: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkProtocolVersion,
          List(
            "1.1",
            "2",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** <a href="https://wikipedia.org/wiki/Transport_layer">OSI transport layer</a> or <a
        * href="https://wikipedia.org/wiki/Inter-process_communication">inter-process communication method</a>.
        *
        * @note
        *   <p> The value SHOULD be normalized to lowercase. <p> Consider always setting the transport when setting a
        *   port number, since a port number is ambiguous without knowing the transport. For example different processes
        *   could be listening on TCP port 12345 and UDP port 12345.
        */
      val networkTransport: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkTransport,
          List(
            "tcp",
            "udp",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** The fully-qualified logical name of the method from the RPC interface perspective.
        *
        * @note
        *   <p> The method name MAY have unbounded cardinality in edge or error cases. <p> Some RPC frameworks or
        *   libraries provide a fixed set of recognized methods for client stubs and server implementations.
        *   Instrumentations for such frameworks MUST set this attribute to the original method name only when the
        *   method is recognized by the framework or library. <p> When the method is not recognized, for example, when
        *   the server receives a request for a method that is not predefined on the server, or when instrumentation is
        *   not able to reliably detect if the method is predefined, the attribute MUST be set to `_OTHER`. In such
        *   cases, tracing instrumentations MUST also set `rpc.method_original` attribute to the original method value.
        *   <p> If the RPC instrumentation could end up converting valid RPC methods to `_OTHER`, then it SHOULD provide
        *   a way to configure the list of recognized RPC methods. <p> The `rpc.method` can be different from the name
        *   of any implementing method/function. The `code.function.name` attribute may be used to record the
        *   fully-qualified method actually executing the call on the server side, or the RPC client stub method on the
        *   client side.
        */
      val rpcMethod: AttributeSpec[String] =
        AttributeSpec(
          RpcExperimentalAttributes.RpcMethod,
          List(
            "com.example.ExampleService/exampleMethod",
            "EchoService/Echo",
            "_OTHER",
          ),
          Requirement.conditionallyRequired("if available."),
          Stability.development
        )

      /** Status code of the RPC returned by the RPC server or generated by the client
        *
        * @note
        *   <p> Usually it represents an error code, but may also represent partial success, warning, or differentiate
        *   between various types of successful outcomes. Semantic conventions for individual RPC frameworks SHOULD
        *   document what `rpc.response.status_code` means in the context of that system and which values are considered
        *   to represent errors.
        */
      val rpcResponseStatusCode: AttributeSpec[String] =
        AttributeSpec(
          RpcExperimentalAttributes.RpcResponseStatusCode,
          List(
            "OK",
            "DEADLINE_EXCEEDED",
            "-32602",
          ),
          Requirement.conditionallyRequired("if available."),
          Stability.development
        )

      /** The Remote Procedure Call (RPC) system.
        *
        * @note
        *   <p> The client and server RPC systems may differ for the same RPC interaction. For example, a client may use
        *   Apache Dubbo or Connect RPC to communicate with a server that uses gRPC since both protocols provide
        *   compatibility with gRPC.
        */
      val rpcSystemName: AttributeSpec[String] =
        AttributeSpec(
          RpcExperimentalAttributes.RpcSystemName,
          List(
          ),
          Requirement.required,
          Stability.development
        )

      /** RPC server <a href="https://grpc.github.io/grpc/core/md_doc_naming.html">host name</a>.
        *
        * @note
        *   <p> May contain server IP address, DNS name, or local socket name. When host component is an IP address,
        *   instrumentations SHOULD NOT do a reverse proxy lookup to obtain DNS name and SHOULD set `server.address` to
        *   the IP address provided in the host component.
        */
      val serverAddress: AttributeSpec[String] =
        AttributeSpec(
          ServerAttributes.ServerAddress,
          List(
            "example.com",
            "10.1.2.80",
            "/tmp/my.sock",
          ),
          Requirement.conditionallyRequired("If available."),
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
          Requirement.conditionallyRequired(
            "if `server.address` is set and if the port is supported by the network transport used for communication."
          ),
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          errorType,
          networkProtocolName,
          networkProtocolVersion,
          networkTransport,
          rpcMethod,
          rpcResponseStatusCode,
          rpcSystemName,
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

  /** Measures the size of RPC request messages (uncompressed).
    *
    * @note
    *   <p> <strong>Streaming</strong>: Recorded per message in a streaming batch
    */
  object ClientRequestSize extends MetricSpec.Unsealed {

    val name: String = "rpc.client.request.size"
    val description: String = "Measures the size of RPC request messages (uncompressed)."
    val unit: String = "By"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Describes a class of error the operation ended with.
        *
        * @note
        *   <p> If the RPC fails with an error before status code is returned, `error.type` SHOULD be set to the
        *   exception type (its fully-qualified class name, if applicable) or a component-specific, low cardinality
        *   error identifier. <p> If a response status code is returned and status indicates an error, `error.type`
        *   SHOULD be set to that status code. Check system-specific conventions for the details on which values of
        *   `rpc.response.status_code` are considered errors. <p> The `error.type` value SHOULD be predictable and
        *   SHOULD have low cardinality. Instrumentations SHOULD document the list of errors they report. <p> If the
        *   request has completed successfully, instrumentations SHOULD NOT set `error.type`.
        */
      val errorType: AttributeSpec[String] =
        AttributeSpec(
          ErrorAttributes.ErrorType,
          List(
            "DEADLINE_EXCEEDED",
            "java.net.UnknownHostException",
            "-32602",
          ),
          Requirement.conditionallyRequired("If and only if the operation failed."),
          Stability.stable
        )

      /** <a href="https://wikipedia.org/wiki/Application_layer">OSI application layer</a> or non-OSI equivalent.
        *
        * @note
        *   <p> The value SHOULD be normalized to lowercase.
        */
      val networkProtocolName: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkProtocolName,
          List(
            "http",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** The actual version of the protocol used for network communication.
        *
        * @note
        *   <p> If protocol version is subject to negotiation (for example using <a
        *   href="https://www.rfc-editor.org/rfc/rfc7301.html">ALPN</a>), this attribute SHOULD be set to the negotiated
        *   version. If the actual protocol version is not known, this attribute SHOULD NOT be set.
        */
      val networkProtocolVersion: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkProtocolVersion,
          List(
            "1.1",
            "2",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** <a href="https://wikipedia.org/wiki/Transport_layer">OSI transport layer</a> or <a
        * href="https://wikipedia.org/wiki/Inter-process_communication">inter-process communication method</a>.
        *
        * @note
        *   <p> The value SHOULD be normalized to lowercase. <p> Consider always setting the transport when setting a
        *   port number, since a port number is ambiguous without knowing the transport. For example different processes
        *   could be listening on TCP port 12345 and UDP port 12345.
        */
      val networkTransport: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkTransport,
          List(
            "tcp",
            "udp",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** The fully-qualified logical name of the method from the RPC interface perspective.
        *
        * @note
        *   <p> The method name MAY have unbounded cardinality in edge or error cases. <p> Some RPC frameworks or
        *   libraries provide a fixed set of recognized methods for client stubs and server implementations.
        *   Instrumentations for such frameworks MUST set this attribute to the original method name only when the
        *   method is recognized by the framework or library. <p> When the method is not recognized, for example, when
        *   the server receives a request for a method that is not predefined on the server, or when instrumentation is
        *   not able to reliably detect if the method is predefined, the attribute MUST be set to `_OTHER`. In such
        *   cases, tracing instrumentations MUST also set `rpc.method_original` attribute to the original method value.
        *   <p> If the RPC instrumentation could end up converting valid RPC methods to `_OTHER`, then it SHOULD provide
        *   a way to configure the list of recognized RPC methods. <p> The `rpc.method` can be different from the name
        *   of any implementing method/function. The `code.function.name` attribute may be used to record the
        *   fully-qualified method actually executing the call on the server side, or the RPC client stub method on the
        *   client side.
        */
      val rpcMethod: AttributeSpec[String] =
        AttributeSpec(
          RpcExperimentalAttributes.RpcMethod,
          List(
            "com.example.ExampleService/exampleMethod",
            "EchoService/Echo",
            "_OTHER",
          ),
          Requirement.conditionallyRequired("if available."),
          Stability.development
        )

      /** Status code of the RPC returned by the RPC server or generated by the client
        *
        * @note
        *   <p> Usually it represents an error code, but may also represent partial success, warning, or differentiate
        *   between various types of successful outcomes. Semantic conventions for individual RPC frameworks SHOULD
        *   document what `rpc.response.status_code` means in the context of that system and which values are considered
        *   to represent errors.
        */
      val rpcResponseStatusCode: AttributeSpec[String] =
        AttributeSpec(
          RpcExperimentalAttributes.RpcResponseStatusCode,
          List(
            "OK",
            "DEADLINE_EXCEEDED",
            "-32602",
          ),
          Requirement.conditionallyRequired("if available."),
          Stability.development
        )

      /** The Remote Procedure Call (RPC) system.
        *
        * @note
        *   <p> The client and server RPC systems may differ for the same RPC interaction. For example, a client may use
        *   Apache Dubbo or Connect RPC to communicate with a server that uses gRPC since both protocols provide
        *   compatibility with gRPC.
        */
      val rpcSystemName: AttributeSpec[String] =
        AttributeSpec(
          RpcExperimentalAttributes.RpcSystemName,
          List(
          ),
          Requirement.required,
          Stability.development
        )

      /** RPC server <a href="https://grpc.github.io/grpc/core/md_doc_naming.html">host name</a>.
        *
        * @note
        *   <p> May contain server IP address, DNS name, or local socket name. When host component is an IP address,
        *   instrumentations SHOULD NOT do a reverse proxy lookup to obtain DNS name and SHOULD set `server.address` to
        *   the IP address provided in the host component.
        */
      val serverAddress: AttributeSpec[String] =
        AttributeSpec(
          ServerAttributes.ServerAddress,
          List(
            "example.com",
            "10.1.2.80",
            "/tmp/my.sock",
          ),
          Requirement.conditionallyRequired("If available."),
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
          Requirement.conditionallyRequired(
            "if `server.address` is set and if the port is supported by the network transport used for communication."
          ),
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          errorType,
          networkProtocolName,
          networkProtocolVersion,
          networkTransport,
          rpcMethod,
          rpcResponseStatusCode,
          rpcSystemName,
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

  /** Measures the number of messages received per RPC.
    *
    * @note
    *   <p> Should be 1 for all non-streaming RPCs. <p> <strong>Streaming</strong>: This metric is required for server
    *   and client streaming RPCs
    */
  @deprecated("Removed, no replacement at this time.", "")
  object ClientRequestsPerRpc extends MetricSpec.Unsealed {

    val name: String = "rpc.client.requests_per_rpc"
    val description: String = "Measures the number of messages received per RPC."
    val unit: String = "{count}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Describes a class of error the operation ended with.
        *
        * @note
        *   <p> If the RPC fails with an error before status code is returned, `error.type` SHOULD be set to the
        *   exception type (its fully-qualified class name, if applicable) or a component-specific, low cardinality
        *   error identifier. <p> If a response status code is returned and status indicates an error, `error.type`
        *   SHOULD be set to that status code. Check system-specific conventions for the details on which values of
        *   `rpc.response.status_code` are considered errors. <p> The `error.type` value SHOULD be predictable and
        *   SHOULD have low cardinality. Instrumentations SHOULD document the list of errors they report. <p> If the
        *   request has completed successfully, instrumentations SHOULD NOT set `error.type`.
        */
      val errorType: AttributeSpec[String] =
        AttributeSpec(
          ErrorAttributes.ErrorType,
          List(
            "DEADLINE_EXCEEDED",
            "java.net.UnknownHostException",
            "-32602",
          ),
          Requirement.conditionallyRequired("If and only if the operation failed."),
          Stability.stable
        )

      /** <a href="https://wikipedia.org/wiki/Application_layer">OSI application layer</a> or non-OSI equivalent.
        *
        * @note
        *   <p> The value SHOULD be normalized to lowercase.
        */
      val networkProtocolName: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkProtocolName,
          List(
            "http",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** The actual version of the protocol used for network communication.
        *
        * @note
        *   <p> If protocol version is subject to negotiation (for example using <a
        *   href="https://www.rfc-editor.org/rfc/rfc7301.html">ALPN</a>), this attribute SHOULD be set to the negotiated
        *   version. If the actual protocol version is not known, this attribute SHOULD NOT be set.
        */
      val networkProtocolVersion: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkProtocolVersion,
          List(
            "1.1",
            "2",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** <a href="https://wikipedia.org/wiki/Transport_layer">OSI transport layer</a> or <a
        * href="https://wikipedia.org/wiki/Inter-process_communication">inter-process communication method</a>.
        *
        * @note
        *   <p> The value SHOULD be normalized to lowercase. <p> Consider always setting the transport when setting a
        *   port number, since a port number is ambiguous without knowing the transport. For example different processes
        *   could be listening on TCP port 12345 and UDP port 12345.
        */
      val networkTransport: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkTransport,
          List(
            "tcp",
            "udp",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** The fully-qualified logical name of the method from the RPC interface perspective.
        *
        * @note
        *   <p> The method name MAY have unbounded cardinality in edge or error cases. <p> Some RPC frameworks or
        *   libraries provide a fixed set of recognized methods for client stubs and server implementations.
        *   Instrumentations for such frameworks MUST set this attribute to the original method name only when the
        *   method is recognized by the framework or library. <p> When the method is not recognized, for example, when
        *   the server receives a request for a method that is not predefined on the server, or when instrumentation is
        *   not able to reliably detect if the method is predefined, the attribute MUST be set to `_OTHER`. In such
        *   cases, tracing instrumentations MUST also set `rpc.method_original` attribute to the original method value.
        *   <p> If the RPC instrumentation could end up converting valid RPC methods to `_OTHER`, then it SHOULD provide
        *   a way to configure the list of recognized RPC methods. <p> The `rpc.method` can be different from the name
        *   of any implementing method/function. The `code.function.name` attribute may be used to record the
        *   fully-qualified method actually executing the call on the server side, or the RPC client stub method on the
        *   client side.
        */
      val rpcMethod: AttributeSpec[String] =
        AttributeSpec(
          RpcExperimentalAttributes.RpcMethod,
          List(
            "com.example.ExampleService/exampleMethod",
            "EchoService/Echo",
            "_OTHER",
          ),
          Requirement.conditionallyRequired("if available."),
          Stability.development
        )

      /** Status code of the RPC returned by the RPC server or generated by the client
        *
        * @note
        *   <p> Usually it represents an error code, but may also represent partial success, warning, or differentiate
        *   between various types of successful outcomes. Semantic conventions for individual RPC frameworks SHOULD
        *   document what `rpc.response.status_code` means in the context of that system and which values are considered
        *   to represent errors.
        */
      val rpcResponseStatusCode: AttributeSpec[String] =
        AttributeSpec(
          RpcExperimentalAttributes.RpcResponseStatusCode,
          List(
            "OK",
            "DEADLINE_EXCEEDED",
            "-32602",
          ),
          Requirement.conditionallyRequired("if available."),
          Stability.development
        )

      /** The Remote Procedure Call (RPC) system.
        *
        * @note
        *   <p> The client and server RPC systems may differ for the same RPC interaction. For example, a client may use
        *   Apache Dubbo or Connect RPC to communicate with a server that uses gRPC since both protocols provide
        *   compatibility with gRPC.
        */
      val rpcSystemName: AttributeSpec[String] =
        AttributeSpec(
          RpcExperimentalAttributes.RpcSystemName,
          List(
          ),
          Requirement.required,
          Stability.development
        )

      /** RPC server <a href="https://grpc.github.io/grpc/core/md_doc_naming.html">host name</a>.
        *
        * @note
        *   <p> May contain server IP address, DNS name, or local socket name. When host component is an IP address,
        *   instrumentations SHOULD NOT do a reverse proxy lookup to obtain DNS name and SHOULD set `server.address` to
        *   the IP address provided in the host component.
        */
      val serverAddress: AttributeSpec[String] =
        AttributeSpec(
          ServerAttributes.ServerAddress,
          List(
            "example.com",
            "10.1.2.80",
            "/tmp/my.sock",
          ),
          Requirement.conditionallyRequired("If available."),
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
          Requirement.conditionallyRequired(
            "if `server.address` is set and if the port is supported by the network transport used for communication."
          ),
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          errorType,
          networkProtocolName,
          networkProtocolVersion,
          networkTransport,
          rpcMethod,
          rpcResponseStatusCode,
          rpcSystemName,
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

  /** Measures the size of RPC response messages (uncompressed).
    *
    * @note
    *   <p> <strong>Streaming</strong>: Recorded per response in a streaming batch
    */
  object ClientResponseSize extends MetricSpec.Unsealed {

    val name: String = "rpc.client.response.size"
    val description: String = "Measures the size of RPC response messages (uncompressed)."
    val unit: String = "By"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Describes a class of error the operation ended with.
        *
        * @note
        *   <p> If the RPC fails with an error before status code is returned, `error.type` SHOULD be set to the
        *   exception type (its fully-qualified class name, if applicable) or a component-specific, low cardinality
        *   error identifier. <p> If a response status code is returned and status indicates an error, `error.type`
        *   SHOULD be set to that status code. Check system-specific conventions for the details on which values of
        *   `rpc.response.status_code` are considered errors. <p> The `error.type` value SHOULD be predictable and
        *   SHOULD have low cardinality. Instrumentations SHOULD document the list of errors they report. <p> If the
        *   request has completed successfully, instrumentations SHOULD NOT set `error.type`.
        */
      val errorType: AttributeSpec[String] =
        AttributeSpec(
          ErrorAttributes.ErrorType,
          List(
            "DEADLINE_EXCEEDED",
            "java.net.UnknownHostException",
            "-32602",
          ),
          Requirement.conditionallyRequired("If and only if the operation failed."),
          Stability.stable
        )

      /** <a href="https://wikipedia.org/wiki/Application_layer">OSI application layer</a> or non-OSI equivalent.
        *
        * @note
        *   <p> The value SHOULD be normalized to lowercase.
        */
      val networkProtocolName: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkProtocolName,
          List(
            "http",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** The actual version of the protocol used for network communication.
        *
        * @note
        *   <p> If protocol version is subject to negotiation (for example using <a
        *   href="https://www.rfc-editor.org/rfc/rfc7301.html">ALPN</a>), this attribute SHOULD be set to the negotiated
        *   version. If the actual protocol version is not known, this attribute SHOULD NOT be set.
        */
      val networkProtocolVersion: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkProtocolVersion,
          List(
            "1.1",
            "2",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** <a href="https://wikipedia.org/wiki/Transport_layer">OSI transport layer</a> or <a
        * href="https://wikipedia.org/wiki/Inter-process_communication">inter-process communication method</a>.
        *
        * @note
        *   <p> The value SHOULD be normalized to lowercase. <p> Consider always setting the transport when setting a
        *   port number, since a port number is ambiguous without knowing the transport. For example different processes
        *   could be listening on TCP port 12345 and UDP port 12345.
        */
      val networkTransport: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkTransport,
          List(
            "tcp",
            "udp",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** The fully-qualified logical name of the method from the RPC interface perspective.
        *
        * @note
        *   <p> The method name MAY have unbounded cardinality in edge or error cases. <p> Some RPC frameworks or
        *   libraries provide a fixed set of recognized methods for client stubs and server implementations.
        *   Instrumentations for such frameworks MUST set this attribute to the original method name only when the
        *   method is recognized by the framework or library. <p> When the method is not recognized, for example, when
        *   the server receives a request for a method that is not predefined on the server, or when instrumentation is
        *   not able to reliably detect if the method is predefined, the attribute MUST be set to `_OTHER`. In such
        *   cases, tracing instrumentations MUST also set `rpc.method_original` attribute to the original method value.
        *   <p> If the RPC instrumentation could end up converting valid RPC methods to `_OTHER`, then it SHOULD provide
        *   a way to configure the list of recognized RPC methods. <p> The `rpc.method` can be different from the name
        *   of any implementing method/function. The `code.function.name` attribute may be used to record the
        *   fully-qualified method actually executing the call on the server side, or the RPC client stub method on the
        *   client side.
        */
      val rpcMethod: AttributeSpec[String] =
        AttributeSpec(
          RpcExperimentalAttributes.RpcMethod,
          List(
            "com.example.ExampleService/exampleMethod",
            "EchoService/Echo",
            "_OTHER",
          ),
          Requirement.conditionallyRequired("if available."),
          Stability.development
        )

      /** Status code of the RPC returned by the RPC server or generated by the client
        *
        * @note
        *   <p> Usually it represents an error code, but may also represent partial success, warning, or differentiate
        *   between various types of successful outcomes. Semantic conventions for individual RPC frameworks SHOULD
        *   document what `rpc.response.status_code` means in the context of that system and which values are considered
        *   to represent errors.
        */
      val rpcResponseStatusCode: AttributeSpec[String] =
        AttributeSpec(
          RpcExperimentalAttributes.RpcResponseStatusCode,
          List(
            "OK",
            "DEADLINE_EXCEEDED",
            "-32602",
          ),
          Requirement.conditionallyRequired("if available."),
          Stability.development
        )

      /** The Remote Procedure Call (RPC) system.
        *
        * @note
        *   <p> The client and server RPC systems may differ for the same RPC interaction. For example, a client may use
        *   Apache Dubbo or Connect RPC to communicate with a server that uses gRPC since both protocols provide
        *   compatibility with gRPC.
        */
      val rpcSystemName: AttributeSpec[String] =
        AttributeSpec(
          RpcExperimentalAttributes.RpcSystemName,
          List(
          ),
          Requirement.required,
          Stability.development
        )

      /** RPC server <a href="https://grpc.github.io/grpc/core/md_doc_naming.html">host name</a>.
        *
        * @note
        *   <p> May contain server IP address, DNS name, or local socket name. When host component is an IP address,
        *   instrumentations SHOULD NOT do a reverse proxy lookup to obtain DNS name and SHOULD set `server.address` to
        *   the IP address provided in the host component.
        */
      val serverAddress: AttributeSpec[String] =
        AttributeSpec(
          ServerAttributes.ServerAddress,
          List(
            "example.com",
            "10.1.2.80",
            "/tmp/my.sock",
          ),
          Requirement.conditionallyRequired("If available."),
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
          Requirement.conditionallyRequired(
            "if `server.address` is set and if the port is supported by the network transport used for communication."
          ),
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          errorType,
          networkProtocolName,
          networkProtocolVersion,
          networkTransport,
          rpcMethod,
          rpcResponseStatusCode,
          rpcSystemName,
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

  /** Measures the number of messages sent per RPC.
    *
    * @note
    *   <p> Should be 1 for all non-streaming RPCs. <p> <strong>Streaming</strong>: This metric is required for server
    *   and client streaming RPCs
    */
  @deprecated("Removed, no replacement at this time.", "")
  object ClientResponsesPerRpc extends MetricSpec.Unsealed {

    val name: String = "rpc.client.responses_per_rpc"
    val description: String = "Measures the number of messages sent per RPC."
    val unit: String = "{count}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Describes a class of error the operation ended with.
        *
        * @note
        *   <p> If the RPC fails with an error before status code is returned, `error.type` SHOULD be set to the
        *   exception type (its fully-qualified class name, if applicable) or a component-specific, low cardinality
        *   error identifier. <p> If a response status code is returned and status indicates an error, `error.type`
        *   SHOULD be set to that status code. Check system-specific conventions for the details on which values of
        *   `rpc.response.status_code` are considered errors. <p> The `error.type` value SHOULD be predictable and
        *   SHOULD have low cardinality. Instrumentations SHOULD document the list of errors they report. <p> If the
        *   request has completed successfully, instrumentations SHOULD NOT set `error.type`.
        */
      val errorType: AttributeSpec[String] =
        AttributeSpec(
          ErrorAttributes.ErrorType,
          List(
            "DEADLINE_EXCEEDED",
            "java.net.UnknownHostException",
            "-32602",
          ),
          Requirement.conditionallyRequired("If and only if the operation failed."),
          Stability.stable
        )

      /** <a href="https://wikipedia.org/wiki/Application_layer">OSI application layer</a> or non-OSI equivalent.
        *
        * @note
        *   <p> The value SHOULD be normalized to lowercase.
        */
      val networkProtocolName: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkProtocolName,
          List(
            "http",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** The actual version of the protocol used for network communication.
        *
        * @note
        *   <p> If protocol version is subject to negotiation (for example using <a
        *   href="https://www.rfc-editor.org/rfc/rfc7301.html">ALPN</a>), this attribute SHOULD be set to the negotiated
        *   version. If the actual protocol version is not known, this attribute SHOULD NOT be set.
        */
      val networkProtocolVersion: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkProtocolVersion,
          List(
            "1.1",
            "2",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** <a href="https://wikipedia.org/wiki/Transport_layer">OSI transport layer</a> or <a
        * href="https://wikipedia.org/wiki/Inter-process_communication">inter-process communication method</a>.
        *
        * @note
        *   <p> The value SHOULD be normalized to lowercase. <p> Consider always setting the transport when setting a
        *   port number, since a port number is ambiguous without knowing the transport. For example different processes
        *   could be listening on TCP port 12345 and UDP port 12345.
        */
      val networkTransport: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkTransport,
          List(
            "tcp",
            "udp",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** The fully-qualified logical name of the method from the RPC interface perspective.
        *
        * @note
        *   <p> The method name MAY have unbounded cardinality in edge or error cases. <p> Some RPC frameworks or
        *   libraries provide a fixed set of recognized methods for client stubs and server implementations.
        *   Instrumentations for such frameworks MUST set this attribute to the original method name only when the
        *   method is recognized by the framework or library. <p> When the method is not recognized, for example, when
        *   the server receives a request for a method that is not predefined on the server, or when instrumentation is
        *   not able to reliably detect if the method is predefined, the attribute MUST be set to `_OTHER`. In such
        *   cases, tracing instrumentations MUST also set `rpc.method_original` attribute to the original method value.
        *   <p> If the RPC instrumentation could end up converting valid RPC methods to `_OTHER`, then it SHOULD provide
        *   a way to configure the list of recognized RPC methods. <p> The `rpc.method` can be different from the name
        *   of any implementing method/function. The `code.function.name` attribute may be used to record the
        *   fully-qualified method actually executing the call on the server side, or the RPC client stub method on the
        *   client side.
        */
      val rpcMethod: AttributeSpec[String] =
        AttributeSpec(
          RpcExperimentalAttributes.RpcMethod,
          List(
            "com.example.ExampleService/exampleMethod",
            "EchoService/Echo",
            "_OTHER",
          ),
          Requirement.conditionallyRequired("if available."),
          Stability.development
        )

      /** Status code of the RPC returned by the RPC server or generated by the client
        *
        * @note
        *   <p> Usually it represents an error code, but may also represent partial success, warning, or differentiate
        *   between various types of successful outcomes. Semantic conventions for individual RPC frameworks SHOULD
        *   document what `rpc.response.status_code` means in the context of that system and which values are considered
        *   to represent errors.
        */
      val rpcResponseStatusCode: AttributeSpec[String] =
        AttributeSpec(
          RpcExperimentalAttributes.RpcResponseStatusCode,
          List(
            "OK",
            "DEADLINE_EXCEEDED",
            "-32602",
          ),
          Requirement.conditionallyRequired("if available."),
          Stability.development
        )

      /** The Remote Procedure Call (RPC) system.
        *
        * @note
        *   <p> The client and server RPC systems may differ for the same RPC interaction. For example, a client may use
        *   Apache Dubbo or Connect RPC to communicate with a server that uses gRPC since both protocols provide
        *   compatibility with gRPC.
        */
      val rpcSystemName: AttributeSpec[String] =
        AttributeSpec(
          RpcExperimentalAttributes.RpcSystemName,
          List(
          ),
          Requirement.required,
          Stability.development
        )

      /** RPC server <a href="https://grpc.github.io/grpc/core/md_doc_naming.html">host name</a>.
        *
        * @note
        *   <p> May contain server IP address, DNS name, or local socket name. When host component is an IP address,
        *   instrumentations SHOULD NOT do a reverse proxy lookup to obtain DNS name and SHOULD set `server.address` to
        *   the IP address provided in the host component.
        */
      val serverAddress: AttributeSpec[String] =
        AttributeSpec(
          ServerAttributes.ServerAddress,
          List(
            "example.com",
            "10.1.2.80",
            "/tmp/my.sock",
          ),
          Requirement.conditionallyRequired("If available."),
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
          Requirement.conditionallyRequired(
            "if `server.address` is set and if the port is supported by the network transport used for communication."
          ),
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          errorType,
          networkProtocolName,
          networkProtocolVersion,
          networkTransport,
          rpcMethod,
          rpcResponseStatusCode,
          rpcSystemName,
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

  /** Measures the duration of inbound remote procedure calls (RPC).
    *
    * @note
    *   <p> When this metric is reported alongside an RPC server span, the metric value SHOULD be the same as the RPC
    *   server span duration.
    */
  object ServerCallDuration extends MetricSpec.Unsealed {

    val name: String = "rpc.server.call.duration"
    val description: String = "Measures the duration of inbound remote procedure calls (RPC)."
    val unit: String = "s"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Describes a class of error the operation ended with.
        *
        * @note
        *   <p> If the RPC fails with an error before status code is returned, `error.type` SHOULD be set to the
        *   exception type (its fully-qualified class name, if applicable) or a component-specific, low cardinality
        *   error identifier. <p> If a response status code is returned and status indicates an error, `error.type`
        *   SHOULD be set to that status code. Check system-specific conventions for the details on which values of
        *   `rpc.response.status_code` are considered errors. <p> The `error.type` value SHOULD be predictable and
        *   SHOULD have low cardinality. Instrumentations SHOULD document the list of errors they report. <p> If the
        *   request has completed successfully, instrumentations SHOULD NOT set `error.type`.
        */
      val errorType: AttributeSpec[String] =
        AttributeSpec(
          ErrorAttributes.ErrorType,
          List(
            "DEADLINE_EXCEEDED",
            "java.net.UnknownHostException",
            "-32602",
          ),
          Requirement.conditionallyRequired("If and only if the operation failed."),
          Stability.stable
        )

      /** <a href="https://wikipedia.org/wiki/Application_layer">OSI application layer</a> or non-OSI equivalent.
        *
        * @note
        *   <p> The value SHOULD be normalized to lowercase.
        */
      val networkProtocolName: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkProtocolName,
          List(
            "http",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** The actual version of the protocol used for network communication.
        *
        * @note
        *   <p> If protocol version is subject to negotiation (for example using <a
        *   href="https://www.rfc-editor.org/rfc/rfc7301.html">ALPN</a>), this attribute SHOULD be set to the negotiated
        *   version. If the actual protocol version is not known, this attribute SHOULD NOT be set.
        */
      val networkProtocolVersion: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkProtocolVersion,
          List(
            "1.1",
            "2",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** <a href="https://wikipedia.org/wiki/Transport_layer">OSI transport layer</a> or <a
        * href="https://wikipedia.org/wiki/Inter-process_communication">inter-process communication method</a>.
        *
        * @note
        *   <p> The value SHOULD be normalized to lowercase. <p> Consider always setting the transport when setting a
        *   port number, since a port number is ambiguous without knowing the transport. For example different processes
        *   could be listening on TCP port 12345 and UDP port 12345.
        */
      val networkTransport: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkTransport,
          List(
            "tcp",
            "udp",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** The fully-qualified logical name of the method from the RPC interface perspective.
        *
        * @note
        *   <p> The method name MAY have unbounded cardinality in edge or error cases. <p> Some RPC frameworks or
        *   libraries provide a fixed set of recognized methods for client stubs and server implementations.
        *   Instrumentations for such frameworks MUST set this attribute to the original method name only when the
        *   method is recognized by the framework or library. <p> When the method is not recognized, for example, when
        *   the server receives a request for a method that is not predefined on the server, or when instrumentation is
        *   not able to reliably detect if the method is predefined, the attribute MUST be set to `_OTHER`. In such
        *   cases, tracing instrumentations MUST also set `rpc.method_original` attribute to the original method value.
        *   <p> If the RPC instrumentation could end up converting valid RPC methods to `_OTHER`, then it SHOULD provide
        *   a way to configure the list of recognized RPC methods. <p> The `rpc.method` can be different from the name
        *   of any implementing method/function. The `code.function.name` attribute may be used to record the
        *   fully-qualified method actually executing the call on the server side, or the RPC client stub method on the
        *   client side.
        */
      val rpcMethod: AttributeSpec[String] =
        AttributeSpec(
          RpcExperimentalAttributes.RpcMethod,
          List(
            "com.example.ExampleService/exampleMethod",
            "EchoService/Echo",
            "_OTHER",
          ),
          Requirement.conditionallyRequired("if available."),
          Stability.development
        )

      /** Status code of the RPC returned by the RPC server or generated by the client
        *
        * @note
        *   <p> Usually it represents an error code, but may also represent partial success, warning, or differentiate
        *   between various types of successful outcomes. Semantic conventions for individual RPC frameworks SHOULD
        *   document what `rpc.response.status_code` means in the context of that system and which values are considered
        *   to represent errors.
        */
      val rpcResponseStatusCode: AttributeSpec[String] =
        AttributeSpec(
          RpcExperimentalAttributes.RpcResponseStatusCode,
          List(
            "OK",
            "DEADLINE_EXCEEDED",
            "-32602",
          ),
          Requirement.conditionallyRequired("if available."),
          Stability.development
        )

      /** The Remote Procedure Call (RPC) system.
        *
        * @note
        *   <p> The client and server RPC systems may differ for the same RPC interaction. For example, a client may use
        *   Apache Dubbo or Connect RPC to communicate with a server that uses gRPC since both protocols provide
        *   compatibility with gRPC.
        */
      val rpcSystemName: AttributeSpec[String] =
        AttributeSpec(
          RpcExperimentalAttributes.RpcSystemName,
          List(
          ),
          Requirement.required,
          Stability.development
        )

      /** RPC server <a href="https://grpc.github.io/grpc/core/md_doc_naming.html">host name</a>.
        */
      val serverAddress: AttributeSpec[String] =
        AttributeSpec(
          ServerAttributes.ServerAddress,
          List(
            "example.com",
            "10.1.2.80",
            "/tmp/my.sock",
          ),
          Requirement.optIn,
          Stability.stable
        )

      /** Server port number.
        */
      val serverPort: AttributeSpec[Long] =
        AttributeSpec(
          ServerAttributes.ServerPort,
          List(
            80,
            8080,
            443,
          ),
          Requirement.optIn,
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          errorType,
          networkProtocolName,
          networkProtocolVersion,
          networkTransport,
          rpcMethod,
          rpcResponseStatusCode,
          rpcSystemName,
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

  /** Deprecated, use `rpc.server.call.duration` instead. Note: the unit also changed from `ms` to `s`.
    *
    * @note
    *   <p> While streaming RPCs may record this metric as start-of-batch to end-of-batch, it's hard to interpret in
    *   practice. <p> <strong>Streaming</strong>: N/A.
    */
  @deprecated("Replaced by `rpc.server.call.duration` with unit `s`.", "")
  object ServerDuration extends MetricSpec.Unsealed {

    val name: String = "rpc.server.duration"
    val description: String =
      "Deprecated, use `rpc.server.call.duration` instead. Note: the unit also changed from `ms` to `s`."
    val unit: String = "ms"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Describes a class of error the operation ended with.
        *
        * @note
        *   <p> If the RPC fails with an error before status code is returned, `error.type` SHOULD be set to the
        *   exception type (its fully-qualified class name, if applicable) or a component-specific, low cardinality
        *   error identifier. <p> If a response status code is returned and status indicates an error, `error.type`
        *   SHOULD be set to that status code. Check system-specific conventions for the details on which values of
        *   `rpc.response.status_code` are considered errors. <p> The `error.type` value SHOULD be predictable and
        *   SHOULD have low cardinality. Instrumentations SHOULD document the list of errors they report. <p> If the
        *   request has completed successfully, instrumentations SHOULD NOT set `error.type`.
        */
      val errorType: AttributeSpec[String] =
        AttributeSpec(
          ErrorAttributes.ErrorType,
          List(
            "DEADLINE_EXCEEDED",
            "java.net.UnknownHostException",
            "-32602",
          ),
          Requirement.conditionallyRequired("If and only if the operation failed."),
          Stability.stable
        )

      /** <a href="https://wikipedia.org/wiki/Application_layer">OSI application layer</a> or non-OSI equivalent.
        *
        * @note
        *   <p> The value SHOULD be normalized to lowercase.
        */
      val networkProtocolName: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkProtocolName,
          List(
            "http",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** The actual version of the protocol used for network communication.
        *
        * @note
        *   <p> If protocol version is subject to negotiation (for example using <a
        *   href="https://www.rfc-editor.org/rfc/rfc7301.html">ALPN</a>), this attribute SHOULD be set to the negotiated
        *   version. If the actual protocol version is not known, this attribute SHOULD NOT be set.
        */
      val networkProtocolVersion: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkProtocolVersion,
          List(
            "1.1",
            "2",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** <a href="https://wikipedia.org/wiki/Transport_layer">OSI transport layer</a> or <a
        * href="https://wikipedia.org/wiki/Inter-process_communication">inter-process communication method</a>.
        *
        * @note
        *   <p> The value SHOULD be normalized to lowercase. <p> Consider always setting the transport when setting a
        *   port number, since a port number is ambiguous without knowing the transport. For example different processes
        *   could be listening on TCP port 12345 and UDP port 12345.
        */
      val networkTransport: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkTransport,
          List(
            "tcp",
            "udp",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** The fully-qualified logical name of the method from the RPC interface perspective.
        *
        * @note
        *   <p> The method name MAY have unbounded cardinality in edge or error cases. <p> Some RPC frameworks or
        *   libraries provide a fixed set of recognized methods for client stubs and server implementations.
        *   Instrumentations for such frameworks MUST set this attribute to the original method name only when the
        *   method is recognized by the framework or library. <p> When the method is not recognized, for example, when
        *   the server receives a request for a method that is not predefined on the server, or when instrumentation is
        *   not able to reliably detect if the method is predefined, the attribute MUST be set to `_OTHER`. In such
        *   cases, tracing instrumentations MUST also set `rpc.method_original` attribute to the original method value.
        *   <p> If the RPC instrumentation could end up converting valid RPC methods to `_OTHER`, then it SHOULD provide
        *   a way to configure the list of recognized RPC methods. <p> The `rpc.method` can be different from the name
        *   of any implementing method/function. The `code.function.name` attribute may be used to record the
        *   fully-qualified method actually executing the call on the server side, or the RPC client stub method on the
        *   client side.
        */
      val rpcMethod: AttributeSpec[String] =
        AttributeSpec(
          RpcExperimentalAttributes.RpcMethod,
          List(
            "com.example.ExampleService/exampleMethod",
            "EchoService/Echo",
            "_OTHER",
          ),
          Requirement.conditionallyRequired("if available."),
          Stability.development
        )

      /** Status code of the RPC returned by the RPC server or generated by the client
        *
        * @note
        *   <p> Usually it represents an error code, but may also represent partial success, warning, or differentiate
        *   between various types of successful outcomes. Semantic conventions for individual RPC frameworks SHOULD
        *   document what `rpc.response.status_code` means in the context of that system and which values are considered
        *   to represent errors.
        */
      val rpcResponseStatusCode: AttributeSpec[String] =
        AttributeSpec(
          RpcExperimentalAttributes.RpcResponseStatusCode,
          List(
            "OK",
            "DEADLINE_EXCEEDED",
            "-32602",
          ),
          Requirement.conditionallyRequired("if available."),
          Stability.development
        )

      /** The Remote Procedure Call (RPC) system.
        *
        * @note
        *   <p> The client and server RPC systems may differ for the same RPC interaction. For example, a client may use
        *   Apache Dubbo or Connect RPC to communicate with a server that uses gRPC since both protocols provide
        *   compatibility with gRPC.
        */
      val rpcSystemName: AttributeSpec[String] =
        AttributeSpec(
          RpcExperimentalAttributes.RpcSystemName,
          List(
          ),
          Requirement.required,
          Stability.development
        )

      /** RPC server <a href="https://grpc.github.io/grpc/core/md_doc_naming.html">host name</a>.
        */
      val serverAddress: AttributeSpec[String] =
        AttributeSpec(
          ServerAttributes.ServerAddress,
          List(
            "example.com",
            "10.1.2.80",
            "/tmp/my.sock",
          ),
          Requirement.optIn,
          Stability.stable
        )

      /** Server port number.
        */
      val serverPort: AttributeSpec[Long] =
        AttributeSpec(
          ServerAttributes.ServerPort,
          List(
            80,
            8080,
            443,
          ),
          Requirement.optIn,
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          errorType,
          networkProtocolName,
          networkProtocolVersion,
          networkTransport,
          rpcMethod,
          rpcResponseStatusCode,
          rpcSystemName,
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

  /** Measures the size of RPC request messages (uncompressed).
    *
    * @note
    *   <p> <strong>Streaming</strong>: Recorded per message in a streaming batch
    */
  object ServerRequestSize extends MetricSpec.Unsealed {

    val name: String = "rpc.server.request.size"
    val description: String = "Measures the size of RPC request messages (uncompressed)."
    val unit: String = "By"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Describes a class of error the operation ended with.
        *
        * @note
        *   <p> If the RPC fails with an error before status code is returned, `error.type` SHOULD be set to the
        *   exception type (its fully-qualified class name, if applicable) or a component-specific, low cardinality
        *   error identifier. <p> If a response status code is returned and status indicates an error, `error.type`
        *   SHOULD be set to that status code. Check system-specific conventions for the details on which values of
        *   `rpc.response.status_code` are considered errors. <p> The `error.type` value SHOULD be predictable and
        *   SHOULD have low cardinality. Instrumentations SHOULD document the list of errors they report. <p> If the
        *   request has completed successfully, instrumentations SHOULD NOT set `error.type`.
        */
      val errorType: AttributeSpec[String] =
        AttributeSpec(
          ErrorAttributes.ErrorType,
          List(
            "DEADLINE_EXCEEDED",
            "java.net.UnknownHostException",
            "-32602",
          ),
          Requirement.conditionallyRequired("If and only if the operation failed."),
          Stability.stable
        )

      /** <a href="https://wikipedia.org/wiki/Application_layer">OSI application layer</a> or non-OSI equivalent.
        *
        * @note
        *   <p> The value SHOULD be normalized to lowercase.
        */
      val networkProtocolName: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkProtocolName,
          List(
            "http",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** The actual version of the protocol used for network communication.
        *
        * @note
        *   <p> If protocol version is subject to negotiation (for example using <a
        *   href="https://www.rfc-editor.org/rfc/rfc7301.html">ALPN</a>), this attribute SHOULD be set to the negotiated
        *   version. If the actual protocol version is not known, this attribute SHOULD NOT be set.
        */
      val networkProtocolVersion: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkProtocolVersion,
          List(
            "1.1",
            "2",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** <a href="https://wikipedia.org/wiki/Transport_layer">OSI transport layer</a> or <a
        * href="https://wikipedia.org/wiki/Inter-process_communication">inter-process communication method</a>.
        *
        * @note
        *   <p> The value SHOULD be normalized to lowercase. <p> Consider always setting the transport when setting a
        *   port number, since a port number is ambiguous without knowing the transport. For example different processes
        *   could be listening on TCP port 12345 and UDP port 12345.
        */
      val networkTransport: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkTransport,
          List(
            "tcp",
            "udp",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** The fully-qualified logical name of the method from the RPC interface perspective.
        *
        * @note
        *   <p> The method name MAY have unbounded cardinality in edge or error cases. <p> Some RPC frameworks or
        *   libraries provide a fixed set of recognized methods for client stubs and server implementations.
        *   Instrumentations for such frameworks MUST set this attribute to the original method name only when the
        *   method is recognized by the framework or library. <p> When the method is not recognized, for example, when
        *   the server receives a request for a method that is not predefined on the server, or when instrumentation is
        *   not able to reliably detect if the method is predefined, the attribute MUST be set to `_OTHER`. In such
        *   cases, tracing instrumentations MUST also set `rpc.method_original` attribute to the original method value.
        *   <p> If the RPC instrumentation could end up converting valid RPC methods to `_OTHER`, then it SHOULD provide
        *   a way to configure the list of recognized RPC methods. <p> The `rpc.method` can be different from the name
        *   of any implementing method/function. The `code.function.name` attribute may be used to record the
        *   fully-qualified method actually executing the call on the server side, or the RPC client stub method on the
        *   client side.
        */
      val rpcMethod: AttributeSpec[String] =
        AttributeSpec(
          RpcExperimentalAttributes.RpcMethod,
          List(
            "com.example.ExampleService/exampleMethod",
            "EchoService/Echo",
            "_OTHER",
          ),
          Requirement.conditionallyRequired("if available."),
          Stability.development
        )

      /** Status code of the RPC returned by the RPC server or generated by the client
        *
        * @note
        *   <p> Usually it represents an error code, but may also represent partial success, warning, or differentiate
        *   between various types of successful outcomes. Semantic conventions for individual RPC frameworks SHOULD
        *   document what `rpc.response.status_code` means in the context of that system and which values are considered
        *   to represent errors.
        */
      val rpcResponseStatusCode: AttributeSpec[String] =
        AttributeSpec(
          RpcExperimentalAttributes.RpcResponseStatusCode,
          List(
            "OK",
            "DEADLINE_EXCEEDED",
            "-32602",
          ),
          Requirement.conditionallyRequired("if available."),
          Stability.development
        )

      /** The Remote Procedure Call (RPC) system.
        *
        * @note
        *   <p> The client and server RPC systems may differ for the same RPC interaction. For example, a client may use
        *   Apache Dubbo or Connect RPC to communicate with a server that uses gRPC since both protocols provide
        *   compatibility with gRPC.
        */
      val rpcSystemName: AttributeSpec[String] =
        AttributeSpec(
          RpcExperimentalAttributes.RpcSystemName,
          List(
          ),
          Requirement.required,
          Stability.development
        )

      /** RPC server <a href="https://grpc.github.io/grpc/core/md_doc_naming.html">host name</a>.
        */
      val serverAddress: AttributeSpec[String] =
        AttributeSpec(
          ServerAttributes.ServerAddress,
          List(
            "example.com",
            "10.1.2.80",
            "/tmp/my.sock",
          ),
          Requirement.optIn,
          Stability.stable
        )

      /** Server port number.
        */
      val serverPort: AttributeSpec[Long] =
        AttributeSpec(
          ServerAttributes.ServerPort,
          List(
            80,
            8080,
            443,
          ),
          Requirement.optIn,
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          errorType,
          networkProtocolName,
          networkProtocolVersion,
          networkTransport,
          rpcMethod,
          rpcResponseStatusCode,
          rpcSystemName,
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

  /** Measures the number of messages received per RPC.
    *
    * @note
    *   <p> Should be 1 for all non-streaming RPCs. <p> <strong>Streaming</strong> : This metric is required for server
    *   and client streaming RPCs
    */
  @deprecated("Removed, no replacement at this time.", "")
  object ServerRequestsPerRpc extends MetricSpec.Unsealed {

    val name: String = "rpc.server.requests_per_rpc"
    val description: String = "Measures the number of messages received per RPC."
    val unit: String = "{count}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Describes a class of error the operation ended with.
        *
        * @note
        *   <p> If the RPC fails with an error before status code is returned, `error.type` SHOULD be set to the
        *   exception type (its fully-qualified class name, if applicable) or a component-specific, low cardinality
        *   error identifier. <p> If a response status code is returned and status indicates an error, `error.type`
        *   SHOULD be set to that status code. Check system-specific conventions for the details on which values of
        *   `rpc.response.status_code` are considered errors. <p> The `error.type` value SHOULD be predictable and
        *   SHOULD have low cardinality. Instrumentations SHOULD document the list of errors they report. <p> If the
        *   request has completed successfully, instrumentations SHOULD NOT set `error.type`.
        */
      val errorType: AttributeSpec[String] =
        AttributeSpec(
          ErrorAttributes.ErrorType,
          List(
            "DEADLINE_EXCEEDED",
            "java.net.UnknownHostException",
            "-32602",
          ),
          Requirement.conditionallyRequired("If and only if the operation failed."),
          Stability.stable
        )

      /** <a href="https://wikipedia.org/wiki/Application_layer">OSI application layer</a> or non-OSI equivalent.
        *
        * @note
        *   <p> The value SHOULD be normalized to lowercase.
        */
      val networkProtocolName: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkProtocolName,
          List(
            "http",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** The actual version of the protocol used for network communication.
        *
        * @note
        *   <p> If protocol version is subject to negotiation (for example using <a
        *   href="https://www.rfc-editor.org/rfc/rfc7301.html">ALPN</a>), this attribute SHOULD be set to the negotiated
        *   version. If the actual protocol version is not known, this attribute SHOULD NOT be set.
        */
      val networkProtocolVersion: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkProtocolVersion,
          List(
            "1.1",
            "2",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** <a href="https://wikipedia.org/wiki/Transport_layer">OSI transport layer</a> or <a
        * href="https://wikipedia.org/wiki/Inter-process_communication">inter-process communication method</a>.
        *
        * @note
        *   <p> The value SHOULD be normalized to lowercase. <p> Consider always setting the transport when setting a
        *   port number, since a port number is ambiguous without knowing the transport. For example different processes
        *   could be listening on TCP port 12345 and UDP port 12345.
        */
      val networkTransport: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkTransport,
          List(
            "tcp",
            "udp",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** The fully-qualified logical name of the method from the RPC interface perspective.
        *
        * @note
        *   <p> The method name MAY have unbounded cardinality in edge or error cases. <p> Some RPC frameworks or
        *   libraries provide a fixed set of recognized methods for client stubs and server implementations.
        *   Instrumentations for such frameworks MUST set this attribute to the original method name only when the
        *   method is recognized by the framework or library. <p> When the method is not recognized, for example, when
        *   the server receives a request for a method that is not predefined on the server, or when instrumentation is
        *   not able to reliably detect if the method is predefined, the attribute MUST be set to `_OTHER`. In such
        *   cases, tracing instrumentations MUST also set `rpc.method_original` attribute to the original method value.
        *   <p> If the RPC instrumentation could end up converting valid RPC methods to `_OTHER`, then it SHOULD provide
        *   a way to configure the list of recognized RPC methods. <p> The `rpc.method` can be different from the name
        *   of any implementing method/function. The `code.function.name` attribute may be used to record the
        *   fully-qualified method actually executing the call on the server side, or the RPC client stub method on the
        *   client side.
        */
      val rpcMethod: AttributeSpec[String] =
        AttributeSpec(
          RpcExperimentalAttributes.RpcMethod,
          List(
            "com.example.ExampleService/exampleMethod",
            "EchoService/Echo",
            "_OTHER",
          ),
          Requirement.conditionallyRequired("if available."),
          Stability.development
        )

      /** Status code of the RPC returned by the RPC server or generated by the client
        *
        * @note
        *   <p> Usually it represents an error code, but may also represent partial success, warning, or differentiate
        *   between various types of successful outcomes. Semantic conventions for individual RPC frameworks SHOULD
        *   document what `rpc.response.status_code` means in the context of that system and which values are considered
        *   to represent errors.
        */
      val rpcResponseStatusCode: AttributeSpec[String] =
        AttributeSpec(
          RpcExperimentalAttributes.RpcResponseStatusCode,
          List(
            "OK",
            "DEADLINE_EXCEEDED",
            "-32602",
          ),
          Requirement.conditionallyRequired("if available."),
          Stability.development
        )

      /** The Remote Procedure Call (RPC) system.
        *
        * @note
        *   <p> The client and server RPC systems may differ for the same RPC interaction. For example, a client may use
        *   Apache Dubbo or Connect RPC to communicate with a server that uses gRPC since both protocols provide
        *   compatibility with gRPC.
        */
      val rpcSystemName: AttributeSpec[String] =
        AttributeSpec(
          RpcExperimentalAttributes.RpcSystemName,
          List(
          ),
          Requirement.required,
          Stability.development
        )

      /** RPC server <a href="https://grpc.github.io/grpc/core/md_doc_naming.html">host name</a>.
        */
      val serverAddress: AttributeSpec[String] =
        AttributeSpec(
          ServerAttributes.ServerAddress,
          List(
            "example.com",
            "10.1.2.80",
            "/tmp/my.sock",
          ),
          Requirement.optIn,
          Stability.stable
        )

      /** Server port number.
        */
      val serverPort: AttributeSpec[Long] =
        AttributeSpec(
          ServerAttributes.ServerPort,
          List(
            80,
            8080,
            443,
          ),
          Requirement.optIn,
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          errorType,
          networkProtocolName,
          networkProtocolVersion,
          networkTransport,
          rpcMethod,
          rpcResponseStatusCode,
          rpcSystemName,
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

  /** Measures the size of RPC response messages (uncompressed).
    *
    * @note
    *   <p> <strong>Streaming</strong>: Recorded per response in a streaming batch
    */
  object ServerResponseSize extends MetricSpec.Unsealed {

    val name: String = "rpc.server.response.size"
    val description: String = "Measures the size of RPC response messages (uncompressed)."
    val unit: String = "By"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Describes a class of error the operation ended with.
        *
        * @note
        *   <p> If the RPC fails with an error before status code is returned, `error.type` SHOULD be set to the
        *   exception type (its fully-qualified class name, if applicable) or a component-specific, low cardinality
        *   error identifier. <p> If a response status code is returned and status indicates an error, `error.type`
        *   SHOULD be set to that status code. Check system-specific conventions for the details on which values of
        *   `rpc.response.status_code` are considered errors. <p> The `error.type` value SHOULD be predictable and
        *   SHOULD have low cardinality. Instrumentations SHOULD document the list of errors they report. <p> If the
        *   request has completed successfully, instrumentations SHOULD NOT set `error.type`.
        */
      val errorType: AttributeSpec[String] =
        AttributeSpec(
          ErrorAttributes.ErrorType,
          List(
            "DEADLINE_EXCEEDED",
            "java.net.UnknownHostException",
            "-32602",
          ),
          Requirement.conditionallyRequired("If and only if the operation failed."),
          Stability.stable
        )

      /** <a href="https://wikipedia.org/wiki/Application_layer">OSI application layer</a> or non-OSI equivalent.
        *
        * @note
        *   <p> The value SHOULD be normalized to lowercase.
        */
      val networkProtocolName: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkProtocolName,
          List(
            "http",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** The actual version of the protocol used for network communication.
        *
        * @note
        *   <p> If protocol version is subject to negotiation (for example using <a
        *   href="https://www.rfc-editor.org/rfc/rfc7301.html">ALPN</a>), this attribute SHOULD be set to the negotiated
        *   version. If the actual protocol version is not known, this attribute SHOULD NOT be set.
        */
      val networkProtocolVersion: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkProtocolVersion,
          List(
            "1.1",
            "2",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** <a href="https://wikipedia.org/wiki/Transport_layer">OSI transport layer</a> or <a
        * href="https://wikipedia.org/wiki/Inter-process_communication">inter-process communication method</a>.
        *
        * @note
        *   <p> The value SHOULD be normalized to lowercase. <p> Consider always setting the transport when setting a
        *   port number, since a port number is ambiguous without knowing the transport. For example different processes
        *   could be listening on TCP port 12345 and UDP port 12345.
        */
      val networkTransport: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkTransport,
          List(
            "tcp",
            "udp",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** The fully-qualified logical name of the method from the RPC interface perspective.
        *
        * @note
        *   <p> The method name MAY have unbounded cardinality in edge or error cases. <p> Some RPC frameworks or
        *   libraries provide a fixed set of recognized methods for client stubs and server implementations.
        *   Instrumentations for such frameworks MUST set this attribute to the original method name only when the
        *   method is recognized by the framework or library. <p> When the method is not recognized, for example, when
        *   the server receives a request for a method that is not predefined on the server, or when instrumentation is
        *   not able to reliably detect if the method is predefined, the attribute MUST be set to `_OTHER`. In such
        *   cases, tracing instrumentations MUST also set `rpc.method_original` attribute to the original method value.
        *   <p> If the RPC instrumentation could end up converting valid RPC methods to `_OTHER`, then it SHOULD provide
        *   a way to configure the list of recognized RPC methods. <p> The `rpc.method` can be different from the name
        *   of any implementing method/function. The `code.function.name` attribute may be used to record the
        *   fully-qualified method actually executing the call on the server side, or the RPC client stub method on the
        *   client side.
        */
      val rpcMethod: AttributeSpec[String] =
        AttributeSpec(
          RpcExperimentalAttributes.RpcMethod,
          List(
            "com.example.ExampleService/exampleMethod",
            "EchoService/Echo",
            "_OTHER",
          ),
          Requirement.conditionallyRequired("if available."),
          Stability.development
        )

      /** Status code of the RPC returned by the RPC server or generated by the client
        *
        * @note
        *   <p> Usually it represents an error code, but may also represent partial success, warning, or differentiate
        *   between various types of successful outcomes. Semantic conventions for individual RPC frameworks SHOULD
        *   document what `rpc.response.status_code` means in the context of that system and which values are considered
        *   to represent errors.
        */
      val rpcResponseStatusCode: AttributeSpec[String] =
        AttributeSpec(
          RpcExperimentalAttributes.RpcResponseStatusCode,
          List(
            "OK",
            "DEADLINE_EXCEEDED",
            "-32602",
          ),
          Requirement.conditionallyRequired("if available."),
          Stability.development
        )

      /** The Remote Procedure Call (RPC) system.
        *
        * @note
        *   <p> The client and server RPC systems may differ for the same RPC interaction. For example, a client may use
        *   Apache Dubbo or Connect RPC to communicate with a server that uses gRPC since both protocols provide
        *   compatibility with gRPC.
        */
      val rpcSystemName: AttributeSpec[String] =
        AttributeSpec(
          RpcExperimentalAttributes.RpcSystemName,
          List(
          ),
          Requirement.required,
          Stability.development
        )

      /** RPC server <a href="https://grpc.github.io/grpc/core/md_doc_naming.html">host name</a>.
        */
      val serverAddress: AttributeSpec[String] =
        AttributeSpec(
          ServerAttributes.ServerAddress,
          List(
            "example.com",
            "10.1.2.80",
            "/tmp/my.sock",
          ),
          Requirement.optIn,
          Stability.stable
        )

      /** Server port number.
        */
      val serverPort: AttributeSpec[Long] =
        AttributeSpec(
          ServerAttributes.ServerPort,
          List(
            80,
            8080,
            443,
          ),
          Requirement.optIn,
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          errorType,
          networkProtocolName,
          networkProtocolVersion,
          networkTransport,
          rpcMethod,
          rpcResponseStatusCode,
          rpcSystemName,
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

  /** Measures the number of messages sent per RPC.
    *
    * @note
    *   <p> Should be 1 for all non-streaming RPCs. <p> <strong>Streaming</strong>: This metric is required for server
    *   and client streaming RPCs
    */
  @deprecated("Removed, no replacement at this time.", "")
  object ServerResponsesPerRpc extends MetricSpec.Unsealed {

    val name: String = "rpc.server.responses_per_rpc"
    val description: String = "Measures the number of messages sent per RPC."
    val unit: String = "{count}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Describes a class of error the operation ended with.
        *
        * @note
        *   <p> If the RPC fails with an error before status code is returned, `error.type` SHOULD be set to the
        *   exception type (its fully-qualified class name, if applicable) or a component-specific, low cardinality
        *   error identifier. <p> If a response status code is returned and status indicates an error, `error.type`
        *   SHOULD be set to that status code. Check system-specific conventions for the details on which values of
        *   `rpc.response.status_code` are considered errors. <p> The `error.type` value SHOULD be predictable and
        *   SHOULD have low cardinality. Instrumentations SHOULD document the list of errors they report. <p> If the
        *   request has completed successfully, instrumentations SHOULD NOT set `error.type`.
        */
      val errorType: AttributeSpec[String] =
        AttributeSpec(
          ErrorAttributes.ErrorType,
          List(
            "DEADLINE_EXCEEDED",
            "java.net.UnknownHostException",
            "-32602",
          ),
          Requirement.conditionallyRequired("If and only if the operation failed."),
          Stability.stable
        )

      /** <a href="https://wikipedia.org/wiki/Application_layer">OSI application layer</a> or non-OSI equivalent.
        *
        * @note
        *   <p> The value SHOULD be normalized to lowercase.
        */
      val networkProtocolName: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkProtocolName,
          List(
            "http",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** The actual version of the protocol used for network communication.
        *
        * @note
        *   <p> If protocol version is subject to negotiation (for example using <a
        *   href="https://www.rfc-editor.org/rfc/rfc7301.html">ALPN</a>), this attribute SHOULD be set to the negotiated
        *   version. If the actual protocol version is not known, this attribute SHOULD NOT be set.
        */
      val networkProtocolVersion: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkProtocolVersion,
          List(
            "1.1",
            "2",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** <a href="https://wikipedia.org/wiki/Transport_layer">OSI transport layer</a> or <a
        * href="https://wikipedia.org/wiki/Inter-process_communication">inter-process communication method</a>.
        *
        * @note
        *   <p> The value SHOULD be normalized to lowercase. <p> Consider always setting the transport when setting a
        *   port number, since a port number is ambiguous without knowing the transport. For example different processes
        *   could be listening on TCP port 12345 and UDP port 12345.
        */
      val networkTransport: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkTransport,
          List(
            "tcp",
            "udp",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** The fully-qualified logical name of the method from the RPC interface perspective.
        *
        * @note
        *   <p> The method name MAY have unbounded cardinality in edge or error cases. <p> Some RPC frameworks or
        *   libraries provide a fixed set of recognized methods for client stubs and server implementations.
        *   Instrumentations for such frameworks MUST set this attribute to the original method name only when the
        *   method is recognized by the framework or library. <p> When the method is not recognized, for example, when
        *   the server receives a request for a method that is not predefined on the server, or when instrumentation is
        *   not able to reliably detect if the method is predefined, the attribute MUST be set to `_OTHER`. In such
        *   cases, tracing instrumentations MUST also set `rpc.method_original` attribute to the original method value.
        *   <p> If the RPC instrumentation could end up converting valid RPC methods to `_OTHER`, then it SHOULD provide
        *   a way to configure the list of recognized RPC methods. <p> The `rpc.method` can be different from the name
        *   of any implementing method/function. The `code.function.name` attribute may be used to record the
        *   fully-qualified method actually executing the call on the server side, or the RPC client stub method on the
        *   client side.
        */
      val rpcMethod: AttributeSpec[String] =
        AttributeSpec(
          RpcExperimentalAttributes.RpcMethod,
          List(
            "com.example.ExampleService/exampleMethod",
            "EchoService/Echo",
            "_OTHER",
          ),
          Requirement.conditionallyRequired("if available."),
          Stability.development
        )

      /** Status code of the RPC returned by the RPC server or generated by the client
        *
        * @note
        *   <p> Usually it represents an error code, but may also represent partial success, warning, or differentiate
        *   between various types of successful outcomes. Semantic conventions for individual RPC frameworks SHOULD
        *   document what `rpc.response.status_code` means in the context of that system and which values are considered
        *   to represent errors.
        */
      val rpcResponseStatusCode: AttributeSpec[String] =
        AttributeSpec(
          RpcExperimentalAttributes.RpcResponseStatusCode,
          List(
            "OK",
            "DEADLINE_EXCEEDED",
            "-32602",
          ),
          Requirement.conditionallyRequired("if available."),
          Stability.development
        )

      /** The Remote Procedure Call (RPC) system.
        *
        * @note
        *   <p> The client and server RPC systems may differ for the same RPC interaction. For example, a client may use
        *   Apache Dubbo or Connect RPC to communicate with a server that uses gRPC since both protocols provide
        *   compatibility with gRPC.
        */
      val rpcSystemName: AttributeSpec[String] =
        AttributeSpec(
          RpcExperimentalAttributes.RpcSystemName,
          List(
          ),
          Requirement.required,
          Stability.development
        )

      /** RPC server <a href="https://grpc.github.io/grpc/core/md_doc_naming.html">host name</a>.
        */
      val serverAddress: AttributeSpec[String] =
        AttributeSpec(
          ServerAttributes.ServerAddress,
          List(
            "example.com",
            "10.1.2.80",
            "/tmp/my.sock",
          ),
          Requirement.optIn,
          Stability.stable
        )

      /** Server port number.
        */
      val serverPort: AttributeSpec[Long] =
        AttributeSpec(
          ServerAttributes.ServerPort,
          List(
            80,
            8080,
            443,
          ),
          Requirement.optIn,
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          errorType,
          networkProtocolName,
          networkProtocolVersion,
          networkTransport,
          rpcMethod,
          rpcResponseStatusCode,
          rpcSystemName,
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

}
