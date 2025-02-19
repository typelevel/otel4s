/*
 * Copyright 2024 Typelevel
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
package experimental
package metrics

import cats.effect.Resource
import org.typelevel.otel4s.metrics._
import org.typelevel.otel4s.semconv.experimental.attributes._

// DO NOT EDIT, this is an Auto-generated file from buildscripts/templates/registry/otel4s/metrics/SemanticMetrics.scala.j2
object VcsExperimentalMetrics {

  val specs: List[MetricSpec] = List(
    ChangeCount,
    ChangeDuration,
    ChangeTimeToApproval,
    ChangeTimeToMerge,
    ContributorCount,
    RefCount,
    RefLinesDelta,
    RefRevisionsDelta,
    RefTime,
    RepositoryCount,
  )

  /** The number of changes (pull requests/merge requests/changelists) in a repository, categorized by their state (e.g.
    * open or merged)
    */
  object ChangeCount extends MetricSpec {

    val name: String = "vcs.change.count"
    val description: String =
      "The number of changes (pull requests/merge requests/changelists) in a repository, categorized by their state (e.g. open or merged)"
    val unit: String = "{change}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The state of the change (pull request/merge request/changelist).
        */
      val vcsChangeState: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsChangeState,
          List(
            "open",
            "closed",
            "merged",
          ),
          Requirement.required,
          Stability.development
        )

      /** The human readable name of the repository. It SHOULD NOT include any additional identifier like Group/SubGroup
        * in GitLab or organization in GitHub. <p>
        * @note
        *   <p> Due to it only being the name, it can clash with forks of the same repository if collecting telemetry
        *   across multiple orgs or groups in the same backends.
        */
      val vcsRepositoryName: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsRepositoryName,
          List(
            "semantic-conventions",
            "my-cool-repo",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The <a
        * href="https://support.google.com/webmasters/answer/10347851?hl=en#:~:text=A%20canonical%20URL%20is%20the,Google%20chooses%20one%20as%20canonical.">canonical
        * URL</a> of the repository providing the complete HTTP(S) address in order to locate and identify the
        * repository through a browser. <p>
        * @note
        *   <p> In Git Version Control Systems, the canonical URL SHOULD NOT include the `.git` extension.
        */
      val vcsRepositoryUrlFull: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsRepositoryUrlFull,
          List(
            "https://github.com/opentelemetry/open-telemetry-collector-contrib",
            "https://gitlab.com/my-org/my-project/my-projects-project/repo",
          ),
          Requirement.required,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          vcsChangeState,
          vcsRepositoryName,
          vcsRepositoryUrlFull,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[UpDownCounter[F, A]] =
      Meter[F]
        .upDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableUpDownCounter] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** The time duration a change (pull request/merge request/changelist) has been in a given state.
    */
  object ChangeDuration extends MetricSpec {

    val name: String = "vcs.change.duration"
    val description: String =
      "The time duration a change (pull request/merge request/changelist) has been in a given state."
    val unit: String = "s"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The state of the change (pull request/merge request/changelist).
        */
      val vcsChangeState: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsChangeState,
          List(
            "open",
            "closed",
            "merged",
          ),
          Requirement.required,
          Stability.development
        )

      /** The name of the <a href="https://git-scm.com/docs/gitglossary#def_ref">reference</a> such as
        * <strong>branch</strong> or <strong>tag</strong> in the repository. <p>
        * @note
        *   <p> `head` refers to where you are right now; the current reference at a given time.
        */
      val vcsRefHeadName: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsRefHeadName,
          List(
            "my-feature-branch",
            "tag-1-test",
          ),
          Requirement.required,
          Stability.development
        )

      /** The human readable name of the repository. It SHOULD NOT include any additional identifier like Group/SubGroup
        * in GitLab or organization in GitHub. <p>
        * @note
        *   <p> Due to it only being the name, it can clash with forks of the same repository if collecting telemetry
        *   across multiple orgs or groups in the same backends.
        */
      val vcsRepositoryName: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsRepositoryName,
          List(
            "semantic-conventions",
            "my-cool-repo",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The <a
        * href="https://support.google.com/webmasters/answer/10347851?hl=en#:~:text=A%20canonical%20URL%20is%20the,Google%20chooses%20one%20as%20canonical.">canonical
        * URL</a> of the repository providing the complete HTTP(S) address in order to locate and identify the
        * repository through a browser. <p>
        * @note
        *   <p> In Git Version Control Systems, the canonical URL SHOULD NOT include the `.git` extension.
        */
      val vcsRepositoryUrlFull: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsRepositoryUrlFull,
          List(
            "https://github.com/opentelemetry/open-telemetry-collector-contrib",
            "https://gitlab.com/my-org/my-project/my-projects-project/repo",
          ),
          Requirement.required,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          vcsChangeState,
          vcsRefHeadName,
          vcsRepositoryName,
          vcsRepositoryUrlFull,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Gauge[F, A]] =
      Meter[F]
        .gauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** The amount of time since its creation it took a change (pull request/merge request/changelist) to get the first
    * approval.
    */
  object ChangeTimeToApproval extends MetricSpec {

    val name: String = "vcs.change.time_to_approval"
    val description: String =
      "The amount of time since its creation it took a change (pull request/merge request/changelist) to get the first approval."
    val unit: String = "s"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The name of the <a href="https://git-scm.com/docs/gitglossary#def_ref">reference</a> such as
        * <strong>branch</strong> or <strong>tag</strong> in the repository. <p>
        * @note
        *   <p> `base` refers to the starting point of a change. For example, `main` would be the base reference of type
        *   branch if you've created a new reference of type branch from it and created new commits.
        */
      val vcsRefBaseName: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsRefBaseName,
          List(
            "my-feature-branch",
            "tag-1-test",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The revision, literally <a href="https://www.merriam-webster.com/dictionary/revision">revised version</a>, The
        * revision most often refers to a commit object in Git, or a revision number in SVN. <p>
        * @note
        *   <p> `base` refers to the starting point of a change. For example, `main` would be the base reference of type
        *   branch if you've created a new reference of type branch from it and created new commits. The revision can be
        *   a full <a href="https://nvlpubs.nist.gov/nistpubs/FIPS/NIST.FIPS.186-5.pdf">hash value (see glossary)</a>,
        *   of the recorded change to a ref within a repository pointing to a commit <a
        *   href="https://git-scm.com/docs/git-commit">commit</a> object. It does not necessarily have to be a hash; it
        *   can simply define a <a href="https://svnbook.red-bean.com/en/1.7/svn.tour.revs.specifiers.html">revision
        *   number</a> which is an integer that is monotonically increasing. In cases where it is identical to the
        *   `ref.base.name`, it SHOULD still be included. It is up to the implementer to decide which value to set as
        *   the revision based on the VCS system and situational context.
        */
      val vcsRefBaseRevision: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsRefBaseRevision,
          List(
            "9d59409acf479dfa0df1aa568182e43e43df8bbe28d60fcf2bc52e30068802cc",
            "main",
            "123",
            "HEAD",
          ),
          Requirement.optIn,
          Stability.development
        )

      /** The name of the <a href="https://git-scm.com/docs/gitglossary#def_ref">reference</a> such as
        * <strong>branch</strong> or <strong>tag</strong> in the repository. <p>
        * @note
        *   <p> `head` refers to where you are right now; the current reference at a given time.
        */
      val vcsRefHeadName: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsRefHeadName,
          List(
            "my-feature-branch",
            "tag-1-test",
          ),
          Requirement.required,
          Stability.development
        )

      /** The revision, literally <a href="https://www.merriam-webster.com/dictionary/revision">revised version</a>, The
        * revision most often refers to a commit object in Git, or a revision number in SVN. <p>
        * @note
        *   <p> `head` refers to where you are right now; the current reference at a given time.The revision can be a
        *   full <a href="https://nvlpubs.nist.gov/nistpubs/FIPS/NIST.FIPS.186-5.pdf">hash value (see glossary)</a>, of
        *   the recorded change to a ref within a repository pointing to a commit <a
        *   href="https://git-scm.com/docs/git-commit">commit</a> object. It does not necessarily have to be a hash; it
        *   can simply define a <a href="https://svnbook.red-bean.com/en/1.7/svn.tour.revs.specifiers.html">revision
        *   number</a> which is an integer that is monotonically increasing. In cases where it is identical to the
        *   `ref.head.name`, it SHOULD still be included. It is up to the implementer to decide which value to set as
        *   the revision based on the VCS system and situational context.
        */
      val vcsRefHeadRevision: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsRefHeadRevision,
          List(
            "9d59409acf479dfa0df1aa568182e43e43df8bbe28d60fcf2bc52e30068802cc",
            "main",
            "123",
            "HEAD",
          ),
          Requirement.optIn,
          Stability.development
        )

      /** The human readable name of the repository. It SHOULD NOT include any additional identifier like Group/SubGroup
        * in GitLab or organization in GitHub. <p>
        * @note
        *   <p> Due to it only being the name, it can clash with forks of the same repository if collecting telemetry
        *   across multiple orgs or groups in the same backends.
        */
      val vcsRepositoryName: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsRepositoryName,
          List(
            "semantic-conventions",
            "my-cool-repo",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The <a
        * href="https://support.google.com/webmasters/answer/10347851?hl=en#:~:text=A%20canonical%20URL%20is%20the,Google%20chooses%20one%20as%20canonical.">canonical
        * URL</a> of the repository providing the complete HTTP(S) address in order to locate and identify the
        * repository through a browser. <p>
        * @note
        *   <p> In Git Version Control Systems, the canonical URL SHOULD NOT include the `.git` extension.
        */
      val vcsRepositoryUrlFull: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsRepositoryUrlFull,
          List(
            "https://github.com/opentelemetry/open-telemetry-collector-contrib",
            "https://gitlab.com/my-org/my-project/my-projects-project/repo",
          ),
          Requirement.required,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          vcsRefBaseName,
          vcsRefBaseRevision,
          vcsRefHeadName,
          vcsRefHeadRevision,
          vcsRepositoryName,
          vcsRepositoryUrlFull,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Gauge[F, A]] =
      Meter[F]
        .gauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** The amount of time since its creation it took a change (pull request/merge request/changelist) to get merged into
    * the target(base) ref.
    */
  object ChangeTimeToMerge extends MetricSpec {

    val name: String = "vcs.change.time_to_merge"
    val description: String =
      "The amount of time since its creation it took a change (pull request/merge request/changelist) to get merged into the target(base) ref."
    val unit: String = "s"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The name of the <a href="https://git-scm.com/docs/gitglossary#def_ref">reference</a> such as
        * <strong>branch</strong> or <strong>tag</strong> in the repository. <p>
        * @note
        *   <p> `base` refers to the starting point of a change. For example, `main` would be the base reference of type
        *   branch if you've created a new reference of type branch from it and created new commits.
        */
      val vcsRefBaseName: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsRefBaseName,
          List(
            "my-feature-branch",
            "tag-1-test",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The revision, literally <a href="https://www.merriam-webster.com/dictionary/revision">revised version</a>, The
        * revision most often refers to a commit object in Git, or a revision number in SVN. <p>
        * @note
        *   <p> `base` refers to the starting point of a change. For example, `main` would be the base reference of type
        *   branch if you've created a new reference of type branch from it and created new commits. The revision can be
        *   a full <a href="https://nvlpubs.nist.gov/nistpubs/FIPS/NIST.FIPS.186-5.pdf">hash value (see glossary)</a>,
        *   of the recorded change to a ref within a repository pointing to a commit <a
        *   href="https://git-scm.com/docs/git-commit">commit</a> object. It does not necessarily have to be a hash; it
        *   can simply define a <a href="https://svnbook.red-bean.com/en/1.7/svn.tour.revs.specifiers.html">revision
        *   number</a> which is an integer that is monotonically increasing. In cases where it is identical to the
        *   `ref.base.name`, it SHOULD still be included. It is up to the implementer to decide which value to set as
        *   the revision based on the VCS system and situational context.
        */
      val vcsRefBaseRevision: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsRefBaseRevision,
          List(
            "9d59409acf479dfa0df1aa568182e43e43df8bbe28d60fcf2bc52e30068802cc",
            "main",
            "123",
            "HEAD",
          ),
          Requirement.optIn,
          Stability.development
        )

      /** The name of the <a href="https://git-scm.com/docs/gitglossary#def_ref">reference</a> such as
        * <strong>branch</strong> or <strong>tag</strong> in the repository. <p>
        * @note
        *   <p> `head` refers to where you are right now; the current reference at a given time.
        */
      val vcsRefHeadName: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsRefHeadName,
          List(
            "my-feature-branch",
            "tag-1-test",
          ),
          Requirement.required,
          Stability.development
        )

      /** The revision, literally <a href="https://www.merriam-webster.com/dictionary/revision">revised version</a>, The
        * revision most often refers to a commit object in Git, or a revision number in SVN. <p>
        * @note
        *   <p> `head` refers to where you are right now; the current reference at a given time.The revision can be a
        *   full <a href="https://nvlpubs.nist.gov/nistpubs/FIPS/NIST.FIPS.186-5.pdf">hash value (see glossary)</a>, of
        *   the recorded change to a ref within a repository pointing to a commit <a
        *   href="https://git-scm.com/docs/git-commit">commit</a> object. It does not necessarily have to be a hash; it
        *   can simply define a <a href="https://svnbook.red-bean.com/en/1.7/svn.tour.revs.specifiers.html">revision
        *   number</a> which is an integer that is monotonically increasing. In cases where it is identical to the
        *   `ref.head.name`, it SHOULD still be included. It is up to the implementer to decide which value to set as
        *   the revision based on the VCS system and situational context.
        */
      val vcsRefHeadRevision: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsRefHeadRevision,
          List(
            "9d59409acf479dfa0df1aa568182e43e43df8bbe28d60fcf2bc52e30068802cc",
            "main",
            "123",
            "HEAD",
          ),
          Requirement.optIn,
          Stability.development
        )

      /** The human readable name of the repository. It SHOULD NOT include any additional identifier like Group/SubGroup
        * in GitLab or organization in GitHub. <p>
        * @note
        *   <p> Due to it only being the name, it can clash with forks of the same repository if collecting telemetry
        *   across multiple orgs or groups in the same backends.
        */
      val vcsRepositoryName: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsRepositoryName,
          List(
            "semantic-conventions",
            "my-cool-repo",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The <a
        * href="https://support.google.com/webmasters/answer/10347851?hl=en#:~:text=A%20canonical%20URL%20is%20the,Google%20chooses%20one%20as%20canonical.">canonical
        * URL</a> of the repository providing the complete HTTP(S) address in order to locate and identify the
        * repository through a browser. <p>
        * @note
        *   <p> In Git Version Control Systems, the canonical URL SHOULD NOT include the `.git` extension.
        */
      val vcsRepositoryUrlFull: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsRepositoryUrlFull,
          List(
            "https://github.com/opentelemetry/open-telemetry-collector-contrib",
            "https://gitlab.com/my-org/my-project/my-projects-project/repo",
          ),
          Requirement.required,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          vcsRefBaseName,
          vcsRefBaseRevision,
          vcsRefHeadName,
          vcsRefHeadRevision,
          vcsRepositoryName,
          vcsRepositoryUrlFull,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Gauge[F, A]] =
      Meter[F]
        .gauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** The number of unique contributors to a repository
    */
  object ContributorCount extends MetricSpec {

    val name: String = "vcs.contributor.count"
    val description: String = "The number of unique contributors to a repository"
    val unit: String = "{contributor}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The human readable name of the repository. It SHOULD NOT include any additional identifier like Group/SubGroup
        * in GitLab or organization in GitHub. <p>
        * @note
        *   <p> Due to it only being the name, it can clash with forks of the same repository if collecting telemetry
        *   across multiple orgs or groups in the same backends.
        */
      val vcsRepositoryName: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsRepositoryName,
          List(
            "semantic-conventions",
            "my-cool-repo",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The <a
        * href="https://support.google.com/webmasters/answer/10347851?hl=en#:~:text=A%20canonical%20URL%20is%20the,Google%20chooses%20one%20as%20canonical.">canonical
        * URL</a> of the repository providing the complete HTTP(S) address in order to locate and identify the
        * repository through a browser. <p>
        * @note
        *   <p> In Git Version Control Systems, the canonical URL SHOULD NOT include the `.git` extension.
        */
      val vcsRepositoryUrlFull: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsRepositoryUrlFull,
          List(
            "https://github.com/opentelemetry/open-telemetry-collector-contrib",
            "https://gitlab.com/my-org/my-project/my-projects-project/repo",
          ),
          Requirement.required,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          vcsRepositoryName,
          vcsRepositoryUrlFull,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Gauge[F, A]] =
      Meter[F]
        .gauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** The number of refs of type branch or tag in a repository.
    */
  object RefCount extends MetricSpec {

    val name: String = "vcs.ref.count"
    val description: String = "The number of refs of type branch or tag in a repository."
    val unit: String = "{ref}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The type of the <a href="https://git-scm.com/docs/gitglossary#def_ref">reference</a> in the repository.
        */
      val vcsRefType: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsRefType,
          List(
            "branch",
            "tag",
          ),
          Requirement.required,
          Stability.development
        )

      /** The human readable name of the repository. It SHOULD NOT include any additional identifier like Group/SubGroup
        * in GitLab or organization in GitHub. <p>
        * @note
        *   <p> Due to it only being the name, it can clash with forks of the same repository if collecting telemetry
        *   across multiple orgs or groups in the same backends.
        */
      val vcsRepositoryName: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsRepositoryName,
          List(
            "semantic-conventions",
            "my-cool-repo",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The <a
        * href="https://support.google.com/webmasters/answer/10347851?hl=en#:~:text=A%20canonical%20URL%20is%20the,Google%20chooses%20one%20as%20canonical.">canonical
        * URL</a> of the repository providing the complete HTTP(S) address in order to locate and identify the
        * repository through a browser. <p>
        * @note
        *   <p> In Git Version Control Systems, the canonical URL SHOULD NOT include the `.git` extension.
        */
      val vcsRepositoryUrlFull: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsRepositoryUrlFull,
          List(
            "https://github.com/opentelemetry/open-telemetry-collector-contrib",
            "https://gitlab.com/my-org/my-project/my-projects-project/repo",
          ),
          Requirement.required,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          vcsRefType,
          vcsRepositoryName,
          vcsRepositoryUrlFull,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[UpDownCounter[F, A]] =
      Meter[F]
        .upDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableUpDownCounter] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** The number of lines added/removed in a ref (branch) relative to the ref from the `vcs.ref.base.name` attribute.
    * <p>
    * @note
    *   <p> This metric should be reported for each `vcs.line_change.type` value. For example if a ref added 3 lines and
    *   removed 2 lines, instrumentation SHOULD report two measurements: 3 and 2 (both positive numbers). If number of
    *   lines added/removed should be calculated from the start of time, then `vcs.ref.base.name` SHOULD be set to an
    *   empty string.
    */
  object RefLinesDelta extends MetricSpec {

    val name: String = "vcs.ref.lines_delta"
    val description: String =
      "The number of lines added/removed in a ref (branch) relative to the ref from the `vcs.ref.base.name` attribute."
    val unit: String = "{line}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The ID of the change (pull request/merge request/changelist) if applicable. This is usually a unique (within
        * repository) identifier generated by the VCS system.
        */
      val vcsChangeId: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsChangeId,
          List(
            "123",
          ),
          Requirement.conditionallyRequired("if a change is associate with the ref."),
          Stability.development
        )

      /** The type of line change being measured on a branch or change.
        */
      val vcsLineChangeType: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsLineChangeType,
          List(
            "added",
            "removed",
          ),
          Requirement.required,
          Stability.development
        )

      /** The name of the <a href="https://git-scm.com/docs/gitglossary#def_ref">reference</a> such as
        * <strong>branch</strong> or <strong>tag</strong> in the repository. <p>
        * @note
        *   <p> `base` refers to the starting point of a change. For example, `main` would be the base reference of type
        *   branch if you've created a new reference of type branch from it and created new commits.
        */
      val vcsRefBaseName: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsRefBaseName,
          List(
            "my-feature-branch",
            "tag-1-test",
          ),
          Requirement.required,
          Stability.development
        )

      /** The type of the <a href="https://git-scm.com/docs/gitglossary#def_ref">reference</a> in the repository. <p>
        * @note
        *   <p> `base` refers to the starting point of a change. For example, `main` would be the base reference of type
        *   branch if you've created a new reference of type branch from it and created new commits.
        */
      val vcsRefBaseType: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsRefBaseType,
          List(
            "branch",
            "tag",
          ),
          Requirement.required,
          Stability.development
        )

      /** The name of the <a href="https://git-scm.com/docs/gitglossary#def_ref">reference</a> such as
        * <strong>branch</strong> or <strong>tag</strong> in the repository. <p>
        * @note
        *   <p> `head` refers to where you are right now; the current reference at a given time.
        */
      val vcsRefHeadName: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsRefHeadName,
          List(
            "my-feature-branch",
            "tag-1-test",
          ),
          Requirement.required,
          Stability.development
        )

      /** The type of the <a href="https://git-scm.com/docs/gitglossary#def_ref">reference</a> in the repository. <p>
        * @note
        *   <p> `head` refers to where you are right now; the current reference at a given time.
        */
      val vcsRefHeadType: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsRefHeadType,
          List(
            "branch",
            "tag",
          ),
          Requirement.required,
          Stability.development
        )

      /** The human readable name of the repository. It SHOULD NOT include any additional identifier like Group/SubGroup
        * in GitLab or organization in GitHub. <p>
        * @note
        *   <p> Due to it only being the name, it can clash with forks of the same repository if collecting telemetry
        *   across multiple orgs or groups in the same backends.
        */
      val vcsRepositoryName: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsRepositoryName,
          List(
            "semantic-conventions",
            "my-cool-repo",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The <a
        * href="https://support.google.com/webmasters/answer/10347851?hl=en#:~:text=A%20canonical%20URL%20is%20the,Google%20chooses%20one%20as%20canonical.">canonical
        * URL</a> of the repository providing the complete HTTP(S) address in order to locate and identify the
        * repository through a browser. <p>
        * @note
        *   <p> In Git Version Control Systems, the canonical URL SHOULD NOT include the `.git` extension.
        */
      val vcsRepositoryUrlFull: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsRepositoryUrlFull,
          List(
            "https://github.com/opentelemetry/open-telemetry-collector-contrib",
            "https://gitlab.com/my-org/my-project/my-projects-project/repo",
          ),
          Requirement.required,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          vcsChangeId,
          vcsLineChangeType,
          vcsRefBaseName,
          vcsRefBaseType,
          vcsRefHeadName,
          vcsRefHeadType,
          vcsRepositoryName,
          vcsRepositoryUrlFull,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Gauge[F, A]] =
      Meter[F]
        .gauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** The number of revisions (commits) a ref (branch) is ahead/behind the branch from the `vcs.ref.base.name` attribute
    * <p>
    * @note
    *   <p> This metric should be reported for each `vcs.revision_delta.direction` value. For example if branch `a` is 3
    *   commits behind and 2 commits ahead of `trunk`, instrumentation SHOULD report two measurements: 3 and 2 (both
    *   positive numbers) and `vcs.ref.base.name` is set to `trunk`.
    */
  object RefRevisionsDelta extends MetricSpec {

    val name: String = "vcs.ref.revisions_delta"
    val description: String =
      "The number of revisions (commits) a ref (branch) is ahead/behind the branch from the `vcs.ref.base.name` attribute"
    val unit: String = "{revision}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The ID of the change (pull request/merge request/changelist) if applicable. This is usually a unique (within
        * repository) identifier generated by the VCS system.
        */
      val vcsChangeId: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsChangeId,
          List(
            "123",
          ),
          Requirement.conditionallyRequired("if a change is associate with the ref."),
          Stability.development
        )

      /** The name of the <a href="https://git-scm.com/docs/gitglossary#def_ref">reference</a> such as
        * <strong>branch</strong> or <strong>tag</strong> in the repository. <p>
        * @note
        *   <p> `base` refers to the starting point of a change. For example, `main` would be the base reference of type
        *   branch if you've created a new reference of type branch from it and created new commits.
        */
      val vcsRefBaseName: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsRefBaseName,
          List(
            "my-feature-branch",
            "tag-1-test",
          ),
          Requirement.required,
          Stability.development
        )

      /** The type of the <a href="https://git-scm.com/docs/gitglossary#def_ref">reference</a> in the repository. <p>
        * @note
        *   <p> `base` refers to the starting point of a change. For example, `main` would be the base reference of type
        *   branch if you've created a new reference of type branch from it and created new commits.
        */
      val vcsRefBaseType: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsRefBaseType,
          List(
            "branch",
            "tag",
          ),
          Requirement.required,
          Stability.development
        )

      /** The name of the <a href="https://git-scm.com/docs/gitglossary#def_ref">reference</a> such as
        * <strong>branch</strong> or <strong>tag</strong> in the repository. <p>
        * @note
        *   <p> `head` refers to where you are right now; the current reference at a given time.
        */
      val vcsRefHeadName: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsRefHeadName,
          List(
            "my-feature-branch",
            "tag-1-test",
          ),
          Requirement.required,
          Stability.development
        )

      /** The type of the <a href="https://git-scm.com/docs/gitglossary#def_ref">reference</a> in the repository. <p>
        * @note
        *   <p> `head` refers to where you are right now; the current reference at a given time.
        */
      val vcsRefHeadType: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsRefHeadType,
          List(
            "branch",
            "tag",
          ),
          Requirement.required,
          Stability.development
        )

      /** The human readable name of the repository. It SHOULD NOT include any additional identifier like Group/SubGroup
        * in GitLab or organization in GitHub. <p>
        * @note
        *   <p> Due to it only being the name, it can clash with forks of the same repository if collecting telemetry
        *   across multiple orgs or groups in the same backends.
        */
      val vcsRepositoryName: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsRepositoryName,
          List(
            "semantic-conventions",
            "my-cool-repo",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The <a
        * href="https://support.google.com/webmasters/answer/10347851?hl=en#:~:text=A%20canonical%20URL%20is%20the,Google%20chooses%20one%20as%20canonical.">canonical
        * URL</a> of the repository providing the complete HTTP(S) address in order to locate and identify the
        * repository through a browser. <p>
        * @note
        *   <p> In Git Version Control Systems, the canonical URL SHOULD NOT include the `.git` extension.
        */
      val vcsRepositoryUrlFull: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsRepositoryUrlFull,
          List(
            "https://github.com/opentelemetry/open-telemetry-collector-contrib",
            "https://gitlab.com/my-org/my-project/my-projects-project/repo",
          ),
          Requirement.required,
          Stability.development
        )

      /** The type of revision comparison.
        */
      val vcsRevisionDeltaDirection: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsRevisionDeltaDirection,
          List(
            "ahead",
            "behind",
          ),
          Requirement.required,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          vcsChangeId,
          vcsRefBaseName,
          vcsRefBaseType,
          vcsRefHeadName,
          vcsRefHeadType,
          vcsRepositoryName,
          vcsRepositoryUrlFull,
          vcsRevisionDeltaDirection,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Gauge[F, A]] =
      Meter[F]
        .gauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** Time a ref (branch) created from the default branch (trunk) has existed. The `ref.type` attribute will always be
    * `branch`
    */
  object RefTime extends MetricSpec {

    val name: String = "vcs.ref.time"
    val description: String =
      "Time a ref (branch) created from the default branch (trunk) has existed. The `ref.type` attribute will always be `branch`"
    val unit: String = "s"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = AttributeSpecs.specs

    object AttributeSpecs {

      /** The name of the <a href="https://git-scm.com/docs/gitglossary#def_ref">reference</a> such as
        * <strong>branch</strong> or <strong>tag</strong> in the repository. <p>
        * @note
        *   <p> `head` refers to where you are right now; the current reference at a given time.
        */
      val vcsRefHeadName: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsRefHeadName,
          List(
            "my-feature-branch",
            "tag-1-test",
          ),
          Requirement.required,
          Stability.development
        )

      /** The type of the <a href="https://git-scm.com/docs/gitglossary#def_ref">reference</a> in the repository. <p>
        * @note
        *   <p> `head` refers to where you are right now; the current reference at a given time.
        */
      val vcsRefHeadType: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsRefHeadType,
          List(
            "branch",
            "tag",
          ),
          Requirement.required,
          Stability.development
        )

      /** The human readable name of the repository. It SHOULD NOT include any additional identifier like Group/SubGroup
        * in GitLab or organization in GitHub. <p>
        * @note
        *   <p> Due to it only being the name, it can clash with forks of the same repository if collecting telemetry
        *   across multiple orgs or groups in the same backends.
        */
      val vcsRepositoryName: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsRepositoryName,
          List(
            "semantic-conventions",
            "my-cool-repo",
          ),
          Requirement.recommended,
          Stability.development
        )

      /** The <a
        * href="https://support.google.com/webmasters/answer/10347851?hl=en#:~:text=A%20canonical%20URL%20is%20the,Google%20chooses%20one%20as%20canonical.">canonical
        * URL</a> of the repository providing the complete HTTP(S) address in order to locate and identify the
        * repository through a browser. <p>
        * @note
        *   <p> In Git Version Control Systems, the canonical URL SHOULD NOT include the `.git` extension.
        */
      val vcsRepositoryUrlFull: AttributeSpec[String] =
        AttributeSpec(
          VcsExperimentalAttributes.VcsRepositoryUrlFull,
          List(
            "https://github.com/opentelemetry/open-telemetry-collector-contrib",
            "https://gitlab.com/my-org/my-project/my-projects-project/repo",
          ),
          Requirement.required,
          Stability.development
        )

      val specs: List[AttributeSpec[_]] =
        List(
          vcsRefHeadName,
          vcsRefHeadType,
          vcsRepositoryName,
          vcsRepositoryUrlFull,
        )
    }

    def create[F[_]: Meter, A: MeasurementValue]: F[Gauge[F, A]] =
      Meter[F]
        .gauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] =
      Meter[F]
        .observableGauge[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

  /** The number of repositories in an organization.
    */
  object RepositoryCount extends MetricSpec {

    val name: String = "vcs.repository.count"
    val description: String = "The number of repositories in an organization."
    val unit: String = "{repository}"
    val stability: Stability = Stability.development
    val attributeSpecs: List[AttributeSpec[_]] = Nil

    def create[F[_]: Meter, A: MeasurementValue]: F[UpDownCounter[F, A]] =
      Meter[F]
        .upDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .create

    def createObserver[F[_]: Meter, A: MeasurementValue]: F[ObservableMeasurement[F, A]] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createObserver

    def createWithCallback[F[_]: Meter, A: MeasurementValue](
        callback: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableUpDownCounter] =
      Meter[F]
        .observableUpDownCounter[A](name)
        .withDescription(description)
        .withUnit(unit)
        .createWithCallback(callback)

  }

}
