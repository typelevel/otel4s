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
package sdk.trace.samplers

import cats.Applicative
import org.typelevel.otel4s.sdk.trace.data.LinkData
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.SpanKind
import scodec.bits.ByteVector

/** Sampler that uses the sampled flag of the parent Span, if present.
  *
  * If the span has no parent, this Sampler will use the "root" sampler that it is built with.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/trace/sdk/#parentbased]]
  */
private[samplers] final case class ParentBasedSampler[F[_]] private (
    root: Sampler[F],
    remoteParentSampled: Sampler[F],
    remoteParentNotSampled: Sampler[F],
    localParentSampled: Sampler[F],
    localParentNotSampled: Sampler[F]
) extends Sampler.Unsealed[F] {

  def shouldSample(
      parentContext: Option[SpanContext],
      traceId: ByteVector,
      name: String,
      spanKind: SpanKind,
      attributes: Attributes,
      parentLinks: Vector[LinkData]
  ): F[SamplingResult] = {
    val sampler = parentContext.filter(_.isValid) match {
      case Some(ctx) if ctx.isRemote =>
        if (ctx.isSampled) remoteParentSampled else remoteParentNotSampled

      case Some(ctx) =>
        if (ctx.isSampled) localParentSampled else localParentNotSampled

      case None =>
        root
    }

    sampler.shouldSample(
      parentContext,
      traceId,
      name,
      spanKind,
      attributes,
      parentLinks
    )
  }

  val description: String =
    s"ParentBased{root=$root, " +
      s"remoteParentSampled=$remoteParentSampled, " +
      s"remoteParentNotSampled=$remoteParentNotSampled, " +
      s"localParentSampled=$localParentSampled, " +
      s"localParentNotSampled=$localParentNotSampled}"
}

object ParentBasedSampler {

  /** Creates a [[Builder]] for the parent-based sampler that enables configuration of the parent-based sampling
    * strategy.
    *
    * The parent's sampling decision is used if a parent span exists, otherwise this strategy uses the root sampler's
    * decision.
    *
    * There are a several options available on the builder to control the precise behavior of how the decision will be
    * made.
    *
    * @param root
    *   the [[Sampler]] which is used to make the sampling decisions if the parent does not exist
    */
  def builder[F[_]: Applicative](root: Sampler[F]): Builder[F] =
    BuilderImpl(root, None, None, None, None)

  /** A builder for creating parent-based sampler.
    */
  sealed trait Builder[F[_]] {

    /** Assigns the [[Sampler]] to use when there is a remote parent that was sampled.
      *
      * If not set, defaults to always sampling if the remote parent was sampled.
      */
    def withRemoteParentSampled(sampler: Sampler[F]): Builder[F]

    /** Assigns the [[Sampler]] to use when there is a remote parent that was not sampled.
      *
      * If not set, defaults to never sampling when the remote parent isn't sampled.
      */
    def withRemoteParentNotSampled(sampler: Sampler[F]): Builder[F]

    /** Assigns the [[Sampler]] to use when there is a local parent that was sampled.
      *
      * If not set, defaults to always sampling if the local parent was sampled.
      */
    def withLocalParentSampled(sampler: Sampler[F]): Builder[F]

    /** Assigns the [[Sampler]] to use when there is a local parent that was not sampled.
      *
      * If not set, defaults to never sampling when the local parent isn't sampled.
      */
    def withLocalParentNotSampled(sampler: Sampler[F]): Builder[F]

    /** Creates a parent-based sampler using the configuration of this builder.
      */
    def build: Sampler[F]
  }

  private final case class BuilderImpl[F[_]: Applicative](
      root: Sampler[F],
      remoteParentSampled: Option[Sampler[F]],
      remoteParentNotSampled: Option[Sampler[F]],
      localParentSampled: Option[Sampler[F]],
      localParentNotSampled: Option[Sampler[F]]
  ) extends Builder[F] {
    def withRemoteParentSampled(sampler: Sampler[F]): Builder[F] =
      copy(remoteParentSampled = Some(sampler))

    def withRemoteParentNotSampled(sampler: Sampler[F]): Builder[F] =
      copy(remoteParentNotSampled = Some(sampler))

    def withLocalParentSampled(sampler: Sampler[F]): Builder[F] =
      copy(localParentSampled = Some(sampler))

    def withLocalParentNotSampled(sampler: Sampler[F]): Builder[F] =
      copy(localParentNotSampled = Some(sampler))

    def build: Sampler[F] =
      ParentBasedSampler(
        root,
        remoteParentSampled.getOrElse(Sampler.alwaysOn),
        remoteParentNotSampled.getOrElse(Sampler.alwaysOff),
        localParentSampled.getOrElse(Sampler.alwaysOn),
        localParentNotSampled.getOrElse(Sampler.alwaysOff)
      )
  }
}
