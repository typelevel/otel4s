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

import cats.effect.Resource
import org.typelevel.otel4s.metrics._
import org.typelevel.otel4s.semconv.attributes._
import org.typelevel.otel4s.semconv.experimental.attributes._

// DO NOT EDIT, this is an Auto-generated file from buildscripts/templates/registry/otel4s/metrics/SemanticMetrics.scala.j2
object NfsExperimentalMetrics {

  val specs: List[MetricSpec] = List(
    ClientNetCount,
    ClientNetTcpConnectionAccepted,
    ClientOperationCount,
    ClientProcedureCount,
    ClientRpcAuthrefreshCount,
    ClientRpcCount,
    ClientRpcRetransmitCount,
    ServerFhStaleCount,
    ServerIo,
    ServerNetCount,
    ServerNetTcpConnectionAccepted,
    ServerOperationCount,
    ServerProcedureCount,
    ServerRepcacheRequests,
    ServerRpcCount,
    ServerThreadCount,
  )

  /** Reports the count of kernel NFS client TCP segments and UDP datagrams handled.
    *
    * @note
    *   <p> Linux: this metric is taken from the Linux kernel's svc_stat.netudpcnt and svc_stat.nettcpcnt
    */
  object ClientNetCount extends MetricSpec.Unsealed {

