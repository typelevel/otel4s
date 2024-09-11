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

import org.typelevel.otel4s.metrics._
import org.typelevel.otel4s.semconv.attributes._
import org.typelevel.otel4s.semconv.experimental.attributes._

// DO NOT EDIT, this is an Auto-generated file from buildscripts/templates/registry/otel4s/metrics/SemanticMetrics.scala.j2
object DbExperimentalMetrics {

  /** The number of connections that are currently in state described by the `state` attribute
    */
  object ClientConnectionCount {

    val Name = "db.client.connection.count"
    val Description = "The number of connections that are currently in state described by the `state` attribute"
    val Unit = "{connection}"

    object AttributeSpecs {

      /** The name of the connection pool; unique within the instrumented application. In case the connection pool
        * implementation doesn't provide a name, instrumentation SHOULD use a combination of parameters that would make
        * the name unique, for example, combining attributes `server.address`, `server.port`, and `db.namespace`,
        * formatted as `server.address:server.port/db.namespace`. Instrumentations that generate connection pool name
        * following different patterns SHOULD document it.
        */
      val dbClientConnectionPoolName: AttributeSpec[String] =
        AttributeSpec(
          DbExperimentalAttributes.DbClientConnectionPoolName,
          List(
            "myDataSource",
          ),
          Requirement.required,
          Stability.experimental
        )

      /** The state of a connection in the pool
        */
      val dbClientConnectionState: AttributeSpec[String] =
        AttributeSpec(
          DbExperimentalAttributes.DbClientConnectionState,
          List(
            "idle",
          ),
          Requirement.required,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          dbClientConnectionPoolName,
          dbClientConnectionState,
        )
    }

    def create[F[_]: Meter]: F[UpDownCounter[F, Long]] =
      Meter[F]
        .upDownCounter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** The time it took to create a new connection
    */
  object ClientConnectionCreateTime {

    val Name = "db.client.connection.create_time"
    val Description = "The time it took to create a new connection"
    val Unit = "s"

    object AttributeSpecs {

      /** The name of the connection pool; unique within the instrumented application. In case the connection pool
        * implementation doesn't provide a name, instrumentation SHOULD use a combination of parameters that would make
        * the name unique, for example, combining attributes `server.address`, `server.port`, and `db.namespace`,
        * formatted as `server.address:server.port/db.namespace`. Instrumentations that generate connection pool name
        * following different patterns SHOULD document it.
        */
      val dbClientConnectionPoolName: AttributeSpec[String] =
        AttributeSpec(
          DbExperimentalAttributes.DbClientConnectionPoolName,
          List(
            "myDataSource",
          ),
          Requirement.required,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          dbClientConnectionPoolName,
        )
    }

