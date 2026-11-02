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
object KafkaExperimentalAttributes {

  /** The Kafka cluster ID, obtained from the broker metadata exposed through the Kafka client (or AdminClient) API.
    *
    * @note
    *   <p> The cluster ID is a unique identifier reported by the Kafka broker. It identifies the cluster independently
    *   of the individual brokers the client is configured to connect to, and remains stable even if broker hostnames,
    *   IP addresses, or ports change.
    */
  val KafkaClusterId: AttributeKey[String] =
    AttributeKey("kafka.cluster.id")

  /** Message keys in Kafka are used for grouping alike messages to ensure they're processed on the same partition. They
    * differ from `messaging.message.id` in that they're not unique. If the key is `null`, the attribute MUST NOT be
    * set.
    *
    * @note
    *   <p> If the key type is not string, its string representation has to be supplied for the attribute. If the key
    *   has no unambiguous, canonical string form, don't include its value.
    */
  val KafkaMessageKey: AttributeKey[String] =
    AttributeKey("kafka.message.key")

  /** A boolean that is true if the message is a tombstone.
    */
  val KafkaMessageTombstone: AttributeKey[Boolean] =
    AttributeKey("kafka.message.tombstone")

  /** The offset of a record in the corresponding Kafka partition.
    */
  val KafkaOffset: AttributeKey[Long] =
    AttributeKey("kafka.offset")

}
