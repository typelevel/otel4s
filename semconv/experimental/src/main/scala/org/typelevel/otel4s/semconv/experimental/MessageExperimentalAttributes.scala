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
import org.typelevel.otel4s.AttributeKey._

// DO NOT EDIT, this is an Auto-generated file from buildscripts/semantic-convention/templates/SemanticAttributes.scala.j2
object MessageExperimentalAttributes {

  /**
  * Deprecated, use `rpc.message.compressed_size` instead.
  */
  @deprecated("Use `rpc.message.compressed_size` instead", "0.5.0")
  val MessageCompressedSize: AttributeKey[Long] = long("message.compressed_size")

  /**
  * Deprecated, use `rpc.message.id` instead.
  */
  @deprecated("Use `rpc.message.id` instead", "0.5.0")
  val MessageId: AttributeKey[Long] = long("message.id")

  /**
  * Deprecated, use `rpc.message.type` instead.
  */
  @deprecated("Use `rpc.message.type` instead", "0.5.0")
  val MessageType: AttributeKey[String] = string("message.type")

  /**
  * Deprecated, use `rpc.message.uncompressed_size` instead.
  */
  @deprecated("Use `rpc.message.uncompressed_size` instead", "0.5.0")
  val MessageUncompressedSize: AttributeKey[Long] = long("message.uncompressed_size")
  // Enum definitions
  
  /**
   * Values for [[MessageType]].
   */
  @deprecated("Use `rpc.message.type` instead", "0.5.0")
  abstract class MessageTypeValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object MessageTypeValue {
    /** sent. */
    case object Sent extends MessageTypeValue("SENT")
    /** received. */
    case object Received extends MessageTypeValue("RECEIVED")
  }

}