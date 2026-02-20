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
package attributes

// DO NOT EDIT, this is an Auto-generated file from buildscripts/templates/registry/otel4s/attributes/SemanticAttributes.scala.j2
object DbAttributes {

  /** The name of a collection (table, container) within the database.
    *
    * @note
    *   <p> It is RECOMMENDED to capture the value as provided by the application without attempting to do any case
    *   normalization. <p> The collection name SHOULD NOT be extracted from `db.query.text`, when the database system
    *   supports query text with multiple collections in non-batch operations. <p> For batch operations, if the
    *   individual operations are known to have the same collection name then that collection name SHOULD be used.
    */
  val DbCollectionName: AttributeKey[String] =
    AttributeKey("db.collection.name")

  /** The name of the database, fully qualified within the server address and port.
    *
    * @note
    *   <p> If a database system has multiple namespace components, they SHOULD be concatenated from the most general to
    *   the most specific namespace component, using `|` as a separator between the components. Any missing components
    *   (and their associated separators) SHOULD be omitted. Semantic conventions for individual database systems SHOULD
    *   document what `db.namespace` means in the context of that system. It is RECOMMENDED to capture the value as
    *   provided by the application without attempting to do any case normalization.
    */
  val DbNamespace: AttributeKey[String] =
    AttributeKey("db.namespace")

  /** The number of queries included in a batch operation.
    *
    * @note
    *   <p> Operations are only considered batches when they contain two or more operations, and so
    *   `db.operation.batch.size` SHOULD never be `1`.
    */
  val DbOperationBatchSize: AttributeKey[Long] =
    AttributeKey("db.operation.batch.size")

  /** The name of the operation or command being executed.
    *
    * @note
    *   <p> It is RECOMMENDED to capture the value as provided by the application without attempting to do any case
    *   normalization. <p> The operation name SHOULD NOT be extracted from `db.query.text`, when the database system
    *   supports query text with multiple operations in non-batch operations. <p> If spaces can occur in the operation
    *   name, multiple consecutive spaces SHOULD be normalized to a single space. <p> For batch operations, if the
    *   individual operations are known to have the same operation name then that operation name SHOULD be used
    *   prepended by `BATCH `, otherwise `db.operation.name` SHOULD be `BATCH` or some other database system specific
    *   term if more applicable.
    */
  val DbOperationName: AttributeKey[String] =
    AttributeKey("db.operation.name")

  /** Low cardinality summary of a database query.
    *
    * @note
    *   <p> The query summary describes a class of database queries and is useful as a grouping key, especially when
    *   analyzing telemetry for database calls involving complex queries. <p> Summary may be available to the
    *   instrumentation through instrumentation hooks or other means. If it is not available, instrumentations that
    *   support query parsing SHOULD generate a summary following <a
    *   href="/docs/db/database-spans.md#generating-a-summary-of-the-query">Generating query summary</a> section. <p>
    *   For batch operations, if the individual operations are known to have the same query summary then that query
    *   summary SHOULD be used prepended by `BATCH `, otherwise `db.query.summary` SHOULD be `BATCH` or some other
    *   database system specific term if more applicable.
    */
  val DbQuerySummary: AttributeKey[String] =
    AttributeKey("db.query.summary")

  /** The database query being executed.
    *
    * @note
    *   <p> For sanitization see <a href="/docs/db/database-spans.md#sanitization-of-dbquerytext">Sanitization of
    *   `db.query.text`</a>. For batch operations, if the individual operations are known to have the same query text
    *   then that query text SHOULD be used, otherwise all of the individual query texts SHOULD be concatenated with
    *   separator `; ` or some other database system specific separator if more applicable. Parameterized query text
    *   SHOULD NOT be sanitized. Even though parameterized query text can potentially have sensitive data, by using a
    *   parameterized query the user is giving a strong signal that any sensitive data will be passed as parameter
    *   values, and the benefit to observability of capturing the static part of the query text by default outweighs the
    *   risk.
    */
  val DbQueryText: AttributeKey[String] =
    AttributeKey("db.query.text")

  /** Database response status code.
    *
    * @note
    *   <p> The status code returned by the database. Usually it represents an error code, but may also represent
    *   partial success, warning, or differentiate between various types of successful outcomes. Semantic conventions
    *   for individual database systems SHOULD document what `db.response.status_code` means in the context of that
    *   system.
    */
  val DbResponseStatusCode: AttributeKey[String] =
    AttributeKey("db.response.status_code")

  /** The name of a stored procedure within the database.
    *
    * @note
    *   <p> It is RECOMMENDED to capture the value as provided by the application without attempting to do any case
    *   normalization. <p> For batch operations, if the individual operations are known to have the same stored
    *   procedure name then that stored procedure name SHOULD be used.
    */
  val DbStoredProcedureName: AttributeKey[String] =
    AttributeKey("db.stored_procedure.name")

  /** The database management system (DBMS) product as identified by the client instrumentation.
    *
    * @note
    *   <p> The actual DBMS may differ from the one identified by the client. For example, when using PostgreSQL client
    *   libraries to connect to a CockroachDB, the `db.system.name` is set to `postgresql` based on the
    *   instrumentation's best knowledge.
    */
  val DbSystemName: AttributeKey[String] =
    AttributeKey("db.system.name")

  /** Values for [[DbSystemName]].
    */
  abstract class DbSystemNameValue(val value: String)
  object DbSystemNameValue {
    implicit val attributeFromDbSystemNameValue: Attribute.From[DbSystemNameValue, String] = _.value

    /** <a href="https://mariadb.org/">MariaDB</a>
      */
    case object Mariadb extends DbSystemNameValue("mariadb")

    /** <a href="https://www.microsoft.com/sql-server">Microsoft SQL Server</a>
      */
    case object MicrosoftSqlServer extends DbSystemNameValue("microsoft.sql_server")

    /** <a href="https://www.mysql.com/">MySQL</a>
      */
    case object Mysql extends DbSystemNameValue("mysql")

    /** <a href="https://www.postgresql.org/">PostgreSQL</a>
      */
    case object Postgresql extends DbSystemNameValue("postgresql")
  }

}
