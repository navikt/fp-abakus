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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import no.nav.foreldrepenger.abakus.kodeverk.KodeverkRepository;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;


@Api(tags = "kodeverk")
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
    @ApiOperation(value = "Lister ut alle kodeverk og kodeverdier på disse")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response kodeverk() {

        Map<String, Set<String>> kodelister = kodeverkRepository.hentAlle();

        return Response.ok(kodelister.keySet()).build();
    }

    @GET
    @Path("/kodelister")
    @ApiOperation(value = "Lister ut alle kodeverk og kodeverdier på disse")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response kodelister() {

        Map<String, Set<String>> kodelister = kodeverkRepository.hentAlle();

        return Response.ok(kodelister).build();
    }
}
