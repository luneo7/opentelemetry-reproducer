package org.acme.opentelemetry;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.smallrye.common.annotation.Blocking;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class Service {
    @Inject
    Tracer tracer;

    @Blocking
    public String getBlocking() {
        Span span = tracer.spanBuilder("getBlocking")
                          .startSpan();

        try (Scope ignored = span.makeCurrent()) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "hello";
        } finally {
            span.end();
        }
    }
}
