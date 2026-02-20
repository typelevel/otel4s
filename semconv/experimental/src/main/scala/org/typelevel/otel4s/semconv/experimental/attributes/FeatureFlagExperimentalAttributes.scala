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
object FeatureFlagExperimentalAttributes {

  /** The unique identifier for the flag evaluation context. For example, the targeting key.
    */
  val FeatureFlagContextId: AttributeKey[String] =
    AttributeKey("feature_flag.context.id")

  /** A message providing more detail about an error that occurred during feature flag evaluation in human-readable
    * form.
    */
  val FeatureFlagErrorMessage: AttributeKey[String] =
    AttributeKey("feature_flag.error.message")

  /** Deprecated, use `feature_flag.error.message` instead.
    */
  @deprecated("Replaced by `feature_flag.error.message`.", "")
  val FeatureFlagEvaluationErrorMessage: AttributeKey[String] =
    AttributeKey("feature_flag.evaluation.error.message")

  /** Deprecated, use `feature_flag.result.reason` instead.
    */
  @deprecated("Replaced by `feature_flag.result.reason`.", "")
  val FeatureFlagEvaluationReason: AttributeKey[String] =
    AttributeKey("feature_flag.evaluation.reason")

  /** The lookup key of the feature flag.
    */
  val FeatureFlagKey: AttributeKey[String] =
    AttributeKey("feature_flag.key")

  /** Identifies the feature flag provider.
    */
  val FeatureFlagProviderName: AttributeKey[String] =
    AttributeKey("feature_flag.provider.name")

  /** The reason code which shows how a feature flag value was determined.
    */
  val FeatureFlagResultReason: AttributeKey[String] =
    AttributeKey("feature_flag.result.reason")

  /** A semantic identifier for an evaluated flag value.
    *
    * @note
    *   <p> A semantic identifier, commonly referred to as a variant, provides a means for referring to a value without
    *   including the value itself. This can provide additional context for understanding the meaning behind a value.
    *   For example, the variant `red` maybe be used for the value `#c05543`.
    */
  val FeatureFlagResultVariant: AttributeKey[String] =
    AttributeKey("feature_flag.result.variant")

  /** The identifier of the <a href="https://openfeature.dev/specification/glossary/#flag-set">flag set</a> to which the
    * feature flag belongs.
    */
  val FeatureFlagSetId: AttributeKey[String] =
    AttributeKey("feature_flag.set.id")

  /** Deprecated, use `feature_flag.result.variant` instead.
    */
  @deprecated("Replaced by `feature_flag.result.variant`.", "")
  val FeatureFlagVariant: AttributeKey[String] =
    AttributeKey("feature_flag.variant")

  /** The version of the ruleset used during the evaluation. This may be any stable value which uniquely identifies the
    * ruleset.
    */
  val FeatureFlagVersion: AttributeKey[String] =
    AttributeKey("feature_flag.version")

  /** Values for [[FeatureFlagEvaluationReason]].
    */
  @deprecated("Replaced by `feature_flag.result.reason`.", "")
  abstract class FeatureFlagEvaluationReasonValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object FeatureFlagEvaluationReasonValue {
    implicit val attributeFromFeatureFlagEvaluationReasonValue
        : Attribute.From[FeatureFlagEvaluationReasonValue, String] = _.value

    /** The resolved value is static (no dynamic evaluation).
      */
    case object Static extends FeatureFlagEvaluationReasonValue("static")

    /** The resolved value fell back to a pre-configured value (no dynamic evaluation occurred or dynamic evaluation
      * yielded no result).
      */
    case object Default extends FeatureFlagEvaluationReasonValue("default")

    /** The resolved value was the result of a dynamic evaluation, such as a rule or specific user-targeting.
      */
    case object TargetingMatch extends FeatureFlagEvaluationReasonValue("targeting_match")

    /** The resolved value was the result of pseudorandom assignment.
      */
    case object Split extends FeatureFlagEvaluationReasonValue("split")

    /** The resolved value was retrieved from cache.
      */
    case object Cached extends FeatureFlagEvaluationReasonValue("cached")

    /** The resolved value was the result of the flag being disabled in the management system.
      */
    case object Disabled extends FeatureFlagEvaluationReasonValue("disabled")

    /** The reason for the resolved value could not be determined.
      */
    case object Unknown extends FeatureFlagEvaluationReasonValue("unknown")

    /** The resolved value is non-authoritative or possibly out of date
      */
    case object Stale extends FeatureFlagEvaluationReasonValue("stale")

    /** The resolved value was the result of an error.
      */
    case object Error extends FeatureFlagEvaluationReasonValue("error")
  }

  /** Values for [[FeatureFlagResultReason]].
    */
  abstract class FeatureFlagResultReasonValue(val value: String)
  object FeatureFlagResultReasonValue {
    implicit val attributeFromFeatureFlagResultReasonValue: Attribute.From[FeatureFlagResultReasonValue, String] =
      _.value

    /** The resolved value is static (no dynamic evaluation).
      */
    case object Static extends FeatureFlagResultReasonValue("static")

    /** The resolved value fell back to a pre-configured value (no dynamic evaluation occurred or dynamic evaluation
      * yielded no result).
      */
    case object Default extends FeatureFlagResultReasonValue("default")

    /** The resolved value was the result of a dynamic evaluation, such as a rule or specific user-targeting.
      */
    case object TargetingMatch extends FeatureFlagResultReasonValue("targeting_match")

    /** The resolved value was the result of pseudorandom assignment.
      */
    case object Split extends FeatureFlagResultReasonValue("split")

    /** The resolved value was retrieved from cache.
      */
    case object Cached extends FeatureFlagResultReasonValue("cached")

    /** The resolved value was the result of the flag being disabled in the management system.
      */
    case object Disabled extends FeatureFlagResultReasonValue("disabled")

    /** The reason for the resolved value could not be determined.
      */
    case object Unknown extends FeatureFlagResultReasonValue("unknown")

    /** The resolved value is non-authoritative or possibly out of date
      */
    case object Stale extends FeatureFlagResultReasonValue("stale")

    /** The resolved value was the result of an error.
      */
    case object Error extends FeatureFlagResultReasonValue("error")
  }

}
