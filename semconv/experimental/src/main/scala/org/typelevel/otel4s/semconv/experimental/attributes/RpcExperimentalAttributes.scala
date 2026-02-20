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
object RpcExperimentalAttributes {

  /** Deprecated, use `rpc.response.status_code` attribute instead.
    */
  @deprecated("Replaced by `rpc.response.status_code`.", "")
  val RpcConnectRpcErrorCode: AttributeKey[String] =
    AttributeKey("rpc.connect_rpc.error_code")

  /** Deprecated, use `rpc.request.metadata` instead.
    */
  @deprecated("Replaced by `rpc.request.metadata`.", "")
  val RpcConnectRpcRequestMetadata: AttributeKey[Seq[String]] =
    AttributeKey("rpc.connect_rpc.request.metadata")

  /** Deprecated, use `rpc.response.metadata` instead.
    */
  @deprecated("Replaced by `rpc.response.metadata`.", "")
  val RpcConnectRpcResponseMetadata: AttributeKey[Seq[String]] =
    AttributeKey("rpc.connect_rpc.response.metadata")

  /** Deprecated, use `rpc.request.metadata` instead.
    */
  @deprecated("Replaced by `rpc.request.metadata`.", "")
  val RpcGrpcRequestMetadata: AttributeKey[Seq[String]] =
    AttributeKey("rpc.grpc.request.metadata")

  /** Deprecated, use `rpc.response.metadata` instead.
    */
  @deprecated("Replaced by `rpc.response.metadata`.", "")
  val RpcGrpcResponseMetadata: AttributeKey[Seq[String]] =
    AttributeKey("rpc.grpc.response.metadata")

  /** Deprecated, use string representation on the `rpc.response.status_code` attribute instead.
    */
  @deprecated("Use string representation of the gRPC status code on the `rpc.response.status_code` attribute.", "")
  val RpcGrpcStatusCode: AttributeKey[Long] =
    AttributeKey("rpc.grpc.status_code")

  /** Deprecated, use string representation on the `rpc.response.status_code` attribute instead.
    */
  @deprecated("Use string representation of the error code on the `rpc.response.status_code` attribute.", "")
  val RpcJsonrpcErrorCode: AttributeKey[Long] =
    AttributeKey("rpc.jsonrpc.error_code")

  /** Deprecated, use the span status description when reporting JSON-RPC spans.
    */
  @deprecated("Use the span status description when reporting JSON-RPC spans.", "")
  val RpcJsonrpcErrorMessage: AttributeKey[String] =
    AttributeKey("rpc.jsonrpc.error_message")

  /** Deprecated, use `jsonrpc.request.id` instead.
    */
  @deprecated("Replaced by `jsonrpc.request.id`.", "")
  val RpcJsonrpcRequestId: AttributeKey[String] =
    AttributeKey("rpc.jsonrpc.request_id")

  /** Deprecated, use `jsonrpc.protocol.version` instead.
    */
  @deprecated("Replaced by `jsonrpc.protocol.version`.", "")
  val RpcJsonrpcVersion: AttributeKey[String] =
    AttributeKey("rpc.jsonrpc.version")

  /** Compressed size of the message in bytes.
    */
  @deprecated("Deprecated, no replacement at this time.", "")
  val RpcMessageCompressedSize: AttributeKey[Long] =
    AttributeKey("rpc.message.compressed_size")

  /** MUST be calculated as two different counters starting from `1` one for sent messages and one for received message.
    *
    * @note
    *   <p> This way we guarantee that the values will be consistent between different implementations.
    */
  @deprecated("Deprecated, no replacement at this time.", "")
  val RpcMessageId: AttributeKey[Long] =
    AttributeKey("rpc.message.id")

  /** Whether this is a received or sent message.
    */
  @deprecated("Deprecated, no replacement at this time.", "")
  val RpcMessageType: AttributeKey[String] =
    AttributeKey("rpc.message.type")

