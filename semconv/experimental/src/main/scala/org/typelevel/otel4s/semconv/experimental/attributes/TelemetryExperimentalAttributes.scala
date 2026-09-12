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
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.TelemetryAttributes.TelemetryDistroName` instead.",
    ""
  )
  val TelemetryDistroName: AttributeKey[String] =
    AttributeKey("telemetry.distro.name")

  /** The version string of the auto instrumentation agent or distribution, if used.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.TelemetryAttributes.TelemetryDistroVersion` instead.",
    ""
  )
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

    /** <a href="https://opentelemetry.io/docs/languages/cpp/">C++</a>
      */
    case object Cpp extends TelemetrySdkLanguageValue("cpp")

    /** <a href="https://opentelemetry.io/docs/languages/dotnet/">.NET</a>
      */
    case object Dotnet extends TelemetrySdkLanguageValue("dotnet")

    /** <a href="https://opentelemetry.io/docs/languages/erlang/">Erlang/Elixir</a>
      */
    case object Erlang extends TelemetrySdkLanguageValue("erlang")

    /** <a href="https://opentelemetry.io/docs/languages/go/">Go</a>
      */
    case object Go extends TelemetrySdkLanguageValue("go")

    /** <a href="https://opentelemetry.io/docs/languages/java/">Java</a>
      */
    case object Java extends TelemetrySdkLanguageValue("java")

    /** <a href="https://opentelemetry.io/docs/languages/kotlin/">Kotlin</a>
      */
    case object Kotlin extends TelemetrySdkLanguageValue("kotlin")

    /** <a href="https://opentelemetry.io/docs/languages/js/">Node.js</a>
      */
    case object Nodejs extends TelemetrySdkLanguageValue("nodejs")

    /** <a href="https://opentelemetry.io/docs/languages/php/">PHP</a>
      */
    case object Php extends TelemetrySdkLanguageValue("php")

    /** <a href="https://opentelemetry.io/docs/languages/python/">Python</a>
      */
    case object Python extends TelemetrySdkLanguageValue("python")

    /** <a href="https://opentelemetry.io/docs/languages/ruby/">Ruby</a>
      */
    case object Ruby extends TelemetrySdkLanguageValue("ruby")

    /** <a href="https://opentelemetry.io/docs/languages/rust/">Rust</a>
      */
    case object Rust extends TelemetrySdkLanguageValue("rust")

    /** <a href="https://opentelemetry.io/docs/languages/swift/">Swift</a>
      */
    case object Swift extends TelemetrySdkLanguageValue("swift")

    /** <a href="https://opentelemetry.io/docs/languages/js/">Browser</a>
      */
    case object Webjs extends TelemetrySdkLanguageValue("webjs")

    /** scala. */
    case object Scala extends TelemetrySdkLanguageValue("scala")
  }

}
