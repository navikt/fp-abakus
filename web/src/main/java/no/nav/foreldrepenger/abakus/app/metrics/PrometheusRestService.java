package no.nav.foreldrepenger.abakus.app.metrics;


import static no.nav.vedtak.log.metrics.MetricsUtil.REGISTRY;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/metrics")
@ApplicationScoped
public class PrometheusRestService {

    @GET
    @Operation(tags = "metrics", hidden = true)
    @Path("/prometheus")
    public String prometheus() {
        return REGISTRY.scrape();
    }
}
