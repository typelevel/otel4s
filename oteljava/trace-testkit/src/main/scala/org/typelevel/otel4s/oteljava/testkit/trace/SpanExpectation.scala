/*
 * Copyright 2024 Typelevel
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

package org.typelevel.otel4s.oteljava.testkit.trace

import cats.data.NonEmptyList
import io.opentelemetry.api.trace.{SpanKind => JSpanKind}
import io.opentelemetry.sdk.common.{InstrumentationScopeInfo => JInstrumentationScopeInfo}
import io.opentelemetry.sdk.resources.{Resource => JResource}
import io.opentelemetry.sdk.trace.data.SpanData
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.oteljava.AttributeConverters._
import org.typelevel.otel4s.oteljava.testkit.AttributesExpectation
import org.typelevel.otel4s.oteljava.testkit.ExpectationChecks
import org.typelevel.otel4s.oteljava.testkit.InstrumentationScopeExpectation
import org.typelevel.otel4s.oteljava.testkit.TelemetryResourceExpectation
import org.typelevel.otel4s.oteljava.trace.SpanContextConversions
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.SpanKind

import scala.concurrent.duration._
import scala.concurrent.duration.FiniteDuration
import scala.jdk.CollectionConverters._

/** A partial expectation for a single exported OpenTelemetry Java `SpanData`. */
sealed trait SpanExpectation {
  private[trace] def expectedName: Option[String]

  /** An optional human-readable clue shown in mismatch messages. */
  def clue: Option[String]

  /** Requires the span name to match exactly. */
  def name(name: String): SpanExpectation

  /** Requires the span kind to match exactly. */
  def kind(kind: SpanKind): SpanExpectation

  /** Requires the span status to match the given expectation. */
  def status(expectation: StatusExpectation): SpanExpectation

  /** Requires the span context to match the given expectation. */
  def spanContext(expectation: SpanContextExpectation): SpanExpectation

  /** Requires the span context to match exactly. */
  def spanContextExact(spanContext: SpanContext): SpanExpectation

  /** Requires the parent span context to match the given expectation. */
  def parentSpanContext(expectation: SpanContextExpectation): SpanExpectation

  /** Requires the parent span context to match exactly. */
  def parentSpanContextExact(spanContext: SpanContext): SpanExpectation

  /** Requires the span to have no parent span context. */
  def noParentSpanContext: SpanExpectation

  /** Requires the span start timestamp to match exactly. */
  def startTimestamp(timestamp: FiniteDuration): SpanExpectation

  /** Requires the span end timestamp to match exactly. */
  def endTimestamp(timestamp: FiniteDuration): SpanExpectation

  /** Requires the span end timestamp to match exactly, including missing end timestamps. */
  def endTimestamp(timestamp: Option[FiniteDuration]): SpanExpectation

  /** Requires the span to be ended. */
  def hasEnded: SpanExpectation

  /** Requires the span to not be ended. */
  def hasNotEnded: SpanExpectation

  /** Requires the span attributes to match the given expectation. */
  def attributes(expectation: AttributesExpectation): SpanExpectation

  /** Requires the span attributes to match exactly. */
  def attributesExact(attributes: Attributes): SpanExpectation

  /** Requires the span attributes to match exactly. */
  def attributesExact(attributes: Attribute[_]*): SpanExpectation

  /** Requires the span attributes to contain at least the given subset. */
  def attributesSubset(attributes: Attributes): SpanExpectation

  /** Requires the span attributes to contain at least the given subset. */
  def attributesSubset(attributes: Attribute[_]*): SpanExpectation

  /** Requires the span to have no attributes. */
  def attributesEmpty: SpanExpectation

  /** Adds a collection-level expectation over the span events. */
  def events(expectation: EventSetExpectation): SpanExpectation

  /** Requires the span to contain all given event expectations. */
  def containsEvents(first: EventExpectation, rest: EventExpectation*): SpanExpectation

  /** Requires the span events to match the given expectations exactly. */
  def exactlyEvents(first: EventExpectation, rest: EventExpectation*): SpanExpectation

