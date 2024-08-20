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

  /**
  * The number of messages sent, received, or processed in the scope of the batching operation.
  *
  * @note 
  *  - Instrumentations SHOULD NOT set `messaging.batch.message_count` on spans that operate with a single message. When a messaging client library supports both batch and single-message API for the same operation, instrumentations SHOULD use `messaging.batch.message_count` for batching APIs and SHOULD NOT use it for single-message APIs.
  */
  val MessagingBatchMessageCount: AttributeKey[Long] = long("messaging.batch.message_count")

  /**
  * A unique identifier for the client that consumes or produces a message.
  */
  val MessagingClientId: AttributeKey[String] = string("messaging.client.id")

  /**
  * The name of the consumer group with which a consumer is associated.
  *
  * @note 
  *  - Semantic conventions for individual messaging systems SHOULD document whether `messaging.consumer.group.name` is applicable and what it means in the context of that system.
  */
  val MessagingConsumerGroupName: AttributeKey[String] = string("messaging.consumer.group.name")

  /**
  * A boolean that is true if the message destination is anonymous (could be unnamed or have auto-generated name).
  */
  val MessagingDestinationAnonymous: AttributeKey[Boolean] = boolean("messaging.destination.anonymous")

  /**
  * The message destination name
  *
  * @note 
  *  - Destination name SHOULD uniquely identify a specific queue, topic or other entity within the broker. If
the broker doesn't have such notion, the destination name SHOULD uniquely identify the broker.
  */
  val MessagingDestinationName: AttributeKey[String] = string("messaging.destination.name")

  /**
  * The identifier of the partition messages are sent to or received from, unique within the `messaging.destination.name`.
  */
  val MessagingDestinationPartitionId: AttributeKey[String] = string("messaging.destination.partition.id")

  /**
  * The name of the destination subscription from which a message is consumed.
  *
  * @note 
  *  - Semantic conventions for individual messaging systems SHOULD document whether `messaging.destination.subscription.name` is applicable and what it means in the context of that system.
  */
  val MessagingDestinationSubscriptionName: AttributeKey[String] = string("messaging.destination.subscription.name")

  /**
  * Low cardinality representation of the messaging destination name
  *
  * @note 
  *  - Destination names could be constructed from templates. An example would be a destination name involving a user name or product id. Although the destination name in this case is of high cardinality, the underlying template is of low cardinality and can be effectively used for grouping and aggregation.
  */
  val MessagingDestinationTemplate: AttributeKey[String] = string("messaging.destination.template")

  /**
  * A boolean that is true if the message destination is temporary and might not exist anymore after messages are processed.
  */
  val MessagingDestinationTemporary: AttributeKey[Boolean] = boolean("messaging.destination.temporary")

  /**
  * Deprecated, no replacement at this time.
  */
  @deprecated("No replacement at this time", "0.5.0")
  val MessagingDestinationPublishAnonymous: AttributeKey[Boolean] = boolean("messaging.destination_publish.anonymous")

  /**
  * Deprecated, no replacement at this time.
  */
  @deprecated("No replacement at this time", "0.5.0")
  val MessagingDestinationPublishName: AttributeKey[String] = string("messaging.destination_publish.name")

  /**
  * Deprecated, use `messaging.consumer.group.name` instead.
  */
  @deprecated("Use `messaging.consumer.group.name` instead", "0.5.0")
  val MessagingEventhubsConsumerGroup: AttributeKey[String] = string("messaging.eventhubs.consumer.group")

  /**
  * The UTC epoch seconds at which the message has been accepted and stored in the entity.
  */
  val MessagingEventhubsMessageEnqueuedTime: AttributeKey[Long] = long("messaging.eventhubs.message.enqueued_time")

  /**
  * The ack deadline in seconds set for the modify ack deadline request.
  */
  val MessagingGcpPubsubMessageAckDeadline: AttributeKey[Long] = long("messaging.gcp_pubsub.message.ack_deadline")

  /**
  * The ack id for a given message.
  */
  val MessagingGcpPubsubMessageAckId: AttributeKey[String] = string("messaging.gcp_pubsub.message.ack_id")

  /**
  * The delivery attempt for a given message.
  */
  val MessagingGcpPubsubMessageDeliveryAttempt: AttributeKey[Long] = long("messaging.gcp_pubsub.message.delivery_attempt")

  /**
  * The ordering key for a given message. If the attribute is not present, the message does not have an ordering key.
  */
  val MessagingGcpPubsubMessageOrderingKey: AttributeKey[String] = string("messaging.gcp_pubsub.message.ordering_key")

  /**
  * Deprecated, use `messaging.consumer.group.name` instead.
  */
  @deprecated("Use `messaging.consumer.group.name` instead", "0.5.0")
  val MessagingKafkaConsumerGroup: AttributeKey[String] = string("messaging.kafka.consumer.group")

  /**
  * Deprecated, use `messaging.destination.partition.id` instead.
  */
  @deprecated("Use `messaging.destination.partition.id` instead", "0.5.0")
  val MessagingKafkaDestinationPartition: AttributeKey[Long] = long("messaging.kafka.destination.partition")

  /**
  * Message keys in Kafka are used for grouping alike messages to ensure they're processed on the same partition. They differ from `messaging.message.id` in that they're not unique. If the key is `null`, the attribute MUST NOT be set.
  *
  * @note 
  *  - If the key type is not string, it's string representation has to be supplied for the attribute. If the key has no unambiguous, canonical string form, don't include its value.
  */
  val MessagingKafkaMessageKey: AttributeKey[String] = string("messaging.kafka.message.key")

  /**
  * Deprecated, use `messaging.kafka.offset` instead.
  */
  @deprecated("Use `messaging.kafka.offset` instead", "0.5.0")
  val MessagingKafkaMessageOffset: AttributeKey[Long] = long("messaging.kafka.message.offset")

  /**
  * A boolean that is true if the message is a tombstone.
  */
  val MessagingKafkaMessageTombstone: AttributeKey[Boolean] = boolean("messaging.kafka.message.tombstone")

  /**
  * The offset of a record in the corresponding Kafka partition.
  */
  val MessagingKafkaOffset: AttributeKey[Long] = long("messaging.kafka.offset")

  /**
  * The size of the message body in bytes.
  *
  * @note 
  *  - This can refer to both the compressed or uncompressed body size. If both sizes are known, the uncompressed
body size should be used.
  */
  val MessagingMessageBodySize: AttributeKey[Long] = long("messaging.message.body.size")

  /**
  * The conversation ID identifying the conversation to which the message belongs, represented as a string. Sometimes called &quot;Correlation ID&quot;.
  */
  val MessagingMessageConversationId: AttributeKey[String] = string("messaging.message.conversation_id")

  /**
  * The size of the message body and metadata in bytes.
  *
  * @note 
  *  - This can refer to both the compressed or uncompressed size. If both sizes are known, the uncompressed
size should be used.
  */
  val MessagingMessageEnvelopeSize: AttributeKey[Long] = long("messaging.message.envelope.size")

  /**
  * A value used by the messaging system as an identifier for the message, represented as a string.
  */
  val MessagingMessageId: AttributeKey[String] = string("messaging.message.id")

  /**
  * Deprecated, use `messaging.operation.type` instead.
  */
  @deprecated("Use `messaging.operation.type` instead", "0.5.0")
  val MessagingOperation: AttributeKey[String] = string("messaging.operation")

  /**
  * The system-specific name of the messaging operation.
  */
  val MessagingOperationName: AttributeKey[String] = string("messaging.operation.name")

  /**
  * A string identifying the type of the messaging operation.
  *
  * @note 
  *  - If a custom value is used, it MUST be of low cardinality.
  */
  val MessagingOperationType: AttributeKey[String] = string("messaging.operation.type")

  /**
  * RabbitMQ message routing key.
  */
  val MessagingRabbitmqDestinationRoutingKey: AttributeKey[String] = string("messaging.rabbitmq.destination.routing_key")

  /**
  * RabbitMQ message delivery tag
  */
  val MessagingRabbitmqMessageDeliveryTag: AttributeKey[Long] = long("messaging.rabbitmq.message.delivery_tag")

  /**
  * Deprecated, use `messaging.consumer.group.name` instead.
  */
  @deprecated("Use `messaging.consumer.group.name` instead", "0.5.0")
  val MessagingRocketmqClientGroup: AttributeKey[String] = string("messaging.rocketmq.client_group")

  /**
  * Model of message consumption. This only applies to consumer spans.
  */
  val MessagingRocketmqConsumptionModel: AttributeKey[String] = string("messaging.rocketmq.consumption_model")

  /**
  * The delay time level for delay message, which determines the message delay time.
  */
  val MessagingRocketmqMessageDelayTimeLevel: AttributeKey[Long] = long("messaging.rocketmq.message.delay_time_level")

  /**
  * The timestamp in milliseconds that the delay message is expected to be delivered to consumer.
  */
  val MessagingRocketmqMessageDeliveryTimestamp: AttributeKey[Long] = long("messaging.rocketmq.message.delivery_timestamp")

  /**
  * It is essential for FIFO message. Messages that belong to the same message group are always processed one by one within the same consumer group.
  */
  val MessagingRocketmqMessageGroup: AttributeKey[String] = string("messaging.rocketmq.message.group")

  /**
  * Key(s) of message, another way to mark message besides message id.
  */
  val MessagingRocketmqMessageKeys: AttributeKey[Seq[String]] = stringSeq("messaging.rocketmq.message.keys")

  /**
  * The secondary classifier of message besides topic.
  */
  val MessagingRocketmqMessageTag: AttributeKey[String] = string("messaging.rocketmq.message.tag")

  /**
  * Type of message.
  */
  val MessagingRocketmqMessageType: AttributeKey[String] = string("messaging.rocketmq.message.type")

  /**
  * Namespace of RocketMQ resources, resources in different namespaces are individual.
  */
  val MessagingRocketmqNamespace: AttributeKey[String] = string("messaging.rocketmq.namespace")

  /**
  * Deprecated, use `messaging.servicebus.destination.subscription_name` instead.
  */
  @deprecated("Use `messaging.servicebus.destination.subscription_name` instead", "0.5.0")
  val MessagingServicebusDestinationSubscriptionName: AttributeKey[String] = string("messaging.servicebus.destination.subscription_name")

  /**
  * Describes the <a href="https://learn.microsoft.com/azure/service-bus-messaging/message-transfers-locks-settlement#peeklock">settlement type</a>.
  */
  val MessagingServicebusDispositionStatus: AttributeKey[String] = string("messaging.servicebus.disposition_status")

  /**
  * Number of deliveries that have been attempted for this message.
  */
  val MessagingServicebusMessageDeliveryCount: AttributeKey[Long] = long("messaging.servicebus.message.delivery_count")

  /**
  * The UTC epoch seconds at which the message has been accepted and stored in the entity.
  */
  val MessagingServicebusMessageEnqueuedTime: AttributeKey[Long] = long("messaging.servicebus.message.enqueued_time")

  /**
  * The messaging system as identified by the client instrumentation.
  *
  * @note 
  *  - The actual messaging system may differ from the one known by the client. For example, when using Kafka client libraries to communicate with Azure Event Hubs, the `messaging.system` is set to `kafka` based on the instrumentation's best knowledge.
  */
  val MessagingSystem: AttributeKey[String] = string("messaging.system")
  // Enum definitions
  
  /**
   * Values for [[MessagingOperationType]].
   */
  abstract class MessagingOperationTypeValue(val value: String)
  object MessagingOperationTypeValue {
    /** One or more messages are provided for publishing to an intermediary. If a single message is published, the context of the &#34;Publish&#34; span can be used as the creation context and no &#34;Create&#34; span needs to be created. */
    case object Publish extends MessagingOperationTypeValue("publish")
    /** A message is created. &#34;Create&#34; spans always refer to a single message and are used to provide a unique creation context for messages in batch publishing scenarios. */
    case object Create extends MessagingOperationTypeValue("create")
    /** One or more messages are requested by a consumer. This operation refers to pull-based scenarios, where consumers explicitly call methods of messaging SDKs to receive messages. */
    case object Receive extends MessagingOperationTypeValue("receive")
    /** One or more messages are processed by a consumer. */
    case object Process extends MessagingOperationTypeValue("process")
    /** One or more messages are settled. */
    case object Settle extends MessagingOperationTypeValue("settle")
    /** Deprecated. Use `process` instead. */
    case object Deliver extends MessagingOperationTypeValue("deliver")
  }
  /**
   * Values for [[MessagingRocketmqConsumptionModel]].
   */
  abstract class MessagingRocketmqConsumptionModelValue(val value: String)
  object MessagingRocketmqConsumptionModelValue {
    /** Clustering consumption model. */
    case object Clustering extends MessagingRocketmqConsumptionModelValue("clustering")
    /** Broadcasting consumption model. */
    case object Broadcasting extends MessagingRocketmqConsumptionModelValue("broadcasting")
  }
  /**
   * Values for [[MessagingRocketmqMessageType]].
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
    case object Transaction extends MessagingRocketmqMessageTypeValue("transaction")
  }
  /**
   * Values for [[MessagingServicebusDispositionStatus]].
   */
  abstract class MessagingServicebusDispositionStatusValue(val value: String)
  object MessagingServicebusDispositionStatusValue {
    /** Message is completed. */
    case object Complete extends MessagingServicebusDispositionStatusValue("complete")
    /** Message is abandoned. */
    case object Abandon extends MessagingServicebusDispositionStatusValue("abandon")
    /** Message is sent to dead letter queue. */
    case object DeadLetter extends MessagingServicebusDispositionStatusValue("dead_letter")
    /** Message is deferred. */
    case object Defer extends MessagingServicebusDispositionStatusValue("defer")
  }
  /**
   * Values for [[MessagingSystem]].
   */
  abstract class MessagingSystemValue(val value: String)
  object MessagingSystemValue {
    /** Apache ActiveMQ. */
    case object Activemq extends MessagingSystemValue("activemq")
    /** Amazon Simple Queue Service (SQS). */
    case object AwsSqs extends MessagingSystemValue("aws_sqs")
    /** Azure Event Grid. */
    case object Eventgrid extends MessagingSystemValue("eventgrid")
    /** Azure Event Hubs. */
    case object Eventhubs extends MessagingSystemValue("eventhubs")
    /** Azure Service Bus. */
    case object Servicebus extends MessagingSystemValue("servicebus")
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
    /** Apache Pulsar. */
    case object Pulsar extends MessagingSystemValue("pulsar")
  }

}