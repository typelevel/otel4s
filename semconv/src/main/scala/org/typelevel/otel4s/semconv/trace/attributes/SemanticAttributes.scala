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

package org.typelevel.otel4s.semconv.trace.attributes

import org.typelevel.otel4s.AttributeKey
import org.typelevel.otel4s.AttributeKey._

// DO NOT EDIT, this is an Auto-generated file from buildscripts/semantic-convention/templates/SemanticAttributes.scala.j2
object SemanticAttributes {
  /**
   * The URL of the OpenTelemetry schema for these keys and values.
   */
  final val SchemaUrl = "https://opentelemetry.io/schemas/1.24.0"

  /**
  * The name of the invoked function.
  *
  * <p>Notes:
    <ul> <li>SHOULD be equal to the `faas.name` resource attribute of the invoked function.</li> </ul>
  */
  val FaasInvokedName: AttributeKey[String] = string("faas.invoked_name")

  /**
  * The cloud provider of the invoked function.
  *
  * <p>Notes:
    <ul> <li>SHOULD be equal to the `cloud.provider` resource attribute of the invoked function.</li> </ul>
  */
  val FaasInvokedProvider: AttributeKey[String] = string("faas.invoked_provider")

  /**
  * The cloud region of the invoked function.
  *
  * <p>Notes:
    <ul> <li>SHOULD be equal to the `cloud.region` resource attribute of the invoked function.</li> </ul>
  */
  val FaasInvokedRegion: AttributeKey[String] = string("faas.invoked_region")

  /**
  * Type of the trigger which caused this function invocation.
  */
  val FaasTrigger: AttributeKey[String] = string("faas.trigger")

  /**
  * The <a href="/docs/resource/README.md#service">`service.name`</a> of the remote service. SHOULD be equal to the actual `service.name` resource attribute of the remote service if any.
  */
  val PeerService: AttributeKey[String] = string("peer.service")

  /**
  * Username or client_id extracted from the access token or <a href="https://tools.ietf.org/html/rfc7235#section-4.2">Authorization</a> header in the inbound request from outside the system.
  */
  val EnduserId: AttributeKey[String] = string("enduser.id")

  /**
  * Actual/assumed role the client is making the request under extracted from token or application security context.
  */
  val EnduserRole: AttributeKey[String] = string("enduser.role")

  /**
  * Scopes or granted authorities the client currently possesses extracted from token or application security context. The value would come from the scope associated with an <a href="https://tools.ietf.org/html/rfc6749#section-3.3">OAuth 2.0 Access Token</a> or an attribute value in a <a href="http://docs.oasis-open.org/security/saml/Post2.0/sstc-saml-tech-overview-2.0.html">SAML 2.0 Assertion</a>.
  */
  val EnduserScope: AttributeKey[String] = string("enduser.scope")

  /**
  * Identifies the class / type of event.
  *
  * <p>Notes:
    <ul> <li>Event names are subject to the same rules as <a href="https://github.com/open-telemetry/opentelemetry-specification/tree/v1.26.0/specification/common/attribute-naming.md">attribute names</a>. Notably, event names are namespaced to avoid collisions and provide a clean separation of semantics for events in separate domains like browser, mobile, and kubernetes.</li> </ul>
  */
  val EventName: AttributeKey[String] = string("event.name")

  /**
  * A unique identifier for the Log Record.
  *
  * <p>Notes:
    <ul> <li>If an id is provided, other log records with the same id will be considered duplicates and can be removed safely. This means, that two distinguishable log records MUST have different values.
The id MAY be an <a href="https://github.com/ulid/spec">Universally Unique Lexicographically Sortable Identifier (ULID)</a>, but other identifiers (e.g. UUID) may be used as needed.</li> </ul>
  */
  val LogRecordUid: AttributeKey[String] = string("log.record.uid")

  /**
  * The stream associated with the log. See below for a list of well-known values.
  */
  val LogIostream: AttributeKey[String] = string("log.iostream")

  /**
  * The basename of the file.
  */
  val LogFileName: AttributeKey[String] = string("log.file.name")

  /**
  * The basename of the file, with symlinks resolved.
  */
  val LogFileNameResolved: AttributeKey[String] = string("log.file.name_resolved")

  /**
  * The full path to the file.
  */
  val LogFilePath: AttributeKey[String] = string("log.file.path")

  /**
  * The full path to the file, with symlinks resolved.
  */
  val LogFilePathResolved: AttributeKey[String] = string("log.file.path_resolved")

  /**
  * This attribute represents the state the application has transitioned into at the occurrence of the event.
  *
  * <p>Notes:
    <ul> <li>The iOS lifecycle states are defined in the <a href="https://developer.apple.com/documentation/uikit/uiapplicationdelegate#1656902">UIApplicationDelegate documentation</a>, and from which the `OS terminology` column values are derived.</li> </ul>
  */
  val IosState: AttributeKey[String] = string("ios.state")

  /**
  * This attribute represents the state the application has transitioned into at the occurrence of the event.
  *
  * <p>Notes:
    <ul> <li>The Android lifecycle states are defined in <a href="https://developer.android.com/guide/components/activities/activity-lifecycle#lc">Activity lifecycle callbacks</a>, and from which the `OS identifiers` are derived.</li> </ul>
  */
  val AndroidState: AttributeKey[String] = string("android.state")

  /**
  * The name of the connection pool; unique within the instrumented application. In case the connection pool implementation doesn't provide a name, then the <a href="/docs/database/database-spans.md#connection-level-attributes">db.connection_string</a> should be used
  */
  val PoolName: AttributeKey[String] = string("pool.name")

  /**
  * The state of a connection in the pool
  */
  val State: AttributeKey[String] = string("state")

  /**
  * Full type name of the <a href="https://learn.microsoft.com/dotnet/api/microsoft.aspnetcore.diagnostics.iexceptionhandler">`IExceptionHandler`</a> implementation that handled the exception.
  */
  val AspnetcoreDiagnosticsHandlerType: AttributeKey[String] = string("aspnetcore.diagnostics.handler.type")

  /**
  * Rate limiting policy name.
  */
  val AspnetcoreRateLimitingPolicy: AttributeKey[String] = string("aspnetcore.rate_limiting.policy")

  /**
  * Rate-limiting result, shows whether the lease was acquired or contains a rejection reason
  */
  val AspnetcoreRateLimitingResult: AttributeKey[String] = string("aspnetcore.rate_limiting.result")

  /**
  * Flag indicating if request was handled by the application pipeline.
  */
  val AspnetcoreRequestIsUnhandled: AttributeKey[Boolean] = boolean("aspnetcore.request.is_unhandled")

  /**
  * A value that indicates whether the matched route is a fallback route.
  */
  val AspnetcoreRoutingIsFallback: AttributeKey[Boolean] = boolean("aspnetcore.routing.is_fallback")

  /**
  * SignalR HTTP connection closure status.
  */
  val SignalrConnectionStatus: AttributeKey[String] = string("signalr.connection.status")

  /**
  * <a href="https://github.com/dotnet/aspnetcore/blob/main/src/SignalR/docs/specs/TransportProtocols.md">SignalR transport type</a>
  */
  val SignalrTransport: AttributeKey[String] = string("signalr.transport")

  /**
  * Name of the buffer pool.
  *
  * <p>Notes:
    <ul> <li>Pool names are generally obtained via <a href="https://docs.oracle.com/en/java/javase/11/docs/api/java.management/java/lang/management/BufferPoolMXBean.html#getName()">BufferPoolMXBean#getName()</a>.</li> </ul>
  */
  val JvmBufferPoolName: AttributeKey[String] = string("jvm.buffer.pool.name")

  /**
  * Name of the memory pool.
  *
  * <p>Notes:
    <ul> <li>Pool names are generally obtained via <a href="https://docs.oracle.com/en/java/javase/11/docs/api/java.management/java/lang/management/MemoryPoolMXBean.html#getName()">MemoryPoolMXBean#getName()</a>.</li> </ul>
  */
  val JvmMemoryPoolName: AttributeKey[String] = string("jvm.memory.pool.name")

  /**
  * The type of memory.
  */
  val JvmMemoryType: AttributeKey[String] = string("jvm.memory.type")

  /**
  * The device identifier
  */
  val SystemDevice: AttributeKey[String] = string("system.device")

  /**
  * The logical CPU number [0..n-1]
  */
  val SystemCpuLogicalNumber: AttributeKey[Long] = long("system.cpu.logical_number")

  /**
  * The state of the CPU
  */
  val SystemCpuState: AttributeKey[String] = string("system.cpu.state")

  /**
  * The memory state
  */
  val SystemMemoryState: AttributeKey[String] = string("system.memory.state")

  /**
  * The paging access direction
  */
  val SystemPagingDirection: AttributeKey[String] = string("system.paging.direction")

  /**
  * The memory paging state
  */
  val SystemPagingState: AttributeKey[String] = string("system.paging.state")

  /**
  * The memory paging type
  */
  val SystemPagingType: AttributeKey[String] = string("system.paging.type")

  /**
  * The filesystem mode
  */
  val SystemFilesystemMode: AttributeKey[String] = string("system.filesystem.mode")

  /**
  * The filesystem mount path
  */
  val SystemFilesystemMountpoint: AttributeKey[String] = string("system.filesystem.mountpoint")

  /**
  * The filesystem state
  */
  val SystemFilesystemState: AttributeKey[String] = string("system.filesystem.state")

  /**
  * The filesystem type
  */
  val SystemFilesystemType: AttributeKey[String] = string("system.filesystem.type")

  /**
  * A stateless protocol MUST NOT set this attribute
  */
  val SystemNetworkState: AttributeKey[String] = string("system.network.state")

  /**
  * The process state, e.g., <a href="https://man7.org/linux/man-pages/man1/ps.1.html#PROCESS_STATE_CODES">Linux Process State Codes</a>
  */
  val SystemProcessesStatus: AttributeKey[String] = string("system.processes.status")

  /**
  * Client address - domain name if available without reverse DNS lookup; otherwise, IP address or Unix domain socket name.
  *
  * <p>Notes:
    <ul> <li>When observed from the server side, and when communicating through an intermediary, `client.address` SHOULD represent the client address behind any intermediaries,  for example proxies, if it's available.</li> </ul>
  */
  val ClientAddress: AttributeKey[String] = string("client.address")

  /**
  * Client port number.
  *
  * <p>Notes:
    <ul> <li>When observed from the server side, and when communicating through an intermediary, `client.port` SHOULD represent the client port behind any intermediaries,  for example proxies, if it's available.</li> </ul>
  */
  val ClientPort: AttributeKey[Long] = long("client.port")

  /**
  * The column number in `code.filepath` best representing the operation. It SHOULD point within the code unit named in `code.function`.
  */
  val CodeColumn: AttributeKey[Long] = long("code.column")

  /**
  * The source code file name that identifies the code unit as uniquely as possible (preferably an absolute file path).
  */
  val CodeFilepath: AttributeKey[String] = string("code.filepath")

  /**
  * The method or function name, or equivalent (usually rightmost part of the code unit's name).
  */
  val CodeFunction: AttributeKey[String] = string("code.function")

  /**
  * The line number in `code.filepath` best representing the operation. It SHOULD point within the code unit named in `code.function`.
  */
  val CodeLineno: AttributeKey[Long] = long("code.lineno")

  /**
  * The &quot;namespace&quot; within which `code.function` is defined. Usually the qualified class or module name, such that `code.namespace` + some separator + `code.function` form a unique identifier for the code unit.
  */
  val CodeNamespace: AttributeKey[String] = string("code.namespace")

  /**
  * A stacktrace as a string in the natural representation for the language runtime. The representation is to be determined and documented by each language SIG.
  */
  val CodeStacktrace: AttributeKey[String] = string("code.stacktrace")

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
  * The name of the primary Cassandra table that the operation is acting upon, including the keyspace name (if applicable).
  *
  * <p>Notes:
    <ul> <li>This mirrors the db.sql.table attribute but references cassandra rather than sql. It is not recommended to attempt any client-side parsing of `db.statement` just to get this property, but it should be set if it is provided by the library being instrumented. If the operation is acting upon an anonymous table, or more than one table, this value MUST NOT be set.</li> </ul>
  */
  val DbCassandraTable: AttributeKey[String] = string("db.cassandra.table")

  /**
  * The connection string used to connect to the database. It is recommended to remove embedded credentials.
  */
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
  * Cosmos DB container name.
  */
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
  * Represents the identifier of an Elasticsearch cluster.
  */
  val DbElasticsearchClusterName: AttributeKey[String] = string("db.elasticsearch.cluster.name")

  /**
  * Represents the human-readable identifier of the node/instance to which a request was routed.
  */
  val DbElasticsearchNodeName: AttributeKey[String] = string("db.elasticsearch.node.name")

  /**
  * An identifier (address, unique name, or any other identifier) of the database instance that is executing queries or mutations on the current connection. This is useful in cases where the database is running in a clustered environment and the instrumentation is able to record the node executing the query. The client may obtain this value in databases like MySQL using queries like `select @@hostname`.
  */
  val DbInstanceId: AttributeKey[String] = string("db.instance.id")

  /**
  * The fully-qualified class name of the <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/jdbc/">Java Database Connectivity (JDBC)</a> driver used to connect.
  */
  val DbJdbcDriverClassname: AttributeKey[String] = string("db.jdbc.driver_classname")

  /**
  * The MongoDB collection being accessed within the database stated in `db.name`.
  */
  val DbMongodbCollection: AttributeKey[String] = string("db.mongodb.collection")

  /**
  * The Microsoft SQL Server <a href="https://docs.microsoft.com/sql/connect/jdbc/building-the-connection-url?view=sql-server-ver15">instance name</a> connecting to. This name is used to determine the port of a named instance.
  *
  * <p>Notes:
    <ul> <li>If setting a `db.mssql.instance_name`, `server.port` is no longer required (but still recommended if non-standard).</li> </ul>
  */
  val DbMssqlInstanceName: AttributeKey[String] = string("db.mssql.instance_name")

  /**
  * This attribute is used to report the name of the database being accessed. For commands that switch the database, this should be set to the target database (even if the command fails).
  *
  * <p>Notes:
    <ul> <li>In some SQL databases, the database name to be used is called &quot;schema name&quot;. In case there are multiple layers that could be considered for database name (e.g. Oracle instance name and schema name), the database name to be used is the more specific layer (e.g. Oracle schema name).</li> </ul>
  */
  val DbName: AttributeKey[String] = string("db.name")

  /**
  * The name of the operation being executed, e.g. the <a href="https://docs.mongodb.com/manual/reference/command/#database-operations">MongoDB command name</a> such as `findAndModify`, or the SQL keyword.
  *
  * <p>Notes:
    <ul> <li>When setting this to an SQL keyword, it is not recommended to attempt any client-side parsing of `db.statement` just to get this property, but it should be set if the operation name is provided by the library being instrumented. If the SQL statement has an ambiguous operation, or performs more than one operation, this value may be omitted.</li> </ul>
  */
  val DbOperation: AttributeKey[String] = string("db.operation")

  /**
  * The index of the database being accessed as used in the <a href="https://redis.io/commands/select">`SELECT` command</a>, provided as an integer. To be used instead of the generic `db.name` attribute.
  */
  val DbRedisDatabaseIndex: AttributeKey[Long] = long("db.redis.database_index")

  /**
  * The name of the primary table that the operation is acting upon, including the database name (if applicable).
  *
  * <p>Notes:
    <ul> <li>It is not recommended to attempt any client-side parsing of `db.statement` just to get this property, but it should be set if it is provided by the library being instrumented. If the operation is acting upon an anonymous table, or more than one table, this value MUST NOT be set.</li> </ul>
  */
  val DbSqlTable: AttributeKey[String] = string("db.sql.table")

  /**
  * The database statement being executed.
  */
  val DbStatement: AttributeKey[String] = string("db.statement")

  /**
  * An identifier for the database management system (DBMS) product being used. See below for a list of well-known identifiers.
  */
  val DbSystem: AttributeKey[String] = string("db.system")

  /**
  * Username for accessing the database.
  */
  val DbUser: AttributeKey[String] = string("db.user")

  /**
  * Deprecated, use `network.protocol.name` instead.
  */
  @deprecated("Use `network.protocol.name` instead", "0.3.0")
  val HttpFlavor: AttributeKey[String] = string("http.flavor")

