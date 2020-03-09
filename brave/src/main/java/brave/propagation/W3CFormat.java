package brave.propagation;

public class W3CFormat {
  public static String writeW3CFormat(TraceContext context) {
    return String.format("00-%016x%016x-%016x-01", context.traceIdHigh, context.traceId, context.spanId);
  }
}
