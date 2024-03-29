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
object MessagingExperimentalAttributes {

  /** The number of messages sent, received, or processed in the scope of the
    * batching operation.
    *
    * @note
    *   - Instrumentations SHOULD NOT set `messaging.batch.message_count` on
    *     spans that operate with a single message. When a messaging client
    *     library supports both batch and single-message API for the same
    *     operation, instrumentations SHOULD use `messaging.batch.message_count`
    *     for batching APIs and SHOULD NOT use it for single-message APIs.
    */
  val MessagingBatchMessageCount: AttributeKey[Long] = long(
    "messaging.batch.message_count"
  )

  /** A unique identifier for the client that consumes or produces a message.
    */
  val MessagingClientId: AttributeKey[String] = string("messaging.client_id")

  /** A boolean that is true if the message destination is anonymous (could be
    * unnamed or have auto-generated name).
    */
  val MessagingDestinationAnonymous: AttributeKey[Boolean] = boolean(
    "messaging.destination.anonymous"
  )

  /** The message destination name
    *
    * @note
    *   - Destination name SHOULD uniquely identify a specific queue, topic or
    *     other entity within the broker. If the broker doesn't have such
    *     notion, the destination name SHOULD uniquely identify the broker.
    */
  val MessagingDestinationName: AttributeKey[String] = string(
    "messaging.destination.name"
  )

  /** Low cardinality representation of the messaging destination name
    *
    * @note
    *   - Destination names could be constructed from templates. An example
    *     would be a destination name involving a user name or product id.
    *     Although the destination name in this case is of high cardinality, the
    *     underlying template is of low cardinality and can be effectively used
    *     for grouping and aggregation.
    */
  val MessagingDestinationTemplate: AttributeKey[String] = string(
    "messaging.destination.template"
  )

  /** A boolean that is true if the message destination is temporary and might
    * not exist anymore after messages are processed.
    */
  val MessagingDestinationTemporary: AttributeKey[Boolean] = boolean(
    "messaging.destination.temporary"
  )

  /** A boolean that is true if the publish message destination is anonymous
    * (could be unnamed or have auto-generated name).
    */
  val MessagingDestinationPublishAnonymous: AttributeKey[Boolean] = boolean(
    "messaging.destination_publish.anonymous"
  )

  /** The name of the original destination the message was published to
    *
    * @note
    *   - The name SHOULD uniquely identify a specific queue, topic, or other
    *     entity within the broker. If the broker doesn't have such notion, the
    *     original destination name SHOULD uniquely identify the broker.
    */
  val MessagingDestinationPublishName: AttributeKey[String] = string(
    "messaging.destination_publish.name"
  )

  /** The ordering key for a given message. If the attribute is not present, the
    * message does not have an ordering key.
    */
  val MessagingGcpPubsubMessageOrderingKey: AttributeKey[String] = string(
    "messaging.gcp_pubsub.message.ordering_key"
  )

  /** Name of the Kafka Consumer Group that is handling the message. Only
    * applies to consumers, not producers.
    */
  val MessagingKafkaConsumerGroup: AttributeKey[String] = string(
    "messaging.kafka.consumer.group"
  )

  /** Partition the message is sent to.
    */
  val MessagingKafkaDestinationPartition: AttributeKey[Long] = long(
    "messaging.kafka.destination.partition"
  )

  /** Message keys in Kafka are used for grouping alike messages to ensure
    * they're processed on the same partition. They differ from
    * `messaging.message.id` in that they're not unique. If the key is `null`,
    * the attribute MUST NOT be set.
    *
    * @note
    *   - If the key type is not string, it's string representation has to be
    *     supplied for the attribute. If the key has no unambiguous, canonical
    *     string form, don't include its value.
    */
  val MessagingKafkaMessageKey: AttributeKey[String] = string(
    "messaging.kafka.message.key"
  )

  /** The offset of a record in the corresponding Kafka partition.
    */
  val MessagingKafkaMessageOffset: AttributeKey[Long] = long(
    "messaging.kafka.message.offset"
  )

  /** A boolean that is true if the message is a tombstone.
    */
  val MessagingKafkaMessageTombstone: AttributeKey[Boolean] = boolean(
    "messaging.kafka.message.tombstone"
  )

  /** The size of the message body in bytes.
    *
    * @note
    *   - This can refer to both the compressed or uncompressed body size. If
    *     both sizes are known, the uncompressed body size should be used.
    */
  val MessagingMessageBodySize: AttributeKey[Long] = long(
    "messaging.message.body.size"
  )

  /** The conversation ID identifying the conversation to which the message
    * belongs, represented as a string. Sometimes called &quot;Correlation
    * ID&quot;.
    */
  val MessagingMessageConversationId: AttributeKey[String] = string(
    "messaging.message.conversation_id"
  )

  /** The size of the message body and metadata in bytes.
    *
    * @note
    *   - This can refer to both the compressed or uncompressed size. If both
    *     sizes are known, the uncompressed size should be used.
    */
  val MessagingMessageEnvelopeSize: AttributeKey[Long] = long(
    "messaging.message.envelope.size"
  )

  /** A value used by the messaging system as an identifier for the message,
    * represented as a string.
    */
  val MessagingMessageId: AttributeKey[String] = string("messaging.message.id")

  /** A string identifying the kind of messaging operation.
    *
    * @note
    *   - If a custom value is used, it MUST be of low cardinality.
    */
  val MessagingOperation: AttributeKey[String] = string("messaging.operation")

  /** RabbitMQ message routing key.
    */
  val MessagingRabbitmqDestinationRoutingKey: AttributeKey[String] = string(
    "messaging.rabbitmq.destination.routing_key"
  )

