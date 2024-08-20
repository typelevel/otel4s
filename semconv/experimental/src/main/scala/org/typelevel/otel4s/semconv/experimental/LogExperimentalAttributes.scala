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
object LogExperimentalAttributes {

  /**
  * The basename of the file.
  */
  val LogFileName: AttributeKey[String] = string("log.file.name")

  /**
  * The basename of the file, with symlinks resolved.
  */
  val LogFileNameResolved: AttributeKey[String] = string("log.file.name_resolved")

  /**
  * The full path to the file.
  */
  val LogFilePath: AttributeKey[String] = string("log.file.path")

  /**
  * The full path to the file, with symlinks resolved.
  */
  val LogFilePathResolved: AttributeKey[String] = string("log.file.path_resolved")

  /**
  * The stream associated with the log. See below for a list of well-known values.
  */
  val LogIostream: AttributeKey[String] = string("log.iostream")

  /**
  * The complete orignal Log Record.
  *
  * @note 
  *  - This value MAY be added when processing a Log Record which was originally transmitted as a string or equivalent data type AND the Body field of the Log Record does not contain the same value. (e.g. a syslog or a log record read from a file.)
  */
  val LogRecordOriginal: AttributeKey[String] = string("log.record.original")

  /**
  * A unique identifier for the Log Record.
  *
  * @note 
  *  - If an id is provided, other log records with the same id will be considered duplicates and can be removed safely. This means, that two distinguishable log records MUST have different values.
The id MAY be an <a href="https://github.com/ulid/spec">Universally Unique Lexicographically Sortable Identifier (ULID)</a>, but other identifiers (e.g. UUID) may be used as needed.
  */
  val LogRecordUid: AttributeKey[String] = string("log.record.uid")
  // Enum definitions
  
  /**
   * Values for [[LogIostream]].
   */
  abstract class LogIostreamValue(val value: String)
  object LogIostreamValue {
    /** Logs from stdout stream. */
    case object Stdout extends LogIostreamValue("stdout")
    /** Events from stderr stream. */
    case object Stderr extends LogIostreamValue("stderr")
  }

}