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
object CodeExperimentalAttributes {

  /** The column number in `code.filepath` best representing the operation. It
    * SHOULD point within the code unit named in `code.function`.
    */
  val CodeColumn: AttributeKey[Long] = long("code.column")

  /** The source code file name that identifies the code unit as uniquely as
    * possible (preferably an absolute file path).
    */
  val CodeFilepath: AttributeKey[String] = string("code.filepath")

  /** The method or function name, or equivalent (usually rightmost part of the
    * code unit's name).
    */
  val CodeFunction: AttributeKey[String] = string("code.function")

  /** The line number in `code.filepath` best representing the operation. It
    * SHOULD point within the code unit named in `code.function`.
    */
  val CodeLineno: AttributeKey[Long] = long("code.lineno")

  /** The &quot;namespace&quot; within which `code.function` is defined. Usually
    * the qualified class or module name, such that `code.namespace` + some
    * separator + `code.function` form a unique identifier for the code unit.
    */
  val CodeNamespace: AttributeKey[String] = string("code.namespace")

  /** A stacktrace as a string in the natural representation for the language
    * runtime. The representation is to be determined and documented by each
    * language SIG.
    */
  val CodeStacktrace: AttributeKey[String] = string("code.stacktrace")

}
