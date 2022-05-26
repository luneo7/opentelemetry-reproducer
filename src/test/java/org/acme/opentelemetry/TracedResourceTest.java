package org.acme.opentelemetry;

import static io.opentelemetry.api.trace.SpanKind.CLIENT;
import static io.opentelemetry.api.trace.SpanKind.INTERNAL;
import static io.opentelemetry.api.trace.SpanKind.SERVER;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.opentelemetry.sdk.trace.data.SpanData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

import javax.inject.Inject;
import java.util.List;

@QuarkusTest
public class TracedResourceTest {
    @Inject
    TestSpanExporter spanExporter;

    @AfterEach
    void tearDown() {
        spanExporter.reset();
    }

    @Test
    public void testHelloEndpoint() {
        given()
                .when().get("/hello")
                .then()
                .statusCode(200)
                .body(is("hello"));

        List<SpanData> spans = spanExporter.getFinishedSpanItems(1);

        assertEquals("/hello", spans.get(0).getName());
        assertEquals(SERVER, spans.get(0).getKind());
    }

    @Test
    public void testHelloWorkerPoolEndpoint() {
        given()
                .when().get("/hello-worker-pool")
                .then()
                .statusCode(200)
                .body(is("hello"));

        List<SpanData> spans = spanExporter.getFinishedSpanItems(2);

        assertEquals("getBlocking", spans.get(0).getName());
        assertEquals(INTERNAL, spans.get(0).getKind());
        assertEquals("/hello-worker-pool", spans.get(1).getName());
        assertEquals(SERVER, spans.get(1).getKind());
        assertEquals(spans.get(0).getParentSpanId(), spans.get(1).getSpanId());
    }

    @Test
    public void testHelloDefaultExecutorEndpoint() {
        given()
                .when().get("/hello-default-executor")
                .then()
                .statusCode(200)
                .body(is("hello"));

        List<SpanData> spans = spanExporter.getFinishedSpanItems(2);

        assertEquals("getBlocking", spans.get(0).getName());
        assertEquals(INTERNAL, spans.get(0).getKind());
        assertEquals("/hello-default-executor", spans.get(1).getName());
        assertEquals(SERVER, spans.get(1).getKind());
        assertEquals(spans.get(0).getParentSpanId(), spans.get(1).getSpanId());
    }


    @Test
    public void testChainEndpoint() {
        given()
                .when().get("/chain")
                .then()
                .statusCode(200)
                .body(is("chain -> hello"));

        List<SpanData> spans = spanExporter.getFinishedSpanItems(3);

        assertEquals("/hello", spans.get(0).getName());
        assertEquals(SERVER, spans.get(0).getKind());
        assertEquals("/hello", spans.get(1).getName());
        assertEquals(CLIENT, spans.get(1).getKind());
        assertEquals("/chain", spans.get(2).getName());
        assertEquals(SERVER, spans.get(2).getKind());
        assertEquals(spans.get(0).getParentSpanId(), spans.get(1).getSpanId());
        assertEquals(spans.get(1).getParentSpanId(), spans.get(2).getSpanId());
    }

}
