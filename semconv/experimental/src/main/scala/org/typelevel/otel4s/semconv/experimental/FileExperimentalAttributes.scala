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
object FileExperimentalAttributes {

  /** Directory where the file is located. It should include the drive letter,
    * when appropriate.
    */
  val FileDirectory: AttributeKey[String] = string("file.directory")

  /** File extension, excluding the leading dot.
    *
    * @note
    *   - When the file name has multiple extensions (example.tar.gz), only the
    *     last one should be captured (&quot;gz&quot;, not &quot;tar.gz&quot;).
    */
  val FileExtension: AttributeKey[String] = string("file.extension")

  /** Name of the file including the extension, without the directory.
    */
  val FileName: AttributeKey[String] = string("file.name")

  /** Full path to the file, including the file name. It should include the
    * drive letter, when appropriate.
    */
  val FilePath: AttributeKey[String] = string("file.path")

  /** File size in bytes.
    */
  val FileSize: AttributeKey[Long] = long("file.size")

}
