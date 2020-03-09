package brave.propagation;

import java.util.Collections;
import java.util.List;

import static brave.propagation.Propagation.KeyFactory.STRING;
import static brave.propagation.W3CFormat.writeW3CFormat;
import static java.util.Arrays.asList;

public final class W3CPropagation<K> implements Propagation<K> {

  public static final Propagation<String> W3C_STRING = W3CPropagation.newFactory().create(STRING);

  public static Propagation.Factory newFactory() {
    return new Factory();
  }

  static final String TRACE_PARENT_NAME = "traceparent";
  static final String TRACE_STATE_NAME = "tracestate";

  final K traceParentKey, traceStateKey;
  final List<K> fields;

  W3CPropagation(Propagation.KeyFactory<K> keyFactory) {
    traceParentKey = keyFactory.create(TRACE_PARENT_NAME);
    traceStateKey = keyFactory.create(TRACE_STATE_NAME);
    fields = Collections.unmodifiableList(asList(traceParentKey, traceStateKey));
  }

  @Override
  public List<K> keys() {
    return fields;
  }

  @Override
  public <C> TraceContext.Injector<C> injector(Setter<C, K> setter) {
    if (setter == null) throw new NullPointerException("setter == null");
    return new W3CInjector<>(this, setter);
  }

  static final class W3CInjector<C, K> implements TraceContext.Injector<C> {
    final W3CPropagation<K> propagation;
    final Setter<C, K> setter;

    W3CInjector(W3CPropagation<K> propagation, Setter<C, K> setter) {
      this.propagation = propagation;
      this.setter = setter;
    }

    @Override
    public void inject(TraceContext traceContext, C carrier) {
      setter.put(carrier, propagation.traceParentKey, writeW3CFormat(traceContext));
    }
  }

  @Override
  public <C> TraceContext.Extractor<C> extractor(Getter<C, K> getter) {
    return null;
  }

  static final class Factory extends Propagation.Factory {
    @Override
    public <K1> Propagation<K1> create(Propagation.KeyFactory<K1> keyFactory) {
      return new W3CPropagation<>(keyFactory);
    }

    @Override
    public boolean supportsJoin() {
      return true;
    }

    @Override
    public boolean requires128BitTraceId() {
      return true;
    }

    @Override
    public String toString() {
      return "W3CPropagationFactory";
    }
  }
}
