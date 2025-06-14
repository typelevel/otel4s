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
object DbExperimentalAttributes {

  /** Deprecated, use `cassandra.consistency.level` instead.
    */
  @deprecated("Replaced by `cassandra.consistency.level`.", "")
  val DbCassandraConsistencyLevel: AttributeKey[String] =
    AttributeKey("db.cassandra.consistency_level")

  /** Deprecated, use `cassandra.coordinator.dc` instead.
    */
  @deprecated("Replaced by `cassandra.coordinator.dc`.", "")
  val DbCassandraCoordinatorDc: AttributeKey[String] =
    AttributeKey("db.cassandra.coordinator.dc")

  /** Deprecated, use `cassandra.coordinator.id` instead.
    */
  @deprecated("Replaced by `cassandra.coordinator.id`.", "")
  val DbCassandraCoordinatorId: AttributeKey[String] =
    AttributeKey("db.cassandra.coordinator.id")

  /** Deprecated, use `cassandra.query.idempotent` instead.
    */
  @deprecated("Replaced by `cassandra.query.idempotent`.", "")
  val DbCassandraIdempotence: AttributeKey[Boolean] =
    AttributeKey("db.cassandra.idempotence")

  /** Deprecated, use `cassandra.page.size` instead.
    */
  @deprecated("Replaced by `cassandra.page.size`.", "")
  val DbCassandraPageSize: AttributeKey[Long] =
    AttributeKey("db.cassandra.page_size")

  /** Deprecated, use `cassandra.speculative_execution.count` instead.
    */
  @deprecated("Replaced by `cassandra.speculative_execution.count`.", "")
  val DbCassandraSpeculativeExecutionCount: AttributeKey[Long] =
    AttributeKey("db.cassandra.speculative_execution_count")

  /** Deprecated, use `db.collection.name` instead.
    */
  @deprecated("Replaced by `db.collection.name`.", "")
  val DbCassandraTable: AttributeKey[String] =
    AttributeKey("db.cassandra.table")

  /** The name of the connection pool; unique within the instrumented application. In case the connection pool
    * implementation doesn't provide a name, instrumentation SHOULD use a combination of parameters that would make the
    * name unique, for example, combining attributes `server.address`, `server.port`, and `db.namespace`, formatted as
    * `server.address:server.port/db.namespace`. Instrumentations that generate connection pool name following different
    * patterns SHOULD document it.
    */
  val DbClientConnectionPoolName: AttributeKey[String] =
    AttributeKey("db.client.connection.pool.name")

  /** The state of a connection in the pool
    */
  val DbClientConnectionState: AttributeKey[String] =
    AttributeKey("db.client.connection.state")

  /** Deprecated, use `db.client.connection.pool.name` instead.
    */
  @deprecated("Replaced by `db.client.connection.pool.name`.", "")
  val DbClientConnectionsPoolName: AttributeKey[String] =
    AttributeKey("db.client.connections.pool.name")

  /** Deprecated, use `db.client.connection.state` instead.
    */
  @deprecated("Replaced by `db.client.connection.state`.", "")
  val DbClientConnectionsState: AttributeKey[String] =
    AttributeKey("db.client.connections.state")

  /** The name of a collection (table, container) within the database.
    *
    * @note
    *   <p> It is RECOMMENDED to capture the value as provided by the application without attempting to do any case
    *   normalization. <p> The collection name SHOULD NOT be extracted from `db.query.text`, when the database system
    *   supports query text with multiple collections in non-batch operations. <p> For batch operations, if the
    *   individual operations are known to have the same collection name then that collection name SHOULD be used.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.DbAttributes.DbCollectionName` instead.",
    ""
  )
  val DbCollectionName: AttributeKey[String] =
    AttributeKey("db.collection.name")

  /** Deprecated, use `server.address`, `server.port` attributes instead.
    */
  @deprecated("Replaced by `server.address` and `server.port`.", "")
  val DbConnectionString: AttributeKey[String] =
    AttributeKey("db.connection_string")

  /** Deprecated, use `azure.client.id` instead.
    */
  @deprecated("Replaced by `azure.client.id`.", "")
  val DbCosmosdbClientId: AttributeKey[String] =
    AttributeKey("db.cosmosdb.client_id")

