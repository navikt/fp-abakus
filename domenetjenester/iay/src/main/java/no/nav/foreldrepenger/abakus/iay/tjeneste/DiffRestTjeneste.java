package no.nav.foreldrepenger.abakus.iay.tjeneste;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.felles.diff.DiffResult;
import no.nav.foreldrepenger.abakus.iay.DiffInntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.HentDiffGrunnlagDto;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Api(tags = "diff")
@Path("/diff")
@ApplicationScoped
@Transaction
public class DiffRestTjeneste {

    private DiffInntektArbeidYtelseTjeneste diffTjeneste;

    public DiffRestTjeneste() {
    }

    @Inject
    public DiffRestTjeneste(DiffInntektArbeidYtelseTjeneste diffTjeneste) {
        this.diffTjeneste = diffTjeneste;
    }

    @POST
    @Path("/")
    @ApiOperation(value = "Gir diff resultat på sporrede endringer mellom to grunnlag")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response diffGrunnlag(@Valid HentDiffGrunnlagDto grunnlagDto) {
        InntektArbeidYtelseGrunnlag gammeltGrunnlag = diffTjeneste.hentAggregat(grunnlagDto.getGammelReferanse().getReferanse());
        InntektArbeidYtelseGrunnlag nyttGrunnlag = diffTjeneste.hentAggregat(grunnlagDto.getNyReferanse().getReferanse());

        // TODO: Mappe om til egnede objekter
        DiffResult diff = diffTjeneste.diff(gammeltGrunnlag, nyttGrunnlag);

        return Response.ok(diff).build();
    }

}
