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
object RocketmqExperimentalAttributes {

  /** Model of message consumption. This only applies to consumer spans.
    */
  val RocketmqConsumptionModel: AttributeKey[String] =
    AttributeKey("rocketmq.consumption_model")

  /** The delay time level for delay message, which determines the message delay time.
    */
  val RocketmqMessageDelayTimeLevel: AttributeKey[Long] =
    AttributeKey("rocketmq.message.delay_time_level")

  /** The timestamp in milliseconds that the delay message is expected to be delivered to consumer.
    */
  val RocketmqMessageDeliveryTimestamp: AttributeKey[Long] =
    AttributeKey("rocketmq.message.delivery_timestamp")

  /** It is essential for FIFO message. Messages that belong to the same message group are always processed one by one
    * within the same consumer group.
    */
  val RocketmqMessageGroup: AttributeKey[String] =
    AttributeKey("rocketmq.message.group")

  /** Key(s) of message, another way to mark message besides message ID.
    */
  val RocketmqMessageKeys: AttributeKey[Seq[String]] =
    AttributeKey("rocketmq.message.keys")

  /** The secondary classifier of message besides topic.
    */
  val RocketmqMessageTag: AttributeKey[String] =
    AttributeKey("rocketmq.message.tag")

  /** Type of message.
    */
  val RocketmqMessageType: AttributeKey[String] =
    AttributeKey("rocketmq.message.type")

  /** Namespace of RocketMQ resources, resources in different namespaces are individual.
    */
  val RocketmqNamespace: AttributeKey[String] =
    AttributeKey("rocketmq.namespace")

  /** Values for [[RocketmqConsumptionModel]].
    */
  abstract class RocketmqConsumptionModelValue(val value: String)
  object RocketmqConsumptionModelValue {
    implicit val attributeFromRocketmqConsumptionModelValue: Attribute.From[RocketmqConsumptionModelValue, String] =
      _.value

    /** Clustering consumption model
      */
    case object Clustering extends RocketmqConsumptionModelValue("clustering")

    /** Broadcasting consumption model
      */
    case object Broadcasting extends RocketmqConsumptionModelValue("broadcasting")
  }

  /** Values for [[RocketmqMessageType]].
    */
  abstract class RocketmqMessageTypeValue(val value: String)
  object RocketmqMessageTypeValue {
    implicit val attributeFromRocketmqMessageTypeValue: Attribute.From[RocketmqMessageTypeValue, String] = _.value

    /** Normal message
      */
    case object Normal extends RocketmqMessageTypeValue("normal")

    /** FIFO message
      */
    case object Fifo extends RocketmqMessageTypeValue("fifo")

    /** Delay message
      */
    case object Delay extends RocketmqMessageTypeValue("delay")

    /** Transaction message
      */
    case object Transaction extends RocketmqMessageTypeValue("transaction")
  }

}
