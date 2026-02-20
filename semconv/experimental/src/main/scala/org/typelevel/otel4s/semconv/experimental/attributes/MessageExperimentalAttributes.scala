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

  /** Deprecated, no replacement at this time.
    */
  @deprecated("Deprecated, no replacement at this time.", "")
  val MessageCompressedSize: AttributeKey[Long] =
    AttributeKey("message.compressed_size")

  /** Deprecated, no replacement at this time.
    */
  @deprecated("Deprecated, no replacement at this time.", "")
  val MessageId: AttributeKey[Long] =
    AttributeKey("message.id")

  /** Deprecated, no replacement at this time.
    */
  @deprecated("Deprecated, no replacement at this time.", "")
  val MessageType: AttributeKey[String] =
    AttributeKey("message.type")

  /** Deprecated, no replacement at this time.
    */
  @deprecated("Deprecated, no replacement at this time.", "")
  val MessageUncompressedSize: AttributeKey[Long] =
    AttributeKey("message.uncompressed_size")

  /** Values for [[MessageType]].
    */
  @deprecated("Deprecated, no replacement at this time.", "")
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