  /**
  * Deprecated, use `http.request.method` instead.
  */
  @deprecated("Use `http.request.method` instead", "0.3.0")
  val HttpMethod: AttributeKey[String] = string("http.method")

  /**
  * Deprecated, use `http.request.header.content-length` instead.
  */
  @deprecated("Use `http.request.header.content-length` instead", "0.3.0")
  val HttpRequestContentLength: AttributeKey[Long] = long("http.request_content_length")

  /**
  * Deprecated, use `http.response.header.content-length` instead.
  */
  @deprecated("Use `http.response.header.content-length` instead", "0.3.0")
  val HttpResponseContentLength: AttributeKey[Long] = long("http.response_content_length")

  /**
  * Deprecated, use `url.scheme` instead.
  */
  @deprecated("Use `url.scheme` instead", "0.3.0")
  val HttpScheme: AttributeKey[String] = string("http.scheme")

  /**
  * Deprecated, use `http.response.status_code` instead.
  */
  @deprecated("Use `http.response.status_code` instead", "0.3.0")
  val HttpStatusCode: AttributeKey[Long] = long("http.status_code")

  /**
  * Deprecated, use `url.path` and `url.query` instead.
  */
  @deprecated("Use `url.path` and `url.query` instead", "0.3.0")
  val HttpTarget: AttributeKey[String] = string("http.target")

  /**
  * Deprecated, use `url.full` instead.
  */
  @deprecated("Use `url.full` instead", "0.3.0")
  val HttpUrl: AttributeKey[String] = string("http.url")

  /**
  * Deprecated, use `user_agent.original` instead.
  */
  @deprecated("Use `user_agent.original` instead", "0.3.0")
  val HttpUserAgent: AttributeKey[String] = string("http.user_agent")

  /**
  * Deprecated, use `server.address`.
  */
  @deprecated("Use `server.address`", "0.3.0")
  val NetHostName: AttributeKey[String] = string("net.host.name")

  /**
  * Deprecated, use `server.port`.
  */
  @deprecated("Use `server.port`", "0.3.0")
  val NetHostPort: AttributeKey[Long] = long("net.host.port")

  /**
  * Deprecated, use `server.address` on client spans and `client.address` on server spans.
  */
  @deprecated("Use `server.address` on client spans and `client.address` on server spans", "0.3.0")
  val NetPeerName: AttributeKey[String] = string("net.peer.name")

  /**
  * Deprecated, use `server.port` on client spans and `client.port` on server spans.
  */
  @deprecated("Use `server.port` on client spans and `client.port` on server spans", "0.3.0")
  val NetPeerPort: AttributeKey[Long] = long("net.peer.port")

  /**
  * Deprecated, use `network.protocol.name`.
  */
  @deprecated("Use `network.protocol.name`", "0.3.0")
  val NetProtocolName: AttributeKey[String] = string("net.protocol.name")

  /**
  * Deprecated, use `network.protocol.version`.
  */
  @deprecated("Use `network.protocol.version`", "0.3.0")
  val NetProtocolVersion: AttributeKey[String] = string("net.protocol.version")

  /**
  * Deprecated, use `network.transport` and `network.type`.
  */
  @deprecated("Use `network.transport` and `network.type`", "0.3.0")
  val NetSockFamily: AttributeKey[String] = string("net.sock.family")

  /**
  * Deprecated, use `network.local.address`.
  */
  @deprecated("Use `network.local.address`", "0.3.0")
  val NetSockHostAddr: AttributeKey[String] = string("net.sock.host.addr")

  /**
  * Deprecated, use `network.local.port`.
  */
  @deprecated("Use `network.local.port`", "0.3.0")
  val NetSockHostPort: AttributeKey[Long] = long("net.sock.host.port")

  /**
  * Deprecated, use `network.peer.address`.
  */
  @deprecated("Use `network.peer.address`", "0.3.0")
  val NetSockPeerAddr: AttributeKey[String] = string("net.sock.peer.addr")

  /**
  * Deprecated, no replacement at this time.
  */
  @deprecated("No replacement at this time", "0.3.0")
  val NetSockPeerName: AttributeKey[String] = string("net.sock.peer.name")

  /**
  * Deprecated, use `network.peer.port`.
  */
  @deprecated("Use `network.peer.port`", "0.3.0")
  val NetSockPeerPort: AttributeKey[Long] = long("net.sock.peer.port")

  /**
  * Deprecated, use `network.transport`.
  */
  @deprecated("Use `network.transport`", "0.3.0")
  val NetTransport: AttributeKey[String] = string("net.transport")

  /**
  * Destination address - domain name if available without reverse DNS lookup; otherwise, IP address or Unix domain socket name.
  *
  * <p>Notes:
    <ul> <li>When observed from the source side, and when communicating through an intermediary, `destination.address` SHOULD represent the destination address behind any intermediaries, for example proxies, if it's available.</li> </ul>
  */
  val DestinationAddress: AttributeKey[String] = string("destination.address")

  /**
  * Destination port number
  */
  val DestinationPort: AttributeKey[Long] = long("destination.port")

  /**
  * The disk IO operation direction.
  */
  val DiskIoDirection: AttributeKey[String] = string("disk.io.direction")

  /**
  * Describes a class of error the operation ended with.
  *
  * <p>Notes:
    <ul> <li>The `error.type` SHOULD be predictable and SHOULD have low cardinality.
Instrumentations SHOULD document the list of errors they report.</li><li>The cardinality of `error.type` within one instrumentation library SHOULD be low.
Telemetry consumers that aggregate data from multiple instrumentation libraries and applications
should be prepared for `error.type` to have high cardinality at query time when no
additional filters are applied.</li><li>If the operation has completed successfully, instrumentations SHOULD NOT set `error.type`.</li><li>If a specific domain defines its own set of error identifiers (such as HTTP or gRPC status codes),
it's RECOMMENDED to:</li><li>Use a domain-specific attribute</li>
<li>Set `error.type` to capture all errors, regardless of whether they are defined within the domain-specific set or not.</li>
 </ul>
  */
  val ErrorType: AttributeKey[String] = string("error.type")

  /**
  * SHOULD be set to true if the exception event is recorded at a point where it is known that the exception is escaping the scope of the span.
  *
  * <p>Notes:
    <ul> <li>An exception is considered to have escaped (or left) the scope of a span,
if that span is ended while the exception is still logically &quot;in flight&quot;.
This may be actually &quot;in flight&quot; in some languages (e.g. if the exception
is passed to a Context manager's `__exit__` method in Python) but will
usually be caught at the point of recording the exception in most languages.</li><li>It is usually not possible to determine at the point where an exception is thrown
whether it will escape the scope of a span.
However, it is trivial to know that an exception
will escape, if one checks for an active exception just before ending the span,
as done in the <a href="#recording-an-exception">example for recording span exceptions</a>.</li><li>It follows that an exception may still escape the scope of the span
even if the `exception.escaped` attribute was not set or set to false,
since the event might have been recorded at a time where it was not
clear whether the exception will escape.</li> </ul>
  */
  val ExceptionEscaped: AttributeKey[Boolean] = boolean("exception.escaped")

  /**
  * The exception message.
  */
  val ExceptionMessage: AttributeKey[String] = string("exception.message")

  /**
  * A stacktrace as a string in the natural representation for the language runtime. The representation is to be determined and documented by each language SIG.
  */
  val ExceptionStacktrace: AttributeKey[String] = string("exception.stacktrace")

  /**
  * The type of the exception (its fully-qualified class name, if applicable). The dynamic type of the exception should be preferred over the static type in languages that support it.
  */
  val ExceptionType: AttributeKey[String] = string("exception.type")

  /**
  * The size of the request payload body in bytes. This is the number of bytes transferred excluding headers and is often, but not always, present as the <a href="https://www.rfc-editor.org/rfc/rfc9110.html#field.content-length">Content-Length</a> header. For requests using transport encoding, this should be the compressed size.
  */
  val HttpRequestBodySize: AttributeKey[Long] = long("http.request.body.size")

  /**
  * HTTP request method.
  *
  * <p>Notes:
    <ul> <li>HTTP request method value SHOULD be &quot;known&quot; to the instrumentation.
By default, this convention defines &quot;known&quot; methods as the ones listed in <a href="https://www.rfc-editor.org/rfc/rfc9110.html#name-methods">RFC9110</a>
and the PATCH method defined in <a href="https://www.rfc-editor.org/rfc/rfc5789.html">RFC5789</a>.</li><li>If the HTTP request method is not known to instrumentation, it MUST set the `http.request.method` attribute to `_OTHER`.</li><li>If the HTTP instrumentation could end up converting valid HTTP request methods to `_OTHER`, then it MUST provide a way to override
the list of known HTTP methods. If this override is done via environment variable, then the environment variable MUST be named
OTEL_INSTRUMENTATION_HTTP_KNOWN_METHODS and support a comma-separated list of case-sensitive known HTTP methods
(this list MUST be a full override of the default known method, it is not a list of known methods in addition to the defaults).</li><li>HTTP method names are case-sensitive and `http.request.method` attribute value MUST match a known HTTP method name exactly.
Instrumentations for specific web frameworks that consider HTTP methods to be case insensitive, SHOULD populate a canonical equivalent.
Tracing instrumentations that do so, MUST also set `http.request.method_original` to the original value.</li> </ul>
  */
  val HttpRequestMethod: AttributeKey[String] = string("http.request.method")

  /**
  * Original HTTP method sent by the client in the request line.
  */
  val HttpRequestMethodOriginal: AttributeKey[String] = string("http.request.method_original")

  /**
  * The ordinal number of request resending attempt (for any reason, including redirects).
  *
  * <p>Notes:
    <ul> <li>The resend count SHOULD be updated each time an HTTP request gets resent by the client, regardless of what was the cause of the resending (e.g. redirection, authorization failure, 503 Server Unavailable, network issues, or any other).</li> </ul>
  */
  val HttpRequestResendCount: AttributeKey[Long] = long("http.request.resend_count")

  /**
  * The size of the response payload body in bytes. This is the number of bytes transferred excluding headers and is often, but not always, present as the <a href="https://www.rfc-editor.org/rfc/rfc9110.html#field.content-length">Content-Length</a> header. For requests using transport encoding, this should be the compressed size.
  */
  val HttpResponseBodySize: AttributeKey[Long] = long("http.response.body.size")

  /**
  * <a href="https://tools.ietf.org/html/rfc7231#section-6">HTTP response status code</a>.
  */
  val HttpResponseStatusCode: AttributeKey[Long] = long("http.response.status_code")

  /**
  * The matched route, that is, the path template in the format used by the respective server framework.
  *
  * <p>Notes:
    <ul> <li>MUST NOT be populated when this is not supported by the HTTP server framework as the route attribute should have low-cardinality and the URI path can NOT substitute it.
SHOULD include the <a href="/docs/http/http-spans.md#http-server-definitions">application root</a> if there is one.</li> </ul>
  */
  val HttpRoute: AttributeKey[String] = string("http.route")

  /**
  * The number of messages sent, received, or processed in the scope of the batching operation.
  *
  * <p>Notes:
    <ul> <li>Instrumentations SHOULD NOT set `messaging.batch.message_count` on spans that operate with a single message. When a messaging client library supports both batch and single-message API for the same operation, instrumentations SHOULD use `messaging.batch.message_count` for batching APIs and SHOULD NOT use it for single-message APIs.</li> </ul>
  */
  val MessagingBatchMessageCount: AttributeKey[Long] = long("messaging.batch.message_count")

  /**
  * A unique identifier for the client that consumes or produces a message.
  */
  val MessagingClientId: AttributeKey[String] = string("messaging.client_id")

  /**
  * A boolean that is true if the message destination is anonymous (could be unnamed or have auto-generated name).
  */
  val MessagingDestinationAnonymous: AttributeKey[Boolean] = boolean("messaging.destination.anonymous")

  /**
  * The message destination name
  *
  * <p>Notes:
    <ul> <li>Destination name SHOULD uniquely identify a specific queue, topic or other entity within the broker. If
the broker doesn't have such notion, the destination name SHOULD uniquely identify the broker.</li> </ul>
  */
  val MessagingDestinationName: AttributeKey[String] = string("messaging.destination.name")

  /**
  * Low cardinality representation of the messaging destination name
  *
  * <p>Notes:
    <ul> <li>Destination names could be constructed from templates. An example would be a destination name involving a user name or product id. Although the destination name in this case is of high cardinality, the underlying template is of low cardinality and can be effectively used for grouping and aggregation.</li> </ul>
  */
  val MessagingDestinationTemplate: AttributeKey[String] = string("messaging.destination.template")

  /**
  * A boolean that is true if the message destination is temporary and might not exist anymore after messages are processed.
  */
  val MessagingDestinationTemporary: AttributeKey[Boolean] = boolean("messaging.destination.temporary")

  /**
  * A boolean that is true if the publish message destination is anonymous (could be unnamed or have auto-generated name).
  */
  val MessagingDestinationPublishAnonymous: AttributeKey[Boolean] = boolean("messaging.destination_publish.anonymous")

  /**
  * The name of the original destination the message was published to
  *
  * <p>Notes:
    <ul> <li>The name SHOULD uniquely identify a specific queue, topic, or other entity within the broker. If
the broker doesn't have such notion, the original destination name SHOULD uniquely identify the broker.</li> </ul>
  */
  val MessagingDestinationPublishName: AttributeKey[String] = string("messaging.destination_publish.name")

  /**
  * The ordering key for a given message. If the attribute is not present, the message does not have an ordering key.
  */
  val MessagingGcpPubsubMessageOrderingKey: AttributeKey[String] = string("messaging.gcp_pubsub.message.ordering_key")

  /**
  * Name of the Kafka Consumer Group that is handling the message. Only applies to consumers, not producers.
  */
  val MessagingKafkaConsumerGroup: AttributeKey[String] = string("messaging.kafka.consumer.group")

  /**
  * Partition the message is sent to.
  */
  val MessagingKafkaDestinationPartition: AttributeKey[Long] = long("messaging.kafka.destination.partition")

  /**
  * Message keys in Kafka are used for grouping alike messages to ensure they're processed on the same partition. They differ from `messaging.message.id` in that they're not unique. If the key is `null`, the attribute MUST NOT be set.
  *
  * <p>Notes:
    <ul> <li>If the key type is not string, it's string representation has to be supplied for the attribute. If the key has no unambiguous, canonical string form, don't include its value.</li> </ul>
  */
  val MessagingKafkaMessageKey: AttributeKey[String] = string("messaging.kafka.message.key")

  /**
  * The offset of a record in the corresponding Kafka partition.
  */
  val MessagingKafkaMessageOffset: AttributeKey[Long] = long("messaging.kafka.message.offset")

  /**
  * A boolean that is true if the message is a tombstone.
  */
  val MessagingKafkaMessageTombstone: AttributeKey[Boolean] = boolean("messaging.kafka.message.tombstone")

  /**
  * The size of the message body in bytes.
  *
  * <p>Notes:
    <ul> <li>This can refer to both the compressed or uncompressed body size. If both sizes are known, the uncompressed
body size should be used.</li> </ul>
  */
  val MessagingMessageBodySize: AttributeKey[Long] = long("messaging.message.body.size")

  /**
  * The conversation ID identifying the conversation to which the message belongs, represented as a string. Sometimes called &quot;Correlation ID&quot;.
  */
  val MessagingMessageConversationId: AttributeKey[String] = string("messaging.message.conversation_id")

  /**
  * The size of the message body and metadata in bytes.
  *
  * <p>Notes:
    <ul> <li>This can refer to both the compressed or uncompressed size. If both sizes are known, the uncompressed
size should be used.</li> </ul>
  */
  val MessagingMessageEnvelopeSize: AttributeKey[Long] = long("messaging.message.envelope.size")

  /**
  * A value used by the messaging system as an identifier for the message, represented as a string.
  */
  val MessagingMessageId: AttributeKey[String] = string("messaging.message.id")

  /**
  * A string identifying the kind of messaging operation.
  *
  * <p>Notes:
    <ul> <li>If a custom value is used, it MUST be of low cardinality.</li> </ul>
  */
  val MessagingOperation: AttributeKey[String] = string("messaging.operation")

