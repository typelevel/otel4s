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
object DeviceExperimentalAttributes {

  /**
  * A unique identifier representing the device
  *
  * @note 
  *  - The device identifier MUST only be defined using the values outlined below. This value is not an advertising identifier and MUST NOT be used as such. On iOS (Swift or Objective-C), this value MUST be equal to the <a href="https://developer.apple.com/documentation/uikit/uidevice/1620059-identifierforvendor">vendor identifier</a>. On Android (Java or Kotlin), this value MUST be equal to the Firebase Installation ID or a globally unique UUID which is persisted across sessions in your application. More information can be found <a href="https://developer.android.com/training/articles/user-data-ids">here</a> on best practices and exact implementation details. Caution should be taken when storing personal data or anything which can identify a user. GDPR and data protection laws may apply, ensure you do your own due diligence.
  */
  val DeviceId: AttributeKey[String] = string("device.id")

  /**
  * The name of the device manufacturer
  *
  * @note 
  *  - The Android OS provides this field via <a href="https://developer.android.com/reference/android/os/Build#MANUFACTURER">Build</a>. iOS apps SHOULD hardcode the value `Apple`.
  */
  val DeviceManufacturer: AttributeKey[String] = string("device.manufacturer")

  /**
  * The model identifier for the device
  *
  * @note 
  *  - It's recommended this value represents a machine-readable version of the model identifier rather than the market or consumer-friendly name of the device.
  */
  val DeviceModelIdentifier: AttributeKey[String] = string("device.model.identifier")

  /**
  * The marketing name for the device model
  *
  * @note 
  *  - It's recommended this value represents a human-readable version of the device model rather than a machine-readable alternative.
  */
  val DeviceModelName: AttributeKey[String] = string("device.model.name")

}