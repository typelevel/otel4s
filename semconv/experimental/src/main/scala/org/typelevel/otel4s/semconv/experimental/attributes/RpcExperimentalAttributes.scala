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

package org.typelevel.otel4s.semconv.experimental.attributes

import org.typelevel.otel4s.AttributeKey

// DO NOT EDIT, this is an Auto-generated file from buildscripts/templates/registry/otel4s/attributes/experimental/SemanticAttributes.scala.j2
object RpcExperimentalAttributes {

  /** The <a href="https://connect.build/docs/protocol/#error-codes">error codes</a> of the Connect request. Error codes
    * are always string values.
    */
  val RpcConnectRpcErrorCode: AttributeKey[String] =
    AttributeKey("rpc.connect_rpc.error_code")

  /** Connect request metadata, `<key>` being the normalized Connect Metadata key (lowercase), the value being the
    * metadata values. <p>
    * @note
    *   <p> Instrumentations SHOULD require an explicit configuration of which metadata values are to be captured.
    *   Including all request metadata values can be a security risk - explicit configuration helps avoid leaking
    *   sensitive information.
    */
  val RpcConnectRpcRequestMetadata: AttributeKey[Seq[String]] =
    AttributeKey("rpc.connect_rpc.request.metadata")

  /** Connect response metadata, `<key>` being the normalized Connect Metadata key (lowercase), the value being the
    * metadata values. <p>
    * @note
    *   <p> Instrumentations SHOULD require an explicit configuration of which metadata values are to be captured.
    *   Including all response metadata values can be a security risk - explicit configuration helps avoid leaking
    *   sensitive information.
    */
  val RpcConnectRpcResponseMetadata: AttributeKey[Seq[String]] =
    AttributeKey("rpc.connect_rpc.response.metadata")

  /** gRPC request metadata, `<key>` being the normalized gRPC Metadata key (lowercase), the value being the metadata
    * values. <p>
    * @note
    *   <p> Instrumentations SHOULD require an explicit configuration of which metadata values are to be captured.
    *   Including all request metadata values can be a security risk - explicit configuration helps avoid leaking
    *   sensitive information.
    */
  val RpcGrpcRequestMetadata: AttributeKey[Seq[String]] =
    AttributeKey("rpc.grpc.request.metadata")

  /** gRPC response metadata, `<key>` being the normalized gRPC Metadata key (lowercase), the value being the metadata
    * values. <p>
    * @note
    *   <p> Instrumentations SHOULD require an explicit configuration of which metadata values are to be captured.
    *   Including all response metadata values can be a security risk - explicit configuration helps avoid leaking
    *   sensitive information.
    */
  val RpcGrpcResponseMetadata: AttributeKey[Seq[String]] =
    AttributeKey("rpc.grpc.response.metadata")

  /** The <a href="https://github.com/grpc/grpc/blob/v1.33.2/doc/statuscodes.md">numeric status code</a> of the gRPC
    * request.
    */
  val RpcGrpcStatusCode: AttributeKey[Long] =
    AttributeKey("rpc.grpc.status_code")

  /** `error.code` property of response if it is an error response.
    */
  val RpcJsonrpcErrorCode: AttributeKey[Long] =
    AttributeKey("rpc.jsonrpc.error_code")

  /** `error.message` property of response if it is an error response.
    */
  val RpcJsonrpcErrorMessage: AttributeKey[String] =
    AttributeKey("rpc.jsonrpc.error_message")

  /** `id` property of request or response. Since protocol allows id to be int, string, `null` or missing (for
    * notifications), value is expected to be cast to string for simplicity. Use empty string in case of `null` value.
    * Omit entirely if this is a notification.
    */
  val RpcJsonrpcRequestId: AttributeKey[String] =
    AttributeKey("rpc.jsonrpc.request_id")

  /** Protocol version as in `jsonrpc` property of request/response. Since JSON-RPC 1.0 doesn't specify this, the value
    * can be omitted.
    */
  val RpcJsonrpcVersion: AttributeKey[String] =
    AttributeKey("rpc.jsonrpc.version")

  /** Compressed size of the message in bytes.
    */
  val RpcMessageCompressedSize: AttributeKey[Long] =
    AttributeKey("rpc.message.compressed_size")

  /** MUST be calculated as two different counters starting from `1` one for sent messages and one for received message.
    * <p>
    * @note
    *   <p> This way we guarantee that the values will be consistent between different implementations.
    */
  val RpcMessageId: AttributeKey[Long] =
    AttributeKey("rpc.message.id")

  /** Whether this is a received or sent message.
    */
  val RpcMessageType: AttributeKey[String] =
    AttributeKey("rpc.message.type")

  /** Uncompressed size of the message in bytes.
    */
  val RpcMessageUncompressedSize: AttributeKey[Long] =
    AttributeKey("rpc.message.uncompressed_size")

  /** The name of the (logical) method being called, must be equal to the $$method part in the span name. <p>
    * @note
    *   <p> This is the logical name of the method from the RPC interface perspective, which can be different from the
    *   name of any implementing method/function. The `code.function` attribute may be used to store the latter (e.g.,
    *   method actually executing the call on the server side, RPC client stub method on the client side).
    */
  val RpcMethod: AttributeKey[String] =
    AttributeKey("rpc.method")

  /** The full (logical) name of the service being called, including its package name, if applicable. <p>
    * @note
    *   <p> This is the logical name of the service from the RPC interface perspective, which can be different from the
    *   name of any implementing class. The `code.namespace` attribute may be used to store the latter (despite the
    *   attribute name, it may include a class name; e.g., class with method actually executing the call on the server
    *   side, RPC client stub class on the client side).
    */
  val RpcService: AttributeKey[String] =
    AttributeKey("rpc.service")

  /** A string identifying the remoting system. See below for a list of well-known identifiers.
    */
  val RpcSystem: AttributeKey[String] =
    AttributeKey("rpc.system")

  /** Values for [[RpcConnectRpcErrorCode]].
    */
  abstract class RpcConnectRpcErrorCodeValue(val value: String)
  object RpcConnectRpcErrorCodeValue {

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
  abstract class RpcGrpcStatusCodeValue(val value: Long)
  object RpcGrpcStatusCodeValue {

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
  abstract class RpcMessageTypeValue(val value: String)
  object RpcMessageTypeValue {

    /** sent.
      */
    case object Sent extends RpcMessageTypeValue("SENT")

    /** received.
      */
    case object Received extends RpcMessageTypeValue("RECEIVED")
  }

  /** Values for [[RpcSystem]].
    */
  abstract class RpcSystemValue(val value: String)
  object RpcSystemValue {

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
  }

}
