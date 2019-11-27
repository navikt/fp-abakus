package no.nav.foreldrepenger.abakus.kodeverk.tjeneste;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import no.nav.foreldrepenger.abakus.kodeverk.KodeverkRepository;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;


@OpenAPIDefinition(tags = {@Tag(name = "kodeverk")})
@Path("/kodeverk/v1")
@ApplicationScoped
@Transaction
public class KodeverkRestTjeneste {

    private KodeverkRepository kodeverkRepository;

    public KodeverkRestTjeneste() {
    }

    @Inject
    public KodeverkRestTjeneste(KodeverkRepository kodeverkRepository) {
        this.kodeverkRepository = kodeverkRepository;
    }

    @GET
    @Path("/kodeverk")
    @Operation(description = "Lister ut alle kodeverk og kodeverdier på disse", tags = "kodeverk")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response kodeverk() {

        Map<String, Set<String>> kodelister = kodeverkRepository.hentAlle();

        return Response.ok(kodelister.keySet()).build();
    }

    @GET
    @Path("/kodelister")
    @Operation(description = "Lister ut alle kodeverk og kodeverdier på disse", tags = "kodeverk")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response kodelister() {

        Map<String, Set<String>> kodelister = kodeverkRepository.hentAlle();

        return Response.ok(kodelister).build();
    }
}
