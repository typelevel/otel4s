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
object ExceptionAttributes {

  /** The exception message.
    */
  val ExceptionMessage: AttributeKey[String] =
    AttributeKey("exception.message")

  /** A stacktrace as a string in the natural representation for the language runtime. The representation is to be
    * determined and documented by each language SIG.
    */
  val ExceptionStacktrace: AttributeKey[String] =
    AttributeKey("exception.stacktrace")

  /** The type of the exception (its fully-qualified class name, if applicable). The dynamic type of the exception
    * should be preferred over the static type in languages that support it.
    */
  val ExceptionType: AttributeKey[String] =
    AttributeKey("exception.type")

}