  /** Requires the span to have exactly the given number of events. */
  def eventCount(count: Int): SpanExpectation

  /** Adds a collection-level expectation over the span links. */
  def links(expectation: LinkSetExpectation): SpanExpectation

  /** Requires the span to contain all given link expectations. */
  def containsLinks(first: LinkExpectation, rest: LinkExpectation*): SpanExpectation

  /** Requires the span links to match the given expectations exactly. */
  def exactlyLinks(first: LinkExpectation, rest: LinkExpectation*): SpanExpectation

  /** Requires the span to have exactly the given number of links. */
  def linkCount(count: Int): SpanExpectation

  /** Requires the instrumentation scope name to match exactly. */
  def scopeName(name: String): SpanExpectation

  /** Requires the instrumentation scope to match the given expectation. */
  def scope(scope: InstrumentationScopeExpectation): SpanExpectation

  /** Requires the telemetry resource to match the given expectation. */
  def resource(resource: TelemetryResourceExpectation): SpanExpectation

  /** Attaches a human-readable clue to this expectation. */
  def clue(text: String): SpanExpectation

  /** Adds a custom predicate over the span data. */
  def where(f: SpanData => Boolean): SpanExpectation

  /** Adds a custom predicate over the span data with a clue shown in mismatches. */
  def where(clue: String)(f: SpanData => Boolean): SpanExpectation

  /** Checks the given span and returns structured mismatches when the expectation does not match. */
  def check(span: SpanData): Either[NonEmptyList[SpanExpectation.Mismatch], Unit]

  /** Returns `true` if this expectation matches the given span. */
  final def matches(span: SpanData): Boolean =
    check(span).isRight
}

object SpanExpectation {

  /** A structured reason explaining why a [[SpanExpectation]] did not match a span. */
  sealed trait Mismatch extends Product with Serializable {
    def message: String
  }

  object Mismatch {

    private[trace] final case class NameMismatch(expected: String, actual: String) extends Mismatch {
      def message: String = s"name mismatch: expected '$expected', got '$actual'"
    }

    private[trace] final case class KindMismatch(expected: SpanKind, actual: SpanKind) extends Mismatch {
      def message: String = s"kind mismatch: expected '$expected', got '$actual'"
    }

    private[trace] final case class StatusMismatch(mismatches: NonEmptyList[StatusExpectation.Mismatch])
        extends Mismatch {
      def message: String = s"status mismatch: ${mismatches.toList.map(_.message).mkString(", ")}"
    }

    private[trace] final case class SpanContextMismatch(mismatches: NonEmptyList[SpanContextExpectation.Mismatch])
        extends Mismatch {
      def message: String = s"span context mismatch: ${mismatches.toList.map(_.message).mkString(", ")}"
    }

    private[trace] final case class ParentSpanContextMismatch(
        mismatches: NonEmptyList[SpanContextExpectation.Mismatch]
    ) extends Mismatch {
      def message: String = s"parent span context mismatch: ${mismatches.toList.map(_.message).mkString(", ")}"
    }

    private[trace] case object MissingParentSpanContext extends Mismatch {
      def message: String = "parent span context mismatch: expected a parent, got <missing>"
    }

    private[trace] final case class UnexpectedParentSpanContext(actual: SpanContext) extends Mismatch {
      def message: String = s"parent span context mismatch: expected <missing>, got '$actual'"
    }

    private[trace] final case class StartTimestampMismatch(expected: FiniteDuration, actual: FiniteDuration)
        extends Mismatch {
      def message: String = s"start timestamp mismatch: expected '$expected', got '$actual'"
    }

    private[trace] final case class EndTimestampMismatch(
        expected: Option[FiniteDuration],
        actual: Option[FiniteDuration]
    ) extends Mismatch {
      def message: String = s"end timestamp mismatch: expected ${render(expected)}, got ${render(actual)}"
      private def render(value: Option[FiniteDuration]): String = value.fold("<missing>")(v => s"'$v'")
    }

    private[trace] final case class EndedMismatch(expected: Boolean, actual: Boolean) extends Mismatch {
      def message: String = s"ended mismatch: expected '$expected', got '$actual'"
    }

