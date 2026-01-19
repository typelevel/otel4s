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
object TelemetryExperimentalAttributes {

  /** The name of the auto instrumentation agent or distribution, if used.
    *
    * @note
    *   <p> Official auto instrumentation agents and distributions SHOULD set the `telemetry.distro.name` attribute to a
    *   string starting with `opentelemetry-`, e.g. `opentelemetry-java-instrumentation`.
    */
  val TelemetryDistroName: AttributeKey[String] =
    AttributeKey("telemetry.distro.name")

  /** The version string of the auto instrumentation agent or distribution, if used.
    */
  val TelemetryDistroVersion: AttributeKey[String] =
    AttributeKey("telemetry.distro.version")

  /** The language of the telemetry SDK.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.TelemetryAttributes.TelemetrySdkLanguage` instead.",
    ""
  )
  val TelemetrySdkLanguage: AttributeKey[String] =
    AttributeKey("telemetry.sdk.language")

  /** The name of the telemetry SDK as defined above.
    *
    * @note
    *   <p> The OpenTelemetry SDK MUST set the `telemetry.sdk.name` attribute to `opentelemetry`. If another SDK, like a
    *   fork or a vendor-provided implementation, is used, this SDK MUST set the `telemetry.sdk.name` attribute to the
    *   fully-qualified class or module name of this SDK's main entry point or another suitable identifier depending on
    *   the language. The identifier `opentelemetry` is reserved and MUST NOT be used in this case. All custom
    *   identifiers SHOULD be stable across different versions of an implementation.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.TelemetryAttributes.TelemetrySdkName` instead.",
    ""
  )
  val TelemetrySdkName: AttributeKey[String] =
    AttributeKey("telemetry.sdk.name")

  /** The version string of the telemetry SDK.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.TelemetryAttributes.TelemetrySdkVersion` instead.",
    ""
  )
  val TelemetrySdkVersion: AttributeKey[String] =
    AttributeKey("telemetry.sdk.version")

  /** Values for [[TelemetrySdkLanguage]].
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.TelemetryAttributes.TelemetrySdkLanguage` instead.",
    ""
  )
  abstract class TelemetrySdkLanguageValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object TelemetrySdkLanguageValue {
    implicit val attributeFromTelemetrySdkLanguageValue: Attribute.From[TelemetrySdkLanguageValue, String] = _.value

    /** cpp.
      */
    case object Cpp extends TelemetrySdkLanguageValue("cpp")

    /** dotnet.
      */
    case object Dotnet extends TelemetrySdkLanguageValue("dotnet")

    /** erlang.
      */
    case object Erlang extends TelemetrySdkLanguageValue("erlang")

    /** go.
      */
    case object Go extends TelemetrySdkLanguageValue("go")

    /** java.
      */
    case object Java extends TelemetrySdkLanguageValue("java")

    /** nodejs.
      */
    case object Nodejs extends TelemetrySdkLanguageValue("nodejs")

    /** php.
      */
    case object Php extends TelemetrySdkLanguageValue("php")

    /** python.
      */
    case object Python extends TelemetrySdkLanguageValue("python")

    /** ruby.
      */
    case object Ruby extends TelemetrySdkLanguageValue("ruby")

    /** rust.
      */
    case object Rust extends TelemetrySdkLanguageValue("rust")

    /** swift.
      */
    case object Swift extends TelemetrySdkLanguageValue("swift")

    /** webjs.
      */
    case object Webjs extends TelemetrySdkLanguageValue("webjs")

    /** scala. */
    case object Scala extends TelemetrySdkLanguageValue("scala")
  }

}
