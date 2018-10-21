package brave.context.rxjava2;

import brave.context.rxjava2.internal.fuseable.MaybeFuseable;
import brave.propagation.CurrentTraceContext;
import brave.propagation.CurrentTraceContext.Scope;
import brave.propagation.TraceContext;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

final class TraceContextFlowable<T> extends Flowable<T> {
  final Publisher<T> source;
  final CurrentTraceContext currentTraceContext;
  final TraceContext assemblyContext;

  TraceContextFlowable(
      Publisher<T> source, CurrentTraceContext currentTraceContext, TraceContext assemblyContext) {
    this.source = source;
    this.currentTraceContext = currentTraceContext;
    this.assemblyContext = assemblyContext;
  }

  @Override protected void subscribeActual(Subscriber s) {
    Scope scope = currentTraceContext.maybeScope(assemblyContext);
    try { // retrolambda can't resolve this try/finally
      source.subscribe(MaybeFuseable.get().wrap(s, currentTraceContext, assemblyContext));
    } finally {
      scope.close();
    }
  }
}