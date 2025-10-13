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

package org.typelevel.otel4s.sdk.contrib.aws.resource

import cats.effect.Async
import cats.effect.Resource
import cats.effect.std.Console
import cats.effect.std.Env
import cats.syntax.applicativeError._
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import fs2.io.file.Files
import fs2.io.file.Path
import fs2.io.net.Network
import io.circe.Json
import org.http4s.EntityDecoder
import org.http4s.Header
import org.http4s.Headers
import org.http4s.Method
import org.http4s.Request
import org.http4s.Uri
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import org.typelevel.ci._
import org.typelevel.otel4s.AttributeKey
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.resource.TelemetryResourceDetector
import org.typelevel.otel4s.semconv.SchemaUrls

import scala.concurrent.duration._

private class AwsEksDetector[F[_]: Async: Files: Console: Network: Env] private (
    customClient: Option[Client[F]]
) extends TelemetryResourceDetector.Unsealed[F] {

  import AwsEksDetector.Const
  import AwsEksDetector.Keys

  private implicit val jsonDecoder: EntityDecoder[F, Json] = accumulatingJsonOf

  def name: String = Const.Name

  def detect: F[Option[TelemetryResource]] =
    mkClient.use { client =>
      isEks(client)
        .flatMap {
          case true  => buildResource(client).map(Option(_))
          case false => Async[F].pure(Option.empty[TelemetryResource])
        }
        .handleErrorWith { e =>
          Console[F]
            .errorln(s"AwsEksDetector: cannot retrieve EKS metadata due to ${e.getMessage}")
            .as(Option.empty[TelemetryResource])
        }
    }

  private def buildResource(client: Client[F]): F[TelemetryResource] =
    getCgroupPath.flatMap { cgroupPath =>
      val dockerHelper = DockerHelper[F](cgroupPath.toString)
      (getClusterName(client), dockerHelper.getContainerId).mapN(build)
    }

  private def isEks(client: Client[F]): F[Boolean] =
    isK8s.flatMap {
      case true  => checkAwsAuth(client)
      case false => Async[F].pure(false)
    }

  private def isK8s: F[Boolean] =
    (getK8sTokenPath, getK8sCertPath).flatMapN { (tokenPath, certPath) =>
      (Files[F].exists(tokenPath), Files[F].exists(certPath)).mapN(_ && _)
    }

  private def getK8sTokenPath: F[Path] =
    Env[F].get(Const.K8sTokenPathEnv).map(_.getOrElse(Const.DefaultK8sTokenPath)).map(Path(_))

  private def getK8sCertPath: F[Path] =
    Env[F].get(Const.K8sCertPathEnv).map(_.getOrElse(Const.DefaultK8sCertPath)).map(Path(_))

  private def getCgroupPath: F[Path] =
    Env[F].get(Const.CgroupPathEnv).map(_.getOrElse(Const.DefaultCgroupPath)).map(Path(_))

  private def checkAwsAuth(client: Client[F]): F[Boolean] =
    readK8sToken.flatMap { token =>
      val request = Request[F](
        Method.GET,
        uri = Uri.unsafeFromString(Const.K8sServiceUrl + Const.AuthConfigMapPath),
        headers = Headers(Header.Raw(ci"Authorization", token))
      )

      client
        .expect[Json](request)
        .as(true)
        .handleErrorWith(_ => Async[F].pure(false))
    }

  private def build(clusterName: Option[String], containerId: Option[String]): TelemetryResource = {
    val builder = Attributes.newBuilder

    builder.addOne(Keys.CloudProvider, Const.CloudProvider)
    builder.addOne(Keys.CloudPlatform, Const.CloudPlatform)

    clusterName.foreach(name => builder.addOne(Keys.K8sClusterName, name))
    containerId.foreach(id => builder.addOne(Keys.ContainerId, id))

    TelemetryResource(builder.result(), Some(SchemaUrls.Current))
  }

  private def getClusterName(client: Client[F]): F[Option[String]] =
    readK8sToken
      .flatMap(token => fetchClusterInfo(client, token))
      .handleErrorWith(_ => Async[F].pure(None))

  private def readK8sToken: F[String] =
    getK8sTokenPath.flatMap { tokenPath =>
      Files[F]
        .readUtf8(tokenPath)
        .compile
        .string
        .map(token => s"Bearer $token")
    }

  private def fetchClusterInfo(client: Client[F], token: String): F[Option[String]] = {
    val request = Request[F](
      Method.GET,
      uri = Uri.unsafeFromString(Const.K8sServiceUrl + Const.CloudWatchConfigMapPath),
      headers = Headers(Header.Raw(ci"Authorization", token))
    )

    client
      .expect[Json](request)
      .map { json =>
        json.hcursor
          .downField("data")
          .downField("cluster.name")
          .as[String]
          .toOption
      }
  }

  private def mkClient: Resource[F, Client[F]] =
    customClient match {
      case Some(client) => Resource.pure(client)
      case None         => EmberClientBuilder.default[F].withTimeout(Const.Timeout).build
    }
}

