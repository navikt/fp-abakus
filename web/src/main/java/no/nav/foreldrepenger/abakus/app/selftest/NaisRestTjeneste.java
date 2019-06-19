package no.nav.foreldrepenger.abakus.app.selftest;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import no.nav.foreldrepenger.abakus.app.konfig.ApplicationServiceStarter;
import no.nav.foreldrepenger.abakus.app.konfig.InternalApplication;

@Api(tags = {"nais"})
@Path("/")
@Produces(TEXT_PLAIN)
@RequestScoped
public class NaisRestTjeneste {

    private static final String RESPONSE_CACHE_KEY = "Cache-Control";
    private static final String RESPONSE_CACHE_VAL = "must-revalidate,no-cache,no-store";
    private static final String RESPONSE_OK = "OK";

    private static Logger logger = LoggerFactory.getLogger(InternalApplication.class);

    private ApplicationServiceStarter starterService;

    public NaisRestTjeneste() {
        // CDI
    }

    @Inject
    public NaisRestTjeneste(ApplicationServiceStarter starterService) {
        this.starterService = starterService;
    }

    @GET
    @Path("isAlive")
    public Response isAlive() {
        if (starterService.isKafkaAlive()) {
            logger.debug("Application is alive.");
            return Response
                .ok(RESPONSE_OK)
                .header(RESPONSE_CACHE_KEY, RESPONSE_CACHE_VAL)
                .build();
        } else {
            return Response
                .serverError()
                .header(RESPONSE_CACHE_KEY, RESPONSE_CACHE_VAL)
                .build();
        }
    }

    @GET
    @Path("isReady")
    public Response isReady() {
        if (starterService.isKafkaAlive()) {
            logger.debug("Application is alive.");
            return Response
                .ok(RESPONSE_OK)
                .header(RESPONSE_CACHE_KEY, RESPONSE_CACHE_VAL)
                .build();
        } else {
            return Response
                .serverError()
                .header(RESPONSE_CACHE_KEY, RESPONSE_CACHE_VAL)
                .build();
        }
    }

    @GET
    @Path("preStop")
    public Response preStop() {
        starterService.stopServices();
        return Response.ok(RESPONSE_OK).build();
    }

}
