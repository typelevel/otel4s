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

package org.typelevel.otel4s.sdk.resource

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/** A mapping of the Node.js OS API.
  *
  * @see
  *   [[https://nodejs.org/api/os.html]]
  */
private object OS {

  @js.native
  @JSImport("os", "arch")
  def arch(): String = js.native

  @js.native
  @JSImport("os", "hostname")
  def hostname(): String = js.native

  @js.native
  @JSImport("os", "platform")
  def platform(): String = js.native

  @js.native
  @JSImport("os", "release")
  def release(): String = js.native

  @js.native
  @JSImport("os", "userInfo")
  def userInfo(): UserInfo = js.native

  @js.native
  trait UserInfo extends js.Object {
    def username: String = js.native
  }
}
