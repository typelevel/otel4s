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

package org.typelevel.otel4s.sdk.scalacheck

import org.scalacheck.Cogen
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.common.InstrumentationScope

trait Cogens extends org.typelevel.otel4s.scalacheck.Cogens {

  implicit val telemetryResourceCogen: Cogen[TelemetryResource] =
    Cogen[(Attributes, Option[String])].contramap { r =>
      (r.attributes, r.schemaUrl)
    }

  implicit val instrumentationScopeCogen: Cogen[InstrumentationScope] =
    Cogen[(String, Option[String], Option[String], Attributes)].contramap { s =>
      (s.name, s.version, s.schemaUrl, s.attributes)
    }

}

object Cogens extends Cogens
