/*
 * Copyright 2022 Typelevel
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

package org.typelevel.otel4s.trace.experimental

import org.typelevel.otel4s.AttributeKey

import scala.annotation.StaticAnnotation
import scala.annotation.unused

/** Marks a method parameter to be captured by the `@withSpan` annotation.
  *
  * @param name
  *   the custom name of the attribute to use. If not specified, the name of the
  *   parameter will be used
  */
class spanAttribute(@unused name: String = "") extends StaticAnnotation {
  def this(key: AttributeKey[_]) =
    this(key.name)
}
