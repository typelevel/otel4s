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
package attributes

// DO NOT EDIT, this is an Auto-generated file from buildscripts/templates/registry/otel4s/attributes/SemanticAttributes.scala.j2
object CodeAttributes {

  /** The column number in `code.file.path` best representing the operation. It SHOULD point within the code unit named
    * in `code.function.name`. This attribute MUST NOT be used on the Profile signal since the data is already captured
    * in 'message Line'. This constraint is imposed to prevent redundancy and maintain data integrity.
    */
  val CodeColumnNumber: AttributeKey[Long] =
    AttributeKey("code.column.number")

  /** The source code file name that identifies the code unit as uniquely as possible (preferably an absolute file
    * path). This attribute MUST NOT be used on the Profile signal since the data is already captured in 'message
    * Function'. This constraint is imposed to prevent redundancy and maintain data integrity.
    */
  val CodeFilePath: AttributeKey[String] =
    AttributeKey("code.file.path")

  /** The method or function fully-qualified name without arguments. The value should fit the natural representation of
    * the language runtime, which is also likely the same used within `code.stacktrace` attribute value. This attribute
    * MUST NOT be used on the Profile signal since the data is already captured in 'message Function'. This constraint
    * is imposed to prevent redundancy and maintain data integrity.
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
    * `code.function.name`. This attribute MUST NOT be used on the Profile signal since the data is already captured in
    * 'message Line'. This constraint is imposed to prevent redundancy and maintain data integrity.
    */
  val CodeLineNumber: AttributeKey[Long] =
    AttributeKey("code.line.number")

  /** A stacktrace as a string in the natural representation for the language runtime. The representation is identical
    * to <a href="/docs/exceptions/exceptions-spans.md#stacktrace-representation">`exception.stacktrace`</a>. This
    * attribute MUST NOT be used on the Profile signal since the data is already captured in 'message Location'. This
    * constraint is imposed to prevent redundancy and maintain data integrity.
    */
  val CodeStacktrace: AttributeKey[String] =
    AttributeKey("code.stacktrace")

}
