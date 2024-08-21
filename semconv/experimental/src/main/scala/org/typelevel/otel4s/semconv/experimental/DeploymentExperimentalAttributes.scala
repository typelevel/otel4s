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
object DeploymentExperimentalAttributes {

  /** 'Deprecated, use `deployment.environment.name` instead.'
    */
  @deprecated("'use `deployment.environment.name` instead.'", "0.5.0")
  val DeploymentEnvironment: AttributeKey[String] = string(
    "deployment.environment"
  )

  /** Name of the <a
    * href="https://wikipedia.org/wiki/Deployment_environment">deployment
    * environment</a> (aka deployment tier).
    *
    * @note
    *   - `deployment.environment.name` does not affect the uniqueness
    *     constraints defined through the `service.namespace`, `service.name`
    *     and `service.instance.id` resource attributes. This implies that
    *     resources carrying the following attribute combinations MUST be
    *     considered to be identifying the same
    *     service:<li>`service.name=frontend`,
    *     `deployment.environment.name=production`</li>
    *     <li>`service.name=frontend`,
    *     `deployment.environment.name=staging`.</li>
    */
  val DeploymentEnvironmentName: AttributeKey[String] = string(
    "deployment.environment.name"
  )

  /** The id of the deployment.
    */
  val DeploymentId: AttributeKey[String] = string("deployment.id")

  /** The name of the deployment.
    */
  val DeploymentName: AttributeKey[String] = string("deployment.name")

  /** The status of the deployment.
    */
  val DeploymentStatus: AttributeKey[String] = string("deployment.status")
  // Enum definitions

  /** Values for [[DeploymentStatus]].
    */
  abstract class DeploymentStatusValue(val value: String)
  object DeploymentStatusValue {

    /** failed. */
    case object Failed extends DeploymentStatusValue("failed")

    /** succeeded. */
    case object Succeeded extends DeploymentStatusValue("succeeded")
  }

}