  /** Deprecated, use `azure.cosmosdb.connection.mode` instead.
    */
  @deprecated("Replaced by `azure.cosmosdb.connection.mode`.", "")
  val DbCosmosdbConnectionMode: AttributeKey[String] =
    AttributeKey("db.cosmosdb.connection_mode")

  /** Deprecated, use `cosmosdb.consistency.level` instead.
    */
  @deprecated("Replaced by `azure.cosmosdb.consistency.level`.", "")
  val DbCosmosdbConsistencyLevel: AttributeKey[String] =
    AttributeKey("db.cosmosdb.consistency_level")

  /** Deprecated, use `db.collection.name` instead.
    */
  @deprecated("Replaced by `db.collection.name`.", "")
  val DbCosmosdbContainer: AttributeKey[String] =
    AttributeKey("db.cosmosdb.container")

  /** Deprecated, no replacement at this time.
    */
  @deprecated("Removed, no replacement at this time.", "")
  val DbCosmosdbOperationType: AttributeKey[String] =
    AttributeKey("db.cosmosdb.operation_type")

  /** Deprecated, use `azure.cosmosdb.operation.contacted_regions` instead.
    */
  @deprecated("Replaced by `azure.cosmosdb.operation.contacted_regions`.", "")
  val DbCosmosdbRegionsContacted: AttributeKey[Seq[String]] =
    AttributeKey("db.cosmosdb.regions_contacted")

  /** Deprecated, use `azure.cosmosdb.operation.request_charge` instead.
    */
  @deprecated("Replaced by `azure.cosmosdb.operation.request_charge`.", "")
  val DbCosmosdbRequestCharge: AttributeKey[Double] =
    AttributeKey("db.cosmosdb.request_charge")

  /** Deprecated, use `azure.cosmosdb.request.body.size` instead.
    */
  @deprecated("Replaced by `azure.cosmosdb.request.body.size`.", "")
  val DbCosmosdbRequestContentLength: AttributeKey[Long] =
    AttributeKey("db.cosmosdb.request_content_length")

  /** Deprecated, use `db.response.status_code` instead.
    */
  @deprecated("Replaced by `db.response.status_code`.", "")
  val DbCosmosdbStatusCode: AttributeKey[Long] =
    AttributeKey("db.cosmosdb.status_code")

  /** Deprecated, use `azure.cosmosdb.response.sub_status_code` instead.
    */
  @deprecated("Replaced by `azure.cosmosdb.response.sub_status_code`.", "")
  val DbCosmosdbSubStatusCode: AttributeKey[Long] =
    AttributeKey("db.cosmosdb.sub_status_code")

  /** Deprecated, use `db.namespace` instead.
    */
  @deprecated("Replaced by `db.namespace`.", "")
  val DbElasticsearchClusterName: AttributeKey[String] =
    AttributeKey("db.elasticsearch.cluster.name")

  /** Deprecated, use `elasticsearch.node.name` instead.
    */
  @deprecated("Replaced by `elasticsearch.node.name`.", "")
  val DbElasticsearchNodeName: AttributeKey[String] =
    AttributeKey("db.elasticsearch.node.name")

  /** Deprecated, use `db.operation.parameter` instead.
    */
  @deprecated("Replaced by `db.operation.parameter`.", "")
  val DbElasticsearchPathParts: AttributeKey[String] =
    AttributeKey("db.elasticsearch.path_parts")

  /** Deprecated, no general replacement at this time. For Elasticsearch, use `db.elasticsearch.node.name` instead.
    */
  @deprecated(
    "Removed, no general replacement at this time. For Elasticsearch, use `db.elasticsearch.node.name` instead.",
    ""
  )
  val DbInstanceId: AttributeKey[String] =
    AttributeKey("db.instance.id")

  /** Removed, no replacement at this time.
    */
  @deprecated("Removed, no replacement at this time.", "")
  val DbJdbcDriverClassname: AttributeKey[String] =
    AttributeKey("db.jdbc.driver_classname")

