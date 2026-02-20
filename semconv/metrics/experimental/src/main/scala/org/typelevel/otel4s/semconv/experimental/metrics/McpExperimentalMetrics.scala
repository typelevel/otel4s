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
object McpExperimentalMetrics {

  val specs: List[MetricSpec] = List(
    ClientOperationDuration,
    ClientSessionDuration,
    ServerOperationDuration,
    ServerSessionDuration,
  )

  /** The duration of the MCP request or notification as observed on the sender from the time it was sent until the
    * response or ack is received.
    */
  object ClientOperationDuration extends MetricSpec.Unsealed {

    val name: String = "mcp.client.operation.duration"
    val description: String =
      "The duration of the MCP request or notification as observed on the sender from the time it was sent until the response or ack is received."
    val unit: String = "s"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Describes a class of error the operation ended with.
        *
        * @note
        *   <p> This attribute SHOULD be set to the string representation of the JSON-RPC error code, if one is
        *   returned. <p> When JSON-RPC call is successful, but an error is returned within the result payload, this
        *   attribute SHOULD be set to the low-cardinality string representation of the error. When <a
        *   href="https://github.com/modelcontextprotocol/modelcontextprotocol/blob/9c8a44e47e16b789a1f9d47c89ea23ed13a37cf9/schema/2025-06-18/schema.ts#L715">CallToolResult</a>
        *   is returned with `isError` set to `true`, this attribute SHOULD be set to `tool_error`.
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
          Requirement.conditionallyRequired("If and only if the operation fails."),
          Stability.stable
        )

      /** The name of the GenAI operation being performed.
        *
        * @note
        *   <p> Populating this attribute for tool calling along with `mcp.method.name` allows consumers to treat MCP
        *   tool calls spans similarly with other tool call types.
        */
      val genAiOperationName: AttributeSpec[String] =
        AttributeSpec(
          GenAiExperimentalAttributes.GenAiOperationName,
          List(
            "execute_tool",
          ),
          Requirement.recommended(
            "SHOULD be set to `execute_tool` when the operation describes a tool call and SHOULD NOT be set otherwise."
          ),
          Stability.development
        )

      /** The name of the prompt or prompt template provided in the request or response.
        */
      val genAiPromptName: AttributeSpec[String] =
        AttributeSpec(
          GenAiExperimentalAttributes.GenAiPromptName,
          List(
            "analyze-code",
          ),
          Requirement.conditionallyRequired("When operation is related to a specific prompt."),
          Stability.development
        )

      /** Name of the tool utilized by the agent.
        */
      val genAiToolName: AttributeSpec[String] =
        AttributeSpec(
          GenAiExperimentalAttributes.GenAiToolName,
          List(
            "Flights",
          ),
          Requirement.conditionallyRequired("When operation is related to a specific tool."),
          Stability.development
        )

      /** Protocol version, as specified in the `jsonrpc` property of the request and its corresponding response.
        */
      val jsonrpcProtocolVersion: AttributeSpec[String] =
        AttributeSpec(
          JsonrpcExperimentalAttributes.JsonrpcProtocolVersion,
          List(
            "2.0",
            "1.0",
          ),
          Requirement.recommended("when it's not `2.0`."),
          Stability.development
        )

      /** The name of the request or notification method.
        */
      val mcpMethodName: AttributeSpec[String] =
        AttributeSpec(
          McpExperimentalAttributes.McpMethodName,
          List(
          ),
          Requirement.required,
          Stability.development
        )

      /** The <a href="https://modelcontextprotocol.io/specification/versioning">version</a> of the Model Context
        * Protocol used.
        */
      val mcpProtocolVersion: AttributeSpec[String] =
        AttributeSpec(
          McpExperimentalAttributes.McpProtocolVersion,
          List(
            "2025-06-18",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The value of the resource uri.
        *
        * @note
        *   <p> This is a URI of the resource provided in the following requests or notifications: `resources/read`,
        *   `resources/subscribe`, `resources/unsubscribe`, or `notifications/resources/updated`.
        */
      val mcpResourceUri: AttributeSpec[String] =
        AttributeSpec(
          McpExperimentalAttributes.McpResourceUri,
          List(
            "postgres://database/customers/schema",
            "file:///home/user/documents/report.pdf",
          ),
          Requirement.optIn,
          Stability.development
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
            "websocket",
          ),
          Requirement.recommended("When applicable."),
          Stability.stable
        )

      /** The actual version of the protocol used for network communication.
        */
      val networkProtocolVersion: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkProtocolVersion,
          List(
            "1.1",
            "2",
          ),
          Requirement.recommended("When applicable."),
          Stability.stable
        )

