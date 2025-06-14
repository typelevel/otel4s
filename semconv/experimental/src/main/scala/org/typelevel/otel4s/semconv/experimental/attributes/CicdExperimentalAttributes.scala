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
object CicdExperimentalAttributes {

  /** The kind of action a pipeline run is performing.
    */
  val CicdPipelineActionName: AttributeKey[String] =
    AttributeKey("cicd.pipeline.action.name")

  /** The human readable name of the pipeline within a CI/CD system.
    */
  val CicdPipelineName: AttributeKey[String] =
    AttributeKey("cicd.pipeline.name")

  /** The result of a pipeline run.
    */
  val CicdPipelineResult: AttributeKey[String] =
    AttributeKey("cicd.pipeline.result")

  /** The unique identifier of a pipeline run within a CI/CD system.
    */
  val CicdPipelineRunId: AttributeKey[String] =
    AttributeKey("cicd.pipeline.run.id")

  /** The pipeline run goes through these states during its lifecycle.
    */
  val CicdPipelineRunState: AttributeKey[String] =
    AttributeKey("cicd.pipeline.run.state")

  /** The <a href="https://wikipedia.org/wiki/URL">URL</a> of the pipeline run, providing the complete address in order
    * to locate and identify the pipeline run.
    */
  val CicdPipelineRunUrlFull: AttributeKey[String] =
    AttributeKey("cicd.pipeline.run.url.full")

  /** The human readable name of a task within a pipeline. Task here most closely aligns with a <a
    * href="https://wikipedia.org/wiki/Pipeline_(computing)">computing process</a> in a pipeline. Other terms for tasks
    * include commands, steps, and procedures.
    */
  val CicdPipelineTaskName: AttributeKey[String] =
    AttributeKey("cicd.pipeline.task.name")

  /** The unique identifier of a task run within a pipeline.
    */
  val CicdPipelineTaskRunId: AttributeKey[String] =
    AttributeKey("cicd.pipeline.task.run.id")

  /** The result of a task run.
    */
  val CicdPipelineTaskRunResult: AttributeKey[String] =
    AttributeKey("cicd.pipeline.task.run.result")

  /** The <a href="https://wikipedia.org/wiki/URL">URL</a> of the pipeline task run, providing the complete address in
    * order to locate and identify the pipeline task run.
    */
  val CicdPipelineTaskRunUrlFull: AttributeKey[String] =
    AttributeKey("cicd.pipeline.task.run.url.full")

  /** The type of the task within a pipeline.
    */
  val CicdPipelineTaskType: AttributeKey[String] =
    AttributeKey("cicd.pipeline.task.type")

  /** The name of a component of the CICD system.
    */
  val CicdSystemComponent: AttributeKey[String] =
    AttributeKey("cicd.system.component")

  /** The unique identifier of a worker within a CICD system.
    */
  val CicdWorkerId: AttributeKey[String] =
    AttributeKey("cicd.worker.id")

  /** The name of a worker within a CICD system.
    */
  val CicdWorkerName: AttributeKey[String] =
    AttributeKey("cicd.worker.name")

  /** The state of a CICD worker / agent.
    */
  val CicdWorkerState: AttributeKey[String] =
    AttributeKey("cicd.worker.state")

  /** The <a href="https://wikipedia.org/wiki/URL">URL</a> of the worker, providing the complete address in order to
    * locate and identify the worker.
    */
  val CicdWorkerUrlFull: AttributeKey[String] =
    AttributeKey("cicd.worker.url.full")

  /** Values for [[CicdPipelineActionName]].
    */
  abstract class CicdPipelineActionNameValue(val value: String)
  object CicdPipelineActionNameValue {

    /** The pipeline run is executing a build.
      */
    case object Build extends CicdPipelineActionNameValue("BUILD")

    /** The pipeline run is executing.
      */
    case object Run extends CicdPipelineActionNameValue("RUN")

    /** The pipeline run is executing a sync.
      */
    case object Sync extends CicdPipelineActionNameValue("SYNC")
  }

