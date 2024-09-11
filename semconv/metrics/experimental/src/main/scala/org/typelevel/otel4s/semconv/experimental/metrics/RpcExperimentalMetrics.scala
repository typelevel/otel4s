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

package org.typelevel.otel4s
package semconv
package metrics

import org.typelevel.otel4s.metrics._

// DO NOT EDIT, this is an Auto-generated file from buildscripts/templates/registry/otel4s/metrics/SemanticMetrics.scala.j2
object RpcExperimentalMetrics {

  /** Measures the duration of outbound RPC. <p>
    * @note
    *   <p> While streaming RPCs may record this metric as start-of-batch to end-of-batch, it's hard to interpret in
    *   practice. <p> <strong>Streaming</strong>: N/A.
    */
  object ClientDuration {

    val Name = "rpc.client.duration"
    val Description = "Measures the duration of outbound RPC."
    val Unit = "ms"

    def create[F[_]: Meter](
        boundaries: BucketBoundaries
    ): F[Histogram[F, Double]] =
      Meter[F]
        .histogram[Double](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

  /** Measures the size of RPC request messages (uncompressed). <p>
    * @note
    *   <p> <strong>Streaming</strong>: Recorded per message in a streaming batch
    */
  object ClientRequestSize {

    val Name = "rpc.client.request.size"
    val Description =
      "Measures the size of RPC request messages (uncompressed)."
    val Unit = "By"

    def create[F[_]: Meter](
        boundaries: BucketBoundaries
    ): F[Histogram[F, Double]] =
      Meter[F]
        .histogram[Double](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

  /** Measures the number of messages received per RPC. <p>
    * @note
    *   <p> Should be 1 for all non-streaming RPCs. <p> <strong>Streaming</strong>: This metric is required for server
    *   and client streaming RPCs
    */
  object ClientRequestsPerRpc {

    val Name = "rpc.client.requests_per_rpc"
    val Description = "Measures the number of messages received per RPC."
    val Unit = "{count}"

    def create[F[_]: Meter](
        boundaries: BucketBoundaries
    ): F[Histogram[F, Double]] =
      Meter[F]
        .histogram[Double](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

  /** Measures the size of RPC response messages (uncompressed). <p>
    * @note
    *   <p> <strong>Streaming</strong>: Recorded per response in a streaming batch
    */
  object ClientResponseSize {

    val Name = "rpc.client.response.size"
    val Description =
      "Measures the size of RPC response messages (uncompressed)."
    val Unit = "By"

    def create[F[_]: Meter](
        boundaries: BucketBoundaries
    ): F[Histogram[F, Double]] =
      Meter[F]
        .histogram[Double](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

  /** Measures the number of messages sent per RPC. <p>
    * @note
    *   <p> Should be 1 for all non-streaming RPCs. <p> <strong>Streaming</strong>: This metric is required for server
    *   and client streaming RPCs
    */
  object ClientResponsesPerRpc {

    val Name = "rpc.client.responses_per_rpc"
    val Description = "Measures the number of messages sent per RPC."
    val Unit = "{count}"

    def create[F[_]: Meter](
        boundaries: BucketBoundaries
    ): F[Histogram[F, Double]] =
      Meter[F]
        .histogram[Double](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

  /** Measures the duration of inbound RPC. <p>
    * @note
    *   <p> While streaming RPCs may record this metric as start-of-batch to end-of-batch, it's hard to interpret in
    *   practice. <p> <strong>Streaming</strong>: N/A.
    */
  object ServerDuration {

    val Name = "rpc.server.duration"
    val Description = "Measures the duration of inbound RPC."
    val Unit = "ms"

    def create[F[_]: Meter](
        boundaries: BucketBoundaries
    ): F[Histogram[F, Double]] =
      Meter[F]
        .histogram[Double](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

  /** Measures the size of RPC request messages (uncompressed). <p>
    * @note
    *   <p> <strong>Streaming</strong>: Recorded per message in a streaming batch
    */
  object ServerRequestSize {

    val Name = "rpc.server.request.size"
    val Description =
      "Measures the size of RPC request messages (uncompressed)."
    val Unit = "By"

    def create[F[_]: Meter](
        boundaries: BucketBoundaries
    ): F[Histogram[F, Double]] =
      Meter[F]
        .histogram[Double](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

  /** Measures the number of messages received per RPC. <p>
    * @note
    *   <p> Should be 1 for all non-streaming RPCs. <p> <strong>Streaming</strong> : This metric is required for server
    *   and client streaming RPCs
    */
  object ServerRequestsPerRpc {

    val Name = "rpc.server.requests_per_rpc"
    val Description = "Measures the number of messages received per RPC."
    val Unit = "{count}"

    def create[F[_]: Meter](
        boundaries: BucketBoundaries
    ): F[Histogram[F, Double]] =
      Meter[F]
        .histogram[Double](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

  /** Measures the size of RPC response messages (uncompressed). <p>
    * @note
    *   <p> <strong>Streaming</strong>: Recorded per response in a streaming batch
    */
  object ServerResponseSize {

    val Name = "rpc.server.response.size"
    val Description =
      "Measures the size of RPC response messages (uncompressed)."
    val Unit = "By"

    def create[F[_]: Meter](
        boundaries: BucketBoundaries
    ): F[Histogram[F, Double]] =
      Meter[F]
        .histogram[Double](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

  /** Measures the number of messages sent per RPC. <p>
    * @note
    *   <p> Should be 1 for all non-streaming RPCs. <p> <strong>Streaming</strong>: This metric is required for server
    *   and client streaming RPCs
    */
  object ServerResponsesPerRpc {

    val Name = "rpc.server.responses_per_rpc"
    val Description = "Measures the number of messages sent per RPC."
    val Unit = "{count}"

    def create[F[_]: Meter](
        boundaries: BucketBoundaries
    ): F[Histogram[F, Double]] =
      Meter[F]
        .histogram[Double](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

}