      /** The transport protocol used for the MCP session.
        *
        * @note
        *   <p> This attribute SHOULD be set to `tcp` or `quic` if the transport protocol is HTTP. It SHOULD be set to
        *   `pipe` if the transport is stdio.
        */
      val networkTransport: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkTransport,
          List(
            "tcp",
            "quic",
            "pipe",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** The error code from the JSON-RPC response.
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
          Requirement.conditionallyRequired("If response contains an error code."),
          Stability.releaseCandidate
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
          Requirement.recommended("If applicable"),
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
          Requirement.recommended("When `server.address` is set"),
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          errorType,
          genAiOperationName,
          genAiPromptName,
          genAiToolName,
          jsonrpcProtocolVersion,
          mcpMethodName,
          mcpProtocolVersion,
          mcpResourceUri,
          networkProtocolName,
          networkProtocolVersion,
          networkTransport,
          rpcResponseStatusCode,
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

  /** The duration of the MCP session as observed on the MCP client.
    */
  object ClientSessionDuration extends MetricSpec.Unsealed {

    val name: String = "mcp.client.session.duration"
    val description: String = "The duration of the MCP session as observed on the MCP client."
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
        *   error identifiers (such as HTTP or RPC status codes), it's RECOMMENDED to: <ul> <li>Use a domain-specific
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
          Requirement.conditionallyRequired("If and only if session ends with an error."),
          Stability.stable
        )

      /** Protocol version, as specified in the `jsonrpc` property of the request and its corresponding response.
        */
      val jsonrpcProtocolVersion: AttributeSpec[String] =
        AttributeSpec(
          JsonrpcExperimentalAttributes.JsonrpcProtocolVersion,
          List(
            "2.0",
            "1.0",
          ),
          Requirement.recommended("when it's not `2.0`."),
          Stability.development
        )

      /** The <a href="https://modelcontextprotocol.io/specification/versioning">version</a> of the Model Context
        * Protocol used.
        */
      val mcpProtocolVersion: AttributeSpec[String] =
        AttributeSpec(
          McpExperimentalAttributes.McpProtocolVersion,
          List(
            "2025-06-18",
          ),
          Requirement.recommended,
          Stability.development
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
            "websocket",
          ),
          Requirement.recommended("When applicable."),
          Stability.stable
        )

