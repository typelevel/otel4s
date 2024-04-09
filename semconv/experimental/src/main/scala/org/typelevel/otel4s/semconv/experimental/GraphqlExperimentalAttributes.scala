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
object GraphqlExperimentalAttributes {

  /** The GraphQL document being executed.
    *
    * @note
    *   - The value may be sanitized to exclude sensitive information.
    */
  val GraphqlDocument: AttributeKey[String] = string("graphql.document")

  /** The name of the operation being executed.
    */
  val GraphqlOperationName: AttributeKey[String] = string(
    "graphql.operation.name"
  )

  /** The type of the operation being executed.
    */
  val GraphqlOperationType: AttributeKey[String] = string(
    "graphql.operation.type"
  )
  // Enum definitions

  /** Values for [[GraphqlOperationType]].
    */
  abstract class GraphqlOperationTypeValue(val value: String)
  object GraphqlOperationTypeValue {

    /** GraphQL query. */
    case object Query extends GraphqlOperationTypeValue("query")

    /** GraphQL mutation. */
    case object Mutation extends GraphqlOperationTypeValue("mutation")

    /** GraphQL subscription. */
    case object Subscription extends GraphqlOperationTypeValue("subscription")
  }

}