  /**
  * RabbitMQ message routing key.
  */
  val MessagingRabbitmqDestinationRoutingKey: AttributeKey[String] = string("messaging.rabbitmq.destination.routing_key")

  /**
  * Name of the RocketMQ producer/consumer group that is handling the message. The client type is identified by the SpanKind.
  */
  val MessagingRocketmqClientGroup: AttributeKey[String] = string("messaging.rocketmq.client_group")

  /**
  * Model of message consumption. This only applies to consumer spans.
  */
  val MessagingRocketmqConsumptionModel: AttributeKey[String] = string("messaging.rocketmq.consumption_model")

  /**
  * The delay time level for delay message, which determines the message delay time.
  */
  val MessagingRocketmqMessageDelayTimeLevel: AttributeKey[Long] = long("messaging.rocketmq.message.delay_time_level")

  /**
  * The timestamp in milliseconds that the delay message is expected to be delivered to consumer.
  */
  val MessagingRocketmqMessageDeliveryTimestamp: AttributeKey[Long] = long("messaging.rocketmq.message.delivery_timestamp")

  /**
  * It is essential for FIFO message. Messages that belong to the same message group are always processed one by one within the same consumer group.
  */
  val MessagingRocketmqMessageGroup: AttributeKey[String] = string("messaging.rocketmq.message.group")

  /**
  * Key(s) of message, another way to mark message besides message id.
  */
  val MessagingRocketmqMessageKeys: AttributeKey[Seq[String]] = stringSeq("messaging.rocketmq.message.keys")

  /**
  * The secondary classifier of message besides topic.
  */
  val MessagingRocketmqMessageTag: AttributeKey[String] = string("messaging.rocketmq.message.tag")

  /**
  * Type of message.
  */
  val MessagingRocketmqMessageType: AttributeKey[String] = string("messaging.rocketmq.message.type")

  /**
  * Namespace of RocketMQ resources, resources in different namespaces are individual.
  */
  val MessagingRocketmqNamespace: AttributeKey[String] = string("messaging.rocketmq.namespace")

  /**
  * An identifier for the messaging system being used. See below for a list of well-known identifiers.
  */
  val MessagingSystem: AttributeKey[String] = string("messaging.system")

  /**
  * The ISO 3166-1 alpha-2 2-character country code associated with the mobile carrier network.
  */
  val NetworkCarrierIcc: AttributeKey[String] = string("network.carrier.icc")

  /**
  * The mobile carrier country code.
  */
  val NetworkCarrierMcc: AttributeKey[String] = string("network.carrier.mcc")

  /**
  * The mobile carrier network code.
  */
  val NetworkCarrierMnc: AttributeKey[String] = string("network.carrier.mnc")

  /**
  * The name of the mobile carrier.
  */
  val NetworkCarrierName: AttributeKey[String] = string("network.carrier.name")

  /**
  * This describes more details regarding the connection.type. It may be the type of cell technology connection, but it could be used for describing details about a wifi connection.
  */
  val NetworkConnectionSubtype: AttributeKey[String] = string("network.connection.subtype")

  /**
  * The internet connection type.
  */
  val NetworkConnectionType: AttributeKey[String] = string("network.connection.type")

  /**
  * The network IO operation direction.
  */
  val NetworkIoDirection: AttributeKey[String] = string("network.io.direction")

  /**
  * Local address of the network connection - IP address or Unix domain socket name.
  */
  val NetworkLocalAddress: AttributeKey[String] = string("network.local.address")

  /**
  * Local port number of the network connection.
  */
  val NetworkLocalPort: AttributeKey[Long] = long("network.local.port")

  /**
  * Peer address of the network connection - IP address or Unix domain socket name.
  */
  val NetworkPeerAddress: AttributeKey[String] = string("network.peer.address")

  /**
  * Peer port number of the network connection.
  */
  val NetworkPeerPort: AttributeKey[Long] = long("network.peer.port")

  /**
  * <a href="https://osi-model.com/application-layer/">OSI application layer</a> or non-OSI equivalent.
  *
  * <p>Notes:
    <ul> <li>The value SHOULD be normalized to lowercase.</li> </ul>
  */
  val NetworkProtocolName: AttributeKey[String] = string("network.protocol.name")

  /**
  * Version of the protocol specified in `network.protocol.name`.
  *
  * <p>Notes:
    <ul> <li>`network.protocol.version` refers to the version of the protocol used and might be different from the protocol client's version. If the HTTP client has a version of `0.27.2`, but sends HTTP version `1.1`, this attribute should be set to `1.1`.</li> </ul>
  */
  val NetworkProtocolVersion: AttributeKey[String] = string("network.protocol.version")

  /**
  * <a href="https://osi-model.com/transport-layer/">OSI transport layer</a> or <a href="https://wikipedia.org/wiki/Inter-process_communication">inter-process communication method</a>.
  *
  * <p>Notes:
    <ul> <li>The value SHOULD be normalized to lowercase.</li><li>Consider always setting the transport when setting a port number, since
a port number is ambiguous without knowing the transport. For example
different processes could be listening on TCP port 12345 and UDP port 12345.</li> </ul>
  */
  val NetworkTransport: AttributeKey[String] = string("network.transport")

  /**
  * <a href="https://osi-model.com/network-layer/">OSI network layer</a> or non-OSI equivalent.
  *
  * <p>Notes:
    <ul> <li>The value SHOULD be normalized to lowercase.</li> </ul>
  */
  val NetworkType: AttributeKey[String] = string("network.type")

  /**
  * The <a href="https://connect.build/docs/protocol/#error-codes">error codes</a> of the Connect request. Error codes are always string values.
  */
  val RpcConnectRpcErrorCode: AttributeKey[String] = string("rpc.connect_rpc.error_code")

  /**
  * The <a href="https://github.com/grpc/grpc/blob/v1.33.2/doc/statuscodes.md">numeric status code</a> of the gRPC request.
  */
  val RpcGrpcStatusCode: AttributeKey[Long] = long("rpc.grpc.status_code")

  /**
  * `error.code` property of response if it is an error response.
  */
  val RpcJsonrpcErrorCode: AttributeKey[Long] = long("rpc.jsonrpc.error_code")

  /**
  * `error.message` property of response if it is an error response.
  */
  val RpcJsonrpcErrorMessage: AttributeKey[String] = string("rpc.jsonrpc.error_message")

  /**
  * `id` property of request or response. Since protocol allows id to be int, string, `null` or missing (for notifications), value is expected to be cast to string for simplicity. Use empty string in case of `null` value. Omit entirely if this is a notification.
  */
  val RpcJsonrpcRequestId: AttributeKey[String] = string("rpc.jsonrpc.request_id")

  /**
  * Protocol version as in `jsonrpc` property of request/response. Since JSON-RPC 1.0 doesn't specify this, the value can be omitted.
  */
  val RpcJsonrpcVersion: AttributeKey[String] = string("rpc.jsonrpc.version")

  /**
  * The name of the (logical) method being called, must be equal to the method part in the span name.
  *
  * <p>Notes:
    <ul> <li>This is the logical name of the method from the RPC interface perspective, which can be different from the name of any implementing method/function. The `code.function` attribute may be used to store the latter (e.g., method actually executing the call on the server side, RPC client stub method on the client side).</li> </ul>
  */
  val RpcMethod: AttributeKey[String] = string("rpc.method")

  /**
  * The full (logical) name of the service being called, including its package name, if applicable.
  *
  * <p>Notes:
    <ul> <li>This is the logical name of the service from the RPC interface perspective, which can be different from the name of any implementing class. The `code.namespace` attribute may be used to store the latter (despite the attribute name, it may include a class name; e.g., class with method actually executing the call on the server side, RPC client stub class on the client side).</li> </ul>
  */
  val RpcService: AttributeKey[String] = string("rpc.service")

  /**
  * A string identifying the remoting system. See below for a list of well-known identifiers.
  */
  val RpcSystem: AttributeKey[String] = string("rpc.system")

  /**
  * Server domain name if available without reverse DNS lookup; otherwise, IP address or Unix domain socket name.
  *
  * <p>Notes:
    <ul> <li>When observed from the client side, and when communicating through an intermediary, `server.address` SHOULD represent the server address behind any intermediaries, for example proxies, if it's available.</li> </ul>
  */
  val ServerAddress: AttributeKey[String] = string("server.address")

  /**
  * Server port number.
  *
  * <p>Notes:
    <ul> <li>When observed from the client side, and when communicating through an intermediary, `server.port` SHOULD represent the server port behind any intermediaries, for example proxies, if it's available.</li> </ul>
  */
  val ServerPort: AttributeKey[Long] = long("server.port")

  /**
  * Source address - domain name if available without reverse DNS lookup; otherwise, IP address or Unix domain socket name.
  *
  * <p>Notes:
    <ul> <li>When observed from the destination side, and when communicating through an intermediary, `source.address` SHOULD represent the source address behind any intermediaries, for example proxies, if it's available.</li> </ul>
  */
  val SourceAddress: AttributeKey[String] = string("source.address")

  /**
  * Source port number
  */
  val SourcePort: AttributeKey[Long] = long("source.port")

  /**
  * Current &quot;managed&quot; thread ID (as opposed to OS thread ID).
  */
  val ThreadId: AttributeKey[Long] = long("thread.id")

  /**
  * Current thread name.
  */
  val ThreadName: AttributeKey[String] = string("thread.name")

  /**
  * String indicating the <a href="https://datatracker.ietf.org/doc/html/rfc5246#appendix-A.5">cipher</a> used during the current connection.
  *
  * <p>Notes:
    <ul> <li>The values allowed for `tls.cipher` MUST be one of the `Descriptions` of the <a href="https://www.iana.org/assignments/tls-parameters/tls-parameters.xhtml#table-tls-parameters-4">registered TLS Cipher Suits</a>.</li> </ul>
  */
  val TlsCipher: AttributeKey[String] = string("tls.cipher")

  /**
  * PEM-encoded stand-alone certificate offered by the client. This is usually mutually-exclusive of `client.certificate_chain` since this value also exists in that list.
  */
  val TlsClientCertificate: AttributeKey[String] = string("tls.client.certificate")

  /**
  * Array of PEM-encoded certificates that make up the certificate chain offered by the client. This is usually mutually-exclusive of `client.certificate` since that value should be the first certificate in the chain.
  */
  val TlsClientCertificateChain: AttributeKey[Seq[String]] = stringSeq("tls.client.certificate_chain")

  /**
  * Certificate fingerprint using the MD5 digest of DER-encoded version of certificate offered by the client. For consistency with other hash values, this value should be formatted as an uppercase hash.
  */
  val TlsClientHashMd5: AttributeKey[String] = string("tls.client.hash.md5")

  /**
  * Certificate fingerprint using the SHA1 digest of DER-encoded version of certificate offered by the client. For consistency with other hash values, this value should be formatted as an uppercase hash.
  */
  val TlsClientHashSha1: AttributeKey[String] = string("tls.client.hash.sha1")

  /**
  * Certificate fingerprint using the SHA256 digest of DER-encoded version of certificate offered by the client. For consistency with other hash values, this value should be formatted as an uppercase hash.
  */
  val TlsClientHashSha256: AttributeKey[String] = string("tls.client.hash.sha256")

  /**
  * Distinguished name of <a href="https://datatracker.ietf.org/doc/html/rfc5280#section-4.1.2.6">subject</a> of the issuer of the x.509 certificate presented by the client.
  */
  val TlsClientIssuer: AttributeKey[String] = string("tls.client.issuer")

  /**
  * A hash that identifies clients based on how they perform an SSL/TLS handshake.
  */
  val TlsClientJa3: AttributeKey[String] = string("tls.client.ja3")

  /**
  * Date/Time indicating when client certificate is no longer considered valid.
  */
  val TlsClientNotAfter: AttributeKey[String] = string("tls.client.not_after")

  /**
  * Date/Time indicating when client certificate is first considered valid.
  */
  val TlsClientNotBefore: AttributeKey[String] = string("tls.client.not_before")

  /**
  * Also called an SNI, this tells the server which hostname to which the client is attempting to connect to.
  */
  val TlsClientServerName: AttributeKey[String] = string("tls.client.server_name")

  /**
  * Distinguished name of subject of the x.509 certificate presented by the client.
  */
  val TlsClientSubject: AttributeKey[String] = string("tls.client.subject")

  /**
  * Array of ciphers offered by the client during the client hello.
  */
  val TlsClientSupportedCiphers: AttributeKey[Seq[String]] = stringSeq("tls.client.supported_ciphers")

  /**
  * String indicating the curve used for the given cipher, when applicable
  */
  val TlsCurve: AttributeKey[String] = string("tls.curve")

  /**
  * Boolean flag indicating if the TLS negotiation was successful and transitioned to an encrypted tunnel.
  */
  val TlsEstablished: AttributeKey[Boolean] = boolean("tls.established")

  /**
  * String indicating the protocol being tunneled. Per the values in the <a href="https://www.iana.org/assignments/tls-extensiontype-values/tls-extensiontype-values.xhtml#alpn-protocol-ids">IANA registry</a>, this string should be lower case.
  */
  val TlsNextProtocol: AttributeKey[String] = string("tls.next_protocol")

  /**
  * Normalized lowercase protocol name parsed from original string of the negotiated <a href="https://www.openssl.org/docs/man1.1.1/man3/SSL_get_version.html#RETURN-VALUES">SSL/TLS protocol version</a>
  */
  val TlsProtocolName: AttributeKey[String] = string("tls.protocol.name")

  /**
  * Numeric part of the version parsed from the original string of the negotiated <a href="https://www.openssl.org/docs/man1.1.1/man3/SSL_get_version.html#RETURN-VALUES">SSL/TLS protocol version</a>
  */
  val TlsProtocolVersion: AttributeKey[String] = string("tls.protocol.version")

  /**
  * Boolean flag indicating if this TLS connection was resumed from an existing TLS negotiation.
  */
  val TlsResumed: AttributeKey[Boolean] = boolean("tls.resumed")

  /**
  * PEM-encoded stand-alone certificate offered by the server. This is usually mutually-exclusive of `server.certificate_chain` since this value also exists in that list.
  */
  val TlsServerCertificate: AttributeKey[String] = string("tls.server.certificate")

  /**
  * Array of PEM-encoded certificates that make up the certificate chain offered by the server. This is usually mutually-exclusive of `server.certificate` since that value should be the first certificate in the chain.
  */
  val TlsServerCertificateChain: AttributeKey[Seq[String]] = stringSeq("tls.server.certificate_chain")

  /**
  * Certificate fingerprint using the MD5 digest of DER-encoded version of certificate offered by the server. For consistency with other hash values, this value should be formatted as an uppercase hash.
  */
  val TlsServerHashMd5: AttributeKey[String] = string("tls.server.hash.md5")

  /**
  * Certificate fingerprint using the SHA1 digest of DER-encoded version of certificate offered by the server. For consistency with other hash values, this value should be formatted as an uppercase hash.
  */
  val TlsServerHashSha1: AttributeKey[String] = string("tls.server.hash.sha1")

  /**
  * Certificate fingerprint using the SHA256 digest of DER-encoded version of certificate offered by the server. For consistency with other hash values, this value should be formatted as an uppercase hash.
  */
  val TlsServerHashSha256: AttributeKey[String] = string("tls.server.hash.sha256")

  /**
  * Distinguished name of <a href="https://datatracker.ietf.org/doc/html/rfc5280#section-4.1.2.6">subject</a> of the issuer of the x.509 certificate presented by the client.
  */
  val TlsServerIssuer: AttributeKey[String] = string("tls.server.issuer")

  /**
  * A hash that identifies servers based on how they perform an SSL/TLS handshake.
  */
  val TlsServerJa3s: AttributeKey[String] = string("tls.server.ja3s")

  /**
  * Date/Time indicating when server certificate is no longer considered valid.
  */
  val TlsServerNotAfter: AttributeKey[String] = string("tls.server.not_after")

  /**
  * Date/Time indicating when server certificate is first considered valid.
  */
  val TlsServerNotBefore: AttributeKey[String] = string("tls.server.not_before")

  /**
  * Distinguished name of subject of the x.509 certificate presented by the server.
  */
  val TlsServerSubject: AttributeKey[String] = string("tls.server.subject")

  /**
  * The <a href="https://www.rfc-editor.org/rfc/rfc3986#section-3.5">URI fragment</a> component
  */
  val UrlFragment: AttributeKey[String] = string("url.fragment")

