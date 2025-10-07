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
import fs2.io.file.Files
import fs2.io.file.Path
import munit.CatsEffectSuite

class DockerHelperSuite extends CatsEffectSuite {

  test("DockerHelper should extract container ID from cgroup file") {
    val expectedContainerId = "386a1920640799b5bf5a39bd94e489e5159a88677d96ca822ce7c433ff350163"
    val cgroupContent = s"dummy\n11:devices:/ecs/bbc36dd0-5ee0-4007-ba96-c590e0b278d2/$expectedContainerId"

    withTestFile(cgroupContent) { cgroupPath =>
      val dockerHelper = DockerHelper[IO](cgroupPath.toString)
      dockerHelper.getContainerId.assertEquals(Some(expectedContainerId))
    }
  }

  test("DockerHelper should return None when cgroup file is missing") {
    val dockerHelper = DockerHelper[IO]("non-existent-file")
    dockerHelper.getContainerId.assertEquals(None)
  }

  test("DockerHelper should return None when no line is longer than 64 characters") {
    val cgroupContent = "13:pids:/\n12:hugetlb:/\n11:net_prio:/"

    withTestFile(cgroupContent) { cgroupPath =>
      val dockerHelper = DockerHelper[IO](cgroupPath.toString)
      dockerHelper.getContainerId.assertEquals(None)
    }
  }

  test("DockerHelper should extract container ID from EKS-style cgroup") {
    val expectedContainerId = "34567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef12"
    val cgroupContent = s"""12:cpuset:/kubepods/burstable/pod12345678-1234-1234-1234-123456789012/$expectedContainerId
11:devices:/kubepods/burstable/pod12345678-1234-1234-1234-123456789012/$expectedContainerId
10:memory:/kubepods/burstable/pod12345678-1234-1234-1234-123456789012/$expectedContainerId"""

    withTestFile(cgroupContent) { cgroupPath =>
      val dockerHelper = DockerHelper[IO](cgroupPath.toString)
      dockerHelper.getContainerId.assertEquals(Some(expectedContainerId))
    }
  }

  test("DockerHelper should extract container ID from Docker-style cgroup") {
    val expectedContainerId = "386a1920640799b5bf5a39bd94e489e5159a88677d96ca822ce7c433ff350163"
    val cgroupContent = s"""12:cpuset:/docker/$expectedContainerId
11:devices:/docker/$expectedContainerId
10:memory:/docker/$expectedContainerId"""

    withTestFile(cgroupContent) { cgroupPath =>
      val dockerHelper = DockerHelper[IO](cgroupPath.toString)
      dockerHelper.getContainerId.assertEquals(Some(expectedContainerId))
    }
  }

  test("DockerHelper should handle empty cgroup file") {
    withTestFile("") { cgroupPath =>
      val dockerHelper = DockerHelper[IO](cgroupPath.toString)
      dockerHelper.getContainerId.assertEquals(None)
    }
  }

  test("DockerHelper should handle malformed cgroup content") {
    val malformedContent = "invalid:cgroup:content:without:proper:format"

    withTestFile(malformedContent) { cgroupPath =>
      val dockerHelper = DockerHelper[IO](cgroupPath.toString)
      dockerHelper.getContainerId.assertEquals(None)
    }
  }

  private def withTestFile(content: String)(test: Path => IO[Unit]): IO[Unit] = {
    Files[IO].tempFile.use { path =>
      for {
        _ <- if (content.nonEmpty) write(content, path) else IO.unit
        _ <- test(path)
      } yield ()
    }
  }

  private def write(content: String, path: Path): IO[Unit] =
    fs2.Stream(content).through(fs2.text.utf8.encode).through(Files[IO].writeAll(path)).compile.drain
}
