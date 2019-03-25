package no.nav.foreldrepenger.abakus.iay.tjeneste;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.HentArbeidsforholdForReferanseDto;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.IAYDtoTjeneste;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Api(tags = "arbeidsforhold")
@Path("/iay")
@ApplicationScoped
@Transaction
public class GrunnlagRestTjeneste {

    private InntektArbeidYtelseTjeneste iayTjeneste;
    private IAYDtoTjeneste dtoTjeneste;

    public GrunnlagRestTjeneste() {
    }

    @Inject
    public GrunnlagRestTjeneste(InntektArbeidYtelseTjeneste iayTjeneste, IAYDtoTjeneste dtoTjeneste) {
        this.iayTjeneste = iayTjeneste;
        this.dtoTjeneste = dtoTjeneste;
    }

    @POST
    @Path("/grunnlag")
    @ApiOperation(value = "Gir ut informasjom om alle arbeidsforhold på referansen for kobling/grunnlag")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response arbeidsforholdForReferanse(@NotNull @Valid HentArbeidsforholdForReferanseDto referanse) {
        UUID uuid = UUID.fromString(referanse.getReferanseDto().getReferanse());
        InntektArbeidYtelseGrunnlag grunnlag = iayTjeneste.hentAggregat(uuid);

        return Response.ok(dtoTjeneste.mapTil(grunnlag, iayTjeneste.hentKoblingIdFor(uuid))).build();
    }

}