  /** Deprecated, use `db.collection.name` instead.
    */
  @deprecated("Replaced by `db.collection.name`.", "")
  val DbMongodbCollection: AttributeKey[String] =
    AttributeKey("db.mongodb.collection")

  /** Deprecated, SQL Server instance is now populated as a part of `db.namespace` attribute.
    */
  @deprecated("Removed, no replacement at this time.", "")
  val DbMssqlInstanceName: AttributeKey[String] =
    AttributeKey("db.mssql.instance_name")

  /** Deprecated, use `db.namespace` instead.
    */
  @deprecated("Replaced by `db.namespace`.", "")
  val DbName: AttributeKey[String] =
    AttributeKey("db.name")

  /** The name of the database, fully qualified within the server address and port.
    *
    * @note
    *   <p> If a database system has multiple namespace components, they SHOULD be concatenated from the most general to
    *   the most specific namespace component, using `|` as a separator between the components. Any missing components
    *   (and their associated separators) SHOULD be omitted. Semantic conventions for individual database systems SHOULD
    *   document what `db.namespace` means in the context of that system. It is RECOMMENDED to capture the value as
    *   provided by the application without attempting to do any case normalization.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.DbAttributes.DbNamespace` instead.",
    ""
  )
  val DbNamespace: AttributeKey[String] =
    AttributeKey("db.namespace")

  /** Deprecated, use `db.operation.name` instead.
    */
  @deprecated("Replaced by `db.operation.name`.", "")
  val DbOperation: AttributeKey[String] =
    AttributeKey("db.operation")

  /** The number of queries included in a batch operation.
    *
    * @note
    *   <p> Operations are only considered batches when they contain two or more operations, and so
    *   `db.operation.batch.size` SHOULD never be `1`.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.DbAttributes.DbOperationBatchSize` instead.",
    ""
  )
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
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.DbAttributes.DbOperationName` instead.",
    ""
  )
  val DbOperationName: AttributeKey[String] =
    AttributeKey("db.operation.name")

  /** A database operation parameter, with `<key>` being the parameter name, and the attribute value being a string
    * representation of the parameter value.
    *
    * @note
    *   <p> For example, a client-side maximum number of rows to read from the database MAY be recorded as the
    *   `db.operation.parameter.max_rows` attribute. <p> `db.query.text` parameters SHOULD be captured using
    *   `db.query.parameter.<key>` instead of `db.operation.parameter.<key>`.
    */
  val DbOperationParameter: AttributeKey[String] =
    AttributeKey("db.operation.parameter")

  /** A database query parameter, with `<key>` being the parameter name, and the attribute value being a string
    * representation of the parameter value.
    *
    * @note
    *   <p> If a query parameter has no name and instead is referenced only by index, then `<key>` SHOULD be the 0-based
    *   index. <p> `db.query.parameter.<key>` SHOULD match up with the parameterized placeholders present in
    *   `db.query.text`. <p> `db.query.parameter.<key>` SHOULD NOT be captured on batch operations. <p> Examples: <ul>
    *   <li>For a query `SELECT * FROM users where username =  %s` with the parameter `"jdoe"`, the attribute
    *   `db.query.parameter.0` SHOULD be set to `"jdoe"`. <li>For a query
    *   `"SELECT * FROM users WHERE username = %(username)s;` with parameter `username = "jdoe"`, the attribute
    *   `db.query.parameter.username` SHOULD be set to `"jdoe"`. </ul>
    */
  val DbQueryParameter: AttributeKey[String] =
    AttributeKey("db.query.parameter")

