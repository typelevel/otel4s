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
object CloudfoundryExperimentalAttributes {

  /** The guid of the application.
    *
    * @note
    *   <p> Application instrumentation should use the value from environment variable
    *   `VCAP_APPLICATION.application_id`. This is the same value as reported by `cf app <app-name> --guid`.
    */
  val CloudfoundryAppId: AttributeKey[String] =
    AttributeKey("cloudfoundry.app.id")

  /** The index of the application instance. 0 when just one instance is active.
    *
    * @note
    *   <p> CloudFoundry defines the `instance_id` in the <a
    *   href="https://github.com/cloudfoundry/loggregator-api#v2-envelope">Loggregator v2 envelope</a>. It is used for
    *   logs and metrics emitted by CloudFoundry. It is supposed to contain the application instance index for
    *   applications deployed on the runtime. <p> Application instrumentation should use the value from environment
    *   variable `CF_INSTANCE_INDEX`.
    */
  val CloudfoundryAppInstanceId: AttributeKey[String] =
    AttributeKey("cloudfoundry.app.instance.id")

  /** The name of the application.
    *
    * @note
    *   <p> Application instrumentation should use the value from environment variable
    *   `VCAP_APPLICATION.application_name`. This is the same value as reported by `cf apps`.
    */
  val CloudfoundryAppName: AttributeKey[String] =
    AttributeKey("cloudfoundry.app.name")

  /** The guid of the CloudFoundry org the application is running in.
    *
    * @note
    *   <p> Application instrumentation should use the value from environment variable `VCAP_APPLICATION.org_id`. This
    *   is the same value as reported by `cf org <org-name> --guid`.
    */
  val CloudfoundryOrgId: AttributeKey[String] =
    AttributeKey("cloudfoundry.org.id")

  /** The name of the CloudFoundry organization the app is running in.
    *
    * @note
    *   <p> Application instrumentation should use the value from environment variable `VCAP_APPLICATION.org_name`. This
    *   is the same value as reported by `cf orgs`.
    */
  val CloudfoundryOrgName: AttributeKey[String] =
    AttributeKey("cloudfoundry.org.name")

  /** The UID identifying the process.
    *
    * @note
    *   <p> Application instrumentation should use the value from environment variable `VCAP_APPLICATION.process_id`. It
    *   is supposed to be equal to `VCAP_APPLICATION.app_id` for applications deployed to the runtime. For system
    *   components, this could be the actual PID.
    */
  val CloudfoundryProcessId: AttributeKey[String] =
    AttributeKey("cloudfoundry.process.id")

  /** The type of process.
    *
    * @note
    *   <p> CloudFoundry applications can consist of multiple jobs. Usually the main process will be of type `web`.
    *   There can be additional background tasks or side-cars with different process types.
    */
  val CloudfoundryProcessType: AttributeKey[String] =
    AttributeKey("cloudfoundry.process.type")

  /** The guid of the CloudFoundry space the application is running in.
    *
    * @note
    *   <p> Application instrumentation should use the value from environment variable `VCAP_APPLICATION.space_id`. This
    *   is the same value as reported by `cf space <space-name> --guid`.
    */
  val CloudfoundrySpaceId: AttributeKey[String] =
    AttributeKey("cloudfoundry.space.id")

  /** The name of the CloudFoundry space the application is running in.
    *
    * @note
    *   <p> Application instrumentation should use the value from environment variable `VCAP_APPLICATION.space_name`.
    *   This is the same value as reported by `cf spaces`.
    */
  val CloudfoundrySpaceName: AttributeKey[String] =
    AttributeKey("cloudfoundry.space.name")

  /** A guid or another name describing the event source.
    *
    * @note
    *   <p> CloudFoundry defines the `source_id` in the <a
    *   href="https://github.com/cloudfoundry/loggregator-api#v2-envelope">Loggregator v2 envelope</a>. It is used for
    *   logs and metrics emitted by CloudFoundry. It is supposed to contain the component name, e.g. "gorouter", for
    *   CloudFoundry components. <p> When system components are instrumented, values from the <a
    *   href="https://bosh.io/docs/jobs/#properties-spec">Bosh spec</a> should be used. The `system.id` should be set to
    *   `spec.deployment/spec.name`.
    */
  val CloudfoundrySystemId: AttributeKey[String] =
    AttributeKey("cloudfoundry.system.id")

  /** A guid describing the concrete instance of the event source.
    *
    * @note
    *   <p> CloudFoundry defines the `instance_id` in the <a
    *   href="https://github.com/cloudfoundry/loggregator-api#v2-envelope">Loggregator v2 envelope</a>. It is used for
    *   logs and metrics emitted by CloudFoundry. It is supposed to contain the vm id for CloudFoundry components. <p>
    *   When system components are instrumented, values from the <a
    *   href="https://bosh.io/docs/jobs/#properties-spec">Bosh spec</a> should be used. The `system.instance.id` should
    *   be set to `spec.id`.
    */
  val CloudfoundrySystemInstanceId: AttributeKey[String] =
    AttributeKey("cloudfoundry.system.instance.id")

}
