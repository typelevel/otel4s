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
package sdk
package trace

import org.scalacheck.Cogen
import org.scalacheck.rng.Seed
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.trace.data.EventData
import org.typelevel.otel4s.sdk.trace.data.LinkData
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.sdk.trace.data.StatusData
import org.typelevel.otel4s.sdk.trace.samplers.SamplingDecision
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.SpanKind
import org.typelevel.otel4s.trace.Status
import org.typelevel.otel4s.trace.TraceFlags
import org.typelevel.otel4s.trace.TraceState

import scala.concurrent.duration.FiniteDuration

object Cogens {

  implicit val attributeTypeCogen: Cogen[AttributeType[_]] =
    Cogen[String].contramap(_.toString)

  implicit def attributeKeyCogen[A]: Cogen[AttributeKey[A]] =
    Cogen[(String, String)].contramap[AttributeKey[A]] { attribute =>
      (attribute.name, attribute.`type`.toString)
    }

  implicit def attributeCogen[A: Cogen]: Cogen[Attribute[A]] =
    Cogen[(AttributeKey[A], A)].contramap(a => (a.key, a.value))

  implicit val attributeExistentialCogen: Cogen[Attribute[_]] =
    Cogen { (seed, attr) =>
      def primitive[A: Cogen](seed: Seed): Seed =
        Cogen[A].perturb(seed, attr.value.asInstanceOf[A])

      def list[A: Cogen](seed: Seed): Seed =
        Cogen[List[A]].perturb(seed, attr.value.asInstanceOf[List[A]])

      val valueCogen: Seed => Seed = attr.key.`type` match {
        case AttributeType.Boolean     => primitive[Boolean]
        case AttributeType.Double      => primitive[Double]
        case AttributeType.String      => primitive[String]
        case AttributeType.Long        => primitive[Long]
        case AttributeType.BooleanList => list[Boolean]
        case AttributeType.DoubleList  => list[Double]
        case AttributeType.StringList  => list[String]
        case AttributeType.LongList    => list[Long]
      }

      valueCogen(attributeKeyCogen.perturb(seed, attr.key))
    }

  implicit val attributesCogen: Cogen[Attributes] =
    Cogen[List[Attribute[_]]].contramap(_.toList)

  implicit val instrumentationScopeCogen: Cogen[InstrumentationScope] =
    Cogen[(String, Option[String], Option[String], Attributes)].contramap { s =>
      (s.name, s.version, s.schemaUrl, s.attributes)
    }

  implicit val resourceCogen: Cogen[Resource] =
    Cogen[(Attributes, Option[String])].contramap { r =>
      (r.attributes, r.schemaUrl)
    }

  implicit val samplingDecisionCogen: Cogen[SamplingDecision] =
    Cogen[String].contramap(_.toString)

  implicit val spanKindCogen: Cogen[SpanKind] =
    Cogen[String].contramap(_.toString)

  implicit val statusCogen: Cogen[Status] =
    Cogen[String].contramap(_.toString)

  implicit val traceFlagsCogen: Cogen[TraceFlags] =
    Cogen[Byte].contramap(_.toByte)

  implicit val traceStateCogen: Cogen[TraceState] =
    Cogen[Map[String, String]].contramap(_.asMap)

  implicit val spanContextCogen: Cogen[SpanContext] =
    Cogen[(String, String, TraceFlags, TraceState, Boolean, Boolean)]
      .contramap { c =>
        (
          c.traceIdHex,
          c.spanIdHex,
          c.traceFlags,
          c.traceState,
          c.isRemote,
          c.isValid
        )
      }

  implicit val eventDataCogen: Cogen[EventData] =
    Cogen[(String, FiniteDuration, Attributes)].contramap { data =>
      (data.name, data.timestamp, data.attributes)
    }

  implicit val linkDataCogen: Cogen[LinkData] =
    Cogen[(SpanContext, Attributes)].contramap { data =>
      (data.spanContext, data.attributes)
    }

  implicit val statusDataCogen: Cogen[StatusData] =
    Cogen[(Status, Option[String])].contramap { data =>
      (data.status, data.description)
    }

  implicit val spanDataCogen: Cogen[SpanData] = Cogen[
    (
        String,
        SpanContext,
        Option[SpanContext],
        SpanKind,
        FiniteDuration,
        Option[FiniteDuration],
        StatusData,
        Attributes,
        Vector[EventData],
        Vector[LinkData],
        InstrumentationScope,
        Resource
    )
  ].contramap { spanData =>
    (
      spanData.name,
      spanData.spanContext,
      spanData.parentSpanContext,
      spanData.kind,
      spanData.startTimestamp,
      spanData.endTimestamp,
      spanData.status,
      spanData.attributes,
      spanData.events,
      spanData.links,
      spanData.instrumentationScope,
      spanData.resource
    )
  }

}