    def create[F[_]: Meter](boundaries: BucketBoundaries): F[Histogram[F, Double]] =
      Meter[F]
        .histogram[Double](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

  /** The maximum number of idle open connections allowed
    */
  object ClientConnectionIdleMax {

    val Name = "db.client.connection.idle.max"
    val Description = "The maximum number of idle open connections allowed"
    val Unit = "{connection}"

    object AttributeSpecs {

      /** The name of the connection pool; unique within the instrumented application. In case the connection pool
        * implementation doesn't provide a name, instrumentation SHOULD use a combination of parameters that would make
        * the name unique, for example, combining attributes `server.address`, `server.port`, and `db.namespace`,
        * formatted as `server.address:server.port/db.namespace`. Instrumentations that generate connection pool name
        * following different patterns SHOULD document it.
        */
      val dbClientConnectionPoolName: AttributeSpec[String] =
        AttributeSpec(
          DbExperimentalAttributes.DbClientConnectionPoolName,
          List(
            "myDataSource",
          ),
          Requirement.required,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          dbClientConnectionPoolName,
        )
    }

    def create[F[_]: Meter]: F[UpDownCounter[F, Long]] =
      Meter[F]
        .upDownCounter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** The minimum number of idle open connections allowed
    */
  object ClientConnectionIdleMin {

    val Name = "db.client.connection.idle.min"
    val Description = "The minimum number of idle open connections allowed"
    val Unit = "{connection}"

    object AttributeSpecs {

      /** The name of the connection pool; unique within the instrumented application. In case the connection pool
        * implementation doesn't provide a name, instrumentation SHOULD use a combination of parameters that would make
        * the name unique, for example, combining attributes `server.address`, `server.port`, and `db.namespace`,
        * formatted as `server.address:server.port/db.namespace`. Instrumentations that generate connection pool name
        * following different patterns SHOULD document it.
        */
      val dbClientConnectionPoolName: AttributeSpec[String] =
        AttributeSpec(
          DbExperimentalAttributes.DbClientConnectionPoolName,
          List(
            "myDataSource",
          ),
          Requirement.required,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          dbClientConnectionPoolName,
        )
    }

    def create[F[_]: Meter]: F[UpDownCounter[F, Long]] =
      Meter[F]
        .upDownCounter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** The maximum number of open connections allowed
    */
  object ClientConnectionMax {

    val Name = "db.client.connection.max"
    val Description = "The maximum number of open connections allowed"
    val Unit = "{connection}"

    object AttributeSpecs {

      /** The name of the connection pool; unique within the instrumented application. In case the connection pool
        * implementation doesn't provide a name, instrumentation SHOULD use a combination of parameters that would make
        * the name unique, for example, combining attributes `server.address`, `server.port`, and `db.namespace`,
        * formatted as `server.address:server.port/db.namespace`. Instrumentations that generate connection pool name
        * following different patterns SHOULD document it.
        */
      val dbClientConnectionPoolName: AttributeSpec[String] =
        AttributeSpec(
          DbExperimentalAttributes.DbClientConnectionPoolName,
          List(
            "myDataSource",
          ),
          Requirement.required,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          dbClientConnectionPoolName,
        )
    }

    def create[F[_]: Meter]: F[UpDownCounter[F, Long]] =
      Meter[F]
        .upDownCounter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** The number of pending requests for an open connection, cumulative for the entire pool
    */
  object ClientConnectionPendingRequests {

    val Name = "db.client.connection.pending_requests"
    val Description = "The number of pending requests for an open connection, cumulative for the entire pool"
    val Unit = "{request}"

    object AttributeSpecs {

      /** The name of the connection pool; unique within the instrumented application. In case the connection pool
        * implementation doesn't provide a name, instrumentation SHOULD use a combination of parameters that would make
        * the name unique, for example, combining attributes `server.address`, `server.port`, and `db.namespace`,
        * formatted as `server.address:server.port/db.namespace`. Instrumentations that generate connection pool name
        * following different patterns SHOULD document it.
        */
      val dbClientConnectionPoolName: AttributeSpec[String] =
        AttributeSpec(
          DbExperimentalAttributes.DbClientConnectionPoolName,
          List(
            "myDataSource",
          ),
          Requirement.required,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          dbClientConnectionPoolName,
        )
    }

    def create[F[_]: Meter]: F[UpDownCounter[F, Long]] =
      Meter[F]
        .upDownCounter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** The number of connection timeouts that have occurred trying to obtain a connection from the pool
    */
  object ClientConnectionTimeouts {

    val Name = "db.client.connection.timeouts"
    val Description = "The number of connection timeouts that have occurred trying to obtain a connection from the pool"
    val Unit = "{timeout}"

    object AttributeSpecs {

      /** The name of the connection pool; unique within the instrumented application. In case the connection pool
        * implementation doesn't provide a name, instrumentation SHOULD use a combination of parameters that would make
        * the name unique, for example, combining attributes `server.address`, `server.port`, and `db.namespace`,
        * formatted as `server.address:server.port/db.namespace`. Instrumentations that generate connection pool name
        * following different patterns SHOULD document it.
        */
      val dbClientConnectionPoolName: AttributeSpec[String] =
        AttributeSpec(
          DbExperimentalAttributes.DbClientConnectionPoolName,
          List(
            "myDataSource",
          ),
          Requirement.required,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          dbClientConnectionPoolName,
        )
    }

    def create[F[_]: Meter]: F[Counter[F, Long]] =
      Meter[F]
        .counter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** The time between borrowing a connection and returning it to the pool
    */
  object ClientConnectionUseTime {

    val Name = "db.client.connection.use_time"
    val Description = "The time between borrowing a connection and returning it to the pool"
    val Unit = "s"

    object AttributeSpecs {

      /** The name of the connection pool; unique within the instrumented application. In case the connection pool
        * implementation doesn't provide a name, instrumentation SHOULD use a combination of parameters that would make
        * the name unique, for example, combining attributes `server.address`, `server.port`, and `db.namespace`,
        * formatted as `server.address:server.port/db.namespace`. Instrumentations that generate connection pool name
        * following different patterns SHOULD document it.
        */
      val dbClientConnectionPoolName: AttributeSpec[String] =
        AttributeSpec(
          DbExperimentalAttributes.DbClientConnectionPoolName,
          List(
            "myDataSource",
          ),
          Requirement.required,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          dbClientConnectionPoolName,
        )
    }

    def create[F[_]: Meter](boundaries: BucketBoundaries): F[Histogram[F, Double]] =
      Meter[F]
        .histogram[Double](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

  /** The time it took to obtain an open connection from the pool
    */
  object ClientConnectionWaitTime {

    val Name = "db.client.connection.wait_time"
    val Description = "The time it took to obtain an open connection from the pool"
    val Unit = "s"

    object AttributeSpecs {

      /** The name of the connection pool; unique within the instrumented application. In case the connection pool
        * implementation doesn't provide a name, instrumentation SHOULD use a combination of parameters that would make
        * the name unique, for example, combining attributes `server.address`, `server.port`, and `db.namespace`,
        * formatted as `server.address:server.port/db.namespace`. Instrumentations that generate connection pool name
        * following different patterns SHOULD document it.
        */
      val dbClientConnectionPoolName: AttributeSpec[String] =
        AttributeSpec(
          DbExperimentalAttributes.DbClientConnectionPoolName,
          List(
            "myDataSource",
          ),
          Requirement.required,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          dbClientConnectionPoolName,
        )
    }

    def create[F[_]: Meter](boundaries: BucketBoundaries): F[Histogram[F, Double]] =
      Meter[F]
        .histogram[Double](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

  /** Deprecated, use `db.client.connection.create_time` instead. Note: the unit also changed from `ms` to `s`.
    */
  @deprecated("Replaced by `db.client.connection.create_time`. Note: the unit also changed from `ms` to `s`.", "")
  object ClientConnectionsCreateTime {

    val Name = "db.client.connections.create_time"
    val Description =
      "Deprecated, use `db.client.connection.create_time` instead. Note: the unit also changed from `ms` to `s`."
    val Unit = "ms"

    object AttributeSpecs {

      /** Deprecated, use `db.client.connection.pool.name` instead.
        */
      @deprecated("Replaced by `db.client.connection.pool.name`.", "")
      val dbClientConnectionsPoolName: AttributeSpec[String] =
        AttributeSpec(
          DbExperimentalAttributes.DbClientConnectionsPoolName,
          List(
            "myDataSource",
          ),
          Requirement.required,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          dbClientConnectionsPoolName,
        )
    }

    def create[F[_]: Meter](boundaries: BucketBoundaries): F[Histogram[F, Double]] =
      Meter[F]
        .histogram[Double](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

  /** Deprecated, use `db.client.connection.idle.max` instead.
    */
  @deprecated("Replaced by `db.client.connection.idle.max`.", "")
  object ClientConnectionsIdleMax {

    val Name = "db.client.connections.idle.max"
    val Description = "Deprecated, use `db.client.connection.idle.max` instead."
    val Unit = "{connection}"

    object AttributeSpecs {

      /** Deprecated, use `db.client.connection.pool.name` instead.
        */
      @deprecated("Replaced by `db.client.connection.pool.name`.", "")
      val dbClientConnectionsPoolName: AttributeSpec[String] =
        AttributeSpec(
          DbExperimentalAttributes.DbClientConnectionsPoolName,
          List(
            "myDataSource",
          ),
          Requirement.required,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          dbClientConnectionsPoolName,
        )
    }

    def create[F[_]: Meter]: F[UpDownCounter[F, Long]] =
      Meter[F]
        .upDownCounter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Deprecated, use `db.client.connection.idle.min` instead.
    */
  @deprecated("Replaced by `db.client.connection.idle.min`.", "")
  object ClientConnectionsIdleMin {

    val Name = "db.client.connections.idle.min"
    val Description = "Deprecated, use `db.client.connection.idle.min` instead."
    val Unit = "{connection}"

    object AttributeSpecs {

      /** Deprecated, use `db.client.connection.pool.name` instead.
        */
      @deprecated("Replaced by `db.client.connection.pool.name`.", "")
      val dbClientConnectionsPoolName: AttributeSpec[String] =
        AttributeSpec(
          DbExperimentalAttributes.DbClientConnectionsPoolName,
          List(
            "myDataSource",
          ),
          Requirement.required,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          dbClientConnectionsPoolName,
        )
    }

    def create[F[_]: Meter]: F[UpDownCounter[F, Long]] =
      Meter[F]
        .upDownCounter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Deprecated, use `db.client.connection.max` instead.
    */
  @deprecated("Replaced by `db.client.connection.max`.", "")
  object ClientConnectionsMax {

    val Name = "db.client.connections.max"
    val Description = "Deprecated, use `db.client.connection.max` instead."
    val Unit = "{connection}"

    object AttributeSpecs {

      /** Deprecated, use `db.client.connection.pool.name` instead.
        */
      @deprecated("Replaced by `db.client.connection.pool.name`.", "")
      val dbClientConnectionsPoolName: AttributeSpec[String] =
        AttributeSpec(
          DbExperimentalAttributes.DbClientConnectionsPoolName,
          List(
            "myDataSource",
          ),
          Requirement.required,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          dbClientConnectionsPoolName,
        )
    }

    def create[F[_]: Meter]: F[UpDownCounter[F, Long]] =
      Meter[F]
        .upDownCounter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Deprecated, use `db.client.connection.pending_requests` instead.
    */
  @deprecated("Replaced by `db.client.connection.pending_requests`.", "")
  object ClientConnectionsPendingRequests {

    val Name = "db.client.connections.pending_requests"
    val Description = "Deprecated, use `db.client.connection.pending_requests` instead."
    val Unit = "{request}"

    object AttributeSpecs {

      /** Deprecated, use `db.client.connection.pool.name` instead.
        */
      @deprecated("Replaced by `db.client.connection.pool.name`.", "")
      val dbClientConnectionsPoolName: AttributeSpec[String] =
        AttributeSpec(
          DbExperimentalAttributes.DbClientConnectionsPoolName,
          List(
            "myDataSource",
          ),
          Requirement.required,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          dbClientConnectionsPoolName,
        )
    }

    def create[F[_]: Meter]: F[UpDownCounter[F, Long]] =
      Meter[F]
        .upDownCounter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Deprecated, use `db.client.connection.timeouts` instead.
    */
  @deprecated("Replaced by `db.client.connection.timeouts`.", "")
  object ClientConnectionsTimeouts {

    val Name = "db.client.connections.timeouts"
    val Description = "Deprecated, use `db.client.connection.timeouts` instead."
    val Unit = "{timeout}"

    object AttributeSpecs {

      /** Deprecated, use `db.client.connection.pool.name` instead.
        */
      @deprecated("Replaced by `db.client.connection.pool.name`.", "")
      val dbClientConnectionsPoolName: AttributeSpec[String] =
        AttributeSpec(
          DbExperimentalAttributes.DbClientConnectionsPoolName,
          List(
            "myDataSource",
          ),
          Requirement.required,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          dbClientConnectionsPoolName,
        )
    }

    def create[F[_]: Meter]: F[Counter[F, Long]] =
      Meter[F]
        .counter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Deprecated, use `db.client.connection.count` instead.
    */
  @deprecated("Replaced by `db.client.connection.count`.", "")
  object ClientConnectionsUsage {

    val Name = "db.client.connections.usage"
    val Description = "Deprecated, use `db.client.connection.count` instead."
    val Unit = "{connection}"

    object AttributeSpecs {

      /** Deprecated, use `db.client.connection.pool.name` instead.
        */
      @deprecated("Replaced by `db.client.connection.pool.name`.", "")
      val dbClientConnectionsPoolName: AttributeSpec[String] =
        AttributeSpec(
          DbExperimentalAttributes.DbClientConnectionsPoolName,
          List(
            "myDataSource",
          ),
          Requirement.required,
          Stability.experimental
        )

      /** Deprecated, use `db.client.connection.state` instead.
        */
      @deprecated("Replaced by `db.client.connection.state`.", "")
      val dbClientConnectionsState: AttributeSpec[String] =
        AttributeSpec(
          DbExperimentalAttributes.DbClientConnectionsState,
          List(
            "idle",
          ),
          Requirement.required,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          dbClientConnectionsPoolName,
          dbClientConnectionsState,
        )
    }

    def create[F[_]: Meter]: F[UpDownCounter[F, Long]] =
      Meter[F]
        .upDownCounter[Long](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .create

  }

  /** Deprecated, use `db.client.connection.use_time` instead. Note: the unit also changed from `ms` to `s`.
    */
  @deprecated("Replaced by `db.client.connection.use_time`. Note: the unit also changed from `ms` to `s`.", "")
  object ClientConnectionsUseTime {

    val Name = "db.client.connections.use_time"
    val Description =
      "Deprecated, use `db.client.connection.use_time` instead. Note: the unit also changed from `ms` to `s`."
    val Unit = "ms"

    object AttributeSpecs {

      /** Deprecated, use `db.client.connection.pool.name` instead.
        */
      @deprecated("Replaced by `db.client.connection.pool.name`.", "")
      val dbClientConnectionsPoolName: AttributeSpec[String] =
        AttributeSpec(
          DbExperimentalAttributes.DbClientConnectionsPoolName,
          List(
            "myDataSource",
          ),
          Requirement.required,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          dbClientConnectionsPoolName,
        )
    }

    def create[F[_]: Meter](boundaries: BucketBoundaries): F[Histogram[F, Double]] =
      Meter[F]
        .histogram[Double](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

  /** Deprecated, use `db.client.connection.wait_time` instead. Note: the unit also changed from `ms` to `s`.
    */
  @deprecated("Replaced by `db.client.connection.wait_time`. Note: the unit also changed from `ms` to `s`.", "")
  object ClientConnectionsWaitTime {

    val Name = "db.client.connections.wait_time"
    val Description =
      "Deprecated, use `db.client.connection.wait_time` instead. Note: the unit also changed from `ms` to `s`."
    val Unit = "ms"

    object AttributeSpecs {

      /** Deprecated, use `db.client.connection.pool.name` instead.
        */
      @deprecated("Replaced by `db.client.connection.pool.name`.", "")
      val dbClientConnectionsPoolName: AttributeSpec[String] =
        AttributeSpec(
          DbExperimentalAttributes.DbClientConnectionsPoolName,
          List(
            "myDataSource",
          ),
          Requirement.required,
          Stability.experimental
        )

      val specs: List[AttributeSpec[_]] =
        List(
          dbClientConnectionsPoolName,
        )
    }

    def create[F[_]: Meter](boundaries: BucketBoundaries): F[Histogram[F, Double]] =
      Meter[F]
        .histogram[Double](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

  /** Duration of database client operations. <p>
    * @note
    *   <p> Batch operations SHOULD be recorded as a single operation.
    */
  object ClientOperationDuration {

    val Name = "db.client.operation.duration"
    val Description = "Duration of database client operations."
    val Unit = "s"

    object AttributeSpecs {

      /** The name of a collection (table, container) within the database. <p>
        * @note
        *   <p> It is RECOMMENDED to capture the value as provided by the application without attempting to do any case
        *   normalization. If the collection name is parsed from the query text, it SHOULD be the first collection name
        *   found in the query and it SHOULD match the value provided in the query text including any schema and
        *   database name prefix. For batch operations, if the individual operations are known to have the same
        *   collection name then that collection name SHOULD be used, otherwise `db.collection.name` SHOULD NOT be
        *   captured.
        */
      val dbCollectionName: AttributeSpec[String] =
        AttributeSpec(
          DbExperimentalAttributes.DbCollectionName,
          List(
            "public.users",
            "customers",
          ),
          Requirement.conditionallyRequired(
            "If readily available. The collection name MAY be parsed from the query text, in which case it SHOULD be the first collection name in the query."
          ),
          Stability.experimental
        )

      /** The name of the database, fully qualified within the server address and port. <p>
        * @note
        *   <p> If a database system has multiple namespace components, they SHOULD be concatenated (potentially using
        *   database system specific conventions) from most general to most specific namespace component, and more
        *   specific namespaces SHOULD NOT be captured without the more general namespaces, to ensure that "startswith"
        *   queries for the more general namespaces will be valid. Semantic conventions for individual database systems
        *   SHOULD document what `db.namespace` means in the context of that system. It is RECOMMENDED to capture the
        *   value as provided by the application without attempting to do any case normalization.
        */
      val dbNamespace: AttributeSpec[String] =
        AttributeSpec(
          DbExperimentalAttributes.DbNamespace,
          List(
            "customers",
            "test.users",
          ),
          Requirement.conditionallyRequired("If available."),
          Stability.experimental
        )

      /** The name of the operation or command being executed. <p>
        * @note
        *   <p> It is RECOMMENDED to capture the value as provided by the application without attempting to do any case
        *   normalization. If the operation name is parsed from the query text, it SHOULD be the first operation name
        *   found in the query. For batch operations, if the individual operations are known to have the same operation
        *   name then that operation name SHOULD be used prepended by `BATCH `, otherwise `db.operation.name` SHOULD be
        *   `BATCH` or some other database system specific term if more applicable.
        */
      val dbOperationName: AttributeSpec[String] =
        AttributeSpec(
          DbExperimentalAttributes.DbOperationName,
          List(
            "findAndModify",
            "HMSET",
            "SELECT",
          ),
          Requirement.conditionallyRequired(
            "If readily available. The operation name MAY be parsed from the query text, in which case it SHOULD be the first operation name found in the query."
          ),
          Stability.experimental
        )

      /** The database management system (DBMS) product as identified by the client instrumentation. <p>
        * @note
        *   <p> The actual DBMS may differ from the one identified by the client. For example, when using PostgreSQL
        *   client libraries to connect to a CockroachDB, the `db.system` is set to `postgresql` based on the
        *   instrumentation's best knowledge.
        */
      val dbSystem: AttributeSpec[String] =
        AttributeSpec(
          DbExperimentalAttributes.DbSystem,
          List(
          ),
          Requirement.required,
          Stability.experimental
        )

      /** Describes a class of error the operation ended with. <p>
        * @note
        *   <p> The `error.type` SHOULD match the error code returned by the database or the client library, the
        *   canonical name of exception that occurred, or another low-cardinality error identifier. Instrumentations
        *   SHOULD document the list of errors they report.
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

      /** Peer address of the database node where the operation was performed. <p>
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

      /** Name of the database host. <p>
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

      /** Server port number. <p>
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
          dbSystem,
          errorType,
          networkPeerAddress,
          networkPeerPort,
          serverAddress,
          serverPort,
        )
    }

    def create[F[_]: Meter](boundaries: BucketBoundaries): F[Histogram[F, Double]] =
      Meter[F]
        .histogram[Double](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

}
