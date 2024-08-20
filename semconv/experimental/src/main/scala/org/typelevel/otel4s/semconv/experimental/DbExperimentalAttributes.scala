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
object DbExperimentalAttributes {

  /**
  * The consistency level of the query. Based on consistency values from <a href="https://docs.datastax.com/en/cassandra-oss/3.0/cassandra/dml/dmlConfigConsistency.html">CQL</a>.
  */
  val DbCassandraConsistencyLevel: AttributeKey[String] = string("db.cassandra.consistency_level")

  /**
  * The data center of the coordinating node for a query.
  */
  val DbCassandraCoordinatorDc: AttributeKey[String] = string("db.cassandra.coordinator.dc")

  /**
  * The ID of the coordinating node for a query.
  */
  val DbCassandraCoordinatorId: AttributeKey[String] = string("db.cassandra.coordinator.id")

  /**
  * Whether or not the query is idempotent.
  */
  val DbCassandraIdempotence: AttributeKey[Boolean] = boolean("db.cassandra.idempotence")

  /**
  * The fetch size used for paging, i.e. how many rows will be returned at once.
  */
  val DbCassandraPageSize: AttributeKey[Long] = long("db.cassandra.page_size")

  /**
  * The number of times a query was speculatively executed. Not set or `0` if the query was not executed speculatively.
  */
  val DbCassandraSpeculativeExecutionCount: AttributeKey[Long] = long("db.cassandra.speculative_execution_count")

  /**
  * Deprecated, use `db.collection.name` instead.
  */
  @deprecated("Use `db.collection.name` instead", "0.5.0")
  val DbCassandraTable: AttributeKey[String] = string("db.cassandra.table")

  /**
  * The name of the connection pool; unique within the instrumented application. In case the connection pool implementation doesn't provide a name, instrumentation SHOULD use a combination of parameters that would make the name unique, for example, combining attributes `server.address`, `server.port`, and `db.namespace`, formatted as `server.address:server.port/db.namespace`. Instrumentations that generate connection pool name following different patterns SHOULD document it.
  */
  val DbClientConnectionPoolName: AttributeKey[String] = string("db.client.connection.pool.name")

  /**
  * The state of a connection in the pool
  */
  val DbClientConnectionState: AttributeKey[String] = string("db.client.connection.state")

  /**
  * Deprecated, use `db.client.connection.pool.name` instead.
  */
  @deprecated("Use `db.client.connection.pool.name` instead", "0.5.0")
  val DbClientConnectionsPoolName: AttributeKey[String] = string("db.client.connections.pool.name")

  /**
  * Deprecated, use `db.client.connection.state` instead.
  */
  @deprecated("Use `db.client.connection.state` instead", "0.5.0")
  val DbClientConnectionsState: AttributeKey[String] = string("db.client.connections.state")

  /**
  * The name of a collection (table, container) within the database.
  *
  * @note 
  *  - It is RECOMMENDED to capture the value as provided by the application without attempting to do any case normalization.
If the collection name is parsed from the query text, it SHOULD be the first collection name found in the query and it SHOULD match the value provided in the query text including any schema and database name prefix.
For batch operations, if the individual operations are known to have the same collection name then that collection name SHOULD be used, otherwise `db.collection.name` SHOULD NOT be captured.
  */
  val DbCollectionName: AttributeKey[String] = string("db.collection.name")

  /**
  * Deprecated, use `server.address`, `server.port` attributes instead.
  */
  @deprecated("Use `server.address`, `server.port` attributes instead", "0.5.0")
  val DbConnectionString: AttributeKey[String] = string("db.connection_string")

  /**
  * Unique Cosmos client instance id.
  */
  val DbCosmosdbClientId: AttributeKey[String] = string("db.cosmosdb.client_id")

  /**
  * Cosmos client connection mode.
  */
  val DbCosmosdbConnectionMode: AttributeKey[String] = string("db.cosmosdb.connection_mode")

  /**
  * Deprecated, use `db.collection.name` instead.
  */
  @deprecated("Use `db.collection.name` instead", "0.5.0")
  val DbCosmosdbContainer: AttributeKey[String] = string("db.cosmosdb.container")

