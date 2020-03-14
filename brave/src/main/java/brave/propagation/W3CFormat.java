package brave.propagation;

import brave.internal.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class W3CFormat {

  public static final int W3C_FORMAT_VERSION = 0x00;
  private static final String W3C_FORMAT_VERSION_STRING = "00";

  private final String traceId;
  private final String parentId;
  private final int traceFlags;

  private W3CFormat(String traceId, String parentId, int traceFlags) {
    this.traceId = traceId;
    this.parentId = parentId;
    this.traceFlags = traceFlags;
  }

  private long traceIdHigh() {
    return Long.parseLong(traceId.substring(0, 16));
  }

  private long traceId() {
    return Long.parseLong(traceId.substring(16));
  }

  private long spanId() {
    return Long.parseLong(parentId);
  }

  private boolean sampled() {
    return traceFlags == 1;
  }

  public static String writeTraceParent(TraceContext context) {
    int sampled = context.sampled() != null && context.sampled() ? 0x01 : 0x00;
    return String.format("%02x-%016x%016x-%016x-%02x",
      W3C_FORMAT_VERSION, context.traceIdHigh, context.traceId, context.spanId, sampled);
  }

  private static final Pattern TRACE_PARENT_PATTERN = Pattern.compile(
    "^00-(?<traceId>[0-9a-f]{32})-(?<parentId>[0-9a-f]{16})-(?<traceFlags>0[01])$");

  private static W3CFormat buildFromTraceParent(String traceParent) {
    Matcher matcher = TRACE_PARENT_PATTERN.matcher(traceParent);
    if (matcher.matches()) {
      String traceId = matcher.group("traceId");
      String parentId = matcher.group("parentId");
      String traceFlags = matcher.group("traceFlags");
      return new W3CFormat(traceId, parentId, Integer.parseInt(traceFlags));
    }
    return null;
  }

  @Nullable
  public static TraceContextOrSamplingFlags parseTraceParent(String traceParent) {

    W3CFormat format = buildFromTraceParent(traceParent);
    if (format == null) return null;

    return TraceContextOrSamplingFlags.create(TraceContext.newBuilder()
      .traceIdHigh(format.traceIdHigh())
      .traceId(format.traceId())
      .spanId(format.spanId())
      .sampled(format.sampled())
      .build());
  }
}
