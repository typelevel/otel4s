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

package org.typelevel.otel4s.sdk.trace.context.propagation

import munit.FunSuite
import org.typelevel.otel4s.baggage.Baggage
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.trace.SdkContextKeys

class W3CBaggagePropagatorSuite extends FunSuite {

  private val propagator = W3CBaggagePropagator.default

  //
  // Common
  //

  test("fields") {
    assertEquals(propagator.fields, List("baggage"))
  }

  test("toString") {
    assertEquals(propagator.toString, "W3CBaggagePropagator")
  }

  //
  // Inject
  //

  test("inject nothing when context is empty") {
    val result = propagator.inject(Context.root, Map.empty[String, String])
    assertEquals(result.size, 0)
  }

  test("inject - empty baggage - do nothing") {
    val context = Context.root.updated(SdkContextKeys.BaggageKey, Baggage.empty)

    assertEquals(
      propagator.inject(context, Map.empty[String, String]),
      Map.empty[String, String]
    )
  }

  test("inject - non-empty baggage - keep valid keys and encode values") {
    val baggage = Baggage.empty
      .updated("nometa", "nometa-value")
      .updated("needsEncoding", "blah blah blah")
      .updated("meta", "meta-value", Some("somemetadata; someother=foo"))
      .updated("\u0002ab\u0003cd", "wacky key nonprintable")

    val context = Context.root.updated(SdkContextKeys.BaggageKey, baggage)
    val result = propagator.inject(context, Map.empty[String, String])
    val expected = Map(
      "baggage" -> "nometa=nometa-value,needsEncoding=blah+blah+blah,meta=meta-value;somemetadata%3B+someother%3Dfoo"
    )

    assertEquals(result, expected)
    assertEquals(
      getBaggage(propagator.extract(context, expected)),
      Some(baggage.removed("\u0002ab\u0003cd"))
    )
  }

  //
  // Extract
  //

  test("extract - empty carrier") {
    val ctx = propagator.extract(Context.root, Map.empty[String, String])
    assertEquals(getBaggage(ctx), None)
  }

  test("extract - 'baggage' header is missing") {
    val ctx = propagator.extract(Context.root, Map("key" -> "value"))
    assertEquals(getBaggage(ctx), None)
  }

  test("extract - single key value") {
    val ctx = propagator.extract(Context.root, Map("baggage" -> "key=value"))
    val expected = Baggage.empty.updated("key", "value")
    assertEquals(getBaggage(ctx), Some(expected))
  }

  test("extract - multiple unique entries") {
    val ctx = propagator.extract(
      Context.root,
      Map("baggage" -> "key=value,key1=value1")
    )
    val expected =
      Baggage.empty.updated("key", "value").updated("key1", "value1")
    assertEquals(getBaggage(ctx), Some(expected))
  }

  test("extract - duplicated keys - keep the latter") {
    val ctx = propagator.extract(
      Context.root,
      Map("baggage" -> "key=value1,key=value2")
    )

    assertEquals(getBaggage(ctx), Some(Baggage.empty.updated("key", "value2")))
  }

  test("extract - entry with metadata") {
    val ctx = propagator.extract(
      Context.root,
      Map("baggage" -> "key=val;metadata-key=value;somemeta")
    )
    val expected =
      Baggage.empty.updated("key", "val", Some("metadata-key=value;somemeta"))
    assertEquals(getBaggage(ctx), Some(expected))
  }

  test("extract - entries with metadata and whitespaces") {
    val ctx = propagator.extract(
      Context.root,
      Map(
        "baggage" -> "key= val; metadata-key = value; somemeta, key2 =value , key3 = \tvalue3 ; "
      )
    )

    val expected = Baggage.empty
      .updated("key", "val", Some("metadata-key = value; somemeta"))
      .updated("key2", "value")
      .updated("key3", "value3")

    assertEquals(getBaggage(ctx), Some(expected))
  }

  test("extract - entries with metadata - some invalid") {
    val ctx = propagator.extract(
      Context.root,
      Map(
        "baggage" -> "key1= v;alsdf;-asdflkjasdf===asdlfkjadsf ,,a sdf9asdf-alue1; metadata-key = value; othermetadata, key2 =value2, key3 =\tvalue3 ; "
      )
    )

    val expected = Baggage.empty
      .updated("key1", "v", Some("alsdf;-asdflkjasdf===asdlfkjadsf"))
      .updated("key2", "value2")
      .updated("key3", "value3")

    assertEquals(getBaggage(ctx), Some(expected))
  }

  test("extract - key with leading spaces - trim") {
    val ctx = propagator.extract(Context.root, Map("baggage" -> "   key=value"))
    assertEquals(getBaggage(ctx), Some(Baggage.empty.updated("key", "value")))
  }

  test("extract - key with trailing spaces - trim") {
    val ctx = propagator.extract(Context.root, Map("baggage" -> "key  =value"))
    assertEquals(getBaggage(ctx), Some(Baggage.empty.updated("key", "value")))
  }

  test("extract - empty key - ignore") {
    val ctx = propagator.extract(Context.root, Map("baggage" -> "=value"))
    assertEquals(getBaggage(ctx), None)
  }