  /**
  * CosmosDB Operation Type.
  */
  val DbCosmosdbOperationType: AttributeKey[String] = string("db.cosmosdb.operation_type")

  /**
  * RU consumed for that operation
  */
  val DbCosmosdbRequestCharge: AttributeKey[Double] = double("db.cosmosdb.request_charge")

  /**
  * Request payload size in bytes
  */
  val DbCosmosdbRequestContentLength: AttributeKey[Long] = long("db.cosmosdb.request_content_length")

  /**
  * Cosmos DB status code.
  */
  val DbCosmosdbStatusCode: AttributeKey[Long] = long("db.cosmosdb.status_code")

  /**
  * Cosmos DB sub status code.
  */
  val DbCosmosdbSubStatusCode: AttributeKey[Long] = long("db.cosmosdb.sub_status_code")

  /**
  * Deprecated, use `db.namespace` instead.
  */
  @deprecated("Use `db.namespace` instead", "0.5.0")
  val DbElasticsearchClusterName: AttributeKey[String] = string("db.elasticsearch.cluster.name")

  /**
  * Represents the human-readable identifier of the node/instance to which a request was routed.
  */
  val DbElasticsearchNodeName: AttributeKey[String] = string("db.elasticsearch.node.name")

  /**
  * A dynamic value in the url path.
  *
  * @note 
  *  - Many Elasticsearch url paths allow dynamic values. These SHOULD be recorded in span attributes in the format `db.elasticsearch.path_parts.<key>`, where `<key>` is the url path part name. The implementation SHOULD reference the <a href="https://raw.githubusercontent.com/elastic/elasticsearch-specification/main/output/schema/schema.json">elasticsearch schema</a> in order to map the path part values to their names.
  */
  val DbElasticsearchPathParts: AttributeKey[String] = string("db.elasticsearch.path_parts")

  /**
  * Deprecated, no general replacement at this time. For Elasticsearch, use `db.elasticsearch.node.name` instead.
  */
  @deprecated("No general replacement at this time. for elasticsearch, use `db.elasticsearch.node.name` instead", "0.5.0")
  val DbInstanceId: AttributeKey[String] = string("db.instance.id")

  /**
  * Removed, no replacement at this time.
  */
  @deprecated("Removed, no replacement at this time", "0.5.0")
  val DbJdbcDriverClassname: AttributeKey[String] = string("db.jdbc.driver_classname")

  /**
  * Deprecated, use `db.collection.name` instead.
  */
  @deprecated("Use `db.collection.name` instead", "0.5.0")
  val DbMongodbCollection: AttributeKey[String] = string("db.mongodb.collection")

  /**
  * Deprecated, SQL Server instance is now populated as a part of `db.namespace` attribute.
  */
  @deprecated("Sql server instance is now populated as a part of `db.namespace` attribute", "0.5.0")
  val DbMssqlInstanceName: AttributeKey[String] = string("db.mssql.instance_name")

  /**
  * Deprecated, use `db.namespace` instead.
  */
  @deprecated("Use `db.namespace` instead", "0.5.0")
  val DbName: AttributeKey[String] = string("db.name")

  /**
  * The name of the database, fully qualified within the server address and port.
  *
  * @note 
  *  - If a database system has multiple namespace components, they SHOULD be concatenated (potentially using database system specific conventions) from most general to most specific namespace component, and more specific namespaces SHOULD NOT be captured without the more general namespaces, to ensure that &quot;startswith&quot; queries for the more general namespaces will be valid.
Semantic conventions for individual database systems SHOULD document what `db.namespace` means in the context of that system.
It is RECOMMENDED to capture the value as provided by the application without attempting to do any case normalization.
  */
  val DbNamespace: AttributeKey[String] = string("db.namespace")

  /**
  * Deprecated, use `db.operation.name` instead.
  */
  @deprecated("Use `db.operation.name` instead", "0.5.0")
  val DbOperation: AttributeKey[String] = string("db.operation")

