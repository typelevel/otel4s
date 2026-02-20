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
object OracleExperimentalAttributes {

  /** The database domain associated with the connection.
    *
    * @note
    *   <p> This attribute SHOULD be set to the value of the `DB_DOMAIN` initialization parameter, as exposed in
    *   `v$$parameter`. `DB_DOMAIN` defines the domain portion of the global database name and SHOULD be configured when
    *   a database is, or may become, part of a distributed environment. Its value consists of one or more valid
    *   identifiers (alphanumeric ASCII characters) separated by periods.
    */
  val OracleDbDomain: AttributeKey[String] =
    AttributeKey("oracle.db.domain")

  /** The instance name associated with the connection in an Oracle Real Application Clusters environment.
    *
    * @note
    *   <p> There can be multiple instances associated with a single database service. It indicates the unique instance
    *   name to which the connection is currently bound. For non-RAC databases, this value defaults to the
    *   `oracle.db.name`.
    */
  val OracleDbInstanceName: AttributeKey[String] =
    AttributeKey("oracle.db.instance.name")

  /** The database name associated with the connection.
    *
    * @note
    *   <p> This attribute SHOULD be set to the value of the parameter `DB_NAME` exposed in `v$$parameter`.
    */
  val OracleDbName: AttributeKey[String] =
    AttributeKey("oracle.db.name")

  /** The pluggable database (PDB) name associated with the connection.
    *
    * @note
    *   <p> This attribute SHOULD reflect the PDB that the session is currently connected to. If instrumentation cannot
    *   reliably obtain the active PDB name for each operation without issuing an additional query (such as
    *   `SELECT SYS_CONTEXT`), it is RECOMMENDED to fall back to the PDB name specified at connection establishment.
    */
  val OracleDbPdb: AttributeKey[String] =
    AttributeKey("oracle.db.pdb")

  /** The service name currently associated with the database connection.
    *
    * @note
    *   <p> The effective service name for a connection can change during its lifetime, for example after executing sql,
    *   `ALTER SESSION`. If an instrumentation cannot reliably obtain the current service name for each operation
    *   without issuing an additional query (such as `SELECT SYS_CONTEXT`), it is RECOMMENDED to fall back to the
    *   service name originally provided at connection establishment.
    */
  val OracleDbService: AttributeKey[String] =
    AttributeKey("oracle.db.service")

}
