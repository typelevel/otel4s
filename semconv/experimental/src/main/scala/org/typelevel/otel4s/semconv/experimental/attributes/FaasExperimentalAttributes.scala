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
object FaasExperimentalAttributes {

  /** A boolean that is true if the serverless function is executed for the first time (aka cold-start).
    */
  val FaasColdstart: AttributeKey[Boolean] =
    AttributeKey("faas.coldstart")

  /** A string containing the schedule period as <a
    * href="https://docs.oracle.com/cd/E12058_01/doc/doc.1014/e12030/cron_expressions.htm">Cron Expression</a>.
    */
  val FaasCron: AttributeKey[String] =
    AttributeKey("faas.cron")

  /** The name of the source on which the triggering operation was performed. For example, in Cloud Storage or S3
    * corresponds to the bucket name, and in Cosmos DB to the database name.
    */
  val FaasDocumentCollection: AttributeKey[String] =
    AttributeKey("faas.document.collection")

  /** The document name/table subjected to the operation. For example, in Cloud Storage or S3 is the name of the file,
    * and in Cosmos DB the table name.
    */
  val FaasDocumentName: AttributeKey[String] =
    AttributeKey("faas.document.name")

  /** Describes the type of the operation that was performed on the data.
    */
  val FaasDocumentOperation: AttributeKey[String] =
    AttributeKey("faas.document.operation")

  /** A string containing the time when the data was accessed in the <a
    * href="https://www.iso.org/iso-8601-date-and-time-format.html">ISO 8601</a> format expressed in <a
    * href="https://www.w3.org/TR/NOTE-datetime">UTC</a>.
    */
  val FaasDocumentTime: AttributeKey[String] =
    AttributeKey("faas.document.time")

  /** The execution environment ID as a string, that will be potentially reused for other invocations to the same
    * function/function version.
    *
    * @note
    *   <ul> <li><strong>AWS Lambda:</strong> Use the (full) log stream name. </ul>
    */
  val FaasInstance: AttributeKey[String] =
    AttributeKey("faas.instance")

  /** The invocation ID of the current function invocation.
    */
  val FaasInvocationId: AttributeKey[String] =
    AttributeKey("faas.invocation_id")

  /** The name of the invoked function.
    *
    * @note
    *   <p> SHOULD be equal to the `faas.name` resource attribute of the invoked function.
    */
  val FaasInvokedName: AttributeKey[String] =
    AttributeKey("faas.invoked_name")

  /** The cloud provider of the invoked function.
    *
    * @note
    *   <p> SHOULD be equal to the `cloud.provider` resource attribute of the invoked function.
    */
  val FaasInvokedProvider: AttributeKey[String] =
    AttributeKey("faas.invoked_provider")

  /** The cloud region of the invoked function.
    *
    * @note
    *   <p> SHOULD be equal to the `cloud.region` resource attribute of the invoked function.
    */
  val FaasInvokedRegion: AttributeKey[String] =
    AttributeKey("faas.invoked_region")

  /** The amount of memory available to the serverless function converted to Bytes.
    *
    * @note
    *   <p> It's recommended to set this attribute since e.g. too little memory can easily stop a Java AWS Lambda
    *   function from working correctly. On AWS Lambda, the environment variable `AWS_LAMBDA_FUNCTION_MEMORY_SIZE`
    *   provides this information (which must be multiplied by 1,048,576).
    */
  val FaasMaxMemory: AttributeKey[Long] =
    AttributeKey("faas.max_memory")

  /** The name of the single function that this runtime instance executes.
    *
    * @note
    *   <p> This is the name of the function as configured/deployed on the FaaS platform and is usually different from
    *   the name of the callback function (which may be stored in the <a
    *   href="/docs/general/attributes.md#source-code-attributes">`code.namespace`/`code.function.name`</a> span
    *   attributes). <p> For some cloud providers, the above definition is ambiguous. The following definition of
    *   function name MUST be used for this attribute (and consequently the span name) for the listed cloud
    *   providers/products: <ul> <li><strong>Azure:</strong> The full name `<FUNCAPP>/<FUNC>`, i.e., function app name
    *   followed by a forward slash followed by the function name (this form can also be seen in the resource JSON for
    *   the function). This means that a span attribute MUST be used, as an Azure function app can host multiple
    *   functions that would usually share a TracerProvider (see also the `cloud.resource_id` attribute). </ul>
    */
  val FaasName: AttributeKey[String] =
    AttributeKey("faas.name")

