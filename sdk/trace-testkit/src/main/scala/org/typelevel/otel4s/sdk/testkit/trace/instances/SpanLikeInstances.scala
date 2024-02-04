package org.typelevel.otel4s.sdk.testkit.trace.instances

import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.testkit.trace.SpanTree

trait SpanLikeInstances {

  implicit val spanDataSpanLike: SpanTree.SpanLike[SpanData] =
    SpanTree.SpanLike.make(
      _.spanContext.spanIdHex,
      _.parentSpanContext.filter(_.isValid).map(_.spanIdHex)
    )

}
