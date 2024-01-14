package org.typelevel.otel4s.sdk.metrics.data

sealed trait ExponentialHistogramBuckets {
  def scale: Int
  def offset: Int
  def bucketCounts: Vector[Long]
  def totalCount: Long
}
