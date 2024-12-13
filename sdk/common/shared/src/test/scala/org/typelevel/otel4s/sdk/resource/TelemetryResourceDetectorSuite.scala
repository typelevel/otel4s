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

import cats.effect.IO
import munit.CatsEffectSuite
import munit.internal.PlatformCompat
import org.typelevel.otel4s.AttributeKey
import org.typelevel.otel4s.semconv.SchemaUrls
import org.typelevel.otel4s.semconv.experimental.attributes.HostExperimentalAttributes._
import org.typelevel.otel4s.semconv.experimental.attributes.OsExperimentalAttributes._
import org.typelevel.otel4s.semconv.experimental.attributes.ProcessExperimentalAttributes._

class TelemetryResourceDetectorSuite extends CatsEffectSuite {

  test("HostDetector - detect name and arch") {
    val keys: Set[AttributeKey[_]] = Set(HostName, HostArch)

    for {
      resource <- HostDetector[IO].detect
    } yield {
      assertEquals(resource.map(_.attributes.map(_.key).toSet), Some(keys))
      assertEquals(resource.flatMap(_.schemaUrl), Some(SchemaUrls.Current))
    }
  }

  test("OSDetector - detect OS type and description") {
    val keys: Set[AttributeKey[_]] = Set(OsType, OsDescription)

    for {
      resource <- OSDetector[IO].detect
    } yield {
      assertEquals(resource.map(_.attributes.map(_.key).toSet), Some(keys))
      assertEquals(resource.flatMap(_.schemaUrl), Some(SchemaUrls.Current))
    }
  }

  test("ProcessDetector - detect pid, exe path, exe name") {
    val keys: Set[AttributeKey[_]] =
      if (PlatformCompat.isJS)
        Set(
          ProcessExecutablePath,
          ProcessExecutableName,
          ProcessPid,
          ProcessOwner,
          ProcessCommandArgs
        )
      else if (PlatformCompat.isNative)
        Set(ProcessPid)
      else
        Set(
          ProcessExecutablePath,
          ProcessPid,
          ProcessCommandLine
        )

    for {
      resource <- ProcessDetector[IO].detect
    } yield {
      assertEquals(resource.map(_.attributes.map(_.key).toSet), Some(keys))
      assertEquals(resource.flatMap(_.schemaUrl), Some(SchemaUrls.Current))
    }
  }

  test("ProcessRuntimeDetector - detect name, version, and description") {
    val keys: Set[AttributeKey[_]] =
      if (PlatformCompat.isNative)
        Set(ProcessRuntimeName, ProcessRuntimeDescription)
      else
        Set(
          ProcessRuntimeName,
          ProcessRuntimeVersion,
          ProcessRuntimeDescription
        )

    for {
      resource <- ProcessRuntimeDetector[IO].detect
    } yield {
      assertEquals(resource.map(_.attributes.map(_.key).toSet), Some(keys))
      assertEquals(resource.flatMap(_.schemaUrl), Some(SchemaUrls.Current))
    }
  }

  test("default - contain host, os, process, process_runtime detectors") {
    val detectors = TelemetryResourceDetector.default[IO].map(_.name)
    val expected = Set("host", "os", "process", "process_runtime")

    assertEquals(detectors.map(_.name), expected)
  }

}
