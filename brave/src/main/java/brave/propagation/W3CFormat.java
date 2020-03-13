package brave.propagation;

public class W3CFormat {

  public static final int W3C_FORMAT_VERSION = 0x00;

  public static String writeTraceParent(TraceContext context) {
    int sampled = context.sampled() != null && context.sampled() ? 0x01 : 0x00;
    return String.format("%02x-%016x%016x-%016x-%02x",
      W3C_FORMAT_VERSION, context.traceIdHigh, context.traceId, context.spanId, sampled);
  }
}
