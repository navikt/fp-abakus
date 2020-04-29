package no.nav.foreldrepenger.abakus.iay.tjeneste;

import static no.nav.foreldrepenger.abakus.felles.sikkerhet.AbakusBeskyttetRessursAttributt.ARBEIDSFORHOLD;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.CREATE;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import no.nav.abakus.iaygrunnlag.ArbeidsforholdReferanse;
import no.nav.abakus.iaygrunnlag.Periode;
import no.nav.abakus.iaygrunnlag.request.AktørDatoRequest;
import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.felles.FellesRestTjeneste;
import no.nav.foreldrepenger.abakus.felles.metrikker.MetrikkerTjeneste;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.arbeidsforhold.ArbeidsforholdDtoTjeneste;
import no.nav.foreldrepenger.abakus.kobling.KoblingLås;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.kobling.KoblingTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.virksomhet.VirksomhetTjeneste;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.InternArbeidsforholdRef;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;

@OpenAPIDefinition(tags = @Tag(name = "arbeidsforhold"))
@Path("/arbeidsforhold/v1")
@ApplicationScoped
@Transactional
public class ArbeidsforholdRestTjeneste extends FellesRestTjeneste {

    private KoblingTjeneste koblingTjeneste;
    private InntektArbeidYtelseTjeneste iayTjeneste;
    private ArbeidsforholdDtoTjeneste dtoTjeneste;
    private VirksomhetTjeneste virksomhetTjeneste;

    public ArbeidsforholdRestTjeneste() {} // RESTEASY ctor

    @Inject
    public ArbeidsforholdRestTjeneste(KoblingTjeneste koblingTjeneste, InntektArbeidYtelseTjeneste iayTjeneste,
                                      ArbeidsforholdDtoTjeneste dtoTjeneste, VirksomhetTjeneste virksomhetTjeneste, MetrikkerTjeneste metrikkerTjeneste) {
        super(metrikkerTjeneste);
        this.koblingTjeneste = koblingTjeneste;
        this.iayTjeneste = iayTjeneste;
        this.dtoTjeneste = dtoTjeneste;
        this.virksomhetTjeneste = virksomhetTjeneste;
    }

    @POST
    @Path("/arbeidstaker")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Gir ut alle arbeidsforhold i en gitt periode/dato for en gitt aktør. NB! Kaller direkte til aa-registeret",
        tags = "arbeidsforhold")
    @BeskyttetRessurs(action = READ, resource = ARBEIDSFORHOLD, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentArbeidsforhold(@NotNull @TilpassetAbacAttributt(supplierClass = AktørDatoRequestAbacDataSupplier.class) @Valid AktørDatoRequest request) {
        var startTx = Instant.now();

        AktørId aktørId = new AktørId(request.getAktør().getIdent());
        Periode periode = request.getPeriode();

        LocalDate fom = periode.getFom();
        LocalDate tom = Objects.equals(fom, periode.getTom())
            ? fom.plusDays(1) // enkel dato søk
            : periode.getTom(); // periode søk
        var arbeidstakersArbeidsforhold = dtoTjeneste.mapFor(aktørId, fom, tom);
        final Response response = Response.ok(arbeidstakersArbeidsforhold).build();

        logMetrikk("/arbeidsforhold/v1/arbeidstaker", Duration.between(startTx, Instant.now()));
        return response;
    }

    @POST
    @Path("/referanse")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Finner eksisterende intern referanse for arbeidsforholdId eller lager en ny", tags = "arbeidsforhold")
    @BeskyttetRessurs(action = CREATE, resource = ARBEIDSFORHOLD, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response finnEllerOpprettArbeidsforholdReferanse(@NotNull @TilpassetAbacAttributt(supplierClass = ArbeidsforholdReferanseAbacDataSupplier.class) @Valid ArbeidsforholdReferanse request) {
        var startTx = Instant.now();

        KoblingReferanse referanse = new KoblingReferanse(UUID.fromString(request.getKoblingReferanse().getReferanse()));
        KoblingLås koblingLås = koblingTjeneste.taSkrivesLås(referanse);

        ArbeidsforholdInformasjon arbeidsforholdInformasjon = iayTjeneste.hentArbeidsforholdInformasjonForKobling(referanse);

        String abakusReferanse = request.getArbeidsforholdId().getAbakusReferanse();
        InternArbeidsforholdRef arbeidsforholdRef = arbeidsforholdInformasjon.finnEllerOpprett(tilArbeidsgiver(request),
            InternArbeidsforholdRef.ref(abakusReferanse));

        var dto = dtoTjeneste.mapArbeidsforhold(request.getArbeidsgiver(), abakusReferanse, arbeidsforholdRef.getReferanse());
        koblingTjeneste.oppdaterLåsVersjon(koblingLås);
        final Response response = Response.ok(dto).build();

        logMetrikk("/arbeidsforhold/v1/referanse", Duration.between(startTx, Instant.now()));
        return response;
    }

    private Arbeidsgiver tilArbeidsgiver(@Valid @NotNull ArbeidsforholdReferanse request) {
        var arbeidsgiver = request.getArbeidsgiver();
        if (arbeidsgiver.getErOrganisasjon()) {
            return Arbeidsgiver.virksomhet(virksomhetTjeneste.hentOgLagreOrganisasjon(arbeidsgiver.getIdent()));
        }
        return Arbeidsgiver.person(new AktørId(arbeidsgiver.getIdent()));
    }

    public static class AktørDatoRequestAbacDataSupplier implements Function<Object, AbacDataAttributter> {

        public AktørDatoRequestAbacDataSupplier() {
        }

        @Override
        public AbacDataAttributter apply(Object obj) {
            AktørDatoRequest req = (AktørDatoRequest) obj;
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.AKTØR_ID, req.getAktør().getIdent());
        }
    }

    public static class ArbeidsforholdReferanseAbacDataSupplier implements Function<Object, AbacDataAttributter> {
        public ArbeidsforholdReferanseAbacDataSupplier() {
        }

        @Override
        public AbacDataAttributter apply(Object obj) {
            return AbacDataAttributter.opprett();
        }
    }
}