  /**
  * The number of queries included in a <a href="/docs/database/database-spans.md#batch-operations">batch operation</a>.
  *
  * @note 
  *  - Operations are only considered batches when they contain two or more operations, and so `db.operation.batch.size` SHOULD never be `1`.
  */
  val DbOperationBatchSize: AttributeKey[Long] = long("db.operation.batch.size")

  /**
  * The name of the operation or command being executed.
  *
  * @note 
  *  - It is RECOMMENDED to capture the value as provided by the application without attempting to do any case normalization.
If the operation name is parsed from the query text, it SHOULD be the first operation name found in the query.
For batch operations, if the individual operations are known to have the same operation name then that operation name SHOULD be used prepended by `BATCH`, otherwise `db.operation.name` SHOULD be `BATCH` or some other database system specific term if more applicable.
  */
  val DbOperationName: AttributeKey[String] = string("db.operation.name")

  /**
  * A query parameter used in `db.query.text`, with `<key>` being the parameter name, and the attribute value being a string representation of the parameter value.
  *
  * @note 
  *  - Query parameters should only be captured when `db.query.text` is parameterized with placeholders.
If a parameter has no name and instead is referenced only by index, then `<key>` SHOULD be the 0-based index.
  */
  val DbQueryParameter: AttributeKey[String] = string("db.query.parameter")

  /**
  * The database query being executed.
  *
  * @note 
  *  - For sanitization see <a href="../../docs/database/database-spans.md#sanitization-of-dbquerytext">Sanitization of `db.query.text`</a>.
For batch operations, if the individual operations are known to have the same query text then that query text SHOULD be used, otherwise all of the individual query texts SHOULD be concatenated with separator `;` or some other database system specific separator if more applicable.
Even though parameterized query text can potentially have sensitive data, by using a parameterized query the user is giving a strong signal that any sensitive data will be passed as parameter values, and the benefit to observability of capturing the static part of the query text by default outweighs the risk.
  */
  val DbQueryText: AttributeKey[String] = string("db.query.text")

  /**
  * Deprecated, use `db.namespace` instead.
  */
  @deprecated("Use `db.namespace` instead", "0.5.0")
  val DbRedisDatabaseIndex: AttributeKey[Long] = long("db.redis.database_index")

  /**
  * Deprecated, use `db.collection.name` instead.
  */
  @deprecated("Use `db.collection.name` instead", "0.5.0")
  val DbSqlTable: AttributeKey[String] = string("db.sql.table")

  /**
  * The database statement being executed.
  */
  @deprecated("The database statement being executed", "0.5.0")
  val DbStatement: AttributeKey[String] = string("db.statement")

  /**
  * The database management system (DBMS) product as identified by the client instrumentation.
  *
  * @note 
  *  - The actual DBMS may differ from the one identified by the client. For example, when using PostgreSQL client libraries to connect to a CockroachDB, the `db.system` is set to `postgresql` based on the instrumentation's best knowledge.
  */
  val DbSystem: AttributeKey[String] = string("db.system")

  /**
  * Deprecated, no replacement at this time.
  */
  @deprecated("No replacement at this time", "0.5.0")
  val DbUser: AttributeKey[String] = string("db.user")
  // Enum definitions
  