  /** Uncompressed size of the message in bytes.
    */
  @deprecated("Deprecated, no replacement at this time.", "")
  val RpcMessageUncompressedSize: AttributeKey[Long] =
    AttributeKey("rpc.message.uncompressed_size")

  /** The fully-qualified logical name of the method from the RPC interface perspective.
    *
    * @note
    *   <p> The method name MAY have unbounded cardinality in edge or error cases. <p> Some RPC frameworks or libraries
    *   provide a fixed set of recognized methods for client stubs and server implementations. Instrumentations for such
    *   frameworks MUST set this attribute to the original method name only when the method is recognized by the
    *   framework or library. <p> When the method is not recognized, for example, when the server receives a request for
    *   a method that is not predefined on the server, or when instrumentation is not able to reliably detect if the
    *   method is predefined, the attribute MUST be set to `_OTHER`. In such cases, tracing instrumentations MUST also
    *   set `rpc.method_original` attribute to the original method value. <p> If the RPC instrumentation could end up
    *   converting valid RPC methods to `_OTHER`, then it SHOULD provide a way to configure the list of recognized RPC
    *   methods. <p> The `rpc.method` can be different from the name of any implementing method/function. The
    *   `code.function.name` attribute may be used to record the fully-qualified method actually executing the call on
    *   the server side, or the RPC client stub method on the client side.
    */
  val RpcMethod: AttributeKey[String] =
    AttributeKey("rpc.method")

  /** The original name of the method used by the client.
    */
  val RpcMethodOriginal: AttributeKey[String] =
    AttributeKey("rpc.method_original")

  /** RPC request metadata, `<key>` being the normalized RPC metadata key (lowercase), the value being the metadata
    * values.
    *
    * @note
    *   <p> Instrumentations SHOULD require an explicit configuration of which metadata values are to be captured.
    *   Including all request metadata values can be a security risk - explicit configuration helps avoid leaking
    *   sensitive information. <p> For example, a property `my-custom-key` with value `["1.2.3.4", "1.2.3.5"]` SHOULD be
    *   recorded as `rpc.request.metadata.my-custom-key` attribute with value `["1.2.3.4", "1.2.3.5"]`
    */
  val RpcRequestMetadata: AttributeKey[Seq[String]] =
    AttributeKey("rpc.request.metadata")

  /** RPC response metadata, `<key>` being the normalized RPC metadata key (lowercase), the value being the metadata
    * values.
    *
    * @note
    *   <p> Instrumentations SHOULD require an explicit configuration of which metadata values are to be captured.
    *   Including all response metadata values can be a security risk - explicit configuration helps avoid leaking
    *   sensitive information. <p> For example, a property `my-custom-key` with value `["attribute_value"]` SHOULD be
    *   recorded as the `rpc.response.metadata.my-custom-key` attribute with value `["attribute_value"]`
    */
  val RpcResponseMetadata: AttributeKey[Seq[String]] =
    AttributeKey("rpc.response.metadata")

  /** Status code of the RPC returned by the RPC server or generated by the client
    *
    * @note
    *   <p> Usually it represents an error code, but may also represent partial success, warning, or differentiate
    *   between various types of successful outcomes. Semantic conventions for individual RPC frameworks SHOULD document
    *   what `rpc.response.status_code` means in the context of that system and which values are considered to represent
    *   errors.
    */
  val RpcResponseStatusCode: AttributeKey[String] =
    AttributeKey("rpc.response.status_code")

  /** Deprecated, use fully-qualified `rpc.method` instead.
    */
  @deprecated("Value should be included in `rpc.method` which is expected to be a fully-qualified name.", "")
  val RpcService: AttributeKey[String] =
    AttributeKey("rpc.service")

  /** Deprecated, use `rpc.system.name` attribute instead.
    */
  @deprecated("Replaced by `rpc.system.name`.", "")
  val RpcSystem: AttributeKey[String] =
    AttributeKey("rpc.system")

