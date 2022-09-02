package no.nav.foreldrepenger.abakus.app.vedlikehold;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static no.nav.foreldrepenger.abakus.felles.sikkerhet.AbakusBeskyttetRessursAttributt.DRIFT;
import static no.nav.foreldrepenger.abakus.felles.sikkerhet.AbakusBeskyttetRessursAttributt.GRUNNLAG;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.CREATE;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.UPDATE;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlagBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektsmeldingAggregat;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdReferanse;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.InntektsmeldingBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjening;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.typer.InternArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.JournalpostId;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;

@Path("/forvaltning")
@ApplicationScoped
@Transactional
public class ForvaltningRestTjeneste {

    private static final String GAMMEL = "gammel";
    private static final String GJELDENDE = "gjeldende";

    private InntektArbeidYtelseTjeneste iayTjeneste;

    private EntityManager entityManager;

    public ForvaltningRestTjeneste() {
        // For CDI
    }

    @Inject
    public ForvaltningRestTjeneste(EntityManager entityManager,
                                   InntektArbeidYtelseTjeneste iayTjeneste) {
        this.entityManager = entityManager;
        this.iayTjeneste = iayTjeneste;
    }

    @POST
    @Path("/settVarigEndring")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(description = "Setter oppgitt opptjening til å være varig endring",
        tags = "FORVALTNING",
        responses = {
            @ApiResponse(responseCode = "200", description = "Forekomster av utgått aktørid erstattet.")
        })
    @BeskyttetRessurs(action = UPDATE, resource = GRUNNLAG)
    public Response setVarigEndring(@TilpassetAbacAttributt(supplierClass = ForvaltningRestTjeneste.AbacDataSupplier.class) @NotNull @Valid VarigEndringRequest request) {
        var oppgittOpptjeningEksternReferanse = request.getEksternReferanse().toUuidReferanse();
        var org = new OrgNummer(request.getOrgnummer());
        OppgittOpptjening oppgittOpptjening = iayTjeneste.hentOppgittOpptjeningFor(oppgittOpptjeningEksternReferanse).orElseThrow();
        var næring = oppgittOpptjening.getEgenNæring().stream().filter(n -> n.getOrgnummer().equals(org)).findFirst().orElseThrow();
        if (næring.getVarigEndring()) {
            throw new IllegalArgumentException("Allerede varig endring");
        }
        int antall = entityManager.createNativeQuery(
                "UPDATE iay_egen_naering SET varig_endring = 'J', endring_dato = :edato , brutto_inntekt = :belop, endret_av = :begr WHERE id = :enid")
            .setParameter("edato", request.getEndringDato())
            .setParameter("belop", request.getBruttoInntekt())
            .setParameter("begr", request.getEndringBegrunnelse())
            .setParameter("enid", næring.getId())
            .executeUpdate();
        return Response.ok(antall).build();
    }

    @POST
    @Path("/eliminerInntektsmelding")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(description = "Fjerner angitt inntektsmelding/journalpost fra grunnlag",
        tags = "FORVALTNING",
        responses = {
            @ApiResponse(responseCode = "200", description = "Inntektsmelding eliminert.")
        })
    @BeskyttetRessurs(action = UPDATE, resource = GRUNNLAG)
    public Response eliminerInntektsmelding(@TilpassetAbacAttributt(supplierClass = ForvaltningRestTjeneste.AbacDataSupplier.class) @NotNull @Valid EliminerInntektsmeldingRequest request) {
        var koblingReferanse = new KoblingReferanse(request.getEksternReferanse().toUuidReferanse());
        var journalpost = new JournalpostId(request.getJournalpostId());
        var eksisterende = iayTjeneste.hentGrunnlagFor(koblingReferanse).orElseThrow();
        var grunnlagBuilder = InntektArbeidYtelseGrunnlagBuilder.oppdatere(eksisterende);
        eksisterende.getInntektsmeldinger()
            .map(InntektsmeldingAggregat::getInntektsmeldinger).orElse(List.of()).stream()
            .filter(im -> journalpost.equals(im.getJournalpostId()))
            .findFirst().orElseThrow();
        var beholdIM = eksisterende.getInntektsmeldinger()
            .map(InntektsmeldingAggregat::getInntektsmeldinger).orElse(List.of()).stream()
            .filter(im -> !journalpost.equals(im.getJournalpostId()))
            .collect(Collectors.toList());
        grunnlagBuilder.setInntektsmeldinger(new InntektsmeldingAggregat(beholdIM));
        iayTjeneste.lagre(koblingReferanse, grunnlagBuilder);
        return Response.ok().build();
    }


