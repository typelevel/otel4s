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

package org.typelevel.otel4s
package semconv
package experimental
package metrics

import org.typelevel.otel4s.metrics._

// DO NOT EDIT, this is an Auto-generated file from buildscripts/templates/registry/otel4s/metrics/SemanticMetrics.scala.j2
object MessagingExperimentalMetrics {

  /** Number of messages that were delivered to the application. <p>
    * @note
    *   <p> Records the number of messages pulled from the broker or number of messages dispatched to the application in
    *   push-based scenarios. The metric SHOULD be reported once per message delivery. For example, if receiving and
    *   processing operations are both instrumented for a single message delivery, this counter is incremented when the
    *   message is received and not reported when it is processed.
    */
  object ClientConsumedMessages {

    val Name = "messaging.client.consumed.messages"
    val Description = "Number of messages that were delivered to the application."
    val Unit = "{message}"

    object AttributeSpecs {

      /** Describes a class of error the operation ended with. <p>
        * @note
        *   <p> The `error.type` SHOULD be predictable, and SHOULD have low cardinality. <p> When `error.type` is set to
        *   a type (e.g., an exception type), its canonical class name identifying the type within the artifact SHOULD
        *   be used. <p> Instrumentations SHOULD document the list of errors they report. <p> The cardinality of
        *   `error.type` within one instrumentation library SHOULD be low. Telemetry consumers that aggregate data from
        *   multiple instrumentation libraries and applications should be prepared for `error.type` to have high
        *   cardinality at query time when no additional filters are applied. <p> If the operation has completed
        *   successfully, instrumentations SHOULD NOT set `error.type`. <p> If a specific domain defines its own set of
        *   error identifiers (such as HTTP or gRPC status codes), it's RECOMMENDED to: <p> <ul> <li>Use a
        *   domain-specific attribute <li>Set `error.type` to capture all errors, regardless of whether they are defined
        *   within the domain-specific set or not. </ul>
        */
      val errorType: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("error.type"),
          List(
            "amqp:decode-error",
            "KAFKA_STORAGE_ERROR",
            "channel-error",
          ),
          Requirement.conditionallyRequired("If and only if the messaging operation has failed."),
          Stability.stable
        )

      /** The name of the consumer group with which a consumer is associated. <p>
        * @note
        *   <p> Semantic conventions for individual messaging systems SHOULD document whether
        *   `messaging.consumer.group.name` is applicable and what it means in the context of that system.
        */
      val messagingConsumerGroupName: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("messaging.consumer.group.name"),
          List(
            "my-group",
            "indexer",
          ),
          Requirement.conditionallyRequired("if applicable."),
          Stability.experimental
        )

