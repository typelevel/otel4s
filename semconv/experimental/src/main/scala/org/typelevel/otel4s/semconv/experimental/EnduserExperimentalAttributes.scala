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
object EnduserExperimentalAttributes {

  /** Username or client_id extracted from the access token or <a
    * href="https://tools.ietf.org/html/rfc7235#section-4.2">Authorization</a>
    * header in the inbound request from outside the system.
    */
  val EnduserId: AttributeKey[String] = string("enduser.id")

  /** Actual/assumed role the client is making the request under extracted from
    * token or application security context.
    */
  val EnduserRole: AttributeKey[String] = string("enduser.role")

  /** Scopes or granted authorities the client currently possesses extracted
    * from token or application security context. The value would come from the
    * scope associated with an <a
    * href="https://tools.ietf.org/html/rfc6749#section-3.3">OAuth 2.0 Access
    * Token</a> or an attribute value in a <a
    * href="http://docs.oasis-open.org/security/saml/Post2.0/sstc-saml-tech-overview-2.0.html">SAML
    * 2.0 Assertion</a>.
    */
  val EnduserScope: AttributeKey[String] = string("enduser.scope")

}
