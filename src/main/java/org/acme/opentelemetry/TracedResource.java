package org.acme.opentelemetry;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

@Path("/")
public class TracedResource {

    private static final Logger LOG = Logger.getLogger(TracedResource.class);

    @Context
    private UriInfo uriInfo;

    @Inject
    Service service;

    @GET
    @Path("/hello")
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        LOG.info("hello");
        return "hello";
    }

    @GET
    @Path("/hello-worker-pool")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> helloWorkerPool() {
        LOG.info("hello-worker");
        return Uni.createFrom()
                  .item(service::getBlocking)
                  .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @GET
    @Path("/hello-default-executor")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> helloDefaultExecutor() {
        LOG.info("hello-worker");
        return Uni.createFrom()
                  .item(service::getBlocking)
                  .runSubscriptionOn(Infrastructure.getDefaultExecutor());
    }

    @GET
    @Path("/chain")
    @Produces(MediaType.TEXT_PLAIN)
    public String chain() {
        ResourceClient resourceClient = RestClientBuilder.newBuilder()
                                                         .baseUri(uriInfo.getBaseUri())
                                                         .build(ResourceClient.class);
        return "chain -> " + resourceClient.hello();
    }
}