      /** The actual version of the protocol used for network communication.
        */
      val networkProtocolVersion: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkProtocolVersion,
          List(
            "1.1",
            "2",
          ),
          Requirement.recommended("When applicable."),
          Stability.stable
        )

      /** The transport protocol used for the MCP session.
        *
        * @note
        *   <p> This attribute SHOULD be set to `tcp` or `quic` if the transport protocol is HTTP. It SHOULD be set to
        *   `pipe` if the transport is stdio.
        */
      val networkTransport: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkTransport,
          List(
            "tcp",
            "quic",
            "pipe",
          ),
          Requirement.recommended,
          Stability.stable
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
          Requirement.recommended("If applicable"),
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
          Requirement.recommended("When `server.address` is set"),
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          errorType,
          jsonrpcProtocolVersion,
          mcpProtocolVersion,
          networkProtocolName,
          networkProtocolVersion,
          networkTransport,
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

  /** MCP request or notification duration as observed on the receiver from the time it was received until the result or
    * ack is sent.
    */
  object ServerOperationDuration extends MetricSpec.Unsealed {

    val name: String = "mcp.server.operation.duration"
    val description: String =
      "MCP request or notification duration as observed on the receiver from the time it was received until the result or ack is sent."
    val unit: String = "s"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Describes a class of error the operation ended with.
        *
        * @note
        *   <p> This attribute SHOULD be set to the string representation of the JSON-RPC error code, if one is
        *   returned. <p> When JSON-RPC call is successful, but an error is returned within the result payload, this
        *   attribute SHOULD be set to the low-cardinality string representation of the error. When <a
        *   href="https://github.com/modelcontextprotocol/modelcontextprotocol/blob/9c8a44e47e16b789a1f9d47c89ea23ed13a37cf9/schema/2025-06-18/schema.ts#L715">CallToolResult</a>
        *   is returned with `isError` set to `true`, this attribute SHOULD be set to `tool_error`.
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
          Requirement.conditionallyRequired("If and only if the operation fails."),
          Stability.stable
        )

      /** The name of the GenAI operation being performed.
        *
        * @note
        *   <p> Populating this attribute for tool calling along with `mcp.method.name` allows consumers to treat MCP
        *   tool calls spans similarly with other tool call types.
        */
      val genAiOperationName: AttributeSpec[String] =
        AttributeSpec(
          GenAiExperimentalAttributes.GenAiOperationName,
          List(
            "execute_tool",
          ),
          Requirement.recommended(
            "SHOULD be set to `execute_tool` when the operation describes a tool call and SHOULD NOT be set otherwise."
          ),
          Stability.development
        )

      /** The name of the prompt or prompt template provided in the request or response.
        */
      val genAiPromptName: AttributeSpec[String] =
        AttributeSpec(
          GenAiExperimentalAttributes.GenAiPromptName,
          List(
            "analyze-code",
          ),
          Requirement.conditionallyRequired("When operation is related to a specific prompt."),
          Stability.development
        )

      /** Name of the tool utilized by the agent.
        */
      val genAiToolName: AttributeSpec[String] =
        AttributeSpec(
          GenAiExperimentalAttributes.GenAiToolName,
          List(
            "Flights",
          ),
          Requirement.conditionallyRequired("When operation is related to a specific tool."),
          Stability.development
        )

      /** Protocol version, as specified in the `jsonrpc` property of the request and its corresponding response.
        */
      val jsonrpcProtocolVersion: AttributeSpec[String] =
        AttributeSpec(
          JsonrpcExperimentalAttributes.JsonrpcProtocolVersion,
          List(
            "2.0",
            "1.0",
          ),
          Requirement.recommended("when it's not `2.0`."),
          Stability.development
        )

      /** The name of the request or notification method.
        */
      val mcpMethodName: AttributeSpec[String] =
        AttributeSpec(
          McpExperimentalAttributes.McpMethodName,
          List(
          ),
          Requirement.required,
          Stability.development
        )

      /** The <a href="https://modelcontextprotocol.io/specification/versioning">version</a> of the Model Context
        * Protocol used.
        */
      val mcpProtocolVersion: AttributeSpec[String] =
        AttributeSpec(
          McpExperimentalAttributes.McpProtocolVersion,
          List(
            "2025-06-18",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The value of the resource uri.
        *
        * @note
        *   <p> This is a URI of the resource provided in the following requests or notifications: `resources/read`,
        *   `resources/subscribe`, `resources/unsubscribe`, or `notifications/resources/updated`.
        */
      val mcpResourceUri: AttributeSpec[String] =
        AttributeSpec(
          McpExperimentalAttributes.McpResourceUri,
          List(
            "postgres://database/customers/schema",
            "file:///home/user/documents/report.pdf",
          ),
          Requirement.optIn,
          Stability.development
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
            "websocket",
          ),
          Requirement.recommended("When applicable."),
          Stability.stable
        )

      /** The actual version of the protocol used for network communication.
        */
      val networkProtocolVersion: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkProtocolVersion,
          List(
            "1.1",
            "2",
          ),
          Requirement.recommended("When applicable."),
          Stability.stable
        )

      /** The transport protocol used for the MCP session.
        *
        * @note
        *   <p> This attribute SHOULD be set to `tcp` or `quic` if the transport protocol is HTTP. It SHOULD be set to
        *   `pipe` if the transport is stdio.
        */
      val networkTransport: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkTransport,
          List(
            "tcp",
            "quic",
            "pipe",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** The error code from the JSON-RPC response.
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
          Requirement.conditionallyRequired("If response contains an error code."),
          Stability.releaseCandidate
        )

      val specs: List[AttributeSpec[_]] =
        List(
          errorType,
          genAiOperationName,
          genAiPromptName,
          genAiToolName,
          jsonrpcProtocolVersion,
          mcpMethodName,
          mcpProtocolVersion,
          mcpResourceUri,
          networkProtocolName,
          networkProtocolVersion,
          networkTransport,
          rpcResponseStatusCode,
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

  /** The duration of the MCP session as observed on the MCP server.
    */
  object ServerSessionDuration extends MetricSpec.Unsealed {

    val name: String = "mcp.server.session.duration"
    val description: String = "The duration of the MCP session as observed on the MCP server."
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
        *   error identifiers (such as HTTP or RPC status codes), it's RECOMMENDED to: <ul> <li>Use a domain-specific
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
          Requirement.conditionallyRequired("If and only if session ends with an error."),
          Stability.stable
        )

      /** Protocol version, as specified in the `jsonrpc` property of the request and its corresponding response.
        */
      val jsonrpcProtocolVersion: AttributeSpec[String] =
        AttributeSpec(
          JsonrpcExperimentalAttributes.JsonrpcProtocolVersion,
          List(
            "2.0",
            "1.0",
          ),
          Requirement.recommended("when it's not `2.0`."),
          Stability.development
        )

      /** The <a href="https://modelcontextprotocol.io/specification/versioning">version</a> of the Model Context
        * Protocol used.
        */
      val mcpProtocolVersion: AttributeSpec[String] =
        AttributeSpec(
          McpExperimentalAttributes.McpProtocolVersion,
          List(
            "2025-06-18",
          ),
          Requirement.recommended,
          Stability.development
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
            "websocket",
          ),
          Requirement.recommended("When applicable."),
          Stability.stable
        )

      /** The actual version of the protocol used for network communication.
        */
      val networkProtocolVersion: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkProtocolVersion,
          List(
            "1.1",
            "2",
          ),
          Requirement.recommended("When applicable."),
          Stability.stable
        )

      /** The transport protocol used for the MCP session.
        *
        * @note
        *   <p> This attribute SHOULD be set to `tcp` or `quic` if the transport protocol is HTTP. It SHOULD be set to
        *   `pipe` if the transport is stdio.
        */
      val networkTransport: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkTransport,
          List(
            "tcp",
            "quic",
            "pipe",
          ),
          Requirement.recommended,
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          errorType,
          jsonrpcProtocolVersion,
          mcpProtocolVersion,
          networkProtocolName,
          networkProtocolVersion,
          networkTransport,
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
