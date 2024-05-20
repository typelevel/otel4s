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
object TlsExperimentalAttributes {

  /** String indicating the <a
    * href="https://datatracker.ietf.org/doc/html/rfc5246#appendix-A.5">cipher</a>
    * used during the current connection.
    *
    * @note
    *   - The values allowed for `tls.cipher` MUST be one of the `Descriptions`
    *     of the <a
    *     href="https://www.iana.org/assignments/tls-parameters/tls-parameters.xhtml#table-tls-parameters-4">registered
    *     TLS Cipher Suits</a>.
    */
  val TlsCipher: AttributeKey[String] = string("tls.cipher")

  /** PEM-encoded stand-alone certificate offered by the client. This is usually
    * mutually-exclusive of `client.certificate_chain` since this value also
    * exists in that list.
    */
  val TlsClientCertificate: AttributeKey[String] = string(
    "tls.client.certificate"
  )

  /** Array of PEM-encoded certificates that make up the certificate chain
    * offered by the client. This is usually mutually-exclusive of
    * `client.certificate` since that value should be the first certificate in
    * the chain.
    */
  val TlsClientCertificateChain: AttributeKey[Seq[String]] = stringSeq(
    "tls.client.certificate_chain"
  )

  /** Certificate fingerprint using the MD5 digest of DER-encoded version of
    * certificate offered by the client. For consistency with other hash values,
    * this value should be formatted as an uppercase hash.
    */
  val TlsClientHashMd5: AttributeKey[String] = string("tls.client.hash.md5")

  /** Certificate fingerprint using the SHA1 digest of DER-encoded version of
    * certificate offered by the client. For consistency with other hash values,
    * this value should be formatted as an uppercase hash.
    */
  val TlsClientHashSha1: AttributeKey[String] = string("tls.client.hash.sha1")

  /** Certificate fingerprint using the SHA256 digest of DER-encoded version of
    * certificate offered by the client. For consistency with other hash values,
    * this value should be formatted as an uppercase hash.
    */
  val TlsClientHashSha256: AttributeKey[String] = string(
    "tls.client.hash.sha256"
  )

  /** Distinguished name of <a
    * href="https://datatracker.ietf.org/doc/html/rfc5280#section-4.1.2.6">subject</a>
    * of the issuer of the x.509 certificate presented by the client.
    */
  val TlsClientIssuer: AttributeKey[String] = string("tls.client.issuer")

  /** A hash that identifies clients based on how they perform an SSL/TLS
    * handshake.
    */
  val TlsClientJa3: AttributeKey[String] = string("tls.client.ja3")

  /** Date/Time indicating when client certificate is no longer considered
    * valid.
    */
  val TlsClientNotAfter: AttributeKey[String] = string("tls.client.not_after")

  /** Date/Time indicating when client certificate is first considered valid.
    */
  val TlsClientNotBefore: AttributeKey[String] = string("tls.client.not_before")

  /** Also called an SNI, this tells the server which hostname to which the
    * client is attempting to connect to.
    */
  val TlsClientServerName: AttributeKey[String] = string(
    "tls.client.server_name"
  )

  /** Distinguished name of subject of the x.509 certificate presented by the
    * client.
    */
  val TlsClientSubject: AttributeKey[String] = string("tls.client.subject")

  /** Array of ciphers offered by the client during the client hello.
    */
  val TlsClientSupportedCiphers: AttributeKey[Seq[String]] = stringSeq(
    "tls.client.supported_ciphers"
  )

  /** String indicating the curve used for the given cipher, when applicable
    */
  val TlsCurve: AttributeKey[String] = string("tls.curve")

  /** Boolean flag indicating if the TLS negotiation was successful and
    * transitioned to an encrypted tunnel.
    */
  val TlsEstablished: AttributeKey[Boolean] = boolean("tls.established")