  /**
  * Absolute URL describing a network resource according to <a href="https://www.rfc-editor.org/rfc/rfc3986">RFC3986</a>
  *
  * <p>Notes:
    <ul> <li>For network calls, URL usually has `scheme://host[:port][path][?query][#fragment]` format, where the fragment is not transmitted over HTTP, but if it is known, it SHOULD be included nevertheless.
`url.full` MUST NOT contain credentials passed via URL in form of `https://username:password@www.example.com/`. In such case username and password SHOULD be redacted and attribute's value SHOULD be `https://REDACTED:REDACTED@www.example.com/`.
`url.full` SHOULD capture the absolute URL when it is available (or can be reconstructed) and SHOULD NOT be validated or modified except for sanitizing purposes.</li> </ul>
  */
  val UrlFull: AttributeKey[String] = string("url.full")

  /**
  * The <a href="https://www.rfc-editor.org/rfc/rfc3986#section-3.3">URI path</a> component
  */
  val UrlPath: AttributeKey[String] = string("url.path")

  /**
  * The <a href="https://www.rfc-editor.org/rfc/rfc3986#section-3.4">URI query</a> component
  *
  * <p>Notes:
    <ul> <li>Sensitive content provided in query string SHOULD be scrubbed when instrumentations can identify it.</li> </ul>
  */
  val UrlQuery: AttributeKey[String] = string("url.query")

  /**
  * The <a href="https://www.rfc-editor.org/rfc/rfc3986#section-3.1">URI scheme</a> component identifying the used protocol.
  */
  val UrlScheme: AttributeKey[String] = string("url.scheme")

  /**
  * Value of the <a href="https://www.rfc-editor.org/rfc/rfc9110.html#field.user-agent">HTTP User-Agent</a> header sent by the client.
  */
  val UserAgentOriginal: AttributeKey[String] = string("user_agent.original")

  /**
  * A unique id to identify a session.
  */
  val SessionId: AttributeKey[String] = string("session.id")

  /**
  * The previous `session.id` for this user, when known.
  */
  val SessionPreviousId: AttributeKey[String] = string("session.previous_id")

  /**
  * The full invoked ARN as provided on the `Context` passed to the function (`Lambda-Runtime-Invoked-Function-Arn` header on the `/runtime/invocation/next` applicable).
  *
  * <p>Notes:
    <ul> <li>This may be different from `cloud.resource_id` if an alias is involved.</li> </ul>
  */
  val AwsLambdaInvokedArn: AttributeKey[String] = string("aws.lambda.invoked_arn")

  /**
  * The <a href="https://github.com/cloudevents/spec/blob/v1.0.2/cloudevents/spec.md#id">event_id</a> uniquely identifies the event.
  */
  val CloudeventsEventId: AttributeKey[String] = string("cloudevents.event_id")

  /**
  * The <a href="https://github.com/cloudevents/spec/blob/v1.0.2/cloudevents/spec.md#source-1">source</a> identifies the context in which an event happened.
  */
  val CloudeventsEventSource: AttributeKey[String] = string("cloudevents.event_source")

  /**
  * The <a href="https://github.com/cloudevents/spec/blob/v1.0.2/cloudevents/spec.md#specversion">version of the CloudEvents specification</a> which the event uses.
  */
  val CloudeventsEventSpecVersion: AttributeKey[String] = string("cloudevents.event_spec_version")

  /**
  * The <a href="https://github.com/cloudevents/spec/blob/v1.0.2/cloudevents/spec.md#subject">subject</a> of the event in the context of the event producer (identified by source).
  */
  val CloudeventsEventSubject: AttributeKey[String] = string("cloudevents.event_subject")

  /**
  * The <a href="https://github.com/cloudevents/spec/blob/v1.0.2/cloudevents/spec.md#type">event_type</a> contains a value describing the type of event related to the originating occurrence.
  */
  val CloudeventsEventType: AttributeKey[String] = string("cloudevents.event_type")

  /**
  * Parent-child Reference type
  *
  * <p>Notes:
    <ul> <li>The causal relationship between a child Span and a parent Span.</li> </ul>
  */
  val OpentracingRefType: AttributeKey[String] = string("opentracing.ref_type")

  /**
  * Name of the code, either &quot;OK&quot; or &quot;ERROR&quot;. MUST NOT be set if the status code is UNSET.
  */
  val OtelStatusCode: AttributeKey[String] = string("otel.status_code")

  /**
  * Description of the Status if it has a value, otherwise not set.
  */
  val OtelStatusDescription: AttributeKey[String] = string("otel.status_description")

  /**
  * The invocation ID of the current function invocation.
  */
  val FaasInvocationId: AttributeKey[String] = string("faas.invocation_id")

  /**
  * The name of the source on which the triggering operation was performed. For example, in Cloud Storage or S3 corresponds to the bucket name, and in Cosmos DB to the database name.
  */
  val FaasDocumentCollection: AttributeKey[String] = string("faas.document.collection")

  /**
  * The document name/table subjected to the operation. For example, in Cloud Storage or S3 is the name of the file, and in Cosmos DB the table name.
  */
  val FaasDocumentName: AttributeKey[String] = string("faas.document.name")

  /**
  * Describes the type of the operation that was performed on the data.
  */
  val FaasDocumentOperation: AttributeKey[String] = string("faas.document.operation")

  /**
  * A string containing the time when the data was accessed in the <a href="https://www.iso.org/iso-8601-date-and-time-format.html">ISO 8601</a> format expressed in <a href="https://www.w3.org/TR/NOTE-datetime">UTC</a>.
  */
  val FaasDocumentTime: AttributeKey[String] = string("faas.document.time")

  /**
  * A string containing the schedule period as <a href="https://docs.oracle.com/cd/E12058_01/doc/doc.1014/e12030/cron_expressions.htm">Cron Expression</a>.
  */
  val FaasCron: AttributeKey[String] = string("faas.cron")

  /**
  * A string containing the function invocation time in the <a href="https://www.iso.org/iso-8601-date-and-time-format.html">ISO 8601</a> format expressed in <a href="https://www.w3.org/TR/NOTE-datetime">UTC</a>.
  */
  val FaasTime: AttributeKey[String] = string("faas.time")

  /**
  * A boolean that is true if the serverless function is executed for the first time (aka cold-start).
  */
  val FaasColdstart: AttributeKey[Boolean] = boolean("faas.coldstart")

  /**
  * The unique identifier of the feature flag.
  */
  val FeatureFlagKey: AttributeKey[String] = string("feature_flag.key")

  /**
  * The name of the service provider that performs the flag evaluation.
  */
  val FeatureFlagProviderName: AttributeKey[String] = string("feature_flag.provider_name")

  /**
  * SHOULD be a semantic identifier for a value. If one is unavailable, a stringified version of the value can be used.
  *
  * <p>Notes:
    <ul> <li>A semantic identifier, commonly referred to as a variant, provides a means
for referring to a value without including the value itself. This can
provide additional context for understanding the meaning behind a value.
For example, the variant `red` maybe be used for the value `#c05543`.</li><li>A stringified version of the value can be used in situations where a
semantic identifier is unavailable. String representation of the value
should be determined by the implementer.</li> </ul>
  */
  val FeatureFlagVariant: AttributeKey[String] = string("feature_flag.variant")

  /**
  * The AWS request ID as returned in the response headers `x-amz-request-id` or `x-amz-requestid`.
  */
  val AwsRequestId: AttributeKey[String] = string("aws.request_id")

  /**
  * The value of the `AttributesToGet` request parameter.
  */
  val AwsDynamodbAttributesToGet: AttributeKey[Seq[String]] = stringSeq("aws.dynamodb.attributes_to_get")

  /**
  * The value of the `ConsistentRead` request parameter.
  */
  val AwsDynamodbConsistentRead: AttributeKey[Boolean] = boolean("aws.dynamodb.consistent_read")

  /**
  * The JSON-serialized value of each item in the `ConsumedCapacity` response field.
  */
  val AwsDynamodbConsumedCapacity: AttributeKey[Seq[String]] = stringSeq("aws.dynamodb.consumed_capacity")

  /**
  * The value of the `IndexName` request parameter.
  */
  val AwsDynamodbIndexName: AttributeKey[String] = string("aws.dynamodb.index_name")

  /**
  * The JSON-serialized value of the `ItemCollectionMetrics` response field.
  */
  val AwsDynamodbItemCollectionMetrics: AttributeKey[String] = string("aws.dynamodb.item_collection_metrics")

  /**
  * The value of the `Limit` request parameter.
  */
  val AwsDynamodbLimit: AttributeKey[Long] = long("aws.dynamodb.limit")

  /**
  * The value of the `ProjectionExpression` request parameter.
  */
  val AwsDynamodbProjection: AttributeKey[String] = string("aws.dynamodb.projection")

  /**
  * The value of the `ProvisionedThroughput.ReadCapacityUnits` request parameter.
  */
  val AwsDynamodbProvisionedReadCapacity: AttributeKey[Double] = double("aws.dynamodb.provisioned_read_capacity")

  /**
  * The value of the `ProvisionedThroughput.WriteCapacityUnits` request parameter.
  */
  val AwsDynamodbProvisionedWriteCapacity: AttributeKey[Double] = double("aws.dynamodb.provisioned_write_capacity")

  /**
  * The value of the `Select` request parameter.
  */
  val AwsDynamodbSelect: AttributeKey[String] = string("aws.dynamodb.select")

  /**
  * The keys in the `RequestItems` object field.
  */
  val AwsDynamodbTableNames: AttributeKey[Seq[String]] = stringSeq("aws.dynamodb.table_names")

  /**
  * The JSON-serialized value of each item of the `GlobalSecondaryIndexes` request field
  */
  val AwsDynamodbGlobalSecondaryIndexes: AttributeKey[Seq[String]] = stringSeq("aws.dynamodb.global_secondary_indexes")

  /**
  * The JSON-serialized value of each item of the `LocalSecondaryIndexes` request field.
  */
  val AwsDynamodbLocalSecondaryIndexes: AttributeKey[Seq[String]] = stringSeq("aws.dynamodb.local_secondary_indexes")

  /**
  * The value of the `ExclusiveStartTableName` request parameter.
  */
  val AwsDynamodbExclusiveStartTable: AttributeKey[String] = string("aws.dynamodb.exclusive_start_table")

  /**
  * The the number of items in the `TableNames` response parameter.
  */
  val AwsDynamodbTableCount: AttributeKey[Long] = long("aws.dynamodb.table_count")

  /**
  * The value of the `ScanIndexForward` request parameter.
  */
  val AwsDynamodbScanForward: AttributeKey[Boolean] = boolean("aws.dynamodb.scan_forward")

  /**
  * The value of the `Count` response parameter.
  */
  val AwsDynamodbCount: AttributeKey[Long] = long("aws.dynamodb.count")

  /**
  * The value of the `ScannedCount` response parameter.
  */
  val AwsDynamodbScannedCount: AttributeKey[Long] = long("aws.dynamodb.scanned_count")

  /**
  * The value of the `Segment` request parameter.
  */
  val AwsDynamodbSegment: AttributeKey[Long] = long("aws.dynamodb.segment")

  /**
  * The value of the `TotalSegments` request parameter.
  */
  val AwsDynamodbTotalSegments: AttributeKey[Long] = long("aws.dynamodb.total_segments")

  /**
  * The JSON-serialized value of each item in the `AttributeDefinitions` request field.
  */
  val AwsDynamodbAttributeDefinitions: AttributeKey[Seq[String]] = stringSeq("aws.dynamodb.attribute_definitions")

  /**
  * The JSON-serialized value of each item in the the `GlobalSecondaryIndexUpdates` request field.
  */
  val AwsDynamodbGlobalSecondaryIndexUpdates: AttributeKey[Seq[String]] = stringSeq("aws.dynamodb.global_secondary_index_updates")

  /**
  * The S3 bucket name the request refers to. Corresponds to the `--bucket` parameter of the <a href="https://docs.aws.amazon.com/cli/latest/reference/s3api/index.html">S3 API</a> operations.
  *
  * <p>Notes:
    <ul> <li>The `bucket` attribute is applicable to all S3 operations that reference a bucket, i.e. that require the bucket name as a mandatory parameter.
This applies to almost all S3 operations except `list-buckets`.</li> </ul>
  */
  val AwsS3Bucket: AttributeKey[String] = string("aws.s3.bucket")

  /**
  * The source object (in the form `bucket`/`key`) for the copy operation.
  *
  * <p>Notes:
    <ul> <li>The `copy_source` attribute applies to S3 copy operations and corresponds to the `--copy-source` parameter
of the <a href="https://docs.aws.amazon.com/cli/latest/reference/s3api/copy-object.html">copy-object operation within the S3 API</a>.
This applies in particular to the following operations:</li><li><a href="https://docs.aws.amazon.com/cli/latest/reference/s3api/copy-object.html">copy-object</a></li>
<li><a href="https://docs.aws.amazon.com/cli/latest/reference/s3api/upload-part-copy.html">upload-part-copy</a></li>
 </ul>
  */
  val AwsS3CopySource: AttributeKey[String] = string("aws.s3.copy_source")

  /**
  * The delete request container that specifies the objects to be deleted.
  *
  * <p>Notes:
    <ul> <li>The `delete` attribute is only applicable to the <a href="https://docs.aws.amazon.com/cli/latest/reference/s3api/delete-object.html">delete-object</a> operation.
The `delete` attribute corresponds to the `--delete` parameter of the
<a href="https://docs.aws.amazon.com/cli/latest/reference/s3api/delete-objects.html">delete-objects operation within the S3 API</a>.</li> </ul>
  */
  val AwsS3Delete: AttributeKey[String] = string("aws.s3.delete")

  /**
  * The S3 object key the request refers to. Corresponds to the `--key` parameter of the <a href="https://docs.aws.amazon.com/cli/latest/reference/s3api/index.html">S3 API</a> operations.
  *
  * <p>Notes:
    <ul> <li>The `key` attribute is applicable to all object-related S3 operations, i.e. that require the object key as a mandatory parameter.
This applies in particular to the following operations:</li><li><a href="https://docs.aws.amazon.com/cli/latest/reference/s3api/copy-object.html">copy-object</a></li>
<li><a href="https://docs.aws.amazon.com/cli/latest/reference/s3api/delete-object.html">delete-object</a></li>
<li><a href="https://docs.aws.amazon.com/cli/latest/reference/s3api/get-object.html">get-object</a></li>
<li><a href="https://docs.aws.amazon.com/cli/latest/reference/s3api/head-object.html">head-object</a></li>
<li><a href="https://docs.aws.amazon.com/cli/latest/reference/s3api/put-object.html">put-object</a></li>
<li><a href="https://docs.aws.amazon.com/cli/latest/reference/s3api/restore-object.html">restore-object</a></li>
<li><a href="https://docs.aws.amazon.com/cli/latest/reference/s3api/select-object-content.html">select-object-content</a></li>
<li><a href="https://docs.aws.amazon.com/cli/latest/reference/s3api/abort-multipart-upload.html">abort-multipart-upload</a></li>
<li><a href="https://docs.aws.amazon.com/cli/latest/reference/s3api/complete-multipart-upload.html">complete-multipart-upload</a></li>
<li><a href="https://docs.aws.amazon.com/cli/latest/reference/s3api/create-multipart-upload.html">create-multipart-upload</a></li>
<li><a href="https://docs.aws.amazon.com/cli/latest/reference/s3api/list-parts.html">list-parts</a></li>
<li><a href="https://docs.aws.amazon.com/cli/latest/reference/s3api/upload-part.html">upload-part</a></li>
<li><a href="https://docs.aws.amazon.com/cli/latest/reference/s3api/upload-part-copy.html">upload-part-copy</a></li>
 </ul>
  */
  val AwsS3Key: AttributeKey[String] = string("aws.s3.key")

  /**
  * The part number of the part being uploaded in a multipart-upload operation. This is a positive integer between 1 and 10,000.
  *
  * <p>Notes:
    <ul> <li>The `part_number` attribute is only applicable to the <a href="https://docs.aws.amazon.com/cli/latest/reference/s3api/upload-part.html">upload-part</a>
and <a href="https://docs.aws.amazon.com/cli/latest/reference/s3api/upload-part-copy.html">upload-part-copy</a> operations.
The `part_number` attribute corresponds to the `--part-number` parameter of the
<a href="https://docs.aws.amazon.com/cli/latest/reference/s3api/upload-part.html">upload-part operation within the S3 API</a>.</li> </ul>
  */
  val AwsS3PartNumber: AttributeKey[Long] = long("aws.s3.part_number")