  test("extract - key with only empty spaces - ignore") {
    val ctx = propagator.extract(Context.root, Map("baggage" -> "    =value"))
    assertEquals(getBaggage(ctx), None)
  }

  test("extract - key with inner spaces - ignore") {
    val ctx = propagator.extract(Context.root, Map("baggage" -> "ke y=value"))
    assertEquals(getBaggage(ctx), None)
  }

  test("extract - key with separators - ignore") {
    val ctx = propagator.extract(Context.root, Map("baggage" -> "ke?y=value"))
    assertEquals(getBaggage(ctx), None)
  }

  test("extract - one valid key, multiple invalid - parse valid") {
    val ctx = propagator.extract(
      Context.root,
      Map(
        "baggage" -> "ke<y=value1, ;sss,key=value;meta1=value1;meta2=value2,ke(y=value;meta=val "
      )
    )

    val expected =
      Baggage.empty.updated("key", "value", Some("meta1=value1;meta2=value2"))

    assertEquals(getBaggage(ctx), Some(expected))
  }

  test("extract - value with leading spaces - trim") {
    val ctx = propagator.extract(Context.root, Map("baggage" -> "key=  value"))
    assertEquals(getBaggage(ctx), Some(Baggage.empty.updated("key", "value")))
  }

  test("extract - value with trailing spaces - trim") {
    val ctx = propagator.extract(Context.root, Map("baggage" -> "key=value  "))
    assertEquals(getBaggage(ctx), Some(Baggage.empty.updated("key", "value")))
  }

  test("extract - empty value - ignore") {
    val ctx = propagator.extract(Context.root, Map("baggage" -> "key="))
    assertEquals(getBaggage(ctx), None)
  }

  test("extract - value with only empty spaces - ignore") {
    val ctx = propagator.extract(Context.root, Map("baggage" -> "key=       "))
    assertEquals(getBaggage(ctx), None)
  }

  test("extract - empty value with metadata - ignore") {
    val ctx =
      propagator.extract(Context.root, Map("baggage" -> "key=;meta1=meta2"))
    assertEquals(getBaggage(ctx), None)
  }

  test("extract - value with inner spaces - ignore") {
    val ctx = propagator.extract(Context.root, Map("baggage" -> "key=val ue"))
    assertEquals(getBaggage(ctx), None)
  }

  test("extract - value with separators - ignore") {
    val ctx = propagator.extract(Context.root, Map("baggage" -> "key=val\\ue"))
    assertEquals(getBaggage(ctx), None)
  }

  test("extract - value with multiple leading spaces") {
    val ctx = propagator.extract(
      Context.root,
      Map("baggage" -> "key=     value,key1=value1")
    )
    val expected =
      Baggage.empty.updated("key", "value").updated("key1", "value1")
    assertEquals(getBaggage(ctx), Some(expected))
  }

  test("extract - value with multiple trailing spaces") {
    val ctx = propagator.extract(
      Context.root,
      Map("baggage" -> "key=value    ,key1=value1")
    )
    val expected =
      Baggage.empty.updated("key", "value").updated("key1", "value1")
    assertEquals(getBaggage(ctx), Some(expected))
  }

  test("extract - URL encoded value") {
    val ctx =
      propagator.extract(Context.root, Map("baggage" -> "key=value%201"))
    val expected = Baggage.empty.updated("key", "value 1")
    assertEquals(getBaggage(ctx), Some(expected))
  }

  test("extract - value with trailing spaces and metadata") {
    val ctx = propagator.extract(
      Context.root,
      Map("baggage" -> "key=value1       ;meta1=meta2")
    )
    val expected = Baggage.empty.updated("key", "value1", Some("meta1=meta2"))
    assertEquals(getBaggage(ctx), Some(expected))
  }

  test("extract - same keys - some values are empty with metadata") {
    val ctx = propagator.extract(
      Context.root,
      Map("baggage" -> "key=;metakey=metaval,key=val")
    )
    val expected = Baggage.empty.updated("key", "val")
    assertEquals(getBaggage(ctx), Some(expected))
  }

  test("extract - same keys - some values are empty") {
    val ctx = propagator.extract(Context.root, Map("baggage" -> "key=,key=val"))
    val expected = Baggage.empty.updated("key", "val")
    assertEquals(getBaggage(ctx), Some(expected))
  }

  test("extract - same keys - some values with only spaces") {
    val ctx =
      propagator.extract(Context.root, Map("baggage" -> "key=    ,key=val"))
    val expected = Baggage.empty.updated("key", "val")
    assertEquals(getBaggage(ctx), Some(expected))
  }

  test("extract - same keys - some values with inner spaces") {
    val ctx =
      propagator.extract(Context.root, Map("baggage" -> "key=val ue,key=val"))
    val expected = Baggage.empty.updated("key", "val")
    assertEquals(getBaggage(ctx), Some(expected))
  }

  test("extract - same keys - some values with separators spaces") {
    val ctx =
      propagator.extract(Context.root, Map("baggage" -> "key=val\\ue,key=val"))
    val expected = Baggage.empty.updated("key", "val")
    assertEquals(getBaggage(ctx), Some(expected))
  }

  private def getBaggage(ctx: Context): Option[Baggage] =
    ctx.get(SdkContextKeys.BaggageKey)

}
