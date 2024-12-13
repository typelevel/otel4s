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

package org.typelevel.otel4s.sdk.contrib.aws.context.propagation

import org.typelevel.otel4s.context.propagation.TextMapGetter
import org.typelevel.otel4s.context.propagation.TextMapPropagator
import org.typelevel.otel4s.context.propagation.TextMapUpdater
import org.typelevel.otel4s.sdk.autoconfigure.AutoConfigure
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.trace.SdkContextKeys

/** An example of the AWS X-Ray Tracing Header:
  * {{{
  *   X-Amzn-Trace-Id: Root=1-5759e988-bd862e3fe1be46a994272793;Parent=53995c3f42cd8ad8;Sampled=1
  * }}}
  *
  * If the header is missing, the Lambda's `com.amazonaws.xray.traceHeader` system property or `_X_AMZN_TRACE_ID`
  * environment variable will be used as a fallback.
  *
  * @see
  *   [[https://docs.aws.amazon.com/xray/latest/devguide/xray-concepts.html#xray-concepts-tracingheader]]
  *
  * @see
  *   [[https://docs.aws.amazon.com/lambda/latest/dg/configuration-envvars.html]]
  */
private final class AwsXRayLambdaPropagator private[propagation] (
    getProp: String => Option[String],
    getEnv: String => Option[String]
) extends TextMapPropagator[Context] {
  import AwsXRayLambdaPropagator.Const

  private val propagator = AwsXRayPropagator()

  def fields: Iterable[String] = propagator.fields

  def extract[A: TextMapGetter](ctx: Context, carrier: A): Context = {
    val xRayContext = propagator.extract(ctx, carrier)
    xRayContext.get(SdkContextKeys.SpanContextKey) match {
      case Some(_) =>
        xRayContext

      case None =>
        val headerOpt =
          getProp(Const.TraceHeaderSystemProp)
            .orElse(getEnv(Const.TraceHeaderEnvVar))

        headerOpt match {
          case Some(header) =>
            propagator.extract(
              ctx,
              Map(AwsXRayPropagator.Headers.TraceId -> header)
            )

          case None =>
            xRayContext
        }
    }
  }

  def inject[A: TextMapUpdater](ctx: Context, carrier: A): A =
    propagator.inject(ctx, carrier)

  override def toString: String = "AwsXRayLambdaPropagator"

}

object AwsXRayLambdaPropagator {
  private val Propagator =
    new AwsXRayLambdaPropagator(sys.props.get, sys.env.get)

  private object Const {
    val name = "xray-lambda"

    val TraceHeaderEnvVar = "_X_AMZN_TRACE_ID"
    val TraceHeaderSystemProp = "com.amazonaws.xray.traceHeader"
  }

  /** Returns an instance of the AwsXRayLambdaPropagator.
    *
    * The propagator utilizes `X-Amzn-Trace-Id` header to extract and inject tracing details.
    *
    * If the header is missing, the Lambda's `com.amazonaws.xray.traceHeader` system property or `_X_AMZN_TRACE_ID`
    * environment variable will be used as a fallback.
    *
    * An example of the AWS X-Ray Tracing Header:
    * {{{
    *   X-Amzn-Trace-Id: Root=1-5759e988-bd862e3fe1be46a994272793;Parent=53995c3f42cd8ad8;Sampled=1
    * }}}
    *
    * @example
    *   {{{
    * OpenTelemetrySdk.autoConfigured[IO](
    *   _.addTracerProviderCustomizer((b, _) => b.addTextMapPropagators(AwsXRayLambdaPropagator())
    * )
    *   }}}
    * @see
    *   [[https://docs.aws.amazon.com/xray/latest/devguide/xray-concepts.html#xray-concepts-tracingheader]]
    */
  def apply(): TextMapPropagator[Context] = Propagator

  /** Returns the named configurer `xray-lambda`. You can use it to dynamically enable AWS X-Ray lambda propagator via
    * environment variable or system properties.
    *
    * @example
    *   {{{
    * OpenTelemetrySdk.autoConfigured[IO](
    *   _.addTextMapPropagatorConfigurer(AwsXRayLambdaPropagator.configurer[IO])
    * )
    *   }}}
    *
    * Enable propagator via environment variable:
    * {{{
    *  OTEL_PROPAGATORS=xray-lambda
    * }}}
    * or system property:
    * {{{
    *    -Dotel.propagators=xray-lambda
    * }}}
    */
  def configurer[F[_]]: AutoConfigure.Named[F, TextMapPropagator[Context]] =
    AutoConfigure.Named.const(Const.name, Propagator)

}
