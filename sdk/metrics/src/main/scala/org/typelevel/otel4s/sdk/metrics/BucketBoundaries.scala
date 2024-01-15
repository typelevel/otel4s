package org.typelevel.otel4s.sdk.metrics

sealed trait BucketBoundaries {
  final def length: Int = boundaries.length
  def boundaries: Vector[Double]
  def bucketIndex(value: Double): Int
}

object BucketBoundaries {
  private val Default = Impl(
    Vector(
      0d, 5d, 10d, 25d, 50d, 75d, 100d, 250d, 500d, 750d, 1_000d, 2_500d,
      5_000d, 7_500d, 10_000d
    )
  )

  def default: BucketBoundaries = Default

  private final case class Impl(
      boundaries: Vector[Double]
  ) extends BucketBoundaries {
    def bucketIndex(value: Double): Int = {
      val idx = boundaries.indexWhere(boundary => value <= boundary)
      if (idx == -1) boundaries.length else idx
    }
  }
}
