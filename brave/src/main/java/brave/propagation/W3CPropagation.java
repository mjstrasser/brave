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

import java.util.Collections;
import java.util.List;

import static brave.propagation.Propagation.KeyFactory.STRING;
import static brave.propagation.W3CFormat.writeTraceParent;
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
      if (carrier == null) throw new NullPointerException("carrier == null");
      setter.put(carrier, propagation.traceParentKey, writeTraceParent(traceContext));
      TraceState traceState = traceContext.findExtra(TraceState.class);
      if (traceState != null) {
        setter.put(carrier, propagation.traceStateKey, traceState.value);
      }
    }
  }

  @Override
  public <C> TraceContext.Extractor<C> extractor(Getter<C, K> getter) {
    if (getter == null) throw new NullPointerException("getter == null");
    return new W3CExtractor<>(this, getter);
  }

  static final class W3CExtractor<C, K> implements TraceContext.Extractor<C> {
    final W3CPropagation<K> propagation;
    final Getter<C, K> getter;

    W3CExtractor(W3CPropagation<K> propagation, Getter<C, K> getter) {
      this.propagation = propagation;
      this.getter = getter;
    }

    @Override
    public TraceContextOrSamplingFlags extract(C carrier) {
      if (carrier == null) throw new NullPointerException("carrier == null");

      String traceParent = getter.get(carrier, propagation.traceParentKey);
      TraceContext context = W3CFormat.parseTraceParent(traceParent);
      if (context == null) return TraceContextOrSamplingFlags.EMPTY;

      String traceState = getter.get(carrier, propagation.traceStateKey);
      if (traceState == null) {
        return TraceContextOrSamplingFlags.create(context);
      } else {
        return TraceContextOrSamplingFlags.create(
          context.toBuilder()
            .extra(Collections.singletonList(new TraceState(traceState)))
            .build()
        );
      }
    }
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

  static final class TraceState {
    final String value;

    TraceState(String value) {
      this.value = value;
    }
  }
}