  /** The Remote Procedure Call (RPC) system.
    *
    * @note
    *   <p> The client and server RPC systems may differ for the same RPC interaction. For example, a client may use
    *   Apache Dubbo or Connect RPC to communicate with a server that uses gRPC since both protocols provide
    *   compatibility with gRPC.
    */
  val RpcSystemName: AttributeKey[String] =
    AttributeKey("rpc.system.name")

  /** Values for [[RpcConnectRpcErrorCode]].
    */
  @deprecated("Replaced by `rpc.response.status_code`.", "")
  abstract class RpcConnectRpcErrorCodeValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object RpcConnectRpcErrorCodeValue {
    implicit val attributeFromRpcConnectRpcErrorCodeValue: Attribute.From[RpcConnectRpcErrorCodeValue, String] = _.value

    /** cancelled.
      */
    case object Cancelled extends RpcConnectRpcErrorCodeValue("cancelled")

    /** unknown.
      */
    case object Unknown extends RpcConnectRpcErrorCodeValue("unknown")

    /** invalid_argument.
      */
    case object InvalidArgument extends RpcConnectRpcErrorCodeValue("invalid_argument")

    /** deadline_exceeded.
      */
    case object DeadlineExceeded extends RpcConnectRpcErrorCodeValue("deadline_exceeded")

    /** not_found.
      */
    case object NotFound extends RpcConnectRpcErrorCodeValue("not_found")

    /** already_exists.
      */
    case object AlreadyExists extends RpcConnectRpcErrorCodeValue("already_exists")

    /** permission_denied.
      */
    case object PermissionDenied extends RpcConnectRpcErrorCodeValue("permission_denied")

    /** resource_exhausted.
      */
    case object ResourceExhausted extends RpcConnectRpcErrorCodeValue("resource_exhausted")

    /** failed_precondition.
      */
    case object FailedPrecondition extends RpcConnectRpcErrorCodeValue("failed_precondition")

    /** aborted.
      */
    case object Aborted extends RpcConnectRpcErrorCodeValue("aborted")

    /** out_of_range.
      */
    case object OutOfRange extends RpcConnectRpcErrorCodeValue("out_of_range")

    /** unimplemented.
      */
    case object Unimplemented extends RpcConnectRpcErrorCodeValue("unimplemented")

    /** internal.
      */
    case object Internal extends RpcConnectRpcErrorCodeValue("internal")

    /** unavailable.
      */
    case object Unavailable extends RpcConnectRpcErrorCodeValue("unavailable")

    /** data_loss.
      */
    case object DataLoss extends RpcConnectRpcErrorCodeValue("data_loss")

    /** unauthenticated.
      */
    case object Unauthenticated extends RpcConnectRpcErrorCodeValue("unauthenticated")
  }

  /** Values for [[RpcGrpcStatusCode]].
    */
  @deprecated("Use string representation of the gRPC status code on the `rpc.response.status_code` attribute.", "")
  abstract class RpcGrpcStatusCodeValue(val value: Long)
  @annotation.nowarn("cat=deprecation")
  object RpcGrpcStatusCodeValue {
    implicit val attributeFromRpcGrpcStatusCodeValue: Attribute.From[RpcGrpcStatusCodeValue, Long] = _.value

    /** OK
      */
    case object Ok extends RpcGrpcStatusCodeValue(0)

    /** CANCELLED
      */
    case object Cancelled extends RpcGrpcStatusCodeValue(1)

    /** UNKNOWN
      */
    case object Unknown extends RpcGrpcStatusCodeValue(2)

    /** INVALID_ARGUMENT
      */
    case object InvalidArgument extends RpcGrpcStatusCodeValue(3)

    /** DEADLINE_EXCEEDED
      */
    case object DeadlineExceeded extends RpcGrpcStatusCodeValue(4)