    @POST
    @Path("/oppdaterAktoerId")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(description = "MERGE: Oppdaterer aktørid for bruker i nødvendige tabeller",
        tags = "FORVALTNING",
        responses = {
            @ApiResponse(responseCode = "200", description = "Forekomster av utgått aktørid erstattet.")
        })
    @BeskyttetRessurs(action = CREATE, resource = DRIFT)
    public Response oppdaterAktoerId(@TilpassetAbacAttributt(supplierClass = ForvaltningRestTjeneste.AktørRequestAbacDataSupplier.class) @NotNull @Valid ByttAktørRequest request) {
        int antall = oppdaterAktørIdFor(request.getUtgåttAktør().getVerdi(), request.getGyldigAktør().getVerdi());
        return Response.ok(antall).build();
    }

    @POST
    @Path("/migrerArbeidsforholdRefForSak")
    @Consumes(TEXT_PLAIN)
    @Operation(description = "UPDATE: Endrer referanser på IM til referanse som finnes i aareg ved match med ignore case",
        tags = "FORVALTNING")
    @BeskyttetRessurs(action = CREATE, resource = DRIFT)
    public Response migrerArbeidsforholdRefForSak(@TilpassetAbacAttributt(supplierClass = ForvaltningRestTjeneste.AbacDataSupplier.class) @NotNull @Valid UUID koblingReferanse) {

        var inntektArbeidYtelseGrunnlag = iayTjeneste.hentAggregat(new KoblingReferanse(koblingReferanse));

        var iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagBuilder.oppdatere(inntektArbeidYtelseGrunnlag);

        var arbeidsforholdInformasjonOpt = inntektArbeidYtelseGrunnlag.getArbeidsforholdInformasjon();

        if (arbeidsforholdInformasjonOpt.isEmpty()) {
            return Response.notModified().build();
        }

        var arbeidsforholdInformasjon = arbeidsforholdInformasjonOpt.get();
        var referanserForKobling = arbeidsforholdInformasjon
            .getArbeidsforholdReferanser();

        var referansePrArbeidsgiverMap = referanserForKobling.stream()
            .collect(Collectors.groupingBy(ArbeidsforholdReferanse::getArbeidsgiver));

        var likeReferanserIgnoreCase = referanserForKobling
            .stream()
            .filter(r -> referansePrArbeidsgiverMap.get(r.getArbeidsgiver()).stream().anyMatch(r2 -> likeHvisIgnoreCaseEllersIkke(r, r2)))
            .collect(Collectors.toSet());


        var alleYrkesaktiviteter = inntektArbeidYtelseGrunnlag.getRegisterVersjon().stream().flatMap(i -> i.getAktørArbeid().stream())
            .flatMap(a -> a.hentAlleYrkesaktiviteter().stream())
            .collect(Collectors.toSet());

        var alleInntektsmeldinger = inntektArbeidYtelseGrunnlag.getInntektsmeldinger()
            .map(InntektsmeldingAggregat::getInntektsmeldinger).orElse(Collections.emptyList());

        var referanserFraInntektsmelding = likeReferanserIgnoreCase.stream()
            .filter(r -> alleInntektsmeldinger.stream().anyMatch(im ->
                im.getArbeidsforholdRef().getReferanse() != null &&
                    im.getArbeidsforholdRef().getReferanse().equals(r.getInternReferanse().getReferanse())))
            .collect(Collectors.toSet());


        var referanserFraAareg = likeReferanserIgnoreCase.stream()
            .filter(r -> alleYrkesaktiviteter.stream().anyMatch(y ->
                y.getArbeidsforholdRef().getReferanse() != null &&
                    y.getArbeidsforholdRef().getReferanse().equals(r.getInternReferanse().getReferanse())))
            .collect(Collectors.toSet());

        var referanserFraInntektsmeldingSomIkkeErIAareg = referanserFraInntektsmelding.stream()
            .filter(r -> referanserFraAareg.stream().noneMatch(r2 -> r2.getEksternReferanse().getReferanse().equals(r.getEksternReferanse().getReferanse())))
            .collect(Collectors.toSet());

        var feilTilRiktigMap = referanserFraInntektsmeldingSomIkkeErIAareg
            .stream()
            .collect(Collectors.toMap(
                r -> r.getInternReferanse().getReferanse(),
                r -> referanserFraAareg.stream().filter(r2 -> likeHvisIgnoreCaseEllersIkke(r, r2)).findFirst()
                    .map(ArbeidsforholdReferanse::getInternReferanse)
                    .map(InternArbeidsforholdRef::getReferanse).orElseThrow()));

        var oppdaterteInntektsmeldinger = alleInntektsmeldinger.stream()
            .map(im -> {
                if (harFeilInternReferanse(feilTilRiktigMap, im)) {
                    return lagNyInntektsmeldingMedRiktigReferanse(feilTilRiktigMap, im);
                }
                return im;
            }).collect(Collectors.toSet());

        iayGrunnlagBuilder.setInntektsmeldinger(new InntektsmeldingAggregat(oppdaterteInntektsmeldinger));
        var informasjonBuilder = ArbeidsforholdInformasjonBuilder.oppdatere(arbeidsforholdInformasjon);
        referanserForKobling.stream()
            .filter(r -> feilTilRiktigMap.containsKey(r.getInternReferanse().getReferanse()))
            .forEach(informasjonBuilder::fjernReferanse);


        iayTjeneste.lagre(new KoblingReferanse(koblingReferanse), iayGrunnlagBuilder);

        return Response.ok().build();
    }

