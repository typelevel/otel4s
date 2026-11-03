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
object MessagingExperimentalAttributes {

  /** The number of messages sent, received, or processed in the scope of the batching operation.
    *
    * @note
    *   <p> Instrumentations SHOULD NOT set `messaging.batch.message_count` on spans that operate with a single message.
    *   When a messaging client library supports both batch and single-message API for the same operation,
    *   instrumentations SHOULD use `messaging.batch.message_count` for batching APIs and SHOULD NOT use it for
    *   single-message APIs.
    */
  val MessagingBatchMessageCount: AttributeKey[Long] =
    AttributeKey("messaging.batch.message_count")

  /** A unique identifier for the client that consumes or produces a message.
    */
  val MessagingClientId: AttributeKey[String] =
    AttributeKey("messaging.client.id")

  /** The name of the consumer group with which a consumer is associated.
    *
    * @note
    *   <p> Semantic conventions for individual messaging systems SHOULD document whether
    *   `messaging.consumer.group.name` is applicable and what it means in the context of that system.
    */
  val MessagingConsumerGroupName: AttributeKey[String] =
    AttributeKey("messaging.consumer.group.name")

  /** A boolean that is true if the message destination is anonymous (could be unnamed or have auto-generated name).
    */
  val MessagingDestinationAnonymous: AttributeKey[Boolean] =
    AttributeKey("messaging.destination.anonymous")

  /** The message destination name
    *
    * @note
    *   <p> SHOULD uniquely identify a specific queue, topic or other entity within the broker. If the broker doesn't
    *   have such notion, it SHOULD uniquely identify the broker.
    */
  val MessagingDestinationName: AttributeKey[String] =
    AttributeKey("messaging.destination.name")

  /** The identifier of the partition messages are sent to or received from, unique within the
    * `messaging.destination.name`.
    */
  val MessagingDestinationPartitionId: AttributeKey[String] =
    AttributeKey("messaging.destination.partition.id")

  /** The name of the destination subscription from which a message is consumed.
    *
    * @note
    *   <p> Semantic conventions for individual messaging systems SHOULD document whether
    *   `messaging.destination.subscription.name` is applicable and what it means in the context of that system.
    */
  val MessagingDestinationSubscriptionName: AttributeKey[String] =
    AttributeKey("messaging.destination.subscription.name")

  /** Low cardinality representation of the messaging destination name
    *
    * @note
    *   <p> Destination names could be constructed from templates. An example would be a destination name involving a
    *   username or product ID. Although the destination name in this case is of high cardinality, the underlying
    *   template is of low cardinality and can be effectively used for grouping and aggregation.
    */
  val MessagingDestinationTemplate: AttributeKey[String] =
    AttributeKey("messaging.destination.template")

  /** A boolean that is true if the message destination is temporary and might not exist anymore after messages are
    * processed.
    */
  val MessagingDestinationTemporary: AttributeKey[Boolean] =
    AttributeKey("messaging.destination.temporary")

  /** Deprecated, no replacement at this time.
    */
  @deprecated("Removed. No replacement at this time.", "")
  val MessagingDestinationPublishAnonymous: AttributeKey[Boolean] =
    AttributeKey("messaging.destination_publish.anonymous")

  /** Deprecated, no replacement at this time.
    */
  @deprecated("Removed. No replacement at this time.", "")
  val MessagingDestinationPublishName: AttributeKey[String] =
    AttributeKey("messaging.destination_publish.name")

  /** Deprecated, use `messaging.consumer.group.name` instead.
    */
  @deprecated("Replaced by `messaging.consumer.group.name`.", "")
  val MessagingEventhubsConsumerGroup: AttributeKey[String] =
    AttributeKey("messaging.eventhubs.consumer.group")

  /** Deprecated, use `azure.eventhubs.message.enqueued_time` instead.
    */
  @deprecated("Replaced by `azure.eventhubs.message.enqueued_time`.", "")
  val MessagingEventhubsMessageEnqueuedTime: AttributeKey[Long] =
    AttributeKey("messaging.eventhubs.message.enqueued_time")

  /** Deprecated, use `gcp.pubsub.message.ack_deadline` instead.
    */
  @deprecated("Replaced by `gcp.pubsub.message.ack_deadline`.", "")
  val MessagingGcpPubsubMessageAckDeadline: AttributeKey[Long] =
    AttributeKey("messaging.gcp_pubsub.message.ack_deadline")

