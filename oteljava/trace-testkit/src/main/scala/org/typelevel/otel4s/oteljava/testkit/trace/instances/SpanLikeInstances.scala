package org.typelevel.otel4s.oteljava.testkit.trace.instances

import io.opentelemetry.sdk.trace.data.SpanData
import org.typelevel.otel4s.testkit.trace.SpanTree

trait SpanLikeInstances {

  implicit val spanDataSpanLike: SpanTree.SpanLike[SpanData] =
    SpanTree.SpanLike.make(
      _.getSpanId,
      s => Option.when(s.getParentSpanContext.isValid)(s.getParentSpanId)
    )

}
