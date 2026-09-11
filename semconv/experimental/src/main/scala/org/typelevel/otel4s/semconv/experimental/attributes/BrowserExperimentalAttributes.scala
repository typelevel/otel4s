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
object BrowserExperimentalAttributes {

  /** Array of brand name and version separated by a space
    *
    * @note
    *   <p> This value is intended to be taken from the <a href="https://wicg.github.io/ua-client-hints/#interface">UA
    *   client hints API</a> (`navigator.userAgentData.brands`).
    */
  val BrowserBrands: AttributeKey[Seq[String]] =
    AttributeKey("browser.brands")

  /** Absolute URL of the current browser document according to <a
    * href="https://www.rfc-editor.org/rfc/rfc3986">RFC3986</a>.
    */
  val BrowserDocumentUrlFull: AttributeKey[String] =
    AttributeKey("browser.document.url.full")

  /** Preferred language of the user using the browser
    *
    * @note
    *   <p> This value is intended to be taken from the Navigator API `navigator.language`.
    */
  val BrowserLanguage: AttributeKey[String] =
    AttributeKey("browser.language")

  /** A boolean that is true if the browser is running on a mobile device
    *
    * @note
    *   <p> This value is intended to be taken from the <a href="https://wicg.github.io/ua-client-hints/#interface">UA
    *   client hints API</a> (`navigator.userAgentData.mobile`). If unavailable, this attribute SHOULD be left unset.
    */
  val BrowserMobile: AttributeKey[Boolean] =
    AttributeKey("browser.mobile")

  /** The platform on which the browser is running
    *
    * @note
    *   <p> This value is intended to be taken from the <a href="https://wicg.github.io/ua-client-hints/#interface">UA
    *   client hints API</a> (`navigator.userAgentData.platform`). If unavailable, the legacy `navigator.platform` API
    *   SHOULD NOT be used instead and this attribute SHOULD be left unset in order for the values to be consistent. The
    *   list of possible values is defined in the <a
    *   href="https://wicg.github.io/ua-client-hints/#sec-ch-ua-platform">W3C User-Agent Client Hints specification</a>.
    *   Note that some (but not all) of these values can overlap with values in the <a href="./os.md">`os.type` and
    *   `os.name` attributes</a>. However, for consistency, the values in the `browser.platform` attribute should
    *   capture the exact value that the user agent provides.
    */
  val BrowserPlatform: AttributeKey[String] =
    AttributeKey("browser.platform")

  /** The delta between the current value and the last-reported value. See <a
    * href="https://github.com/GoogleChrome/web-vitals?tab=readme-ov-file#report-only-the-delta-of-changes">delta</a>.
    */
  val BrowserWebVitalDelta: AttributeKey[Double] =
    AttributeKey("browser.web_vital.delta")

  /** A unique ID representing this particular metric instance.
    */
  val BrowserWebVitalId: AttributeKey[String] =
    AttributeKey("browser.web_vital.id")

  /** Name of the web vital.
    */
  val BrowserWebVitalName: AttributeKey[String] =
    AttributeKey("browser.web_vital.name")

  /** The type of navigation, as reported by the <a
    * href="https://developer.mozilla.org/docs/Web/API/PerformanceNavigationTiming/type">Navigation Timing API</a>, with
    * additional values reported by the web-vitals library.
    */
  val BrowserWebVitalNavigationType: AttributeKey[String] =
    AttributeKey("browser.web_vital.navigation_type")

  /** The rating of the web vital value against the "good", "needs improvement", and "poor" thresholds defined for the
    * metric.
    */
  val BrowserWebVitalRating: AttributeKey[String] =
    AttributeKey("browser.web_vital.rating")

  /** Value of the web vital.
    */
  val BrowserWebVitalValue: AttributeKey[Double] =
    AttributeKey("browser.web_vital.value")

  /** Values for [[BrowserWebVitalName]].
    */
  abstract class BrowserWebVitalNameValue(val value: String)
  object BrowserWebVitalNameValue {
    implicit val attributeFromBrowserWebVitalNameValue: Attribute.From[BrowserWebVitalNameValue, String] = _.value

    /** Cumulative Layout Shift. See <a href="https://web.dev/articles/cls">cls</a>.
      */
    case object Cls extends BrowserWebVitalNameValue("cls")

    /** Largest Contentful Paint. See <a href="https://web.dev/articles/lcp">lcp</a>.
      */
    case object Lcp extends BrowserWebVitalNameValue("lcp")

    /** First Contentful Paint. See <a href="https://web.dev/articles/fcp">fcp</a>.
      */
    case object Fcp extends BrowserWebVitalNameValue("fcp")

    /** Interaction to Next Paint. See <a href="https://web.dev/articles/inp">inp</a>.
      */
    case object Inp extends BrowserWebVitalNameValue("inp")

    /** Time to First Byte. See <a href="https://web.dev/articles/ttfb">ttfb</a>.
      */
    case object Ttfb extends BrowserWebVitalNameValue("ttfb")

    /** First Input Delay. See <a href="https://web.dev/articles/fid">fid</a>.
      */
    case object Fid extends BrowserWebVitalNameValue("fid")
  }

  /** Values for [[BrowserWebVitalNavigationType]].
    */
  abstract class BrowserWebVitalNavigationTypeValue(val value: String)
  object BrowserWebVitalNavigationTypeValue {
    implicit val attributeFromBrowserWebVitalNavigationTypeValue
        : Attribute.From[BrowserWebVitalNavigationTypeValue, String] = _.value

    /** Navigation started by clicking a link, entering a URL, form submission, or a script operation.
      */
    case object Navigate extends BrowserWebVitalNavigationTypeValue("navigate")

    /** Navigation through a reload operation or a `Location.reload()` call.
      */
    case object Reload extends BrowserWebVitalNavigationTypeValue("reload")

    /** Navigation through the browser's history traversal (e.g. back/forward buttons).
      */
    case object BackForward extends BrowserWebVitalNavigationTypeValue("back-forward")

    /** Navigation restoring a page from the back/forward cache (bfcache).
      */
    case object BackForwardCache extends BrowserWebVitalNavigationTypeValue("back-forward-cache")

    /** Navigation to a page that was prerendered.
      */
    case object Prerender extends BrowserWebVitalNavigationTypeValue("prerender")

    /** Navigation restoring a page that was previously discarded by the browser.
      */
    case object Restore extends BrowserWebVitalNavigationTypeValue("restore")
  }

  /** Values for [[BrowserWebVitalRating]].
    */
  abstract class BrowserWebVitalRatingValue(val value: String)
  object BrowserWebVitalRatingValue {
    implicit val attributeFromBrowserWebVitalRatingValue: Attribute.From[BrowserWebVitalRatingValue, String] = _.value

    /** The metric value is within the "good" threshold.
      */
    case object Good extends BrowserWebVitalRatingValue("good")

    /** The metric value is within the "needs improvement" threshold.
      */
    case object NeedsImprovement extends BrowserWebVitalRatingValue("needs-improvement")

    /** The metric value is within the "poor" threshold.
      */
    case object Poor extends BrowserWebVitalRatingValue("poor")
  }

}
