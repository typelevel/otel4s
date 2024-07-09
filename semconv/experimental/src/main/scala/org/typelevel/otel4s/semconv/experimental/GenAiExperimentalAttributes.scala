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

  /** The full response received from the LLM.
    *
    * @note
    *   - It's RECOMMENDED to format completions as JSON string matching <a
    *     href="https://platform.openai.com/docs/guides/text-generation">OpenAI
    *     messages format</a>
    */
  val GenAiCompletion: AttributeKey[String] = string("gen_ai.completion")

  /** The full prompt sent to an LLM.
    *
    * @note
    *   - It's RECOMMENDED to format prompts as JSON string matching <a
    *     href="https://platform.openai.com/docs/guides/text-generation">OpenAI
    *     messages format</a>
    */
  val GenAiPrompt: AttributeKey[String] = string("gen_ai.prompt")

  /** The maximum number of tokens the LLM generates for a request.
    */
  val GenAiRequestMaxTokens: AttributeKey[Long] = long(
    "gen_ai.request.max_tokens"
  )

  /** The name of the LLM a request is being made to.
    */
  val GenAiRequestModel: AttributeKey[String] = string("gen_ai.request.model")

  /** The temperature setting for the LLM request.
    */
  val GenAiRequestTemperature: AttributeKey[Double] = double(
    "gen_ai.request.temperature"
  )

  /** The top_p sampling setting for the LLM request.
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

  /** The name of the LLM a response was generated from.
    */
  val GenAiResponseModel: AttributeKey[String] = string("gen_ai.response.model")

  /** The Generative AI product as identified by the client instrumentation.
    *
    * @note
    *   - The actual GenAI product may differ from the one identified by the
    *     client. For example, when using OpenAI client libraries to communicate
    *     with Mistral, the `gen_ai.system` is set to `openai` based on the
    *     instrumentation's best knowledge.
    */
  val GenAiSystem: AttributeKey[String] = string("gen_ai.system")

  /** The number of tokens used in the LLM response (completion).
    */
  val GenAiUsageCompletionTokens: AttributeKey[Long] = long(
    "gen_ai.usage.completion_tokens"
  )

  /** The number of tokens used in the LLM prompt.
    */
  val GenAiUsagePromptTokens: AttributeKey[Long] = long(
    "gen_ai.usage.prompt_tokens"
  )
  // Enum definitions

  /** Values for [[GenAiSystem]].
    */
  abstract class GenAiSystemValue(val value: String)
  object GenAiSystemValue {

    /** OpenAI. */
    case object Openai extends GenAiSystemValue("openai")
  }

}
