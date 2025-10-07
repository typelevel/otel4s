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

import cats.effect.IO
import cats.effect.std.Env
import fs2.io.file.Files
import fs2.io.file.Path
import io.circe.Json
import munit.CatsEffectSuite
import org.http4s.HttpApp
import org.http4s.HttpRoutes
import org.http4s.client.Client
import org.http4s.dsl.io._
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.semconv.SchemaUrls
import org.typelevel.otel4s.semconv.experimental.attributes.CloudExperimentalAttributes._
import org.typelevel.otel4s.semconv.experimental.attributes.ContainerExperimentalAttributes._
import org.typelevel.otel4s.semconv.experimental.attributes.K8sExperimentalAttributes._

import scala.collection.immutable

import AwsEksDetector.Const

class AwsEksDetectorSuite extends CatsEffectSuite {

  private val TestClusterName = "my-eks-cluster"
  private val TestContainerId = "34567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef12"
  private val TestToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.test-token"
  private val TestCgroupContent =
    """12:cpuset:/kubepods/burstable/pod12345678-1234-1234-1234-123456789012/34567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef12
11:devices:/kubepods/burstable/pod12345678-1234-1234-1234-123456789012/34567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef12
10:memory:/kubepods/burstable/pod12345678-1234-1234-1234-123456789012/34567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef12"""

  test("detect EKS environment and add all attributes") {
    val configMapResponse = createConfigMapResponse(TestClusterName)
    val client = Client.fromHttpApp(mockServer(configMapResponse))
    val expected = TelemetryResource(
      Attributes(
        CloudProvider(CloudProviderValue.Aws.value),
        CloudPlatform(CloudPlatformValue.AwsEks.value),
        K8sClusterName(TestClusterName),
        ContainerId(TestContainerId)
      ),
      Some(SchemaUrls.Current)
    )

    withTestFiles(TestToken, TestCgroupContent) { (tokenPath, cgroupPath) =>
      implicit val env: Env[IO] = createTestEnv(tokenPath, cgroupPath)
      val detector = AwsEksDetector[IO](client)
      detector.detect.assertEquals(Some(expected))
    }
  }

  test("detect EKS environment with only cluster name") {
    val configMapResponse = createConfigMapResponse(TestClusterName)
    val client = Client.fromHttpApp(mockServer(configMapResponse))
    val expected = TelemetryResource(
      Attributes(
        CloudProvider(CloudProviderValue.Aws.value),
        CloudPlatform(CloudPlatformValue.AwsEks.value),
        K8sClusterName(TestClusterName)
      ),
      Some(SchemaUrls.Current)
    )

    withTestFiles(TestToken, "") { (tokenPath, _) =>
      implicit val env: Env[IO] = createTestEnv(tokenPath, Path("/nonexistent/cgroup"))
      val detector = AwsEksDetector[IO](client)
      detector.detect.assertEquals(Some(expected))
    }
  }

  test("detect EKS environment with only container ID") {
    val client = Client.fromHttpApp(HttpApp.notFound[IO])

    withTestFiles("", TestCgroupContent) { (_, cgroupPath) =>
      implicit val env: Env[IO] = createTestEnv(Path("/nonexistent/token"), cgroupPath)
      val detector = AwsEksDetector[IO](client)
      detector.detect.assertEquals(None)
    }
  }

  test("detect EKS environment with only cloud attributes") {
    val client = Client.fromHttpApp(HttpApp.notFound[IO])
    implicit val env: Env[IO] = createTestEnv(Path("/nonexistent/token"), Path("/nonexistent/cgroup"))
    val detector = AwsEksDetector[IO](client)
    detector.detect.assertEquals(None)
  }

  test("handle malformed cgroup content gracefully") {
    val configMapResponse = createConfigMapResponse(TestClusterName)
    val client = Client.fromHttpApp(mockServer(configMapResponse))
    val malformedCgroupContent = """12:cpuset:/
11:devices:/
10:memory:/"""
    val expected = TelemetryResource(
      Attributes(
        CloudProvider(CloudProviderValue.Aws.value),
        CloudPlatform(CloudPlatformValue.AwsEks.value),
        K8sClusterName(TestClusterName)
      ),
      Some(SchemaUrls.Current)
    )

    withTestFiles(TestToken, malformedCgroupContent) { (tokenPath, cgroupPath) =>
      implicit val env: Env[IO] = createTestEnv(tokenPath, cgroupPath)
      val detector = AwsEksDetector[IO](client)
      detector.detect.assertEquals(Some(expected))
    }
  }

  test("handle configmap with empty cluster.name") {
    val configMapResponse = createConfigMapResponse("")
    val client = Client.fromHttpApp(mockServer(configMapResponse))
    val expected = TelemetryResource(
      Attributes(
        CloudProvider(CloudProviderValue.Aws.value),
        CloudPlatform(CloudPlatformValue.AwsEks.value),
        K8sClusterName(""),
        ContainerId(TestContainerId)
      ),
      Some(SchemaUrls.Current)
    )

    withTestFiles(TestToken, TestCgroupContent) { (tokenPath, cgroupPath) =>
      implicit val env: Env[IO] = createTestEnv(tokenPath, cgroupPath)
      val detector = AwsEksDetector[IO](client)
      detector.detect.assertEquals(Some(expected))
    }
  }

