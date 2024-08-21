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
object CicdExperimentalAttributes {

  /** The human readable name of the pipeline within a CI/CD system.
    */
  val CicdPipelineName: AttributeKey[String] = string("cicd.pipeline.name")

  /** The unique identifier of a pipeline run within a CI/CD system.
    */
  val CicdPipelineRunId: AttributeKey[String] = string("cicd.pipeline.run.id")

  /** The human readable name of a task within a pipeline. Task here most
    * closely aligns with a <a
    * href="https://en.wikipedia.org/wiki/Pipeline_(computing)">computing
    * process</a> in a pipeline. Other terms for tasks include commands, steps,
    * and procedures.
    */
  val CicdPipelineTaskName: AttributeKey[String] = string(
    "cicd.pipeline.task.name"
  )

  /** The unique identifier of a task run within a pipeline.
    */
  val CicdPipelineTaskRunId: AttributeKey[String] = string(
    "cicd.pipeline.task.run.id"
  )

  /** The <a href="https://en.wikipedia.org/wiki/URL">URL</a> of the pipeline
    * run providing the complete address in order to locate and identify the
    * pipeline run.
    */
  val CicdPipelineTaskRunUrlFull: AttributeKey[String] = string(
    "cicd.pipeline.task.run.url.full"
  )

  /** The type of the task within a pipeline.
    */
  val CicdPipelineTaskType: AttributeKey[String] = string(
    "cicd.pipeline.task.type"
  )
  // Enum definitions

  /** Values for [[CicdPipelineTaskType]].
    */
  abstract class CicdPipelineTaskTypeValue(val value: String)
  object CicdPipelineTaskTypeValue {

    /** build. */
    case object Build extends CicdPipelineTaskTypeValue("build")

    /** test. */
    case object Test extends CicdPipelineTaskTypeValue("test")

    /** deploy. */
    case object Deploy extends CicdPipelineTaskTypeValue("deploy")
  }

}
