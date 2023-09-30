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

package org.typelevel.otel4s.sdk.trace

sealed trait SpanLimits {
  def maxNumberOfAttributes: Int
  def maxNumberOfEvents: Int
  def maxNumberOfLinks: Int
  def maxNumberOfAttributesPerEvent: Int
  def maxNumberOfAttributesPerLink: Int
  def maxAttributeValueLength: Int
}

object SpanLimits {

  private final case class SpanLimitsImpl(
      maxNumberOfAttributes: Int,
      maxNumberOfEvents: Int,
      maxNumberOfLinks: Int,
      maxNumberOfAttributesPerEvent: Int,
      maxNumberOfAttributesPerLink: Int,
      maxAttributeValueLength: Int,
  ) extends SpanLimits

  val Default: SpanLimits = SpanLimitsImpl(
    maxNumberOfAttributes = 100,
    maxNumberOfEvents = 100,
    maxNumberOfLinks = 100,
    maxNumberOfAttributesPerEvent = 100,
    maxNumberOfAttributesPerLink = 100,
    maxAttributeValueLength = 100
  )

}