  /** Deprecated, use `gcp.pubsub.message.ack_id` instead.
    */
  @deprecated("Replaced by `gcp.pubsub.message.ack_id`.", "")
  val MessagingGcpPubsubMessageAckId: AttributeKey[String] =
    AttributeKey("messaging.gcp_pubsub.message.ack_id")

  /** Deprecated, use `gcp.pubsub.message.delivery_attempt` instead.
    */
  @deprecated("Replaced by `gcp.pubsub.message.delivery_attempt`.", "")
  val MessagingGcpPubsubMessageDeliveryAttempt: AttributeKey[Long] =
    AttributeKey("messaging.gcp_pubsub.message.delivery_attempt")

  /** Deprecated, use `gcp.pubsub.message.ordering_key` instead.
    */
  @deprecated("Replaced by `gcp.pubsub.message.ordering_key`.", "")
  val MessagingGcpPubsubMessageOrderingKey: AttributeKey[String] =
    AttributeKey("messaging.gcp_pubsub.message.ordering_key")

  /** Deprecated, use `kafka.cluster.id` instead.
    *
    * @note
    *   <p> The cluster ID is a unique identifier reported by the Kafka broker. It identifies the cluster independently
    *   of the individual brokers the client is configured to connect to, and remains stable even if broker hostnames,
    *   IP addresses, or ports change.
    */
  @deprecated("Replaced by `kafka.cluster.id`.", "")
  val MessagingKafkaClusterId: AttributeKey[String] =
    AttributeKey("messaging.kafka.cluster.id")

  /** Deprecated, use `messaging.consumer.group.name` instead.
    */
  @deprecated("Replaced by `messaging.consumer.group.name`.", "")
  val MessagingKafkaConsumerGroup: AttributeKey[String] =
    AttributeKey("messaging.kafka.consumer.group")

  /** Deprecated, use `messaging.destination.partition.id` instead.
    */
  @deprecated("Record string representation of the partition ID in `messaging.destination.partition.id` attribute.", "")
  val MessagingKafkaDestinationPartition: AttributeKey[Long] =
    AttributeKey("messaging.kafka.destination.partition")

  /** Deprecated, use `kafka.message.key` instead.
    *
    * @note
    *   <p> If the key type is not string, it's string representation has to be supplied for the attribute. If the key
    *   has no unambiguous, canonical string form, don't include its value.
    */
  @deprecated("Replaced by `kafka.message.key`.", "")
  val MessagingKafkaMessageKey: AttributeKey[String] =
    AttributeKey("messaging.kafka.message.key")

  /** Deprecated, use `kafka.offset` instead.
    */
  @deprecated("Replaced by `kafka.offset`.", "")
  val MessagingKafkaMessageOffset: AttributeKey[Long] =
    AttributeKey("messaging.kafka.message.offset")

  /** Deprecated, use `kafka.message.tombstone` instead.
    */
  @deprecated("Replaced by `kafka.message.tombstone`.", "")
  val MessagingKafkaMessageTombstone: AttributeKey[Boolean] =
    AttributeKey("messaging.kafka.message.tombstone")

  /** Deprecated, use `kafka.offset` instead.
    */
  @deprecated("Replaced by `kafka.offset`.", "")
  val MessagingKafkaOffset: AttributeKey[Long] =
    AttributeKey("messaging.kafka.offset")

  /** The size of the message body in bytes.
    *
    * @note
    *   <p> This can refer to both the compressed or uncompressed body size. If both sizes are known, the uncompressed
    *   body size should be used.
    */
  val MessagingMessageBodySize: AttributeKey[Long] =
    AttributeKey("messaging.message.body.size")

  /** The conversation ID identifying the conversation to which the message belongs, represented as a string. Sometimes
    * called "Correlation ID".
    */
  val MessagingMessageConversationId: AttributeKey[String] =
    AttributeKey("messaging.message.conversation_id")

  /** The size of the message body and metadata in bytes.
    *
    * @note
    *   <p> This can refer to both the compressed or uncompressed size. If both sizes are known, the uncompressed size
    *   should be used.
    */
  val MessagingMessageEnvelopeSize: AttributeKey[Long] =
    AttributeKey("messaging.message.envelope.size")

  /** A value used by the messaging system as an identifier for the message, represented as a string.
    */
  val MessagingMessageId: AttributeKey[String] =
    AttributeKey("messaging.message.id")

  /** Deprecated, use `messaging.operation.type` instead.
    */
  @deprecated("Replaced by `messaging.operation.type`.", "")
  val MessagingOperation: AttributeKey[String] =
    AttributeKey("messaging.operation")

