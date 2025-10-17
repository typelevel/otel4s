/*
 * Copyright 2023 Typelevel
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

package org.typelevel.otel4s.sdk.exporter.otlp

import cats.effect.Async
import cats.effect.Temporal
import cats.effect.std.Console
import cats.effect.syntax.all._
import cats.syntax.all._
import com.comcast.ip4s._
import com.comcast.ip4s.Port
import fs2.io.net.Network
import fs2.io.process.ProcessBuilder
import fs2.io.process.Processes
import org.http4s.ember.client.EmberClientBuilder

import scala.concurrent.duration._

object DockerUtils {

  final case class CollectorPortMappings(
      health: Port,
      otlpGrpc: Port,
      otlpHttp: Port
  )

  def getCollectorPortMappings[F[_]: Async: Console: Network: Processes](
      container: String
  ): F[CollectorPortMappings] = {
    val getPortMappings =
      for {
        health <- getPortMapping[F](container, port"13133")
        otlpGrpc <- getPortMapping[F](container, port"4317")
        otlpHttp <- getPortMapping[F](container, port"4318")
      } yield CollectorPortMappings(health, otlpGrpc, otlpHttp)

    val portMappings: F[CollectorPortMappings] = retry(20, 500.millis)(getPortMappings) {
      case Right(_)    => Right(())
      case Left(error) => Left(s"Cannot get collector ports: $error")
    }

    def awaitHealthy(collector: CollectorPortMappings): F[Unit] =
      EmberClientBuilder.default[F].build.use { client =>
        val url = s"http://localhost:${collector.health}/health"

        retry(20, 500.millis)(client.expect[String](url)) {
          case Right(content) =>
            Either.cond(content.contains("Server available"), (), s"Collector is not healthy: $content")

          case Left(e) =>
            Left(s"Collector is not health: ${e.getMessage}")
        }.void
      }

    for {
      mappings <- portMappings
      _ <- awaitHealthy(mappings)
    } yield mappings
  }

  def getPortMapping[F[_]: Async: Processes](container: String, port: Port): F[Port] = {
    val format = s"{{ (index (index .NetworkSettings.Ports \"$port/tcp\") 0).HostPort }}"

    def tryParsePort(text: String): F[Port] =
      Async[F].fromOption(
        Port.fromString(text.trim),
        new RuntimeException(s"Cannot parse [$text] as port")
      )

    def failure(exitCode: Int, stdout: String, stderr: String): F[Port] =
      Async[F].raiseError(
        new RuntimeException(
          s"Cannot retrieve port number. Exit code [$exitCode]. stdout: $stdout. stderr: $stderr"
        )
      )

    ProcessBuilder("docker", "inspect", "-f", format, container).spawn[F].use { process =>
      val stdoutF = process.stdout.through(fs2.text.utf8.decode).compile.string
      val stderrF = process.stderr.through(fs2.text.utf8.decode).compile.string
      val exitCodeF = process.exitValue

      stdoutF.both(stderrF).both(exitCodeF).flatMap { case ((stdout, stderr), exitCode) =>
        if (exitCode == 0) tryParsePort(stdout) else failure(exitCode, stdout, stderr)
      }
    }
  }

  private def retry[F[_]: Temporal: Console, A](
      maxAttempts: Int,
      delay: FiniteDuration
  )(fa: F[A])(checkResult: Either[Throwable, A] => Either[String, Unit]): F[A] = {
    def loop(attempt: Int): F[A] = {
      val step = s"${attempt + 1}/$maxAttempts"

      def retry(message: String, cause: Option[Throwable]): F[A] =
        if (attempt < maxAttempts)
          Console[F].errorln(s"[$step] $message") *> loop(attempt + 1).delayBy(delay)
        else
          Temporal[F].raiseError(new RuntimeException(s"[$step] $message", cause.orNull))

      fa.attempt.flatMap {
        case Right(result) =>
          checkResult(Right(result)).fold(violation => retry(violation, None), _ => Temporal[F].pure(result))

        case Left(e) =>
          checkResult(Left(e)).fold(violation => retry(violation, Some(e)), _ => Temporal[F].raiseError(e))
      }
    }

    loop(0)
  }

}