  /**
  * Upload ID that identifies the multipart upload.
  *
  * <p>Notes:
    <ul> <li>The `upload_id` attribute applies to S3 multipart-upload operations and corresponds to the `--upload-id` parameter
of the <a href="https://docs.aws.amazon.com/cli/latest/reference/s3api/index.html">S3 API</a> multipart operations.
This applies in particular to the following operations:</li><li><a href="https://docs.aws.amazon.com/cli/latest/reference/s3api/abort-multipart-upload.html">abort-multipart-upload</a></li>
<li><a href="https://docs.aws.amazon.com/cli/latest/reference/s3api/complete-multipart-upload.html">complete-multipart-upload</a></li>
<li><a href="https://docs.aws.amazon.com/cli/latest/reference/s3api/list-parts.html">list-parts</a></li>
<li><a href="https://docs.aws.amazon.com/cli/latest/reference/s3api/upload-part.html">upload-part</a></li>
<li><a href="https://docs.aws.amazon.com/cli/latest/reference/s3api/upload-part-copy.html">upload-part-copy</a></li>
 </ul>
  */
  val AwsS3UploadId: AttributeKey[String] = string("aws.s3.upload_id")

  /**
  * The GraphQL document being executed.
  *
  * <p>Notes:
    <ul> <li>The value may be sanitized to exclude sensitive information.</li> </ul>
  */
  val GraphqlDocument: AttributeKey[String] = string("graphql.document")

  /**
  * The name of the operation being executed.
  */
  val GraphqlOperationName: AttributeKey[String] = string("graphql.operation.name")

  /**
  * The type of the operation being executed.
  */
  val GraphqlOperationType: AttributeKey[String] = string("graphql.operation.type")

  /**
  * Compressed size of the message in bytes.
  */
  val MessageCompressedSize: AttributeKey[Long] = long("message.compressed_size")

  /**
  * MUST be calculated as two different counters starting from `1` one for sent messages and one for received message.
  *
  * <p>Notes:
    <ul> <li>This way we guarantee that the values will be consistent between different implementations.</li> </ul>
  */
  val MessageId: AttributeKey[Long] = long("message.id")

  /**
  * Whether this is a received or sent message.
  */
  val MessageType: AttributeKey[String] = string("message.type")

  /**
  * Uncompressed size of the message in bytes.
  */
  val MessageUncompressedSize: AttributeKey[Long] = long("message.uncompressed_size")

  // Enum definitions
  abstract class FaasInvokedProviderValue(val value: String)
  object FaasInvokedProviderValue {
      /** Alibaba Cloud. */
      case object AlibabaCloud extends FaasInvokedProviderValue("alibaba_cloud")
      /** Amazon Web Services. */
      case object Aws extends FaasInvokedProviderValue("aws")
      /** Microsoft Azure. */
      case object Azure extends FaasInvokedProviderValue("azure")
      /** Google Cloud Platform. */
      case object Gcp extends FaasInvokedProviderValue("gcp")
      /** Tencent Cloud. */
      case object TencentCloud extends FaasInvokedProviderValue("tencent_cloud")

  }

  
  abstract class FaasTriggerValue(val value: String)
  object FaasTriggerValue {
      /** A response to some data source operation such as a database or filesystem read/write. */
      case object Datasource extends FaasTriggerValue("datasource")
      /** To provide an answer to an inbound HTTP request. */
      case object Http extends FaasTriggerValue("http")
      /** A function is set to be executed when messages are sent to a messaging system. */
      case object Pubsub extends FaasTriggerValue("pubsub")
      /** A function is scheduled to be executed regularly. */
      case object Timer extends FaasTriggerValue("timer")
      /** If none of the others apply. */
      case object Other extends FaasTriggerValue("other")

  }

  
  abstract class LogIostreamValue(val value: String)
  object LogIostreamValue {
      /** Logs from stdout stream. */
      case object Stdout extends LogIostreamValue("stdout")
      /** Events from stderr stream. */
      case object Stderr extends LogIostreamValue("stderr")

  }

  
  abstract class IosStateValue(val value: String)
  object IosStateValue {
      /** The app has become `active`. Associated with UIKit notification `applicationDidBecomeActive`. */
      case object Active extends IosStateValue("active")
      /** The app is now `inactive`. Associated with UIKit notification `applicationWillResignActive`. */
      case object Inactive extends IosStateValue("inactive")
      /** The app is now in the background. This value is associated with UIKit notification `applicationDidEnterBackground`. */
      case object Background extends IosStateValue("background")
      /** The app is now in the foreground. This value is associated with UIKit notification `applicationWillEnterForeground`. */
      case object Foreground extends IosStateValue("foreground")
      /** The app is about to terminate. Associated with UIKit notification `applicationWillTerminate`. */
      case object Terminate extends IosStateValue("terminate")

  }

  
  abstract class AndroidStateValue(val value: String)
  object AndroidStateValue {
      /** Any time before Activity.onResume() or, if the app has no Activity, Context.startService() has been called in the app for the first time. */
      case object Created extends AndroidStateValue("created")
      /** Any time after Activity.onPause() or, if the app has no Activity, Context.stopService() has been called when the app was in the foreground state. */
      case object Background extends AndroidStateValue("background")
      /** Any time after Activity.onResume() or, if the app has no Activity, Context.startService() has been called when the app was in either the created or background states. */
      case object Foreground extends AndroidStateValue("foreground")

  }

  
  abstract class StateValue(val value: String)
  object StateValue {
      /** idle. */
      case object Idle extends StateValue("idle")
      /** used. */
      case object Used extends StateValue("used")

  }

  
  abstract class AspnetcoreRateLimitingResultValue(val value: String)
  object AspnetcoreRateLimitingResultValue {
      /** Lease was acquired. */
      case object Acquired extends AspnetcoreRateLimitingResultValue("acquired")
      /** Lease request was rejected by the endpoint limiter. */
      case object EndpointLimiter extends AspnetcoreRateLimitingResultValue("endpoint_limiter")
      /** Lease request was rejected by the global limiter. */
      case object GlobalLimiter extends AspnetcoreRateLimitingResultValue("global_limiter")
      /** Lease request was canceled. */
      case object RequestCanceled extends AspnetcoreRateLimitingResultValue("request_canceled")

  }

  
  abstract class SignalrConnectionStatusValue(val value: String)
  object SignalrConnectionStatusValue {
      /** The connection was closed normally. */
      case object NormalClosure extends SignalrConnectionStatusValue("normal_closure")
      /** The connection was closed due to a timeout. */
      case object Timeout extends SignalrConnectionStatusValue("timeout")
      /** The connection was closed because the app is shutting down. */
      case object AppShutdown extends SignalrConnectionStatusValue("app_shutdown")

  }

  
  abstract class SignalrTransportValue(val value: String)
  object SignalrTransportValue {
      /** ServerSentEvents protocol. */
      case object ServerSentEvents extends SignalrTransportValue("server_sent_events")
      /** LongPolling protocol. */
      case object LongPolling extends SignalrTransportValue("long_polling")
      /** WebSockets protocol. */
      case object WebSockets extends SignalrTransportValue("web_sockets")

  }

  
  abstract class JvmMemoryTypeValue(val value: String)
  object JvmMemoryTypeValue {
      /** Heap memory. */
      case object Heap extends JvmMemoryTypeValue("heap")
      /** Non-heap memory. */
      case object NonHeap extends JvmMemoryTypeValue("non_heap")

  }

  
  abstract class SystemCpuStateValue(val value: String)
  object SystemCpuStateValue {
      /** user. */
      case object User extends SystemCpuStateValue("user")
      /** system. */
      case object System extends SystemCpuStateValue("system")
      /** nice. */
      case object Nice extends SystemCpuStateValue("nice")
      /** idle. */
      case object Idle extends SystemCpuStateValue("idle")
      /** iowait. */
      case object Iowait extends SystemCpuStateValue("iowait")
      /** interrupt. */
      case object Interrupt extends SystemCpuStateValue("interrupt")
      /** steal. */
      case object Steal extends SystemCpuStateValue("steal")

  }

  
  abstract class SystemMemoryStateValue(val value: String)
  object SystemMemoryStateValue {
      /** used. */
      case object Used extends SystemMemoryStateValue("used")
      /** free. */
      case object Free extends SystemMemoryStateValue("free")
      /** shared. */
      case object Shared extends SystemMemoryStateValue("shared")
      /** buffers. */
      case object Buffers extends SystemMemoryStateValue("buffers")
      /** cached. */
      case object Cached extends SystemMemoryStateValue("cached")

