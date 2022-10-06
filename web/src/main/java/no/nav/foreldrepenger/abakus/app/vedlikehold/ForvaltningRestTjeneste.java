package no.nav.foreldrepenger.abakus.app.vedlikehold;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.foreldrepenger.abakus.felles.sikkerhet.AbakusBeskyttetRessursAttributt.DRIFT;
import static no.nav.foreldrepenger.abakus.felles.sikkerhet.AbakusBeskyttetRessursAttributt.GRUNNLAG;

import java.util.Collections;
import java.util.List;
import java.util.Map;
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
import no.nav.abakus.iaygrunnlag.UuidDto;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlagBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektsmeldingAggregat;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdReferanse;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ForvaltningReferanseTjeneste;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.ForvaltningEndreInternReferanse;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittEgenNæring;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjening;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.kobling.KoblingTjeneste;
import no.nav.foreldrepenger.abakus.typer.InternArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.JournalpostId;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;

@Path("/forvaltning")
@ApplicationScoped
@Transactional
public class ForvaltningRestTjeneste {

    private static final String GAMMEL = "gammel";
    private static final String GJELDENDE = "gjeldende";

    private InntektArbeidYtelseTjeneste iayTjeneste;
    private KoblingTjeneste koblingTjeneste;

    private EntityManager entityManager;

    public ForvaltningRestTjeneste() {
        // For CDI
    }

    @Inject
    public ForvaltningRestTjeneste(EntityManager entityManager,
                                   InntektArbeidYtelseTjeneste iayTjeneste, KoblingTjeneste koblingTjeneste) {
        this.entityManager = entityManager;
        this.iayTjeneste = iayTjeneste;
        this.koblingTjeneste = koblingTjeneste;
    }

