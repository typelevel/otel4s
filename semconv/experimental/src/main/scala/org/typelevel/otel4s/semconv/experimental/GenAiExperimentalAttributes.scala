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

package org.typelevel.otel4s.semconv.experimental.attributes

import org.typelevel.otel4s.AttributeKey
import org.typelevel.otel4s.AttributeKey._

// DO NOT EDIT, this is an Auto-generated file from buildscripts/semantic-convention/templates/SemanticAttributes.scala.j2
object GenAiExperimentalAttributes {

  /** The full response received from the GenAI model.
    *
    * @note
    *   - It's RECOMMENDED to format completions as JSON string matching <a
    *     href="https://platform.openai.com/docs/guides/text-generation">OpenAI
    *     messages format</a>
    */
  val GenAiCompletion: AttributeKey[String] = string("gen_ai.completion")

  /** The name of the operation being performed.
    *
    * @note
    *   - If one of the predefined values applies, but specific system uses a
    *     different name it's RECOMMENDED to document it in the semantic
    *     conventions for specific GenAI system and use system-specific name in
    *     the instrumentation. If a different name is not documented,
    *     instrumentation libraries SHOULD use applicable predefined value.
    */
  val GenAiOperationName: AttributeKey[String] = string("gen_ai.operation.name")

  /** The full prompt sent to the GenAI model.
    *
    * @note
    *   - It's RECOMMENDED to format prompts as JSON string matching <a
    *     href="https://platform.openai.com/docs/guides/text-generation">OpenAI
    *     messages format</a>
    */
  val GenAiPrompt: AttributeKey[String] = string("gen_ai.prompt")

  /** The frequency penalty setting for the GenAI request.
    */
  val GenAiRequestFrequencyPenalty: AttributeKey[Double] = double(
    "gen_ai.request.frequency_penalty"
  )

  /** The maximum number of tokens the model generates for a request.
    */
  val GenAiRequestMaxTokens: AttributeKey[Long] = long(
    "gen_ai.request.max_tokens"
  )

  /** The name of the GenAI model a request is being made to.
    */
  val GenAiRequestModel: AttributeKey[String] = string("gen_ai.request.model")

  /** The presence penalty setting for the GenAI request.
    */
  val GenAiRequestPresencePenalty: AttributeKey[Double] = double(
    "gen_ai.request.presence_penalty"
  )

  /** List of sequences that the model will use to stop generating further
    * tokens.
    */
  val GenAiRequestStopSequences: AttributeKey[Seq[String]] = stringSeq(
    "gen_ai.request.stop_sequences"
  )

  /** The temperature setting for the GenAI request.
    */
  val GenAiRequestTemperature: AttributeKey[Double] = double(
    "gen_ai.request.temperature"
  )

  /** The top_k sampling setting for the GenAI request.
    */
  val GenAiRequestTopK: AttributeKey[Double] = double("gen_ai.request.top_k")

  /** The top_p sampling setting for the GenAI request.
    */
  val GenAiRequestTopP: AttributeKey[Double] = double("gen_ai.request.top_p")

  /** Array of reasons the model stopped generating tokens, corresponding to
    * each generation received.
    */
  val GenAiResponseFinishReasons: AttributeKey[Seq[String]] = stringSeq(
    "gen_ai.response.finish_reasons"
  )

  /** The unique identifier for the completion.
    */
  val GenAiResponseId: AttributeKey[String] = string("gen_ai.response.id")

  /** The name of the model that generated the response.
    */
  val GenAiResponseModel: AttributeKey[String] = string("gen_ai.response.model")

  /** The Generative AI product as identified by the client or server
    * instrumentation.
    *
    * @note
    *   - The `gen_ai.system` describes a family of GenAI models with specific
    *     model identified by `gen_ai.request.model` and `gen_ai.response.model`
    *     attributes.
    *   - The actual GenAI product may differ from the one identified by the
    *     client. For example, when using OpenAI client libraries to communicate
    *     with Mistral, the `gen_ai.system` is set to `openai` based on the
    *     instrumentation's best knowledge.
    *   - For custom model, a custom friendly name SHOULD be used. If none of
    *     these options apply, the `gen_ai.system` SHOULD be set to `_OTHER`.
    */
  val GenAiSystem: AttributeKey[String] = string("gen_ai.system")

  /** The type of token being counted.
    */
  val GenAiTokenType: AttributeKey[String] = string("gen_ai.token.type")

  /** Deprecated, use `gen_ai.usage.output_tokens` instead.
    */
  @deprecated("Use `gen_ai.usage.output_tokens` instead", "0.5.0")
  val GenAiUsageCompletionTokens: AttributeKey[Long] = long(
    "gen_ai.usage.completion_tokens"
  )

  /** The number of tokens used in the GenAI input (prompt).
    */
  val GenAiUsageInputTokens: AttributeKey[Long] = long(
    "gen_ai.usage.input_tokens"
  )

  /** The number of tokens used in the GenAI response (completion).
    */
  val GenAiUsageOutputTokens: AttributeKey[Long] = long(
    "gen_ai.usage.output_tokens"
  )

  /** Deprecated, use `gen_ai.usage.input_tokens` instead.
    */
  @deprecated("Use `gen_ai.usage.input_tokens` instead", "0.5.0")
  val GenAiUsagePromptTokens: AttributeKey[Long] = long(
    "gen_ai.usage.prompt_tokens"
  )
  // Enum definitions

  /** Values for [[GenAiOperationName]].
    */
  abstract class GenAiOperationNameValue(val value: String)
  object GenAiOperationNameValue {

    /** Chat completion operation such as [OpenAI Chat
      * API](https://platform.openai.com/docs/api-reference/chat).
      */
    case object Chat extends GenAiOperationNameValue("chat")

    /** Text completions operation such as [OpenAI Completions API
      * (Legacy)](https://platform.openai.com/docs/api-reference/completions).
      */
    case object TextCompletion
        extends GenAiOperationNameValue("text_completion")
  }

  /** Values for [[GenAiSystem]].
    */
  abstract class GenAiSystemValue(val value: String)
  object GenAiSystemValue {

    /** OpenAI. */
    case object Openai extends GenAiSystemValue("openai")

    /** Vertex AI. */
    case object VertexAi extends GenAiSystemValue("vertex_ai")

    /** Anthropic. */
    case object Anthropic extends GenAiSystemValue("anthropic")

    /** Cohere. */
    case object Cohere extends GenAiSystemValue("cohere")
  }

  /** Values for [[GenAiTokenType]].
    */
  abstract class GenAiTokenTypeValue(val value: String)
  object GenAiTokenTypeValue {

    /** Input tokens (prompt, input, etc.). */
    case object Input extends GenAiTokenTypeValue("input")

    /** Output tokens (completion, response, etc.). */
    case object Completion extends GenAiTokenTypeValue("output")
  }

}