  /** Name of the RocketMQ producer/consumer group that is handling the message.
    * The client type is identified by the SpanKind.
    */
  val MessagingRocketmqClientGroup: AttributeKey[String] = string(
    "messaging.rocketmq.client_group"
  )

  /** Model of message consumption. This only applies to consumer spans.
    */
  val MessagingRocketmqConsumptionModel: AttributeKey[String] = string(
    "messaging.rocketmq.consumption_model"
  )

  /** The delay time level for delay message, which determines the message delay
    * time.
    */
  val MessagingRocketmqMessageDelayTimeLevel: AttributeKey[Long] = long(
    "messaging.rocketmq.message.delay_time_level"
  )

  /** The timestamp in milliseconds that the delay message is expected to be
    * delivered to consumer.
    */
  val MessagingRocketmqMessageDeliveryTimestamp: AttributeKey[Long] = long(
    "messaging.rocketmq.message.delivery_timestamp"
  )

  /** It is essential for FIFO message. Messages that belong to the same message
    * group are always processed one by one within the same consumer group.
    */
  val MessagingRocketmqMessageGroup: AttributeKey[String] = string(
    "messaging.rocketmq.message.group"
  )

  /** Key(s) of message, another way to mark message besides message id.
    */
  val MessagingRocketmqMessageKeys: AttributeKey[Seq[String]] = stringSeq(
    "messaging.rocketmq.message.keys"
  )

  /** The secondary classifier of message besides topic.
    */
  val MessagingRocketmqMessageTag: AttributeKey[String] = string(
    "messaging.rocketmq.message.tag"
  )

  /** Type of message.
    */
  val MessagingRocketmqMessageType: AttributeKey[String] = string(
    "messaging.rocketmq.message.type"
  )

  /** Namespace of RocketMQ resources, resources in different namespaces are
    * individual.
    */
  val MessagingRocketmqNamespace: AttributeKey[String] = string(
    "messaging.rocketmq.namespace"
  )

  /** An identifier for the messaging system being used. See below for a list of
    * well-known identifiers.
    */
  val MessagingSystem: AttributeKey[String] = string("messaging.system")
  // Enum definitions

  /** Values for [[MessagingOperation]].
    */
  abstract class MessagingOperationValue(val value: String)
  object MessagingOperationValue {

    /** One or more messages are provided for publishing to an intermediary. If
      * a single message is published, the context of the &#34;Publish&#34; span
      * can be used as the creation context and no &#34;Create&#34; span needs
      * to be created.
      */
    case object Publish extends MessagingOperationValue("publish")

    /** A message is created. &#34;Create&#34; spans always refer to a single
      * message and are used to provide a unique creation context for messages
      * in batch publishing scenarios.
      */
    case object Create extends MessagingOperationValue("create")

    /** One or more messages are requested by a consumer. This operation refers
      * to pull-based scenarios, where consumers explicitly call methods of
      * messaging SDKs to receive messages.
      */
    case object Receive extends MessagingOperationValue("receive")

    /** One or more messages are passed to a consumer. This operation refers to
      * push-based scenarios, where consumer register callbacks which get called
      * by messaging SDKs.
      */
    case object Deliver extends MessagingOperationValue("deliver")
  }

  /** Values for [[MessagingRocketmqConsumptionModel]].
    */
  abstract class MessagingRocketmqConsumptionModelValue(val value: String)
  object MessagingRocketmqConsumptionModelValue {

    /** Clustering consumption model. */
    case object Clustering
        extends MessagingRocketmqConsumptionModelValue("clustering")

    /** Broadcasting consumption model. */
    case object Broadcasting
        extends MessagingRocketmqConsumptionModelValue("broadcasting")
  }

  /** Values for [[MessagingRocketmqMessageType]].
    */
  abstract class MessagingRocketmqMessageTypeValue(val value: String)
  object MessagingRocketmqMessageTypeValue {

    /** Normal message. */
    case object Normal extends MessagingRocketmqMessageTypeValue("normal")

    /** FIFO message. */
    case object Fifo extends MessagingRocketmqMessageTypeValue("fifo")

    /** Delay message. */
    case object Delay extends MessagingRocketmqMessageTypeValue("delay")

    /** Transaction message. */
    case object Transaction
        extends MessagingRocketmqMessageTypeValue("transaction")
  }

  /** Values for [[MessagingSystem]].
    */
  abstract class MessagingSystemValue(val value: String)
  object MessagingSystemValue {

    /** Apache ActiveMQ. */
    case object Activemq extends MessagingSystemValue("activemq")

    /** Amazon Simple Queue Service (SQS). */
    case object AwsSqs extends MessagingSystemValue("aws_sqs")

    /** Azure Event Grid. */
    case object AzureEventgrid extends MessagingSystemValue("azure_eventgrid")

    /** Azure Event Hubs. */
    case object AzureEventhubs extends MessagingSystemValue("azure_eventhubs")

    /** Azure Service Bus. */
    case object AzureServicebus extends MessagingSystemValue("azure_servicebus")

    /** Google Cloud Pub/Sub. */
    case object GcpPubsub extends MessagingSystemValue("gcp_pubsub")

    /** Java Message Service. */
    case object Jms extends MessagingSystemValue("jms")

    /** Apache Kafka. */
    case object Kafka extends MessagingSystemValue("kafka")

    /** RabbitMQ. */
    case object Rabbitmq extends MessagingSystemValue("rabbitmq")

    /** Apache RocketMQ. */
    case object Rocketmq extends MessagingSystemValue("rocketmq")
  }

}
