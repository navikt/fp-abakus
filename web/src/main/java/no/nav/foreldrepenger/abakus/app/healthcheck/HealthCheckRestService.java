package no.nav.foreldrepenger.abakus.app.healthcheck;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import no.nav.foreldrepenger.abakus.app.tjenester.ApplicationServiceStarter;
import no.nav.vedtak.log.metrics.LivenessAware;
import no.nav.vedtak.log.metrics.ReadinessAware;

@Path("/")
@Produces(TEXT_PLAIN)
@RequestScoped
public class HealthCheckRestService {

    private static final CacheControl CC = cacheControl();
    private static final Logger LOG = LoggerFactory.getLogger(HealthCheckRestService.class);
    private static final String RESPONSE_OK = "OK";

    private List<LivenessAware> live;
    private List<ReadinessAware> ready;
    private ApplicationServiceStarter starter;

    HealthCheckRestService() {
        // CDI
    }

    @Inject
    public HealthCheckRestService(ApplicationServiceStarter starter,
                                  @Any Instance<LivenessAware> livenessAware,
                                  @Any Instance<ReadinessAware> readinessAware) {
        this(starter, livenessAware.stream().toList(), readinessAware.stream().toList());
    }

    public HealthCheckRestService(ApplicationServiceStarter starter, List<LivenessAware> live, List<ReadinessAware> ready) {
        this.starter = starter;
        this.live = live;
        this.ready = ready;
    }

    private static CacheControl cacheControl() {
        var cc = new CacheControl();
        cc.setNoCache(true);
        cc.setNoStore(true);
        cc.setMustRevalidate(true);
        return cc;
    }

    @GET
    @Path("/health/isAlive")
    @Operation(description = "Sjekker om poden lever", tags = "nais", hidden = true)
    public Response isAlive() {
        if (live.stream().allMatch(LivenessAware::isAlive)) {
            return Response.ok(RESPONSE_OK).cacheControl(CC).build();
        }
        LOG.info("/isAlive NOK.");
        return Response.serverError().cacheControl(CC).build();
    }

    /**
     * @deprecated Både k9-verdikjede og fpsak-autotest trenger å bytte til det nye endepunkt /internal/health/isAlive
     */
    @Deprecated(since = "03.2023", forRemoval = true)
    @GET
    @Path("/isAlive")
    @Operation(description = "Sjekker om poden lever", tags = "nais", hidden = true)
    public Response isAliveDeprecated() {
        return isAlive();
    }

    @GET
    @Path("/health/isReady")
    @Operation(description = "sjekker om poden er klar", tags = "nais", hidden = true)
    public Response isReady() {
        if (ready.stream().allMatch(ReadinessAware::isReady)) {
            return Response.ok(RESPONSE_OK).cacheControl(CC).build();
        }
        LOG.info("/isReady NOK.");
        return Response.status(SERVICE_UNAVAILABLE).cacheControl(CC).build();
    }

    /**
     * @deprecated Både k9-verdikjede og fpsak-autotest trenger å bytte til det nye endepunkt /internal/health/isReady
     */
    @Deprecated(since = "03.2023", forRemoval = true)
    @GET
    @Path("/isReady")
    @Operation(description = "sjekker om poden er klar", tags = "nais", hidden = true)
    public Response isReadyDeprecated() {
        return isReady();
    }

    @GET
    @Path("/health/preStop")
    @Operation(description = "Kalles på før stopp", tags = "nais", hidden = true)
    public Response preStop() {
        LOG.info("/preStop endepunkt kalt.");
        starter.stopServices();
        return Response.ok(RESPONSE_OK).build();
    }
}