    @POST
    @Path("/vaskBegrunnelse")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(description = "Vasker begrunnelse for ugyldige tegn",
        tags = "FORVALTNING",
        responses = {
            @ApiResponse(responseCode = "200", description = "Forekomster av egen næring med vasket begrunnelse")
        })
    @BeskyttetRessurs(actionType = ActionType.CREATE, resource = DRIFT)
    public Response vaskBegrunnelse(@TilpassetAbacAttributt(supplierClass = ForvaltningRestTjeneste.AbacDataSupplier.class) @NotNull @Valid UuidDto eksternReferanse) {
        OppgittOpptjening oppgittOpptjening = iayTjeneste.hentOppgittOpptjeningFor(eksternReferanse.toUuidReferanse()).orElseThrow();
        var næringer = oppgittOpptjening.getEgenNæring().stream().filter(OppgittEgenNæring::getVarigEndring).toList();
        var antall = næringer.stream().map(næring -> {
            var begrunnelse = BegrunnelseVasker.vask(næring.getBegrunnelse());
                if (!begrunnelse.equals(næring.getBegrunnelse())) {
                    return entityManager.createNativeQuery(
                            "UPDATE iay_egen_naering SET begrunnelse = :begr WHERE id = :enid")
                        .setParameter("begr", begrunnelse)
                        .setParameter("enid", næring.getId())
                        .executeUpdate();
                }
                return 0;
            }
        ).reduce(Integer::sum).orElse(0);
        return Response.ok(antall).build();
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
    @BeskyttetRessurs(actionType = ActionType.UPDATE, resource = GRUNNLAG)
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
    @BeskyttetRessurs(actionType = ActionType.UPDATE, resource = GRUNNLAG)
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
    @BeskyttetRessurs(actionType = ActionType.CREATE, resource = DRIFT)
    public Response oppdaterAktoerId(@TilpassetAbacAttributt(supplierClass = ForvaltningRestTjeneste.AktørRequestAbacDataSupplier.class) @NotNull @Valid ByttAktørRequest request) {
        int antall = oppdaterAktørIdFor(request.getUtgåttAktør().getVerdi(), request.getGyldigAktør().getVerdi());
        return Response.ok(antall).build();
    }

    @POST
    @Path("/migrerArbeidsforholdRefForSak")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(description = "UPDATE: Endrer referanser på IM til referanse som finnes i aareg ved match med ignore case",
        tags = "FORVALTNING")
    @BeskyttetRessurs(actionType = ActionType.CREATE, resource = DRIFT)
    public Response migrerArbeidsforholdRefForSak(@TilpassetAbacAttributt(supplierClass = ForvaltningRestTjeneste.AbacDataSupplier.class) @NotNull @Valid UuidDto koblingReferanse) {
        Map<String, ArbeidsforholdReferanse> feilTilRiktigMap = finnMappingFraGammelTilNyReferanse(koblingReferanse);
        var kobling = koblingTjeneste.hentFor(new KoblingReferanse(koblingReferanse.getReferanse()));
        migrerAlleGrunnlagPåKobling(kobling.get(), feilTilRiktigMap);
        return Response.ok().build();
    }

    private Map<String, ArbeidsforholdReferanse> finnMappingFraGammelTilNyReferanse(UuidDto koblingReferanse) {
        var inntektArbeidYtelseGrunnlag = iayTjeneste.hentAggregat(new KoblingReferanse(koblingReferanse.getReferanse()));
        var arbeidsforholdInformasjonOpt = inntektArbeidYtelseGrunnlag.getArbeidsforholdInformasjon();
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

        return referanserFraInntektsmeldingSomIkkeErIAareg
            .stream()
            .collect(Collectors.toMap(
                r -> r.getInternReferanse().getReferanse(),
                r -> referanserFraAareg.stream().filter(r2 -> likeHvisIgnoreCaseEllersIkke(r, r2)).findFirst().orElseThrow()));
    }

    private void migrerAlleGrunnlagPåKobling(Kobling kobling, Map<String, ArbeidsforholdReferanse> feilTilRiktigMap) {
        var alleIayGrunnlag = iayTjeneste.hentAlleGrunnlagFor(kobling.getAktørId(),
            kobling.getSaksnummer(), kobling.getYtelseType(), false);
        for (var gr : alleIayGrunnlag) {
            migrerGrunnlag(feilTilRiktigMap, gr);
        }
    }

    private void migrerGrunnlag(Map<String, ArbeidsforholdReferanse> feilTilRiktigMap, InntektArbeidYtelseGrunnlag gr) {
        var inntektsmeldinger = gr.getInntektsmeldinger().stream().flatMap(im -> im.getInntektsmeldinger().stream()).toList();
        var informasjonOpt = gr.getArbeidsforholdInformasjon();
        inntektsmeldinger.forEach(im -> fjernFeilReferanseDersomEksisterer(feilTilRiktigMap, im));
        if (informasjonOpt.isPresent()) {
            // Fjerner referanse fra informasjon
            var informasjon = informasjonOpt.get();
            fjernFeilOgLeggTilRiktig(feilTilRiktigMap, informasjon);
        }
    }

    private void fjernFeilReferanseDersomEksisterer(Map<String, ArbeidsforholdReferanse> feilTilRiktigMap, Inntektsmelding im) {
        if (harFeilInternReferanse(feilTilRiktigMap, im)) {
            var feilReferanse = im.getArbeidsforholdRef();
            var riktigReferanse = feilTilRiktigMap.get(feilReferanse.getReferanse());
            // Endrer referanse for IM
            ForvaltningEndreInternReferanse.endreReferanse(im, riktigReferanse.getInternReferanse());
            entityManager.persist(im);
        }
    }

    private void fjernFeilOgLeggTilRiktig(Map<String, ArbeidsforholdReferanse> feilTilRiktigMap, ArbeidsforholdInformasjon informasjon) {
        if (harFeilReferanse(feilTilRiktigMap, informasjon)) {
            informasjon.getArbeidsforholdReferanser().stream()
                .filter(r -> feilTilRiktigMap.containsKey(r.getInternReferanse().getReferanse()))
                .forEach(ref -> {
                    var riktigReferanse = feilTilRiktigMap.get(ref.getInternReferanse().getReferanse());
                    var deleteQuery = entityManager.createNativeQuery("DELETE FROM IAY_ARBEIDSFORHOLD_REFER WHERE ID = :sletteId")
                        .setParameter("sletteId", ref.getId());
                    deleteQuery.executeUpdate();
                    if (!harInformasjonRiktigReferanse(informasjon, riktigReferanse.getInternReferanse())) {
                        var kopi = new ArbeidsforholdReferanse(riktigReferanse.getArbeidsgiver(), riktigReferanse.getInternReferanse(), riktigReferanse.getEksternReferanse());
                        ForvaltningReferanseTjeneste.leggTilReferanse(informasjon, kopi);
                    }
                });
            entityManager.persist(informasjon);
        }
    }

    private boolean harFeilReferanse(Map<String, ArbeidsforholdReferanse> feilTilRiktigMap, ArbeidsforholdInformasjon informasjon) {
        return informasjon.getArbeidsforholdReferanser().stream().anyMatch(r -> feilTilRiktigMap.containsKey(r.getInternReferanse().getReferanse()));
    }

    private boolean harInformasjonRiktigReferanse(ArbeidsforholdInformasjon informasjon,
                                                  InternArbeidsforholdRef riktigReferanse) {
        return informasjon.getArbeidsforholdReferanser().stream().anyMatch(r -> r.getInternReferanse().equals(riktigReferanse));
    }

    private boolean harFeilInternReferanse(Map<String, ArbeidsforholdReferanse> feilTilRiktigMap, Inntektsmelding im) {
        return im.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold() &&
            feilTilRiktigMap.keySet().stream().anyMatch(r -> r.equals(im.getArbeidsforholdRef().getReferanse()));
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