    /** NOT_FOUND
      */
    case object NotFound extends RpcGrpcStatusCodeValue(5)

    /** ALREADY_EXISTS
      */
    case object AlreadyExists extends RpcGrpcStatusCodeValue(6)

    /** PERMISSION_DENIED
      */
    case object PermissionDenied extends RpcGrpcStatusCodeValue(7)

    /** RESOURCE_EXHAUSTED
      */
    case object ResourceExhausted extends RpcGrpcStatusCodeValue(8)

    /** FAILED_PRECONDITION
      */
    case object FailedPrecondition extends RpcGrpcStatusCodeValue(9)

    /** ABORTED
      */
    case object Aborted extends RpcGrpcStatusCodeValue(10)

    /** OUT_OF_RANGE
      */
    case object OutOfRange extends RpcGrpcStatusCodeValue(11)

    /** UNIMPLEMENTED
      */
    case object Unimplemented extends RpcGrpcStatusCodeValue(12)

    /** INTERNAL
      */
    case object Internal extends RpcGrpcStatusCodeValue(13)

    /** UNAVAILABLE
      */
    case object Unavailable extends RpcGrpcStatusCodeValue(14)

    /** DATA_LOSS
      */
    case object DataLoss extends RpcGrpcStatusCodeValue(15)

    /** UNAUTHENTICATED
      */
    case object Unauthenticated extends RpcGrpcStatusCodeValue(16)
  }

  /** Values for [[RpcMessageType]].
    */
  @deprecated("Deprecated, no replacement at this time.", "")
  abstract class RpcMessageTypeValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object RpcMessageTypeValue {
    implicit val attributeFromRpcMessageTypeValue: Attribute.From[RpcMessageTypeValue, String] = _.value

    /** sent.
      */
    case object Sent extends RpcMessageTypeValue("SENT")

    /** received.
      */
    case object Received extends RpcMessageTypeValue("RECEIVED")
  }

  /** Values for [[RpcSystem]].
    */
  @deprecated("Replaced by `rpc.system.name`.", "")
  abstract class RpcSystemValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object RpcSystemValue {
    implicit val attributeFromRpcSystemValue: Attribute.From[RpcSystemValue, String] = _.value

    /** gRPC
      */
    case object Grpc extends RpcSystemValue("grpc")

    /** Java RMI
      */
    case object JavaRmi extends RpcSystemValue("java_rmi")

    /** .NET WCF
      */
    case object DotnetWcf extends RpcSystemValue("dotnet_wcf")

    /** Apache Dubbo
      */
    case object ApacheDubbo extends RpcSystemValue("apache_dubbo")

    /** Connect RPC
      */
    case object ConnectRpc extends RpcSystemValue("connect_rpc")

    /** <a href="https://datatracker.ietf.org/doc/html/rfc5531">ONC RPC (Sun RPC)</a>
      */
    case object OncRpc extends RpcSystemValue("onc_rpc")

    /** JSON-RPC
      */
    case object Jsonrpc extends RpcSystemValue("jsonrpc")
  }

  /** Values for [[RpcSystemName]].
    */
  abstract class RpcSystemNameValue(val value: String)
  object RpcSystemNameValue {
    implicit val attributeFromRpcSystemNameValue: Attribute.From[RpcSystemNameValue, String] = _.value

    /** <a href="https://grpc.io/">gRPC</a>
      */
    case object Grpc extends RpcSystemNameValue("grpc")

    /** <a href="https://dubbo.apache.org/">Apache Dubbo</a>
      */
    case object Dubbo extends RpcSystemNameValue("dubbo")

    /** <a href="https://connectrpc.com/">Connect RPC</a>
      */
    case object Connectrpc extends RpcSystemNameValue("connectrpc")

    /** <a href="https://www.jsonrpc.org/">JSON-RPC</a>
      */
    case object Jsonrpc extends RpcSystemNameValue("jsonrpc")
  }

}
