package no.nav.foreldrepenger.abakus.app.selftest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_HTML;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@OpenAPIDefinition(tags = @Tag(name = "selftest"))
@Path("/selftest")
@RequestScoped
public class SelftestRestTjeneste {

    private SelftestService selftestService;

    public SelftestRestTjeneste() {
        // CDI
    }

    @Inject
    public SelftestRestTjeneste(SelftestService selftestService) {
        this.selftestService = selftestService;
    }

    @GET
    @Produces({TEXT_HTML, APPLICATION_JSON})
    @Operation(description = "Sjekker systemavhengigheter", tags = "selftest")
    public Response doSelftest(@HeaderParam("Content-Type") String contentType, @QueryParam("json") boolean writeJsonAsHtml) {
        return selftestService.doSelftest(contentType, writeJsonAsHtml);
    }


}
