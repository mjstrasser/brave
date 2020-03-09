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

import brave.Request;
import brave.Span;
import brave.internal.Platform;
import brave.propagation.B3Propagation.Format;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static brave.propagation.W3CFormat.writeW3CFormat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
// Added to declutter console: tells power mock not to mess with implicit classes we aren't testing
@PowerMockIgnore({"org.apache.logging.*", "javax.script.*"})
@PrepareForTest({Platform.class, W3CFormat.class})
public class W3CFormatTest {
  String traceId = "0000000000000000" + "0000000000000001";
  String parentId = "0000000000000002";
  String spanId = "0000000000000003";

  Platform platform = mock(Platform.class);

  @Before public void setupLogger() {
    mockStatic(Platform.class);
    when(Platform.get()).thenReturn(platform);
  }

  /** Either we asserted on the log messages or there weren't any */
  @After public void ensureNothingLogged() {
    verifyNoMoreInteractions(platform);
  }

  @Test public void writeW3CFormat_good() {
    TraceContext context = TraceContext.newBuilder().traceIdHigh(0).traceId(1).spanId(3).build();
    assertThat(writeW3CFormat(context))
      .isEqualTo(String.format("00-%s-%s-01", traceId, spanId));
  }

}
