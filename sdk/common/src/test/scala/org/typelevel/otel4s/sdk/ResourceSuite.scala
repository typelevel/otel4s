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

package org.typelevel.otel4s.sdk

import munit.FunSuite

class ResourceSuite extends FunSuite {

  def checkSchemaMerge(
      leftSchemaUrl: Option[String],
      rightSchemaUrl: Option[String],
      expected: Option[String]
  ): Unit =
    assertEquals(
      Resource(Attributes.Empty, leftSchemaUrl)
        .mergeInto(Resource(Attributes.Empty, rightSchemaUrl))
        .schemaUrl,
      expected
    )

  test(
    "Resource#merge should create a resource with the same schemaUrl when merging resources with identical schemaUrls"
  ) {
    val schemaUrl = Some("http://example.com")
    checkSchemaMerge(schemaUrl, schemaUrl, schemaUrl)
  }

  /*
    The behavior in this case is not defined in the specification and it's up to the SDK implementation.
    The easiest way to implement this is to drop the schemaUrl if they are different. In the future, we may
    apply schema transformations whenever possible.
   */
  test("Resource#merge should drop schemaUrl if they are different") {
    checkSchemaMerge(
      Some("http://example.com"),
      Some("http://example.org"),
      None
    )
  }

  test(
    "Resource#merge should return the left schemaUrl if the right is empty"
  ) {
    checkSchemaMerge(
      Some("http://example.com"),
      None,
      Some("http://example.com")
    )
  }

  test(
    "Resource#merge should return the right schemaUrl if the left is empty"
  ) {
    checkSchemaMerge(
      None,
      Some("http://example.com"),
      Some("http://example.com")
    )
  }

  test("Resource#merge should return None if both schemaUrls are empty") {
    checkSchemaMerge(None, None, None)
  }

}
