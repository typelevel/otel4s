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

  def check(
      leftSchemaUrl: Option[String],
      rightSchemaUrl: Option[String],
      expected: Option[String]
  ) =
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
    check(schemaUrl, schemaUrl, schemaUrl)
  }

  test("Resource#merge should drop schemaUrl if they are different") {
    check(Some("http://example.com"), Some("http://example.org"), None)
  }

  test(
    "Resource#merge should return the left schemaUrl if the right is empty"
  ) {
    check(Some("http://example.com"), None, Some("http://example.com"))
  }

  test(
    "Resource#merge should return the right schemaUrl if the left is empty"
  ) {
    check(None, Some("http://example.com"), Some("http://example.com"))
  }

  test("Resource#merge should return None if both schemaUrls are empty") {
    check(None, None, None)
  }

}