  /** Low cardinality summary of a database query.
    *
    * @note
    *   <p> The query summary describes a class of database queries and is useful as a grouping key, especially when
    *   analyzing telemetry for database calls involving complex queries. <p> Summary may be available to the
    *   instrumentation through instrumentation hooks or other means. If it is not available, instrumentations that
    *   support query parsing SHOULD generate a summary following <a
    *   href="/docs/database/database-spans.md#generating-a-summary-of-the-query">Generating query summary</a> section.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.DbAttributes.DbQuerySummary` instead.",
    ""
  )
  val DbQuerySummary: AttributeKey[String] =
    AttributeKey("db.query.summary")

  /** The database query being executed.
    *
    * @note
    *   <p> For sanitization see <a href="/docs/database/database-spans.md#sanitization-of-dbquerytext">Sanitization of
    *   `db.query.text`</a>. For batch operations, if the individual operations are known to have the same query text
    *   then that query text SHOULD be used, otherwise all of the individual query texts SHOULD be concatenated with
    *   separator `; ` or some other database system specific separator if more applicable. Parameterized query text
    *   SHOULD NOT be sanitized. Even though parameterized query text can potentially have sensitive data, by using a
    *   parameterized query the user is giving a strong signal that any sensitive data will be passed as parameter
    *   values, and the benefit to observability of capturing the static part of the query text by default outweighs the
    *   risk.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.DbAttributes.DbQueryText` instead.",
    ""
  )
  val DbQueryText: AttributeKey[String] =
    AttributeKey("db.query.text")

  /** Deprecated, use `db.namespace` instead.
    */
  @deprecated("Replaced by `db.namespace`.", "")
  val DbRedisDatabaseIndex: AttributeKey[Long] =
    AttributeKey("db.redis.database_index")

  /** Number of rows returned by the operation.
    */
  val DbResponseReturnedRows: AttributeKey[Long] =
    AttributeKey("db.response.returned_rows")

  /** Database response status code.
    *
    * @note
    *   <p> The status code returned by the database. Usually it represents an error code, but may also represent
    *   partial success, warning, or differentiate between various types of successful outcomes. Semantic conventions
    *   for individual database systems SHOULD document what `db.response.status_code` means in the context of that
    *   system.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.DbAttributes.DbResponseStatusCode` instead.",
    ""
  )
  val DbResponseStatusCode: AttributeKey[String] =
    AttributeKey("db.response.status_code")

  /** Deprecated, use `db.collection.name` instead.
    */
  @deprecated("Replaced by `db.collection.name`, but only if not extracting the value from `db.query.text`.", "")
  val DbSqlTable: AttributeKey[String] =
    AttributeKey("db.sql.table")

  /** The database statement being executed.
    */
  @deprecated("Replaced by `db.query.text`.", "")
  val DbStatement: AttributeKey[String] =
    AttributeKey("db.statement")

  /** The name of a stored procedure within the database.
    *
    * @note
    *   <p> It is RECOMMENDED to capture the value as provided by the application without attempting to do any case
    *   normalization. <p> For batch operations, if the individual operations are known to have the same stored
    *   procedure name then that stored procedure name SHOULD be used.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.DbAttributes.DbStoredProcedureName` instead.",
    ""
  )
  val DbStoredProcedureName: AttributeKey[String] =
    AttributeKey("db.stored_procedure.name")

  /** Deprecated, use `db.system.name` instead.
    */
  @deprecated("Replaced by `db.system.name`.", "")
  val DbSystem: AttributeKey[String] =
    AttributeKey("db.system")

  /** The database management system (DBMS) product as identified by the client instrumentation.
    *
    * @note
    *   <p> The actual DBMS may differ from the one identified by the client. For example, when using PostgreSQL client
    *   libraries to connect to a CockroachDB, the `db.system.name` is set to `postgresql` based on the
    *   instrumentation's best knowledge.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.DbAttributes.DbSystemName` instead.",
    ""
  )
  val DbSystemName: AttributeKey[String] =
    AttributeKey("db.system.name")

  /** Deprecated, no replacement at this time.
    */
  @deprecated("Removed, no replacement at this time.", "")
  val DbUser: AttributeKey[String] =
    AttributeKey("db.user")

  /** Values for [[DbCassandraConsistencyLevel]].
    */
  @deprecated("Replaced by `cassandra.consistency.level`.", "")
  abstract class DbCassandraConsistencyLevelValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object DbCassandraConsistencyLevelValue {

    /** all.
      */
    case object All extends DbCassandraConsistencyLevelValue("all")

    /** each_quorum.
      */
    case object EachQuorum extends DbCassandraConsistencyLevelValue("each_quorum")

    /** quorum.
      */
    case object Quorum extends DbCassandraConsistencyLevelValue("quorum")

