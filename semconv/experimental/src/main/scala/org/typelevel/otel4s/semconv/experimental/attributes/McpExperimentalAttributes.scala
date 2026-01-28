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
object McpExperimentalAttributes {

  /** The name of the request or notification method.
    */
  val McpMethodName: AttributeKey[String] =
    AttributeKey("mcp.method.name")

  /** The <a href="https://modelcontextprotocol.io/specification/versioning">version</a> of the Model Context Protocol
    * used.
    */
  val McpProtocolVersion: AttributeKey[String] =
    AttributeKey("mcp.protocol.version")

  /** The value of the resource uri.
    *
    * @note
    *   <p> This is a URI of the resource provided in the following requests or notifications: `resources/read`,
    *   `resources/subscribe`, `resources/unsubscribe`, or `notifications/resources/updated`.
    */
  val McpResourceUri: AttributeKey[String] =
    AttributeKey("mcp.resource.uri")

  /** Identifies <a
    * href="https://modelcontextprotocol.io/specification/2025-06-18/basic/transports#session-management">MCP
    * session</a>.
    */
  val McpSessionId: AttributeKey[String] =
    AttributeKey("mcp.session.id")

  /** Values for [[McpMethodName]].
    */
  abstract class McpMethodNameValue(val value: String)
  object McpMethodNameValue {
    implicit val attributeFromMcpMethodNameValue: Attribute.From[McpMethodNameValue, String] = _.value

    /** Notification cancelling a previously-issued request.
      */
    case object NotificationsCancelled extends McpMethodNameValue("notifications/cancelled")

    /** Request to initialize the MCP client.
      */
    case object Initialize extends McpMethodNameValue("initialize")

    /** Notification indicating that the MCP client has been initialized.
      */
    case object NotificationsInitialized extends McpMethodNameValue("notifications/initialized")

    /** Notification indicating the progress for a long-running operation.
      */
    case object NotificationsProgress extends McpMethodNameValue("notifications/progress")

    /** Request to check that the other party is still alive.
      */
    case object Ping extends McpMethodNameValue("ping")

    /** Request to list resources available on server.
      */
    case object ResourcesList extends McpMethodNameValue("resources/list")

    /** Request to list resource templates available on server.
      */
    case object ResourcesTemplatesList extends McpMethodNameValue("resources/templates/list")

    /** Request to read a resource.
      */
    case object ResourcesRead extends McpMethodNameValue("resources/read")

    /** Notification indicating that the list of resources has changed.
      */
    case object NotificationsResourcesListChanged extends McpMethodNameValue("notifications/resources/list_changed")

    /** Request to subscribe to a resource.
      */
    case object ResourcesSubscribe extends McpMethodNameValue("resources/subscribe")

    /** Request to unsubscribe from resource updates.
      */
    case object ResourcesUnsubscribe extends McpMethodNameValue("resources/unsubscribe")

    /** Notification indicating that a resource has been updated.
      */
    case object NotificationsResourcesUpdated extends McpMethodNameValue("notifications/resources/updated")

    /** Request to list prompts available on server.
      */
    case object PromptsList extends McpMethodNameValue("prompts/list")

    /** Request to get a prompt.
      */
    case object PromptsGet extends McpMethodNameValue("prompts/get")

    /** Notification indicating that the list of prompts has changed.
      */
    case object NotificationsPromptsListChanged extends McpMethodNameValue("notifications/prompts/list_changed")

    /** Request to list tools available on server.
      */
    case object ToolsList extends McpMethodNameValue("tools/list")

    /** Request to call a tool.
      */
    case object ToolsCall extends McpMethodNameValue("tools/call")

    /** Notification indicating that the list of tools has changed.
      */
    case object NotificationsToolsListChanged extends McpMethodNameValue("notifications/tools/list_changed")

    /** Request to set the logging level.
      */
    case object LoggingSetLevel extends McpMethodNameValue("logging/setLevel")

    /** Notification indicating that a message has been received.
      */
    case object NotificationsMessage extends McpMethodNameValue("notifications/message")

    /** Request to create a sampling message.
      */
    case object SamplingCreateMessage extends McpMethodNameValue("sampling/createMessage")

    /** Request to complete a prompt.
      */
    case object CompletionComplete extends McpMethodNameValue("completion/complete")

    /** Request to list roots available on server.
      */
    case object RootsList extends McpMethodNameValue("roots/list")

    /** Notification indicating that the list of roots has changed.
      */
    case object NotificationsRootsListChanged extends McpMethodNameValue("notifications/roots/list_changed")

    /** Request from the server to elicit additional information from the user via the client
      */
    case object ElicitationCreate extends McpMethodNameValue("elicitation/create")
  }

}
