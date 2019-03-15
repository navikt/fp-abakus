package no.nav.foreldrepenger.abakus.iay.tjeneste;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import javax.enterprise.context.ApplicationScoped;
import javax.validation.Valid;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.HentDiffGrunnlagDto;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Api(tags = "diff")
@Path("/diff")
@ApplicationScoped
@Transaction
public class DiffRestTjeneste {

    public DiffRestTjeneste() {
    }

    @POST
    @Path("/")
    @ApiOperation(value = "Gir ut registerdata som kobler til en ")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response diffGrunnlag(@Valid HentDiffGrunnlagDto grunnlagDto) {
        return Response.noContent().build();
    }

}