    /** local_quorum.
      */
    case object LocalQuorum extends DbCassandraConsistencyLevelValue("local_quorum")

    /** one.
      */
    case object One extends DbCassandraConsistencyLevelValue("one")

    /** two.
      */
    case object Two extends DbCassandraConsistencyLevelValue("two")

    /** three.
      */
    case object Three extends DbCassandraConsistencyLevelValue("three")

    /** local_one.
      */
    case object LocalOne extends DbCassandraConsistencyLevelValue("local_one")

    /** any.
      */
    case object Any extends DbCassandraConsistencyLevelValue("any")

    /** serial.
      */
    case object Serial extends DbCassandraConsistencyLevelValue("serial")

    /** local_serial.
      */
    case object LocalSerial extends DbCassandraConsistencyLevelValue("local_serial")
  }

  /** Values for [[DbClientConnectionState]].
    */
  abstract class DbClientConnectionStateValue(val value: String)
  object DbClientConnectionStateValue {

    /** idle.
      */
    case object Idle extends DbClientConnectionStateValue("idle")

    /** used.
      */
    case object Used extends DbClientConnectionStateValue("used")
  }

  /** Values for [[DbClientConnectionsState]].
    */
  @deprecated("Replaced by `db.client.connection.state`.", "")
  abstract class DbClientConnectionsStateValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object DbClientConnectionsStateValue {

    /** idle.
      */
    case object Idle extends DbClientConnectionsStateValue("idle")

    /** used.
      */
    case object Used extends DbClientConnectionsStateValue("used")
  }

  /** Values for [[DbCosmosdbConnectionMode]].
    */
  @deprecated("Replaced by `azure.cosmosdb.connection.mode`.", "")
  abstract class DbCosmosdbConnectionModeValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object DbCosmosdbConnectionModeValue {

    /** Gateway (HTTP) connection.
      */
    case object Gateway extends DbCosmosdbConnectionModeValue("gateway")

    /** Direct connection.
      */
    case object Direct extends DbCosmosdbConnectionModeValue("direct")
  }

  /** Values for [[DbCosmosdbConsistencyLevel]].
    */
  @deprecated("Replaced by `azure.cosmosdb.consistency.level`.", "")
  abstract class DbCosmosdbConsistencyLevelValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object DbCosmosdbConsistencyLevelValue {

    /** strong.
      */
    case object Strong extends DbCosmosdbConsistencyLevelValue("Strong")

    /** bounded_staleness.
      */
    case object BoundedStaleness extends DbCosmosdbConsistencyLevelValue("BoundedStaleness")

    /** session.
      */
    case object Session extends DbCosmosdbConsistencyLevelValue("Session")

    /** eventual.
      */
    case object Eventual extends DbCosmosdbConsistencyLevelValue("Eventual")

    /** consistent_prefix.
      */
    case object ConsistentPrefix extends DbCosmosdbConsistencyLevelValue("ConsistentPrefix")
  }

  /** Values for [[DbCosmosdbOperationType]].
    */
  @deprecated("Removed, no replacement at this time.", "")
  abstract class DbCosmosdbOperationTypeValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object DbCosmosdbOperationTypeValue {

    /** batch.
      */
    case object Batch extends DbCosmosdbOperationTypeValue("batch")

    /** create.
      */
    case object Create extends DbCosmosdbOperationTypeValue("create")

    /** delete.
      */
    case object Delete extends DbCosmosdbOperationTypeValue("delete")

    /** execute.
      */
    case object Execute extends DbCosmosdbOperationTypeValue("execute")

    /** execute_javascript.
      */
    case object ExecuteJavascript extends DbCosmosdbOperationTypeValue("execute_javascript")

    /** invalid.
      */
    case object Invalid extends DbCosmosdbOperationTypeValue("invalid")

    /** head.
      */
    case object Head extends DbCosmosdbOperationTypeValue("head")

    /** head_feed.
      */
    case object HeadFeed extends DbCosmosdbOperationTypeValue("head_feed")

    /** patch.
      */
    case object Patch extends DbCosmosdbOperationTypeValue("patch")

