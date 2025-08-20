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
package metrics

import org.typelevel.otel4s.metrics._
import org.typelevel.otel4s.semconv.attributes._

// DO NOT EDIT, this is an Auto-generated file from buildscripts/templates/registry/otel4s/metrics/SemanticMetrics.scala.j2
object DbMetrics {

  val specs: List[MetricSpec] = List(
    ClientOperationDuration,
  )

  /** Duration of database client operations.
    *
    * @note
    *   <p> Batch operations SHOULD be recorded as a single operation.
    */
  object ClientOperationDuration extends MetricSpec.Unsealed {

    val name: String = "db.client.operation.duration"
    val description: String = "Duration of database client operations."
    val unit: String = "s"
    val stability: Stability = Stability.stable
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The name of a collection (table, container) within the database.
        *
        * @note
        *   <p> It is RECOMMENDED to capture the value as provided by the application without attempting to do any case
        *   normalization. <p> The collection name SHOULD NOT be extracted from `db.query.text`, when the database
        *   system supports query text with multiple collections in non-batch operations. <p> For batch operations, if
        *   the individual operations are known to have the same collection name then that collection name SHOULD be
        *   used.
        */
      val dbCollectionName: AttributeSpec[String] =
        AttributeSpec(
          DbAttributes.DbCollectionName,
          List(
            "public.users",
            "customers",
          ),
          Requirement.conditionallyRequired(
            "If readily available and if a database call is performed on a single collection."
          ),
          Stability.stable
        )

      /** The name of the database, fully qualified within the server address and port.
        *
        * @note
        *   <p> If a database system has multiple namespace components, they SHOULD be concatenated from the most
        *   general to the most specific namespace component, using `|` as a separator between the components. Any
        *   missing components (and their associated separators) SHOULD be omitted. Semantic conventions for individual
        *   database systems SHOULD document what `db.namespace` means in the context of that system. It is RECOMMENDED
        *   to capture the value as provided by the application without attempting to do any case normalization.
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
        *   operations, if the individual operations are known to have the same operation name then that operation name
        *   SHOULD be used prepended by `BATCH `, otherwise `db.operation.name` SHOULD be `BATCH` or some other database
        *   system specific term if more applicable.
        */
      val dbOperationName: AttributeSpec[String] =
        AttributeSpec(
          DbAttributes.DbOperationName,
          List(
            "findAndModify",
            "HMSET",
            "SELECT",
          ),
          Requirement.conditionallyRequired(
            "If readily available and if there is a single operation name that describes the database call."
          ),
          Stability.stable
        )

      /** Low cardinality summary of a database query.
        *
        * @note
        *   <p> The query summary describes a class of database queries and is useful as a grouping key, especially when
        *   analyzing telemetry for database calls involving complex queries. <p> Summary may be available to the
        *   instrumentation through instrumentation hooks or other means. If it is not available, instrumentations that
        *   support query parsing SHOULD generate a summary following <a
        *   href="/docs/database/database-spans.md#generating-a-summary-of-the-query">Generating query summary</a>
        *   section.
        */
      val dbQuerySummary: AttributeSpec[String] =
        AttributeSpec(
          DbAttributes.DbQuerySummary,
          List(
            "SELECT wuser_table",
            "INSERT shipping_details SELECT orders",
            "get user by id",
          ),
          Requirement.recommended(
            "if available through instrumentation hooks or if the instrumentation supports generating a query summary."
          ),
          Stability.stable
        )

      /** The database query being executed.
        *
        * @note
        *   <p> For sanitization see <a href="/docs/database/database-spans.md#sanitization-of-dbquerytext">Sanitization
        *   of `db.query.text`</a>. For batch operations, if the individual operations are known to have the same query
        *   text then that query text SHOULD be used, otherwise all of the individual query texts SHOULD be concatenated
        *   with separator `; ` or some other database system specific separator if more applicable. Parameterized query
        *   text SHOULD NOT be sanitized. Even though parameterized query text can potentially have sensitive data, by
        *   using a parameterized query the user is giving a strong signal that any sensitive data will be passed as
        *   parameter values, and the benefit to observability of capturing the static part of the query text by default
        *   outweighs the risk.
        */
      val dbQueryText: AttributeSpec[String] =
        AttributeSpec(
          DbAttributes.DbQueryText,
          List(
            "SELECT * FROM wuser_table where username = ?",
            "SET mykey ?",
          ),
          Requirement.optIn,
          Stability.stable
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

      /** The name of a stored procedure within the database.
        *
        * @note
        *   <p> It is RECOMMENDED to capture the value as provided by the application without attempting to do any case
        *   normalization. <p> For batch operations, if the individual operations are known to have the same stored
        *   procedure name then that stored procedure name SHOULD be used.
        */
      val dbStoredProcedureName: AttributeSpec[String] =
        AttributeSpec(
          DbAttributes.DbStoredProcedureName,
          List(
            "GetCustomer",
          ),
          Requirement.recommended("If operation applies to a specific stored procedure."),
          Stability.stable
        )

      /** The database management system (DBMS) product as identified by the client instrumentation.
        *
        * @note
        *   <p> The actual DBMS may differ from the one identified by the client. For example, when using PostgreSQL
        *   client libraries to connect to a CockroachDB, the `db.system.name` is set to `postgresql` based on the
        *   instrumentation's best knowledge.
        */
      val dbSystemName: AttributeSpec[String] =
        AttributeSpec(
          DbAttributes.DbSystemName,
          List(
          ),
          Requirement.required,
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

      /** Peer address of the database node where the operation was performed.
        *
        * @note
        *   <p> Semantic conventions for individual database systems SHOULD document whether `network.peer.*` attributes
        *   are applicable. Network peer address and port are useful when the application interacts with individual
        *   database nodes directly. If a database operation involved multiple network calls (for example retries), the
        *   address of the last contacted node SHOULD be used.
        */
      val networkPeerAddress: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkPeerAddress,
          List(
            "10.1.2.80",
            "/tmp/my.sock",
          ),
          Requirement.recommended("If applicable for this database system."),
          Stability.stable
        )

      /** Peer port number of the network connection.
        */
      val networkPeerPort: AttributeSpec[Long] =
        AttributeSpec(
          NetworkAttributes.NetworkPeerPort,
          List(
            65123,
          ),
          Requirement.recommended("If and only if `network.peer.address` is set."),
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
          dbCollectionName,
          dbNamespace,
          dbOperationName,
          dbQuerySummary,
          dbQueryText,
          dbResponseStatusCode,
          dbStoredProcedureName,
          dbSystemName,
          errorType,
          networkPeerAddress,
          networkPeerPort,
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
