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
object AppExperimentalAttributes {

  /** A unique identifier representing the installation of an application on a specific device
    *
    * @note
    *   <p> Its value SHOULD persist across launches of the same application installation, including through application
    *   upgrades. It SHOULD change if the application is uninstalled or if all applications of the vendor are
    *   uninstalled. Additionally, users might be able to reset this value (e.g. by clearing application data). If an
    *   app is installed multiple times on the same device (e.g. in different accounts on Android), each
    *   `app.installation.id` SHOULD have a different value. If multiple OpenTelemetry SDKs are used within the same
    *   application, they SHOULD use the same value for `app.installation.id`. Hardware IDs (e.g. serial number, IMEI,
    *   MAC address) MUST NOT be used as the `app.installation.id`. <p> For iOS, this value SHOULD be equal to the <a
    *   href="https://developer.apple.com/documentation/uikit/uidevice/identifierforvendor">vendor identifier</a>. <p>
    *   For Android, examples of `app.installation.id` implementations include: <ul> <li><a
    *   href="https://firebase.google.com/docs/projects/manage-installations">Firebase Installation ID</a>. <li>A
    *   globally unique UUID which is persisted across sessions in your application. <li><a
    *   href="https://developer.android.com/identity/app-set-id">App set ID</a>. <li><a
    *   href="https://developer.android.com/reference/android/provider/Settings.Secure#ANDROID_ID">`Settings.getString(Settings.Secure.ANDROID_ID)`</a>.
    *   </ul> <p> More information about Android identifier best practices can be found <a
    *   href="https://developer.android.com/training/articles/user-data-ids">here</a>.
    */
  val AppInstallationId: AttributeKey[String] =
    AttributeKey("app.installation.id")

  /** The x (horizontal) coordinate of a screen coordinate, in screen pixels.
    */
  val AppScreenCoordinateX: AttributeKey[Long] =
    AttributeKey("app.screen.coordinate.x")

  /** The y (vertical) component of a screen coordinate, in screen pixels.
    */
  val AppScreenCoordinateY: AttributeKey[Long] =
    AttributeKey("app.screen.coordinate.y")

  /** An identifier that uniquely differentiates this widget from other widgets in the same application.
    *
    * @note
    *   <p> A widget is an application component, typically an on-screen visual GUI element.
    */
  val AppWidgetId: AttributeKey[String] =
    AttributeKey("app.widget.id")

  /** The name of an application widget.
    *
    * @note
    *   <p> A widget is an application component, typically an on-screen visual GUI element.
    */
  val AppWidgetName: AttributeKey[String] =
    AttributeKey("app.widget.name")

}