  /** The system-specific name of the messaging operation.
    */
  val MessagingOperationName: AttributeKey[String] =
    AttributeKey("messaging.operation.name")

  /** A string identifying the type of the messaging operation.
    *
    * @note
    *   <p> If a custom value is used, it MUST be of low cardinality.
    */
  val MessagingOperationType: AttributeKey[String] =
    AttributeKey("messaging.operation.type")

  /** Deprecated, use `rabbitmq.destination.routing_key` instead.
    */
  @deprecated("Replaced by `rabbitmq.destination.routing_key`.", "")
  val MessagingRabbitmqDestinationRoutingKey: AttributeKey[String] =
    AttributeKey("messaging.rabbitmq.destination.routing_key")

  /** Deprecated, use `rabbitmq.message.delivery_tag` instead.
    */
  @deprecated("Replaced by `rabbitmq.message.delivery_tag`.", "")
  val MessagingRabbitmqMessageDeliveryTag: AttributeKey[Long] =
    AttributeKey("messaging.rabbitmq.message.delivery_tag")

  /** Deprecated, use `messaging.consumer.group.name` instead.
    */
  @deprecated(
    "Replaced by `messaging.consumer.group.name` on the consumer spans. No replacement for producer spans.",
    ""
  )
  val MessagingRocketmqClientGroup: AttributeKey[String] =
    AttributeKey("messaging.rocketmq.client_group")

  /** Deprecated, use `rocketmq.consumption_model` instead.
    */
  @deprecated("Replaced by `rocketmq.consumption_model`.", "")
  val MessagingRocketmqConsumptionModel: AttributeKey[String] =
    AttributeKey("messaging.rocketmq.consumption_model")

  /** Deprecated, use `rocketmq.message.delay_time_level` instead.
    */
  @deprecated("Replaced by `rocketmq.message.delay_time_level`.", "")
  val MessagingRocketmqMessageDelayTimeLevel: AttributeKey[Long] =
    AttributeKey("messaging.rocketmq.message.delay_time_level")

  /** Deprecated, use `rocketmq.message.delivery_timestamp` instead.
    */
  @deprecated("Replaced by `rocketmq.message.delivery_timestamp`.", "")
  val MessagingRocketmqMessageDeliveryTimestamp: AttributeKey[Long] =
    AttributeKey("messaging.rocketmq.message.delivery_timestamp")

  /** Deprecated, use `rocketmq.message.group` instead.
    */
  @deprecated("Replaced by `rocketmq.message.group`.", "")
  val MessagingRocketmqMessageGroup: AttributeKey[String] =
    AttributeKey("messaging.rocketmq.message.group")

  /** Deprecated, use `rocketmq.message.keys` instead.
    */
  @deprecated("Replaced by `rocketmq.message.keys`.", "")
  val MessagingRocketmqMessageKeys: AttributeKey[Seq[String]] =
    AttributeKey("messaging.rocketmq.message.keys")

  /** Deprecated, use `rocketmq.message.tag` instead.
    */
  @deprecated("Replaced by `rocketmq.message.tag`.", "")
  val MessagingRocketmqMessageTag: AttributeKey[String] =
    AttributeKey("messaging.rocketmq.message.tag")

  /** Deprecated, use `rocketmq.message.type` instead.
    */
  @deprecated("Replaced by `rocketmq.message.type`.", "")
  val MessagingRocketmqMessageType: AttributeKey[String] =
    AttributeKey("messaging.rocketmq.message.type")

  /** Deprecated, use `rocketmq.namespace` instead.
    */
  @deprecated("Replaced by `rocketmq.namespace`.", "")
  val MessagingRocketmqNamespace: AttributeKey[String] =
    AttributeKey("messaging.rocketmq.namespace")

  /** Deprecated, use `messaging.destination.subscription.name` instead.
    */
  @deprecated("Replaced by `messaging.destination.subscription.name`.", "")
  val MessagingServicebusDestinationSubscriptionName: AttributeKey[String] =
    AttributeKey("messaging.servicebus.destination.subscription_name")

  /** Deprecated, use `azure.servicebus.disposition_status` instead.
    */
  @deprecated("Replaced by `azure.servicebus.disposition_status`.", "")
  val MessagingServicebusDispositionStatus: AttributeKey[String] =
    AttributeKey("messaging.servicebus.disposition_status")

