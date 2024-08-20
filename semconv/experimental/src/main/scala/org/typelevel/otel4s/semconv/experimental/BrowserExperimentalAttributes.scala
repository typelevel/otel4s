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
object BrowserExperimentalAttributes {

  /**
  * Array of brand name and version separated by a space
  *
  * @note 
  *  - This value is intended to be taken from the <a href="https://wicg.github.io/ua-client-hints/#interface">UA client hints API</a> (`navigator.userAgentData.brands`).
  */
  val BrowserBrands: AttributeKey[Seq[String]] = stringSeq("browser.brands")

  /**
  * Preferred language of the user using the browser
  *
  * @note 
  *  - This value is intended to be taken from the Navigator API `navigator.language`.
  */
  val BrowserLanguage: AttributeKey[String] = string("browser.language")

  /**
  * A boolean that is true if the browser is running on a mobile device
  *
  * @note 
  *  - This value is intended to be taken from the <a href="https://wicg.github.io/ua-client-hints/#interface">UA client hints API</a> (`navigator.userAgentData.mobile`). If unavailable, this attribute SHOULD be left unset.
  */
  val BrowserMobile: AttributeKey[Boolean] = boolean("browser.mobile")

  /**
  * The platform on which the browser is running
  *
  * @note 
  *  - This value is intended to be taken from the <a href="https://wicg.github.io/ua-client-hints/#interface">UA client hints API</a> (`navigator.userAgentData.platform`). If unavailable, the legacy `navigator.platform` API SHOULD NOT be used instead and this attribute SHOULD be left unset in order for the values to be consistent.
The list of possible values is defined in the <a href="https://wicg.github.io/ua-client-hints/#sec-ch-ua-platform">W3C User-Agent Client Hints specification</a>. Note that some (but not all) of these values can overlap with values in the <a href="./os.md">`os.type` and `os.name` attributes</a>. However, for consistency, the values in the `browser.platform` attribute should capture the exact value that the user agent provides.
  */
  val BrowserPlatform: AttributeKey[String] = string("browser.platform")

}