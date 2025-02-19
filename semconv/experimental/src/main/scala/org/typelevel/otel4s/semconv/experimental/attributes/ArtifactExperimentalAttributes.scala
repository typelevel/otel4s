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
object ArtifactExperimentalAttributes {

  /** The provenance filename of the built attestation which directly relates to the build artifact filename. This
    * filename SHOULD accompany the artifact at publish time. See the <a
    * href="https://slsa.dev/spec/v1.0/distributing-provenance#relationship-between-artifacts-and-attestations">SLSA
    * Relationship</a> specification for more information.
    */
  val ArtifactAttestationFilename: AttributeKey[String] =
    AttributeKey("artifact.attestation.filename")

  /** The full <a href="https://nvlpubs.nist.gov/nistpubs/FIPS/NIST.FIPS.186-5.pdf">hash value (see glossary)</a>, of
    * the built attestation. Some envelopes in the <a
    * href="https://github.com/in-toto/attestation/tree/main/spec">software attestation space</a> also refer to this as
    * the <strong>digest</strong>.
    */
  val ArtifactAttestationHash: AttributeKey[String] =
    AttributeKey("artifact.attestation.hash")

  /** The id of the build <a href="https://slsa.dev/attestation-model">software attestation</a>.
    */
  val ArtifactAttestationId: AttributeKey[String] =
    AttributeKey("artifact.attestation.id")

  /** The human readable file name of the artifact, typically generated during build and release processes. Often
    * includes the package name and version in the file name. <p>
    * @note
    *   <p> This file name can also act as the <a href="https://slsa.dev/spec/v1.0/terminology#package-model">Package
    *   Name</a> in cases where the package ecosystem maps accordingly. Additionally, the artifact <a
    *   href="https://slsa.dev/spec/v1.0/terminology#software-supply-chain">can be published</a> for others, but that is
    *   not a guarantee.
    */
  val ArtifactFilename: AttributeKey[String] =
    AttributeKey("artifact.filename")

  /** The full <a href="https://nvlpubs.nist.gov/nistpubs/FIPS/NIST.FIPS.186-5.pdf">hash value (see glossary)</a>, often
    * found in checksum.txt on a release of the artifact and used to verify package integrity. <p>
    * @note
    *   <p> The specific algorithm used to create the cryptographic hash value is not defined. In situations where an
    *   artifact has multiple cryptographic hashes, it is up to the implementer to choose which hash value to set here;
    *   this should be the most secure hash algorithm that is suitable for the situation and consistent with the
    *   corresponding attestation. The implementer can then provide the other hash values through an additional set of
    *   attribute extensions as they deem necessary.
    */
  val ArtifactHash: AttributeKey[String] =
    AttributeKey("artifact.hash")

  /** The <a href="https://github.com/package-url/purl-spec">Package URL</a> of the <a
    * href="https://slsa.dev/spec/v1.0/terminology#package-model">package artifact</a> provides a standard way to
    * identify and locate the packaged artifact.
    */
  val ArtifactPurl: AttributeKey[String] =
    AttributeKey("artifact.purl")

  /** The version of the artifact.
    */
  val ArtifactVersion: AttributeKey[String] =
    AttributeKey("artifact.version")

}
