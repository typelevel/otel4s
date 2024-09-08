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
package metrics

import org.typelevel.otel4s.metrics._

// DO NOT EDIT, this is an Auto-generated file from buildscripts/templates/registry/otel4s/metrics/SemanticMetrics.scala.j2
object DnsExperimentalMetrics {

  /** Measures the time taken to perform a DNS lookup.
    */
  object LookupDuration {

    val Name = "dns.lookup.duration"
    val Description = "Measures the time taken to perform a DNS lookup."
    val Unit = "s"

    object AttributeSpecs {

      /** The name being queried. <p>
        * @note
        *   <p> If the name field contains non-printable characters (below 32 or above 126), those characters should be
        *   represented as escaped base 10 integers (\DDD). Back slashes and quotes should be escaped. Tabs, carriage
        *   returns, and line feeds should be converted to \t, \r, and \n respectively.
        */
      val dnsQuestionName: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("dns.question.name"),
          List(
            "www.example.com",
            "dot.net",
          ),
          Requirement.required,
          Stability.experimental
        )

      /** Describes the error the DNS lookup failed with. <p>
        * @note
        *   <p> Instrumentations SHOULD use error code such as one of errors reported by `getaddrinfo`(<a
        *   href="https://man7.org/linux/man-pages/man3/getaddrinfo.3.html">Linux or other POSIX systems</a> / <a
        *   href="https://learn.microsoft.com/windows/win32/api/ws2tcpip/nf-ws2tcpip-getaddrinfo">Windows</a>) or one
        *   reported by the runtime or client library. If error code is not available, the full name of exception type
        *   SHOULD be used.
        */
      val errorType: AttributeSpec[String] =
        AttributeSpec(
          AttributeKey("error.type"),
          List(
            "host_not_found",
            "no_recovery",
            "java.net.UnknownHostException",
          ),
          Requirement.conditionallyRequired(
            "if and only if an error has occurred."
          ),
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          dnsQuestionName,
          errorType,
        )
    }

    def create[F[_]: Meter](
        boundaries: BucketBoundaries
    ): F[Histogram[F, Double]] =
      Meter[F]
        .histogram[Double](Name)
        .withDescription(Description)
        .withUnit(Unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

}
