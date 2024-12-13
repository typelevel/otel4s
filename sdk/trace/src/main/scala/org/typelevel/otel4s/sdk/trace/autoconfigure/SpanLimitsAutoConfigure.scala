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

package org.typelevel.otel4s.sdk.trace.autoconfigure

import cats.effect.MonadCancelThrow
import cats.effect.Resource
import org.typelevel.otel4s.sdk.autoconfigure.AutoConfigure
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.trace.SpanLimits

/** Autoconfigures [[SpanLimits]].
  *
  * The configuration options:
  * {{{
  * | System property                          | Environment variable                     | Description                                                           |
  * |------------------------------------------|------------------------------------------|-----------------------------------------------------------------------|
  * | otel.span.attribute.count.limit          | OTEL_SPAN_ATTRIBUTE_COUNT_LIMIT          | The maximum allowed span attribute count. Default is `128`.           |
  * | otel.span.event.count.limit              | OTEL_SPAN_EVENT_COUNT_LIMIT              | The maximum allowed span event count. Default is `128`.               |
  * | otel.span.link.count.limit               | OTEL_SPAN_LINK_COUNT_LIMIT               | The maximum allowed span link count. Default is `128`.                |
  * | otel.event.attribute.count.limit         | OTEL_EVENT_ATTRIBUTE_COUNT_LIMIT         | The maximum allowed attribute per span event count. Default is `128`. |
  * | otel.link.attribute.count.limit          | OTEL_LINK_ATTRIBUTE_COUNT_LIMIT          | The maximum allowed attribute per span link count. Default is `128`.  |
  * | otel.span.attribute.value.length.limit   | OTEL_SPAN_ATTRIBUTE_VALUE_LENGTH_LIMIT   | The maximum allowed attribute value size. No limit by default.        |
  * }}}
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/configuration/sdk-environment-variables/#span-limits]]
  */
private final class SpanLimitsAutoConfigure[F[_]: MonadCancelThrow]
    extends AutoConfigure.WithHint[F, SpanLimits](
      "SpanLimits",
      SpanLimitsAutoConfigure.ConfigKeys.All
    ) {

  import SpanLimitsAutoConfigure.ConfigKeys

  def fromConfig(config: Config): Resource[F, SpanLimits] = {
    def configure =
      for {
        maxNumberOfAttributes <- config.get(ConfigKeys.MaxNumberOfAttributes)
        maxNumberOfEvents <- config.get(ConfigKeys.MaxNumberOfEvents)
        maxNumberOfLinks <- config.get(ConfigKeys.MaxNumberOfLinks)
        maxNumberOfAttributesPerEvent <- config.get(
          ConfigKeys.MaxNumberOfAttributesPerEvent
        )
        maxNumberOfAttributesPerLink <- config.get(
          ConfigKeys.MaxNumberOfAttributesPerLink
        )
        maxAttributeValueLength <- config.get(
          ConfigKeys.MaxAttributeValueLength
        )
      } yield {
        val builder = SpanLimits.builder

        val withMaxNumberOfAttributes =
          maxNumberOfAttributes.foldLeft(builder)(
            _.withMaxNumberOfAttributes(_)
          )

        val withMaxNumberOfEvents =
          maxNumberOfEvents.foldLeft(withMaxNumberOfAttributes)(
            _.withMaxNumberOfEvents(_)
          )

        val withMaxNumberOfLinks =
          maxNumberOfLinks.foldLeft(withMaxNumberOfEvents)(
            _.withMaxNumberOfLinks(_)
          )

        val withMaxNumberOfAttributesPerEvent =
          maxNumberOfAttributesPerEvent.foldLeft(withMaxNumberOfLinks)(
            _.withMaxNumberOfAttributesPerEvent(_)
          )

        val withMaxNumberOfAttributesPerLink =
          maxNumberOfAttributesPerLink.foldLeft(
            withMaxNumberOfAttributesPerEvent
          )(
            _.withMaxNumberOfAttributesPerLink(_)
          )

        val withMaxAttributeValueLength =
          maxAttributeValueLength.foldLeft(withMaxNumberOfAttributesPerLink)(
            _.withMaxAttributeValueLength(_)
          )

        withMaxAttributeValueLength.build
      }

    Resource.eval(MonadCancelThrow[F].fromEither(configure))
  }
}

private[sdk] object SpanLimitsAutoConfigure {

  private object ConfigKeys {
    val MaxNumberOfAttributes: Config.Key[Int] =
      Config.Key("otel.span.attribute.count.limit")

    val MaxNumberOfEvents: Config.Key[Int] =
      Config.Key("otel.span.event.count.limit")

    val MaxNumberOfLinks: Config.Key[Int] =
      Config.Key("otel.span.link.count.limit")

    val MaxNumberOfAttributesPerEvent: Config.Key[Int] =
      Config.Key("otel.event.attribute.count.limit")

    val MaxNumberOfAttributesPerLink: Config.Key[Int] =
      Config.Key("otel.link.attribute.count.limit")

    val MaxAttributeValueLength: Config.Key[Int] =
      Config.Key("otel.span.attribute.value.length.limit")

    val All: Set[Config.Key[_]] =
      Set(
        MaxNumberOfAttributes,
        MaxNumberOfEvents,
        MaxNumberOfLinks,
        MaxNumberOfAttributesPerEvent,
        MaxNumberOfAttributesPerLink,
        MaxAttributeValueLength
      )
  }

  /** Returns [[AutoConfigure]] that configures the [[SpanLimits]].
    *
    * The configuration options:
    * {{{
    * | System property                          | Environment variable                     | Description                                                           |
    * |------------------------------------------|------------------------------------------|-----------------------------------------------------------------------|
    * | otel.span.attribute.count.limit          | OTEL_SPAN_ATTRIBUTE_COUNT_LIMIT          | The maximum allowed span attribute count. Default is `128`.           |
    * | otel.span.event.count.limit              | OTEL_SPAN_EVENT_COUNT_LIMIT              | The maximum allowed span event count. Default is `128`.               |
    * | otel.span.link.count.limit               | OTEL_SPAN_LINK_COUNT_LIMIT               | The maximum allowed span link count. Default is `128`.                |
    * | otel.event.attribute.count.limit         | OTEL_EVENT_ATTRIBUTE_COUNT_LIMIT         | The maximum allowed attribute per span event count. Default is `128`. |
    * | otel.link.attribute.count.limit          | OTEL_LINK_ATTRIBUTE_COUNT_LIMIT          | The maximum allowed attribute per span link count. Default is `128`.  |
    * | otel.span.attribute.value.length.limit   | OTEL_SPAN_ATTRIBUTE_VALUE_LENGTH_LIMIT   | The maximum allowed attribute value size. No limit by default.        |
    * }}}
    *
    * @see
    *   [[https://opentelemetry.io/docs/specs/otel/configuration/sdk-environment-variables/#span-limits]]
    */
  def apply[F[_]: MonadCancelThrow]: AutoConfigure[F, SpanLimits] =
    new SpanLimitsAutoConfigure[F]

}