  /** Deprecated, use `azure.servicebus.message.delivery_count` instead.
    */
  @deprecated("Replaced by `azure.servicebus.message.delivery_count`.", "")
  val MessagingServicebusMessageDeliveryCount: AttributeKey[Long] =
    AttributeKey("messaging.servicebus.message.delivery_count")

  /** Deprecated, use `azure.servicebus.message.enqueued_time` instead.
    */
  @deprecated("Replaced by `azure.servicebus.message.enqueued_time`.", "")
  val MessagingServicebusMessageEnqueuedTime: AttributeKey[Long] =
    AttributeKey("messaging.servicebus.message.enqueued_time")

  /** Deprecated, use `messaging.system.name` instead.
    *
    * @note
    *   <p> The actual messaging system may differ from the one known by the client. For example, when using Kafka
    *   client libraries to communicate with Azure Event Hubs, the `messaging.system` is set to `kafka` based on the
    *   instrumentation's best knowledge. <p> When migrating to `messaging.system.name`, update the following values:
    *   <ul> <li>`aws_sqs` to `aws.sqs` <li>`eventgrid` to `azure.eventgrid` <li>`eventhubs` to `azure.eventhubs`
    *   <li>`servicebus` to `azure.servicebus` <li>`gcp_pubsub` to `gcp.pubsub` </ul> <p> All other values are
    *   unchanged.
    */
  @deprecated("Replaced by `messaging.system.name`.", "")
  val MessagingSystem: AttributeKey[String] =
    AttributeKey("messaging.system")

  /** The messaging system as identified by the client instrumentation.
    *
    * @note
    *   <p> The actual messaging system may differ from the one known by the client. For example, when using Kafka
    *   client libraries to communicate with Azure Event Hubs, the `messaging.system.name` is set to `kafka` based on
    *   the instrumentation's best knowledge.
    */
  val MessagingSystemName: AttributeKey[String] =
    AttributeKey("messaging.system.name")

  /** Values for [[MessagingOperationType]].
    */
  abstract class MessagingOperationTypeValue(val value: String)
  object MessagingOperationTypeValue {
    implicit val attributeFromMessagingOperationTypeValue: Attribute.From[MessagingOperationTypeValue, String] = _.value

    /** A message is created. "Create" spans always refer to a single message and are used to provide a unique creation
      * context for messages in batch sending scenarios.
      */
    case object Create extends MessagingOperationTypeValue("create")

    /** One or more messages are provided for sending to an intermediary. If a single message is sent, the context of
      * the "Send" span can be used as the creation context and no "Create" span needs to be created.
      */
    case object Send extends MessagingOperationTypeValue("send")

    /** One or more messages are requested by a consumer. This operation refers to pull-based scenarios, where consumers
      * explicitly call methods of messaging SDKs to receive messages.
      */
    case object Receive extends MessagingOperationTypeValue("receive")

    /** One or more messages are processed by a consumer.
      */
    case object Process extends MessagingOperationTypeValue("process")

    /** One or more messages are settled.
      */
    case object Settle extends MessagingOperationTypeValue("settle")

    /** Deprecated. Use `process` instead.
      */
    case object Deliver extends MessagingOperationTypeValue("deliver")

    /** Deprecated. Use `send` instead.
      */
    case object Publish extends MessagingOperationTypeValue("publish")
  }

  /** Values for [[MessagingRocketmqConsumptionModel]].
    */
  @deprecated("Replaced by `rocketmq.consumption_model`.", "")
  abstract class MessagingRocketmqConsumptionModelValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object MessagingRocketmqConsumptionModelValue {
    implicit val attributeFromMessagingRocketmqConsumptionModelValue
        : Attribute.From[MessagingRocketmqConsumptionModelValue, String] = _.value

    /** Clustering consumption model
      */
    case object Clustering extends MessagingRocketmqConsumptionModelValue("clustering")

    /** Broadcasting consumption model
      */
    case object Broadcasting extends MessagingRocketmqConsumptionModelValue("broadcasting")
  }

  /** Values for [[MessagingRocketmqMessageType]].
    */
  @deprecated("Replaced by `rocketmq.message.type`.", "")
  abstract class MessagingRocketmqMessageTypeValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object MessagingRocketmqMessageTypeValue {
    implicit val attributeFromMessagingRocketmqMessageTypeValue
        : Attribute.From[MessagingRocketmqMessageTypeValue, String] = _.value

    /** Normal message
      */
    case object Normal extends MessagingRocketmqMessageTypeValue("normal")

    /** FIFO message
      */
    case object Fifo extends MessagingRocketmqMessageTypeValue("fifo")

