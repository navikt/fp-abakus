package no.nav.foreldrepenger.abakus.iay.tjeneste;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.domene.iay.ArbeidsgiverEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.arbeidsforhold.ArbeidsforholdDtoTjeneste;
import no.nav.foreldrepenger.abakus.kobling.KoblingLås;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.kobling.KoblingTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.virksomhet.VirksomhetTjeneste;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.ArbeidsforholdRef;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.ArbeidsforholdReferanse;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.Periode;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.request.AktørDatoRequest;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;

@Api(tags = "arbeidsforhold")
@Path("/arbeidsforhold/v1")
@ApplicationScoped
@Transaction
public class ArbeidsforholdRestTjeneste {

    private KoblingTjeneste koblingTjeneste;
    private InntektArbeidYtelseTjeneste iayTjeneste;
    private ArbeidsforholdDtoTjeneste dtoTjeneste;
    private VirksomhetTjeneste virksomhetTjeneste;

    public ArbeidsforholdRestTjeneste() {
    }

    @Inject
    public ArbeidsforholdRestTjeneste(KoblingTjeneste koblingTjeneste, InntektArbeidYtelseTjeneste iayTjeneste,
                                      ArbeidsforholdDtoTjeneste dtoTjeneste, VirksomhetTjeneste virksomhetTjeneste) {
        this.koblingTjeneste = koblingTjeneste;
        this.iayTjeneste = iayTjeneste;
        this.dtoTjeneste = dtoTjeneste;
        this.virksomhetTjeneste = virksomhetTjeneste;
    }

    @POST
    @Path("/arbeidstaker")
    @ApiOperation(value = "Gir ut alle arbeidsforhold i en gitt periode/dato for en gitt aktør. NB! Kaller direkte til aa-registeret")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response arbeidsforholdForReferanse(@NotNull @TilpassetAbacAttributt(supplierClass = AktørDatoRequestAbacDataSupplier.class) @Valid AktørDatoRequest request) {
        AktørId aktørId = new AktørId(request.getAktør().getIdent());
        Periode periode = request.getPeriode();

        LocalDate fom = periode.getFom();
        LocalDate tom = Objects.equals(fom, periode.getTom())
            ? fom.plusDays(1) // enkel dato søk
            : periode.getTom(); // periode søk

        var arbeidstakersArbeidsforhold = dtoTjeneste.mapFor(aktørId, fom, tom);
        return Response.ok(arbeidstakersArbeidsforhold).build();
    }

    @POST
    @Path("/referanse")
    @ApiOperation(value = "Finner eksisterende intern referanse for arbeidsforholdId eller lager en ny")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response referanseForArbeidsforhold(@NotNull @TilpassetAbacAttributt(supplierClass = ArbeidsforholdReferanseAbacDataSupplier.class) @Valid ArbeidsforholdReferanse request) {
        KoblingReferanse referanse = new KoblingReferanse(UUID.fromString(request.getReferanse().getReferanse()));
        KoblingLås koblingLås = koblingTjeneste.taSkrivesLås(referanse);

        ArbeidsforholdInformasjon arbeidsforholdInformasjon = iayTjeneste.hentArbeidsforholdInformasjonForKobling(referanse);

        ArbeidsforholdRef arbeidsforholdRef = arbeidsforholdInformasjon.finnEllerOpprett(tilArbeidsgiver(request),
            ArbeidsforholdRef.ref(request.getArbeidsforholdId()));

        var dto = dtoTjeneste.mapArbeidsforhold(request.getArbeidsgiver(), request.getArbeidsforholdId(), arbeidsforholdRef.getReferanse());
        koblingTjeneste.oppdaterLåsVersjon(koblingLås);

        return Response.ok(dto).build();
    }

    private Arbeidsgiver tilArbeidsgiver(@Valid @NotNull ArbeidsforholdReferanse request) {
        var arbeidsgiver = request.getArbeidsgiver();
        if (arbeidsgiver.getErOrganisasjon()) {
            return ArbeidsgiverEntitet.virksomhet(virksomhetTjeneste.hentOgLagreOrganisasjon(arbeidsgiver.getIdent()));
        }
        return ArbeidsgiverEntitet.person(new AktørId(arbeidsgiver.getIdent()));
    }

    private class AktørDatoRequestAbacDataSupplier implements Function<Object, AbacDataAttributter> {

        @Override
        public AbacDataAttributter apply(Object obj) {
            AktørDatoRequest req = (AktørDatoRequest) obj;
            return AbacDataAttributter.opprett().leggTilAktørId(req.getAktør().getIdent());
        }
    }

    private class ArbeidsforholdReferanseAbacDataSupplier implements Function<Object, AbacDataAttributter> {

        @Override
        public AbacDataAttributter apply(Object obj) {
            return AbacDataAttributter.opprett();
        }
    }
}