  /** Values for [[CicdPipelineResult]].
    */
  abstract class CicdPipelineResultValue(val value: String)
  object CicdPipelineResultValue {

    /** The pipeline run finished successfully.
      */
    case object Success extends CicdPipelineResultValue("success")

    /** The pipeline run did not finish successfully, eg. due to a compile error or a failing test. Such failures are
      * usually detected by non-zero exit codes of the tools executed in the pipeline run.
      */
    case object Failure extends CicdPipelineResultValue("failure")

    /** The pipeline run failed due to an error in the CICD system, eg. due to the worker being killed.
      */
    case object Error extends CicdPipelineResultValue("error")

    /** A timeout caused the pipeline run to be interrupted.
      */
    case object Timeout extends CicdPipelineResultValue("timeout")

    /** The pipeline run was cancelled, eg. by a user manually cancelling the pipeline run.
      */
    case object Cancellation extends CicdPipelineResultValue("cancellation")

    /** The pipeline run was skipped, eg. due to a precondition not being met.
      */
    case object Skip extends CicdPipelineResultValue("skip")
  }

  /** Values for [[CicdPipelineRunState]].
    */
  abstract class CicdPipelineRunStateValue(val value: String)
  object CicdPipelineRunStateValue {

    /** The run pending state spans from the event triggering the pipeline run until the execution of the run starts
      * (eg. time spent in a queue, provisioning agents, creating run resources).
      */
    case object Pending extends CicdPipelineRunStateValue("pending")

    /** The executing state spans the execution of any run tasks (eg. build, test).
      */
    case object Executing extends CicdPipelineRunStateValue("executing")

    /** The finalizing state spans from when the run has finished executing (eg. cleanup of run resources).
      */
    case object Finalizing extends CicdPipelineRunStateValue("finalizing")
  }

  /** Values for [[CicdPipelineTaskRunResult]].
    */
  abstract class CicdPipelineTaskRunResultValue(val value: String)
  object CicdPipelineTaskRunResultValue {

    /** The task run finished successfully.
      */
    case object Success extends CicdPipelineTaskRunResultValue("success")

    /** The task run did not finish successfully, eg. due to a compile error or a failing test. Such failures are
      * usually detected by non-zero exit codes of the tools executed in the task run.
      */
    case object Failure extends CicdPipelineTaskRunResultValue("failure")

    /** The task run failed due to an error in the CICD system, eg. due to the worker being killed.
      */
    case object Error extends CicdPipelineTaskRunResultValue("error")

    /** A timeout caused the task run to be interrupted.
      */
    case object Timeout extends CicdPipelineTaskRunResultValue("timeout")

    /** The task run was cancelled, eg. by a user manually cancelling the task run.
      */
    case object Cancellation extends CicdPipelineTaskRunResultValue("cancellation")

    /** The task run was skipped, eg. due to a precondition not being met.
      */
    case object Skip extends CicdPipelineTaskRunResultValue("skip")
  }

  /** Values for [[CicdPipelineTaskType]].
    */
  abstract class CicdPipelineTaskTypeValue(val value: String)
  object CicdPipelineTaskTypeValue {

    /** build
      */
    case object Build extends CicdPipelineTaskTypeValue("build")

    /** test
      */
    case object Test extends CicdPipelineTaskTypeValue("test")

    /** deploy
      */
    case object Deploy extends CicdPipelineTaskTypeValue("deploy")
  }

  /** Values for [[CicdWorkerState]].
    */
  abstract class CicdWorkerStateValue(val value: String)
  object CicdWorkerStateValue {

    /** The worker is not performing work for the CICD system. It is available to the CICD system to perform work on
      * (online / idle).
      */
    case object Available extends CicdWorkerStateValue("available")

    /** The worker is performing work for the CICD system.
      */
    case object Busy extends CicdWorkerStateValue("busy")

    /** The worker is not available to the CICD system (disconnected / down).
      */
    case object Offline extends CicdWorkerStateValue("offline")
  }

}