  /**
   * Values for [[DbCassandraConsistencyLevel]].
   */
  abstract class DbCassandraConsistencyLevelValue(val value: String)
  object DbCassandraConsistencyLevelValue {
    /** all. */
    case object All extends DbCassandraConsistencyLevelValue("all")
    /** each_quorum. */
    case object EachQuorum extends DbCassandraConsistencyLevelValue("each_quorum")
    /** quorum. */
    case object Quorum extends DbCassandraConsistencyLevelValue("quorum")
    /** local_quorum. */
    case object LocalQuorum extends DbCassandraConsistencyLevelValue("local_quorum")
    /** one. */
    case object One extends DbCassandraConsistencyLevelValue("one")
    /** two. */
    case object Two extends DbCassandraConsistencyLevelValue("two")
    /** three. */
    case object Three extends DbCassandraConsistencyLevelValue("three")
    /** local_one. */
    case object LocalOne extends DbCassandraConsistencyLevelValue("local_one")
    /** any. */
    case object Any extends DbCassandraConsistencyLevelValue("any")
    /** serial. */
    case object Serial extends DbCassandraConsistencyLevelValue("serial")
    /** local_serial. */
    case object LocalSerial extends DbCassandraConsistencyLevelValue("local_serial")
  }
  /**
   * Values for [[DbClientConnectionState]].
   */
  abstract class DbClientConnectionStateValue(val value: String)
  object DbClientConnectionStateValue {
    /** idle. */
    case object Idle extends DbClientConnectionStateValue("idle")
    /** used. */
    case object Used extends DbClientConnectionStateValue("used")
  }
  /**
   * Values for [[DbClientConnectionsState]].
   */
  @deprecated("Use `db.client.connection.state` instead", "0.5.0")
  abstract class DbClientConnectionsStateValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object DbClientConnectionsStateValue {
    /** idle. */
    case object Idle extends DbClientConnectionsStateValue("idle")
    /** used. */
    case object Used extends DbClientConnectionsStateValue("used")
  }
  /**
   * Values for [[DbCosmosdbConnectionMode]].
   */
  abstract class DbCosmosdbConnectionModeValue(val value: String)
  object DbCosmosdbConnectionModeValue {
    /** Gateway (HTTP) connections mode. */
    case object Gateway extends DbCosmosdbConnectionModeValue("gateway")
    /** Direct connection. */
    case object Direct extends DbCosmosdbConnectionModeValue("direct")
  }
  /**
   * Values for [[DbCosmosdbOperationType]].
   */
  abstract class DbCosmosdbOperationTypeValue(val value: String)
  object DbCosmosdbOperationTypeValue {
    /** invalid. */
    case object Invalid extends DbCosmosdbOperationTypeValue("Invalid")
    /** create. */
    case object Create extends DbCosmosdbOperationTypeValue("Create")
    /** patch. */
    case object Patch extends DbCosmosdbOperationTypeValue("Patch")
    /** read. */
    case object Read extends DbCosmosdbOperationTypeValue("Read")
    /** read_feed. */
    case object ReadFeed extends DbCosmosdbOperationTypeValue("ReadFeed")
    /** delete. */
    case object Delete extends DbCosmosdbOperationTypeValue("Delete")
    /** replace. */
    case object Replace extends DbCosmosdbOperationTypeValue("Replace")
    /** execute. */
    case object Execute extends DbCosmosdbOperationTypeValue("Execute")
    /** query. */
    case object Query extends DbCosmosdbOperationTypeValue("Query")
    /** head. */
    case object Head extends DbCosmosdbOperationTypeValue("Head")
    /** head_feed. */
    case object HeadFeed extends DbCosmosdbOperationTypeValue("HeadFeed")
    /** upsert. */
    case object Upsert extends DbCosmosdbOperationTypeValue("Upsert")
    /** batch. */
    case object Batch extends DbCosmosdbOperationTypeValue("Batch")
    /** query_plan. */
    case object QueryPlan extends DbCosmosdbOperationTypeValue("QueryPlan")
    /** execute_javascript. */
    case object ExecuteJavascript extends DbCosmosdbOperationTypeValue("ExecuteJavaScript")
  }
  /**
   * Values for [[DbSystem]].
   */
  abstract class DbSystemValue(val value: String)
  object DbSystemValue {
    /** Some other SQL database. Fallback only. See notes. */
    case object OtherSql extends DbSystemValue("other_sql")
    /** Adabas (Adaptable Database System). */
    case object Adabas extends DbSystemValue("adabas")
    /** Deprecated, use `intersystems_cache` instead. */
    case object Cache extends DbSystemValue("cache")
    /** InterSystems Caché. */
    case object IntersystemsCache extends DbSystemValue("intersystems_cache")
    /** Apache Cassandra. */
    case object Cassandra extends DbSystemValue("cassandra")
    /** ClickHouse. */
    case object Clickhouse extends DbSystemValue("clickhouse")
    /** Deprecated, use `other_sql` instead. */
    case object Cloudscape extends DbSystemValue("cloudscape")
    /** CockroachDB. */
    case object Cockroachdb extends DbSystemValue("cockroachdb")
    /** Deprecated, no replacement at this time. */
    case object Coldfusion extends DbSystemValue("coldfusion")
    /** Microsoft Azure Cosmos DB. */
    case object Cosmosdb extends DbSystemValue("cosmosdb")
    /** Couchbase. */
    case object Couchbase extends DbSystemValue("couchbase")
    /** CouchDB. */
    case object Couchdb extends DbSystemValue("couchdb")
    /** IBM Db2. */
    case object Db2 extends DbSystemValue("db2")
    /** Apache Derby. */
    case object Derby extends DbSystemValue("derby")
    /** Amazon DynamoDB. */
    case object Dynamodb extends DbSystemValue("dynamodb")
    /** EnterpriseDB. */
    case object Edb extends DbSystemValue("edb")
    /** Elasticsearch. */
    case object Elasticsearch extends DbSystemValue("elasticsearch")
    /** FileMaker. */
    case object Filemaker extends DbSystemValue("filemaker")
    /** Firebird. */
    case object Firebird extends DbSystemValue("firebird")
    /** Deprecated, use `other_sql` instead. */
    case object Firstsql extends DbSystemValue("firstsql")
    /** Apache Geode. */
    case object Geode extends DbSystemValue("geode")
    /** H2. */
    case object H2 extends DbSystemValue("h2")
    /** SAP HANA. */
    case object Hanadb extends DbSystemValue("hanadb")
    /** Apache HBase. */
    case object Hbase extends DbSystemValue("hbase")
    /** Apache Hive. */
    case object Hive extends DbSystemValue("hive")
    /** HyperSQL DataBase. */
    case object Hsqldb extends DbSystemValue("hsqldb")
    /** InfluxDB. */
    case object Influxdb extends DbSystemValue("influxdb")
    /** Informix. */
    case object Informix extends DbSystemValue("informix")
    /** Ingres. */
    case object Ingres extends DbSystemValue("ingres")
    /** InstantDB. */
    case object Instantdb extends DbSystemValue("instantdb")
    /** InterBase. */
    case object Interbase extends DbSystemValue("interbase")
    /** MariaDB. */
    case object Mariadb extends DbSystemValue("mariadb")
    /** SAP MaxDB. */
    case object Maxdb extends DbSystemValue("maxdb")
    /** Memcached. */
    case object Memcached extends DbSystemValue("memcached")
    /** MongoDB. */
    case object Mongodb extends DbSystemValue("mongodb")
    /** Microsoft SQL Server. */
    case object Mssql extends DbSystemValue("mssql")
    /** Deprecated, Microsoft SQL Server Compact is discontinued. */
    case object Mssqlcompact extends DbSystemValue("mssqlcompact")
    /** MySQL. */
    case object Mysql extends DbSystemValue("mysql")
    /** Neo4j. */
    case object Neo4j extends DbSystemValue("neo4j")
    /** Netezza. */
    case object Netezza extends DbSystemValue("netezza")
    /** OpenSearch. */
    case object Opensearch extends DbSystemValue("opensearch")
    /** Oracle Database. */
    case object Oracle extends DbSystemValue("oracle")
    /** Pervasive PSQL. */
    case object Pervasive extends DbSystemValue("pervasive")
    /** PointBase. */
    case object Pointbase extends DbSystemValue("pointbase")
    /** PostgreSQL. */
    case object Postgresql extends DbSystemValue("postgresql")
    /** Progress Database. */
    case object Progress extends DbSystemValue("progress")
    /** Redis. */
    case object Redis extends DbSystemValue("redis")
    /** Amazon Redshift. */
    case object Redshift extends DbSystemValue("redshift")
    /** Cloud Spanner. */
    case object Spanner extends DbSystemValue("spanner")
    /** SQLite. */
    case object Sqlite extends DbSystemValue("sqlite")
    /** Sybase. */
    case object Sybase extends DbSystemValue("sybase")
    /** Teradata. */
    case object Teradata extends DbSystemValue("teradata")
    /** Trino. */
    case object Trino extends DbSystemValue("trino")
    /** Vertica. */
    case object Vertica extends DbSystemValue("vertica")
  }

}