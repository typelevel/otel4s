/*
 * Copyright 2022 Typelevel
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
package trace

import cats.Hash
import cats.Show

/** Type of [[Span]]. Can be used to specify additional relationships between
  * spans in addition to a parent/child relationship.
  */
sealed trait SpanKind extends Product with Serializable

object SpanKind {

  /** Default value. Indicates that the span is used internally. */
  case object Internal extends SpanKind

  /** Indicates that the span covers server-side handling of an RPC or other
    * remote request.
    */
  case object Server extends SpanKind

  /** Indicates that the span covers the client-side wrapper around an RPC or
    * other remote request.
    */
  case object Client extends SpanKind

  /** Indicates that the span describes producer sending a message to a broker.
    * Unlike client and server, there is no direct critical path latency
    * relationship between producer and consumer spans.
    */
  case object Producer extends SpanKind

  /** Indicates that the span describes consumer receiving a message from a
    * broker. Unlike client and server, there is no direct critical path latency
    * relationship between producer and consumer spans.
    */
  case object Consumer extends SpanKind

  implicit val spanKindHash: Hash[SpanKind] = Hash.fromUniversalHashCode
  implicit val spanKindShow: Show[SpanKind] = Show.fromToString
}
