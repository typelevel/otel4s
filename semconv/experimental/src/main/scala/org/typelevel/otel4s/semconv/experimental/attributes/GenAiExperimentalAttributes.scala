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

package org.typelevel.otel4s
package semconv
package experimental.attributes

// DO NOT EDIT, this is an Auto-generated file from buildscripts/templates/registry/otel4s/attributes/SemanticAttributes.scala.j2
object GenAiExperimentalAttributes {

  /** Deprecated, use Event API to report completions contents.
    */
  @deprecated("Removed, no replacement at this time.", "")
  val GenAiCompletion: AttributeKey[String] =
    AttributeKey("gen_ai.completion")

  /** The response format that is requested.
    */
  val GenAiOpenaiRequestResponseFormat: AttributeKey[String] =
    AttributeKey("gen_ai.openai.request.response_format")

  /** Deprecated, use `gen_ai.request.seed`.
    */
  @deprecated("Replaced by `gen_ai.request.seed` attribute.", "")
  val GenAiOpenaiRequestSeed: AttributeKey[Long] =
    AttributeKey("gen_ai.openai.request.seed")

  /** The service tier requested. May be a specific tier, default, or auto.
    */
  val GenAiOpenaiRequestServiceTier: AttributeKey[String] =
    AttributeKey("gen_ai.openai.request.service_tier")

  /** The service tier used for the response.
    */
  val GenAiOpenaiResponseServiceTier: AttributeKey[String] =
    AttributeKey("gen_ai.openai.response.service_tier")

  /** A fingerprint to track any eventual change in the Generative AI environment.
    */
  val GenAiOpenaiResponseSystemFingerprint: AttributeKey[String] =
    AttributeKey("gen_ai.openai.response.system_fingerprint")

  /** The name of the operation being performed.
    *
    * @note
    *   <p> If one of the predefined values applies, but specific system uses a different name it's RECOMMENDED to
    *   document it in the semantic conventions for specific GenAI system and use system-specific name in the
    *   instrumentation. If a different name is not documented, instrumentation libraries SHOULD use applicable
    *   predefined value.
    */
  val GenAiOperationName: AttributeKey[String] =
    AttributeKey("gen_ai.operation.name")

  /** Deprecated, use Event API to report prompt contents.
    */
  @deprecated("Removed, no replacement at this time.", "")
  val GenAiPrompt: AttributeKey[String] =
    AttributeKey("gen_ai.prompt")

  /** The encoding formats requested in an embeddings operation, if specified.
    *
    * @note
    *   <p> In some GenAI systems the encoding formats are called embedding types. Also, some GenAI systems only accept
    *   a single format per request.
    */
  val GenAiRequestEncodingFormats: AttributeKey[Seq[String]] =
    AttributeKey("gen_ai.request.encoding_formats")

  /** The frequency penalty setting for the GenAI request.
    */
  val GenAiRequestFrequencyPenalty: AttributeKey[Double] =
    AttributeKey("gen_ai.request.frequency_penalty")

  /** The maximum number of tokens the model generates for a request.
    */
  val GenAiRequestMaxTokens: AttributeKey[Long] =
    AttributeKey("gen_ai.request.max_tokens")

  /** The name of the GenAI model a request is being made to.
    */
  val GenAiRequestModel: AttributeKey[String] =
    AttributeKey("gen_ai.request.model")

  /** The presence penalty setting for the GenAI request.
    */
  val GenAiRequestPresencePenalty: AttributeKey[Double] =
    AttributeKey("gen_ai.request.presence_penalty")

  /** Requests with same seed value more likely to return same result.
    */
  val GenAiRequestSeed: AttributeKey[Long] =
    AttributeKey("gen_ai.request.seed")

  /** List of sequences that the model will use to stop generating further tokens.
    */
  val GenAiRequestStopSequences: AttributeKey[Seq[String]] =
    AttributeKey("gen_ai.request.stop_sequences")

  /** The temperature setting for the GenAI request.
    */
  val GenAiRequestTemperature: AttributeKey[Double] =
    AttributeKey("gen_ai.request.temperature")

  /** The top_k sampling setting for the GenAI request.
    */
  val GenAiRequestTopK: AttributeKey[Double] =
    AttributeKey("gen_ai.request.top_k")

  /** The top_p sampling setting for the GenAI request.
    */
  val GenAiRequestTopP: AttributeKey[Double] =
    AttributeKey("gen_ai.request.top_p")

  /** Array of reasons the model stopped generating tokens, corresponding to each generation received.
    */
  val GenAiResponseFinishReasons: AttributeKey[Seq[String]] =
    AttributeKey("gen_ai.response.finish_reasons")

  /** The unique identifier for the completion.
    */
  val GenAiResponseId: AttributeKey[String] =
    AttributeKey("gen_ai.response.id")

  /** The name of the model that generated the response.
    */
  val GenAiResponseModel: AttributeKey[String] =
    AttributeKey("gen_ai.response.model")

  /** The Generative AI product as identified by the client or server instrumentation.
    *
    * @note
    *   <p> The `gen_ai.system` describes a family of GenAI models with specific model identified by
    *   `gen_ai.request.model` and `gen_ai.response.model` attributes. <p> The actual GenAI product may differ from the
    *   one identified by the client. Multiple systems, including Azure OpenAI and Gemini, are accessible by OpenAI
    *   client libraries. In such cases, the `gen_ai.system` is set to `openai` based on the instrumentation's best
    *   knowledge, instead of the actual system. The `server.address` attribute may help identify the actual system in
    *   use for `openai`. <p> For custom model, a custom friendly name SHOULD be used. If none of these options apply,
    *   the `gen_ai.system` SHOULD be set to `_OTHER`.
    */
  val GenAiSystem: AttributeKey[String] =
    AttributeKey("gen_ai.system")

