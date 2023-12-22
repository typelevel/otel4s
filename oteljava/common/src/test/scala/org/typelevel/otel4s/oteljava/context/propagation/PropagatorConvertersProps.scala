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

package org.typelevel.otel4s.oteljava.context
package propagation

import io.opentelemetry.context.propagation.{
  ContextPropagators => JContextPropagators
}
import io.opentelemetry.context.propagation.{TextMapGetter => JTextMapGetter}
import io.opentelemetry.context.propagation.{
  TextMapPropagator => JTextMapPropagator
}
import io.opentelemetry.extension.incubator.propagation.{
  PassThroughPropagator => JPassThroughPropagator
}
import munit.ScalaCheckSuite
import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalacheck.Prop.forAll
import org.typelevel.otel4s.context.propagation.ContextPropagators
import org.typelevel.otel4s.context.propagation.PassThroughPropagator
import org.typelevel.otel4s.context.propagation.TextMapGetter
import org.typelevel.otel4s.context.propagation.TextMapPropagator
import org.typelevel.otel4s.oteljava.context.propagation.PropagatorConverters._

import java.{lang => jl}
import scala.jdk.CollectionConverters._

class PropagatorConvertersProps extends ScalaCheckSuite {
  private type Carrier = Map[String, String]

  private def mkJTMG: JTextMapGetter[Carrier] =
    new JTextMapGetter[Carrier] {
      def keys(carrier: Carrier): jl.Iterable[String] =
        carrier.keys.asJava

      def get(carrier: Carrier, key: String): String =
        carrier.get(key).orNull
    }
  private implicit def arbitraryJTMG: Arbitrary[JTextMapGetter[Carrier]] =
    Arbitrary(Gen.delay(Gen.const(mkJTMG)))
  private implicit def arbitrarySTMG: Arbitrary[TextMapGetter[Carrier]] =
    Arbitrary(Gen.delay(Gen.const(implicitly)))

  private implicit def arbitraryJTMP: Arbitrary[JTextMapPropagator] =
    Arbitrary {
      for (fields <- Gen.listOf(Gen.alphaNumStr))
        yield JPassThroughPropagator.create(fields.asJava)
    }
  private implicit def arbitrarySTMP: Arbitrary[TextMapPropagator[Context]] =
    Arbitrary {
      for (fields <- Gen.listOf(Gen.alphaNumStr))
        yield PassThroughPropagator[Context, Context.Key](fields)
    }

  private implicit def arbitraryJCP: Arbitrary[JContextPropagators] =
    Arbitrary {
      for (jtmp <- arbitraryJTMP.arbitrary)
        yield JContextPropagators.create(jtmp)
    }
  private implicit def arbitrarySCP: Arbitrary[ContextPropagators[Context]] =
    Arbitrary {
      for (stmp <- arbitrarySTMP.arbitrary)
        yield ContextPropagators.of(stmp)
    }

  property("Java TextMapGetter round trip reference identity") {
    forAll { (tmg: JTextMapGetter[Carrier]) =>
      assert(tmg.asScala.asJava eq tmg)
    }
  }
  property("Scala TextMapGetter round trip reference identity") {
    forAll { (tmg: TextMapGetter[Carrier]) =>
      assert(tmg.asJava.asScala eq tmg)
    }
  }

  property("Java TextMapPropagator round trip reference identity") {
    forAll { (tmp: JTextMapPropagator) =>
      assert(tmp.asScala.asJava eq tmp)
    }
  }
  property("Scala TextMapPropagator round trip reference identity") {
    forAll { (tmp: TextMapPropagator[Context]) =>
      assert(tmp.asJava.asScala eq tmp)
    }
  }

  property("Java ContextPropagators round trip sort of identity") {
    forAll { (cp: JContextPropagators) =>
      assert(cp.asScala.asJava.getTextMapPropagator eq cp.getTextMapPropagator)
    }
  }
  property("Scala ContextPropagators round trip sort of identity") {
    forAll { (cp: ContextPropagators[Context]) =>
      assert(cp.asJava.asScala.textMapPropagator eq cp.textMapPropagator)
    }
  }

  property("Java TextMapGetter and wrapper behavior equivalence") {
    forAll {
      (
          tmg: JTextMapGetter[Carrier],
          carrier: Carrier,
          otherKeys: Seq[String]
      ) =>
        val wrapper = tmg.asScala
        assertEquals(
          wrapper.keys(carrier).toSeq,
          tmg.keys(carrier).asScala.toSeq
        )
        for (key <- carrier.keys ++ otherKeys) {
          assertEquals(wrapper.get(carrier, key), Option(tmg.get(carrier, key)))
        }
    }
  }
  property("Scala TextMapGetter and wrapper behavior equivalence") {
    forAll {
      (
          tmg: TextMapGetter[Carrier],
          carrier: Carrier,
          otherKeys: Seq[String]
      ) =>
        val wrapper = tmg.asJava
        assertEquals(
          wrapper.keys(carrier).asScala.toSeq,
          tmg.keys(carrier).toSeq
        )
        for (key <- carrier.keys ++ otherKeys) {
          assertEquals(Option(wrapper.get(carrier, key)), tmg.get(carrier, key))
        }
    }
  }