  /** String indicating the protocol being tunneled. Per the values in the <a
    * href="https://www.iana.org/assignments/tls-extensiontype-values/tls-extensiontype-values.xhtml#alpn-protocol-ids">IANA
    * registry</a>, this string should be lower case.
    */
  val TlsNextProtocol: AttributeKey[String] = string("tls.next_protocol")

  /** Normalized lowercase protocol name parsed from original string of the
    * negotiated <a
    * href="https://www.openssl.org/docs/man1.1.1/man3/SSL_get_version.html#RETURN-VALUES">SSL/TLS
    * protocol version</a>
    */
  val TlsProtocolName: AttributeKey[String] = string("tls.protocol.name")

  /** Numeric part of the version parsed from the original string of the
    * negotiated <a
    * href="https://www.openssl.org/docs/man1.1.1/man3/SSL_get_version.html#RETURN-VALUES">SSL/TLS
    * protocol version</a>
    */
  val TlsProtocolVersion: AttributeKey[String] = string("tls.protocol.version")

  /** Boolean flag indicating if this TLS connection was resumed from an
    * existing TLS negotiation.
    */
  val TlsResumed: AttributeKey[Boolean] = boolean("tls.resumed")

  /** PEM-encoded stand-alone certificate offered by the server. This is usually
    * mutually-exclusive of `server.certificate_chain` since this value also
    * exists in that list.
    */
  val TlsServerCertificate: AttributeKey[String] = string(
    "tls.server.certificate"
  )

  /** Array of PEM-encoded certificates that make up the certificate chain
    * offered by the server. This is usually mutually-exclusive of
    * `server.certificate` since that value should be the first certificate in
    * the chain.
    */
  val TlsServerCertificateChain: AttributeKey[Seq[String]] = stringSeq(
    "tls.server.certificate_chain"
  )

  /** Certificate fingerprint using the MD5 digest of DER-encoded version of
    * certificate offered by the server. For consistency with other hash values,
    * this value should be formatted as an uppercase hash.
    */
  val TlsServerHashMd5: AttributeKey[String] = string("tls.server.hash.md5")

  /** Certificate fingerprint using the SHA1 digest of DER-encoded version of
    * certificate offered by the server. For consistency with other hash values,
    * this value should be formatted as an uppercase hash.
    */
  val TlsServerHashSha1: AttributeKey[String] = string("tls.server.hash.sha1")

  /** Certificate fingerprint using the SHA256 digest of DER-encoded version of
    * certificate offered by the server. For consistency with other hash values,
    * this value should be formatted as an uppercase hash.
    */
  val TlsServerHashSha256: AttributeKey[String] = string(
    "tls.server.hash.sha256"
  )

  /** Distinguished name of <a
    * href="https://datatracker.ietf.org/doc/html/rfc5280#section-4.1.2.6">subject</a>
    * of the issuer of the x.509 certificate presented by the client.
    */
  val TlsServerIssuer: AttributeKey[String] = string("tls.server.issuer")

  /** A hash that identifies servers based on how they perform an SSL/TLS
    * handshake.
    */
  val TlsServerJa3s: AttributeKey[String] = string("tls.server.ja3s")

  /** Date/Time indicating when server certificate is no longer considered
    * valid.
    */
  val TlsServerNotAfter: AttributeKey[String] = string("tls.server.not_after")

  /** Date/Time indicating when server certificate is first considered valid.
    */
  val TlsServerNotBefore: AttributeKey[String] = string("tls.server.not_before")

  /** Distinguished name of subject of the x.509 certificate presented by the
    * server.
    */
  val TlsServerSubject: AttributeKey[String] = string("tls.server.subject")
  // Enum definitions

  /** Values for [[TlsProtocolName]].
    */
  abstract class TlsProtocolNameValue(val value: String)
  object TlsProtocolNameValue {

    /** ssl. */
    case object Ssl extends TlsProtocolNameValue("ssl")

    /** tls. */
    case object Tls extends TlsProtocolNameValue("tls")
  }

}