  test("handle configmap with missing or null cluster.name field") {
    val configMapResponse = Json.obj(
      "data" -> Json.obj(
        "other.field" -> Json.fromString("some-value")
      )
    )
    val client = Client.fromHttpApp(mockServer(configMapResponse))
    val expected = TelemetryResource(
      Attributes(
        CloudProvider(CloudProviderValue.Aws.value),
        CloudPlatform(CloudPlatformValue.AwsEks.value),
        ContainerId(TestContainerId)
      ),
      Some(SchemaUrls.Current)
    )

    withTestFiles(TestToken, TestCgroupContent) { (tokenPath, cgroupPath) =>
      implicit val env: Env[IO] = createTestEnv(tokenPath, cgroupPath)
      val detector = AwsEksDetector[IO](client)
      detector.detect.assertEquals(Some(expected))
    }
  }

  test("detector should have correct name") {
    implicit val env: Env[IO] = createTestEnv(Path("/nonexistent/token"), Path("/nonexistent/cgroup"))
    val detector = AwsEksDetector[IO]
    assertEquals(detector.name, "aws-eks")
  }

  test("return None when not running on Kubernetes (missing token file)") {
    val client = Client.fromHttpApp(HttpApp.notFound[IO])
    implicit val env: Env[IO] = createTestEnv(Path("/nonexistent/token"), Path("/nonexistent/cgroup"))
    val detector = AwsEksDetector[IO](client)
    detector.detect.assertEquals(None)
  }

  test("return None when not running on Kubernetes (missing cert file)") {
    val client = Client.fromHttpApp(HttpApp.notFound[IO])
    implicit val env: Env[IO] = createTestEnv(Path("/nonexistent/token"), Path("/nonexistent/cgroup"))
    val detector = AwsEksDetector[IO](client)
    detector.detect.assertEquals(None)
  }

  test("return None when running on Kubernetes but not EKS (aws-auth ConfigMap not found)") {
    val client = Client.fromHttpApp(HttpApp.notFound[IO])

    withTestFiles(TestToken, TestCgroupContent) { (tokenPath, cgroupPath) =>
      implicit val env: Env[IO] = createTestEnv(tokenPath, cgroupPath)
      val detector = AwsEksDetector[IO](client)
      detector.detect.assertEquals(None)
    }
  }

  test("return None when running on Kubernetes but not EKS (aws-auth ConfigMap returns error)") {
    val client = Client.fromHttpApp(HttpApp.notFound[IO])

    withTestFiles(TestToken, TestCgroupContent) { (tokenPath, cgroupPath) =>
      implicit val env: Env[IO] = createTestEnv(tokenPath, cgroupPath)
      val detector = AwsEksDetector[IO](client)
      detector.detect.assertEquals(None)
    }
  }

  private def createConfigMapResponse(clusterName: String): Json = Json.obj(
    "data" -> Json.obj(
      "cluster.name" -> Json.fromString(clusterName)
    )
  )

  private def withTestFiles(tokenContent: String, cgroupContent: String)(
      test: (Path, Path) => IO[Unit]
  ): IO[Unit] = {
    Files[IO].tempFile.use { tokenPath =>
      Files[IO].tempFile.use { cgroupPath =>
        for {
          _ <- if (tokenContent.nonEmpty) write(tokenContent, tokenPath) else IO.unit
          _ <- if (cgroupContent.nonEmpty) write(cgroupContent, cgroupPath) else IO.unit
          _ <- test(tokenPath, cgroupPath)
        } yield ()
      }
    }
  }

  private def write(content: String, path: Path): IO[Unit] =
    fs2.Stream(content).through(fs2.text.utf8.encode).through(Files[IO].writeAll(path)).compile.drain

  private def createTestEnv(tokenPath: Path, cgroupPath: Path): Env[IO] =
    new Env[IO] {
      private val params = Map(
        Const.K8sTokenPathEnv -> tokenPath.toString,
        Const.K8sCertPathEnv -> tokenPath.toString, // Use same path for cert for testing
        Const.CgroupPathEnv -> cgroupPath.toString
      )

      def get(name: String): IO[Option[String]] =
        IO.pure(params.get(name))

      def entries: IO[immutable.Iterable[(String, String)]] =
        IO.pure(params)
    }

  private def mockServer(configMapResponse: Json): HttpApp[IO] = {
    import org.http4s.circe.jsonEncoder

    HttpRoutes
      .of[IO] {
        case GET -> Root / "api" / "v1" / "namespaces" / "amazon-cloudwatch" / "configmaps" / "cluster-info" =>
          Ok(configMapResponse)
        case GET -> Root / "api" / "v1" / "namespaces" / "kube-system" / "configmaps" / "aws-auth" =>
          Ok(Json.obj("data" -> Json.obj("mapRoles" -> Json.arr())))
      }
      .orNotFound
  }
}