  /** A string containing the function invocation time in the <a
    * href="https://www.iso.org/iso-8601-date-and-time-format.html">ISO 8601</a> format expressed in <a
    * href="https://www.w3.org/TR/NOTE-datetime">UTC</a>.
    */
  val FaasTime: AttributeKey[String] =
    AttributeKey("faas.time")

  /** Type of the trigger which caused this function invocation.
    */
  val FaasTrigger: AttributeKey[String] =
    AttributeKey("faas.trigger")

  /** The immutable version of the function being executed.
    *
    * @note
    *   <p> Depending on the cloud provider and platform, use: <ul> <li><strong>AWS Lambda:</strong> The <a
    *   href="https://docs.aws.amazon.com/lambda/latest/dg/configuration-versions.html">function version</a> (an integer
    *   represented as a decimal string). <li><strong>Google Cloud Run (Services):</strong> The <a
    *   href="https://cloud.google.com/run/docs/managing/revisions">revision</a> (i.e., the function name plus the
    *   revision suffix). <li><strong>Google Cloud Functions:</strong> The value of the <a
    *   href="https://cloud.google.com/run/docs/container-contract#services-env-vars">`K_REVISION` environment
    *   variable</a>. <li><strong>Azure Functions:</strong> Not applicable. Do not set this attribute. </ul>
    */
  val FaasVersion: AttributeKey[String] =
    AttributeKey("faas.version")

  /** Values for [[FaasDocumentOperation]].
    */
  abstract class FaasDocumentOperationValue(val value: String)
  object FaasDocumentOperationValue {
    implicit val attributeFromFaasDocumentOperationValue: Attribute.From[FaasDocumentOperationValue, String] = _.value

    /** When a new object is created.
      */
    case object Insert extends FaasDocumentOperationValue("insert")

    /** When an object is modified.
      */
    case object Edit extends FaasDocumentOperationValue("edit")

    /** When an object is deleted.
      */
    case object Delete extends FaasDocumentOperationValue("delete")
  }

  /** Values for [[FaasInvokedProvider]].
    */
  abstract class FaasInvokedProviderValue(val value: String)
  object FaasInvokedProviderValue {
    implicit val attributeFromFaasInvokedProviderValue: Attribute.From[FaasInvokedProviderValue, String] = _.value

    /** Alibaba Cloud
      */
    case object AlibabaCloud extends FaasInvokedProviderValue("alibaba_cloud")

    /** Amazon Web Services
      */
    case object Aws extends FaasInvokedProviderValue("aws")

    /** Microsoft Azure
      */
    case object Azure extends FaasInvokedProviderValue("azure")

    /** Google Cloud Platform
      */
    case object Gcp extends FaasInvokedProviderValue("gcp")

    /** Tencent Cloud
      */
    case object TencentCloud extends FaasInvokedProviderValue("tencent_cloud")
  }

  /** Values for [[FaasTrigger]].
    */
  abstract class FaasTriggerValue(val value: String)
  object FaasTriggerValue {
    implicit val attributeFromFaasTriggerValue: Attribute.From[FaasTriggerValue, String] = _.value

    /** A response to some data source operation such as a database or filesystem read/write
      */
    case object Datasource extends FaasTriggerValue("datasource")

    /** To provide an answer to an inbound HTTP request
      */
    case object Http extends FaasTriggerValue("http")

    /** A function is set to be executed when messages are sent to a messaging system
      */
    case object Pubsub extends FaasTriggerValue("pubsub")

    /** A function is scheduled to be executed regularly
      */
    case object Timer extends FaasTriggerValue("timer")

    /** If none of the others apply
      */
    case object Other extends FaasTriggerValue("other")
  }

}