    /** query.
      */
    case object Query extends DbCosmosdbOperationTypeValue("query")

    /** query_plan.
      */
    case object QueryPlan extends DbCosmosdbOperationTypeValue("query_plan")

    /** read.
      */
    case object Read extends DbCosmosdbOperationTypeValue("read")

    /** read_feed.
      */
    case object ReadFeed extends DbCosmosdbOperationTypeValue("read_feed")

    /** replace.
      */
    case object Replace extends DbCosmosdbOperationTypeValue("replace")

    /** upsert.
      */
    case object Upsert extends DbCosmosdbOperationTypeValue("upsert")
  }

  /** Values for [[DbSystem]].
    */
  @deprecated("Replaced by `db.system.name`.", "")
  abstract class DbSystemValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object DbSystemValue {

    /** Some other SQL database. Fallback only. See notes.
      */
    case object OtherSql extends DbSystemValue("other_sql")

    /** Adabas (Adaptable Database System)
      */
    case object Adabas extends DbSystemValue("adabas")

    /** Deprecated, use `intersystems_cache` instead.
      */
    case object Cache extends DbSystemValue("cache")

    /** InterSystems Caché
      */
    case object IntersystemsCache extends DbSystemValue("intersystems_cache")

    /** Apache Cassandra
      */
    case object Cassandra extends DbSystemValue("cassandra")

    /** ClickHouse
      */
    case object Clickhouse extends DbSystemValue("clickhouse")

    /** Deprecated, use `other_sql` instead.
      */
    case object Cloudscape extends DbSystemValue("cloudscape")

    /** CockroachDB
      */
    case object Cockroachdb extends DbSystemValue("cockroachdb")

    /** Deprecated, no replacement at this time.
      */
    case object Coldfusion extends DbSystemValue("coldfusion")

    /** Microsoft Azure Cosmos DB
      */
    case object Cosmosdb extends DbSystemValue("cosmosdb")

    /** Couchbase
      */
    case object Couchbase extends DbSystemValue("couchbase")

    /** CouchDB
      */
    case object Couchdb extends DbSystemValue("couchdb")

    /** IBM Db2
      */
    case object Db2 extends DbSystemValue("db2")

    /** Apache Derby
      */
    case object Derby extends DbSystemValue("derby")

    /** Amazon DynamoDB
      */
    case object Dynamodb extends DbSystemValue("dynamodb")

    /** EnterpriseDB
      */
    case object Edb extends DbSystemValue("edb")

    /** Elasticsearch
      */
    case object Elasticsearch extends DbSystemValue("elasticsearch")

    /** FileMaker
      */
    case object Filemaker extends DbSystemValue("filemaker")

    /** Firebird
      */
    case object Firebird extends DbSystemValue("firebird")

    /** Deprecated, use `other_sql` instead.
      */
    case object Firstsql extends DbSystemValue("firstsql")

    /** Apache Geode
      */
    case object Geode extends DbSystemValue("geode")

    /** H2
      */
    case object H2 extends DbSystemValue("h2")

    /** SAP HANA
      */
    case object Hanadb extends DbSystemValue("hanadb")

    /** Apache HBase
      */
    case object Hbase extends DbSystemValue("hbase")

    /** Apache Hive
      */
    case object Hive extends DbSystemValue("hive")

    /** HyperSQL DataBase
      */
    case object Hsqldb extends DbSystemValue("hsqldb")

    /** InfluxDB
      */
    case object Influxdb extends DbSystemValue("influxdb")

    /** Informix
      */
    case object Informix extends DbSystemValue("informix")

    /** Ingres
      */
    case object Ingres extends DbSystemValue("ingres")

    /** InstantDB
      */
    case object Instantdb extends DbSystemValue("instantdb")

    /** InterBase
      */
    case object Interbase extends DbSystemValue("interbase")

    /** MariaDB
      */
    case object Mariadb extends DbSystemValue("mariadb")

    /** SAP MaxDB
      */
    case object Maxdb extends DbSystemValue("maxdb")

    /** Memcached
      */
    case object Memcached extends DbSystemValue("memcached")

    /** MongoDB
      */
    case object Mongodb extends DbSystemValue("mongodb")