    val name: String = "nfs.client.net.count"
    val description: String = "Reports the count of kernel NFS client TCP segments and UDP datagrams handled."
    val unit: String = "{record}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** <a href="https://wikipedia.org/wiki/Transport_layer">OSI transport layer</a> or <a
        * href="https://wikipedia.org/wiki/Inter-process_communication">inter-process communication method</a>.
        *
        * @note
        *   <p> The value SHOULD be normalized to lowercase. <p> Consider always setting the transport when setting a
        *   port number, since a port number is ambiguous without knowing the transport. For example different processes
        *   could be listening on TCP port 12345 and UDP port 12345.
        */
      val networkTransport: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkTransport,
          List(
            "tcp",
            "udp",
          ),
          Requirement.recommended,
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          networkTransport,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Counter[F, A]] =
      Meter[F]
        .counter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableCounter] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Reports the count of kernel NFS client TCP connections accepted.
    *
    * @note
    *   <p> Linux: this metric is taken from the Linux kernel's svc_stat.nettcpconn
    */
  object ClientNetTcpConnectionAccepted extends MetricSpec.Unsealed {

    val name: String = "nfs.client.net.tcp.connection.accepted"
    val description: String = "Reports the count of kernel NFS client TCP connections accepted."
    val unit: String = "{connection}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = Nil

    def create[F[_]: Meter, A: MeasurementValue]: F[Counter[F, A]] =
      Meter[F]
        .counter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableCounter] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Reports the count of kernel NFSv4+ client operations.
    */
  object ClientOperationCount extends MetricSpec.Unsealed {

    val name: String = "nfs.client.operation.count"
    val description: String = "Reports the count of kernel NFSv4+ client operations."
    val unit: String = "{operation}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** NFSv4+ operation name.
        */
      val nfsOperationName: AttributeSpec[String] =
        AttributeSpec(
          NfsExperimentalAttributes.NfsOperationName,
          List(
            "OPEN",
            "READ",
            "GETATTR",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** ONC/Sun RPC program version.
        */
      val oncRpcVersion: AttributeSpec[Long] =
        AttributeSpec(
          OncRpcExperimentalAttributes.OncRpcVersion,
          List(
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          nfsOperationName,
          oncRpcVersion,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Counter[F, A]] =
      Meter[F]
        .counter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableCounter] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Reports the count of kernel NFS client procedures.
    */
  object ClientProcedureCount extends MetricSpec.Unsealed {

    val name: String = "nfs.client.procedure.count"
    val description: String = "Reports the count of kernel NFS client procedures."
    val unit: String = "{procedure}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** ONC/Sun RPC procedure name.
        */
      val oncRpcProcedureName: AttributeSpec[String] =
        AttributeSpec(
          OncRpcExperimentalAttributes.OncRpcProcedureName,
          List(
            "OPEN",
            "READ",
            "GETATTR",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** ONC/Sun RPC program version.
        */
      val oncRpcVersion: AttributeSpec[Long] =
        AttributeSpec(
          OncRpcExperimentalAttributes.OncRpcVersion,
          List(
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          oncRpcProcedureName,
          oncRpcVersion,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Counter[F, A]] =
      Meter[F]
        .counter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableCounter] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Reports the count of kernel NFS client RPC authentication refreshes.
    *
    * @note
    *   <p> Linux: this metric is taken from the Linux kernel's svc_stat.rpcauthrefresh
    */
  object ClientRpcAuthrefreshCount extends MetricSpec.Unsealed {

    val name: String = "nfs.client.rpc.authrefresh.count"
    val description: String = "Reports the count of kernel NFS client RPC authentication refreshes."
    val unit: String = "{authrefresh}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = Nil

    def create[F[_]: Meter, A: MeasurementValue]: F[Counter[F, A]] =
      Meter[F]
        .counter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableCounter] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Reports the count of kernel NFS client RPCs sent, regardless of whether they're accepted/rejected by the server.
    *
    * @note
    *   <p> Linux: this metric is taken from the Linux kernel's svc_stat.rpccnt
    */
  object ClientRpcCount extends MetricSpec.Unsealed {

    val name: String = "nfs.client.rpc.count"
    val description: String =
      "Reports the count of kernel NFS client RPCs sent, regardless of whether they're accepted/rejected by the server."
    val unit: String = "{request}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = Nil

    def create[F[_]: Meter, A: MeasurementValue]: F[Counter[F, A]] =
      Meter[F]
        .counter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableCounter] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Reports the count of kernel NFS client RPC retransmits.
    *
    * @note
    *   <p> Linux: this metric is taken from the Linux kernel's svc_stat.rpcretrans
    */
  object ClientRpcRetransmitCount extends MetricSpec.Unsealed {

    val name: String = "nfs.client.rpc.retransmit.count"
    val description: String = "Reports the count of kernel NFS client RPC retransmits."
    val unit: String = "{retransmit}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = Nil

    def create[F[_]: Meter, A: MeasurementValue]: F[Counter[F, A]] =
      Meter[F]
        .counter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableCounter] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Reports the count of kernel NFS server stale file handles.
    *
    * @note
    *   <p> Linux: this metric is taken from the Linux kernel NFSD_STATS_FH_STALE counter in the nfsd_net struct
    */
  object ServerFhStaleCount extends MetricSpec.Unsealed {

    val name: String = "nfs.server.fh.stale.count"
    val description: String = "Reports the count of kernel NFS server stale file handles."
    val unit: String = "{fh}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = Nil

    def create[F[_]: Meter, A: MeasurementValue]: F[Counter[F, A]] =
      Meter[F]
        .counter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableCounter] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Reports the count of kernel NFS server bytes returned to receive and transmit (read and write) requests.
    *
    * @note
    *   <p> Linux: this metric is taken from the Linux kernel NFSD_STATS_IO_READ and NFSD_STATS_IO_WRITE counters in the
    *   nfsd_net struct
    */
  object ServerIo extends MetricSpec.Unsealed {

    val name: String = "nfs.server.io"
    val description: String =
      "Reports the count of kernel NFS server bytes returned to receive and transmit (read and write) requests."
    val unit: String = "By"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The network IO operation direction.
        */
      val networkIoDirection: AttributeSpec[String] =
        AttributeSpec(
          NetworkExperimentalAttributes.NetworkIoDirection,
          List(
            "transmit",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          networkIoDirection,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Counter[F, A]] =
      Meter[F]
        .counter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableCounter] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Reports the count of kernel NFS server TCP segments and UDP datagrams handled.
    *
    * @note
    *   <p> Linux: this metric is taken from the Linux kernel's svc_stat.nettcpcnt and svc_stat.netudpcnt
    */
  object ServerNetCount extends MetricSpec.Unsealed {

    val name: String = "nfs.server.net.count"
    val description: String = "Reports the count of kernel NFS server TCP segments and UDP datagrams handled."
    val unit: String = "{record}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** <a href="https://wikipedia.org/wiki/Transport_layer">OSI transport layer</a> or <a
        * href="https://wikipedia.org/wiki/Inter-process_communication">inter-process communication method</a>.
        *
        * @note
        *   <p> The value SHOULD be normalized to lowercase. <p> Consider always setting the transport when setting a
        *   port number, since a port number is ambiguous without knowing the transport. For example different processes
        *   could be listening on TCP port 12345 and UDP port 12345.
        */
      val networkTransport: AttributeSpec[String] =
        AttributeSpec(
          NetworkAttributes.NetworkTransport,
          List(
            "tcp",
            "udp",
          ),
          Requirement.recommended,
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          networkTransport,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Counter[F, A]] =
      Meter[F]
        .counter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableCounter] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Reports the count of kernel NFS server TCP connections accepted.
    *
    * @note
    *   <p> Linux: this metric is taken from the Linux kernel's svc_stat.nettcpconn
    */
  object ServerNetTcpConnectionAccepted extends MetricSpec.Unsealed {

    val name: String = "nfs.server.net.tcp.connection.accepted"
    val description: String = "Reports the count of kernel NFS server TCP connections accepted."
    val unit: String = "{connection}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = Nil

    def create[F[_]: Meter, A: MeasurementValue]: F[Counter[F, A]] =
      Meter[F]
        .counter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableCounter] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Reports the count of kernel NFSv4+ server operations.
    */
  object ServerOperationCount extends MetricSpec.Unsealed {

    val name: String = "nfs.server.operation.count"
    val description: String = "Reports the count of kernel NFSv4+ server operations."
    val unit: String = "{operation}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** NFSv4+ operation name.
        */
      val nfsOperationName: AttributeSpec[String] =
        AttributeSpec(
          NfsExperimentalAttributes.NfsOperationName,
          List(
            "OPEN",
            "READ",
            "GETATTR",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** ONC/Sun RPC program version.
        */
      val oncRpcVersion: AttributeSpec[Long] =
        AttributeSpec(
          OncRpcExperimentalAttributes.OncRpcVersion,
          List(
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          nfsOperationName,
          oncRpcVersion,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Counter[F, A]] =
      Meter[F]
        .counter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableCounter] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Reports the count of kernel NFS server procedures.
    */
  object ServerProcedureCount extends MetricSpec.Unsealed {

    val name: String = "nfs.server.procedure.count"
    val description: String = "Reports the count of kernel NFS server procedures."
    val unit: String = "{procedure}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** ONC/Sun RPC procedure name.
        */
      val oncRpcProcedureName: AttributeSpec[String] =
        AttributeSpec(
          OncRpcExperimentalAttributes.OncRpcProcedureName,
          List(
            "OPEN",
            "READ",
            "GETATTR",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** ONC/Sun RPC program version.
        */
      val oncRpcVersion: AttributeSpec[Long] =
        AttributeSpec(
          OncRpcExperimentalAttributes.OncRpcVersion,
          List(
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          oncRpcProcedureName,
          oncRpcVersion,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Counter[F, A]] =
      Meter[F]
        .counter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableCounter] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Reports the kernel NFS server reply cache request count by cache hit status.
    */
  object ServerRepcacheRequests extends MetricSpec.Unsealed {

    val name: String = "nfs.server.repcache.requests"
    val description: String = "Reports the kernel NFS server reply cache request count by cache hit status."
    val unit: String = "{request}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Linux: one of "hit" (NFSD_STATS_RC_HITS), "miss" (NFSD_STATS_RC_MISSES), or "nocache" (NFSD_STATS_RC_NOCACHE
        * -- uncacheable)
        */
      val nfsServerRepcacheStatus: AttributeSpec[String] =
        AttributeSpec(
          NfsExperimentalAttributes.NfsServerRepcacheStatus,
          List(
            "h",
            "i",
            "t",
          ),
          Requirement.recommended,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          nfsServerRepcacheStatus,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Counter[F, A]] =
      Meter[F]
        .counter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableCounter] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Reports the count of kernel NFS server RPCs handled.
    *
    * @note
    *   <p> Linux: this metric is taken from the Linux kernel's svc_stat.rpccnt, the count of good RPCs. This metric can
    *   have an error.type of "format", "auth", or "client" for svc_stat.badfmt, svc_stat.badauth, and svc_stat.badclnt.
    */
  object ServerRpcCount extends MetricSpec.Unsealed {

    val name: String = "nfs.server.rpc.count"
    val description: String = "Reports the count of kernel NFS server RPCs handled."
    val unit: String = "{request}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Describes a class of error the operation ended with.
        *
        * @note
        *   <p> The `error.type` SHOULD be predictable, and SHOULD have low cardinality. <p> When `error.type` is set to
        *   a type (e.g., an exception type), its canonical class name identifying the type within the artifact SHOULD
        *   be used. <p> Instrumentations SHOULD document the list of errors they report. <p> The cardinality of
        *   `error.type` within one instrumentation library SHOULD be low. Telemetry consumers that aggregate data from
        *   multiple instrumentation libraries and applications should be prepared for `error.type` to have high
        *   cardinality at query time when no additional filters are applied. <p> If the operation has completed
        *   successfully, instrumentations SHOULD NOT set `error.type`. <p> If a specific domain defines its own set of
        *   error identifiers (such as HTTP or RPC status codes), it's RECOMMENDED to: <ul> <li>Use a domain-specific
        *   attribute <li>Set `error.type` to capture all errors, regardless of whether they are defined within the
        *   domain-specific set or not. </ul>
        */
      val errorType: AttributeSpec[String] =
        AttributeSpec(
          ErrorAttributes.ErrorType,
          List(
            "timeout",
            "java.net.UnknownHostException",
            "server_certificate_invalid",
            "500",
          ),
          Requirement.recommended,
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          errorType,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Counter[F, A]] =
      Meter[F]
        .counter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableCounter] =
      Meter[F]
        .observableCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Reports the count of kernel NFS server available threads.
    *
    * @note
    *   <p> Linux: this metric is taken from the Linux kernel nfsd_th_cnt variable
    */
  object ServerThreadCount extends MetricSpec.Unsealed {

    val name: String = "nfs.server.thread.count"
    val description: String = "Reports the count of kernel NFS server available threads."
    val unit: String = "{thread}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = Nil

    def create[F[_]: Meter, A: MeasurementValue]: F[UpDownCounter[F, A]] =
      Meter[F]
        .upDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableUpDownCounter] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

}
