package no.nav.foreldrepenger.abakus.iay.tjeneste;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.ArbeidsforholdForAktørIPeriodeDto;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.ArbeidsforholdForAktørPåDatoDto;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.ArbeidstakersArbeidsforholdDto;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.arbeidsforhold.ArbeidsforholdDtoTjeneste;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.PeriodeDto;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Api(tags = "arbeidsforhold")
@Path("/arbeidsforhold")
@ApplicationScoped
@Transaction
public class ArbeidsforholdRestTjeneste {

    private ArbeidsforholdDtoTjeneste dtoTjeneste;

    public ArbeidsforholdRestTjeneste() {
    }

    @Inject
    public ArbeidsforholdRestTjeneste(ArbeidsforholdDtoTjeneste dtoTjeneste) {
        this.dtoTjeneste = dtoTjeneste;
    }

    @POST
    @Path("/arbeidstaker")
    @ApiOperation(value = "Gir ut alle arbeidsforhold i en gitt periode for en gitt aktør. NB! Kaller direkte til aa-registeret")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response arbeidsforholdForReferanse(@NotNull @Valid ArbeidsforholdForAktørIPeriodeDto request) {
        AktørId aktørId = new AktørId(request.getAktør().getId());
        PeriodeDto periode = request.getPeriode();

        ArbeidstakersArbeidsforholdDto arbeidstakersArbeidsforhold = dtoTjeneste.mapFor(aktørId, periode.getFom(), periode.getTom());
        return Response.ok(arbeidstakersArbeidsforhold).build();
    }

    @POST
    @Path("/arbeidstaker")
    @ApiOperation(value = "Gir ut alle arbeidsforhold på en gitt dato for en gitt aktør. NB! Kaller direkte til aa-registeret")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response arbeidsforholdForReferanse(@NotNull @Valid ArbeidsforholdForAktørPåDatoDto request) {
        AktørId aktørId = new AktørId(request.getAktør().getId());

        ArbeidstakersArbeidsforholdDto arbeidstakersArbeidsforhold = dtoTjeneste.mapFor(aktørId, request.getDato(), request.getDato().plusDays(1));
        return Response.ok(arbeidstakersArbeidsforhold).build();
    }

}
