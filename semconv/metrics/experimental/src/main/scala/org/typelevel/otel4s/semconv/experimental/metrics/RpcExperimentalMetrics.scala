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
package experimental
package metrics

import org.typelevel.otel4s.metrics._

// DO NOT EDIT, this is an Auto-generated file from buildscripts/templates/registry/otel4s/metrics/SemanticMetrics.scala.j2
object RpcExperimentalMetrics {

  val specs: List[MetricSpec] = List(
    ClientDuration,
    ClientRequestSize,
    ClientRequestsPerRpc,
    ClientResponseSize,
    ClientResponsesPerRpc,
    ServerDuration,
    ServerRequestSize,
    ServerRequestsPerRpc,
    ServerResponseSize,
    ServerResponsesPerRpc,
  )

  /** Measures the duration of outbound RPC.
    *
    * @note
    *   <p> While streaming RPCs may record this metric as start-of-batch to end-of-batch, it's hard to interpret in
    *   practice. <p> <strong>Streaming</strong>: N/A.
    */
  object ClientDuration extends MetricSpec.Unsealed {

    val name: String = "rpc.client.duration"
    val description: String = "Measures the duration of outbound RPC."
    val unit: String = "ms"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = Nil

    def create[F[_]: Meter, A: MeasurementValue](boundaries: BucketBoundaries): F[Histogram[F, A]] =
      Meter[F]
        .histogram[A](name)
        .withDescription(description)
        .withUnit(unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

  /** Measures the size of RPC request messages (uncompressed).
    *
    * @note
    *   <p> <strong>Streaming</strong>: Recorded per message in a streaming batch
    */
  object ClientRequestSize extends MetricSpec.Unsealed {

    val name: String = "rpc.client.request.size"
    val description: String = "Measures the size of RPC request messages (uncompressed)."
    val unit: String = "By"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = Nil

    def create[F[_]: Meter, A: MeasurementValue](boundaries: BucketBoundaries): F[Histogram[F, A]] =
      Meter[F]
        .histogram[A](name)
        .withDescription(description)
        .withUnit(unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

  /** Measures the number of messages received per RPC.
    *
    * @note
    *   <p> Should be 1 for all non-streaming RPCs. <p> <strong>Streaming</strong>: This metric is required for server
    *   and client streaming RPCs
    */
  object ClientRequestsPerRpc extends MetricSpec.Unsealed {

    val name: String = "rpc.client.requests_per_rpc"
    val description: String = "Measures the number of messages received per RPC."
    val unit: String = "{count}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = Nil

    def create[F[_]: Meter, A: MeasurementValue](boundaries: BucketBoundaries): F[Histogram[F, A]] =
      Meter[F]
        .histogram[A](name)
        .withDescription(description)
        .withUnit(unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

  /** Measures the size of RPC response messages (uncompressed).
    *
    * @note
    *   <p> <strong>Streaming</strong>: Recorded per response in a streaming batch
    */
  object ClientResponseSize extends MetricSpec.Unsealed {

    val name: String = "rpc.client.response.size"
    val description: String = "Measures the size of RPC response messages (uncompressed)."
    val unit: String = "By"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = Nil

    def create[F[_]: Meter, A: MeasurementValue](boundaries: BucketBoundaries): F[Histogram[F, A]] =
      Meter[F]
        .histogram[A](name)
        .withDescription(description)
        .withUnit(unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

  /** Measures the number of messages sent per RPC.
    *
    * @note
    *   <p> Should be 1 for all non-streaming RPCs. <p> <strong>Streaming</strong>: This metric is required for server
    *   and client streaming RPCs
    */
  object ClientResponsesPerRpc extends MetricSpec.Unsealed {

    val name: String = "rpc.client.responses_per_rpc"
    val description: String = "Measures the number of messages sent per RPC."
    val unit: String = "{count}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = Nil

    def create[F[_]: Meter, A: MeasurementValue](boundaries: BucketBoundaries): F[Histogram[F, A]] =
      Meter[F]
        .histogram[A](name)
        .withDescription(description)
        .withUnit(unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

  /** Measures the duration of inbound RPC.
    *
    * @note
    *   <p> While streaming RPCs may record this metric as start-of-batch to end-of-batch, it's hard to interpret in
    *   practice. <p> <strong>Streaming</strong>: N/A.
    */
  object ServerDuration extends MetricSpec.Unsealed {

    val name: String = "rpc.server.duration"
    val description: String = "Measures the duration of inbound RPC."
    val unit: String = "ms"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = Nil

    def create[F[_]: Meter, A: MeasurementValue](boundaries: BucketBoundaries): F[Histogram[F, A]] =
      Meter[F]
        .histogram[A](name)
        .withDescription(description)
        .withUnit(unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

  /** Measures the size of RPC request messages (uncompressed).
    *
    * @note
    *   <p> <strong>Streaming</strong>: Recorded per message in a streaming batch
    */
  object ServerRequestSize extends MetricSpec.Unsealed {

    val name: String = "rpc.server.request.size"
    val description: String = "Measures the size of RPC request messages (uncompressed)."
    val unit: String = "By"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = Nil

    def create[F[_]: Meter, A: MeasurementValue](boundaries: BucketBoundaries): F[Histogram[F, A]] =
      Meter[F]
        .histogram[A](name)
        .withDescription(description)
        .withUnit(unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

  /** Measures the number of messages received per RPC.
    *
    * @note
    *   <p> Should be 1 for all non-streaming RPCs. <p> <strong>Streaming</strong> : This metric is required for server
    *   and client streaming RPCs
    */
  object ServerRequestsPerRpc extends MetricSpec.Unsealed {

    val name: String = "rpc.server.requests_per_rpc"
    val description: String = "Measures the number of messages received per RPC."
    val unit: String = "{count}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = Nil

    def create[F[_]: Meter, A: MeasurementValue](boundaries: BucketBoundaries): F[Histogram[F, A]] =
      Meter[F]
        .histogram[A](name)
        .withDescription(description)
        .withUnit(unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

  /** Measures the size of RPC response messages (uncompressed).
    *
    * @note
    *   <p> <strong>Streaming</strong>: Recorded per response in a streaming batch
    */
  object ServerResponseSize extends MetricSpec.Unsealed {

    val name: String = "rpc.server.response.size"
    val description: String = "Measures the size of RPC response messages (uncompressed)."
    val unit: String = "By"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = Nil

    def create[F[_]: Meter, A: MeasurementValue](boundaries: BucketBoundaries): F[Histogram[F, A]] =
      Meter[F]
        .histogram[A](name)
        .withDescription(description)
        .withUnit(unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

  /** Measures the number of messages sent per RPC.
    *
    * @note
    *   <p> Should be 1 for all non-streaming RPCs. <p> <strong>Streaming</strong>: This metric is required for server
    *   and client streaming RPCs
    */
  object ServerResponsesPerRpc extends MetricSpec.Unsealed {

    val name: String = "rpc.server.responses_per_rpc"
    val description: String = "Measures the number of messages sent per RPC."
    val unit: String = "{count}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = Nil

    def create[F[_]: Meter, A: MeasurementValue](boundaries: BucketBoundaries): F[Histogram[F, A]] =
      Meter[F]
        .histogram[A](name)
        .withDescription(description)
        .withUnit(unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

}
