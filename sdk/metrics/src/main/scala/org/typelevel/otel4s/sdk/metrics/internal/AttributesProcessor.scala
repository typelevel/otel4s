/*
 * Copyright 2024 Typelevel
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

package org.typelevel.otel4s.sdk.metrics.internal

import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.sdk.context.Context

trait AttributesProcessor {
  def process(incoming: Attributes, context: Context): Attributes
}

object AttributesProcessor {
  def noop: AttributesProcessor =
    (attributes, _) => attributes

  def filterByKeyName(keepWhen: String => Boolean): AttributesProcessor =
    (attributes, _) =>
      attributes.filter(attribute => keepWhen(attribute.key.name))
}
