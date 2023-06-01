/*
 * Copyright 2022 Typelevel
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

import scala.annotation.nowarn

// DO NOT EDIT, this is an Auto-generated file from buildscripts/semantic-convention/templates/SemanticAttributes.scala.j2
@nowarn("msg=never used")
@nowarn("msg=message and version: @deprecated")
object SemanticAttributes {

  /** The URL of the OpenTelemetry schema for these keys and values.
    */
  final val SchemaUrl = "https://opentelemetry.io/schemas/1.19.0"

  /** The type of the exception (its fully-qualified class name, if applicable).
    * The dynamic type of the exception should be preferred over the static type
    * in languages that support it.
    */
  val ExceptionType: AttributeKey[String] = string("exception.type")

  /** The exception message.
    */
  val ExceptionMessage: AttributeKey[String] = string("exception.message")

  /** A stacktrace as a string in the natural representation for the language
    * runtime. The representation is to be determined and documented by each
    * language SIG.
    */
  val ExceptionStacktrace: AttributeKey[String] = string("exception.stacktrace")

  /** HTTP request method.
    */
  val HttpMethod: AttributeKey[String] = string("http.method")

  /** <a href="https://tools.ietf.org/html/rfc7231#section-6">HTTP response
    * status code</a>.
    */
  val HttpStatusCode: AttributeKey[Long] = long("http.status_code")

  /** Kind of HTTP protocol used.
    */
  val HttpFlavor: AttributeKey[String] = string("http.flavor")

  /** The URI scheme identifying the used protocol.
    */
  val HttpScheme: AttributeKey[String] = string("http.scheme")

  /** The matched route (path template in the format used by the respective
    * server framework). See note below
    *
    * <p>Notes: <ul> <li>MUST NOT be populated when this is not supported by the
    * HTTP server framework as the route attribute should have low-cardinality
    * and the URI path can NOT substitute it. SHOULD include the <a
    * href="/specification/trace/semantic_conventions/http.md#http-server-definitions">application
    * root</a> if there is one.</li> </ul>
    */
  val HttpRoute: AttributeKey[String] = string("http.route")

  /** The name identifies the event.
    */
  val EventName: AttributeKey[String] = string("event.name")

  /** The domain identifies the business context for the events.
    *
    * <p>Notes: <ul> <li>Events across different domains may have same {@code
    * event.name}, yet be unrelated events.</li> </ul>
    */
  val EventDomain: AttributeKey[String] = string("event.domain")

  /** The full invoked ARN as provided on the {@code Context} passed to the
    * function ({@code Lambda-Runtime-Invoked-Function-Arn} header on the {@code
    * /runtime/invocation/next} applicable).
    *
    * <p>Notes: <ul> <li>This may be different from {@code cloud.resource_id} if
    * an alias is involved.</li> </ul>
    */
  val AwsLambdaInvokedArn: AttributeKey[String] = string(
    "aws.lambda.invoked_arn"
  )

  /** The <a
    * href="https://github.com/cloudevents/spec/blob/v1.0.2/cloudevents/spec.md#id">event_id</a>
    * uniquely identifies the event.
    */
  val CloudeventsEventId: AttributeKey[String] = string("cloudevents.event_id")

  /** The <a
    * href="https://github.com/cloudevents/spec/blob/v1.0.2/cloudevents/spec.md#source-1">source</a>
    * identifies the context in which an event happened.
    */
  val CloudeventsEventSource: AttributeKey[String] = string(
    "cloudevents.event_source"
  )

  /** The <a
    * href="https://github.com/cloudevents/spec/blob/v1.0.2/cloudevents/spec.md#specversion">version
    * of the CloudEvents specification</a> which the event uses.
    */
  val CloudeventsEventSpecVersion: AttributeKey[String] = string(
    "cloudevents.event_spec_version"
  )

  /** The <a
    * href="https://github.com/cloudevents/spec/blob/v1.0.2/cloudevents/spec.md#type">event_type</a>
    * contains a value describing the type of event related to the originating
    * occurrence.
    */
  val CloudeventsEventType: AttributeKey[String] = string(
    "cloudevents.event_type"
  )

  /** The <a
    * href="https://github.com/cloudevents/spec/blob/v1.0.2/cloudevents/spec.md#subject">subject</a>
    * of the event in the context of the event producer (identified by source).
    */
  val CloudeventsEventSubject: AttributeKey[String] = string(
    "cloudevents.event_subject"
  )

  /** Parent-child Reference type
    *
    * <p>Notes: <ul> <li>The causal relationship between a child Span and a
    * parent Span.</li> </ul>
    */
  val OpentracingRefType: AttributeKey[String] = string("opentracing.ref_type")

  /** An identifier for the database management system (DBMS) product being
    * used. See below for a list of well-known identifiers.
    */
  val DbSystem: AttributeKey[String] = string("db.system")

  /** The connection string used to connect to the database. It is recommended
    * to remove embedded credentials.
    */
  val DbConnectionString: AttributeKey[String] = string("db.connection_string")

  /** Username for accessing the database.
    */
  val DbUser: AttributeKey[String] = string("db.user")

  /** The fully-qualified class name of the <a
    * href="https://docs.oracle.com/javase/8/docs/technotes/guides/jdbc/">Java
    * Database Connectivity (JDBC)</a> driver used to connect.
    */
  val DbJdbcDriverClassname: AttributeKey[String] = string(
    "db.jdbc.driver_classname"
  )

  /** This attribute is used to report the name of the database being accessed.
    * For commands that switch the database, this should be set to the target
    * database (even if the command fails).
    *
    * <p>Notes: <ul> <li>In some SQL databases, the database name to be used is
    * called &quot;schema name&quot;. In case there are multiple layers that
    * could be considered for database name (e.g. Oracle instance name and
    * schema name), the database name to be used is the more specific layer
    * (e.g. Oracle schema name).</li> </ul>
    */
  val DbName: AttributeKey[String] = string("db.name")

  /** The database statement being executed.
    *
    * <p>Notes: <ul> <li>The value may be sanitized to exclude sensitive
    * information.</li> </ul>
    */
  val DbStatement: AttributeKey[String] = string("db.statement")

  /** The name of the operation being executed, e.g. the <a
    * href="https://docs.mongodb.com/manual/reference/command/#database-operations">MongoDB
    * command name</a> such as {@code findAndModify}, or the SQL keyword.
    *
    * <p>Notes: <ul> <li>When setting this to an SQL keyword, it is not
    * recommended to attempt any client-side parsing of {@code db.statement}
    * just to get this property, but it should be set if the operation name is
    * provided by the library being instrumented. If the SQL statement has an
    * ambiguous operation, or performs more than one operation, this value may
    * be omitted.</li> </ul>
    */
  val DbOperation: AttributeKey[String] = string("db.operation")

  /** The Microsoft SQL Server <a
    * href="https://docs.microsoft.com/en-us/sql/connect/jdbc/building-the-connection-url?view=sql-server-ver15">instance
    * name</a> connecting to. This name is used to determine the port of a named
    * instance.
    *
    * <p>Notes: <ul> <li>If setting a {@code db.mssql.instance_name}, {@code
    * net.peer.port} is no longer required (but still recommended if
    * non-standard).</li> </ul>
    */
  val DbMssqlInstanceName: AttributeKey[String] = string(
    "db.mssql.instance_name"
  )

  /** The fetch size used for paging, i.e. how many rows will be returned at
    * once.
    */
  val DbCassandraPageSize: AttributeKey[Long] = long("db.cassandra.page_size")

  /** The consistency level of the query. Based on consistency values from <a
    * href="https://docs.datastax.com/en/cassandra-oss/3.0/cassandra/dml/dmlConfigConsistency.html">CQL</a>.
    */
  val DbCassandraConsistencyLevel: AttributeKey[String] = string(
    "db.cassandra.consistency_level"
  )

  /** The name of the primary table that the operation is acting upon, including
    * the keyspace name (if applicable).
    *
    * <p>Notes: <ul> <li>This mirrors the db.sql.table attribute but references
    * cassandra rather than sql. It is not recommended to attempt any
    * client-side parsing of {@code db.statement} just to get this property, but
    * it should be set if it is provided by the library being instrumented. If
    * the operation is acting upon an anonymous table, or more than one table,
    * this value MUST NOT be set.</li> </ul>
    */
  val DbCassandraTable: AttributeKey[String] = string("db.cassandra.table")

  /** Whether or not the query is idempotent.
    */
  val DbCassandraIdempotence: AttributeKey[Boolean] = boolean(
    "db.cassandra.idempotence"
  )

  /** The number of times a query was speculatively executed. Not set or {@code
    * 0} if the query was not executed speculatively.
    */
  val DbCassandraSpeculativeExecutionCount: AttributeKey[Long] = long(
    "db.cassandra.speculative_execution_count"
  )

  /** The ID of the coordinating node for a query.
    */
  val DbCassandraCoordinatorId: AttributeKey[String] = string(
    "db.cassandra.coordinator.id"
  )

  /** The data center of the coordinating node for a query.
    */
  val DbCassandraCoordinatorDc: AttributeKey[String] = string(
    "db.cassandra.coordinator.dc"
  )

  /** The index of the database being accessed as used in the <a
    * href="https://redis.io/commands/select">{@code SELECT} command</a>,
    * provided as an integer. To be used instead of the generic {@code db.name}
    * attribute.
    */
  val DbRedisDatabaseIndex: AttributeKey[Long] = long("db.redis.database_index")

  /** The collection being accessed within the database stated in {@code
    * db.name}.
    */
  val DbMongodbCollection: AttributeKey[String] = string(
    "db.mongodb.collection"
  )

  /** The name of the primary table that the operation is acting upon, including
    * the database name (if applicable).
    *
    * <p>Notes: <ul> <li>It is not recommended to attempt any client-side
    * parsing of {@code db.statement} just to get this property, but it should
    * be set if it is provided by the library being instrumented. If the
    * operation is acting upon an anonymous table, or more than one table, this
    * value MUST NOT be set.</li> </ul>
    */
  val DbSqlTable: AttributeKey[String] = string("db.sql.table")

  /** Name of the code, either &quot;OK&quot; or &quot;ERROR&quot;. MUST NOT be
    * set if the status code is UNSET.
    */
  val OtelStatusCode: AttributeKey[String] = string("otel.status_code")

  /** Description of the Status if it has a value, otherwise not set.
    */
  val OtelStatusDescription: AttributeKey[String] = string(
    "otel.status_description"
  )

  /** Type of the trigger which caused this function invocation.
    *
    * <p>Notes: <ul> <li>For the server/consumer span on the incoming side,
    * {@code faas.trigger} MUST be set.</li><li>Clients invoking FaaS instances
    * usually cannot set {@code faas.trigger}, since they would typically need
    * to look in the payload to determine the event type. If clients set it, it
    * should be the same as the trigger that corresponding incoming would have
    * (i.e., this has nothing to do with the underlying transport used to make
    * the API call to invoke the lambda, which is often HTTP).</li> </ul>
    */
  val FaasTrigger: AttributeKey[String] = string("faas.trigger")

  /** The invocation ID of the current function invocation.
    */
  val FaasInvocationId: AttributeKey[String] = string("faas.invocation_id")

  /** The name of the source on which the triggering operation was performed.
    * For example, in Cloud Storage or S3 corresponds to the bucket name, and in
    * Cosmos DB to the database name.
    */
  val FaasDocumentCollection: AttributeKey[String] = string(
    "faas.document.collection"
  )

  /** Describes the type of the operation that was performed on the data.
    */
  val FaasDocumentOperation: AttributeKey[String] = string(
    "faas.document.operation"
  )

  /** A string containing the time when the data was accessed in the <a
    * href="https://www.iso.org/iso-8601-date-and-time-format.html">ISO 8601</a>
    * format expressed in <a href="https://www.w3.org/TR/NOTE-datetime">UTC</a>.
    */
  val FaasDocumentTime: AttributeKey[String] = string("faas.document.time")

  /** The document name/table subjected to the operation. For example, in Cloud
    * Storage or S3 is the name of the file, and in Cosmos DB the table name.
    */
  val FaasDocumentName: AttributeKey[String] = string("faas.document.name")

  /** A string containing the function invocation time in the <a
    * href="https://www.iso.org/iso-8601-date-and-time-format.html">ISO 8601</a>
    * format expressed in <a href="https://www.w3.org/TR/NOTE-datetime">UTC</a>.
    */
  val FaasTime: AttributeKey[String] = string("faas.time")

  /** A string containing the schedule period as <a
    * href="https://docs.oracle.com/cd/E12058_01/doc/doc.1014/e12030/cron_expressions.htm">Cron
    * Expression</a>.
    */
  val FaasCron: AttributeKey[String] = string("faas.cron")

  /** A boolean that is true if the serverless function is executed for the
    * first time (aka cold-start).
    */
  val FaasColdstart: AttributeKey[Boolean] = boolean("faas.coldstart")

  /** The name of the invoked function.
    *
    * <p>Notes: <ul> <li>SHOULD be equal to the {@code faas.name} resource
    * attribute of the invoked function.</li> </ul>
    */
  val FaasInvokedName: AttributeKey[String] = string("faas.invoked_name")

  /** The cloud provider of the invoked function.
    *
    * <p>Notes: <ul> <li>SHOULD be equal to the {@code cloud.provider} resource
    * attribute of the invoked function.</li> </ul>
    */
  val FaasInvokedProvider: AttributeKey[String] = string(
    "faas.invoked_provider"
  )

  /** The cloud region of the invoked function.
    *
    * <p>Notes: <ul> <li>SHOULD be equal to the {@code cloud.region} resource
    * attribute of the invoked function.</li> </ul>
    */
  val FaasInvokedRegion: AttributeKey[String] = string("faas.invoked_region")

  /** The unique identifier of the feature flag.
    */
  val FeatureFlagKey: AttributeKey[String] = string("feature_flag.key")

  /** The name of the service provider that performs the flag evaluation.
    */
  val FeatureFlagProviderName: AttributeKey[String] = string(
    "feature_flag.provider_name"
  )

  /** SHOULD be a semantic identifier for a value. If one is unavailable, a
    * stringified version of the value can be used.
    *
    * <p>Notes: <ul> <li>A semantic identifier, commonly referred to as a
    * variant, provides a means for referring to a value without including the
    * value itself. This can provide additional context for understanding the
    * meaning behind a value. For example, the variant {@code red} maybe be used
    * for the value {@code #c05543}.</li><li>A stringified version of the value
    * can be used in situations where a semantic identifier is unavailable.
    * String representation of the value should be determined by the
    * implementer.</li> </ul>
    */
  val FeatureFlagVariant: AttributeKey[String] = string("feature_flag.variant")

  /** Transport protocol used. See note below.
    */
  val NetTransport: AttributeKey[String] = string("net.transport")

  /** Application layer protocol used. The value SHOULD be normalized to
    * lowercase.
    */
  val NetAppProtocolName: AttributeKey[String] = string("net.app.protocol.name")

  /** Version of the application layer protocol used. See note below.
    *
    * <p>Notes: <ul> <li>{@code net.app.protocol.version} refers to the version
    * of the protocol used and might be different from the protocol client's
    * version. If the HTTP client used has a version of {@code 0.27.2}, but
    * sends HTTP version {@code 1.1}, this attribute should be set to {@code
    * 1.1}.</li> </ul>
    */
  val NetAppProtocolVersion: AttributeKey[String] = string(
    "net.app.protocol.version"
  )

  /** Remote socket peer name.
    */
  val NetSockPeerName: AttributeKey[String] = string("net.sock.peer.name")

  /** Remote socket peer address: IPv4 or IPv6 for internet protocols, path for
    * local communication, <a
    * href="https://man7.org/linux/man-pages/man7/address_families.7.html">etc</a>.
    */
  val NetSockPeerAddr: AttributeKey[String] = string("net.sock.peer.addr")

  /** Remote socket peer port.
    */
  val NetSockPeerPort: AttributeKey[Long] = long("net.sock.peer.port")

  /** Protocol <a
    * href="https://man7.org/linux/man-pages/man7/address_families.7.html">address
    * family</a> which is used for communication.
    */
  val NetSockFamily: AttributeKey[String] = string("net.sock.family")

  /** Logical remote hostname, see note below.
    *
    * <p>Notes: <ul> <li>{@code net.peer.name} SHOULD NOT be set if capturing it
    * would require an extra DNS lookup.</li> </ul>
    */
  val NetPeerName: AttributeKey[String] = string("net.peer.name")

  /** Logical remote port number
    */
  val NetPeerPort: AttributeKey[Long] = long("net.peer.port")

  /** Logical local hostname or similar, see note below.
    */
  val NetHostName: AttributeKey[String] = string("net.host.name")

  /** Logical local port number, preferably the one that the peer used to
    * connect
    */
  val NetHostPort: AttributeKey[Long] = long("net.host.port")

  /** Local socket address. Useful in case of a multi-IP host.
    */
  val NetSockHostAddr: AttributeKey[String] = string("net.sock.host.addr")

  /** Local socket port number.
    */
  val NetSockHostPort: AttributeKey[Long] = long("net.sock.host.port")

  /** The internet connection type currently being used by the host.
    */
  val NetHostConnectionType: AttributeKey[String] = string(
    "net.host.connection.type"
  )

  /** This describes more details regarding the connection.type. It may be the
    * type of cell technology connection, but it could be used for describing
    * details about a wifi connection.
    */
  val NetHostConnectionSubtype: AttributeKey[String] = string(
    "net.host.connection.subtype"
  )

  /** The name of the mobile carrier.
    */
  val NetHostCarrierName: AttributeKey[String] = string("net.host.carrier.name")

  /** The mobile carrier country code.
    */
  val NetHostCarrierMcc: AttributeKey[String] = string("net.host.carrier.mcc")

  /** The mobile carrier network code.
    */
  val NetHostCarrierMnc: AttributeKey[String] = string("net.host.carrier.mnc")

  /** The ISO 3166-1 alpha-2 2-character country code associated with the mobile
    * carrier network.
    */
  val NetHostCarrierIcc: AttributeKey[String] = string("net.host.carrier.icc")

  /** The <a href="../../resource/semantic_conventions/README.md#service">{@code
    * service.name}</a> of the remote service. SHOULD be equal to the actual
    * {@code service.name} resource attribute of the remote service if any.
    */
  val PeerService: AttributeKey[String] = string("peer.service")

  /** Username or client_id extracted from the access token or <a
    * href="https://tools.ietf.org/html/rfc7235#section-4.2">Authorization</a>
    * header in the inbound request from outside the system.
    */
  val EnduserId: AttributeKey[String] = string("enduser.id")

  /** Actual/assumed role the client is making the request under extracted from
    * token or application security context.
    */
  val EnduserRole: AttributeKey[String] = string("enduser.role")

  /** Scopes or granted authorities the client currently possesses extracted
    * from token or application security context. The value would come from the
    * scope associated with an <a
    * href="https://tools.ietf.org/html/rfc6749#section-3.3">OAuth 2.0 Access
    * Token</a> or an attribute value in a <a
    * href="http://docs.oasis-open.org/security/saml/Post2.0/sstc-saml-tech-overview-2.0.html">SAML
    * 2.0 Assertion</a>.
    */
  val EnduserScope: AttributeKey[String] = string("enduser.scope")

  /** Current &quot;managed&quot; thread ID (as opposed to OS thread ID).
    */
  val ThreadId: AttributeKey[Long] = long("thread.id")

  /** Current thread name.
    */
  val ThreadName: AttributeKey[String] = string("thread.name")

  /** The method or function name, or equivalent (usually rightmost part of the
    * code unit's name).
    */
  val CodeFunction: AttributeKey[String] = string("code.function")

  /** The &quot;namespace&quot; within which {@code code.function} is defined.
    * Usually the qualified class or module name, such that {@code
    * code.namespace} + some separator + {@code code.function} form a unique
    * identifier for the code unit.
    */
  val CodeNamespace: AttributeKey[String] = string("code.namespace")

  /** The source code file name that identifies the code unit as uniquely as
    * possible (preferably an absolute file path).
    */
  val CodeFilepath: AttributeKey[String] = string("code.filepath")

  /** The line number in {@code code.filepath} best representing the operation.
    * It SHOULD point within the code unit named in {@code code.function}.
    */
  val CodeLineno: AttributeKey[Long] = long("code.lineno")

  /** The column number in {@code code.filepath} best representing the
    * operation. It SHOULD point within the code unit named in {@code
    * code.function}.
    */
  val CodeColumn: AttributeKey[Long] = long("code.column")

  /** The size of the request payload body in bytes. This is the number of bytes
    * transferred excluding headers and is often, but not always, present as the
    * <a
    * href="https://www.rfc-editor.org/rfc/rfc9110.html#field.content-length">Content-Length</a>
    * header. For requests using transport encoding, this should be the
    * compressed size.
    */
  val HttpRequestContentLength: AttributeKey[Long] = long(
    "http.request_content_length"
  )

  /** The size of the response payload body in bytes. This is the number of
    * bytes transferred excluding headers and is often, but not always, present
    * as the <a
    * href="https://www.rfc-editor.org/rfc/rfc9110.html#field.content-length">Content-Length</a>
    * header. For requests using transport encoding, this should be the
    * compressed size.
    */
  val HttpResponseContentLength: AttributeKey[Long] = long(
    "http.response_content_length"
  )

  /** Full HTTP request URL in the form {@code
    * scheme://host[:port]/path?query[#fragment]}. Usually the fragment is not
    * transmitted over HTTP, but if it is known, it should be included
    * nevertheless.
    *
    * <p>Notes: <ul> <li>{@code http.url} MUST NOT contain credentials passed
    * via URL in form of {@code https://username:password@www.example.com/}. In
    * such case the attribute's value should be {@code
    * https://www.example.com/}.</li> </ul>
    */
  val HttpUrl: AttributeKey[String] = string("http.url")

  /** The ordinal number of request resending attempt (for any reason, including
    * redirects).
    *
    * <p>Notes: <ul> <li>The resend count SHOULD be updated each time an HTTP
    * request gets resent by the client, regardless of what was the cause of the
    * resending (e.g. redirection, authorization failure, 503 Server
    * Unavailable, network issues, or any other).</li> </ul>
    */
  val HttpResendCount: AttributeKey[Long] = long("http.resend_count")

  /** The full request target as passed in a HTTP request line or equivalent.
    */
  val HttpTarget: AttributeKey[String] = string("http.target")

  /** The IP address of the original client behind all proxies, if known (e.g.
    * from <a
    * href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Forwarded-For">X-Forwarded-For</a>).
    *
    * <p>Notes: <ul> <li>This is not necessarily the same as {@code
    * net.sock.peer.addr}, which would identify the network-level peer, which
    * may be a proxy.</li><li>This attribute should be set when a source of
    * information different from the one used for {@code net.sock.peer.addr}, is
    * available even if that other source just confirms the same value as {@code
    * net.sock.peer.addr}. Rationale: For {@code net.sock.peer.addr}, one
    * typically does not know if it comes from a proxy, reverse proxy, or the
    * actual client. Setting {@code http.client_ip} when it's the same as {@code
    * net.sock.peer.addr} means that one is at least somewhat confident that the
    * address is not that of the closest proxy.</li> </ul>
    */
  val HttpClientIp: AttributeKey[String] = string("http.client_ip")

  /** The keys in the {@code RequestItems} object field.
    */
  val AwsDynamodbTableNames: AttributeKey[List[String]] = stringList(
    "aws.dynamodb.table_names"
  )

  /** The JSON-serialized value of each item in the {@code ConsumedCapacity}
    * response field.
    */
  val AwsDynamodbConsumedCapacity: AttributeKey[List[String]] = stringList(
    "aws.dynamodb.consumed_capacity"
  )

  /** The JSON-serialized value of the {@code ItemCollectionMetrics} response
    * field.
    */
  val AwsDynamodbItemCollectionMetrics: AttributeKey[String] = string(
    "aws.dynamodb.item_collection_metrics"
  )

  /** The value of the {@code ProvisionedThroughput.ReadCapacityUnits} request
    * parameter.
    */
  val AwsDynamodbProvisionedReadCapacity: AttributeKey[Double] = double(
    "aws.dynamodb.provisioned_read_capacity"
  )

  /** The value of the {@code ProvisionedThroughput.WriteCapacityUnits} request
    * parameter.
    */
  val AwsDynamodbProvisionedWriteCapacity: AttributeKey[Double] = double(
    "aws.dynamodb.provisioned_write_capacity"
  )

  /** The value of the {@code ConsistentRead} request parameter.
    */
  val AwsDynamodbConsistentRead: AttributeKey[Boolean] = boolean(
    "aws.dynamodb.consistent_read"
  )

  /** The value of the {@code ProjectionExpression} request parameter.
    */
  val AwsDynamodbProjection: AttributeKey[String] = string(
    "aws.dynamodb.projection"
  )

  /** The value of the {@code Limit} request parameter.
    */
  val AwsDynamodbLimit: AttributeKey[Long] = long("aws.dynamodb.limit")

  /** The value of the {@code AttributesToGet} request parameter.
    */
  val AwsDynamodbAttributesToGet: AttributeKey[List[String]] = stringList(
    "aws.dynamodb.attributes_to_get"
  )

  /** The value of the {@code IndexName} request parameter.
    */
  val AwsDynamodbIndexName: AttributeKey[String] = string(
    "aws.dynamodb.index_name"
  )

  /** The value of the {@code Select} request parameter.
    */
  val AwsDynamodbSelect: AttributeKey[String] = string("aws.dynamodb.select")

  /** The JSON-serialized value of each item of the {@code
    * GlobalSecondaryIndexes} request field
    */
  val AwsDynamodbGlobalSecondaryIndexes: AttributeKey[List[String]] =
    stringList("aws.dynamodb.global_secondary_indexes")

  /** The JSON-serialized value of each item of the {@code
    * LocalSecondaryIndexes} request field.
    */
  val AwsDynamodbLocalSecondaryIndexes: AttributeKey[List[String]] = stringList(
    "aws.dynamodb.local_secondary_indexes"
  )

  /** The value of the {@code ExclusiveStartTableName} request parameter.
    */
  val AwsDynamodbExclusiveStartTable: AttributeKey[String] = string(
    "aws.dynamodb.exclusive_start_table"
  )

  /** The the number of items in the {@code TableNames} response parameter.
    */
  val AwsDynamodbTableCount: AttributeKey[Long] = long(
    "aws.dynamodb.table_count"
  )

  /** The value of the {@code ScanIndexForward} request parameter.
    */
  val AwsDynamodbScanForward: AttributeKey[Boolean] = boolean(
    "aws.dynamodb.scan_forward"
  )

  /** The value of the {@code Segment} request parameter.
    */
  val AwsDynamodbSegment: AttributeKey[Long] = long("aws.dynamodb.segment")

  /** The value of the {@code TotalSegments} request parameter.
    */
  val AwsDynamodbTotalSegments: AttributeKey[Long] = long(
    "aws.dynamodb.total_segments"
  )

  /** The value of the {@code Count} response parameter.
    */
  val AwsDynamodbCount: AttributeKey[Long] = long("aws.dynamodb.count")

  /** The value of the {@code ScannedCount} response parameter.
    */
  val AwsDynamodbScannedCount: AttributeKey[Long] = long(
    "aws.dynamodb.scanned_count"
  )

  /** The JSON-serialized value of each item in the {@code AttributeDefinitions}
    * request field.
    */
  val AwsDynamodbAttributeDefinitions: AttributeKey[List[String]] = stringList(
    "aws.dynamodb.attribute_definitions"
  )

  /** The JSON-serialized value of each item in the the {@code
    * GlobalSecondaryIndexUpdates} request field.
    */
  val AwsDynamodbGlobalSecondaryIndexUpdates: AttributeKey[List[String]] =
    stringList("aws.dynamodb.global_secondary_index_updates")

  /** The name of the operation being executed.
    */
  val GraphqlOperationName: AttributeKey[String] = string(
    "graphql.operation.name"
  )

  /** The type of the operation being executed.
    */
  val GraphqlOperationType: AttributeKey[String] = string(
    "graphql.operation.type"
  )

  /** The GraphQL document being executed.
    *
    * <p>Notes: <ul> <li>The value may be sanitized to exclude sensitive
    * information.</li> </ul>
    */
  val GraphqlDocument: AttributeKey[String] = string("graphql.document")

  /** A value used by the messaging system as an identifier for the message,
    * represented as a string.
    */
  val MessagingMessageId: AttributeKey[String] = string("messaging.message.id")

  /** The <a href="#conversations">conversation ID</a> identifying the
    * conversation to which the message belongs, represented as a string.
    * Sometimes called &quot;Correlation ID&quot;.
    */
  val MessagingMessageConversationId: AttributeKey[String] = string(
    "messaging.message.conversation_id"
  )

  /** The (uncompressed) size of the message payload in bytes. Also use this
    * attribute if it is unknown whether the compressed or uncompressed payload
    * size is reported.
    */
  val MessagingMessagePayloadSizeBytes: AttributeKey[Long] = long(
    "messaging.message.payload_size_bytes"
  )

  /** The compressed size of the message payload in bytes.
    */
  val MessagingMessagePayloadCompressedSizeBytes: AttributeKey[Long] = long(
    "messaging.message.payload_compressed_size_bytes"
  )

  /** The message destination name
    *
    * <p>Notes: <ul> <li>Destination name SHOULD uniquely identify a specific
    * queue, topic or other entity within the broker. If the broker does not
    * have such notion, the destination name SHOULD uniquely identify the
    * broker.</li> </ul>
    */
  val MessagingDestinationName: AttributeKey[String] = string(
    "messaging.destination.name"
  )

  /** The kind of message destination
    */
  val MessagingDestinationKind: AttributeKey[String] = string(
    "messaging.destination.kind"
  )

  /** Low cardinality representation of the messaging destination name
    *
    * <p>Notes: <ul> <li>Destination names could be constructed from templates.
    * An example would be a destination name involving a user name or product
    * id. Although the destination name in this case is of high cardinality, the
    * underlying template is of low cardinality and can be effectively used for
    * grouping and aggregation.</li> </ul>
    */
  val MessagingDestinationTemplate: AttributeKey[String] = string(
    "messaging.destination.template"
  )

  /** A boolean that is true if the message destination is temporary and might
    * not exist anymore after messages are processed.
    */
  val MessagingDestinationTemporary: AttributeKey[Boolean] = boolean(
    "messaging.destination.temporary"
  )

  /** A boolean that is true if the message destination is anonymous (could be
    * unnamed or have auto-generated name).
    */
  val MessagingDestinationAnonymous: AttributeKey[Boolean] = boolean(
    "messaging.destination.anonymous"
  )

  /** The message source name
    *
    * <p>Notes: <ul> <li>Source name SHOULD uniquely identify a specific queue,
    * topic, or other entity within the broker. If the broker does not have such
    * notion, the source name SHOULD uniquely identify the broker.</li> </ul>
    */
  val MessagingSourceName: AttributeKey[String] = string(
    "messaging.source.name"
  )

  /** The kind of message source
    */
  val MessagingSourceKind: AttributeKey[String] = string(
    "messaging.source.kind"
  )

  /** Low cardinality representation of the messaging source name
    *
    * <p>Notes: <ul> <li>Source names could be constructed from templates. An
    * example would be a source name involving a user name or product id.
    * Although the source name in this case is of high cardinality, the
    * underlying template is of low cardinality and can be effectively used for
    * grouping and aggregation.</li> </ul>
    */
  val MessagingSourceTemplate: AttributeKey[String] = string(
    "messaging.source.template"
  )

  /** A boolean that is true if the message source is temporary and might not
    * exist anymore after messages are processed.
    */
  val MessagingSourceTemporary: AttributeKey[Boolean] = boolean(
    "messaging.source.temporary"
  )

  /** A boolean that is true if the message source is anonymous (could be
    * unnamed or have auto-generated name).
    */
  val MessagingSourceAnonymous: AttributeKey[Boolean] = boolean(
    "messaging.source.anonymous"
  )

  /** A string identifying the messaging system.
    */
  val MessagingSystem: AttributeKey[String] = string("messaging.system")

  /** A string identifying the kind of messaging operation as defined in the <a
    * href="#operation-names">Operation names</a> section above.
    *
    * <p>Notes: <ul> <li>If a custom value is used, it MUST be of low
    * cardinality.</li> </ul>
    */
  val MessagingOperation: AttributeKey[String] = string("messaging.operation")

  /** The number of messages sent, received, or processed in the scope of the
    * batching operation.
    *
    * <p>Notes: <ul> <li>Instrumentations SHOULD NOT set {@code
    * messaging.batch.message_count} on spans that operate with a single
    * message. When a messaging client library supports both batch and
    * single-message API for the same operation, instrumentations SHOULD use
    * {@code messaging.batch.message_count} for batching APIs and SHOULD NOT use
    * it for single-message APIs.</li> </ul>
    */
  val MessagingBatchMessageCount: AttributeKey[Long] = long(
    "messaging.batch.message_count"
  )

  /** The identifier for the consumer receiving a message. For Kafka, set it to
    * {@code {messaging.kafka.consumer.group} - {messaging.kafka.client_id}}, if
    * both are present, or only {@code messaging.kafka.consumer.group}. For
    * brokers, such as RabbitMQ and Artemis, set it to the {@code client_id} of
    * the client consuming the message.
    */
  val MessagingConsumerId: AttributeKey[String] = string(
    "messaging.consumer.id"
  )

  /** RabbitMQ message routing key.
    */
  val MessagingRabbitmqDestinationRoutingKey: AttributeKey[String] = string(
    "messaging.rabbitmq.destination.routing_key"
  )

  /** Message keys in Kafka are used for grouping alike messages to ensure
    * they're processed on the same partition. They differ from {@code
    * messaging.message.id} in that they're not unique. If the key is {@code
    * null}, the attribute MUST NOT be set.
    *
    * <p>Notes: <ul> <li>If the key type is not string, it's string
    * representation has to be supplied for the attribute. If the key has no
    * unambiguous, canonical string form, don't include its value.</li> </ul>
    */
  val MessagingKafkaMessageKey: AttributeKey[String] = string(
    "messaging.kafka.message.key"
  )

  /** Name of the Kafka Consumer Group that is handling the message. Only
    * applies to consumers, not producers.
    */
  val MessagingKafkaConsumerGroup: AttributeKey[String] = string(
    "messaging.kafka.consumer.group"
  )

  /** Client Id for the Consumer or Producer that is handling the message.
    */
  val MessagingKafkaClientId: AttributeKey[String] = string(
    "messaging.kafka.client_id"
  )

  /** Partition the message is sent to.
    */
  val MessagingKafkaDestinationPartition: AttributeKey[Long] = long(
    "messaging.kafka.destination.partition"
  )

  /** Partition the message is received from.
    */
  val MessagingKafkaSourcePartition: AttributeKey[Long] = long(
    "messaging.kafka.source.partition"
  )

  /** The offset of a record in the corresponding Kafka partition.
    */
  val MessagingKafkaMessageOffset: AttributeKey[Long] = long(
    "messaging.kafka.message.offset"
  )

  /** A boolean that is true if the message is a tombstone.
    */
  val MessagingKafkaMessageTombstone: AttributeKey[Boolean] = boolean(
    "messaging.kafka.message.tombstone"
  )

  /** Namespace of RocketMQ resources, resources in different namespaces are
    * individual.
    */
  val MessagingRocketmqNamespace: AttributeKey[String] = string(
    "messaging.rocketmq.namespace"
  )

  /** Name of the RocketMQ producer/consumer group that is handling the message.
    * The client type is identified by the SpanKind.
    */
  val MessagingRocketmqClientGroup: AttributeKey[String] = string(
    "messaging.rocketmq.client_group"
  )

  /** The unique identifier for each client.
    */
  val MessagingRocketmqClientId: AttributeKey[String] = string(
    "messaging.rocketmq.client_id"
  )

  /** The timestamp in milliseconds that the delay message is expected to be
    * delivered to consumer.
    */
  val MessagingRocketmqMessageDeliveryTimestamp: AttributeKey[Long] = long(
    "messaging.rocketmq.message.delivery_timestamp"
  )

  /** The delay time level for delay message, which determines the message delay
    * time.
    */
  val MessagingRocketmqMessageDelayTimeLevel: AttributeKey[Long] = long(
    "messaging.rocketmq.message.delay_time_level"
  )

  /** It is essential for FIFO message. Messages that belong to the same message
    * group are always processed one by one within the same consumer group.
    */
  val MessagingRocketmqMessageGroup: AttributeKey[String] = string(
    "messaging.rocketmq.message.group"
  )

  /** Type of message.
    */
  val MessagingRocketmqMessageType: AttributeKey[String] = string(
    "messaging.rocketmq.message.type"
  )

  /** The secondary classifier of message besides topic.
    */
  val MessagingRocketmqMessageTag: AttributeKey[String] = string(
    "messaging.rocketmq.message.tag"
  )

  /** Key(s) of message, another way to mark message besides message id.
    */
  val MessagingRocketmqMessageKeys: AttributeKey[List[String]] = stringList(
    "messaging.rocketmq.message.keys"
  )

  /** Model of message consumption. This only applies to consumer spans.
    */
  val MessagingRocketmqConsumptionModel: AttributeKey[String] = string(
    "messaging.rocketmq.consumption_model"
  )

  /** A string identifying the remoting system. See below for a list of
    * well-known identifiers.
    */
  val RpcSystem: AttributeKey[String] = string("rpc.system")

  /** The full (logical) name of the service being called, including its package
    * name, if applicable.
    *
    * <p>Notes: <ul> <li>This is the logical name of the service from the RPC
    * interface perspective, which can be different from the name of any
    * implementing class. The {@code code.namespace} attribute may be used to
    * store the latter (despite the attribute name, it may include a class name;
    * e.g., class with method actually executing the call on the server side,
    * RPC client stub class on the client side).</li> </ul>
    */
  val RpcService: AttributeKey[String] = string("rpc.service")

  /** The name of the (logical) method being called, must be equal to the
    * $method part in the span name.
    *
    * <p>Notes: <ul> <li>This is the logical name of the method from the RPC
    * interface perspective, which can be different from the name of any
    * implementing method/function. The {@code code.function} attribute may be
    * used to store the latter (e.g., method actually executing the call on the
    * server side, RPC client stub method on the client side).</li> </ul>
    */
  val RpcMethod: AttributeKey[String] = string("rpc.method")

  /** The <a
    * href="https://github.com/grpc/grpc/blob/v1.33.2/doc/statuscodes.md">numeric
    * status code</a> of the gRPC request.
    */
  val RpcGrpcStatusCode: AttributeKey[Long] = long("rpc.grpc.status_code")

  /** Protocol version as in {@code jsonrpc} property of request/response. Since
    * JSON-RPC 1.0 does not specify this, the value can be omitted.
    */
  val RpcJsonrpcVersion: AttributeKey[String] = string("rpc.jsonrpc.version")

  /** {@code id} property of request or response. Since protocol allows id to be
    * int, string, {@code null} or missing (for notifications), value is
    * expected to be cast to string for simplicity. Use empty string in case of
    * {@code null} value. Omit entirely if this is a notification.
    */
  val RpcJsonrpcRequestId: AttributeKey[String] = string(
    "rpc.jsonrpc.request_id"
  )

  /** {@code error.code} property of response if it is an error response.
    */
  val RpcJsonrpcErrorCode: AttributeKey[Long] = long("rpc.jsonrpc.error_code")

  /** {@code error.message} property of response if it is an error response.
    */
  val RpcJsonrpcErrorMessage: AttributeKey[String] = string(
    "rpc.jsonrpc.error_message"
  )

  /** Whether this is a received or sent message.
    */
  val MessageType: AttributeKey[String] = string("message.type")

  /** MUST be calculated as two different counters starting from {@code 1} one
    * for sent messages and one for received message.
    *
    * <p>Notes: <ul> <li>This way we guarantee that the values will be
    * consistent between different implementations.</li> </ul>
    */
  val MessageId: AttributeKey[Long] = long("message.id")

  /** Compressed size of the message in bytes.
    */
  val MessageCompressedSize: AttributeKey[Long] = long(
    "message.compressed_size"
  )

  /** Uncompressed size of the message in bytes.
    */
  val MessageUncompressedSize: AttributeKey[Long] = long(
    "message.uncompressed_size"
  )

  /** The <a href="https://connect.build/docs/protocol/#error-codes">error
    * codes</a> of the Connect request. Error codes are always string values.
    */
  val RpcConnectRpcErrorCode: AttributeKey[String] = string(
    "rpc.connect_rpc.error_code"
  )

  /** SHOULD be set to true if the exception event is recorded at a point where
    * it is known that the exception is escaping the scope of the span.
    *
    * <p>Notes: <ul> <li>An exception is considered to have escaped (or left)
    * the scope of a span, if that span is ended while the exception is still
    * logically &quot;in flight&quot;. This may be actually &quot;in
    * flight&quot; in some languages (e.g. if the exception is passed to a
    * Context manager's {@code __exit__} method in Python) but will usually be
    * caught at the point of recording the exception in most
    * languages.</li><li>It is usually not possible to determine at the point
    * where an exception is thrown whether it will escape the scope of a span.
    * However, it is trivial to know that an exception will escape, if one
    * checks for an active exception just before ending the span, as done in the
    * <a href="#recording-an-exception">example above</a>.</li><li>It follows
    * that an exception may still escape the scope of the span even if the
    * {@code exception.escaped} attribute was not set or set to false, since the
    * event might have been recorded at a time where it was not clear whether
    * the exception will escape.</li> </ul>
    */
  val ExceptionEscaped: AttributeKey[Boolean] = boolean("exception.escaped")

  /** Value of the <a
    * href="https://www.rfc-editor.org/rfc/rfc9110.html#field.user-agent">HTTP
    * User-Agent</a> header sent by the client.
    */
  val UserAgentOriginal: AttributeKey[String] = string("user_agent.original")

  // Enum definitions
  abstract class HttpFlavorValue(value: String)
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

  abstract class EventDomainValue(value: String)
  object EventDomainValue {

    /** Events from browser apps. */
    case object Browser extends EventDomainValue("browser")

    /** Events from mobile apps. */
    case object Device extends EventDomainValue("device")

    /** Events from Kubernetes. */
    case object K8s extends EventDomainValue("k8s")

  }

  abstract class OpentracingRefTypeValue(value: String)
  object OpentracingRefTypeValue {

    /** The parent Span depends on the child Span in some capacity. */
    case object ChildOf extends OpentracingRefTypeValue("child_of")

    /** The parent Span does not depend in any way on the result of the child
      * Span.
      */
    case object FollowsFrom extends OpentracingRefTypeValue("follows_from")

  }

  abstract class DbSystemValue(value: String)
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

  }

  abstract class DbCassandraConsistencyLevelValue(value: String)
  object DbCassandraConsistencyLevelValue {

    /** all. */
    case object All extends DbCassandraConsistencyLevelValue("all")

    /** each_quorum. */
    case object EachQuorum
        extends DbCassandraConsistencyLevelValue("each_quorum")

    /** quorum. */
    case object Quorum extends DbCassandraConsistencyLevelValue("quorum")

    /** local_quorum. */
    case object LocalQuorum
        extends DbCassandraConsistencyLevelValue("local_quorum")

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
    case object LocalSerial
        extends DbCassandraConsistencyLevelValue("local_serial")

  }

  abstract class OtelStatusCodeValue(value: String)
  object OtelStatusCodeValue {

    /** The operation has been validated by an Application developer or Operator
      * to have completed successfully.
      */
    case object Ok extends OtelStatusCodeValue("OK")

    /** The operation contains an error. */
    case object Error extends OtelStatusCodeValue("ERROR")

  }

  abstract class FaasTriggerValue(value: String)
  object FaasTriggerValue {

    /** A response to some data source operation such as a database or
      * filesystem read/write.
      */
    case object Datasource extends FaasTriggerValue("datasource")

    /** To provide an answer to an inbound HTTP request. */
    case object Http extends FaasTriggerValue("http")

    /** A function is set to be executed when messages are sent to a messaging
      * system.
      */
    case object Pubsub extends FaasTriggerValue("pubsub")

    /** A function is scheduled to be executed regularly. */
    case object Timer extends FaasTriggerValue("timer")

    /** If none of the others apply. */
    case object Other extends FaasTriggerValue("other")

  }

  abstract class FaasDocumentOperationValue(value: String)
  object FaasDocumentOperationValue {

    /** When a new object is created. */
    case object Insert extends FaasDocumentOperationValue("insert")

    /** When an object is modified. */
    case object Edit extends FaasDocumentOperationValue("edit")

    /** When an object is deleted. */
    case object Delete extends FaasDocumentOperationValue("delete")

  }

  abstract class FaasInvokedProviderValue(value: String)
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

  abstract class NetTransportValue(value: String)
  object NetTransportValue {

    /** ip_tcp. */
    case object IpTcp extends NetTransportValue("ip_tcp")

    /** ip_udp. */
    case object IpUdp extends NetTransportValue("ip_udp")

    /** Named or anonymous pipe. See note below. */
    case object Pipe extends NetTransportValue("pipe")

    /** In-process communication. */
    case object Inproc extends NetTransportValue("inproc")

    /** Something else (non IP-based). */
    case object Other extends NetTransportValue("other")

    /** @deprecated
      *   This item has been removed as of 1.13.0 of the semantic conventions.
      */
    @deprecated("This item has been removed", "1.13.0")
    case object Ip extends NetTransportValue("ip")

    /** @deprecated
      *   This item has been removed as of 1.13.0 of the semantic conventions.
      */
    @deprecated("This item has been removed", "1.13.0")
    case object Unix extends NetTransportValue("unix")

  }

  abstract class NetSockFamilyValue(value: String)
  object NetSockFamilyValue {

    /** IPv4 address. */
    case object Inet extends NetSockFamilyValue("inet")

    /** IPv6 address. */
    case object Inet6 extends NetSockFamilyValue("inet6")

    /** Unix domain socket path. */
    case object Unix extends NetSockFamilyValue("unix")

  }

  abstract class NetHostConnectionTypeValue(value: String)
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

  abstract class NetHostConnectionSubtypeValue(value: String)
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
    case object Cdma20001xrtt
        extends NetHostConnectionSubtypeValue("cdma2000_1xrtt")

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

  abstract class GraphqlOperationTypeValue(value: String)
  object GraphqlOperationTypeValue {

    /** GraphQL query. */
    case object Query extends GraphqlOperationTypeValue("query")

    /** GraphQL mutation. */
    case object Mutation extends GraphqlOperationTypeValue("mutation")

    /** GraphQL subscription. */
    case object Subscription extends GraphqlOperationTypeValue("subscription")

  }

  abstract class MessagingDestinationKindValue(value: String)
  object MessagingDestinationKindValue {

    /** A message sent to a queue. */
    case object Queue extends MessagingDestinationKindValue("queue")

    /** A message sent to a topic. */
    case object Topic extends MessagingDestinationKindValue("topic")

  }

  abstract class MessagingSourceKindValue(value: String)
  object MessagingSourceKindValue {

    /** A message received from a queue. */
    case object Queue extends MessagingSourceKindValue("queue")

    /** A message received from a topic. */
    case object Topic extends MessagingSourceKindValue("topic")

  }

  abstract class MessagingOperationValue(value: String)
  object MessagingOperationValue {

    /** publish. */
    case object Publish extends MessagingOperationValue("publish")

    /** receive. */
    case object Receive extends MessagingOperationValue("receive")

    /** process. */
    case object Process extends MessagingOperationValue("process")

  }

  abstract class MessagingRocketmqMessageTypeValue(value: String)
  object MessagingRocketmqMessageTypeValue {

    /** Normal message. */
    case object Normal extends MessagingRocketmqMessageTypeValue("normal")

    /** FIFO message. */
    case object Fifo extends MessagingRocketmqMessageTypeValue("fifo")

    /** Delay message. */
    case object Delay extends MessagingRocketmqMessageTypeValue("delay")

    /** Transaction message. */
    case object Transaction
        extends MessagingRocketmqMessageTypeValue("transaction")

  }

  abstract class MessagingRocketmqConsumptionModelValue(value: String)
  object MessagingRocketmqConsumptionModelValue {

    /** Clustering consumption model. */
    case object Clustering
        extends MessagingRocketmqConsumptionModelValue("clustering")

    /** Broadcasting consumption model. */
    case object Broadcasting
        extends MessagingRocketmqConsumptionModelValue("broadcasting")

  }

  abstract class RpcSystemValue(value: String)
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

  abstract class RpcGrpcStatusCodeValue(value: Long)
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

  abstract class MessageTypeValue(value: String)
  object MessageTypeValue {

    /** sent. */
    case object Sent extends MessageTypeValue("SENT")

    /** received. */
    case object Received extends MessageTypeValue("RECEIVED")

  }

  abstract class RpcConnectRpcErrorCodeValue(value: String)
  object RpcConnectRpcErrorCodeValue {

    /** cancelled. */
    case object Cancelled extends RpcConnectRpcErrorCodeValue("cancelled")

    /** unknown. */
    case object Unknown extends RpcConnectRpcErrorCodeValue("unknown")

    /** invalid_argument. */
    case object InvalidArgument
        extends RpcConnectRpcErrorCodeValue("invalid_argument")

    /** deadline_exceeded. */
    case object DeadlineExceeded
        extends RpcConnectRpcErrorCodeValue("deadline_exceeded")

    /** not_found. */
    case object NotFound extends RpcConnectRpcErrorCodeValue("not_found")

    /** already_exists. */
    case object AlreadyExists
        extends RpcConnectRpcErrorCodeValue("already_exists")

    /** permission_denied. */
    case object PermissionDenied
        extends RpcConnectRpcErrorCodeValue("permission_denied")

    /** resource_exhausted. */
    case object ResourceExhausted
        extends RpcConnectRpcErrorCodeValue("resource_exhausted")

    /** failed_precondition. */
    case object FailedPrecondition
        extends RpcConnectRpcErrorCodeValue("failed_precondition")

    /** aborted. */
    case object Aborted extends RpcConnectRpcErrorCodeValue("aborted")

    /** out_of_range. */
    case object OutOfRange extends RpcConnectRpcErrorCodeValue("out_of_range")

    /** unimplemented. */
    case object Unimplemented
        extends RpcConnectRpcErrorCodeValue("unimplemented")

    /** internal. */
    case object Internal extends RpcConnectRpcErrorCodeValue("internal")

    /** unavailable. */
    case object Unavailable extends RpcConnectRpcErrorCodeValue("unavailable")

    /** data_loss. */
    case object DataLoss extends RpcConnectRpcErrorCodeValue("data_loss")

    /** unauthenticated. */
    case object Unauthenticated
        extends RpcConnectRpcErrorCodeValue("unauthenticated")

  }

  // Manually defined and not YET in the YAML
  /** The name of an event describing an exception.
    *
    * <p>Typically an event with that name should not be manually created.
    * Instead {@link org.typelevel.otel4s.trace.Span#recordException(Throwable)}
    * should be used.
    */
  final val ExceptionEventName = "exception"

  /** The name of the keyspace being accessed.
    *
    * @deprecated
    *   this item has been removed as of 1.8.0 of the semantic conventions.
    *   Please use {@link SemanticAttributes#DbName} instead.
    */
  @deprecated("Use SemanticAttributes.DbName instead", "1.8.0")
  val DbCassandraKeyspace = string("db.cassandra.keyspace")

  /** The <a href="https://hbase.apache.org/book.html#_namespace">HBase
    * namespace</a> being accessed.
    *
    * @deprecated
    *   this item has been removed as of 1.8.0 of the semantic conventions.
    *   Please use {@link SemanticAttributes#DbName} instead.
    */
  @deprecated("Use SemanticAttributes.DbName instead", "1.8.0")
  val DbHbaseNameSpace = string("db.hbase.namespace")

  /** The size of the uncompressed request payload body after transport
    * decoding. Not set if transport encoding not used.
    *
    * @deprecated
    *   this item has been removed as of 1.13.0 of the semantic conventions.
    *   Please use {@link SemanticAttributes#HttpRequestContentLength} instead.
    */
  @deprecated(
    "Use SemanticAttributes.HttpRequestContentLength instead",
    "1.13.0"
  )
  val HttpRequestContentLengthUncompressed = long(
    "http.request_content_length_uncompressed"
  )

  /** @deprecated
    *   This item has been removed as of 1.13.0 of the semantic conventions.
    *   Please use {@link SemanticAttributes#HttpResponseContentLength} instead.
    */
  @deprecated(
    "Use SemanticAttributes.HttpResponseContentLength instead",
    "1.13.0"
  )
  val HttpResponseContentLengthUncompressed = long(
    "http.response_content_length_uncompressed"
  )

  /** @deprecated
    *   This item has been removed as of 1.13.0 of the semantic conventions.
    *   Please use {@link SemanticAttributes#NetHostName} instead.
    */
  @deprecated("Use SemanticAttributes.NetHostName instead", "1.13.0")
  val HttpServerName = string("http.server_name")

  /** @deprecated
    *   This item has been removed as of 1.13.0 of the semantic conventions.
    *   Please use {@link SemanticAttributes#NetHostName} instead.
    */
  @deprecated("Use SemanticAttributes.NetHostName instead", "1.13.0")
  val HttpHost = string("http.host")

  /** @deprecated
    *   This item has been removed as of 1.13.0 of the semantic conventions.
    *   Please use {@link SemanticAttributes#NetSockPeerAddr} instead.
    */
  @deprecated("Use SemanticAttributes.NetSockPeerAddr instead", "1.13.0")
  val NetPeerIp = string("net.peer.ip")

  /** @deprecated
    *   This item has been removed as of 1.13.0 of the semantic conventions.
    *   Please use {@link SemanticAttributes#NetSockHostAddr} instead.
    */
  @deprecated("Use SemanticAttributes.NetSockHostAddr instead", "1.13.0")
  val NetHostIp = string("net.host.ip")

  /** The ordinal number of request re-sending attempt.
    * @deprecated
    *   This item has been removed as of 1.15.0 of the semantic conventions. Use
    *   {@link SemanticAttributes#HttpResendCount} instead.
    */
  @deprecated("Use SemanticAttributes.HttpResendCount instead", "1.15.0")
  val HttpRetryCount = long("http.retry_count")

  /** A string identifying the messaging system.
    * @deprecated
    *   This item has been removed as of 1.17.0 of the semantic conventions. Use
    *   {@link SemanticAttributes#MessagingDestinationName} instead.
    */
  @deprecated(
    "Use SemanticAttributes.MessagingDestinationName instead",
    "1.17.0"
  )
  val MessagingDestination = string("messaging.destination")

  /** A boolean that is true if the message destination is temporary.
    * @deprecated
    *   This item has been removed as of 1.17.0 of the semantic conventions. Use
    *   {@link SemanticAttributes#MessagingDestinationTemporary} instead.
    */
  @deprecated(
    "Use SemanticAttributes.MessagingDestinationTemporary instead",
    "1.17.0"
  )
  val MessagingTempDestination = boolean("messaging.temp_destination")

  /** The name of the transport protocol.
    * @deprecated
    *   This item has been removed as of 1.17.0 of the semantic conventions. Use
    *   {@link SemanticAttributes#NetAppProtocolName} instead.
    */
  @deprecated("Use SemanticAttributes.NetAppProtocolName instead", "1.17.0")
  val MessagingProtocol = string("messaging.protocol")

  /** The version of the transport protocol.
    * @deprecated
    *   This item has been removed as of 1.17.0 of the semantic conventions. Use
    *   {@link SemanticAttributes#NetAppProtocolVersion} instead.
    */
  @deprecated("Use SemanticAttributes.NetAppProtocolVersion instead", "1.17.0")
  val MessagingProtocolVersion = string("messaging.protocol_version")

  /** Connection string.
    * @deprecated
    *   This item has been removed as of 1.17.0 of the semantic conventions.
    *   There is no replacement.
    */
  @deprecated("There is no replacement", "1.17.0")
  val MessagingUrl = string("messaging.url")

  /** The <a href="#conversations">conversation ID</a> identifying the
    * conversation to which the message belongs, represented as a string.
    * Sometimes called &quot;Correlation ID&quot;.
    * @deprecated
    *   This item has been removed as of 1.17.0 of the semantic conventions. Use
    *   {@link SemanticAttributes#MessagingMessageConversationId} instead.
    */
  @deprecated(
    "Use SemanticAttributes.MessagingMessageConversationId instead",
    "1.17.0"
  )
  val MessagingConversationId = string("messaging.conversation_id")

  /** RabbitMQ message routing key.
    * @deprecated
    *   This item has been removed as of 1.17.0 of the semantic conventions. Use
    *   {@link SemanticAttributes#MessagingRabbitmqDestinationRoutingKey}
    *   instead.
    */
  @deprecated(
    "Use SemanticAttributes.MessagingRabbitmqDestinationRoutingKey instead",
    "1.17.0"
  )
  val MessagingRabbitmqRoutingKey = string("messaging.rabbitmq.routing_key")

  /** Partition the message is received from.
    * @deprecated
    *   This item has been removed as of 1.17.0 of the semantic conventions. Use
    *   {@link SemanticAttributes#MessagingKafkaSourcePartition} instead.
    */
  @deprecated(
    "Use SemanticAttributes.MessagingKafkaSourcePartition instead",
    "1.17.0"
  )
  val MessagingKafkaPartition = long("messaging.kafka.partition")

  /** A boolean that is true if the message is a tombstone.
    * @deprecated
    *   This item has been removed as of 1.17.0 of the semantic conventions. Use
    *   {@link SemanticAttributes#MessagingKafkaMessageTombstone} instead.
    */
  @deprecated(
    "Use SemanticAttributes.MessagingKafkaMessageTombstone instead",
    "1.17.0"
  )
  val MessagingKafkaTombstone = boolean("messaging.kafka.tombstone")

  /** The timestamp in milliseconds that the delay message is expected to be
    * delivered to consumer.
    * @deprecated
    *   This item has been removed as of 1.17.0 of the semantic conventions. Use
    *   {@link SemanticAttributes#MessagingRocketmqMessageDeliveryTimestamp}
    *   instead.
    */
  @deprecated(
    "Use SemanticAttributes.MessagingRocketmqMessageDeliveryTimestamp instead",
    "1.17.0"
  )
  val MessagingRocketmqDeliveryTimestamp = long(
    "messaging.rocketmq.delivery_timestamp"
  )

  /** The delay time level for delay message, which determines the message delay
    * time.
    * @deprecated
    *   This item has been removed as of 1.17.0 of the semantic conventions. Use
    *   {@link SemanticAttributes#MessagingRocketmqMessageDelayTimeLevel}
    *   instead.
    */
  @deprecated(
    "Use SemanticAttributes.MessagingRocketmqMessageDelayTimeLevel instead",
    "1.17.0"
  )
  val MessagingRocketmqDelayTimeLevel = long(
    "messaging.rocketmq.delay_time_level"
  )

  /** The name of the instrumentation scope - ({@code InstrumentationScope.Name}
    * in OTLP).
    * @deprecated
    *   This item has been moved, use {@link
    *   org.typelevel.otel4s.semconv.resource.attributes.ResourceAttributes#OtelScopeName}
    *   instead.
    */
  @deprecated("Use ResourceAttributes.OtelScopeName instead")
  val OtelScopeName = string("otel.scope.name")

  /** The version of the instrumentation scope - ({@code
    * InstrumentationScope.Version} in OTLP).
    * @deprecated
    *   This item has been moved, use {@link
    *   org.typelevel.otel4s.semconv.resource.attributes.ResourceAttributes#OtelScopeVersion}
    *   instead.
    */
  @deprecated("Use ResourceAttributes.OtelScopeVersion instead")
  val OtelScopeVersion = string("otel.scope.version")

  /** The execution ID of the current function execution.
    * @deprecated
    *   This item has been renamed in 1.19.0 version of the semantic
    *   conventions. Use {@link SemanticAttributes#FaasInvocationId} instead.
    */
  @deprecated("Use SemanticAttributes.FaasInvocationId instead", "1.19.0")
  val FaasExecution = string("faas.execution")

  /** Value of the <a
    * href="https://www.rfc-editor.org/rfc/rfc9110.html#field.user-agent">HTTP
    * User-Agent</a> header sent by the client.
    * @deprecated
    *   This item has been renamed in 1.19.0 version of the semantic
    *   conventions. Use {@link SemanticAttributes#UserAgentOriginal} instead.
    */
  @deprecated("Use SemanticAttributes.UserAgentOriginal instead", "1.19.0")
  val HttpUserAgent = string("http.user_agent")

  /** Deprecated.
    *
    * @deprecated
    *   Deprecated, use the {@link
    *   org.typelevel.otel4s.semconv.resource.attributes.ResourceAttributes#OtelScopeName}
    *   attribute.
    */
  @deprecated("Use ResourceAttributes.OtelScopeName instead")
  val OtelLibraryName = string("otel.library.name")

  /** Deprecated.
    *
    * @deprecated
    *   Deprecated, use the {@link
    *   org.typelevel.otel4s.semconv.resource.attributes.ResourceAttributes#OtelScopeVersion}
    *   attribute.
    */
  @deprecated("Use ResourceAttributes.OtelScopeVersion instead")
  val OtelLibraryVersion = string("otel.library.version")

}
