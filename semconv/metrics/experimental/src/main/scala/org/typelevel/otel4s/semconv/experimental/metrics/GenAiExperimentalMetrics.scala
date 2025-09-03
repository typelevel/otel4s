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
package experimental
package metrics

import org.typelevel.otel4s.metrics._
import org.typelevel.otel4s.semconv.attributes._
import org.typelevel.otel4s.semconv.experimental.attributes._

// DO NOT EDIT, this is an Auto-generated file from buildscripts/templates/registry/otel4s/metrics/SemanticMetrics.scala.j2
object GenAiExperimentalMetrics {

  val specs: List[MetricSpec] = List(
    ClientOperationDuration,
    ClientTokenUsage,
    ServerRequestDuration,
    ServerTimePerOutputToken,
    ServerTimeToFirstToken,
  )

  /** GenAI operation duration.
    */
  object ClientOperationDuration extends MetricSpec.Unsealed {

    val name: String = "gen_ai.client.operation.duration"
    val description: String = "GenAI operation duration."
    val unit: String = "s"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Describes a class of error the operation ended with.
        *
        * @note
        *   <p> The `error.type` SHOULD match the error code returned by the Generative AI provider or the client
        *   library, the canonical name of exception that occurred, or another low-cardinality error identifier.
        *   Instrumentations SHOULD document the list of errors they report.
        */
      val errorType: AttributeSpec[String] =
        AttributeSpec(
          ErrorAttributes.ErrorType,
          List(
            "timeout",
            "java.net.UnknownHostException",
            "server_certificate_invalid",
            "500",
          ),
          Requirement.conditionallyRequired("if the operation ended in an error"),
          Stability.stable
        )

      /** The name of the operation being performed.
        *
        * @note
        *   <p> If one of the predefined values applies, but specific system uses a different name it's RECOMMENDED to
        *   document it in the semantic conventions for specific GenAI system and use system-specific name in the
        *   instrumentation. If a different name is not documented, instrumentation libraries SHOULD use applicable
        *   predefined value.
        */
      val genAiOperationName: AttributeSpec[String] =
        AttributeSpec(
          GenAiExperimentalAttributes.GenAiOperationName,
          List(
          ),
          Requirement.required,
          Stability.development
        )

      /** The Generative AI provider as identified by the client or server instrumentation.
        *
        * @note
        *   <p> The attribute SHOULD be set based on the instrumentation's best knowledge and may differ from the actual
        *   model provider. <p> Multiple providers, including Azure OpenAI, Gemini, and AI hosting platforms are
        *   accessible using the OpenAI REST API and corresponding client libraries, but may proxy or host models from
        *   different providers. <p> The `gen_ai.request.model`, `gen_ai.response.model`, and `server.address`
        *   attributes may help identify the actual system in use. <p> The `gen_ai.provider.name` attribute acts as a
        *   discriminator that identifies the GenAI telemetry format flavor specific to that provider within GenAI
        *   semantic conventions. It SHOULD be set consistently with provider-specific attributes and signals. For
        *   example, GenAI spans, metrics, and events related to AWS Bedrock should have the `gen_ai.provider.name` set
        *   to `aws.bedrock` and include applicable `aws.bedrock.*` attributes and are not expected to include
        *   `openai.*` attributes.
        */
      val genAiProviderName: AttributeSpec[String] =
        AttributeSpec(
          GenAiExperimentalAttributes.GenAiProviderName,
          List(
          ),
          Requirement.required,
          Stability.development
        )

      /** The name of the GenAI model a request is being made to.
        */
      val genAiRequestModel: AttributeSpec[String] =
        AttributeSpec(
          GenAiExperimentalAttributes.GenAiRequestModel,
          List(
            "g",
            "p",
            "t",
            "-",
            "4",
          ),
          Requirement.conditionallyRequired("If available."),
          Stability.development
        )

