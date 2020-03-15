/*
 * Copyright 2013-2020 The OpenZipkin Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package brave.propagation;

import brave.internal.Nullable;

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Format that encapsulates the W3C Trace Context: https://www.w3.org/TR/trace-context/
 *
 * <p>The context comprises four parts, assembled into a single HTTP header as follows:
 *
 * <pre>{@code
 * traceparent: {version}-{trace-id}-{parent-id}-{trace-flags}
 * }</pre>
 *
 * <p>Parts are:
 * <dl>
 *   <dt>{@code version}</dt>
 *   <dd>One byte in hexadecimal with the version. Current version is {@code 00}.</dd>
 *   <dt>{@code trace-id}</dt>
 *   <dd>16 bytes in hexadecimal with a trace identifier. All zeros is forbidden.</dd>
 *   <dt>{@code parent-id}</dt>
 *   <dd>8 bytes in hexadcimal with the ID of this request as known to the caller.
 *       Equivalent to span ID. All zeros is forbidden.</dd>
 *   <dt>{@code trace-flags}</dt>
 *   <dd>8 bits in hexadecimal with flags. Currently the only defined flag is {@code 0x01}
 *       meaning <b>sampled</b>.</dd>
 * </dl>
 *
 * <p>For example {@code 00-c68aa450c565e733c2fd05bd52771467-391a0978a417ac9b-00} has:
 * <ul>
 *   <li>{@code version} {@code 00}</li>
 *   <li>{@code trace-id} {@code c68aa450c565e733c2fd05bd52771467}</li>
 *   <li>{@code parent-id} {@code 391a0978a417ac9b}</li>
 *   <li>{@code trace-flags}: this span is not being sampled</li>
 * </ul>
 *
 * <h1>Relationship between W3C and B3 tracing</h1>
 * The relationship between W3C {@code parent-id} and B3 {@code spanId} is: ???
 */
public final class W3CFormat {

  /**
   * Current version of W3C Trace Context. Any other versions are ignored.
   */
  public static final int W3C_FORMAT_VERSION = 0x00;

  private final String traceId;
  private final String parentId;
  private final int traceFlags;

  private W3CFormat(String traceId, String parentId, int traceFlags) {
    this.traceId = traceId;
    this.parentId = parentId;
    this.traceFlags = traceFlags;
  }

  private long traceIdHigh() {
    return new BigInteger(traceId.substring(0, 16), 16).longValue();
  }

  private long traceId() {
    return new BigInteger(traceId.substring(16), 16).longValue();
  }

  private long spanId() {
    return new BigInteger(parentId, 16).longValue();
  }

  private boolean sampled() {
    return traceFlags == 1;
  }

  /**
   * Writes a {@code traceparent} HTTP header value from the supplied {@link TraceContext}.
   *
   * <p>Used by {@link W3CPropagation.W3CInjector#inject(TraceContext, Object)} to put
   * context information into a carrier.
   *
   * <ul>
   *   <li>The W3C Tracing {@code version} value is always {@code 00}.</li>
   *   <li>The {@link TraceContext#traceIdHigh()} and {@link TraceContext#traceId()}
   *   hexadecimal values are concatenated into W3C {@code trace-id} value because W3C specifies
   *   only 128-bit values.</li>
   *   <li>The {@link TraceContext#spanId()} hexadecimal value is written to W3C {@code parent-id}
   *   value.</li>
   *   <li>The W3C {@code trace-flags} value is set to {@code 01} if
   *   {@link TraceContext#sampled()} returns {@code true}; else {@code 00}.</li>
   * </ul>
   *
   * @param context current context to be injected
   * @return header value in W3C Trace Context format
   */
  public static String writeTraceParent(TraceContext context) {
    int sampled = context.sampled() != null && context.sampled() ? 0x01 : 0x00;
    return String.format("%02x-%016x%016x-%016x-%02x",
      W3C_FORMAT_VERSION, context.traceIdHigh(), context.traceId(), context.spanId(), sampled);
  }

  private static final Pattern TRACE_PARENT_PATTERN = Pattern.compile("^00-([0-9a-f]{32})-([0-9a-f]{16})-(0[01])$");

  private static W3CFormat buildFromTraceParent(String traceParent) {
    if (traceParent == null) return null;
    Matcher matcher = TRACE_PARENT_PATTERN.matcher(traceParent);
    if (matcher.matches()) {
      String traceId = matcher.group(1);
      String parentId = matcher.group(2);
      String traceFlags = matcher.group(3);
      return new W3CFormat(traceId, parentId, Integer.parseInt(traceFlags));
    }
    return null;
  }

  /**
   * Parses a W3C Trace Context {@code traceparent} header value into a {@link TraceContext}.
   *
   * <ul>
   *   <li>The </li>
   *   <li>If the string does not conform to the W3C format, it returns {@code null}.</li>
   * </ul>
   *
   * @param traceParent W3C Trace Context header value
   * @return a new {@link TraceContext} instance built from the header, or {@code null} if
   * the string is not in the correct format.
   */
  @Nullable
  public static TraceContext parseTraceParent(String traceParent) {

    W3CFormat format = buildFromTraceParent(traceParent);
    if (format == null) return null;

    return TraceContext.newBuilder()
      .traceIdHigh(format.traceIdHigh())
      .traceId(format.traceId())
      .spanId(format.spanId())
      .sampled(format.sampled())
      .build();
  }
}
