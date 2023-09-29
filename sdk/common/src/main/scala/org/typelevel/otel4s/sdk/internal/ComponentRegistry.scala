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
package internal

import org.typelevel.otel4s.sdk.common.InstrumentationScopeInfo

final class ComponentRegistry[F[_], A](
    factory: InstrumentationScopeInfo => F[A]
) {

  def get(
      name: String,
      version: Option[String],
      schemaUrl: Option[String],
      attributes: Attributes
  ): F[A] = {
    buildComponent(
      InstrumentationScopeInfo.create(name, version, schemaUrl, attributes)
    )
  }

  private def buildComponent(info: InstrumentationScopeInfo) = {
    val component = factory(info)
    component
  }

}
