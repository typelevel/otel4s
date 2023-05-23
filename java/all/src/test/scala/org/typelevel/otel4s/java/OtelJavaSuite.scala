/*
 * Copyright 2022 Typelevel
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

package org.typelevel.otel4s.java

import io.opentelemetry.sdk.{OpenTelemetrySdk => JOpenTelemetrySdk}
import munit.CatsEffectSuite

class OtelJavaSuite extends CatsEffectSuite {

  test("JOtelSDK toString returns useful info") {
    assertEquals(
      JOpenTelemetrySdk.builder().build().toString(),
      "OpenTelemetrySdk{tracerProvider=SdkTracerProvider{clock=SystemClock{}, idGenerator=RandomIdGenerator{}, resource=Resource{schemaUrl=null, attributes={service.name=\"unknown_service:java\", telemetry.sdk.language=\"java\", telemetry.sdk.name=\"opentelemetry\", telemetry.sdk.version=\"1.26.0\"}}, spanLimitsSupplier=SpanLimitsValue{maxNumberOfAttributes=128, maxNumberOfEvents=128, maxNumberOfLinks=128, maxNumberOfAttributesPerEvent=128, maxNumberOfAttributesPerLink=128, maxAttributeValueLength=2147483647}, sampler=ParentBased{root:AlwaysOnSampler,remoteParentSampled:AlwaysOnSampler,remoteParentNotSampled:AlwaysOffSampler,localParentSampled:AlwaysOnSampler,localParentNotSampled:AlwaysOffSampler}, spanProcessor=NoopSpanProcessor{}}, meterProvider=SdkMeterProvider{clock=SystemClock{}, resource=Resource{schemaUrl=null, attributes={service.name=\"unknown_service:java\", telemetry.sdk.language=\"java\", telemetry.sdk.name=\"opentelemetry\", telemetry.sdk.version=\"1.26.0\"}}, metricReaders=[], views=[]}, loggerProvider=SdkLoggerProvider{clock=SystemClock{}, resource=Resource{schemaUrl=null, attributes={service.name=\"unknown_service:java\", telemetry.sdk.language=\"java\", telemetry.sdk.name=\"opentelemetry\", telemetry.sdk.version=\"1.26.0\"}}, logLimits=LogLimits{maxNumberOfAttributes=128, maxAttributeValueLength=2147483647}, logRecordProcessor=io.opentelemetry.sdk.logs.NoopLogRecordProcessor@59364403}, propagators=DefaultContextPropagators{textMapPropagator=NoopTextMapPropagator}}"
    )
  }
}
