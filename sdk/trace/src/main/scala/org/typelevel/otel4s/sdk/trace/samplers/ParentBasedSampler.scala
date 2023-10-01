package org.typelevel.otel4s.sdk.trace.samplers

import org.typelevel.otel4s.sdk.Attributes
import org.typelevel.otel4s.sdk.trace.data.LinkData
import org.typelevel.otel4s.trace.{SpanContext, SpanKind}

/** Sampler that uses the sampled flag of the parent Span, if present.
  *
  * If the span has no parent, this Sampler will use the "root" sampler that it
  * is built with.
  */
final class ParentBasedSampler private (
    root: Sampler,
    remoteParentSampled: Sampler,
    remoteParentNotSampled: Sampler,
    localParentSampled: Sampler,
    localParentNotSampled: Sampler
) extends Sampler {

  def shouldSample(
      parentContext: Option[SpanContext],
      traceId: String,
      name: String,
      kind: SpanKind,
      attributes: Attributes,
      parentLinks: List[LinkData]
  ): SamplingResult = {
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
      kind,
      attributes,
      parentLinks
    )
  }

  val description: String =
    s"ParentBasedSampler{root=$root, " +
      s"remoteParentSampled=$remoteParentSampled, " +
      s"remoteParentNotSampled=$remoteParentNotSampled, " +
      s"localParentSampled=$localParentSampled, " +
      s"localParentNotSampled=$localParentNotSampled}"
}

object ParentBasedSampler {

  /** Creates a [[Builder]] for [[ParentBasedSampler]] that enables
    * configuration of the parent-based sampling strategy.
    *
    * The parent's sampling decision is used if a parent span exists, otherwise
    * this strategy uses the root sampler's decision.
    *
    * There are a several options available on the builder to control the
    * precise behavior of how the decision will be made.
    *
    * @param root
    *   the [[Sampler]] which is used to make the sampling decisions if the
    *   parent does not exist
    */
  def builder(root: Sampler): Builder =
    BuilderImpl(root, None, None, None, None)

  /** A builder for creating [[ParentBasedSampler]].
    */
  sealed trait Builder {

    /** Sets the [[Sampler]] to use when there is a remote parent that was
      * sampled.
      *
      * If not set, defaults to always sampling if the remote parent was
      * sampled.
      */
    def setRemoteParentSampled(sampler: Sampler): Builder

    /** Sets the [[Sampler]] to use when there is a remote parent that was not
      * sampled.
      *
      * If not set, defaults to never sampling when the remote parent isn't
      * sampled.
      */
    def setRemoteParentNotSampled(sampler: Sampler): Builder

    /** Sets the [[Sampler]] to use when there is a local parent that was
      * sampled.
      *
      * If not set, defaults to always sampling if the local parent was sampled.
      */
    def setLocalParentSampled(sampler: Sampler): Builder

    /** Sets the [[Sampler]] to use when there is a local parent that was not
      * sampled.
      *
      * If not set, defaults to never sampling when the local parent isn't
      * sampled.
      */
    def setLocalParentNotSampled(sampler: Sampler): Builder

    /** Creates a [[ParentBasedSampler]] using the configuration of this
      * builder.
      */
    def build: Sampler
  }

  private final case class BuilderImpl(
      root: Sampler,
      remoteParentSampled: Option[Sampler],
      remoteParentNotSampled: Option[Sampler],
      localParentSampled: Option[Sampler],
      localParentNotSampled: Option[Sampler]
  ) extends Builder {
    def setRemoteParentSampled(sampler: Sampler): Builder =
      copy(remoteParentSampled = Some(sampler))

    def setRemoteParentNotSampled(sampler: Sampler): Builder =
      copy(remoteParentNotSampled = Some(sampler))

    def setLocalParentSampled(sampler: Sampler): Builder =
      copy(localParentSampled = Some(sampler))

    def setLocalParentNotSampled(sampler: Sampler): Builder =
      copy(localParentNotSampled = Some(sampler))

    def build: Sampler =
      new ParentBasedSampler(
        root,
        remoteParentSampled.getOrElse(Sampler.AlwaysOn),
        remoteParentNotSampled.getOrElse(Sampler.AlwaysOff),
        localParentSampled.getOrElse(Sampler.AlwaysOn),
        localParentNotSampled.getOrElse(Sampler.AlwaysOff)
      )
  }
}