    private Inntektsmelding lagNyInntektsmeldingMedRiktigReferanse(Map<String, String> feilTilRiktigMap, Inntektsmelding im) {
        return InntektsmeldingBuilder.kopi(im)
            .medArbeidsforholdId(InternArbeidsforholdRef.ref(feilTilRiktigMap.get(im.getArbeidsforholdRef().getReferanse())))
            .build();
    }

    private boolean harFeilInternReferanse(Map<String, String> feilTilRiktigMap, Inntektsmelding im) {
        return im.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold() && feilTilRiktigMap.keySet().stream().anyMatch(r -> r.equals(im.getArbeidsforholdRef().getReferanse()));
    }

    private boolean likeHvisIgnoreCaseEllersIkke(ArbeidsforholdReferanse r, ArbeidsforholdReferanse r2) {
        return !r2.getEksternReferanse().getReferanse().equals(r.getEksternReferanse().getReferanse())
            && r2.getEksternReferanse().getReferanse().equalsIgnoreCase(r.getEksternReferanse().getReferanse());
    }


    private int oppdaterAktørIdFor(String gammel, String gjeldende) {
        int antall = 0;
        antall += entityManager.createNativeQuery("UPDATE kobling SET bruker_aktoer_id = :gjeldende WHERE bruker_aktoer_id = :gammel")
            .setParameter(GJELDENDE, gjeldende).setParameter(GAMMEL, gammel).executeUpdate();
        antall += entityManager.createNativeQuery("UPDATE kobling SET bruker_aktoer_id = :gjeldende WHERE bruker_aktoer_id = :gammel")
            .setParameter(GJELDENDE, gjeldende).setParameter(GAMMEL, gammel).executeUpdate();
        antall += entityManager.createNativeQuery("UPDATE kobling SET annen_part_aktoer_id = :gjeldende WHERE annen_part_aktoer_id = :gammel")
            .setParameter(GJELDENDE, gjeldende).setParameter(GAMMEL, gammel).executeUpdate();
        antall += entityManager.createNativeQuery("UPDATE iay_aktoer_inntekt SET AKTOER_ID = :gjeldende WHERE AKTOER_ID = :gammel")
            .setParameter(GJELDENDE, gjeldende).setParameter(GAMMEL, gammel).executeUpdate();
        antall += entityManager.createNativeQuery("UPDATE iay_aktoer_arbeid SET AKTOER_ID = :gjeldende WHERE AKTOER_ID = :gammel")
            .setParameter(GJELDENDE, gjeldende).setParameter(GAMMEL, gammel).executeUpdate();
        antall += entityManager.createNativeQuery("UPDATE iay_aktoer_ytelse SET AKTOER_ID = :gjeldende WHERE AKTOER_ID = :gammel")
            .setParameter(GJELDENDE, gjeldende).setParameter(GAMMEL, gammel).executeUpdate();
        antall += entityManager.createNativeQuery("UPDATE vedtak_ytelse SET AKTOER_ID = :gjeldende WHERE AKTOER_ID = :gammel")
            .setParameter(GJELDENDE, gjeldende).setParameter(GAMMEL, gammel).executeUpdate();
        entityManager.flush();
        return antall;
    }

    public static class AbacDataSupplier implements Function<Object, AbacDataAttributter> {
        @Override
        public AbacDataAttributter apply(Object obj) {
            return AbacDataAttributter.opprett();
        }
    }

    public static class AktørRequestAbacDataSupplier implements Function<Object, AbacDataAttributter> {

        public AktørRequestAbacDataSupplier() {
        }

        @Override
        public AbacDataAttributter apply(Object obj) {
            var req = (ByttAktørRequest) obj;
            return AbacDataAttributter.opprett()
                .leggTil(StandardAbacAttributtType.AKTØR_ID, req.getUtgåttAktør().getVerdi())
                .leggTil(StandardAbacAttributtType.AKTØR_ID, req.getGyldigAktør().getVerdi());
        }
    }


}
