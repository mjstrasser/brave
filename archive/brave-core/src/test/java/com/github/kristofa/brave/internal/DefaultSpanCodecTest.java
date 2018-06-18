package com.github.kristofa.brave.internal;

import com.github.kristofa.brave.SpanId;
import com.twitter.zipkin.gen.Annotation;
import com.twitter.zipkin.gen.BinaryAnnotation;
import com.twitter.zipkin.gen.Endpoint;
import com.twitter.zipkin.gen.Span;
import java.net.Inet6Address;
import java.net.UnknownHostException;
import org.junit.Test;

import static com.github.kristofa.brave.internal.DefaultSpanCodec.newSpan;
import static org.junit.Assert.assertEquals;

public class DefaultSpanCodecTest {

  static {
    InternalSpan.initializeInstanceForTests();
  }

  Endpoint browser = Endpoint.create("browser-client", 1 << 24 | 2 << 16 | 3);
  Endpoint web = Endpoint.builder()
      .serviceName("web")
      .ipv4(124 << 24 | 13 << 16 | 90 << 8 | 3)
      .ipv6(fromIPv6Literal("2001:db8::c001"))
      .port(80).build();

  Span span = newSpan(SpanId.builder().spanId(-692101025335252320L).build()) // browser calls web
      .setName("get")
      .setTimestamp(1444438900939000L)
      .setDuration(376000L)
      .addToAnnotations(Annotation.create(1444438900939000L, "sr", web))
      .addToAnnotations(Annotation.create(1444438901315000L, "ss", web))
      .addToBinary_annotations(BinaryAnnotation.address("ca", browser));

  @Test
  public void roundTripSpan_thrift() {
    byte[] encoded = DefaultSpanCodec.THRIFT.writeSpan(span);
    assertEquals(span, DefaultSpanCodec.THRIFT.readSpan(encoded));
  }

  @Test
  public void roundTripSpan_thrift_128() {
    span = newSpan(SpanId.builder().traceIdHigh(1L).traceId(2L).spanId(3L).build());

    byte[] encoded = DefaultSpanCodec.THRIFT.writeSpan(span);
    assertEquals(span, DefaultSpanCodec.THRIFT.readSpan(encoded));
  }

  @Test
  public void roundTripSpan_json() {
    byte[] encoded = DefaultSpanCodec.JSON.writeSpan(span);
    assertEquals(span, DefaultSpanCodec.JSON.readSpan(encoded));
  }

  @Test
  public void roundTripSpan_json_128() {
    span = newSpan(SpanId.builder().traceIdHigh(1L).traceId(2L).spanId(3L).build());

    byte[] encoded = DefaultSpanCodec.JSON.writeSpan(span);
    assertEquals(span, DefaultSpanCodec.JSON.readSpan(encoded));
  }

  static byte[] fromIPv6Literal(String literal) {
    try {
      return Inet6Address.getByName(literal).getAddress();
    } catch (UnknownHostException e) {
      throw new AssertionError(e);
    }
  }
}