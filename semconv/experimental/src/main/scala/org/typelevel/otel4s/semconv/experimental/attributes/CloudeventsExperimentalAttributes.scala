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
object CloudeventsExperimentalAttributes {

  /** The <a
    * href="https://github.com/cloudevents/spec/blob/v1.0.2/cloudevents/spec.md#id">event_id</a>
    * uniquely identifies the event.
    */
  val CloudeventsEventId: AttributeKey[String] =
    AttributeKey("cloudevents.event_id")

  /** The <a
    * href="https://github.com/cloudevents/spec/blob/v1.0.2/cloudevents/spec.md#source-1">source</a>
    * identifies the context in which an event happened.
    */
  val CloudeventsEventSource: AttributeKey[String] =
    AttributeKey("cloudevents.event_source")

  /** The <a
    * href="https://github.com/cloudevents/spec/blob/v1.0.2/cloudevents/spec.md#specversion">version
    * of the CloudEvents specification</a> which the event uses.
    */
  val CloudeventsEventSpecVersion: AttributeKey[String] =
    AttributeKey("cloudevents.event_spec_version")

  /** The <a
    * href="https://github.com/cloudevents/spec/blob/v1.0.2/cloudevents/spec.md#subject">subject</a>
    * of the event in the context of the event producer (identified by source).
    */
  val CloudeventsEventSubject: AttributeKey[String] =
    AttributeKey("cloudevents.event_subject")

  /** The <a
    * href="https://github.com/cloudevents/spec/blob/v1.0.2/cloudevents/spec.md#type">event_type</a>
    * contains a value describing the type of event related to the originating
    * occurrence.
    */
  val CloudeventsEventType: AttributeKey[String] =
    AttributeKey("cloudevents.event_type")

}