    private[trace] final case class AttributesMismatch(mismatches: NonEmptyList[AttributesExpectation.Mismatch])
        extends Mismatch {
      def message: String = s"attributes mismatch: ${mismatches.toList.map(_.message).mkString(", ")}"
    }

    private[trace] final case class EventsMismatch(
        mismatches: NonEmptyList[EventSetExpectation.Mismatch],
        clue: Option[String]
    ) extends Mismatch {
      def message: String =
        s"events mismatch${clue.fold("")(v => s" [$v]")}: ${mismatches.toList.map(_.message).mkString(", ")}"
    }

    private[trace] final case class LinksMismatch(
        mismatches: NonEmptyList[LinkSetExpectation.Mismatch],
        clue: Option[String]
    ) extends Mismatch {
      def message: String =
        s"links mismatch${clue.fold("")(v => s" [$v]")}: ${mismatches.toList.map(_.message).mkString(", ")}"
    }

    private[trace] final case class ScopeMismatch(mismatches: NonEmptyList[InstrumentationScopeExpectation.Mismatch])
        extends Mismatch {
      def message: String = s"scope mismatch: ${mismatches.toList.map(_.message).mkString(", ")}"
    }

    private[trace] final case class ResourceMismatch(mismatches: NonEmptyList[TelemetryResourceExpectation.Mismatch])
        extends Mismatch {
      def message: String = s"resource mismatch: ${mismatches.toList.map(_.message).mkString(", ")}"
    }

    private[trace] final case class PredicateMismatch(clue: Option[String]) extends Mismatch {
      def message: String = s"predicate mismatch${clue.fold("")(v => s": $v")}"
    }
  }

  /** Creates an expectation that leaves all span fields unconstrained. */
  def any: SpanExpectation = Impl()

  /** Creates an expectation matching a span with the given name. */
  def name(name: String): SpanExpectation = Impl(name = Some(name))

  /** Creates an expectation matching an internal span with the given name. */
  def internal(name: String): SpanExpectation = this.name(name).kind(SpanKind.Internal)

  /** Creates an expectation matching a server span with the given name. */
  def server(name: String): SpanExpectation = this.name(name).kind(SpanKind.Server)

  /** Creates an expectation matching a client span with the given name. */
  def client(name: String): SpanExpectation = this.name(name).kind(SpanKind.Client)

  /** Creates an expectation matching a producer span with the given name. */
  def producer(name: String): SpanExpectation = this.name(name).kind(SpanKind.Producer)

  /** Creates an expectation matching a consumer span with the given name. */
  def consumer(name: String): SpanExpectation = this.name(name).kind(SpanKind.Consumer)