  /** The type of token being counted.
    */
  val GenAiTokenType: AttributeKey[String] =
    AttributeKey("gen_ai.token.type")

  /** Deprecated, use `gen_ai.usage.output_tokens` instead.
    */
  @deprecated("Replaced by `gen_ai.usage.output_tokens` attribute.", "")
  val GenAiUsageCompletionTokens: AttributeKey[Long] =
    AttributeKey("gen_ai.usage.completion_tokens")

  /** The number of tokens used in the GenAI input (prompt).
    */
  val GenAiUsageInputTokens: AttributeKey[Long] =
    AttributeKey("gen_ai.usage.input_tokens")

  /** The number of tokens used in the GenAI response (completion).
    */
  val GenAiUsageOutputTokens: AttributeKey[Long] =
    AttributeKey("gen_ai.usage.output_tokens")

  /** Deprecated, use `gen_ai.usage.input_tokens` instead.
    */
  @deprecated("Replaced by `gen_ai.usage.input_tokens` attribute.", "")
  val GenAiUsagePromptTokens: AttributeKey[Long] =
    AttributeKey("gen_ai.usage.prompt_tokens")

  /** Values for [[GenAiOpenaiRequestResponseFormat]].
    */
  abstract class GenAiOpenaiRequestResponseFormatValue(val value: String)
  object GenAiOpenaiRequestResponseFormatValue {

    /** Text response format
      */
    case object Text extends GenAiOpenaiRequestResponseFormatValue("text")

    /** JSON object response format
      */
    case object JsonObject extends GenAiOpenaiRequestResponseFormatValue("json_object")

    /** JSON schema response format
      */
    case object JsonSchema extends GenAiOpenaiRequestResponseFormatValue("json_schema")
  }

  /** Values for [[GenAiOpenaiRequestServiceTier]].
    */
  abstract class GenAiOpenaiRequestServiceTierValue(val value: String)
  object GenAiOpenaiRequestServiceTierValue {

    /** The system will utilize scale tier credits until they are exhausted.
      */
    case object Auto extends GenAiOpenaiRequestServiceTierValue("auto")

    /** The system will utilize the default scale tier.
      */
    case object Default extends GenAiOpenaiRequestServiceTierValue("default")
  }

  /** Values for [[GenAiOperationName]].
    */
  abstract class GenAiOperationNameValue(val value: String)
  object GenAiOperationNameValue {

    /** Chat completion operation such as <a href="https://platform.openai.com/docs/api-reference/chat">OpenAI Chat
      * API</a>
      */
    case object Chat extends GenAiOperationNameValue("chat")

    /** Text completions operation such as <a href="https://platform.openai.com/docs/api-reference/completions">OpenAI
      * Completions API (Legacy)</a>
      */
    case object TextCompletion extends GenAiOperationNameValue("text_completion")

    /** Embeddings operation such as <a href="https://platform.openai.com/docs/api-reference/embeddings/create">OpenAI
      * Create embeddings API</a>
      */
    case object Embeddings extends GenAiOperationNameValue("embeddings")
  }

  /** Values for [[GenAiSystem]].
    */
  abstract class GenAiSystemValue(val value: String)
  object GenAiSystemValue {

    /** OpenAI
      */
    case object Openai extends GenAiSystemValue("openai")

    /** Vertex AI
      */
    case object VertexAi extends GenAiSystemValue("vertex_ai")

    /** Gemini
      */
    case object Gemini extends GenAiSystemValue("gemini")

    /** Anthropic
      */
    case object Anthropic extends GenAiSystemValue("anthropic")

    /** Cohere
      */
    case object Cohere extends GenAiSystemValue("cohere")

    /** Azure AI Inference
      */
    case object AzAiInference extends GenAiSystemValue("az.ai.inference")

    /** Azure OpenAI
      */
    case object AzAiOpenai extends GenAiSystemValue("az.ai.openai")

    /** IBM Watsonx AI
      */
    case object IbmWatsonxAi extends GenAiSystemValue("ibm.watsonx.ai")

    /** AWS Bedrock
      */
    case object AwsBedrock extends GenAiSystemValue("aws.bedrock")

    /** Perplexity
      */
    case object Perplexity extends GenAiSystemValue("perplexity")

    /** xAI
      */
    case object Xai extends GenAiSystemValue("xai")

    /** DeepSeek
      */
    case object Deepseek extends GenAiSystemValue("deepseek")

    /** Groq
      */
    case object Groq extends GenAiSystemValue("groq")

    /** Mistral AI
      */
    case object MistralAi extends GenAiSystemValue("mistral_ai")
  }

  /** Values for [[GenAiTokenType]].
    */
  abstract class GenAiTokenTypeValue(val value: String)
  object GenAiTokenTypeValue {

    /** Input tokens (prompt, input, etc.)
      */
    case object Input extends GenAiTokenTypeValue("input")

    /** Output tokens (completion, response, etc.)
      */
    case object Completion extends GenAiTokenTypeValue("output")
  }

}
