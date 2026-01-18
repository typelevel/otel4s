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
object GraphqlExperimentalAttributes {

  /** The GraphQL document being executed.
    *
    * @note
    *   <p> The value may be sanitized to exclude sensitive information.
    */
  val GraphqlDocument: AttributeKey[String] =
    AttributeKey("graphql.document")

  /** The name of the operation being executed.
    */
  val GraphqlOperationName: AttributeKey[String] =
    AttributeKey("graphql.operation.name")

  /** The type of the operation being executed.
    */
  val GraphqlOperationType: AttributeKey[String] =
    AttributeKey("graphql.operation.type")

  /** Values for [[GraphqlOperationType]].
    */
  abstract class GraphqlOperationTypeValue(val value: String)
  object GraphqlOperationTypeValue {
    implicit val attributeFromGraphqlOperationTypeValue: Attribute.From[GraphqlOperationTypeValue, String] = _.value

    /** GraphQL query
      */
    case object Query extends GraphqlOperationTypeValue("query")

    /** GraphQL mutation
      */
    case object Mutation extends GraphqlOperationTypeValue("mutation")

    /** GraphQL subscription
      */
    case object Subscription extends GraphqlOperationTypeValue("subscription")
  }

}