  property("Java TextMapPropagator and wrapper behavior equivalence") {
    forAll {
      (entries: Carrier, extraFields: Set[String], unpropagated: Carrier) =>
        val tmp =
          JPassThroughPropagator.create((entries.keySet ++ extraFields).asJava)
        val wrapper = tmp.asScala
        assertEquals(wrapper.fields.toSeq, tmp.fields().asScala.toSeq)
        val toSkip = unpropagated.view
          .filterKeys(k => !entries.contains(k) && !extraFields.contains(k))
          .toMap
        val extracted =
          tmp.extract(Context.root.underlying, entries ++ toSkip, mkJTMG)
        val wrapperExtracted =
          wrapper.extract(Context.root, entries ++ toSkip)
        var injected = Map.empty[String, String]
        tmp.inject[Carrier](
          extracted,
          null,
          (_, k, v) => injected = injected.updated(k, v)
        )
        val wrapperInjected =
          wrapper.inject(wrapperExtracted, Map.empty[String, String])
        assertEquals(wrapperInjected, injected)
    }
  }
  property("Scala TextMapPropagator and wrapper behavior equivalence") {
    forAll {
      (entries: Carrier, extraFields: Set[String], unpropagated: Carrier) =>
        val tmp: TextMapPropagator[Context] =
          PassThroughPropagator(entries.keySet ++ extraFields)
        val wrapper = tmp.asJava
        assertEquals(wrapper.fields().asScala.toSeq, tmp.fields.toSeq)
        val toSkip = unpropagated.view
          .filterKeys(k => !entries.contains(k) && !extraFields.contains(k))
          .toMap
        val extracted =
          tmp.extract(Context.root, entries ++ toSkip)
        val wrapperExtracted =
          wrapper.extract(Context.root.underlying, entries ++ toSkip, mkJTMG)
        val injected = tmp.inject(extracted, Map.empty[String, String])
        var wrapperInjected = Map.empty[String, String]
        wrapper.inject[Carrier](
          wrapperExtracted,
          null,
          (_, k, v) => wrapperInjected = wrapperInjected.updated(k, v)
        )
        assertEquals(wrapperInjected, injected)
    }
  }

  property("Java ContextPropagators and wrapper behavior equivalence") {
    forAll {
      (entries: Carrier, extraFields: Set[String], unpropagated: Carrier) =>
        val cp = JContextPropagators.create(
          JPassThroughPropagator.create((entries.keySet ++ extraFields).asJava)
        )
        val wrapper = cp.asScala
        assertEquals(
          wrapper.textMapPropagator.fields.toSeq,
          cp.getTextMapPropagator.fields().asScala.toSeq
        )
        val toSkip = unpropagated.view
          .filterKeys(k => !entries.contains(k) && !extraFields.contains(k))
          .toMap
        val extracted =
          cp.getTextMapPropagator.extract(
            Context.root.underlying,
            entries ++ toSkip,
            mkJTMG
          )
        val wrapperExtracted =
          wrapper.textMapPropagator.extract(Context.root, entries ++ toSkip)
        var injected = Map.empty[String, String]
        cp.getTextMapPropagator.inject[Carrier](
          extracted,
          null,
          (_, k, v) => injected = injected.updated(k, v)
        )
        val wrapperInjected =
          wrapper.textMapPropagator.inject(
            wrapperExtracted,
            Map.empty[String, String]
          )
        assertEquals(wrapperInjected, injected)
    }
  }
  property("Scala ContextPropagators and wrapper behavior equivalence") {
    forAll {
      (entries: Carrier, extraFields: Set[String], unpropagated: Carrier) =>
        val cp = ContextPropagators.of(
          PassThroughPropagator[Context, Context.Key](
            entries.keySet ++ extraFields
          )
        )
        val wrapper = cp.asJava
        assertEquals(
          wrapper.getTextMapPropagator.fields().asScala.toSeq,
          cp.textMapPropagator.fields.toSeq
        )
        val toSkip = unpropagated.view
          .filterKeys(k => !entries.contains(k) && !extraFields.contains(k))
          .toMap
        val extracted =
          cp.textMapPropagator.extract(Context.root, entries ++ toSkip)
        val wrapperExtracted =
          wrapper.getTextMapPropagator.extract(
            Context.root.underlying,
            entries ++ toSkip,
            mkJTMG
          )
        val injected =
          cp.textMapPropagator.inject(extracted, Map.empty[String, String])
        var wrapperInjected = Map.empty[String, String]
        wrapper.getTextMapPropagator.inject[Carrier](
          wrapperExtracted,
          null,
          (_, k, v) => wrapperInjected = wrapperInjected.updated(k, v)
        )
        assertEquals(wrapperInjected, injected)
    }
  }
}