    /** Delay message
      */
    case object Delay extends MessagingRocketmqMessageTypeValue("delay")

    /** Transaction message
      */
    case object Transaction extends MessagingRocketmqMessageTypeValue("transaction")
  }

  /** Values for [[MessagingServicebusDispositionStatus]].
    */
  @deprecated("Replaced by `azure.servicebus.disposition_status`.", "")
  abstract class MessagingServicebusDispositionStatusValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object MessagingServicebusDispositionStatusValue {
    implicit val attributeFromMessagingServicebusDispositionStatusValue
        : Attribute.From[MessagingServicebusDispositionStatusValue, String] = _.value

    /** Message is completed
      */
    case object Complete extends MessagingServicebusDispositionStatusValue("complete")

    /** Message is abandoned
      */
    case object Abandon extends MessagingServicebusDispositionStatusValue("abandon")

    /** Message is sent to dead letter queue
      */
    case object DeadLetter extends MessagingServicebusDispositionStatusValue("dead_letter")

    /** Message is deferred
      */
    case object Defer extends MessagingServicebusDispositionStatusValue("defer")
  }

  /** Values for [[MessagingSystem]].
    */
  @deprecated("Replaced by `messaging.system.name`.", "")
  abstract class MessagingSystemValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object MessagingSystemValue {
    implicit val attributeFromMessagingSystemValue: Attribute.From[MessagingSystemValue, String] = _.value

    /** Apache ActiveMQ
      */
    case object Activemq extends MessagingSystemValue("activemq")

    /** Amazon Simple Notification Service (SNS)
      */
    case object AwsSns extends MessagingSystemValue("aws.sns")

    /** Amazon Simple Queue Service (SQS)
      */
    case object AwsSqs extends MessagingSystemValue("aws_sqs")

    /** Azure Event Grid
      */
    case object Eventgrid extends MessagingSystemValue("eventgrid")

    /** Azure Event Hubs
      */
    case object Eventhubs extends MessagingSystemValue("eventhubs")

    /** Azure Service Bus
      */
    case object Servicebus extends MessagingSystemValue("servicebus")

    /** Google Cloud Pub/Sub
      */
    case object GcpPubsub extends MessagingSystemValue("gcp_pubsub")

    /** Java Message Service
      */
    case object Jms extends MessagingSystemValue("jms")

    /** Apache Kafka
      */
    case object Kafka extends MessagingSystemValue("kafka")

    /** RabbitMQ
      */
    case object Rabbitmq extends MessagingSystemValue("rabbitmq")

    /** Apache RocketMQ
      */
    case object Rocketmq extends MessagingSystemValue("rocketmq")

    /** Apache Pulsar
      */
    case object Pulsar extends MessagingSystemValue("pulsar")
  }

  /** Values for [[MessagingSystemName]].
    */
  abstract class MessagingSystemNameValue(val value: String)
  object MessagingSystemNameValue {
    implicit val attributeFromMessagingSystemNameValue: Attribute.From[MessagingSystemNameValue, String] = _.value

    /** Apache ActiveMQ
      */
    case object Activemq extends MessagingSystemNameValue("activemq")

    /** Amazon Simple Notification Service (SNS)
      */
    case object AwsSns extends MessagingSystemNameValue("aws.sns")

    /** Amazon Simple Queue Service (SQS)
      */
    case object AwsSqs extends MessagingSystemNameValue("aws.sqs")

    /** Azure Event Grid
      */
    case object AzureEventgrid extends MessagingSystemNameValue("azure.eventgrid")

    /** Azure Event Hubs
      */
    case object AzureEventhubs extends MessagingSystemNameValue("azure.eventhubs")

    /** Azure Service Bus
      */
    case object AzureServicebus extends MessagingSystemNameValue("azure.servicebus")

    /** Google Cloud Pub/Sub
      */
    case object GcpPubsub extends MessagingSystemNameValue("gcp.pubsub")

    /** Java Message Service
      */
    case object Jms extends MessagingSystemNameValue("jms")

    /** Apache Kafka
      */
    case object Kafka extends MessagingSystemNameValue("kafka")

    /** RabbitMQ
      */
    case object Rabbitmq extends MessagingSystemNameValue("rabbitmq")

    /** Apache RocketMQ
      */
    case object Rocketmq extends MessagingSystemNameValue("rocketmq")

    /** Apache Pulsar
      */
    case object Pulsar extends MessagingSystemNameValue("pulsar")
  }

}
