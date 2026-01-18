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
object OpenaiExperimentalAttributes {

  /** The service tier requested. May be a specific tier, default, or auto.
    */
  val OpenaiRequestServiceTier: AttributeKey[String] =
    AttributeKey("openai.request.service_tier")

  /** The service tier used for the response.
    */
  val OpenaiResponseServiceTier: AttributeKey[String] =
    AttributeKey("openai.response.service_tier")

  /** A fingerprint to track any eventual change in the Generative AI environment.
    */
  val OpenaiResponseSystemFingerprint: AttributeKey[String] =
    AttributeKey("openai.response.system_fingerprint")

  /** Values for [[OpenaiRequestServiceTier]].
    */
  abstract class OpenaiRequestServiceTierValue(val value: String)
  object OpenaiRequestServiceTierValue {
    implicit val attributeFromOpenaiRequestServiceTierValue: Attribute.From[OpenaiRequestServiceTierValue, String] =
      _.value

    /** The system will utilize scale tier credits until they are exhausted.
      */
    case object Auto extends OpenaiRequestServiceTierValue("auto")

    /** The system will utilize the default scale tier.
      */
    case object Default extends OpenaiRequestServiceTierValue("default")
  }

}