      /** The message destination name <p>
        * @note
        *   <p> Destination name SHOULD uniquely identify a specific queue, topic or other entity within the broker. If
        *   the broker doesn't have such notion, the destination name SHOULD uniquely identify the broker.
        */
      val messagingDestinationName: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("messaging.destination.name"),
          List(
            "MyQueue",
            "MyTopic",
          ),
          Requirement.conditionallyRequired(
            "if and only if `messaging.destination.name` is known to have low cardinality. Otherwise, `messaging.destination.template` MAY be populated."
          ),
          Stability.experimental
        )

      /** The identifier of the partition messages are sent to or received from, unique within the
        * `messaging.destination.name`.
        */
      val messagingDestinationPartitionId: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("messaging.destination.partition.id"),
          List(
            "1",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      /** The name of the destination subscription from which a message is consumed. <p>
        * @note
        *   <p> Semantic conventions for individual messaging systems SHOULD document whether
        *   `messaging.destination.subscription.name` is applicable and what it means in the context of that system.
        */
      val messagingDestinationSubscriptionName: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("messaging.destination.subscription.name"),
          List(
            "subscription-a",
          ),
          Requirement.conditionallyRequired("if applicable."),
          Stability.experimental
        )

      /** Low cardinality representation of the messaging destination name <p>
        * @note
        *   <p> Destination names could be constructed from templates. An example would be a destination name involving
        *   a user name or product id. Although the destination name in this case is of high cardinality, the underlying
        *   template is of low cardinality and can be effectively used for grouping and aggregation.
        */
      val messagingDestinationTemplate: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("messaging.destination.template"),
          List(
            "/customers/{customerId}",
          ),
          Requirement.conditionallyRequired("if available."),
          Stability.experimental
        )

      /** The system-specific name of the messaging operation.
        */
      val messagingOperationName: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("messaging.operation.name"),
          List(
            "receive",
            "peek",
            "poll",
            "consume",
          ),
          Requirement.required,
          Stability.experimental
        )

      /** The messaging system as identified by the client instrumentation. <p>
        * @note
        *   <p> The actual messaging system may differ from the one known by the client. For example, when using Kafka
        *   client libraries to communicate with Azure Event Hubs, the `messaging.system` is set to `kafka` based on the
        *   instrumentation's best knowledge.
        */
      val messagingSystem: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("messaging.system"),
          List(
          ),
          Requirement.required,
          Stability.experimental
        )

      /** Server domain name if available without reverse DNS lookup; otherwise, IP address or Unix domain socket name.
        * <p>
        * @note
        *   <p> Server domain name of the broker if available without reverse DNS lookup; otherwise, IP address or Unix
        *   domain socket name.
        */
      val serverAddress: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("server.address"),
          List(
            "example.com",
            "10.1.2.80",
            "/tmp/my.sock",
          ),
          Requirement.conditionallyRequired("If available."),
          Stability.stable
        )

      /** Server port number. <p>
        * @note
        *   <p> When observed from the client side, and when communicating through an intermediary, `server.port` SHOULD
        *   represent the server port behind any intermediaries, for example proxies, if it's available.
        */
      val serverPort: AttributeSpec[Long] =
        AttributeSpec(
          AttributeKey("server.port"),
          List(
            80,
            8080,
            443,
          ),
          Requirement.recommended,
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          errorType,
          messagingConsumerGroupName,
          messagingDestinationName,
          messagingDestinationPartitionId,
          messagingDestinationSubscriptionName,
          messagingDestinationTemplate,
          messagingOperationName,
          messagingSystem,
          serverAddress,
          serverPort,
        )
    }

    def create[F[_]: Meter]: F[Counter[F, Long]] =
      Meter[F]
        .counter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Duration of messaging operation initiated by a producer or consumer client. <p>
    * @note
    *   <p> This metric SHOULD NOT be used to report processing duration - processing duration is reported in
    *   `messaging.process.duration` metric.
    */
  object ClientOperationDuration {

    val Name = "messaging.client.operation.duration"
    val Description = "Duration of messaging operation initiated by a producer or consumer client."
    val Unit = "s"

    object AttributeSpecs {

      /** Describes a class of error the operation ended with. <p>
        * @note
        *   <p> The `error.type` SHOULD be predictable, and SHOULD have low cardinality. <p> When `error.type` is set to
        *   a type (e.g., an exception type), its canonical class name identifying the type within the artifact SHOULD
        *   be used. <p> Instrumentations SHOULD document the list of errors they report. <p> The cardinality of
        *   `error.type` within one instrumentation library SHOULD be low. Telemetry consumers that aggregate data from
        *   multiple instrumentation libraries and applications should be prepared for `error.type` to have high
        *   cardinality at query time when no additional filters are applied. <p> If the operation has completed
        *   successfully, instrumentations SHOULD NOT set `error.type`. <p> If a specific domain defines its own set of
        *   error identifiers (such as HTTP or gRPC status codes), it's RECOMMENDED to: <p> <ul> <li>Use a
        *   domain-specific attribute <li>Set `error.type` to capture all errors, regardless of whether they are defined
        *   within the domain-specific set or not. </ul>
        */
      val errorType: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("error.type"),
          List(
            "amqp:decode-error",
            "KAFKA_STORAGE_ERROR",
            "channel-error",
          ),
          Requirement.conditionallyRequired("If and only if the messaging operation has failed."),
          Stability.stable
        )

      /** The name of the consumer group with which a consumer is associated. <p>
        * @note
        *   <p> Semantic conventions for individual messaging systems SHOULD document whether
        *   `messaging.consumer.group.name` is applicable and what it means in the context of that system.
        */
      val messagingConsumerGroupName: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("messaging.consumer.group.name"),
          List(
            "my-group",
            "indexer",
          ),
          Requirement.conditionallyRequired("if applicable."),
          Stability.experimental
        )

      /** The message destination name <p>
        * @note
        *   <p> Destination name SHOULD uniquely identify a specific queue, topic or other entity within the broker. If
        *   the broker doesn't have such notion, the destination name SHOULD uniquely identify the broker.
        */
      val messagingDestinationName: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("messaging.destination.name"),
          List(
            "MyQueue",
            "MyTopic",
          ),
          Requirement.conditionallyRequired(
            "if and only if `messaging.destination.name` is known to have low cardinality. Otherwise, `messaging.destination.template` MAY be populated."
          ),
          Stability.experimental
        )

      /** The identifier of the partition messages are sent to or received from, unique within the
        * `messaging.destination.name`.
        */
      val messagingDestinationPartitionId: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("messaging.destination.partition.id"),
          List(
            "1",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      /** The name of the destination subscription from which a message is consumed. <p>
        * @note
        *   <p> Semantic conventions for individual messaging systems SHOULD document whether
        *   `messaging.destination.subscription.name` is applicable and what it means in the context of that system.
        */
      val messagingDestinationSubscriptionName: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("messaging.destination.subscription.name"),
          List(
            "subscription-a",
          ),
          Requirement.conditionallyRequired("if applicable."),
          Stability.experimental
        )

      /** Low cardinality representation of the messaging destination name <p>
        * @note
        *   <p> Destination names could be constructed from templates. An example would be a destination name involving
        *   a user name or product id. Although the destination name in this case is of high cardinality, the underlying
        *   template is of low cardinality and can be effectively used for grouping and aggregation.
        */
      val messagingDestinationTemplate: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("messaging.destination.template"),
          List(
            "/customers/{customerId}",
          ),
          Requirement.conditionallyRequired("if available."),
          Stability.experimental
        )

      /** The system-specific name of the messaging operation.
        */
      val messagingOperationName: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("messaging.operation.name"),
          List(
            "send",
            "receive",
            "ack",
          ),
          Requirement.required,
          Stability.experimental
        )

      /** A string identifying the type of the messaging operation. <p>
        * @note
        *   <p> If a custom value is used, it MUST be of low cardinality.
        */
      val messagingOperationType: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("messaging.operation.type"),
          List(
          ),
          Requirement.conditionallyRequired("If applicable."),
          Stability.experimental
        )

      /** The messaging system as identified by the client instrumentation. <p>
        * @note
        *   <p> The actual messaging system may differ from the one known by the client. For example, when using Kafka
        *   client libraries to communicate with Azure Event Hubs, the `messaging.system` is set to `kafka` based on the
        *   instrumentation's best knowledge.
        */
      val messagingSystem: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("messaging.system"),
          List(
          ),
          Requirement.required,
          Stability.experimental
        )

      /** Server domain name if available without reverse DNS lookup; otherwise, IP address or Unix domain socket name.
        * <p>
        * @note
        *   <p> Server domain name of the broker if available without reverse DNS lookup; otherwise, IP address or Unix
        *   domain socket name.
        */
      val serverAddress: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("server.address"),
          List(
            "example.com",
            "10.1.2.80",
            "/tmp/my.sock",
          ),
          Requirement.conditionallyRequired("If available."),
          Stability.stable
        )

      /** Server port number. <p>
        * @note
        *   <p> When observed from the client side, and when communicating through an intermediary, `server.port` SHOULD
        *   represent the server port behind any intermediaries, for example proxies, if it's available.
        */
      val serverPort: AttributeSpec[Long] =
        AttributeSpec(
          AttributeKey("server.port"),
          List(
            80,
            8080,
            443,
          ),
          Requirement.recommended,
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          errorType,
          messagingConsumerGroupName,
          messagingDestinationName,
          messagingDestinationPartitionId,
          messagingDestinationSubscriptionName,
          messagingDestinationTemplate,
          messagingOperationName,
          messagingOperationType,
          messagingSystem,
          serverAddress,
          serverPort,
        )
    }

    def create[F[_]: Meter](boundaries: BucketBoundaries): F[Histogram[F, Double]] =
      Meter[F]
        .histogram[Double](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

  /** Number of messages producer attempted to publish to the broker. <p>
    * @note
    *   <p> This metric MUST NOT count messages that were created haven't yet been attempted to be published.
    */
  object ClientPublishedMessages {

    val Name = "messaging.client.published.messages"
    val Description = "Number of messages producer attempted to publish to the broker."
    val Unit = "{message}"

    object AttributeSpecs {

      /** Describes a class of error the operation ended with. <p>
        * @note
        *   <p> The `error.type` SHOULD be predictable, and SHOULD have low cardinality. <p> When `error.type` is set to
        *   a type (e.g., an exception type), its canonical class name identifying the type within the artifact SHOULD
        *   be used. <p> Instrumentations SHOULD document the list of errors they report. <p> The cardinality of
        *   `error.type` within one instrumentation library SHOULD be low. Telemetry consumers that aggregate data from
        *   multiple instrumentation libraries and applications should be prepared for `error.type` to have high
        *   cardinality at query time when no additional filters are applied. <p> If the operation has completed
        *   successfully, instrumentations SHOULD NOT set `error.type`. <p> If a specific domain defines its own set of
        *   error identifiers (such as HTTP or gRPC status codes), it's RECOMMENDED to: <p> <ul> <li>Use a
        *   domain-specific attribute <li>Set `error.type` to capture all errors, regardless of whether they are defined
        *   within the domain-specific set or not. </ul>
        */
      val errorType: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("error.type"),
          List(
            "amqp:decode-error",
            "KAFKA_STORAGE_ERROR",
            "channel-error",
          ),
          Requirement.conditionallyRequired("If and only if the messaging operation has failed."),
          Stability.stable
        )

      /** The message destination name <p>
        * @note
        *   <p> Destination name SHOULD uniquely identify a specific queue, topic or other entity within the broker. If
        *   the broker doesn't have such notion, the destination name SHOULD uniquely identify the broker.
        */
      val messagingDestinationName: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("messaging.destination.name"),
          List(
            "MyQueue",
            "MyTopic",
          ),
          Requirement.conditionallyRequired(
            "if and only if `messaging.destination.name` is known to have low cardinality. Otherwise, `messaging.destination.template` MAY be populated."
          ),
          Stability.experimental
        )

      /** The identifier of the partition messages are sent to or received from, unique within the
        * `messaging.destination.name`.
        */
      val messagingDestinationPartitionId: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("messaging.destination.partition.id"),
          List(
            "1",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      /** Low cardinality representation of the messaging destination name <p>
        * @note
        *   <p> Destination names could be constructed from templates. An example would be a destination name involving
        *   a user name or product id. Although the destination name in this case is of high cardinality, the underlying
        *   template is of low cardinality and can be effectively used for grouping and aggregation.
        */
      val messagingDestinationTemplate: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("messaging.destination.template"),
          List(
            "/customers/{customerId}",
          ),
          Requirement.conditionallyRequired("if available."),
          Stability.experimental
        )

      /** The system-specific name of the messaging operation.
        */
      val messagingOperationName: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("messaging.operation.name"),
          List(
            "send",
            "schedule",
            "enqueue",
          ),
          Requirement.required,
          Stability.experimental
        )

      /** The messaging system as identified by the client instrumentation. <p>
        * @note
        *   <p> The actual messaging system may differ from the one known by the client. For example, when using Kafka
        *   client libraries to communicate with Azure Event Hubs, the `messaging.system` is set to `kafka` based on the
        *   instrumentation's best knowledge.
        */
      val messagingSystem: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("messaging.system"),
          List(
          ),
          Requirement.required,
          Stability.experimental
        )

      /** Server domain name if available without reverse DNS lookup; otherwise, IP address or Unix domain socket name.
        * <p>
        * @note
        *   <p> Server domain name of the broker if available without reverse DNS lookup; otherwise, IP address or Unix
        *   domain socket name.
        */
      val serverAddress: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("server.address"),
          List(
            "example.com",
            "10.1.2.80",
            "/tmp/my.sock",
          ),
          Requirement.conditionallyRequired("If available."),
          Stability.stable
        )

      /** Server port number. <p>
        * @note
        *   <p> When observed from the client side, and when communicating through an intermediary, `server.port` SHOULD
        *   represent the server port behind any intermediaries, for example proxies, if it's available.
        */
      val serverPort: AttributeSpec[Long] =
        AttributeSpec(
          AttributeKey("server.port"),
          List(
            80,
            8080,
            443,
          ),
          Requirement.recommended,
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          errorType,
          messagingDestinationName,
          messagingDestinationPartitionId,
          messagingDestinationTemplate,
          messagingOperationName,
          messagingSystem,
          serverAddress,
          serverPort,
        )
    }

    def create[F[_]: Meter]: F[Counter[F, Long]] =
      Meter[F]
        .counter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Duration of processing operation. <p>
    * @note
    *   <p> This metric MUST be reported for operations with `messaging.operation.type` that matches `process`.
    */
  object ProcessDuration {

    val Name = "messaging.process.duration"
    val Description = "Duration of processing operation."
    val Unit = "s"

    object AttributeSpecs {

      /** Describes a class of error the operation ended with. <p>
        * @note
        *   <p> The `error.type` SHOULD be predictable, and SHOULD have low cardinality. <p> When `error.type` is set to
        *   a type (e.g., an exception type), its canonical class name identifying the type within the artifact SHOULD
        *   be used. <p> Instrumentations SHOULD document the list of errors they report. <p> The cardinality of
        *   `error.type` within one instrumentation library SHOULD be low. Telemetry consumers that aggregate data from
        *   multiple instrumentation libraries and applications should be prepared for `error.type` to have high
        *   cardinality at query time when no additional filters are applied. <p> If the operation has completed
        *   successfully, instrumentations SHOULD NOT set `error.type`. <p> If a specific domain defines its own set of
        *   error identifiers (such as HTTP or gRPC status codes), it's RECOMMENDED to: <p> <ul> <li>Use a
        *   domain-specific attribute <li>Set `error.type` to capture all errors, regardless of whether they are defined
        *   within the domain-specific set or not. </ul>
        */
      val errorType: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("error.type"),
          List(
            "amqp:decode-error",
            "KAFKA_STORAGE_ERROR",
            "channel-error",
          ),
          Requirement.conditionallyRequired("If and only if the messaging operation has failed."),
          Stability.stable
        )

      /** The name of the consumer group with which a consumer is associated. <p>
        * @note
        *   <p> Semantic conventions for individual messaging systems SHOULD document whether
        *   `messaging.consumer.group.name` is applicable and what it means in the context of that system.
        */
      val messagingConsumerGroupName: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("messaging.consumer.group.name"),
          List(
            "my-group",
            "indexer",
          ),
          Requirement.conditionallyRequired("if applicable."),
          Stability.experimental
        )

      /** The message destination name <p>
        * @note
        *   <p> Destination name SHOULD uniquely identify a specific queue, topic or other entity within the broker. If
        *   the broker doesn't have such notion, the destination name SHOULD uniquely identify the broker.
        */
      val messagingDestinationName: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("messaging.destination.name"),
          List(
            "MyQueue",
            "MyTopic",
          ),
          Requirement.conditionallyRequired(
            "if and only if `messaging.destination.name` is known to have low cardinality. Otherwise, `messaging.destination.template` MAY be populated."
          ),
          Stability.experimental
        )

      /** The identifier of the partition messages are sent to or received from, unique within the
        * `messaging.destination.name`.
        */
      val messagingDestinationPartitionId: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("messaging.destination.partition.id"),
          List(
            "1",
          ),
          Requirement.recommended,
          Stability.experimental
        )

      /** The name of the destination subscription from which a message is consumed. <p>
        * @note
        *   <p> Semantic conventions for individual messaging systems SHOULD document whether
        *   `messaging.destination.subscription.name` is applicable and what it means in the context of that system.
        */
      val messagingDestinationSubscriptionName: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("messaging.destination.subscription.name"),
          List(
            "subscription-a",
          ),
          Requirement.conditionallyRequired("if applicable."),
          Stability.experimental
        )

      /** Low cardinality representation of the messaging destination name <p>
        * @note
        *   <p> Destination names could be constructed from templates. An example would be a destination name involving
        *   a user name or product id. Although the destination name in this case is of high cardinality, the underlying
        *   template is of low cardinality and can be effectively used for grouping and aggregation.
        */
      val messagingDestinationTemplate: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("messaging.destination.template"),
          List(
            "/customers/{customerId}",
          ),
          Requirement.conditionallyRequired("if available."),
          Stability.experimental
        )

      /** The system-specific name of the messaging operation.
        */
      val messagingOperationName: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("messaging.operation.name"),
          List(
            "process",
            "consume",
            "handle",
          ),
          Requirement.required,
          Stability.experimental
        )

      /** The messaging system as identified by the client instrumentation. <p>
        * @note
        *   <p> The actual messaging system may differ from the one known by the client. For example, when using Kafka
        *   client libraries to communicate with Azure Event Hubs, the `messaging.system` is set to `kafka` based on the
        *   instrumentation's best knowledge.
        */
      val messagingSystem: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("messaging.system"),
          List(
          ),
          Requirement.required,
          Stability.experimental
        )

      /** Server domain name if available without reverse DNS lookup; otherwise, IP address or Unix domain socket name.
        * <p>
        * @note
        *   <p> Server domain name of the broker if available without reverse DNS lookup; otherwise, IP address or Unix
        *   domain socket name.
        */
      val serverAddress: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("server.address"),
          List(
            "example.com",
            "10.1.2.80",
            "/tmp/my.sock",
          ),
          Requirement.conditionallyRequired("If available."),
          Stability.stable
        )

      /** Server port number. <p>
        * @note
        *   <p> When observed from the client side, and when communicating through an intermediary, `server.port` SHOULD
        *   represent the server port behind any intermediaries, for example proxies, if it's available.
        */
      val serverPort: AttributeSpec[Long] =
        AttributeSpec(
          AttributeKey("server.port"),
          List(
            80,
            8080,
            443,
          ),
          Requirement.recommended,
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          errorType,
          messagingConsumerGroupName,
          messagingDestinationName,
          messagingDestinationPartitionId,
          messagingDestinationSubscriptionName,
          messagingDestinationTemplate,
          messagingOperationName,
          messagingSystem,
          serverAddress,
          serverPort,
        )
    }

    def create[F[_]: Meter](boundaries: BucketBoundaries): F[Histogram[F, Double]] =
      Meter[F]
        .histogram[Double](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

  /** Deprecated. Use `messaging.client.consumed.messages` instead.
    */
  @deprecated("Replaced by `messaging.client.consumed.messages`.", "")
  object ProcessMessages {

    val Name = "messaging.process.messages"
    val Description = "Deprecated. Use `messaging.client.consumed.messages` instead."
    val Unit = "{message}"

    object AttributeSpecs {

      /** Describes a class of error the operation ended with. <p>
        * @note
        *   <p> The `error.type` SHOULD be predictable, and SHOULD have low cardinality. <p> When `error.type` is set to
        *   a type (e.g., an exception type), its canonical class name identifying the type within the artifact SHOULD
        *   be used. <p> Instrumentations SHOULD document the list of errors they report. <p> The cardinality of
        *   `error.type` within one instrumentation library SHOULD be low. Telemetry consumers that aggregate data from
        *   multiple instrumentation libraries and applications should be prepared for `error.type` to have high
        *   cardinality at query time when no additional filters are applied. <p> If the operation has completed
        *   successfully, instrumentations SHOULD NOT set `error.type`. <p> If a specific domain defines its own set of
        *   error identifiers (such as HTTP or gRPC status codes), it's RECOMMENDED to: <p> <ul> <li>Use a
        *   domain-specific attribute <li>Set `error.type` to capture all errors, regardless of whether they are defined
        *   within the domain-specific set or not. </ul>
        */
      val errorType: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("error.type"),
          List(
            "amqp:decode-error",
            "KAFKA_STORAGE_ERROR",
            "channel-error",
          ),
          Requirement.conditionallyRequired("If and only if the messaging operation has failed."),
          Stability.stable
        )

      /** The system-specific name of the messaging operation.
        */
      val messagingOperationName: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("messaging.operation.name"),
          List(
            "ack",
            "nack",
            "send",
          ),
          Requirement.required,
          Stability.experimental
        )

      /** Server domain name if available without reverse DNS lookup; otherwise, IP address or Unix domain socket name.
        * <p>
        * @note
        *   <p> Server domain name of the broker if available without reverse DNS lookup; otherwise, IP address or Unix
        *   domain socket name.
        */
      val serverAddress: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("server.address"),
          List(
            "example.com",
            "10.1.2.80",
            "/tmp/my.sock",
          ),
          Requirement.conditionallyRequired("If available."),
          Stability.stable
        )

      /** Server port number. <p>
        * @note
        *   <p> When observed from the client side, and when communicating through an intermediary, `server.port` SHOULD
        *   represent the server port behind any intermediaries, for example proxies, if it's available.
        */
      val serverPort: AttributeSpec[Long] =
        AttributeSpec(
          AttributeKey("server.port"),
          List(
            80,
            8080,
            443,
          ),
          Requirement.recommended,
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          errorType,
          messagingOperationName,
          serverAddress,
          serverPort,
        )
    }

    def create[F[_]: Meter]: F[Counter[F, Long]] =
      Meter[F]
        .counter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Deprecated. Use `messaging.client.operation.duration` instead.
    */
  @deprecated("Replaced by `messaging.client.operation.duration`.", "")
  object PublishDuration {

    val Name = "messaging.publish.duration"
    val Description = "Deprecated. Use `messaging.client.operation.duration` instead."
    val Unit = "s"

    object AttributeSpecs {

      /** Describes a class of error the operation ended with. <p>
        * @note
        *   <p> The `error.type` SHOULD be predictable, and SHOULD have low cardinality. <p> When `error.type` is set to
        *   a type (e.g., an exception type), its canonical class name identifying the type within the artifact SHOULD
        *   be used. <p> Instrumentations SHOULD document the list of errors they report. <p> The cardinality of
        *   `error.type` within one instrumentation library SHOULD be low. Telemetry consumers that aggregate data from
        *   multiple instrumentation libraries and applications should be prepared for `error.type` to have high
        *   cardinality at query time when no additional filters are applied. <p> If the operation has completed
        *   successfully, instrumentations SHOULD NOT set `error.type`. <p> If a specific domain defines its own set of
        *   error identifiers (such as HTTP or gRPC status codes), it's RECOMMENDED to: <p> <ul> <li>Use a
        *   domain-specific attribute <li>Set `error.type` to capture all errors, regardless of whether they are defined
        *   within the domain-specific set or not. </ul>
        */
      val errorType: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("error.type"),
          List(
            "amqp:decode-error",
            "KAFKA_STORAGE_ERROR",
            "channel-error",
          ),
          Requirement.conditionallyRequired("If and only if the messaging operation has failed."),
          Stability.stable
        )

      /** The system-specific name of the messaging operation.
        */
      val messagingOperationName: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("messaging.operation.name"),
          List(
            "ack",
            "nack",
            "send",
          ),
          Requirement.required,
          Stability.experimental
        )

      /** Server domain name if available without reverse DNS lookup; otherwise, IP address or Unix domain socket name.
        * <p>
        * @note
        *   <p> Server domain name of the broker if available without reverse DNS lookup; otherwise, IP address or Unix
        *   domain socket name.
        */
      val serverAddress: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("server.address"),
          List(
            "example.com",
            "10.1.2.80",
            "/tmp/my.sock",
          ),
          Requirement.conditionallyRequired("If available."),
          Stability.stable
        )

      /** Server port number. <p>
        * @note
        *   <p> When observed from the client side, and when communicating through an intermediary, `server.port` SHOULD
        *   represent the server port behind any intermediaries, for example proxies, if it's available.
        */
      val serverPort: AttributeSpec[Long] =
        AttributeSpec(
          AttributeKey("server.port"),
          List(
            80,
            8080,
            443,
          ),
          Requirement.recommended,
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          errorType,
          messagingOperationName,
          serverAddress,
          serverPort,
        )
    }

    def create[F[_]: Meter](boundaries: BucketBoundaries): F[Histogram[F, Double]] =
      Meter[F]
        .histogram[Double](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

  /** Deprecated. Use `messaging.client.produced.messages` instead.
    */
  @deprecated("Replaced by `messaging.client.produced.messages`.", "")
  object PublishMessages {

    val Name = "messaging.publish.messages"
    val Description = "Deprecated. Use `messaging.client.produced.messages` instead."
    val Unit = "{message}"

    object AttributeSpecs {

      /** Describes a class of error the operation ended with. <p>
        * @note
        *   <p> The `error.type` SHOULD be predictable, and SHOULD have low cardinality. <p> When `error.type` is set to
        *   a type (e.g., an exception type), its canonical class name identifying the type within the artifact SHOULD
        *   be used. <p> Instrumentations SHOULD document the list of errors they report. <p> The cardinality of
        *   `error.type` within one instrumentation library SHOULD be low. Telemetry consumers that aggregate data from
        *   multiple instrumentation libraries and applications should be prepared for `error.type` to have high
        *   cardinality at query time when no additional filters are applied. <p> If the operation has completed
        *   successfully, instrumentations SHOULD NOT set `error.type`. <p> If a specific domain defines its own set of
        *   error identifiers (such as HTTP or gRPC status codes), it's RECOMMENDED to: <p> <ul> <li>Use a
        *   domain-specific attribute <li>Set `error.type` to capture all errors, regardless of whether they are defined
        *   within the domain-specific set or not. </ul>
        */
      val errorType: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("error.type"),
          List(
            "amqp:decode-error",
            "KAFKA_STORAGE_ERROR",
            "channel-error",
          ),
          Requirement.conditionallyRequired("If and only if the messaging operation has failed."),
          Stability.stable
        )

      /** The system-specific name of the messaging operation.
        */
      val messagingOperationName: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("messaging.operation.name"),
          List(
            "ack",
            "nack",
            "send",
          ),
          Requirement.required,
          Stability.experimental
        )

      /** Server domain name if available without reverse DNS lookup; otherwise, IP address or Unix domain socket name.
        * <p>
        * @note
        *   <p> Server domain name of the broker if available without reverse DNS lookup; otherwise, IP address or Unix
        *   domain socket name.
        */
      val serverAddress: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("server.address"),
          List(
            "example.com",
            "10.1.2.80",
            "/tmp/my.sock",
          ),
          Requirement.conditionallyRequired("If available."),
          Stability.stable
        )

      /** Server port number. <p>
        * @note
        *   <p> When observed from the client side, and when communicating through an intermediary, `server.port` SHOULD
        *   represent the server port behind any intermediaries, for example proxies, if it's available.
        */
      val serverPort: AttributeSpec[Long] =
        AttributeSpec(
          AttributeKey("server.port"),
          List(
            80,
            8080,
            443,
          ),
          Requirement.recommended,
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          errorType,
          messagingOperationName,
          serverAddress,
          serverPort,
        )
    }

    def create[F[_]: Meter]: F[Counter[F, Long]] =
      Meter[F]
        .counter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Deprecated. Use `messaging.client.operation.duration` instead.
    */
  @deprecated("Replaced by `messaging.client.operation.duration`.", "")
  object ReceiveDuration {

    val Name = "messaging.receive.duration"
    val Description = "Deprecated. Use `messaging.client.operation.duration` instead."
    val Unit = "s"

    object AttributeSpecs {

      /** Describes a class of error the operation ended with. <p>
        * @note
        *   <p> The `error.type` SHOULD be predictable, and SHOULD have low cardinality. <p> When `error.type` is set to
        *   a type (e.g., an exception type), its canonical class name identifying the type within the artifact SHOULD
        *   be used. <p> Instrumentations SHOULD document the list of errors they report. <p> The cardinality of
        *   `error.type` within one instrumentation library SHOULD be low. Telemetry consumers that aggregate data from
        *   multiple instrumentation libraries and applications should be prepared for `error.type` to have high
        *   cardinality at query time when no additional filters are applied. <p> If the operation has completed
        *   successfully, instrumentations SHOULD NOT set `error.type`. <p> If a specific domain defines its own set of
        *   error identifiers (such as HTTP or gRPC status codes), it's RECOMMENDED to: <p> <ul> <li>Use a
        *   domain-specific attribute <li>Set `error.type` to capture all errors, regardless of whether they are defined
        *   within the domain-specific set or not. </ul>
        */
      val errorType: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("error.type"),
          List(
            "amqp:decode-error",
            "KAFKA_STORAGE_ERROR",
            "channel-error",
          ),
          Requirement.conditionallyRequired("If and only if the messaging operation has failed."),
          Stability.stable
        )

      /** The system-specific name of the messaging operation.
        */
      val messagingOperationName: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("messaging.operation.name"),
          List(
            "ack",
            "nack",
            "send",
          ),
          Requirement.required,
          Stability.experimental
        )

      /** Server domain name if available without reverse DNS lookup; otherwise, IP address or Unix domain socket name.
        * <p>
        * @note
        *   <p> Server domain name of the broker if available without reverse DNS lookup; otherwise, IP address or Unix
        *   domain socket name.
        */
      val serverAddress: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("server.address"),
          List(
            "example.com",
            "10.1.2.80",
            "/tmp/my.sock",
          ),
          Requirement.conditionallyRequired("If available."),
          Stability.stable
        )

      /** Server port number. <p>
        * @note
        *   <p> When observed from the client side, and when communicating through an intermediary, `server.port` SHOULD
        *   represent the server port behind any intermediaries, for example proxies, if it's available.
        */
      val serverPort: AttributeSpec[Long] =
        AttributeSpec(
          AttributeKey("server.port"),
          List(
            80,
            8080,
            443,
          ),
          Requirement.recommended,
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          errorType,
          messagingOperationName,
          serverAddress,
          serverPort,
        )
    }

    def create[F[_]: Meter](boundaries: BucketBoundaries): F[Histogram[F, Double]] =
      Meter[F]
        .histogram[Double](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

  /** Deprecated. Use `messaging.client.consumed.messages` instead.
    */
  @deprecated("Replaced by `messaging.client.consumed.messages`.", "")
  object ReceiveMessages {

    val Name = "messaging.receive.messages"
    val Description = "Deprecated. Use `messaging.client.consumed.messages` instead."
    val Unit = "{message}"

    object AttributeSpecs {

      /** Describes a class of error the operation ended with. <p>
        * @note
        *   <p> The `error.type` SHOULD be predictable, and SHOULD have low cardinality. <p> When `error.type` is set to
        *   a type (e.g., an exception type), its canonical class name identifying the type within the artifact SHOULD
        *   be used. <p> Instrumentations SHOULD document the list of errors they report. <p> The cardinality of
        *   `error.type` within one instrumentation library SHOULD be low. Telemetry consumers that aggregate data from
        *   multiple instrumentation libraries and applications should be prepared for `error.type` to have high
        *   cardinality at query time when no additional filters are applied. <p> If the operation has completed
        *   successfully, instrumentations SHOULD NOT set `error.type`. <p> If a specific domain defines its own set of
        *   error identifiers (such as HTTP or gRPC status codes), it's RECOMMENDED to: <p> <ul> <li>Use a
        *   domain-specific attribute <li>Set `error.type` to capture all errors, regardless of whether they are defined
        *   within the domain-specific set or not. </ul>
        */
      val errorType: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("error.type"),
          List(
            "amqp:decode-error",
            "KAFKA_STORAGE_ERROR",
            "channel-error",
          ),
          Requirement.conditionallyRequired("If and only if the messaging operation has failed."),
          Stability.stable
        )

      /** The system-specific name of the messaging operation.
        */
      val messagingOperationName: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("messaging.operation.name"),
          List(
            "ack",
            "nack",
            "send",
          ),
          Requirement.required,
          Stability.experimental
        )

      /** Server domain name if available without reverse DNS lookup; otherwise, IP address or Unix domain socket name.
        * <p>
        * @note
        *   <p> Server domain name of the broker if available without reverse DNS lookup; otherwise, IP address or Unix
        *   domain socket name.
        */
      val serverAddress: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("server.address"),
          List(
            "example.com",
            "10.1.2.80",
            "/tmp/my.sock",
          ),
          Requirement.conditionallyRequired("If available."),
          Stability.stable
        )

      /** Server port number. <p>
        * @note
        *   <p> When observed from the client side, and when communicating through an intermediary, `server.port` SHOULD
        *   represent the server port behind any intermediaries, for example proxies, if it's available.
        */
      val serverPort: AttributeSpec[Long] =
        AttributeSpec(
          AttributeKey("server.port"),
          List(
            80,
            8080,
            443,
          ),
          Requirement.recommended,
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          errorType,
          messagingOperationName,
          serverAddress,
          serverPort,
        )
    }

    def create[F[_]: Meter]: F[Counter[F, Long]] =
      Meter[F]
        .counter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

}
