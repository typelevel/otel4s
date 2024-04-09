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
object WebengineExperimentalAttributes {

  /** Additional description of the web engine (e.g. detailed version and
    * edition information).
    */
  val WebengineDescription: AttributeKey[String] = string(
    "webengine.description"
  )

  /** The name of the web engine.
    */
  val WebengineName: AttributeKey[String] = string("webengine.name")

  /** The version of the web engine.
    */
  val WebengineVersion: AttributeKey[String] = string("webengine.version")

}
