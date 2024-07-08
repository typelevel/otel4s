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
object EventExperimentalAttributes {

  /**
  * Identifies the class / type of event.
  *
  * @note 
  *  - Event names are subject to the same rules as <a href="https://github.com/open-telemetry/opentelemetry-specification/tree/v1.33.0/specification/common/attribute-naming.md">attribute names</a>. Notably, event names are namespaced to avoid collisions and provide a clean separation of semantics for events in separate domains like browser, mobile, and kubernetes.
  */
  val EventName: AttributeKey[String] = string("event.name")

}