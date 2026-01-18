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
object MessageExperimentalAttributes {

  /** Deprecated, use `rpc.message.compressed_size` instead.
    */
  @deprecated("Replaced by `rpc.message.compressed_size`.", "")
  val MessageCompressedSize: AttributeKey[Long] =
    AttributeKey("message.compressed_size")

  /** Deprecated, use `rpc.message.id` instead.
    */
  @deprecated("Replaced by `rpc.message.id`.", "")
  val MessageId: AttributeKey[Long] =
    AttributeKey("message.id")

  /** Deprecated, use `rpc.message.type` instead.
    */
  @deprecated("Replaced by `rpc.message.type`.", "")
  val MessageType: AttributeKey[String] =
    AttributeKey("message.type")

  /** Deprecated, use `rpc.message.uncompressed_size` instead.
    */
  @deprecated("Replaced by `rpc.message.uncompressed_size`.", "")
  val MessageUncompressedSize: AttributeKey[Long] =
    AttributeKey("message.uncompressed_size")

  /** Values for [[MessageType]].
    */
  @deprecated("Replaced by `rpc.message.type`.", "")
  abstract class MessageTypeValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object MessageTypeValue {
    implicit val attributeFromMessageTypeValue: Attribute.From[MessageTypeValue, String] = _.value

    /** sent.
      */
    case object Sent extends MessageTypeValue("SENT")

    /** received.
      */
    case object Received extends MessageTypeValue("RECEIVED")
  }

}
