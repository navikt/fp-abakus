package no.nav.foreldrepenger.abakus.app.selftest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_HTML;

import java.time.Duration;
import java.time.Instant;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import no.nav.foreldrepenger.abakus.felles.FellesRestTjeneste;
import no.nav.foreldrepenger.abakus.felles.metrikker.MetrikkerTjeneste;

@Path("/selftest")
@RequestScoped
public class SelftestRestTjeneste extends FellesRestTjeneste {

    private SelftestService selftestService;

    public SelftestRestTjeneste() {
        // CDI
    }

    @Inject
    public SelftestRestTjeneste(SelftestService selftestService, MetrikkerTjeneste metrikkerTjeneste) {
        super(metrikkerTjeneste);
        this.selftestService = selftestService;
    }

    @GET
    @Produces({TEXT_HTML, APPLICATION_JSON})
    @Operation(description = "Sjekker systemavhengigheter", tags = "selftest", hidden = true)
    public Response doSelftest(@HeaderParam("Content-Type") String contentType, @QueryParam("json") boolean writeJsonAsHtml) {
        var startTx = Instant.now();

        final Response response = selftestService.doSelftest(contentType, writeJsonAsHtml);

        logMetrikk("/selftest", Duration.between(startTx, Instant.now()));
        return response;
    }


}
