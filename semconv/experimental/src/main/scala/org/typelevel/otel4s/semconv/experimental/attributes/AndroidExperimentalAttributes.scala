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
object AndroidExperimentalAttributes {

  /** Uniquely identifies the framework API revision offered by a version (`os.version`) of the android operating
    * system. More information can be found <a
    * href="https://developer.android.com/guide/topics/manifest/uses-sdk-element#ApiLevels">here</a>.
    */
  val AndroidOsApiLevel: AttributeKey[String] =
    AttributeKey("android.os.api_level")

  /** Deprecated use the `device.app.lifecycle` event definition including `android.state` as a payload field instead.
    * <p>
    * @note
    *   <p> The Android lifecycle states are defined in <a
    *   href="https://developer.android.com/guide/components/activities/activity-lifecycle#lc">Activity lifecycle
    *   callbacks</a>, and from which the `OS identifiers` are derived.
    */
  val AndroidState: AttributeKey[String] =
    AttributeKey("android.state")

  /** Values for [[AndroidState]].
    */
  abstract class AndroidStateValue(val value: String)
  object AndroidStateValue {

    /** Any time before Activity.onResume() or, if the app has no Activity, Context.startService() has been called in
      * the app for the first time.
      */
    case object Created extends AndroidStateValue("created")

    /** Any time after Activity.onPause() or, if the app has no Activity, Context.stopService() has been called when the
      * app was in the foreground state.
      */
    case object Background extends AndroidStateValue("background")

    /** Any time after Activity.onResume() or, if the app has no Activity, Context.startService() has been called when
      * the app was in either the created or background states.
      */
    case object Foreground extends AndroidStateValue("foreground")
  }

}
