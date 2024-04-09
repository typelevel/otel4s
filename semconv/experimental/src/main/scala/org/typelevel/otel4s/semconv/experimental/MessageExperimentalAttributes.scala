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

  /** Compressed size of the message in bytes.
    */
  val MessageCompressedSize: AttributeKey[Long] = long(
    "message.compressed_size"
  )

  /** MUST be calculated as two different counters starting from `1` one for
    * sent messages and one for received message.
    *
    * @note
    *   - This way we guarantee that the values will be consistent between
    *     different implementations.
    */
  val MessageId: AttributeKey[Long] = long("message.id")

  /** Whether this is a received or sent message.
    */
  val MessageType: AttributeKey[String] = string("message.type")

  /** Uncompressed size of the message in bytes.
    */
  val MessageUncompressedSize: AttributeKey[Long] = long(
    "message.uncompressed_size"
  )
  // Enum definitions

  /** Values for [[MessageType]].
    */
  abstract class MessageTypeValue(val value: String)
  object MessageTypeValue {

    /** sent. */
    case object Sent extends MessageTypeValue("SENT")

    /** received. */
    case object Received extends MessageTypeValue("RECEIVED")
  }

}
