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
object ThreadExperimentalAttributes {

  /** Current "managed" thread ID (as opposed to OS thread ID).
    *
    * @note
    *   <p> Examples of where the value can be extracted from: <p>
    *   | Language or platform | Source                                 |
    *   |:---------------------|:---------------------------------------|
    *   | JVM                  | `Thread.currentThread().threadId()`    |
    *   | .NET                 | `Thread.CurrentThread.ManagedThreadId` |
    *   | Python               | `threading.current_thread().ident`     |
    *   | Ruby                 | `Thread.current.object_id`             |
    *   | C++                  | `std::this_thread::get_id()`           |
    *   | Erlang               | `erlang:self()`                        |
    */
  val ThreadId: AttributeKey[Long] =
    AttributeKey("thread.id")

  /** Current thread name.
    *
    * @note
    *   <p> Examples of where the value can be extracted from: <p>
    *   | Language or platform | Source                                         |
    *   |:---------------------|:-----------------------------------------------|
    *   | JVM                  | `Thread.currentThread().getName()`             |
    *   | .NET                 | `Thread.CurrentThread.Name`                    |
    *   | Python               | `threading.current_thread().name`              |
    *   | Ruby                 | `Thread.current.name`                          |
    *   | Erlang               | `erlang:process_info(self(), registered_name)` |
    */
  val ThreadName: AttributeKey[String] =
    AttributeKey("thread.name")

}
