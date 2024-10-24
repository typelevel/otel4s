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

package org.typelevel.otel4s.sdk.exporter.prometheus

import cats.effect.IO
import munit.CatsEffectSuite
import org.http4s.Headers
import org.http4s.HttpRoutes
import org.http4s.MediaRange
import org.http4s.MediaType
import org.http4s.Method
import org.http4s.Request
import org.http4s.Status
import org.http4s.headers.Accept
import org.http4s.implicits._
import org.typelevel.otel4s.sdk.exporter.SuiteRuntimePlatform

class PrometheusHttpRoutesSuite extends CatsEffectSuite with SuiteRuntimePlatform {

  test("respond with a text on GET request") {
    PrometheusMetricExporter.builder[IO].build.flatMap { exporter =>
      val routes: HttpRoutes[IO] = PrometheusHttpRoutes.routes(exporter, PrometheusWriter.Config.default)

      routes.orNotFound
        .run(
          Request(method = Method.GET, uri = uri"/")
        )
        .flatMap { response =>
          IO {
            assertEquals(response.status, Status.Ok)
            assertEquals(response.contentType.exists(_.mediaType.isText), true)
          }
        }
    }
  }

  test("respond with an empty body on HEAD request") {
    PrometheusMetricExporter.builder[IO].build.flatMap { exporter =>
      val routes: HttpRoutes[IO] = PrometheusHttpRoutes.routes(exporter, PrometheusWriter.Config.default)

      for {
        response <- routes.orNotFound
          .run(
            Request(method = Method.HEAD, uri = uri"/")
          )
        body <- response.body.compile.toList
      } yield {
        assertEquals(response.status, Status.Ok)
        assertEquals(body.isEmpty, true)
      }
    }
  }

  test("respond with a text on GET request and wildcard accept") {
    PrometheusMetricExporter.builder[IO].build.flatMap { exporter =>
      val routes: HttpRoutes[IO] = PrometheusHttpRoutes.routes(exporter, PrometheusWriter.Config.default)

      for {
        response <- routes.orNotFound
          .run(
            Request(method = Method.HEAD, uri = uri"/", headers = Headers(Accept(MediaRange.`*/*`)))
          )
        body <- response.body.compile.toList
      } yield {
        assertEquals(response.status, Status.Ok)
        assertEquals(body.isEmpty, true)
      }
    }
  }

  test("respond with 406 error if client defines unacceptable content negotiation header") {
    PrometheusMetricExporter.builder[IO].build.flatMap { exporter =>
      val routes: HttpRoutes[IO] = PrometheusHttpRoutes.routes(exporter, PrometheusWriter.Config.default)

      routes.orNotFound
        .run(
          Request(method = Method.GET, uri = uri"/", headers = Headers(Accept(MediaType.application.json)))
        )
        .flatMap { response =>
          IO {
            assertEquals(response.status, Status.NotAcceptable)
          }
        }
    }
  }

}
