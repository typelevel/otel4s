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
object AzureExperimentalAttributes {

  /** The unique identifier of the client instance.
    */
  val AzureClientId: AttributeKey[String] =
    AttributeKey("azure.client.id")

  /** Cosmos client connection mode.
    */
  val AzureCosmosdbConnectionMode: AttributeKey[String] =
    AttributeKey("azure.cosmosdb.connection.mode")

  /** Account or request <a href="https://learn.microsoft.com/azure/cosmos-db/consistency-levels">consistency level</a>.
    */
  val AzureCosmosdbConsistencyLevel: AttributeKey[String] =
    AttributeKey("azure.cosmosdb.consistency.level")

  /** List of regions contacted during operation in the order that they were contacted. If there is more than one region
    * listed, it indicates that the operation was performed on multiple regions i.e. cross-regional call.
    *
    * @note
    *   <p> Region name matches the format of `displayName` in <a
    *   href="https://learn.microsoft.com/rest/api/subscription/subscriptions/list-locations?view=rest-subscription-2021-10-01&tabs=HTTP#location">Azure
    *   Location API</a>
    */
  val AzureCosmosdbOperationContactedRegions: AttributeKey[Seq[String]] =
    AttributeKey("azure.cosmosdb.operation.contacted_regions")

  /** The number of request units consumed by the operation.
    */
  val AzureCosmosdbOperationRequestCharge: AttributeKey[Double] =
    AttributeKey("azure.cosmosdb.operation.request_charge")

  /** Request payload size in bytes.
    */
  val AzureCosmosdbRequestBodySize: AttributeKey[Long] =
    AttributeKey("azure.cosmosdb.request.body.size")

  /** Cosmos DB sub status code.
    */
  val AzureCosmosdbResponseSubStatusCode: AttributeKey[Long] =
    AttributeKey("azure.cosmosdb.response.sub_status_code")

  /** Values for [[AzureCosmosdbConnectionMode]].
    */
  abstract class AzureCosmosdbConnectionModeValue(val value: String)
  object AzureCosmosdbConnectionModeValue {

    /** Gateway (HTTP) connection.
      */
    case object Gateway extends AzureCosmosdbConnectionModeValue("gateway")

    /** Direct connection.
      */
    case object Direct extends AzureCosmosdbConnectionModeValue("direct")
  }

  /** Values for [[AzureCosmosdbConsistencyLevel]].
    */
  abstract class AzureCosmosdbConsistencyLevelValue(val value: String)
  object AzureCosmosdbConsistencyLevelValue {

    /** strong.
      */
    case object Strong extends AzureCosmosdbConsistencyLevelValue("Strong")

    /** bounded_staleness.
      */
    case object BoundedStaleness extends AzureCosmosdbConsistencyLevelValue("BoundedStaleness")

    /** session.
      */
    case object Session extends AzureCosmosdbConsistencyLevelValue("Session")

    /** eventual.
      */
    case object Eventual extends AzureCosmosdbConsistencyLevelValue("Eventual")

    /** consistent_prefix.
      */
    case object ConsistentPrefix extends AzureCosmosdbConsistencyLevelValue("ConsistentPrefix")
  }

}
