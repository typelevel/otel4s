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

package org.typelevel.otel4s.sdk.resource

import cats.effect.Sync
import cats.effect.std.SystemProperties
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.semconv.SchemaUrls

import java.io.File
import java.lang.management.ManagementFactory
import java.lang.management.RuntimeMXBean
import java.util.Locale
import java.util.regex.Pattern

private[resource] trait ProcessDetectorPlatform { self: ProcessDetector.type =>

  private val JarFilePattern =
    Pattern.compile("^\\S+\\.(jar|war)", Pattern.CASE_INSENSITIVE)

  def apply[F[_]: Sync: SystemProperties]: TelemetryResourceDetector[F] =
    new Detector[F]

  private class Detector[F[_]: Sync: SystemProperties] extends TelemetryResourceDetector[F] {
    def name: String = Const.Name

    def detect: F[Option[TelemetryResource]] =
      for {
        runtime <- Sync[F].delay(ManagementFactory.getRuntimeMXBean)
        javaHomeOpt <- SystemProperties[F].get("java.home")
        osNameOpt <- SystemProperties[F].get("os.name")
        javaCommandOpt <- SystemProperties[F].get("sun.java.command")
      } yield {
        val builder = Attributes.newBuilder

        builder.addAll(Keys.Pid.maybe(getPid(runtime)))

        javaHomeOpt.foreach { javaHome =>
          val exePath = executablePath(javaHome, osNameOpt)
          val cmdLine = exePath + commandLineArgs(runtime, javaCommandOpt)

          builder.addOne(Keys.ExecutablePath(exePath))
          builder.addOne(Keys.CommandLine(cmdLine))
        }

        Some(TelemetryResource(builder.result(), Some(SchemaUrls.Current)))
      }

    private def getPid(runtime: RuntimeMXBean): Option[Long] =
      runtime.getName.split("@").headOption.flatMap(_.toLongOption)

    private def executablePath(
        javaHome: String,
        osName: Option[String]
    ): String = {
      val executablePath = new StringBuilder(javaHome)
      executablePath
        .append(File.separatorChar)
        .append("bin")
        .append(File.separatorChar)
        .append("java")

      if (osName.exists(_.toLowerCase(Locale.ROOT).startsWith("windows")))
        executablePath.append(".exe")

      executablePath.result()
    }

    private def commandLineArgs(
        runtime: RuntimeMXBean,
        javaCommandOpt: Option[String]
    ): String = {
      val commandLine = new StringBuilder()

      // VM args: -Dfile.encoding=UTF-8, -Xms2000m, -Xmx2000m, etc
      runtime.getInputArguments.forEach { arg =>
        commandLine.append(' ').append(arg)
        ()
      }

      // general args, e.g.: org.ClassName param1 param2
      javaCommandOpt.foreach { javaCommand =>
        // '-jar' is missing when launching a jar directly, add it if needed
        if (JarFilePattern.matcher(javaCommand).matches())
          commandLine.append(" -jar")

        commandLine.append(' ').append(javaCommand)
        ()
      }

      commandLine.result()
    }
  }

}
