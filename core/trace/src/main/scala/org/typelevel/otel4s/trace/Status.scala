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

package org.typelevel.otel4s.trace

/** The set of canonical status codes
  */
sealed trait Status extends Product with Serializable

object Status {

  /** The default status. */
  case object Unset extends Status

  /** The operation has been validated by an Application developers or Operator
    * to have completed successfully.
    */
  case object Ok extends Status

  /** The operation contains an error. */
  case object Error extends Status

}