    /**
     * total.
     *
     * @deprecated this value has been removed as of 1.23.1 of the semantic conventions.
     */
    @deprecated("The item has been removed", "0.4.0")
    case object Total extends SystemMemoryStateValue("total")

    

  }

  
  abstract class SystemPagingDirectionValue(val value: String)
  object SystemPagingDirectionValue {
      /** in. */
      case object In extends SystemPagingDirectionValue("in")
      /** out. */
      case object Out extends SystemPagingDirectionValue("out")

  }

  
  abstract class SystemPagingStateValue(val value: String)
  object SystemPagingStateValue {
      /** used. */
      case object Used extends SystemPagingStateValue("used")
      /** free. */
      case object Free extends SystemPagingStateValue("free")

  }

  
  abstract class SystemPagingTypeValue(val value: String)
  object SystemPagingTypeValue {
      /** major. */
      case object Major extends SystemPagingTypeValue("major")
      /** minor. */
      case object Minor extends SystemPagingTypeValue("minor")

  }

  
  abstract class SystemFilesystemStateValue(val value: String)
  object SystemFilesystemStateValue {
      /** used. */
      case object Used extends SystemFilesystemStateValue("used")
      /** free. */
      case object Free extends SystemFilesystemStateValue("free")
      /** reserved. */
      case object Reserved extends SystemFilesystemStateValue("reserved")

  }

  
  abstract class SystemFilesystemTypeValue(val value: String)
  object SystemFilesystemTypeValue {
      /** fat32. */
      case object Fat32 extends SystemFilesystemTypeValue("fat32")
      /** exfat. */
      case object Exfat extends SystemFilesystemTypeValue("exfat")
      /** ntfs. */
      case object Ntfs extends SystemFilesystemTypeValue("ntfs")
      /** refs. */
      case object Refs extends SystemFilesystemTypeValue("refs")
      /** hfsplus. */
      case object Hfsplus extends SystemFilesystemTypeValue("hfsplus")
      /** ext4. */
      case object Ext4 extends SystemFilesystemTypeValue("ext4")

  }

  
  abstract class SystemNetworkStateValue(val value: String)
  object SystemNetworkStateValue {
      /** close. */
      case object Close extends SystemNetworkStateValue("close")
      /** close_wait. */
      case object CloseWait extends SystemNetworkStateValue("close_wait")
      /** closing. */
      case object Closing extends SystemNetworkStateValue("closing")
      /** delete. */
      case object Delete extends SystemNetworkStateValue("delete")
      /** established. */
      case object Established extends SystemNetworkStateValue("established")
      /** fin_wait_1. */
      case object FinWait1 extends SystemNetworkStateValue("fin_wait_1")
      /** fin_wait_2. */
      case object FinWait2 extends SystemNetworkStateValue("fin_wait_2")
      /** last_ack. */
      case object LastAck extends SystemNetworkStateValue("last_ack")
      /** listen. */
      case object Listen extends SystemNetworkStateValue("listen")
      /** syn_recv. */
      case object SynRecv extends SystemNetworkStateValue("syn_recv")
      /** syn_sent. */
      case object SynSent extends SystemNetworkStateValue("syn_sent")
      /** time_wait. */
      case object TimeWait extends SystemNetworkStateValue("time_wait")

  }

  
  abstract class SystemProcessesStatusValue(val value: String)
  object SystemProcessesStatusValue {
      /** running. */
      case object Running extends SystemProcessesStatusValue("running")
      /** sleeping. */
      case object Sleeping extends SystemProcessesStatusValue("sleeping")
      /** stopped. */
      case object Stopped extends SystemProcessesStatusValue("stopped")
      /** defunct. */
      case object Defunct extends SystemProcessesStatusValue("defunct")

  }

  
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

  
  abstract class DbCosmosdbConnectionModeValue(val value: String)
  object DbCosmosdbConnectionModeValue {
      /** Gateway (HTTP) connections mode. */
      case object Gateway extends DbCosmosdbConnectionModeValue("gateway")
      /** Direct connection. */
      case object Direct extends DbCosmosdbConnectionModeValue("direct")

  }

  
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

  
  abstract class DbSystemValue(val value: String)
  object DbSystemValue {
      /** Some other SQL database. Fallback only. See notes. */
      case object OtherSql extends DbSystemValue("other_sql")
      /** Microsoft SQL Server. */
      case object Mssql extends DbSystemValue("mssql")
      /** Microsoft SQL Server Compact. */
      case object Mssqlcompact extends DbSystemValue("mssqlcompact")
      /** MySQL. */
      case object Mysql extends DbSystemValue("mysql")
      /** Oracle Database. */
      case object Oracle extends DbSystemValue("oracle")
      /** IBM Db2. */
      case object Db2 extends DbSystemValue("db2")
      /** PostgreSQL. */
      case object Postgresql extends DbSystemValue("postgresql")
      /** Amazon Redshift. */
      case object Redshift extends DbSystemValue("redshift")
      /** Apache Hive. */
      case object Hive extends DbSystemValue("hive")
      /** Cloudscape. */
      case object Cloudscape extends DbSystemValue("cloudscape")
      /** HyperSQL DataBase. */
      case object Hsqldb extends DbSystemValue("hsqldb")
      /** Progress Database. */
      case object Progress extends DbSystemValue("progress")
      /** SAP MaxDB. */
      case object Maxdb extends DbSystemValue("maxdb")
      /** SAP HANA. */
      case object Hanadb extends DbSystemValue("hanadb")
      /** Ingres. */
      case object Ingres extends DbSystemValue("ingres")
      /** FirstSQL. */
      case object Firstsql extends DbSystemValue("firstsql")
      /** EnterpriseDB. */
      case object Edb extends DbSystemValue("edb")
      /** InterSystems Caché. */
      case object Cache extends DbSystemValue("cache")
      /** Adabas (Adaptable Database System). */
      case object Adabas extends DbSystemValue("adabas")
      /** Firebird. */
      case object Firebird extends DbSystemValue("firebird")
      /** Apache Derby. */
      case object Derby extends DbSystemValue("derby")
      /** FileMaker. */
      case object Filemaker extends DbSystemValue("filemaker")
      /** Informix. */
      case object Informix extends DbSystemValue("informix")
      /** InstantDB. */
      case object Instantdb extends DbSystemValue("instantdb")
      /** InterBase. */
      case object Interbase extends DbSystemValue("interbase")
      /** MariaDB. */
      case object Mariadb extends DbSystemValue("mariadb")
      /** Netezza. */
      case object Netezza extends DbSystemValue("netezza")
      /** Pervasive PSQL. */
      case object Pervasive extends DbSystemValue("pervasive")
      /** PointBase. */
      case object Pointbase extends DbSystemValue("pointbase")
      /** SQLite. */
      case object Sqlite extends DbSystemValue("sqlite")
      /** Sybase. */
      case object Sybase extends DbSystemValue("sybase")
      /** Teradata. */
      case object Teradata extends DbSystemValue("teradata")
      /** Vertica. */
      case object Vertica extends DbSystemValue("vertica")
      /** H2. */
      case object H2 extends DbSystemValue("h2")
      /** ColdFusion IMQ. */
      case object Coldfusion extends DbSystemValue("coldfusion")
      /** Apache Cassandra. */
      case object Cassandra extends DbSystemValue("cassandra")
      /** Apache HBase. */
      case object Hbase extends DbSystemValue("hbase")
      /** MongoDB. */
      case object Mongodb extends DbSystemValue("mongodb")
      /** Redis. */
      case object Redis extends DbSystemValue("redis")
      /** Couchbase. */
      case object Couchbase extends DbSystemValue("couchbase")
      /** CouchDB. */
      case object Couchdb extends DbSystemValue("couchdb")
      /** Microsoft Azure Cosmos DB. */
      case object Cosmosdb extends DbSystemValue("cosmosdb")
      /** Amazon DynamoDB. */
      case object Dynamodb extends DbSystemValue("dynamodb")
      /** Neo4j. */
      case object Neo4j extends DbSystemValue("neo4j")
      /** Apache Geode. */
      case object Geode extends DbSystemValue("geode")
      /** Elasticsearch. */
      case object Elasticsearch extends DbSystemValue("elasticsearch")
      /** Memcached. */
      case object Memcached extends DbSystemValue("memcached")
      /** CockroachDB. */
      case object Cockroachdb extends DbSystemValue("cockroachdb")
      /** OpenSearch. */
      case object Opensearch extends DbSystemValue("opensearch")
      /** ClickHouse. */
      case object Clickhouse extends DbSystemValue("clickhouse")
      /** Cloud Spanner. */
      case object Spanner extends DbSystemValue("spanner")
      /** Trino. */
      case object Trino extends DbSystemValue("trino")

  }

  
  abstract class HttpFlavorValue(val value: String)
  object HttpFlavorValue {
      /** HTTP/1.0. */
      case object Http10 extends HttpFlavorValue("1.0")
      /** HTTP/1.1. */
      case object Http11 extends HttpFlavorValue("1.1")
      /** HTTP/2. */
      case object Http20 extends HttpFlavorValue("2.0")
      /** HTTP/3. */
      case object Http30 extends HttpFlavorValue("3.0")
      /** SPDY protocol. */
      case object Spdy extends HttpFlavorValue("SPDY")
      /** QUIC protocol. */
      case object Quic extends HttpFlavorValue("QUIC")

  }

  
  abstract class NetSockFamilyValue(val value: String)
  object NetSockFamilyValue {
      /** IPv4 address. */
      case object Inet extends NetSockFamilyValue("inet")
      /** IPv6 address. */
      case object Inet6 extends NetSockFamilyValue("inet6")
      /** Unix domain socket path. */
      case object Unix extends NetSockFamilyValue("unix")

  }

  
  abstract class NetTransportValue(val value: String)
  object NetTransportValue {
      /** ip_tcp. */
      case object IpTcp extends NetTransportValue("ip_tcp")
      /** ip_udp. */
      case object IpUdp extends NetTransportValue("ip_udp")
      /** Named or anonymous pipe. */
      case object Pipe extends NetTransportValue("pipe")
      /** In-process communication. */
      case object Inproc extends NetTransportValue("inproc")
      /** Something else (non IP-based). */
      case object Other extends NetTransportValue("other")
    /** @deprecated This item has been removed as of 1.13.0 of the semantic conventions. */
    @deprecated("This item has been removed", "0.3.0")
    case object Ip extends NetTransportValue("ip")
    /** @deprecated This item has been removed as of 1.13.0 of the semantic conventions. */
    @deprecated("This item has been removed", "0.3.0")
    case object Unix extends NetTransportValue("unix")

  }

  
  abstract class DiskIoDirectionValue(val value: String)
  object DiskIoDirectionValue {
      /** read. */
      case object Read extends DiskIoDirectionValue("read")
      /** write. */
      case object Write extends DiskIoDirectionValue("write")

  }

  
  abstract class ErrorTypeValue(val value: String)
  object ErrorTypeValue {
      /** A fallback error value to be used when the instrumentation doesn&#39;t define a custom value. */
      case object Other extends ErrorTypeValue("_OTHER")

  }

  
  abstract class HttpRequestMethodValue(val value: String)
  object HttpRequestMethodValue {
      /** CONNECT method. */
      case object Connect extends HttpRequestMethodValue("CONNECT")
      /** DELETE method. */
      case object Delete extends HttpRequestMethodValue("DELETE")
      /** GET method. */
      case object Get extends HttpRequestMethodValue("GET")
      /** HEAD method. */
      case object Head extends HttpRequestMethodValue("HEAD")
      /** OPTIONS method. */
      case object Options extends HttpRequestMethodValue("OPTIONS")
      /** PATCH method. */
      case object Patch extends HttpRequestMethodValue("PATCH")
      /** POST method. */
      case object Post extends HttpRequestMethodValue("POST")
      /** PUT method. */
      case object Put extends HttpRequestMethodValue("PUT")
      /** TRACE method. */
      case object Trace extends HttpRequestMethodValue("TRACE")
      /** Any HTTP method that the instrumentation has no prior knowledge of. */
      case object Other extends HttpRequestMethodValue("_OTHER")

  }

  
  abstract class MessagingOperationValue(val value: String)
  object MessagingOperationValue {
      /** One or more messages are provided for publishing to an intermediary. If a single message is published, the context of the &#34;Publish&#34; span can be used as the creation context and no &#34;Create&#34; span needs to be created. */
      case object Publish extends MessagingOperationValue("publish")
      /** A message is created. &#34;Create&#34; spans always refer to a single message and are used to provide a unique creation context for messages in batch publishing scenarios. */
      case object Create extends MessagingOperationValue("create")
      /** One or more messages are requested by a consumer. This operation refers to pull-based scenarios, where consumers explicitly call methods of messaging SDKs to receive messages. */
      case object Receive extends MessagingOperationValue("receive")
      /** One or more messages are passed to a consumer. This operation refers to push-based scenarios, where consumer register callbacks which get called by messaging SDKs. */
      case object Deliver extends MessagingOperationValue("deliver")
    /**
     * process.
     *
     * @deprecated this value has been removed as of 1.23.1 of the semantic conventions.
     */
    @deprecated("The item has been removed", "0.4.0")
    case object Process extends MessagingOperationValue("process")

    

  }

  
  abstract class MessagingRocketmqConsumptionModelValue(val value: String)
  object MessagingRocketmqConsumptionModelValue {
      /** Clustering consumption model. */
      case object Clustering extends MessagingRocketmqConsumptionModelValue("clustering")
      /** Broadcasting consumption model. */
      case object Broadcasting extends MessagingRocketmqConsumptionModelValue("broadcasting")

  }

  
  abstract class MessagingRocketmqMessageTypeValue(val value: String)
  object MessagingRocketmqMessageTypeValue {
      /** Normal message. */
      case object Normal extends MessagingRocketmqMessageTypeValue("normal")
      /** FIFO message. */
      case object Fifo extends MessagingRocketmqMessageTypeValue("fifo")
      /** Delay message. */
      case object Delay extends MessagingRocketmqMessageTypeValue("delay")
      /** Transaction message. */
      case object Transaction extends MessagingRocketmqMessageTypeValue("transaction")

  }

  
  abstract class MessagingSystemValue(val value: String)
  object MessagingSystemValue {
      /** Apache ActiveMQ. */
      case object Activemq extends MessagingSystemValue("activemq")
      /** Amazon Simple Queue Service (SQS). */
      case object AwsSqs extends MessagingSystemValue("aws_sqs")
      /** Azure Event Grid. */
      case object AzureEventgrid extends MessagingSystemValue("azure_eventgrid")
      /** Azure Event Hubs. */
      case object AzureEventhubs extends MessagingSystemValue("azure_eventhubs")
      /** Azure Service Bus. */
      case object AzureServicebus extends MessagingSystemValue("azure_servicebus")
      /** Google Cloud Pub/Sub. */
      case object GcpPubsub extends MessagingSystemValue("gcp_pubsub")
      /** Java Message Service. */
      case object Jms extends MessagingSystemValue("jms")
      /** Apache Kafka. */
      case object Kafka extends MessagingSystemValue("kafka")
      /** RabbitMQ. */
      case object Rabbitmq extends MessagingSystemValue("rabbitmq")
      /** Apache RocketMQ. */
      case object Rocketmq extends MessagingSystemValue("rocketmq")

  }

  
  abstract class NetworkConnectionSubtypeValue(val value: String)
  object NetworkConnectionSubtypeValue {
      /** GPRS. */
      case object Gprs extends NetworkConnectionSubtypeValue("gprs")
      /** EDGE. */
      case object Edge extends NetworkConnectionSubtypeValue("edge")
      /** UMTS. */
      case object Umts extends NetworkConnectionSubtypeValue("umts")
      /** CDMA. */
      case object Cdma extends NetworkConnectionSubtypeValue("cdma")
      /** EVDO Rel. 0. */
      case object Evdo0 extends NetworkConnectionSubtypeValue("evdo_0")
      /** EVDO Rev. A. */
      case object EvdoA extends NetworkConnectionSubtypeValue("evdo_a")
      /** CDMA2000 1XRTT. */
      case object Cdma20001xrtt extends NetworkConnectionSubtypeValue("cdma2000_1xrtt")
      /** HSDPA. */
      case object Hsdpa extends NetworkConnectionSubtypeValue("hsdpa")
      /** HSUPA. */
      case object Hsupa extends NetworkConnectionSubtypeValue("hsupa")
      /** HSPA. */
      case object Hspa extends NetworkConnectionSubtypeValue("hspa")
      /** IDEN. */
      case object Iden extends NetworkConnectionSubtypeValue("iden")
      /** EVDO Rev. B. */
      case object EvdoB extends NetworkConnectionSubtypeValue("evdo_b")
      /** LTE. */
      case object Lte extends NetworkConnectionSubtypeValue("lte")
      /** EHRPD. */
      case object Ehrpd extends NetworkConnectionSubtypeValue("ehrpd")
      /** HSPAP. */
      case object Hspap extends NetworkConnectionSubtypeValue("hspap")
      /** GSM. */
      case object Gsm extends NetworkConnectionSubtypeValue("gsm")
      /** TD-SCDMA. */
      case object TdScdma extends NetworkConnectionSubtypeValue("td_scdma")
      /** IWLAN. */
      case object Iwlan extends NetworkConnectionSubtypeValue("iwlan")
      /** 5G NR (New Radio). */
      case object Nr extends NetworkConnectionSubtypeValue("nr")
      /** 5G NRNSA (New Radio Non-Standalone). */
      case object Nrnsa extends NetworkConnectionSubtypeValue("nrnsa")
      /** LTE CA. */
      case object LteCa extends NetworkConnectionSubtypeValue("lte_ca")

  }

  
  abstract class NetworkConnectionTypeValue(val value: String)
  object NetworkConnectionTypeValue {
      /** wifi. */
      case object Wifi extends NetworkConnectionTypeValue("wifi")
      /** wired. */
      case object Wired extends NetworkConnectionTypeValue("wired")
      /** cell. */
      case object Cell extends NetworkConnectionTypeValue("cell")
      /** unavailable. */
      case object Unavailable extends NetworkConnectionTypeValue("unavailable")
      /** unknown. */
      case object Unknown extends NetworkConnectionTypeValue("unknown")

  }

  
  abstract class NetworkIoDirectionValue(val value: String)
  object NetworkIoDirectionValue {
      /** transmit. */
      case object Transmit extends NetworkIoDirectionValue("transmit")
      /** receive. */
      case object Receive extends NetworkIoDirectionValue("receive")

  }

  
  abstract class NetworkTransportValue(val value: String)
  object NetworkTransportValue {
      /** TCP. */
      case object Tcp extends NetworkTransportValue("tcp")
      /** UDP. */
      case object Udp extends NetworkTransportValue("udp")
      /** Named or anonymous pipe. */
      case object Pipe extends NetworkTransportValue("pipe")
      /** Unix domain socket. */
      case object Unix extends NetworkTransportValue("unix")

  }

  
  abstract class NetworkTypeValue(val value: String)
  object NetworkTypeValue {
      /** IPv4. */
      case object Ipv4 extends NetworkTypeValue("ipv4")
      /** IPv6. */
      case object Ipv6 extends NetworkTypeValue("ipv6")

  }

  
  abstract class RpcConnectRpcErrorCodeValue(val value: String)
  object RpcConnectRpcErrorCodeValue {
      /** cancelled. */
      case object Cancelled extends RpcConnectRpcErrorCodeValue("cancelled")
      /** unknown. */
      case object Unknown extends RpcConnectRpcErrorCodeValue("unknown")
      /** invalid_argument. */
      case object InvalidArgument extends RpcConnectRpcErrorCodeValue("invalid_argument")
      /** deadline_exceeded. */
      case object DeadlineExceeded extends RpcConnectRpcErrorCodeValue("deadline_exceeded")
      /** not_found. */
      case object NotFound extends RpcConnectRpcErrorCodeValue("not_found")
      /** already_exists. */
      case object AlreadyExists extends RpcConnectRpcErrorCodeValue("already_exists")
      /** permission_denied. */
      case object PermissionDenied extends RpcConnectRpcErrorCodeValue("permission_denied")
      /** resource_exhausted. */
      case object ResourceExhausted extends RpcConnectRpcErrorCodeValue("resource_exhausted")
      /** failed_precondition. */
      case object FailedPrecondition extends RpcConnectRpcErrorCodeValue("failed_precondition")
      /** aborted. */
      case object Aborted extends RpcConnectRpcErrorCodeValue("aborted")
      /** out_of_range. */
      case object OutOfRange extends RpcConnectRpcErrorCodeValue("out_of_range")
      /** unimplemented. */
      case object Unimplemented extends RpcConnectRpcErrorCodeValue("unimplemented")
      /** internal. */
      case object Internal extends RpcConnectRpcErrorCodeValue("internal")
      /** unavailable. */
      case object Unavailable extends RpcConnectRpcErrorCodeValue("unavailable")
      /** data_loss. */
      case object DataLoss extends RpcConnectRpcErrorCodeValue("data_loss")
      /** unauthenticated. */
      case object Unauthenticated extends RpcConnectRpcErrorCodeValue("unauthenticated")

  }

  
  abstract class RpcGrpcStatusCodeValue(val value: Long)
  object RpcGrpcStatusCodeValue {
      /** OK. */
      case object Ok extends RpcGrpcStatusCodeValue(0)
      /** CANCELLED. */
      case object Cancelled extends RpcGrpcStatusCodeValue(1)
      /** UNKNOWN. */
      case object Unknown extends RpcGrpcStatusCodeValue(2)
      /** INVALID_ARGUMENT. */
      case object InvalidArgument extends RpcGrpcStatusCodeValue(3)
      /** DEADLINE_EXCEEDED. */
      case object DeadlineExceeded extends RpcGrpcStatusCodeValue(4)
      /** NOT_FOUND. */
      case object NotFound extends RpcGrpcStatusCodeValue(5)
      /** ALREADY_EXISTS. */
      case object AlreadyExists extends RpcGrpcStatusCodeValue(6)
      /** PERMISSION_DENIED. */
      case object PermissionDenied extends RpcGrpcStatusCodeValue(7)
      /** RESOURCE_EXHAUSTED. */
      case object ResourceExhausted extends RpcGrpcStatusCodeValue(8)
      /** FAILED_PRECONDITION. */
      case object FailedPrecondition extends RpcGrpcStatusCodeValue(9)
      /** ABORTED. */
      case object Aborted extends RpcGrpcStatusCodeValue(10)
      /** OUT_OF_RANGE. */
      case object OutOfRange extends RpcGrpcStatusCodeValue(11)
      /** UNIMPLEMENTED. */
      case object Unimplemented extends RpcGrpcStatusCodeValue(12)
      /** INTERNAL. */
      case object Internal extends RpcGrpcStatusCodeValue(13)
      /** UNAVAILABLE. */
      case object Unavailable extends RpcGrpcStatusCodeValue(14)
      /** DATA_LOSS. */
      case object DataLoss extends RpcGrpcStatusCodeValue(15)
      /** UNAUTHENTICATED. */
      case object Unauthenticated extends RpcGrpcStatusCodeValue(16)

  }

  
  abstract class RpcSystemValue(val value: String)
  object RpcSystemValue {
      /** gRPC. */
      case object Grpc extends RpcSystemValue("grpc")
      /** Java RMI. */
      case object JavaRmi extends RpcSystemValue("java_rmi")
      /** .NET WCF. */
      case object DotnetWcf extends RpcSystemValue("dotnet_wcf")
      /** Apache Dubbo. */
      case object ApacheDubbo extends RpcSystemValue("apache_dubbo")
      /** Connect RPC. */
      case object ConnectRpc extends RpcSystemValue("connect_rpc")

  }

  
  abstract class TlsProtocolNameValue(val value: String)
  object TlsProtocolNameValue {
      /** ssl. */
      case object Ssl extends TlsProtocolNameValue("ssl")
      /** tls. */
      case object Tls extends TlsProtocolNameValue("tls")

  }

  
  abstract class OpentracingRefTypeValue(val value: String)
  object OpentracingRefTypeValue {
      /** The parent Span depends on the child Span in some capacity. */
      case object ChildOf extends OpentracingRefTypeValue("child_of")
      /** The parent Span doesn&#39;t depend in any way on the result of the child Span. */
      case object FollowsFrom extends OpentracingRefTypeValue("follows_from")

  }

  
  abstract class OtelStatusCodeValue(val value: String)
  object OtelStatusCodeValue {
      /** The operation has been validated by an Application developer or Operator to have completed successfully. */
      case object Ok extends OtelStatusCodeValue("OK")
      /** The operation contains an error. */
      case object Error extends OtelStatusCodeValue("ERROR")

  }

  
  abstract class FaasDocumentOperationValue(val value: String)
  object FaasDocumentOperationValue {
      /** When a new object is created. */
      case object Insert extends FaasDocumentOperationValue("insert")
      /** When an object is modified. */
      case object Edit extends FaasDocumentOperationValue("edit")
      /** When an object is deleted. */
      case object Delete extends FaasDocumentOperationValue("delete")

  }

  
  abstract class GraphqlOperationTypeValue(val value: String)
  object GraphqlOperationTypeValue {
      /** GraphQL query. */
      case object Query extends GraphqlOperationTypeValue("query")
      /** GraphQL mutation. */
      case object Mutation extends GraphqlOperationTypeValue("mutation")
      /** GraphQL subscription. */
      case object Subscription extends GraphqlOperationTypeValue("subscription")

  }

  
  abstract class MessageTypeValue(val value: String)
  object MessageTypeValue {
      /** sent. */
      case object Sent extends MessageTypeValue("SENT")
      /** received. */
      case object Received extends MessageTypeValue("RECEIVED")

  }

  
  // Manually defined and not YET in the YAML
  /**
   * The name of an event describing an exception.
   *
   * <p>Typically an event with that name should not be manually created. Instead
   * `org.typelevel.otel4s.trace.Span#recordException(Throwable)` should be used.
   */
  final val ExceptionEventName = "exception"

