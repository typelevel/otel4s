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
object CodeExperimentalAttributes {

  /** Deprecated, use `code.column.number`
    */
  @deprecated("Replaced by `code.column.number`", "")
  val CodeColumn: AttributeKey[Long] =
    AttributeKey("code.column")

  /** The column number in `code.file.path` best representing the operation. It SHOULD point within the code unit named
    * in `code.function.name`.
    */
  val CodeColumnNumber: AttributeKey[Long] =
    AttributeKey("code.column.number")

  /** The source code file name that identifies the code unit as uniquely as possible (preferably an absolute file
    * path).
    */
  val CodeFilePath: AttributeKey[String] =
    AttributeKey("code.file.path")

  /** Deprecated, use `code.file.path` instead
    */
  val CodeFilepath: AttributeKey[String] =
    AttributeKey("code.filepath")

  /** Deprecated, use `code.function.name` instead
    */
  @deprecated("Replaced by `code.function.name`", "")
  val CodeFunction: AttributeKey[String] =
    AttributeKey("code.function")

  /** The method or function name, or equivalent (usually rightmost part of the code unit's name).
    */
  val CodeFunctionName: AttributeKey[String] =
    AttributeKey("code.function.name")

  /** The line number in `code.file.path` best representing the operation. It SHOULD point within the code unit named in
    * `code.function.name`.
    */
  val CodeLineNumber: AttributeKey[Long] =
    AttributeKey("code.line.number")

  /** Deprecated, use `code.line.number` instead
    */
  @deprecated("Replaced by `code.line.number`", "")
  val CodeLineno: AttributeKey[Long] =
    AttributeKey("code.lineno")

  /** The "namespace" within which `code.function.name` is defined. Usually the qualified class or module name, such
    * that `code.namespace` + some separator + `code.function.name` form a unique identifier for the code unit.
    */
  val CodeNamespace: AttributeKey[String] =
    AttributeKey("code.namespace")

  /** A stacktrace as a string in the natural representation for the language runtime. The representation is to be
    * determined and documented by each language SIG.
    */
  val CodeStacktrace: AttributeKey[String] =
    AttributeKey("code.stacktrace")

}
