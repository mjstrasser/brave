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

import org.junit.Test;

import static brave.propagation.W3CFormat.parseTraceParent;
import static brave.propagation.W3CFormat.writeTraceParent;
import static org.assertj.core.api.Assertions.assertThat;

public class W3CFormatTest {
  String traceId = "0000000000000001" + "0000000000000002";
  String parentId = "0000000000000003";
  String spanId = "0000000000000004";

  @Test
  public void writeTraceParent_trace_span_sampled() {
    TraceContext context = TraceContext.newBuilder()
      .traceIdHigh(1).traceId(2).spanId(4).sampled(true)
      .build();
    assertThat(writeTraceParent(context))
      .isEqualTo(String.format("00-%s-%s-01", traceId, spanId));
  }

  @Test
  public void writeTraceParent_trace_span_not_sampled() {
    TraceContext context = TraceContext.newBuilder()
      .traceIdHigh(1).traceId(2).spanId(4).sampled(false)
      .build();
    assertThat(writeTraceParent(context))
      .isEqualTo(String.format("00-%s-%s-00", traceId, spanId));
  }

  @Test
  public void writeTraceParent_trace_span_null_sampled() {
    TraceContext context = TraceContext.newBuilder()
      .traceIdHigh(1).traceId(2).spanId(4)
      .build();
    assertThat(writeTraceParent(context))
      .isEqualTo(String.format("00-%s-%s-00", traceId, spanId));
  }

  @Test
  public void parseTraceParent_return_null_on_bad_formats() {
    assertThat(parseTraceParent("00-badformat-badformat-01")).isNull();
    assertThat(parseTraceParent("00-c68aa450c565e733c2fd05bd52771467-391a0978a417ac9b-01")).isNotNull();
    assertThat(parseTraceParent("01-c68aa450c565e733c2fd05bd52771467-391a0978a417ac9b-01")).isNull();
    assertThat(parseTraceParent("00-c68aa450c565e733c2fd05bd52771467-391a0978a417ac9b-03")).isNull();
  }

  @Test
  public void parseTraceParent_trace_span_sampled() {
    String traceParent = "00-" + traceId + "-" + spanId + "-01";

    TraceContext context = parseTraceParent(traceParent);

    assertThat(context).isNotNull();
    assertThat(context.traceIdString()).isEqualTo(traceId);
    assertThat(context.spanIdString()).isEqualTo(spanId);
    assertThat(context.sampled()).isTrue();
  }

  @Test
  public void parseTraceParent_trace_span_not_sampled() {
    String traceParent = "00-" + traceId + "-" + spanId + "-00";

    TraceContext context = parseTraceParent(traceParent);

    assertThat(context).isNotNull();
    assertThat(context.traceIdString()).isEqualTo(traceId);
    assertThat(context.spanIdString()).isEqualTo(spanId);
    assertThat(context.sampled()).isFalse();
  }
}
