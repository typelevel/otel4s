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
  @deprecated("Replaced by `code.file.path`", "")
  val CodeFilepath: AttributeKey[String] =
    AttributeKey("code.filepath")

  /** Deprecated, use `code.function.name` instead
    */
  @deprecated("Replaced by `code.function.name`", "")
  val CodeFunction: AttributeKey[String] =
    AttributeKey("code.function")

  /** The method or function fully-qualified name without arguments. The value should fit the natural representation of
    * the language runtime, which is also likely the same used within `code.stacktrace` attribute value.
    *
    * @note
    *   <p> Values and format depends on each language runtime, thus it is impossible to provide an exhaustive list of
    *   examples. The values are usually the same (or prefixes of) the ones found in native stack trace representation
    *   stored in `code.stacktrace` without information on arguments. <p> Examples: <ul> <li>Java method:
    *   `com.example.MyHttpService.serveRequest` <li>Java anonymous class method: `com.mycompany.Main$$1.myMethod`
    *   <li>Java lambda method: `com.mycompany.Main$$$$Lambda/0x0000748ae4149c00.myMethod` <li>PHP function:
    *   `GuzzleHttp\Client::transfer` <li>Go function: `github.com/my/repo/pkg.foo.func5` <li>Elixir:
    *   `OpenTelemetry.Ctx.new` <li>Erlang: `opentelemetry_ctx:new` <li>Rust: `playground::my_module::my_cool_func`
    *   <li>C function: `fopen` </ul>
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

  /** Deprecated, namespace is now included into `code.function.name`
    */
  @deprecated("Value should be included in `code.function.name` which is expected to be a fully-qualified name.", "")
  val CodeNamespace: AttributeKey[String] =
    AttributeKey("code.namespace")

  /** A stacktrace as a string in the natural representation for the language runtime. The representation is identical
    * to <a href="/docs/exceptions/exceptions-spans.md#stacktrace-representation">`exception.stacktrace`</a>.
    */
  val CodeStacktrace: AttributeKey[String] =
    AttributeKey("code.stacktrace")

}
