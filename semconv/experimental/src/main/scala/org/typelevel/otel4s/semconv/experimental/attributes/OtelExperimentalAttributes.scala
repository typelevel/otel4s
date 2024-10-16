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
object OtelExperimentalAttributes {

  /** Deprecated. Use the `otel.scope.name` attribute
    */
  @deprecated("Use the `otel.scope.name` attribute.", "")
  val OtelLibraryName: AttributeKey[String] =
    AttributeKey("otel.library.name")

  /** Deprecated. Use the `otel.scope.version` attribute.
    */
  @deprecated("Use the `otel.scope.version` attribute.", "")
  val OtelLibraryVersion: AttributeKey[String] =
    AttributeKey("otel.library.version")

  /** The name of the instrumentation scope - (`InstrumentationScope.Name` in OTLP).
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.OtelAttributes.OtelScopeName` instead.",
    ""
  )
  val OtelScopeName: AttributeKey[String] =
    AttributeKey("otel.scope.name")

  /** The version of the instrumentation scope - (`InstrumentationScope.Version` in OTLP).
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.OtelAttributes.OtelScopeVersion` instead.",
    ""
  )
  val OtelScopeVersion: AttributeKey[String] =
    AttributeKey("otel.scope.version")

  /** Name of the code, either "OK" or "ERROR". MUST NOT be set if the status code is UNSET.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.OtelAttributes.OtelStatusCode` instead.",
    ""
  )
  val OtelStatusCode: AttributeKey[String] =
    AttributeKey("otel.status_code")

  /** Description of the Status if it has a value, otherwise not set.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.OtelAttributes.OtelStatusDescription` instead.",
    ""
  )
  val OtelStatusDescription: AttributeKey[String] =
    AttributeKey("otel.status_description")

  /** Values for [[OtelStatusCode]].
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.OtelAttributes.OtelStatusCode` instead.",
    ""
  )
  abstract class OtelStatusCodeValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object OtelStatusCodeValue {

    /** The operation has been validated by an Application developer or Operator to have completed successfully.
      */
    case object Ok extends OtelStatusCodeValue("OK")

    /** The operation contains an error.
      */
    case object Error extends OtelStatusCodeValue("ERROR")
  }

}
