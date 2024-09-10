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

// DO NOT EDIT, this is an Auto-generated file from buildscripts/templates/registry/otel4s/attributes/experimental/SemanticAttributes.scala.j2
object OsExperimentalAttributes {

  /** Unique identifier for a particular build or compilation of the operating system.
    */
  val OsBuildId: AttributeKey[String] =
    AttributeKey("os.build_id")

  /** Human readable (not intended to be parsed) OS version information, like e.g. reported by `ver` or `lsb_release -a`
    * commands.
    */
  val OsDescription: AttributeKey[String] =
    AttributeKey("os.description")

  /** Human readable operating system name.
    */
  val OsName: AttributeKey[String] =
    AttributeKey("os.name")

  /** The operating system type.
    */
  val OsType: AttributeKey[String] =
    AttributeKey("os.type")

  /** The version string of the operating system as defined in <a
    * href="/docs/resource/README.md#version-attributes">Version Attributes</a>.
    */
  val OsVersion: AttributeKey[String] =
    AttributeKey("os.version")

  /** Values for [[OsType]].
    */
  abstract class OsTypeValue(val value: String)
  object OsTypeValue {

    /** Microsoft Windows
      */
    case object Windows extends OsTypeValue("windows")

    /** Linux
      */
    case object Linux extends OsTypeValue("linux")

    /** Apple Darwin
      */
    case object Darwin extends OsTypeValue("darwin")

    /** FreeBSD
      */
    case object Freebsd extends OsTypeValue("freebsd")

    /** NetBSD
      */
    case object Netbsd extends OsTypeValue("netbsd")

    /** OpenBSD
      */
    case object Openbsd extends OsTypeValue("openbsd")

    /** DragonFly BSD
      */
    case object Dragonflybsd extends OsTypeValue("dragonflybsd")

    /** HP-UX (Hewlett Packard Unix)
      */
    case object Hpux extends OsTypeValue("hpux")

    /** AIX (Advanced Interactive eXecutive)
      */
    case object Aix extends OsTypeValue("aix")

    /** SunOS, Oracle Solaris
      */
    case object Solaris extends OsTypeValue("solaris")

    /** IBM z/OS
      */
    case object ZOs extends OsTypeValue("z_os")
  }

}