  /**
   * The name of the keyspace being accessed.
   *
   * @deprecated this item has been removed as of 1.8.0 of the semantic conventions. Please use [[SemanticAttributes.DbName]] instead.
   */
  @deprecated("Use SemanticAttributes.DbName instead", "0.3.0")
  val DbCassandraKeyspace = string("db.cassandra.keyspace")

  /**
   * The <a href="https://hbase.apache.org/book.html#_namespace">HBase namespace</a> being accessed.
   *
   * @deprecated this item has been removed as of 1.8.0 of the semantic conventions. Please use [[SemanticAttributes.DbName]] instead.
   */
  @deprecated("Use SemanticAttributes.DbName instead", "0.3.0")
  val DbHbaseNameSpace = string("db.hbase.namespace")

  /**
   * The size of the uncompressed request payload body after transport decoding. Not set if
   * transport encoding not used.
   *
   * @deprecated  this item has been removed as of 1.13.0 of the semantic conventions. Please use [[SemanticAttributes.HttpRequestContentLength]] instead.
   */
  @deprecated("Use SemanticAttributes.HttpRequestContentLength instead", "0.3.0")
  val HttpRequestContentLengthUncompressed = long("http.request_content_length_uncompressed")

  /**
   * @deprecated This item has been removed as of 1.13.0 of the semantic conventions. Please use [[SemanticAttributes.HttpResponseContentLength]] instead.
   */
  @deprecated("Use SemanticAttributes.HttpResponseContentLength instead", "0.3.0")
  val HttpResponseContentLengthUncompressed = long("http.response_content_length_uncompressed")

  /**
   * @deprecated This item has been removed as of 1.13.0 of the semantic conventions. Please use
   *     [[SemanticAttributes.NetHostName]] instead.
   */
  @deprecated("Use SemanticAttributes.NetHostName instead", "0.3.0")
  val HttpServerName = string("http.server_name")

  /**
   * @deprecated This item has been removed as of 1.13.0 of the semantic conventions. Please use
   *     [[SemanticAttributes.NetHostName]] instead.
   */
  @deprecated("Use SemanticAttributes.NetHostName instead", "0.3.0")
  val HttpHost = string("http.host")

  /**
   * @deprecated This item has been removed as of 1.13.0 of the semantic conventions. Please use [[SemanticAttributes.NetSockPeerAddr]] instead.
   */
  @deprecated("Use SemanticAttributes.NetSockPeerAddr instead", "0.3.0")
  val NetPeerIp = string("net.peer.ip")

  /**
   * @deprecated This item has been removed as of 1.13.0 of the semantic conventions. Please use [[SemanticAttributes.NetSockHostAddr]] instead.
   */
  @deprecated("Use SemanticAttributes.NetSockHostAddr instead", "0.3.0")
  val NetHostIp = string("net.host.ip")

  /**
   * The ordinal number of request re-sending attempt.
   * @deprecated This item has been removed as of 1.15.0 of the semantic conventions. Use [[SemanticAttributes.HttpResendCount]] instead.
   */
  @deprecated("Use SemanticAttributes.HttpResendCount instead", "0.3.0")
  val HttpRetryCount = long("http.retry_count")


 /**
  * A string identifying the messaging system.
  * @deprecated This item has been removed as of 1.17.0 of the semantic conventions. Use [[SemanticAttributes.MessagingDestinationName]] instead.
  */
  @deprecated("Use SemanticAttributes.MessagingDestinationName instead", "0.3.0")
  val MessagingDestination = string("messaging.destination")

 /**
  * A boolean that is true if the message destination is temporary.
  * @deprecated This item has been removed as of 1.17.0 of the semantic conventions. Use [[SemanticAttributes.MessagingDestinationTemporary]] instead.
  */
  @deprecated("Use SemanticAttributes.MessagingDestinationTemporary instead", "0.3.0")
  val MessagingTempDestination = boolean("messaging.temp_destination")

 /**
  * The name of the transport protocol.
  * @deprecated This item has been removed as of 1.17.0 of the semantic conventions. Use [[SemanticAttributes.NetAppProtocolName]] instead.
  */
  @deprecated("Use SemanticAttributes.NetAppProtocolName instead", "0.3.0")
  val MessagingProtocol = string("messaging.protocol")

  /**
  * The version of the transport protocol.
  * @deprecated This item has been removed as of 1.17.0 of the semantic conventions. Use [[SemanticAttributes.NetAppProtocolVersion]] instead.
  */
  @deprecated("Use SemanticAttributes.NetAppProtocolVersion instead", "0.3.0")
  val MessagingProtocolVersion = string("messaging.protocol_version")

  /**
   * Connection string.
   * @deprecated This item has been removed as of 1.17.0 of the semantic conventions. There is no replacement.
   */
  @deprecated("There is no replacement", "0.3.0")
  val MessagingUrl = string("messaging.url")

  /**
   * The <a href="#conversations">conversation ID</a> identifying the conversation to which the
   * message belongs, represented as a string. Sometimes called &quot;Correlation ID&quot;.
   * @deprecated This item has been removed as of 1.17.0 of the semantic conventions. Use [[SemanticAttributes.MessagingMessageConversationId]] instead.
   */
  @deprecated("Use SemanticAttributes.MessagingMessageConversationId instead", "0.3.0")
  val MessagingConversationId = string("messaging.conversation_id")

  /**
   * RabbitMQ message routing key.
   * @deprecated This item has been removed as of 1.17.0 of the semantic conventions. Use [[SemanticAttributes.MessagingRabbitmqDestinationRoutingKey]] instead.
   */
  @deprecated("Use SemanticAttributes.MessagingRabbitmqDestinationRoutingKey instead", "0.3.0")
  val MessagingRabbitmqRoutingKey = string("messaging.rabbitmq.routing_key")

  /**
   * Partition the message is received from.
   * @deprecated This item has been removed as of 1.17.0 of the semantic conventions. Use [[SemanticAttributes.MessagingKafkaSourcePartition]] instead.
   */
  @deprecated("Use SemanticAttributes.MessagingKafkaSourcePartition instead", "0.3.0")
  val MessagingKafkaPartition = long("messaging.kafka.partition")

  /**
   * A boolean that is true if the message is a tombstone.
   * @deprecated This item has been removed as of 1.17.0 of the semantic conventions. Use [[SemanticAttributes.MessagingKafkaMessageTombstone]] instead.
   */
  @deprecated("Use SemanticAttributes.MessagingKafkaMessageTombstone instead", "0.3.0")
  val MessagingKafkaTombstone = boolean("messaging.kafka.tombstone")

  /**
   * The timestamp in milliseconds that the delay message is expected to be delivered to consumer.
   * @deprecated This item has been removed as of 1.17.0 of the semantic conventions. Use [[SemanticAttributes.MessagingRocketmqMessageDeliveryTimestamp]] instead.
   */
  @deprecated("Use SemanticAttributes.MessagingRocketmqMessageDeliveryTimestamp instead", "0.3.0")
  val MessagingRocketmqDeliveryTimestamp = long("messaging.rocketmq.delivery_timestamp")

  /**
   * The delay time level for delay message, which determines the message delay time.
   * @deprecated This item has been removed as of 1.17.0 of the semantic conventions. Use [[SemanticAttributes.MessagingRocketmqMessageDelayTimeLevel]] instead.
   */
  @deprecated("Use SemanticAttributes.MessagingRocketmqMessageDelayTimeLevel instead", "0.3.0")
  val MessagingRocketmqDelayTimeLevel = long("messaging.rocketmq.delay_time_level")

  /** 
   * The name of the instrumentation scope - (`InstrumentationScope.Name` in OTLP).
   * @deprecated This item has been moved, use [[org.typelevel.otel4s.semconv.resource.attributes.ResourceAttributes.OtelScopeName ResourceAttributes.OtelScopeName]] instead.
   */
  @deprecated("Use ResourceAttributes.OtelScopeName instead", "0.3.0")
  val OtelScopeName = string("otel.scope.name")

  /** 
   * The version of the instrumentation scope - (`InstrumentationScope.Version` in OTLP).
   * @deprecated This item has been moved, use [[org.typelevel.otel4s.semconv.resource.attributes.ResourceAttributes.OtelScopeVersion ResourceAttributes.OtelScopeVersion]] instead.
   */
  @deprecated("Use ResourceAttributes.OtelScopeVersion instead", "0.3.0")
  val OtelScopeVersion = string("otel.scope.version")

  /**
   * The execution ID of the current function execution.
   * @deprecated This item has been renamed in 1.19.0 version of the semantic conventions. 
   * Use [[SemanticAttributes.FaasInvocationId]] instead.
   */
  @deprecated("Use SemanticAttributes.FaasInvocationId instead", "0.3.0")
  val FaasExecution = string("faas.execution")

  /**
   * Value of the <a href="https://www.rfc-editor.org/rfc/rfc9110.html#field.user-agent">HTTP
   * User-Agent</a> header sent by the client.
   * @deprecated This item has been renamed in 1.19.0 version of the semantic conventions. 
   * Use [[SemanticAttributes.UserAgentOriginal]] instead.
   */
  @deprecated("Use SemanticAttributes.UserAgentOriginal instead", "0.3.0")
  val HttpUserAgent = string("http.user_agent")

  /**
   * Deprecated.
   *
   * @deprecated Deprecated, use the [[org.typelevel.otel4s.semconv.resource.attributes.ResourceAttributes.OtelScopeName ResourceAttributes.OtelScopeName]] attribute.
   */
  @deprecated("Use ResourceAttributes.OtelScopeName instead", "0.3.0")
  val OtelLibraryName = string("otel.library.name")

  /**
   * Deprecated.
   *
   * @deprecated Deprecated, use the [[org.typelevel.otel4s.semconv.resource.attributes.ResourceAttributes.OtelScopeVersion ResourceAttributes.OtelScopeVersion]] attribute.
   */
  @deprecated("Use ResourceAttributes.OtelScopeVersion instead", "0.3.0")
  val OtelLibraryVersion = string("otel.library.version")

  /**
   * Kind of HTTP protocol used.
   * @deprecated This item has been removed as of 1.20.0 of the semantic conventions.
   */
  @deprecated("There is no replacement", "0.3.0")
  val HttpFlavor = string("http.flavor")

  /**
   * Values for [[SemanticAttributes.HttpFlavor]].
   * @deprecated This item has been removed as of 1.20.0 of the semantic conventions.
   */
  @deprecated("There is no replacement", "0.3.0")
  abstract class HttpFlavorValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object HttpFlavorValue {
    /** HTTP/1.0. */
    case object Http10 extends HttpFlavorValue("1.0")
    /** HTTP/1.1. */
    case object Http11 extends HttpFlavorValue("1.1")
    /** HTTP/2. */
    case object Http20 extends HttpFlavorValue("2.0")
    /** HTTP/3. */
    case object Http30 extends HttpFlavorValue("3.0")
    /** SPDY protocol. */
    case object Spdy extends HttpFlavorValue("SPDY")
    /** QUIC protocol. */
    case object Quic extends HttpFlavorValue("QUIC")
  }

  /**
   * Application layer protocol used. The value SHOULD be normalized to lowercase.
   * @deprecated This item has been removed as of 1.20.0 of the semantic conventions. Use [[SemanticAttributes.NetProtocolName]] instead.
   */
  @deprecated("Use SemanticAttributes.NetProtocolName instead", "0.3.0")
  val NetAppProtocolName = string("net.app.protocol.name")

  /**
   * Version of the application layer protocol used. See note below.
   *
   * <p>Notes:
   *
   * <ul>
   *   <li>`net.app.protocol.version` refers to the version of the protocol used and might be
   *       different from the protocol client's version. If the HTTP client used has a version of
   *       `0.27.2`, but sends HTTP version `1.1`, this attribute should be set to
   *       `1.1`.
   * </ul>
   * @deprecated This item has been removed as of 1.20.0 of the semantic conventions. Use [[SemanticAttributes.NetProtocolVersion]] instead.
   */
  @deprecated("Use SemanticAttributes.NetProtocolVersion instead", "0.3.0")
  val NetAppProtocolVersion = string("net.app.protocol.version")

  /**
   * The kind of message destination.
   * @deprecated This item has been removed as of 1.20.0 of the semantic conventions.
   */
  @deprecated("There is no replacement", "0.3.0")
  val MessagingDestinationKind = string("messaging.destination.kind")

  /**
   * Enum values for [[SemanticAttributes.MessagingDestinationKind]].
   * @deprecated This item has been removed as of 1.20.0 of the semantic conventions.
   */
  @deprecated("There is not replacement", "0.3.0")
  abstract class MessagingDestinationKindValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object MessagingDestinationKindValue {
    /** A message sent to a queue. */
    case object Queue extends MessagingDestinationKindValue("queue")
    /** A message sent to a topic. */
    case object Topic extends MessagingDestinationKindValue("topic")
  }

  /**
   * The kind of message source.
   * @deprecated This item has been removed as of 1.20.0 of the semantic conventions.
   */
  @deprecated("There is not replacement", "0.3.0")
  val MessagingSourceKind = string("messaging.source.kind")

  /**
   * Values for [[SemanticAttributes.MessagingSourceKind]].
   * @deprecated This item has been removed as of 1.20.0 of the semantic conventions.
   */
  @deprecated("There is not replacement", "0.3.0")
  abstract class MessagingSourceKindValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object MessagingSourceKindValue {
    /** A message received from a queue. */
    case object Queue extends MessagingSourceKindValue("queue")
    /** A message received from a topic. */
    case object Topic extends MessagingSourceKindValue("topic")
  }

  /**
   * The internet connection type currently being used by the host.
   * @deprecated This item has been removed in 1.21.0 version of the semantic conventions. Use [[SemanticAttributes.NetworkConnectionType]] instead.
   */
  @deprecated("Use SemanticAttributes.NetworkConnectionType instead", "0.3.0")
  val NetHostConnectionType = string("net.host.connection.type")

