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

package org.typelevel.otel4s.sdk
package common

trait InstrumentationScopeInfo {
  def name: String
  def version: Option[String]
  def schemaUrl: Option[String]
  def attributes: Attributes
}

object InstrumentationScopeInfo {

  private final case class InstrumentationScopeInfoImpl(
      name: String,
      version: Option[String],
      schemaUrl: Option[String],
      attributes: Attributes
  ) extends InstrumentationScopeInfo

  def create(
      name: String,
      version: Option[String],
      schemaUrl: Option[String],
      attributes: Attributes
  ): InstrumentationScopeInfo =
    InstrumentationScopeInfoImpl(name, version, schemaUrl, attributes)
}