      /** The name of the model that generated the response.
        */
      val genAiResponseModel: AttributeSpec[String] =
        AttributeSpec(
          GenAiExperimentalAttributes.GenAiResponseModel,
          List(
            "gpt-4-0613",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** GenAI server address.
        *
        * @note
        *   <p> When observed from the client side, and when communicating through an intermediary, `server.address`
        *   SHOULD represent the server address behind any intermediaries, for example proxies, if it's available.
        */
      val serverAddress: AttributeSpec[String] =
        AttributeSpec(
          ServerAttributes.ServerAddress,
          List(
            "example.com",
            "10.1.2.80",
            "/tmp/my.sock",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** GenAI server port.
        *
        * @note
        *   <p> When observed from the client side, and when communicating through an intermediary, `server.port` SHOULD
        *   represent the server port behind any intermediaries, for example proxies, if it's available.
        */
      val serverPort: AttributeSpec[Long] =
        AttributeSpec(
          ServerAttributes.ServerPort,
          List(
            80,
            8080,
            443,
          ),
          Requirement.conditionallyRequired("If `server.address` is set."),
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          errorType,
          genAiOperationName,
          genAiProviderName,
          genAiRequestModel,
          genAiResponseModel,
          serverAddress,
          serverPort,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue](boundaries: BucketBoundaries): F[Histogram[F, A]] =
      Meter[F]
        .histogram[A](name)
        .withDescription(description)
        .withUnit(unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

  /** Number of input and output tokens used.
    */
  object ClientTokenUsage extends MetricSpec.Unsealed {

    val name: String = "gen_ai.client.token.usage"
    val description: String = "Number of input and output tokens used."
    val unit: String = "{token}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The name of the operation being performed.
        *
        * @note
        *   <p> If one of the predefined values applies, but specific system uses a different name it's RECOMMENDED to
        *   document it in the semantic conventions for specific GenAI system and use system-specific name in the
        *   instrumentation. If a different name is not documented, instrumentation libraries SHOULD use applicable
        *   predefined value.
        */
      val genAiOperationName: AttributeSpec[String] =
        AttributeSpec(
          GenAiExperimentalAttributes.GenAiOperationName,
          List(
          ),
          Requirement.required,
          Stability.development
        )

      /** The Generative AI provider as identified by the client or server instrumentation.
        *
        * @note
        *   <p> The attribute SHOULD be set based on the instrumentation's best knowledge and may differ from the actual
        *   model provider. <p> Multiple providers, including Azure OpenAI, Gemini, and AI hosting platforms are
        *   accessible using the OpenAI REST API and corresponding client libraries, but may proxy or host models from
        *   different providers. <p> The `gen_ai.request.model`, `gen_ai.response.model`, and `server.address`
        *   attributes may help identify the actual system in use. <p> The `gen_ai.provider.name` attribute acts as a
        *   discriminator that identifies the GenAI telemetry format flavor specific to that provider within GenAI
        *   semantic conventions. It SHOULD be set consistently with provider-specific attributes and signals. For
        *   example, GenAI spans, metrics, and events related to AWS Bedrock should have the `gen_ai.provider.name` set
        *   to `aws.bedrock` and include applicable `aws.bedrock.*` attributes and are not expected to include
        *   `openai.*` attributes.
        */
      val genAiProviderName: AttributeSpec[String] =
        AttributeSpec(
          GenAiExperimentalAttributes.GenAiProviderName,
          List(
          ),
          Requirement.required,
          Stability.development
        )

      /** The name of the GenAI model a request is being made to.
        */
      val genAiRequestModel: AttributeSpec[String] =
        AttributeSpec(
          GenAiExperimentalAttributes.GenAiRequestModel,
          List(
            "g",
            "p",
            "t",
            "-",
            "4",
          ),
          Requirement.conditionallyRequired("If available."),
          Stability.development
        )

      /** The name of the model that generated the response.
        */
      val genAiResponseModel: AttributeSpec[String] =
        AttributeSpec(
          GenAiExperimentalAttributes.GenAiResponseModel,
          List(
            "gpt-4-0613",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The type of token being counted.
        */
      val genAiTokenType: AttributeSpec[String] =
        AttributeSpec(
          GenAiExperimentalAttributes.GenAiTokenType,
          List(
            "input",
            "output",
          ),
          Requirement.required,
          Stability.development
        )

      /** GenAI server address.
        *
        * @note
        *   <p> When observed from the client side, and when communicating through an intermediary, `server.address`
        *   SHOULD represent the server address behind any intermediaries, for example proxies, if it's available.
        */
      val serverAddress: AttributeSpec[String] =
        AttributeSpec(
          ServerAttributes.ServerAddress,
          List(
            "example.com",
            "10.1.2.80",
            "/tmp/my.sock",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** GenAI server port.
        *
        * @note
        *   <p> When observed from the client side, and when communicating through an intermediary, `server.port` SHOULD
        *   represent the server port behind any intermediaries, for example proxies, if it's available.
        */
      val serverPort: AttributeSpec[Long] =
        AttributeSpec(
          ServerAttributes.ServerPort,
          List(
            80,
            8080,
            443,
          ),
          Requirement.conditionallyRequired("If `server.address` is set."),
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          genAiOperationName,
          genAiProviderName,
          genAiRequestModel,
          genAiResponseModel,
          genAiTokenType,
          serverAddress,
          serverPort,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue](boundaries: BucketBoundaries): F[Histogram[F, A]] =
      Meter[F]
        .histogram[A](name)
        .withDescription(description)
        .withUnit(unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

  /** Generative AI server request duration such as time-to-last byte or last output token.
    */
  object ServerRequestDuration extends MetricSpec.Unsealed {

    val name: String = "gen_ai.server.request.duration"
    val description: String = "Generative AI server request duration such as time-to-last byte or last output token."
    val unit: String = "s"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** Describes a class of error the operation ended with.
        *
        * @note
        *   <p> The `error.type` SHOULD match the error code returned by the Generative AI service, the canonical name
        *   of exception that occurred, or another low-cardinality error identifier. Instrumentations SHOULD document
        *   the list of errors they report.
        */
      val errorType: AttributeSpec[String] =
        AttributeSpec(
          ErrorAttributes.ErrorType,
          List(
            "timeout",
            "java.net.UnknownHostException",
            "server_certificate_invalid",
            "500",
          ),
          Requirement.conditionallyRequired("if the operation ended in an error"),
          Stability.stable
        )

      /** The name of the operation being performed.
        *
        * @note
        *   <p> If one of the predefined values applies, but specific system uses a different name it's RECOMMENDED to
        *   document it in the semantic conventions for specific GenAI system and use system-specific name in the
        *   instrumentation. If a different name is not documented, instrumentation libraries SHOULD use applicable
        *   predefined value.
        */
      val genAiOperationName: AttributeSpec[String] =
        AttributeSpec(
          GenAiExperimentalAttributes.GenAiOperationName,
          List(
          ),
          Requirement.required,
          Stability.development
        )

      /** The Generative AI provider as identified by the client or server instrumentation.
        *
        * @note
        *   <p> The attribute SHOULD be set based on the instrumentation's best knowledge and may differ from the actual
        *   model provider. <p> Multiple providers, including Azure OpenAI, Gemini, and AI hosting platforms are
        *   accessible using the OpenAI REST API and corresponding client libraries, but may proxy or host models from
        *   different providers. <p> The `gen_ai.request.model`, `gen_ai.response.model`, and `server.address`
        *   attributes may help identify the actual system in use. <p> The `gen_ai.provider.name` attribute acts as a
        *   discriminator that identifies the GenAI telemetry format flavor specific to that provider within GenAI
        *   semantic conventions. It SHOULD be set consistently with provider-specific attributes and signals. For
        *   example, GenAI spans, metrics, and events related to AWS Bedrock should have the `gen_ai.provider.name` set
        *   to `aws.bedrock` and include applicable `aws.bedrock.*` attributes and are not expected to include
        *   `openai.*` attributes.
        */
      val genAiProviderName: AttributeSpec[String] =
        AttributeSpec(
          GenAiExperimentalAttributes.GenAiProviderName,
          List(
          ),
          Requirement.required,
          Stability.development
        )

      /** The name of the GenAI model a request is being made to.
        */
      val genAiRequestModel: AttributeSpec[String] =
        AttributeSpec(
          GenAiExperimentalAttributes.GenAiRequestModel,
          List(
            "g",
            "p",
            "t",
            "-",
            "4",
          ),
          Requirement.conditionallyRequired("If available."),
          Stability.development
        )

      /** The name of the model that generated the response.
        */
      val genAiResponseModel: AttributeSpec[String] =
        AttributeSpec(
          GenAiExperimentalAttributes.GenAiResponseModel,
          List(
            "gpt-4-0613",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** GenAI server address.
        *
        * @note
        *   <p> When observed from the client side, and when communicating through an intermediary, `server.address`
        *   SHOULD represent the server address behind any intermediaries, for example proxies, if it's available.
        */
      val serverAddress: AttributeSpec[String] =
        AttributeSpec(
          ServerAttributes.ServerAddress,
          List(
            "example.com",
            "10.1.2.80",
            "/tmp/my.sock",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** GenAI server port.
        *
        * @note
        *   <p> When observed from the client side, and when communicating through an intermediary, `server.port` SHOULD
        *   represent the server port behind any intermediaries, for example proxies, if it's available.
        */
      val serverPort: AttributeSpec[Long] =
        AttributeSpec(
          ServerAttributes.ServerPort,
          List(
            80,
            8080,
            443,
          ),
          Requirement.conditionallyRequired("If `server.address` is set."),
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          errorType,
          genAiOperationName,
          genAiProviderName,
          genAiRequestModel,
          genAiResponseModel,
          serverAddress,
          serverPort,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue](boundaries: BucketBoundaries): F[Histogram[F, A]] =
      Meter[F]
        .histogram[A](name)
        .withDescription(description)
        .withUnit(unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

  /** Time per output token generated after the first token for successful responses.
    */
  object ServerTimePerOutputToken extends MetricSpec.Unsealed {

    val name: String = "gen_ai.server.time_per_output_token"
    val description: String = "Time per output token generated after the first token for successful responses."
    val unit: String = "s"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The name of the operation being performed.
        *
        * @note
        *   <p> If one of the predefined values applies, but specific system uses a different name it's RECOMMENDED to
        *   document it in the semantic conventions for specific GenAI system and use system-specific name in the
        *   instrumentation. If a different name is not documented, instrumentation libraries SHOULD use applicable
        *   predefined value.
        */
      val genAiOperationName: AttributeSpec[String] =
        AttributeSpec(
          GenAiExperimentalAttributes.GenAiOperationName,
          List(
          ),
          Requirement.required,
          Stability.development
        )

      /** The Generative AI provider as identified by the client or server instrumentation.
        *
        * @note
        *   <p> The attribute SHOULD be set based on the instrumentation's best knowledge and may differ from the actual
        *   model provider. <p> Multiple providers, including Azure OpenAI, Gemini, and AI hosting platforms are
        *   accessible using the OpenAI REST API and corresponding client libraries, but may proxy or host models from
        *   different providers. <p> The `gen_ai.request.model`, `gen_ai.response.model`, and `server.address`
        *   attributes may help identify the actual system in use. <p> The `gen_ai.provider.name` attribute acts as a
        *   discriminator that identifies the GenAI telemetry format flavor specific to that provider within GenAI
        *   semantic conventions. It SHOULD be set consistently with provider-specific attributes and signals. For
        *   example, GenAI spans, metrics, and events related to AWS Bedrock should have the `gen_ai.provider.name` set
        *   to `aws.bedrock` and include applicable `aws.bedrock.*` attributes and are not expected to include
        *   `openai.*` attributes.
        */
      val genAiProviderName: AttributeSpec[String] =
        AttributeSpec(
          GenAiExperimentalAttributes.GenAiProviderName,
          List(
          ),
          Requirement.required,
          Stability.development
        )

      /** The name of the GenAI model a request is being made to.
        */
      val genAiRequestModel: AttributeSpec[String] =
        AttributeSpec(
          GenAiExperimentalAttributes.GenAiRequestModel,
          List(
            "g",
            "p",
            "t",
            "-",
            "4",
          ),
          Requirement.conditionallyRequired("If available."),
          Stability.development
        )

      /** The name of the model that generated the response.
        */
      val genAiResponseModel: AttributeSpec[String] =
        AttributeSpec(
          GenAiExperimentalAttributes.GenAiResponseModel,
          List(
            "gpt-4-0613",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** GenAI server address.
        *
        * @note
        *   <p> When observed from the client side, and when communicating through an intermediary, `server.address`
        *   SHOULD represent the server address behind any intermediaries, for example proxies, if it's available.
        */
      val serverAddress: AttributeSpec[String] =
        AttributeSpec(
          ServerAttributes.ServerAddress,
          List(
            "example.com",
            "10.1.2.80",
            "/tmp/my.sock",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** GenAI server port.
        *
        * @note
        *   <p> When observed from the client side, and when communicating through an intermediary, `server.port` SHOULD
        *   represent the server port behind any intermediaries, for example proxies, if it's available.
        */
      val serverPort: AttributeSpec[Long] =
        AttributeSpec(
          ServerAttributes.ServerPort,
          List(
            80,
            8080,
            443,
          ),
          Requirement.conditionallyRequired("If `server.address` is set."),
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          genAiOperationName,
          genAiProviderName,
          genAiRequestModel,
          genAiResponseModel,
          serverAddress,
          serverPort,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue](boundaries: BucketBoundaries): F[Histogram[F, A]] =
      Meter[F]
        .histogram[A](name)
        .withDescription(description)
        .withUnit(unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

  /** Time to generate first token for successful responses.
    */
  object ServerTimeToFirstToken extends MetricSpec.Unsealed {

    val name: String = "gen_ai.server.time_to_first_token"
    val description: String = "Time to generate first token for successful responses."
    val unit: String = "s"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The name of the operation being performed.
        *
        * @note
        *   <p> If one of the predefined values applies, but specific system uses a different name it's RECOMMENDED to
        *   document it in the semantic conventions for specific GenAI system and use system-specific name in the
        *   instrumentation. If a different name is not documented, instrumentation libraries SHOULD use applicable
        *   predefined value.
        */
      val genAiOperationName: AttributeSpec[String] =
        AttributeSpec(
          GenAiExperimentalAttributes.GenAiOperationName,
          List(
          ),
          Requirement.required,
          Stability.development
        )

      /** The Generative AI provider as identified by the client or server instrumentation.
        *
        * @note
        *   <p> The attribute SHOULD be set based on the instrumentation's best knowledge and may differ from the actual
        *   model provider. <p> Multiple providers, including Azure OpenAI, Gemini, and AI hosting platforms are
        *   accessible using the OpenAI REST API and corresponding client libraries, but may proxy or host models from
        *   different providers. <p> The `gen_ai.request.model`, `gen_ai.response.model`, and `server.address`
        *   attributes may help identify the actual system in use. <p> The `gen_ai.provider.name` attribute acts as a
        *   discriminator that identifies the GenAI telemetry format flavor specific to that provider within GenAI
        *   semantic conventions. It SHOULD be set consistently with provider-specific attributes and signals. For
        *   example, GenAI spans, metrics, and events related to AWS Bedrock should have the `gen_ai.provider.name` set
        *   to `aws.bedrock` and include applicable `aws.bedrock.*` attributes and are not expected to include
        *   `openai.*` attributes.
        */
      val genAiProviderName: AttributeSpec[String] =
        AttributeSpec(
          GenAiExperimentalAttributes.GenAiProviderName,
          List(
          ),
          Requirement.required,
          Stability.development
        )

      /** The name of the GenAI model a request is being made to.
        */
      val genAiRequestModel: AttributeSpec[String] =
        AttributeSpec(
          GenAiExperimentalAttributes.GenAiRequestModel,
          List(
            "g",
            "p",
            "t",
            "-",
            "4",
          ),
          Requirement.conditionallyRequired("If available."),
          Stability.development
        )

      /** The name of the model that generated the response.
        */
      val genAiResponseModel: AttributeSpec[String] =
        AttributeSpec(
          GenAiExperimentalAttributes.GenAiResponseModel,
          List(
            "gpt-4-0613",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** GenAI server address.
        *
        * @note
        *   <p> When observed from the client side, and when communicating through an intermediary, `server.address`
        *   SHOULD represent the server address behind any intermediaries, for example proxies, if it's available.
        */
      val serverAddress: AttributeSpec[String] =
        AttributeSpec(
          ServerAttributes.ServerAddress,
          List(
            "example.com",
            "10.1.2.80",
            "/tmp/my.sock",
          ),
          Requirement.recommended,
          Stability.stable
        )

      /** GenAI server port.
        *
        * @note
        *   <p> When observed from the client side, and when communicating through an intermediary, `server.port` SHOULD
        *   represent the server port behind any intermediaries, for example proxies, if it's available.
        */
      val serverPort: AttributeSpec[Long] =
        AttributeSpec(
          ServerAttributes.ServerPort,
          List(
            80,
            8080,
            443,
          ),
          Requirement.conditionallyRequired("If `server.address` is set."),
          Stability.stable
        )

      val specs: List[AttributeSpec[_]] =
        List(
          genAiOperationName,
          genAiProviderName,
          genAiRequestModel,
          genAiResponseModel,
          serverAddress,
          serverPort,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue](boundaries: BucketBoundaries): F[Histogram[F, A]] =
      Meter[F]
        .histogram[A](name)
        .withDescription(description)
        .withUnit(unit)
        .withExplicitBucketBoundaries(boundaries)
        .create

  }

}
