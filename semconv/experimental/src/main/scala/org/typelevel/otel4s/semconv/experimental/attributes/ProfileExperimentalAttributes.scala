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
object ProfileExperimentalAttributes {

  /** Describes the interpreter or compiler of a single frame.
    */
  val ProfileFrameType: AttributeKey[String] =
    AttributeKey("profile.frame.type")

  /** Values for [[ProfileFrameType]].
    */
  abstract class ProfileFrameTypeValue(val value: String)
  object ProfileFrameTypeValue {

    /** <a href="https://wikipedia.org/wiki/.NET">.NET</a>
      */
    case object Dotnet extends ProfileFrameTypeValue("dotnet")

    /** <a href="https://wikipedia.org/wiki/Java_virtual_machine">JVM</a>
      */
    case object Jvm extends ProfileFrameTypeValue("jvm")

    /** <a href="https://wikipedia.org/wiki/Kernel_(operating_system)">Kernel</a>
      */
    case object Kernel extends ProfileFrameTypeValue("kernel")

    /** Can be one of but not limited to <a href="https://wikipedia.org/wiki/C_(programming_language)">C</a>, <a
      * href="https://wikipedia.org/wiki/C%2B%2B">C++</a>, <a
      * href="https://wikipedia.org/wiki/Go_(programming_language)">Go</a> or <a
      * href="https://wikipedia.org/wiki/Rust_(programming_language)">Rust</a>. If possible, a more precise value MUST
      * be used.
      */
    case object Native extends ProfileFrameTypeValue("native")

    /** <a href="https://wikipedia.org/wiki/Perl">Perl</a>
      */
    case object Perl extends ProfileFrameTypeValue("perl")

    /** <a href="https://wikipedia.org/wiki/PHP">PHP</a>
      */
    case object Php extends ProfileFrameTypeValue("php")

    /** <a href="https://wikipedia.org/wiki/Python_(programming_language)">Python</a>
      */
    case object Cpython extends ProfileFrameTypeValue("cpython")

    /** <a href="https://wikipedia.org/wiki/Ruby_(programming_language)">Ruby</a>
      */
    case object Ruby extends ProfileFrameTypeValue("ruby")

    /** <a href="https://wikipedia.org/wiki/V8_(JavaScript_engine)">V8JS</a>
      */
    case object V8js extends ProfileFrameTypeValue("v8js")

    /** <a href="https://en.wikipedia.org/wiki/BEAM_(Erlang_virtual_machine)">Erlang</a>
      */
    case object Beam extends ProfileFrameTypeValue("beam")

    /** <a href="https://wikipedia.org/wiki/Go_(programming_language)">Go</a>,
      */
    case object Go extends ProfileFrameTypeValue("go")

    /** <a href="https://wikipedia.org/wiki/Rust_(programming_language)">Rust</a>
      */
    case object Rust extends ProfileFrameTypeValue("rust")
  }

}
