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

  /** Free-form description of the GenAI agent provided by the application.
    */
  val GenAiAgentDescription: AttributeKey[String] =
    AttributeKey("gen_ai.agent.description")

  /** The unique identifier of the GenAI agent.
    */
  val GenAiAgentId: AttributeKey[String] =
    AttributeKey("gen_ai.agent.id")

  /** Human-readable name of the GenAI agent provided by the application.
    */
  val GenAiAgentName: AttributeKey[String] =
    AttributeKey("gen_ai.agent.name")

  /** The version of the GenAI agent.
    */
  val GenAiAgentVersion: AttributeKey[String] =
    AttributeKey("gen_ai.agent.version")

  /** Deprecated, use Event API to report completions contents.
    */
  @deprecated("Removed, no replacement at this time.", "")
  val GenAiCompletion: AttributeKey[String] =
    AttributeKey("gen_ai.completion")

  /** The unique identifier for a conversation (session, thread), used to store and correlate messages within this
    * conversation.
    */
  val GenAiConversationId: AttributeKey[String] =
    AttributeKey("gen_ai.conversation.id")

  /** The data source identifier.
    *
    * @note
    *   <p> Data sources are used by AI agents and RAG applications to store grounding data. A data source may be an
    *   external database, object store, document collection, website, or any other storage system used by the GenAI
    *   agent or application. The `gen_ai.data_source.id` SHOULD match the identifier used by the GenAI system rather
    *   than a name specific to the external storage, such as a database or object store. Semantic conventions
    *   referencing `gen_ai.data_source.id` MAY also leverage additional attributes, such as `db.*`, to further identify
    *   and describe the data source.
    */
  val GenAiDataSourceId: AttributeKey[String] =
    AttributeKey("gen_ai.data_source.id")

  /** The number of dimensions the resulting output embeddings should have.
    */
  val GenAiEmbeddingsDimensionCount: AttributeKey[Long] =
    AttributeKey("gen_ai.embeddings.dimension.count")

  /** A free-form explanation for the assigned score provided by the evaluator.
    */
  val GenAiEvaluationExplanation: AttributeKey[String] =
    AttributeKey("gen_ai.evaluation.explanation")

  /** The name of the evaluation metric used for the GenAI response.
    */
  val GenAiEvaluationName: AttributeKey[String] =
    AttributeKey("gen_ai.evaluation.name")

  /** Human readable label for evaluation.
    *
    * @note
    *   <p> This attribute provides a human-readable interpretation of the evaluation score produced by an evaluator.
    *   For example, a score value of 1 could mean "relevant" in one evaluation system and "not relevant" in another,
    *   depending on the scoring range and evaluator. The label SHOULD have low cardinality. Possible values depend on
    *   the evaluation metric and evaluator used; implementations SHOULD document the possible values.
    */
  val GenAiEvaluationScoreLabel: AttributeKey[String] =
    AttributeKey("gen_ai.evaluation.score.label")

  /** The evaluation score returned by the evaluator.
    */
  val GenAiEvaluationScoreValue: AttributeKey[Double] =
    AttributeKey("gen_ai.evaluation.score.value")

  /** Deprecated, use `gen_ai.output.type`.
    */
  @deprecated("Replaced by `gen_ai.output.type`.", "")
  val GenAiOpenaiRequestResponseFormat: AttributeKey[String] =
    AttributeKey("gen_ai.openai.request.response_format")

  /** Deprecated, use `gen_ai.request.seed`.
    */
  @deprecated("Replaced by `gen_ai.request.seed`.", "")
  val GenAiOpenaiRequestSeed: AttributeKey[Long] =
    AttributeKey("gen_ai.openai.request.seed")

  /** Deprecated, use `openai.request.service_tier`.
    */
  @deprecated("Replaced by `openai.request.service_tier`.", "")
  val GenAiOpenaiRequestServiceTier: AttributeKey[String] =
    AttributeKey("gen_ai.openai.request.service_tier")

  /** Deprecated, use `openai.response.service_tier`.
    */
  @deprecated("Replaced by `openai.response.service_tier`.", "")
  val GenAiOpenaiResponseServiceTier: AttributeKey[String] =
    AttributeKey("gen_ai.openai.response.service_tier")

  /** Deprecated, use `openai.response.system_fingerprint`.
    */
  @deprecated("Replaced by `openai.response.system_fingerprint`.", "")
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

  /** Represents the content type requested by the client.
    *
    * @note
    *   <p> This attribute SHOULD be used when the client requests output of a specific type. The model may return zero
    *   or more outputs of this type. This attribute specifies the output modality and not the actual output format. For
    *   example, if an image is requested, the actual output could be a URL pointing to an image file. Additional output
    *   format details may be recorded in the future in the `gen_ai.output.{type}.*` attributes.
    */
  val GenAiOutputType: AttributeKey[String] =
    AttributeKey("gen_ai.output.type")

  /** Deprecated, use Event API to report prompt contents.
    */
  @deprecated("Removed, no replacement at this time.", "")
  val GenAiPrompt: AttributeKey[String] =
    AttributeKey("gen_ai.prompt")

  /** The name of the prompt that uniquely identifies it.
    */
  val GenAiPromptName: AttributeKey[String] =
    AttributeKey("gen_ai.prompt.name")

  /** The Generative AI provider as identified by the client or server instrumentation.
    *
    * @note
    *   <p> The attribute SHOULD be set based on the instrumentation's best knowledge and may differ from the actual
    *   model provider. <p> Multiple providers, including Azure OpenAI, Gemini, and AI hosting platforms are accessible
    *   using the OpenAI REST API and corresponding client libraries, but may proxy or host models from different
    *   providers. <p> The `gen_ai.request.model`, `gen_ai.response.model`, and `server.address` attributes may help
    *   identify the actual system in use. <p> The `gen_ai.provider.name` attribute acts as a discriminator that
    *   identifies the GenAI telemetry format flavor specific to that provider within GenAI semantic conventions. It
    *   SHOULD be set consistently with provider-specific attributes and signals. For example, GenAI spans, metrics, and
    *   events related to AWS Bedrock should have the `gen_ai.provider.name` set to `aws.bedrock` and include applicable
    *   `aws.bedrock.*` attributes and are not expected to include `openai.*` attributes.
    */
  val GenAiProviderName: AttributeKey[String] =
    AttributeKey("gen_ai.provider.name")

  /** The target number of candidate completions to return.
    */
  val GenAiRequestChoiceCount: AttributeKey[Long] =
    AttributeKey("gen_ai.request.choice.count")

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

  /** The query text used for retrieval.
    *
    * @note
    *   <blockquote> [!Warning] This attribute may contain sensitive information.</blockquote>
    */
  val GenAiRetrievalQueryText: AttributeKey[String] =
    AttributeKey("gen_ai.retrieval.query.text")

  /** Deprecated, use `gen_ai.provider.name` instead.
    */
  @deprecated("Replaced by `gen_ai.provider.name`.", "")
  val GenAiSystem: AttributeKey[String] =
    AttributeKey("gen_ai.system")

  /** The type of token being counted.
    */
  val GenAiTokenType: AttributeKey[String] =
    AttributeKey("gen_ai.token.type")

  /** The tool call identifier.
    */
  val GenAiToolCallId: AttributeKey[String] =
    AttributeKey("gen_ai.tool.call.id")

  /** The tool description.
    */
  val GenAiToolDescription: AttributeKey[String] =
    AttributeKey("gen_ai.tool.description")

  /** Name of the tool utilized by the agent.
    */
  val GenAiToolName: AttributeKey[String] =
    AttributeKey("gen_ai.tool.name")

  /** Type of the tool utilized by the agent
    *
    * @note
    *   <p> Extension: A tool executed on the agent-side to directly call external APIs, bridging the gap between the
    *   agent and real-world systems. Agent-side operations involve actions that are performed by the agent on the
    *   server or within the agent's controlled environment. Function: A tool executed on the client-side, where the
    *   agent generates parameters for a predefined function, and the client executes the logic. Client-side operations
    *   are actions taken on the user's end or within the client application. Datastore: A tool used by the agent to
    *   access and query structured or unstructured external data for retrieval-augmented tasks or knowledge updates.
    */
  val GenAiToolType: AttributeKey[String] =
    AttributeKey("gen_ai.tool.type")

  /** The number of input tokens written to a provider-managed cache.
    *
    * @note
    *   <p> The value SHOULD be included in `gen_ai.usage.input_tokens`.
    */
  val GenAiUsageCacheCreationInputTokens: AttributeKey[Long] =
    AttributeKey("gen_ai.usage.cache_creation.input_tokens")

  /** The number of input tokens served from a provider-managed cache.
    *
    * @note
    *   <p> The value SHOULD be included in `gen_ai.usage.input_tokens`.
    */
  val GenAiUsageCacheReadInputTokens: AttributeKey[Long] =
    AttributeKey("gen_ai.usage.cache_read.input_tokens")

  /** Deprecated, use `gen_ai.usage.output_tokens` instead.
    */
  @deprecated("Replaced by `gen_ai.usage.output_tokens`.", "")
  val GenAiUsageCompletionTokens: AttributeKey[Long] =
    AttributeKey("gen_ai.usage.completion_tokens")

  /** The number of tokens used in the GenAI input (prompt).
    *
    * @note
    *   <p> This value SHOULD include all types of input tokens, including cached tokens. Instrumentations SHOULD make a
    *   best effort to populate this value, using a total provided by the provider when available or, depending on the
    *   provider API, by summing different token types parsed from the provider output.
    */
  val GenAiUsageInputTokens: AttributeKey[Long] =
    AttributeKey("gen_ai.usage.input_tokens")

  /** The number of tokens used in the GenAI response (completion).
    */
  val GenAiUsageOutputTokens: AttributeKey[Long] =
    AttributeKey("gen_ai.usage.output_tokens")

  /** Deprecated, use `gen_ai.usage.input_tokens` instead.
    */
  @deprecated("Replaced by `gen_ai.usage.input_tokens`.", "")
  val GenAiUsagePromptTokens: AttributeKey[Long] =
    AttributeKey("gen_ai.usage.prompt_tokens")

  /** Values for [[GenAiOpenaiRequestResponseFormat]].
    */
  @deprecated("Replaced by `gen_ai.output.type`.", "")
  abstract class GenAiOpenaiRequestResponseFormatValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object GenAiOpenaiRequestResponseFormatValue {
    implicit val attributeFromGenAiOpenaiRequestResponseFormatValue
        : Attribute.From[GenAiOpenaiRequestResponseFormatValue, String] = _.value

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
  @deprecated("Replaced by `openai.request.service_tier`.", "")
  abstract class GenAiOpenaiRequestServiceTierValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object GenAiOpenaiRequestServiceTierValue {
    implicit val attributeFromGenAiOpenaiRequestServiceTierValue
        : Attribute.From[GenAiOpenaiRequestServiceTierValue, String] = _.value

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
    implicit val attributeFromGenAiOperationNameValue: Attribute.From[GenAiOperationNameValue, String] = _.value

    /** Chat completion operation such as <a href="https://platform.openai.com/docs/api-reference/chat">OpenAI Chat
      * API</a>
      */
    case object Chat extends GenAiOperationNameValue("chat")

    /** Multimodal content generation operation such as <a href="https://ai.google.dev/api/generate-content">Gemini
      * Generate Content</a>
      */
    case object GenerateContent extends GenAiOperationNameValue("generate_content")

    /** Text completions operation such as <a href="https://platform.openai.com/docs/api-reference/completions">OpenAI
      * Completions API (Legacy)</a>
      */
    case object TextCompletion extends GenAiOperationNameValue("text_completion")

    /** Embeddings operation such as <a href="https://platform.openai.com/docs/api-reference/embeddings/create">OpenAI
      * Create embeddings API</a>
      */
    case object Embeddings extends GenAiOperationNameValue("embeddings")

    /** Retrieval operation such as <a href="https://platform.openai.com/docs/api-reference/vector-stores/search">OpenAI
      * Search Vector Store API</a>
      */
    case object Retrieval extends GenAiOperationNameValue("retrieval")

    /** Create GenAI agent
      */
    case object CreateAgent extends GenAiOperationNameValue("create_agent")

    /** Invoke GenAI agent
      */
    case object InvokeAgent extends GenAiOperationNameValue("invoke_agent")

    /** Execute a tool
      */
    case object ExecuteTool extends GenAiOperationNameValue("execute_tool")
  }

  /** Values for [[GenAiOutputType]].
    */
  abstract class GenAiOutputTypeValue(val value: String)
  object GenAiOutputTypeValue {
    implicit val attributeFromGenAiOutputTypeValue: Attribute.From[GenAiOutputTypeValue, String] = _.value

    /** Plain text
      */
    case object Text extends GenAiOutputTypeValue("text")

    /** JSON object with known or unknown schema
      */
    case object Json extends GenAiOutputTypeValue("json")

    /** Image
      */
    case object Image extends GenAiOutputTypeValue("image")

    /** Speech
      */
    case object Speech extends GenAiOutputTypeValue("speech")
  }

  /** Values for [[GenAiProviderName]].
    */
  abstract class GenAiProviderNameValue(val value: String)
  object GenAiProviderNameValue {
    implicit val attributeFromGenAiProviderNameValue: Attribute.From[GenAiProviderNameValue, String] = _.value

    /** <a href="https://openai.com/">OpenAI</a>
      */
    case object Openai extends GenAiProviderNameValue("openai")

    /** Any Google generative AI endpoint
      */
    case object GcpGenAi extends GenAiProviderNameValue("gcp.gen_ai")

    /** <a href="https://cloud.google.com/vertex-ai">Vertex AI</a>
      */
    case object GcpVertexAi extends GenAiProviderNameValue("gcp.vertex_ai")

    /** <a href="https://cloud.google.com/products/gemini">Gemini</a>
      */
    case object GcpGemini extends GenAiProviderNameValue("gcp.gemini")

    /** <a href="https://www.anthropic.com/">Anthropic</a>
      */
    case object Anthropic extends GenAiProviderNameValue("anthropic")

    /** <a href="https://cohere.com/">Cohere</a>
      */
    case object Cohere extends GenAiProviderNameValue("cohere")

    /** Azure AI Inference
      */
    case object AzureAiInference extends GenAiProviderNameValue("azure.ai.inference")

    /** <a href="https://azure.microsoft.com/products/ai-services/openai-service/">Azure OpenAI</a>
      */
    case object AzureAiOpenai extends GenAiProviderNameValue("azure.ai.openai")

    /** <a href="https://www.ibm.com/products/watsonx-ai">IBM Watsonx AI</a>
      */
    case object IbmWatsonxAi extends GenAiProviderNameValue("ibm.watsonx.ai")

    /** <a href="https://aws.amazon.com/bedrock">AWS Bedrock</a>
      */
    case object AwsBedrock extends GenAiProviderNameValue("aws.bedrock")

    /** <a href="https://www.perplexity.ai/">Perplexity</a>
      */
    case object Perplexity extends GenAiProviderNameValue("perplexity")

    /** <a href="https://x.ai/">xAI</a>
      */
    case object XAi extends GenAiProviderNameValue("x_ai")

    /** <a href="https://www.deepseek.com/">DeepSeek</a>
      */
    case object Deepseek extends GenAiProviderNameValue("deepseek")

    /** <a href="https://groq.com/">Groq</a>
      */
    case object Groq extends GenAiProviderNameValue("groq")

    /** <a href="https://mistral.ai/">Mistral AI</a>
      */
    case object MistralAi extends GenAiProviderNameValue("mistral_ai")
  }

  /** Values for [[GenAiSystem]].
    */
  @deprecated("Replaced by `gen_ai.provider.name`.", "")
  abstract class GenAiSystemValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object GenAiSystemValue {
    implicit val attributeFromGenAiSystemValue: Attribute.From[GenAiSystemValue, String] = _.value

    /** OpenAI
      */
    case object Openai extends GenAiSystemValue("openai")

    /** Any Google generative AI endpoint
      */
    case object GcpGenAi extends GenAiSystemValue("gcp.gen_ai")

    /** Vertex AI
      */
    case object GcpVertexAi extends GenAiSystemValue("gcp.vertex_ai")

    /** Gemini
      */
    case object GcpGemini extends GenAiSystemValue("gcp.gemini")

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

    /** Azure AI Inference
      */
    case object AzureAiInference extends GenAiSystemValue("azure.ai.inference")

    /** Azure OpenAI
      */
    case object AzureAiOpenai extends GenAiSystemValue("azure.ai.openai")

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
    implicit val attributeFromGenAiTokenTypeValue: Attribute.From[GenAiTokenTypeValue, String] = _.value

    /** Input tokens (prompt, input, etc.)
      */
    case object Input extends GenAiTokenTypeValue("input")

    /** Output tokens (completion, response, etc.)
      */
    case object Completion extends GenAiTokenTypeValue("output")

    /** Output tokens (completion, response, etc.)
      */
    case object Output extends GenAiTokenTypeValue("output")
  }

}