    /** Microsoft SQL Server
      */
    case object Mssql extends DbSystemValue("mssql")

    /** Deprecated, Microsoft SQL Server Compact is discontinued.
      */
    case object Mssqlcompact extends DbSystemValue("mssqlcompact")

    /** MySQL
      */
    case object Mysql extends DbSystemValue("mysql")

    /** Neo4j
      */
    case object Neo4j extends DbSystemValue("neo4j")

    /** Netezza
      */
    case object Netezza extends DbSystemValue("netezza")

    /** OpenSearch
      */
    case object Opensearch extends DbSystemValue("opensearch")

    /** Oracle Database
      */
    case object Oracle extends DbSystemValue("oracle")

    /** Pervasive PSQL
      */
    case object Pervasive extends DbSystemValue("pervasive")

    /** PointBase
      */
    case object Pointbase extends DbSystemValue("pointbase")

    /** PostgreSQL
      */
    case object Postgresql extends DbSystemValue("postgresql")

    /** Progress Database
      */
    case object Progress extends DbSystemValue("progress")

    /** Redis
      */
    case object Redis extends DbSystemValue("redis")

    /** Amazon Redshift
      */
    case object Redshift extends DbSystemValue("redshift")

    /** Cloud Spanner
      */
    case object Spanner extends DbSystemValue("spanner")

    /** SQLite
      */
    case object Sqlite extends DbSystemValue("sqlite")

    /** Sybase
      */
    case object Sybase extends DbSystemValue("sybase")

    /** Teradata
      */
    case object Teradata extends DbSystemValue("teradata")

    /** Trino
      */
    case object Trino extends DbSystemValue("trino")

    /** Vertica
      */
    case object Vertica extends DbSystemValue("vertica")
  }

  /** Values for [[DbSystemName]].
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.DbAttributes.DbSystemName` instead.",
    ""
  )
  abstract class DbSystemNameValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object DbSystemNameValue {

    /** Some other SQL database. Fallback only.
      */
    case object OtherSql extends DbSystemNameValue("other_sql")

    /** <a href="https://documentation.softwareag.com/?pf=adabas">Adabas (Adaptable Database System)</a>
      */
    case object SoftwareagAdabas extends DbSystemNameValue("softwareag.adabas")

    /** <a href="https://www.actian.com/databases/ingres/">Actian Ingres</a>
      */
    case object ActianIngres extends DbSystemNameValue("actian.ingres")

    /** <a href="https://aws.amazon.com/pm/dynamodb/">Amazon DynamoDB</a>
      */
    case object AwsDynamodb extends DbSystemNameValue("aws.dynamodb")

    /** <a href="https://aws.amazon.com/redshift/">Amazon Redshift</a>
      */
    case object AwsRedshift extends DbSystemNameValue("aws.redshift")

    /** <a href="https://learn.microsoft.com/azure/cosmos-db">Azure Cosmos DB</a>
      */
    case object AzureCosmosdb extends DbSystemNameValue("azure.cosmosdb")

    /** <a href="https://www.intersystems.com/products/cache/">InterSystems Caché</a>
      */
    case object IntersystemsCache extends DbSystemNameValue("intersystems.cache")

    /** <a href="https://cassandra.apache.org/">Apache Cassandra</a>
      */
    case object Cassandra extends DbSystemNameValue("cassandra")

    /** <a href="https://clickhouse.com/">ClickHouse</a>
      */
    case object Clickhouse extends DbSystemNameValue("clickhouse")

    /** <a href="https://www.cockroachlabs.com/">CockroachDB</a>
      */
    case object Cockroachdb extends DbSystemNameValue("cockroachdb")

    /** <a href="https://www.couchbase.com/">Couchbase</a>
      */
    case object Couchbase extends DbSystemNameValue("couchbase")

    /** <a href="https://couchdb.apache.org/">Apache CouchDB</a>
      */
    case object Couchdb extends DbSystemNameValue("couchdb")

    /** <a href="https://db.apache.org/derby/">Apache Derby</a>
      */
    case object Derby extends DbSystemNameValue("derby")

