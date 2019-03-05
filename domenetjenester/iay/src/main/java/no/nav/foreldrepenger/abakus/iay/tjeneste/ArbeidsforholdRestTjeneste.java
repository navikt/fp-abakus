package no.nav.foreldrepenger.abakus.iay.tjeneste;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import javax.enterprise.context.ApplicationScoped;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import no.nav.foreldrepenger.abakus.typer.AktørId;
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
    @Timed
    @Path("/{referanse}/{aktørId}")
    @ApiOperation(value = "Trigger registerinnhenting for en gitt id")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response innhentRegisterdata(@NotNull @PathParam("referanse") String referanse, @PathParam("aktørId") AktørId aktørId) {
        return Response.noContent().build();
    }

}
