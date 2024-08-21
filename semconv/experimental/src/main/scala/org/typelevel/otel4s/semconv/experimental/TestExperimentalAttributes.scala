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
object TestExperimentalAttributes {

  /** The fully qualified human readable name of the <a
    * href="https://en.wikipedia.org/wiki/Test_case">test case</a>.
    */
  val TestCaseName: AttributeKey[String] = string("test.case.name")

  /** The status of the actual test case result from test execution.
    */
  val TestCaseResultStatus: AttributeKey[String] = string(
    "test.case.result.status"
  )

  /** The human readable name of a <a
    * href="https://en.wikipedia.org/wiki/Test_suite">test suite</a>.
    */
  val TestSuiteName: AttributeKey[String] = string("test.suite.name")

  /** The status of the test suite run.
    */
  val TestSuiteRunStatus: AttributeKey[String] = string("test.suite.run.status")
  // Enum definitions

  /** Values for [[TestCaseResultStatus]].
    */
  abstract class TestCaseResultStatusValue(val value: String)
  object TestCaseResultStatusValue {

    /** pass. */
    case object Pass extends TestCaseResultStatusValue("pass")

    /** fail. */
    case object Fail extends TestCaseResultStatusValue("fail")
  }

  /** Values for [[TestSuiteRunStatus]].
    */
  abstract class TestSuiteRunStatusValue(val value: String)
  object TestSuiteRunStatusValue {

    /** success. */
    case object Success extends TestSuiteRunStatusValue("success")

    /** failure. */
    case object Failure extends TestSuiteRunStatusValue("failure")

    /** skipped. */
    case object Skipped extends TestSuiteRunStatusValue("skipped")

    /** aborted. */
    case object Aborted extends TestSuiteRunStatusValue("aborted")

    /** timed_out. */
    case object TimedOut extends TestSuiteRunStatusValue("timed_out")

    /** in_progress. */
    case object InProgress extends TestSuiteRunStatusValue("in_progress")
  }

}
