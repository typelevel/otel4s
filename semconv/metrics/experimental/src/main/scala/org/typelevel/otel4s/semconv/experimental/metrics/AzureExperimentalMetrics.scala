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

import cats.effect.Resource
import org.typelevel.otel4s.metrics._
import org.typelevel.otel4s.semconv.attributes._
import org.typelevel.otel4s.semconv.experimental.attributes._

// DO NOT EDIT, this is an Auto-generated file from buildscripts/templates/registry/otel4s/metrics/SemanticMetrics.scala.j2
object AzureExperimentalMetrics {

  val specs: List[MetricSpec] = List(
    CosmosdbClientActiveInstanceCount,
    CosmosdbClientOperationRequestCharge,
  )

  /** Number of active client instances
    */
  object CosmosdbClientActiveInstanceCount extends MetricSpec {

    val name: String = "azure.cosmosdb.client.active_instance.count"
    val description: String = "Number of active client instances"
    val unit: String = "{instance}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Name of the database host.
        *
        * @note
        *   <p> When observed from the client side, and when communicating through an intermediary, `server.address`
        *   SHOULD represent the server address behind any intermediaries, for example proxies, if it's available.
        */
      val serverAddress: AttributeSpec[String] =
        AttributeSpec(
          ServerAttributes.ServerAddress,
          List(
            "example.com",
            "10.1.2.80",
            "/tmp/my.sock",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** Server port number.
        *
        * @note
        *   <p> When observed from the client side, and when communicating through an intermediary, `server.port` SHOULD
        *   represent the server port behind any intermediaries, for example proxies, if it's available.
        */
      val serverPort: AttributeSpec[Long] =
        AttributeSpec(
          ServerAttributes.ServerPort,
          List(
            80,
            8080,
            443,
          ),
          Requirement.conditionallyRequired(
            "If using a port other than the default port for this DBMS and if `server.address` is set."
          ),
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          serverAddress,
          serverPort,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[UpDownCounter[F, A]] =
      Meter[F]
        .upDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableUpDownCounter] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** <a href="https://learn.microsoft.com/azure/cosmos-db/request-units">Request units</a> consumed by the operation
    */
  object CosmosdbClientOperationRequestCharge extends MetricSpec {

    val name: String = "azure.cosmosdb.client.operation.request_charge"
    val description: String =
      "[Request units](https://learn.microsoft.com/azure/cosmos-db/request-units) consumed by the operation"
    val unit: String = "{request_unit}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Account or request <a href="https://learn.microsoft.com/azure/cosmos-db/consistency-levels">consistency
        * level</a>.
        */
      val azureCosmosdbConsistencyLevel: AttributeSpec[String] =
        AttributeSpec(
          AzureExperimentalAttributes.AzureCosmosdbConsistencyLevel,
          List(
            "Eventual",
            "ConsistentPrefix",
            "BoundedStaleness",
            "Strong",
            "Session",
          ),
          Requirement.conditionallyRequired("If available."),
          Stability.development
        )

      /** List of regions contacted during operation in the order that they were contacted. If there is more than one
        * region listed, it indicates that the operation was performed on multiple regions i.e. cross-regional call.
        *
        * @note
        *   <p> Region name matches the format of `displayName` in <a
        *   href="https://learn.microsoft.com/rest/api/subscription/subscriptions/list-locations?view=rest-subscription-2021-10-01&tabs=HTTP#location">Azure
        *   Location API</a>
        */
      val azureCosmosdbOperationContactedRegions: AttributeSpec[Seq[String]] =
        AttributeSpec(
          AzureExperimentalAttributes.AzureCosmosdbOperationContactedRegions,
          List(
            Seq("North Central US", "Australia East", "Australia Southeast"),
          ),
          Requirement.recommended("If available"),
          Stability.development
        )

      /** Cosmos DB sub status code.
        */
      val azureCosmosdbResponseSubStatusCode: AttributeSpec[Long] =
        AttributeSpec(
          AzureExperimentalAttributes.AzureCosmosdbResponseSubStatusCode,
          List(
            1000,
            1002,
          ),
          Requirement.conditionallyRequired("when response was received and contained sub-code."),
          Stability.development
        )

      /** Cosmos DB container name.
        *
        * @note
        *   <p> It is RECOMMENDED to capture the value as provided by the application without attempting to do any case
        *   normalization.
        */
      val dbCollectionName: AttributeSpec[String] =
        AttributeSpec(
          DbExperimentalAttributes.DbCollectionName,
          List(
            "public.users",
            "customers",
          ),
          Requirement.conditionallyRequired("If available."),
          Stability.releaseCandidate
        )

      /** The name of the database, fully qualified within the server address and port.
        */
      val dbNamespace: AttributeSpec[String] =
        AttributeSpec(
          DbExperimentalAttributes.DbNamespace,
          List(
            "customers",
            "test.users",
          ),
          Requirement.conditionallyRequired("If available."),
          Stability.releaseCandidate
        )

      /** The name of the operation or command being executed.
        *
        * @note
        *   <p> It is RECOMMENDED to capture the value as provided by the application without attempting to do any case
        *   normalization. <p> The operation name SHOULD NOT be extracted from `db.query.text`, when the database system
        *   supports cross-table queries in non-batch operations. <p> If spaces can occur in the operation name,
        *   multiple consecutive spaces SHOULD be normalized to a single space. <p> For batch operations, if the
        *   individual operations are known to have the same operation name then that operation name SHOULD be used
        *   prepended by `BATCH `, otherwise `db.operation.name` SHOULD be `BATCH` or some other database system
        *   specific term if more applicable.
        */
      val dbOperationName: AttributeSpec[String] =
        AttributeSpec(
          DbExperimentalAttributes.DbOperationName,
          List(
            "findAndModify",
            "HMSET",
            "SELECT",
          ),
          Requirement.conditionallyRequired("If readily available."),
          Stability.releaseCandidate
        )

      /** Database response status code.
        *
        * @note
        *   <p> The status code returned by the database. Usually it represents an error code, but may also represent
        *   partial success, warning, or differentiate between various types of successful outcomes. Semantic
        *   conventions for individual database systems SHOULD document what `db.response.status_code` means in the
        *   context of that system.
        */
      val dbResponseStatusCode: AttributeSpec[String] =
        AttributeSpec(
          DbExperimentalAttributes.DbResponseStatusCode,
          List(
            "102",
            "ORA-17002",
            "08P01",
            "404",
          ),
          Requirement.conditionallyRequired("If the operation failed and status code is available."),
          Stability.releaseCandidate
        )

      /** Describes a class of error the operation ended with.
        *
        * @note
        *   <p> The `error.type` SHOULD match the `db.response.status_code` returned by the database or the client
        *   library, or the canonical name of exception that occurred. When using canonical exception type name,
        *   instrumentation SHOULD do the best effort to report the most relevant type. For example, if the original
        *   exception is wrapped into a generic one, the original exception SHOULD be preferred. Instrumentations SHOULD
        *   document how `error.type` is populated.
        */
      val errorType: AttributeSpec[String] =
        AttributeSpec(
          ErrorAttributes.ErrorType,
          List(
            "timeout",
            "java.net.UnknownHostException",
            "server_certificate_invalid",
            "500",
          ),
          Requirement.conditionallyRequired("If and only if the operation failed."),
          Stability.stable
        )

      /** Name of the database host.
        *
        * @note
        *   <p> When observed from the client side, and when communicating through an intermediary, `server.address`
        *   SHOULD represent the server address behind any intermediaries, for example proxies, if it's available.
        */
      val serverAddress: AttributeSpec[String] =
        AttributeSpec(
          ServerAttributes.ServerAddress,
          List(
            "example.com",
            "10.1.2.80",
            "/tmp/my.sock",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** Server port number.
        *
        * @note
        *   <p> When observed from the client side, and when communicating through an intermediary, `server.port` SHOULD
        *   represent the server port behind any intermediaries, for example proxies, if it's available.
        */
      val serverPort: AttributeSpec[Long] =
        AttributeSpec(
          ServerAttributes.ServerPort,
          List(
            80,
            8080,
            443,
          ),
          Requirement.conditionallyRequired(
            "If using a port other than the default port for this DBMS and if `server.address` is set."
          ),
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          azureCosmosdbConsistencyLevel,
          azureCosmosdbOperationContactedRegions,
          azureCosmosdbResponseSubStatusCode,
          dbCollectionName,
          dbNamespace,
          dbOperationName,
          dbResponseStatusCode,
          errorType,
          serverAddress,
          serverPort,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue](boundaries: BucketBoundaries): F[Histogram[F, A]] =
      Meter[F]
        .histogram[A](name)
        .withDescription(description)
        .withUnit(unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

}