object AwsEksDetector {

  private[resource] object Const {
    val Name = "aws-eks"
    val CloudProvider = "aws"
    val CloudPlatform = "aws_eks"
    val K8sServiceUrl = "https://kubernetes.default.svc"
    val AuthConfigMapPath = "/api/v1/namespaces/kube-system/configmaps/aws-auth"
    val CloudWatchConfigMapPath = "/api/v1/namespaces/amazon-cloudwatch/configmaps/cluster-info"
    val K8sTokenPathEnv = "OTEL_K8S_TOKEN_PATH"
    val K8sCertPathEnv = "OTEL_K8S_CERT_PATH"
    val CgroupPathEnv = "OTEL_CGROUP_PATH"
    val DefaultK8sTokenPath = "/var/run/secrets/kubernetes.io/serviceaccount/token"
    val DefaultK8sCertPath = "/var/run/secrets/kubernetes.io/serviceaccount/ca.crt"
    val DefaultCgroupPath = "/proc/self/cgroup"
    val Timeout: FiniteDuration = 2.seconds
  }

  private object Keys {
    val CloudProvider: AttributeKey[String] = AttributeKey("cloud.provider")
    val CloudPlatform: AttributeKey[String] = AttributeKey("cloud.platform")
    val K8sClusterName: AttributeKey[String] = AttributeKey("k8s.cluster.name")
    val ContainerId: AttributeKey[String] = AttributeKey("container.id")
  }

  /** The detector detects if running on AWS EKS and provides EKS-specific resource attributes.
    *
    * The detector checks for Kubernetes service account files and AWS-specific configmaps to determine if running on
    * EKS. It extracts cluster name and container ID information.
    *
    * @example
    *   {{{
    * OpenTelemetrySdk
    *   .autoConfigured[IO](
    *     // register OTLP exporters configurer
    *     _.addExportersConfigurer(OtlpExportersAutoConfigure[IO])
    *     // register AWS EKS detector
    *      .addResourceDetector(AwsEksDetector[IO])
    *   )
    *   .use { autoConfigured =>
    *     val sdk = autoConfigured.sdk
    *     ???
    *   }
    *   }}}
    *
    * @see
    *   [[https://docs.aws.amazon.com/eks/latest/userguide/kubernetes-versions.html]]
    */
  def apply[F[_]: Async: Files: Console: Network: Env]: TelemetryResourceDetector[F] =
    new AwsEksDetector[F](None)

  /** The detector detects if running on AWS EKS using the given `client`.
    *
    * The detector checks for Kubernetes service account files and AWS-specific configmaps to determine if running on
    * EKS. It extracts cluster name and container ID information.
    *
    * @example
    *   {{{
    * OpenTelemetrySdk
    *   .autoConfigured[IO](
    *     // register OTLP exporters configurer
    *     _.addExportersConfigurer(OtlpExportersAutoConfigure[IO])
    *     // register AWS EKS detector
    *      .addResourceDetector(AwsEksDetector[IO])
    *   )
    *   .use { autoConfigured =>
    *     val sdk = autoConfigured.sdk
    *     ???
    *   }
    *   }}}
    *
    * @see
    *   [[https://docs.aws.amazon.com/eks/latest/userguide/kubernetes-versions.html]]
    */
  def apply[F[_]: Async: Files: Console: Network: Env](client: Client[F]): TelemetryResourceDetector[F] =
    new AwsEksDetector[F](Some(client))

  // Visible for testing
  private[resource] def apply[F[_]: Async: Files: Console: Network: Env](
      client: Option[Client[F]]
  ): TelemetryResourceDetector[F] =
    new AwsEksDetector[F](client)
}
