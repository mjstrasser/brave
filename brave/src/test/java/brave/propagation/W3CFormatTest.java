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

import static brave.propagation.W3CFormat.writeTraceParent;
import static org.assertj.core.api.Assertions.assertThat;

public class W3CFormatTest {
  String traceId = "0000000000000000" + "0000000000000001";
  String parentId = "0000000000000002";
  String spanId = "0000000000000003";

  @Test public void writeTraceParent_trace_span_sampled() {
    TraceContext context = TraceContext.newBuilder()
      .traceIdHigh(0).traceId(1).spanId(3).sampled(true)
      .build();
    assertThat(writeTraceParent(context))
      .isEqualTo(String.format("00-%s-%s-01", traceId, spanId));
  }

  @Test public void writeTraceParent_trace_span_not_sampled() {
    TraceContext context = TraceContext.newBuilder()
      .traceIdHigh(0).traceId(1).spanId(3).sampled(false)
      .build();
    assertThat(writeTraceParent(context))
      .isEqualTo(String.format("00-%s-%s-00", traceId, spanId));
  }

  @Test public void writeTraceParent_trace_span_null_sampled() {
    TraceContext context = TraceContext.newBuilder()
      .traceIdHigh(0).traceId(1).spanId(3)
      .build();
    assertThat(writeTraceParent(context))
      .isEqualTo(String.format("00-%s-%s-00", traceId, spanId));
  }

}
