package brave.propagation;

import brave.internal.Platform;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
// Added to declutter console: tells power mock not to mess with implicit classes we aren't testing
@PowerMockIgnore({"org.apache.logging.*", "javax.script.*"})
@PrepareForTest({Platform.class, W3CPropagation.class})
public class W3CPropagationTest {
  String traceId = "0000000000000000" + "0000000000000001";
  String parentId = "0000000000000002";
  String spanId = "0000000000000003";

  TraceContext context = TraceContext.newBuilder()
    .traceIdHigh(0).traceId(1).parentId(2).spanId(3)
    .build();

  Propagation<String> propagation = W3CPropagation.W3C_STRING;
  Platform platform = mock(Platform.class);

  @Before
  public void setup() {
    mockStatic(Platform.class);
    when((Platform.get())).thenReturn(platform);
  }

  @After
  public void ensureNothingLogged() {
    verifyNoMoreInteractions(platform);
  }

  @Test
  public void keys_setCorrectly() {
    propagation = W3CPropagation.newFactory().create(Propagation.KeyFactory.STRING);

    assertThat(propagation.keys()).containsExactly(
      "traceparent",
      "tracestate"
    );
  }

}