  /**
   * This describes more details regarding the connection.type. It may be the type of cell
   * technology connection, but it could be used for describing details about a wifi connection.
   * @deprecated This item has been removed in 1.21.0 version of the semantic conventions. Use [[SemanticAttributes.NetworkConnectionSubtype]] instead.
   */
  @deprecated("Use SemanticAttributes.NetworkConnectionSubtype instead", "0.3.0")
  val NetHostConnectionSubtype = string("net.host.connection.subtype")

  /**
   * The name of the mobile carrier.
   * @deprecated This item has been removed in 1.21.0 version of the semantic conventions. Use [[SemanticAttributes.NetworkCarrierName]] instead.
   */
  @deprecated("Use SemanticAttributes.NetworkCarrierName instead", "0.3.0")
  val NetHostCarrierName = string("net.host.carrier.name")

  /**
   * The mobile carrier country code.
   * @deprecated This item has been removed in 1.21.0 version of the semantic conventions. Use [[SemanticAttributes.NetworkCarrierMcc]] instead.
   */
  @deprecated("Use SemanticAttributes.NetworkCarrierMcc instead", "0.3.0")
  val NetHostCarrierMcc = string("net.host.carrier.mcc")

  /**
   * The mobile carrier network code.
   * @deprecated This item has been removed in 1.21.0 version of the semantic conventions. Use [[SemanticAttributes.NetworkCarrierMnc]] instead.
   */
  @deprecated("Use SemanticAttributes.NetworkCarrierMnc instead", "0.3.0")
  val NetHostCarrierMnc = string("net.host.carrier.mnc")

  /**
   * The ISO 3166-1 alpha-2 2-character country code associated with the mobile carrier network.
   * @deprecated This item has been removed in 1.21.0 version of the semantic conventions. Use [[SemanticAttributes.NetworkCarrierIcc]] instead.
   */
  @deprecated("Use SemanticAttributes.NetworkCarrierIcc instead", "0.3.0")
  val NetHostCarrierIcc = string("net.host.carrier.icc")

  /**
   * The IP address of the original client behind all proxies, if known (e.g. from <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Forwarded-For">X-Forwarded-For</a>).
   *
   * <p>Notes:
   *
   * <ul>
   *   <li>This is not necessarily the same as `net.sock.peer.addr`, which would identify the
   *       network-level peer, which may be a proxy.
   *   <li>This attribute should be set when a source of information different from the one used for
   *       `net.sock.peer.addr`, is available even if that other source just confirms the same
   *       value as `net.sock.peer.addr`. Rationale: For `net.sock.peer.addr`, one
   *       typically does not know if it comes from a proxy, reverse proxy, or the actual client.
   *       Setting `http.client_ip` when it's the same as `net.sock.peer.addr` means
   *       that one is at least somewhat confident that the address is not that of the closest
   *       proxy.
   * </ul>
   * @deprecated This item has been removed in 1.21.0 version of the semantic conventions. Use [[SemanticAttributes.ClientAddress]] instead.
   */
  @deprecated("Use SemanticAttributes.ClientAddress instead", "0.3.0")
  val HttpClientIp = string("http.client_ip")

  /**
   * The message source name.
   *
   * <p>Notes:
   *
   * <ul>
   *   <li>Source name SHOULD uniquely identify a specific queue, topic, or other entity within the
   *       broker. If the broker does not have such notion, the source name SHOULD uniquely identify
   *       the broker.
   * </ul>
   * @deprecated This item has been removed in 1.21.0 version of the semantic conventions.
   */
  @deprecated("There is no replacement", "0.3.0")
  val MessagingSourceName = string("messaging.source.name")

  /**
   * Low cardinality representation of the messaging source name.
   *
   * <p>Notes:
   *
   * <ul>
   *   <li>Source names could be constructed from templates. An example would be a source name
   *       involving a user name or product id. Although the source name in this case is of high
   *       cardinality, the underlying template is of low cardinality and can be effectively used
   *       for grouping and aggregation.
   * </ul>
   * @deprecated This item has been removed in 1.21.0 version of the semantic conventions.
   */
  @deprecated("There is no replacement", "0.3.0")
  val MessagingSourceTemplate = string("messaging.source.template")

  /**
   * A boolean that is true if the message source is temporary and might not exist anymore after
   * messages are processed.
   * @deprecated This item has been removed in 1.21.0 version of the semantic conventions.
   */
  @deprecated("There is no replacement", "0.3.0")
  val MessagingSourceTemporary = boolean("messaging.source.temporary")

  /**
   * A boolean that is true if the message source is anonymous (could be unnamed or have
   * auto-generated name).
   * @deprecated This item has been removed in 1.21.0 version of the semantic conventions.
   */
  @deprecated("There is no replacement", "0.3.0")
  val MessagingSourceAnonymous = boolean("messaging.source.anonymous")

  /**
   * The identifier for the consumer receiving a message. For Kafka, set it to
   * `{messaging.kafka.consumer.group} - {messaging.kafka.client_id}`, if both are present, or only
   * `messaging.kafka.consumer.group`. For brokers, such as RabbitMQ and Artemis, set it to
   * the `client_id` of the client consuming the message.
   * @deprecated This item has been removed in 1.21.0 version of the semantic conventions. See [[SemanticAttributes.MessagingClientId]].
   */
  @deprecated("Use SemanticAttributes.MessagingClientId instead", "0.3.0")
  val MessagingConsumerId = string("messaging.consumer.id")

  /**
   * Client Id for the Consumer or Producer that is handling the message.
   * @deprecated This item has been removed in 1.21.0 version of the semantic conventions. See [[SemanticAttributes.MessagingClientId]].
   */
  @deprecated("Use SemanticAttributes.MessagingClientId instead", "0.3.0")
  val MessagingKafkaClientId = string("messaging.kafka.client_id")

  /**
   * Partition the message is received from.
   * @deprecated This item has been removed in 1.21.0 version of the semantic conventions.
   */
  @deprecated("There is no replacement", "0.3.0")
  val MessagingKafkaSourcePartition = long("messaging.kafka.source.partition")

  /**
   * The unique identifier for each client.
   * @deprecated This item has been removed in 1.21.0 version of the semantic conventions. See [[SemanticAttributes.MessagingClientId]].
   */
  @deprecated("Use SemanticAttributes.MessagingClientId instead", "0.3.0")
  val MessagingRocketmqClientId = string("messaging.rocketmq.client_id")

  /**
   * Values for [[SemanticAttributes.NetHostConnectionType]].
   * @deprecated This item has been removed as of 1.21.0 of the semantic conventions. Use [[SemanticAttributes.NetworkConnectionTypeValue]] instead.
   */
  @deprecated("Use SemanticAttributes.NetworkConnectionTypeValue", "0.3.0")
  abstract class NetHostConnectionTypeValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object NetHostConnectionTypeValue {
    /** wifi. */
    case object Wifi extends NetHostConnectionTypeValue("wifi")
    /** wired. */
    case object Wired extends NetHostConnectionTypeValue("wired")
    /** cell. */
    case object Cell extends NetHostConnectionTypeValue("cell")
    /** unavailable. */
    case object Unavailable extends NetHostConnectionTypeValue("unavailable")
    /** unknown. */
    case object Unknown extends NetHostConnectionTypeValue("unknown")
  }

  /**
   * Values for [[SemanticAttributes.NetHostConnectionSubtype]].
   * @deprecated This item has been removed as of 1.21.0 of the semantic conventions. Use [[SemanticAttributes.NetworkConnectionSubtypeValue]] instead.
   */
  @deprecated("Use SemanticAttributes.NetworkConnectionSubtypeValue", "0.3.0")
  abstract class NetHostConnectionSubtypeValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object NetHostConnectionSubtypeValue {
    /** GPRS. */
    case object Gprs extends NetHostConnectionSubtypeValue("gprs")
    /** EDGE. */
    case object Edge extends NetHostConnectionSubtypeValue("edge")
    /** UMTS. */
    case object Umts extends NetHostConnectionSubtypeValue("umts")
    /** CDMA. */
    case object Cdma extends NetHostConnectionSubtypeValue("cdma")
    /** EVDO Rel. 0. */
    case object Evdo0 extends NetHostConnectionSubtypeValue("evdo_0")
    /** EVDO Rev. A. */
    case object EvdoA extends NetHostConnectionSubtypeValue("evdo_a")
    /** CDMA2000 1XRTT. */
    case object Cdma20001xrtt extends NetHostConnectionSubtypeValue("cdma2000_1xrtt")
    /** HSDPA. */
    case object Hsdpa extends NetHostConnectionSubtypeValue("hsdpa")
    /** HSUPA. */
    case object Hsupa extends NetHostConnectionSubtypeValue("hsupa")
    /** HSPA. */
    case object Hspa extends NetHostConnectionSubtypeValue("hspa")
    /** IDEN. */
    case object Iden extends NetHostConnectionSubtypeValue("iden")
    /** EVDO Rev. B. */
    case object EvdoB extends NetHostConnectionSubtypeValue("evdo_b")
    /** LTE. */
    case object Lte extends NetHostConnectionSubtypeValue("lte")
    /** EHRPD. */
    case object Ehrpd extends NetHostConnectionSubtypeValue("ehrpd")
    /** HSPAP. */
    case object Hspap extends NetHostConnectionSubtypeValue("hspap")
    /** GSM. */
    case object Gsm extends NetHostConnectionSubtypeValue("gsm")
    /** TD-SCDMA. */
    case object TdScdma extends NetHostConnectionSubtypeValue("td_scdma")
    /** IWLAN. */
    case object Iwlan extends NetHostConnectionSubtypeValue("iwlan")
    /** 5G NR (New Radio). */
    case object Nr extends NetHostConnectionSubtypeValue("nr")
    /** 5G NRNSA (New Radio Non-Standalone). */
    case object Nrnsa extends NetHostConnectionSubtypeValue("nrnsa")
    /** LTE CA. */
    case object LteCa extends NetHostConnectionSubtypeValue("lte_ca")
  }

  /**
   * Immediate client peer port number.
   *
   * @deprecated This item has been renamed in 1.22.0 of the semantic conventions. Use [[SemanticAttributes.NetworkPeerPort]] on server telemetry and [[SemanticAttributes.NetworkLocalPort]] on client telemetry instead.
   */
  @deprecated("Use SemanticAttributes.NetworkPeerPort or SemanticAttributes.NetworkLocalPort instead", "0.4.0")
  val ClientSocketPort = long("client.socket.port")

  /**
   * Name of the memory pool.
   *
   * <p>Notes:
   *
   * <ul>
   *   <li>Pool names are generally obtained via <a
   *       href="https://docs.oracle.com/en/java/javase/11/docs/api/java.management/java/lang/management/MemoryPoolMXBean.html#getName()">MemoryPoolMXBean#getName()</a>.
   * </ul>
   * @deprecated This item has been renamed in 1.22.0 of the semantic conventions. Use [[SemanticAttributes.JvmMemoryPoolName]] instead.
   */
  @deprecated("Use SemanticAttributes.JvmMemoryPoolName instead", "0.4.0")
  val Pool = string("pool")

  /**
   * The domain name of the source system.
   *
   * <p>Notes:
   *
   * <ul>
   *   <li>This value may be a host name, a fully qualified domain name, or another host naming
   *       format.
   * </ul>
   * @deprecated This item has been removed in 1.22.0 of the semantic conventions.
   */
  @deprecated("There is no replacement", "0.4.0")
  val SourceDomain = string("source.domain")

  /**
   * Physical server IP address or Unix socket address. If set from the client, should simply use
   * the socket's peer address, and not attempt to find any actual server IP (i.e., if set from
   * client, this may represent some proxy server instead of the logical server).
   *
   * @deprecated This item has been renamed in 1.22.0 of the semantic conventions. Use [[SemanticAttributes.NetworkLocalAddress]] on server telemetry and [[SemanticAttributes.NetworkPeerAddress]] on client telemetry instead.
   */
  @deprecated("Use SemanticAttributes.NetworkLocalAddress or SemanticAttributes.NetworkPeerAddress instead", "0.4.0")
  val ServerSocketAddress = string("server.socket.address")

  /**
   * The (uncompressed) size of the message payload in bytes. Also use this attribute if it is
   * unknown whether the compressed or uncompressed payload size is reported.
   *
   * @deprecated This item has been renamed in 1.22.0 of the semantic conventions. Use [[SemanticAttributes.MessagingMessageBodySize]] instead.
   */
  @deprecated("Use SemanticAttributes.MessagingMessageBodySize instead", "0.4.0")
  val MessagingMessagePayloadSizeBytes = long("messaging.message.payload_size_bytes")

  /**
   * The domain name of the destination system.
   *
   * @deprecated This item has been removed in 1.22.0 of the semantic conventions.
   */
  @deprecated("There is no replacement", "0.4.0")
  val DestinationDomain = string("destination.domain")

  /**
   * The compressed size of the message payload in bytes.
   *
   * @deprecated This item has been removed in 1.22.0 of the semantic conventions.
   */
  @deprecated("There is no replacement", "0.4.0")
  val MessagingMessagePayloadCompressedSizeBytes = long("messaging.message.payload_compressed_size_bytes")

  /**
   * The domain name of an immediate peer.
   *
   * <p>Notes:
   *
   * <ul>
   *   <li>Typically observed from the client side, and represents a proxy or other intermediary
   *       domain name.
   * </ul>
   *
   * @deprecated This item has been removed in 1.22.0 of the semantic conventions.
   */
  @deprecated("There is no replacement", "0.4.0")
  val ServerSocketDomain = string("server.socket.domain")

  /**
   * The type of memory.
   *
   * @deprecated This item has been renamed in 1.22.0 of the semantic conventions. Use [[SemanticAttributes.JvmMemoryType]] instead.
   */
  @deprecated("Use SemanticAttributes.JvmMemoryType instead", "0.4.0")
  val Type = string("type")

  /**
   * Physical server port.
   *
   * @deprecated This item has been renamed in 1.22.0 of the semantic conventions. Use [[SemanticAttributes.NetworkLocalPort]] on server telemetry and [[SemanticAttributes.NetworkPeerPort]] on client telemetry instead.
   */
  @deprecated("Use SemanticAttributes.NetworkLocalPort or SemanticAttributes.NetworkPeerPort instead", "0.4.0")
  val ServerSocketPort = long("server.socket.port")

  /**
   * Immediate client peer address - unix domain socket name, IPv4 or IPv6 address.
   *
   * @deprecated This item has been renamed in 1.22.0 of the semantic conventions. Use [[SemanticAttributes.NetworkPeerAddress]] on server telemetry and [[SemanticAttributes.NetworkLocalAddress]] on client telemetry instead.
   */
  @deprecated("Use SemanticAttributes.NetworkPeerAddress or SemanticAttributes.NetworkLocalAddress instead", "0.4.0")
  val ClientSocketAddress = string("client.socket.address")

  /**
   * @deprecated This item has been renamed as of 1.21.0 of the semantic conventions. Use [[SemanticAttributes.JvmMemoryTypeValue]] instead.
   */
  @deprecated("Use SemanticAttributes.JvmMemoryTypeValue instead", "0.4.0")
  abstract class TypeValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object TypeValue {
    /** Heap memory. */
    case object Heap extends TypeValue("heap")
    /** Non-heap memory. */
    case object NonHeap extends TypeValue("non_heap")
  }


  /**
   * Whether the thread is daemon or not.
   *
   * @deprecated This item has been renamed in 1.23.1 of the semantic conventions. Use [[SemanticAttributes.JvmThreadDaemon]] instead.
   */
  @deprecated("Use SemanticAttributes.JvmThreadDaemon instead", "0.4.0")
  val ThreadDaemon = boolean("thread.daemon")

  /**
   * The ordinal number of request resending attempt (for any reason, including redirects).
   *
   * <p>Notes:
   *
   * <ul>
   *   <li>The resend count SHOULD be updated each time an HTTP request gets resent by the client,
   *       regardless of what was the cause of the resending (e.g. redirection, authorization
   *       failure, 503 Server Unavailable, network issues, or any other).
   * </ul>
   *
   * @deprecated This item has been renamed in 1.23.1 of the semantic conventions. Use [[SemanticAttributes.HttpRequestResendCount]] instead.
   */
  @deprecated("Use SemanticAttributes.HttpRequestResendCount instead", "0.4.0")
  val HttpResendCount = long("http.resend_count")

  

}