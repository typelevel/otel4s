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

  /** Number of active client instances.
    */
  object CosmosdbClientActiveInstanceCount extends MetricSpec.Unsealed {

    val name: String = "azure.cosmosdb.client.active_instance.count"
    val description: String = "Number of active client instances."
    val unit: String = "{instance}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The database address specified in the client configuration.
        *
        * @note
        *   <p> Instrumentation SHOULD set `server.address` to the database address specified in the client
        *   configuration. It SHOULD NOT derive the value from the server selected for an operation. <blockquote>
        *   [!WARNING] <p> `server.address` MUST NOT contain credentials or other sensitive information.</blockquote>
        *   <p> See <a href="/docs/db/database-spans.md#database-server-address">Database server address</a> for
        *   guidance and examples.
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
        *   <p> `server.port` SHOULD reflect the database port specified in the client configuration. It SHOULD NOT be
        *   derived from the server selected for an operation.
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
            "If `server.address` is set and the client is configured with a single database server endpoint that uses a non-default port."
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

  /** <a href="https://learn.microsoft.com/azure/cosmos-db/request-units">Request units</a> consumed by the operation.
    */
  object CosmosdbClientOperationRequestCharge extends MetricSpec.Unsealed {

    val name: String = "azure.cosmosdb.client.operation.request_charge"
    val description: String =
      "[Request units](https://learn.microsoft.com/azure/cosmos-db/request-units) consumed by the operation."
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
        *   href="https://learn.microsoft.com/rest/api/resources/subscriptions/list-locations">Azure Location API</a>
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
          DbAttributes.DbCollectionName,
          List(
            "public.users",
            "customers",
          ),
          Requirement.conditionallyRequired("If available."),
          Stability.stable
        )

      /** The name of the database, fully qualified within the server address and port.
        */
      val dbNamespace: AttributeSpec[String] =
        AttributeSpec(
          DbAttributes.DbNamespace,
          List(
            "customers",
            "test.users",
          ),
          Requirement.conditionallyRequired("If available."),
          Stability.stable
        )

      /** The name of the operation or command being executed.
        *
        * @note
        *   <p> It is RECOMMENDED to capture the value as provided by the application without attempting to do any case
        *   normalization. <p> The operation name SHOULD NOT be extracted from `db.query.text`, when the database system
        *   supports query text with multiple operations in non-batch operations. <p> If spaces can occur in the
        *   operation name, multiple consecutive spaces SHOULD be normalized to a single space. <p> For batch
        *   operations, if the individual operations would all have the same `db.operation.name` when executed as
        *   non-batch operations, then that operation name SHOULD be used prepended by `BATCH `. Otherwise,
        *   `db.operation.name` SHOULD be `BATCH` or some other database system specific term if more applicable.
        */
      val dbOperationName: AttributeSpec[String] =
        AttributeSpec(
          DbAttributes.DbOperationName,
          List(
            "findAndModify",
            "HMSET",
            "SELECT",
          ),
          Requirement.required,
          Stability.stable
        )

      /** Database response status code.
        *
        * @note
        *   <p> The status code returned by the database. Usually it represents an error code, but may also represent
        *   partial success, warning, or differentiate between various types of successful outcomes. The value MAY use a
        *   standardized string representation of a numeric status code, if one exists, instead of the numeric code. The
        *   value MAY be a synthetic status provided by the client library rather than returned by the remote service.
        *   Semantic conventions for individual database systems SHOULD document what `db.response.status_code` means in
        *   the context of that system.
        */
      val dbResponseStatusCode: AttributeSpec[String] =
        AttributeSpec(
          DbAttributes.DbResponseStatusCode,
          List(
            "102",
            "ORA-17002",
            "08P01",
            "404",
          ),
          Requirement.conditionallyRequired("If the operation failed and status code is available."),
          Stability.stable
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

      /** The database address specified in the client configuration.
        *
        * @note
        *   <p> Instrumentation SHOULD set `server.address` to the database address specified in the client
        *   configuration. It SHOULD NOT derive the value from the server selected for an operation. <blockquote>
        *   [!WARNING] <p> `server.address` MUST NOT contain credentials or other sensitive information.</blockquote>
        *   <p> See <a href="/docs/db/database-spans.md#database-server-address">Database server address</a> for
        *   guidance and examples.
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
        *   <p> `server.port` SHOULD reflect the database port specified in the client configuration. It SHOULD NOT be
        *   derived from the server selected for an operation.
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
            "If `server.address` is set and the client is configured with a single database server endpoint that uses a non-default port."
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
