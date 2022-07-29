/*
 * Copyright 2022 Typelevel
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

package org.typelevel.otel4s.trace

import scodec.bits.ByteVector

trait SpanContext {

  /** Returns the trace identifier associated with this [[SpanContext]] as 32
    * character lowercase hex String.
    */
  def getTraceId: String

  /** Returns the trace identifier associated with this [[SpanContext]] as
    * 16-byte vector.
    */
  def getTraceIdBytes: ByteVector

  /** Returns the span identifier associated with this [[SpanContext]] as 16
    * character lowercase hex String.
    */
  def getSpanId: String

  /** Returns the span identifier associated with this [[SpanContext]] as 8-byte
    * vector.
    */
  def getSpanIdBytes: ByteVector

  /** Returns the sampling strategy of this [[SpanContext]]. Indicates whether
    * the span in this context is sampled.
    */
  def sampleStrategy: SampleStrategy

  /** Returns `true` if this [[SpanContext]] is valid.
    */
  def isValid: Boolean

  /** Returns `true` if this [[SpanContext]] was propagated from a remote
    * parent.
    */
  def isRemote: Boolean
}