    /** <a href="https://www.elastic.co/elasticsearch">Elasticsearch</a>
      */
    case object Elasticsearch extends DbSystemNameValue("elasticsearch")

    /** <a href="https://www.firebirdsql.org/">Firebird</a>
      */
    case object Firebirdsql extends DbSystemNameValue("firebirdsql")

    /** <a href="https://cloud.google.com/spanner">Google Cloud Spanner</a>
      */
    case object GcpSpanner extends DbSystemNameValue("gcp.spanner")

    /** <a href="https://geode.apache.org/">Apache Geode</a>
      */
    case object Geode extends DbSystemNameValue("geode")

    /** <a href="https://h2database.com/">H2 Database</a>
      */
    case object H2database extends DbSystemNameValue("h2database")

    /** <a href="https://hbase.apache.org/">Apache HBase</a>
      */
    case object Hbase extends DbSystemNameValue("hbase")

    /** <a href="https://hive.apache.org/">Apache Hive</a>
      */
    case object Hive extends DbSystemNameValue("hive")

    /** <a href="https://hsqldb.org/">HyperSQL Database</a>
      */
    case object Hsqldb extends DbSystemNameValue("hsqldb")

    /** <a href="https://www.ibm.com/db2">IBM Db2</a>
      */
    case object IbmDb2 extends DbSystemNameValue("ibm.db2")

    /** <a href="https://www.ibm.com/products/informix">IBM Informix</a>
      */
    case object IbmInformix extends DbSystemNameValue("ibm.informix")

    /** <a href="https://www.ibm.com/products/netezza">IBM Netezza</a>
      */
    case object IbmNetezza extends DbSystemNameValue("ibm.netezza")

    /** <a href="https://www.influxdata.com/">InfluxDB</a>
      */
    case object Influxdb extends DbSystemNameValue("influxdb")

    /** <a href="https://www.instantdb.com/">Instant</a>
      */
    case object Instantdb extends DbSystemNameValue("instantdb")

    /** <a href="https://mariadb.org/">MariaDB</a>
      */
    case object Mariadb extends DbSystemNameValue("mariadb")

    /** <a href="https://memcached.org/">Memcached</a>
      */
    case object Memcached extends DbSystemNameValue("memcached")

    /** <a href="https://www.mongodb.com/">MongoDB</a>
      */
    case object Mongodb extends DbSystemNameValue("mongodb")

    /** <a href="https://www.microsoft.com/sql-server">Microsoft SQL Server</a>
      */
    case object MicrosoftSqlServer extends DbSystemNameValue("microsoft.sql_server")

    /** <a href="https://www.mysql.com/">MySQL</a>
      */
    case object Mysql extends DbSystemNameValue("mysql")

    /** <a href="https://neo4j.com/">Neo4j</a>
      */
    case object Neo4j extends DbSystemNameValue("neo4j")

    /** <a href="https://opensearch.org/">OpenSearch</a>
      */
    case object Opensearch extends DbSystemNameValue("opensearch")

    /** <a href="https://www.oracle.com/database/">Oracle Database</a>
      */
    case object OracleDb extends DbSystemNameValue("oracle.db")

    /** <a href="https://www.postgresql.org/">PostgreSQL</a>
      */
    case object Postgresql extends DbSystemNameValue("postgresql")

    /** <a href="https://redis.io/">Redis</a>
      */
    case object Redis extends DbSystemNameValue("redis")

    /** <a href="https://www.sap.com/products/technology-platform/hana/what-is-sap-hana.html">SAP HANA</a>
      */
    case object SapHana extends DbSystemNameValue("sap.hana")

    /** <a href="https://maxdb.sap.com/">SAP MaxDB</a>
      */
    case object SapMaxdb extends DbSystemNameValue("sap.maxdb")

    /** <a href="https://www.sqlite.org/">SQLite</a>
      */
    case object Sqlite extends DbSystemNameValue("sqlite")

    /** <a href="https://www.teradata.com/">Teradata</a>
      */
    case object Teradata extends DbSystemNameValue("teradata")

    /** <a href="https://trino.io/">Trino</a>
      */
    case object Trino extends DbSystemNameValue("trino")
  }

}
