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
object DeploymentAttributes {

  /** Name of the <a href="https://wikipedia.org/wiki/Deployment_environment">deployment environment</a> (aka deployment
    * tier).
    *
    * @note
    *   <p> `deployment.environment.name` does not affect the uniqueness constraints defined through the
    *   `service.namespace`, `service.name` and `service.instance.id` resource attributes. This implies that resources
    *   carrying the following attribute combinations MUST be considered to be identifying the same service: <ul>
    *   <li>`service.name=frontend`, `deployment.environment.name=production` <li>`service.name=frontend`,
    *   `deployment.environment.name=staging`. </ul>
    */
  val DeploymentEnvironmentName: AttributeKey[String] =
    AttributeKey("deployment.environment.name")

  /** Values for [[DeploymentEnvironmentName]].
    */
  abstract class DeploymentEnvironmentNameValue(val value: String)
  object DeploymentEnvironmentNameValue {
    implicit val attributeFromDeploymentEnvironmentNameValue: Attribute.From[DeploymentEnvironmentNameValue, String] =
      _.value

    /** Production environment
      */
    case object Production extends DeploymentEnvironmentNameValue("production")

    /** Staging environment
      */
    case object Staging extends DeploymentEnvironmentNameValue("staging")

    /** Testing environment
      */
    case object Test extends DeploymentEnvironmentNameValue("test")

    /** Development environment
      */
    case object Development extends DeploymentEnvironmentNameValue("development")
  }

}
