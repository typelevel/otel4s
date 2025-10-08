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
import cats.syntax.applicativeError._
import cats.syntax.functor._
import fs2.io.file.Files
import fs2.io.file.Path

/** Helper class for extracting Docker container ID from cgroup files.
  *
  * This follows the same pattern as the Java DockerHelper implementation. It extracts container ID by taking the last
  * 64 characters from any line longer than 64 characters in the cgroup file.
  */
class DockerHelper[F[_]: Async: Files] private (cgroupPath: String) {

  private val ContainerIdLength = 64

  def getContainerId: F[Option[String]] =
    Files[F]
      .readUtf8(Path(cgroupPath))
      .compile
      .string
      .map(parseContainerId)
      .handleErrorWith(_ => Async[F].pure(None))

  private def parseContainerId(cgroupContent: String): Option[String] = {
    val lines = cgroupContent.split("\n")
    lines
      .find(_.length > ContainerIdLength)
      .map(_.takeRight(ContainerIdLength))
  }
}

object DockerHelper {

  private val DefaultCgroupPath = "/proc/self/cgroup"

  def apply[F[_]: Async: Files]: DockerHelper[F] =
    new DockerHelper[F](DefaultCgroupPath)

  def apply[F[_]: Async: Files](cgroupPath: String): DockerHelper[F] =
    new DockerHelper[F](cgroupPath)
}
