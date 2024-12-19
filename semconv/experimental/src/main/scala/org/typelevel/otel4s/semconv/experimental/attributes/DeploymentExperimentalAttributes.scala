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
object DeploymentExperimentalAttributes {

  /** 'Deprecated, use `deployment.environment.name` instead.'
    */
  @deprecated("Deprecated, use `deployment.environment.name` instead.", "")
  val DeploymentEnvironment: AttributeKey[String] =
    AttributeKey("deployment.environment")

  /** Name of the <a href="https://wikipedia.org/wiki/Deployment_environment">deployment environment</a> (aka deployment
    * tier). <p>
    * @note
    *   <p> `deployment.environment.name` does not affect the uniqueness constraints defined through the
    *   `service.namespace`, `service.name` and `service.instance.id` resource attributes. This implies that resources
    *   carrying the following attribute combinations MUST be considered to be identifying the same service: <ul>
    *   <li>`service.name=frontend`, `deployment.environment.name=production` <li>`service.name=frontend`,
    *   `deployment.environment.name=staging`. </ul>
    */
  val DeploymentEnvironmentName: AttributeKey[String] =
    AttributeKey("deployment.environment.name")

  /** The id of the deployment.
    */
  val DeploymentId: AttributeKey[String] =
    AttributeKey("deployment.id")

  /** The name of the deployment.
    */
  val DeploymentName: AttributeKey[String] =
    AttributeKey("deployment.name")

  /** The status of the deployment.
    */
  val DeploymentStatus: AttributeKey[String] =
    AttributeKey("deployment.status")

  /** Values for [[DeploymentStatus]].
    */
  abstract class DeploymentStatusValue(val value: String)
  object DeploymentStatusValue {

    /** failed
      */
    case object Failed extends DeploymentStatusValue("failed")

    /** succeeded
      */
    case object Succeeded extends DeploymentStatusValue("succeeded")
  }

}