  private final case class Impl(
      name: Option[String] = None,
      kind: Option[SpanKind] = None,
      status: Option[StatusExpectation] = None,
      spanContext: Option[SpanContextExpectation] = None,
      parentSpanContext: Option[Option[SpanContextExpectation]] = None,
      startTimestamp: Option[FiniteDuration] = None,
      endTimestamp: Option[Option[FiniteDuration]] = None,
      ended: Option[Boolean] = None,
      attributes: Option[AttributesExpectation] = None,
      eventExpectations: List[EventSetExpectation] = Nil,
      linkExpectations: List[LinkSetExpectation] = Nil,
      scope: Option[InstrumentationScopeExpectation] = None,
      resource: Option[TelemetryResourceExpectation] = None,
      clue: Option[String] = None,
      predicates: List[(SpanData => Boolean, Option[String])] = Nil
  ) extends SpanExpectation {
    private[trace] def expectedName: Option[String] = name

    def name(name: String): SpanExpectation = copy(name = Some(name))
    def kind(kind: SpanKind): SpanExpectation = copy(kind = Some(kind))
    def status(expectation: StatusExpectation): SpanExpectation = copy(status = Some(expectation))
    def spanContext(expectation: SpanContextExpectation): SpanExpectation = copy(spanContext = Some(expectation))
    def spanContextExact(spanContext: SpanContext): SpanExpectation =
      this.spanContext(SpanContextExpectation.exact(spanContext))
    def parentSpanContext(expectation: SpanContextExpectation): SpanExpectation =
      copy(parentSpanContext = Some(Some(expectation)))
    def parentSpanContextExact(spanContext: SpanContext): SpanExpectation =
      parentSpanContext(SpanContextExpectation.exact(spanContext))
    def noParentSpanContext: SpanExpectation = copy(parentSpanContext = Some(None))
    def startTimestamp(timestamp: FiniteDuration): SpanExpectation = copy(startTimestamp = Some(timestamp))
    def endTimestamp(timestamp: FiniteDuration): SpanExpectation = endTimestamp(Some(timestamp))
    def endTimestamp(timestamp: Option[FiniteDuration]): SpanExpectation = copy(endTimestamp = Some(timestamp))
    def hasEnded: SpanExpectation = copy(ended = Some(true))
    def hasNotEnded: SpanExpectation = copy(ended = Some(false))
    def attributes(expectation: AttributesExpectation): SpanExpectation = copy(attributes = Some(expectation))
    def attributesExact(attributes: Attributes): SpanExpectation =
      this.attributes(AttributesExpectation.exact(attributes))
    def attributesExact(attributes: Attribute[_]*): SpanExpectation = attributesExact(Attributes(attributes: _*))
    def attributesSubset(attributes: Attributes): SpanExpectation =
      this.attributes(AttributesExpectation.subset(attributes))
    def attributesSubset(attributes: Attribute[_]*): SpanExpectation = attributesSubset(Attributes(attributes: _*))
    def attributesEmpty: SpanExpectation = attributesExact(Attributes.empty)
    def events(expectation: EventSetExpectation): SpanExpectation =
      copy(eventExpectations = eventExpectations :+ expectation)
    def containsEvents(first: EventExpectation, rest: EventExpectation*): SpanExpectation =
      events(EventSetExpectation.contains(first, rest: _*))
    def exactlyEvents(first: EventExpectation, rest: EventExpectation*): SpanExpectation =
      events(EventSetExpectation.exactly(first, rest: _*))
    def eventCount(count: Int): SpanExpectation = events(EventSetExpectation.count(count))
    def links(expectation: LinkSetExpectation): SpanExpectation =
      copy(linkExpectations = linkExpectations :+ expectation)
    def containsLinks(first: LinkExpectation, rest: LinkExpectation*): SpanExpectation =
      links(LinkSetExpectation.contains(first, rest: _*))
    def exactlyLinks(first: LinkExpectation, rest: LinkExpectation*): SpanExpectation =
      links(LinkSetExpectation.exactly(first, rest: _*))
    def linkCount(count: Int): SpanExpectation = links(LinkSetExpectation.count(count))
    def scopeName(name: String): SpanExpectation =
      copy(scope = Some(scope.fold(InstrumentationScopeExpectation.name(name))(_.name(name))))
    def scope(scope: InstrumentationScopeExpectation): SpanExpectation = copy(scope = Some(scope))
    def resource(resource: TelemetryResourceExpectation): SpanExpectation = copy(resource = Some(resource))
    def clue(text: String): SpanExpectation = copy(clue = Some(text))
    def where(f: SpanData => Boolean): SpanExpectation = copy(predicates = predicates :+ (f -> None))
    def where(clue: String)(f: SpanData => Boolean): SpanExpectation =
      copy(predicates = predicates :+ (f -> Some(clue)))

    def check(span: SpanData): Either[NonEmptyList[Mismatch], Unit] =
      ExpectationChecks.combine(
        name.fold(ExpectationChecks.success[Mismatch]) { expected =>
          if (expected == span.getName) ExpectationChecks.success
          else ExpectationChecks.mismatch(Mismatch.NameMismatch(expected, span.getName))
        },
        kind.fold(ExpectationChecks.success[Mismatch]) { expected =>
          val actual = toScalaKind(span)
          if (expected == actual) ExpectationChecks.success
          else ExpectationChecks.mismatch(Mismatch.KindMismatch(expected, actual))
        },
        status.fold(ExpectationChecks.success[Mismatch]) { expected =>
          ExpectationChecks.nested(expected.check(span.getStatus))(Mismatch.StatusMismatch(_))
        },
        spanContext.fold(ExpectationChecks.success[Mismatch]) { expected =>
          ExpectationChecks.nested(expected.check(SpanContextConversions.toScala(span.getSpanContext)))(
            Mismatch.SpanContextMismatch(_)
          )
        },
        parentSpanContext.fold(ExpectationChecks.success[Mismatch]) {
          case None =>
            parentContext(span) match {
              case None        => ExpectationChecks.success
              case Some(value) => ExpectationChecks.mismatch(Mismatch.UnexpectedParentSpanContext(value))
            }
          case Some(expected) =>
            parentContext(span) match {
              case Some(actual) =>
                ExpectationChecks.nested(expected.check(actual))(Mismatch.ParentSpanContextMismatch(_))
              case None =>
                ExpectationChecks.mismatch(Mismatch.MissingParentSpanContext)
            }
        },
        startTimestamp.fold(ExpectationChecks.success[Mismatch]) { expected =>
          val actual = span.getStartEpochNanos.nanos
          if (expected == actual) ExpectationChecks.success
          else ExpectationChecks.mismatch(Mismatch.StartTimestampMismatch(expected, actual))
        },
        endTimestamp.fold(ExpectationChecks.success[Mismatch]) { expected =>
          val actual = Option.when(span.hasEnded())(span.getEndEpochNanos.nanos)
          if (expected == actual) ExpectationChecks.success
          else ExpectationChecks.mismatch(Mismatch.EndTimestampMismatch(expected, actual))
        },
        ended.fold(ExpectationChecks.success[Mismatch]) { expected =>
          val actual = span.hasEnded()
          if (expected == actual) ExpectationChecks.success
          else ExpectationChecks.mismatch(Mismatch.EndedMismatch(expected, actual))
        },
        attributes.fold(ExpectationChecks.success[Mismatch]) { expected =>
          ExpectationChecks.nested(expected.check(span.getAttributes.toScala))(Mismatch.AttributesMismatch(_))
        },
        ExpectationChecks.combine(eventExpectations.map { expectation =>
          ExpectationChecks.nested(expectation.check(span.getEvents.asScala.toList))(mismatches =>
            Mismatch.EventsMismatch(mismatches, expectation.clue)
          )
        }),
        ExpectationChecks.combine(linkExpectations.map { expectation =>
          ExpectationChecks.nested(expectation.check(span.getLinks.asScala.toList))(mismatches =>
            Mismatch.LinksMismatch(mismatches, expectation.clue)
          )
        }),
        scope.fold(ExpectationChecks.success[Mismatch]) { expected =>
          ExpectationChecks.nested(expected.check(scopeInfo(span)))(Mismatch.ScopeMismatch(_))
        },
        resource.fold(ExpectationChecks.success[Mismatch]) { expected =>
          ExpectationChecks.nested(expected.check(resourceInfo(span)))(Mismatch.ResourceMismatch(_))
        },
        ExpectationChecks.combine(predicates.map { case (predicate, clue) =>
          if (predicate(span)) ExpectationChecks.success
          else ExpectationChecks.mismatch(Mismatch.PredicateMismatch(clue))
        })
      )
  }

  private[trace] def toScalaKind(span: SpanData): SpanKind =
    span.getKind match {
      case JSpanKind.INTERNAL => SpanKind.Internal
      case JSpanKind.SERVER   => SpanKind.Server
      case JSpanKind.CLIENT   => SpanKind.Client
      case JSpanKind.PRODUCER => SpanKind.Producer
      case JSpanKind.CONSUMER => SpanKind.Consumer
    }

  private def parentContext(span: SpanData): Option[SpanContext] = {
    val parent = span.getParentSpanContext
    Option.when(parent.isValid)(SpanContextConversions.toScala(parent))
  }

  private def scopeInfo(span: SpanData): JInstrumentationScopeInfo =
    span.getInstrumentationScopeInfo

  private def resourceInfo(span: SpanData): JResource =
    span.getResource
}
