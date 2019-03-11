package no.nav.foreldrepenger.abakus.iay.tjeneste;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import javax.enterprise.context.ApplicationScoped;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.Aktør;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.ReferanseDto;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Api(tags = "arbeidsforhold")
@Path("/arbeidsforhold")
@ApplicationScoped
@Transaction
public class ArbeidsforholdRestTjeneste {

    public ArbeidsforholdRestTjeneste() {
    }

    @GET
    @Path("/{referanse}/{aktørId}")
    @ApiOperation(value = "Gir ut registerdata som kobler til en ")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response innhentRegisterdata(@NotNull @Valid @PathParam("referanse") ReferanseDto referanse, @Valid @PathParam("aktørId") Aktør aktørId) {
        return Response.noContent().build();
    }

}
