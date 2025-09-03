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
object CassandraExperimentalAttributes {

  /** The consistency level of the query. Based on consistency values from <a
    * href="https://docs.datastax.com/en/cassandra-oss/3.0/cassandra/dml/dmlConfigConsistency.html">CQL</a>.
    */
  val CassandraConsistencyLevel: AttributeKey[String] =
    AttributeKey("cassandra.consistency.level")

  /** The data center of the coordinating node for a query.
    */
  val CassandraCoordinatorDc: AttributeKey[String] =
    AttributeKey("cassandra.coordinator.dc")

  /** The ID of the coordinating node for a query.
    */
  val CassandraCoordinatorId: AttributeKey[String] =
    AttributeKey("cassandra.coordinator.id")

  /** The fetch size used for paging, i.e. how many rows will be returned at once.
    */
  val CassandraPageSize: AttributeKey[Long] =
    AttributeKey("cassandra.page.size")

  /** Whether or not the query is idempotent.
    */
  val CassandraQueryIdempotent: AttributeKey[Boolean] =
    AttributeKey("cassandra.query.idempotent")

  /** The number of times a query was speculatively executed. Not set or `0` if the query was not executed
    * speculatively.
    */
  val CassandraSpeculativeExecutionCount: AttributeKey[Long] =
    AttributeKey("cassandra.speculative_execution.count")

  /** Values for [[CassandraConsistencyLevel]].
    */
  abstract class CassandraConsistencyLevelValue(val value: String)
  object CassandraConsistencyLevelValue {

    /** All
      */
    case object All extends CassandraConsistencyLevelValue("all")

    /** Each Quorum
      */
    case object EachQuorum extends CassandraConsistencyLevelValue("each_quorum")

    /** Quorum
      */
    case object Quorum extends CassandraConsistencyLevelValue("quorum")

    /** Local Quorum
      */
    case object LocalQuorum extends CassandraConsistencyLevelValue("local_quorum")

    /** One
      */
    case object One extends CassandraConsistencyLevelValue("one")

    /** Two
      */
    case object Two extends CassandraConsistencyLevelValue("two")

    /** Three
      */
    case object Three extends CassandraConsistencyLevelValue("three")

    /** Local One
      */
    case object LocalOne extends CassandraConsistencyLevelValue("local_one")

    /** Any
      */
    case object Any extends CassandraConsistencyLevelValue("any")

    /** Serial
      */
    case object Serial extends CassandraConsistencyLevelValue("serial")

    /** Local Serial
      */
    case object LocalSerial extends CassandraConsistencyLevelValue("local_serial")
  }

}
