package no.nav.foreldrepenger.abakus.app.vedlikehold;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.foreldrepenger.abakus.felles.sikkerhet.AbakusBeskyttetRessursAttributt.DRIFT;
import static no.nav.foreldrepenger.abakus.felles.sikkerhet.AbakusBeskyttetRessursAttributt.GRUNNLAG;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import no.nav.abakus.iaygrunnlag.UuidDto;
import no.nav.abakus.iaygrunnlag.kodeverk.UtbetaltNæringsYtelseType;
import no.nav.abakus.iaygrunnlag.kodeverk.UtbetaltYtelseFraOffentligeType;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlagBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektsmeldingAggregat;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittEgenNæring;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjening;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
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

    private EntityManager entityManager;

    public ForvaltningRestTjeneste() {
        // For CDI
    }

    @Inject
    public ForvaltningRestTjeneste(EntityManager entityManager, InntektArbeidYtelseTjeneste iayTjeneste) {
        this.entityManager = entityManager;
        this.iayTjeneste = iayTjeneste;
    }

    @POST
    @Path("/vaskBegrunnelse")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(description = "Vasker begrunnelse for ugyldige tegn", tags = "FORVALTNING", responses = {@ApiResponse(responseCode = "200", description = "Forekomster av egen næring med vasket begrunnelse")})
    @BeskyttetRessurs(actionType = ActionType.CREATE, resource = DRIFT)
    public Response vaskBegrunnelse(@TilpassetAbacAttributt(supplierClass = ForvaltningRestTjeneste.AbacDataSupplier.class) @NotNull @Valid UuidDto eksternReferanse) {
        var iayAggregat = iayTjeneste.hentAggregat(new KoblingReferanse(eksternReferanse.getReferanse()));
        var oppgittOpptjening = iayAggregat.getOppgittOpptjeningAggregat().stream().flatMap(oo -> oo.getOppgitteOpptjeninger().stream()).toList();
        var næringer = oppgittOpptjening.stream().flatMap(oo -> oo.getEgenNæring().stream().filter(OppgittEgenNæring::getVarigEndring)).toList();
        var antall = næringer.stream().map(næring -> {
            var begrunnelse = BegrunnelseVasker.vask(næring.getBegrunnelse());
            if (!begrunnelse.equals(næring.getBegrunnelse())) {
                return entityManager.createNativeQuery("UPDATE iay_egen_naering SET begrunnelse = :begr WHERE id = :enid")
                    .setParameter("begr", begrunnelse)
                    .setParameter("enid", næring.getId())
                    .executeUpdate();
            }
            return 0;
        }).reduce(Integer::sum).orElse(0);
        return Response.ok(antall).build();
    }

    @POST
    @Path("/settVarigEndring")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(description = "Setter oppgitt opptjening til å være varig endring", tags = "FORVALTNING", responses = {@ApiResponse(responseCode = "200", description = "Forekomster av utgått aktørid erstattet.")})
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
                "UPDATE iay_egen_naering SET varig_endring = 'J', endring_dato = :edato , brutto_inntekt = :belop, begrunnelse = :begr WHERE id = :enid")
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
    @Operation(description = "Fjerner angitt inntektsmelding/journalpost fra grunnlag", tags = "FORVALTNING", responses = {@ApiResponse(responseCode = "200", description = "Inntektsmelding eliminert.")})
    @BeskyttetRessurs(actionType = ActionType.UPDATE, resource = GRUNNLAG)
    public Response eliminerInntektsmelding(@TilpassetAbacAttributt(supplierClass = ForvaltningRestTjeneste.AbacDataSupplier.class) @NotNull @Valid EliminerInntektsmeldingRequest request) {
        var koblingReferanse = new KoblingReferanse(request.getEksternReferanse().toUuidReferanse());
        var journalpost = new JournalpostId(request.getJournalpostId());
        var eksisterende = iayTjeneste.hentGrunnlagFor(koblingReferanse).orElseThrow();
        var grunnlagBuilder = InntektArbeidYtelseGrunnlagBuilder.oppdatere(eksisterende);
        var sammeJpId = eksisterende.getInntektsmeldinger()
            .map(InntektsmeldingAggregat::getInntektsmeldinger)
            .orElse(List.of())
            .stream()
            .filter(im -> journalpost.equals(im.getJournalpostId()))
            .findFirst();
        if (sammeJpId.isEmpty()) {
            throw new IllegalArgumentException("Fant ingen inntektsmelding med angitt journalpostID");
        }
        var beholdIM = eksisterende.getInntektsmeldinger()
            .map(InntektsmeldingAggregat::getInntektsmeldinger)
            .orElse(List.of())
            .stream()
            .filter(im -> !journalpost.equals(im.getJournalpostId()))
            .toList();
        grunnlagBuilder.setInntektsmeldinger(new InntektsmeldingAggregat(beholdIM));
        iayTjeneste.lagre(koblingReferanse, grunnlagBuilder);
        return Response.ok().build();
    }


    @POST
    @Path("/oppdaterAktoerId")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(description = "MERGE: Oppdaterer aktørid for bruker i nødvendige tabeller", tags = "FORVALTNING", responses = {@ApiResponse(responseCode = "200", description = "Forekomster av utgått aktørid erstattet.")})
    @BeskyttetRessurs(actionType = ActionType.CREATE, resource = DRIFT)
    public Response oppdaterAktoerId(@TilpassetAbacAttributt(supplierClass = ForvaltningRestTjeneste.AktørRequestAbacDataSupplier.class) @NotNull @Valid ByttAktørRequest request) {
        int antall = oppdaterAktørIdFor(request.getUtgåttAktør().getVerdi(), request.getGyldigAktør().getVerdi());
        return Response.ok(antall).build();
    }

    @POST
    @Path("/migrerInntektspostNæring")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(description = "UPDATE: Endrer referanser på IM til referanse som finnes i aareg ved match med ignore case", tags = "FORVALTNING")
    @BeskyttetRessurs(actionType = ActionType.CREATE, resource = DRIFT)
    public Response migrerInntektspostNæring() {
        Map<UtbetaltNæringsYtelseType, List<UtbetaltNæringsYtelseType>> mapNæring = Map.of(
            UtbetaltNæringsYtelseType.SYKEPENGER_NÆRING, List.of(UtbetaltNæringsYtelseType.SYKEPENGER, UtbetaltNæringsYtelseType.SYKEPENGER_TIL_FISKER, UtbetaltNæringsYtelseType.SYKEPENGER_TIL_DAGMAMMA, UtbetaltNæringsYtelseType.SYKEPENGER_TIL_JORD_OG_SKOGBRUKERE),
            UtbetaltNæringsYtelseType.DAGPENGER_NÆRING, List.of(UtbetaltNæringsYtelseType.DAGPENGER_TIL_FISKER, UtbetaltNæringsYtelseType.DAGPENGER_VED_ARBEIDSLØSHET),
            UtbetaltNæringsYtelseType.VEDERLAG, List.of(UtbetaltNæringsYtelseType.VEDERLAG_DAGMAMMA_I_EGETHJEM)
        );
        int antall = 0;
        for (var e : mapNæring.entrySet()) {
            var key = e.getKey();
            for (var l : e.getValue()) {
                antall += oppdaterInntektsPost(l.getKodeverk(), l.getKode(), key.getKode());
            }
        }
        return Response.ok(antall).build();
    }

    @POST
    @Path("/migrerInntektspostYtelse")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(description = "UPDATE: Endrer referanser på IM til referanse som finnes i aareg ved match med ignore case", tags = "FORVALTNING")
    @BeskyttetRessurs(actionType = ActionType.CREATE, resource = DRIFT)
    public Response migrerInntektspostYtelse() {
        Map<UtbetaltYtelseFraOffentligeType, List<UtbetaltYtelseFraOffentligeType>> mapYtelse = Map.of(
            UtbetaltYtelseFraOffentligeType.SYKEPENGER, List.of(UtbetaltYtelseFraOffentligeType.SYKEPENGER_FISKER),
            UtbetaltYtelseFraOffentligeType.DAGPENGER, List.of(UtbetaltYtelseFraOffentligeType.DAGPENGER_ARBEIDSLØS, UtbetaltYtelseFraOffentligeType.DAGPENGER_FISKER)
        );
        int antall = 0;
        for (var e : mapYtelse.entrySet()) {
            var key = e.getKey();
            for (var l : e.getValue()) {
                antall += oppdaterInntektsPost(l.getKodeverk(), l.getKode(), key.getKode());
            }
        }
        return Response.ok(antall).build();
    }

    @POST
    @Path("/slettInntektspostUføre")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(description = "UPDATE: Endrer referanser på IM til referanse som finnes i aareg ved match med ignore case", tags = "FORVALTNING")
    @BeskyttetRessurs(actionType = ActionType.CREATE, resource = DRIFT)
    public Response slettInntektspostUføre() {
        int antall = entityManager.createNativeQuery("DELETE FROM IAY_INNTEKTSPOST WHERE ytelse_type in  ('UFØRETRYGD','UFØRETRYGD_ETTEROPPGJØR')")
            .executeUpdate();
        return Response.ok(antall).build();
    }

    private int oppdaterInntektsPost(String kodeverk, String gammel, String nykode) {
        return entityManager.createNativeQuery("UPDATE IAY_INNTEKTSPOST SET ytelse_type = :nykode WHERE KL_ytelse_type = :kodeverk and ytelse_type = :gammelkode")
            .setParameter("kodeverk", kodeverk)
            .setParameter("gammelkode", gammel)
            .setParameter("nykode", nykode)
            .executeUpdate();
    }

    private int oppdaterAktørIdFor(String gammel, String gjeldende) {
        int antall = 0;
        antall += entityManager.createNativeQuery("UPDATE kobling SET bruker_aktoer_id = :gjeldende WHERE bruker_aktoer_id = :gammel")
            .setParameter(GJELDENDE, gjeldende)
            .setParameter(GAMMEL, gammel)
            .executeUpdate();
        antall += entityManager.createNativeQuery("UPDATE kobling SET bruker_aktoer_id = :gjeldende WHERE bruker_aktoer_id = :gammel")
            .setParameter(GJELDENDE, gjeldende)
            .setParameter(GAMMEL, gammel)
            .executeUpdate();
        antall += entityManager.createNativeQuery("UPDATE kobling SET annen_part_aktoer_id = :gjeldende WHERE annen_part_aktoer_id = :gammel")
            .setParameter(GJELDENDE, gjeldende)
            .setParameter(GAMMEL, gammel)
            .executeUpdate();
        antall += entityManager.createNativeQuery("UPDATE iay_aktoer_inntekt SET AKTOER_ID = :gjeldende WHERE AKTOER_ID = :gammel")
            .setParameter(GJELDENDE, gjeldende)
            .setParameter(GAMMEL, gammel)
            .executeUpdate();
        antall += entityManager.createNativeQuery("UPDATE iay_aktoer_arbeid SET AKTOER_ID = :gjeldende WHERE AKTOER_ID = :gammel")
            .setParameter(GJELDENDE, gjeldende)
            .setParameter(GAMMEL, gammel)
            .executeUpdate();
        antall += entityManager.createNativeQuery("UPDATE iay_aktoer_ytelse SET AKTOER_ID = :gjeldende WHERE AKTOER_ID = :gammel")
            .setParameter(GJELDENDE, gjeldende)
            .setParameter(GAMMEL, gammel)
            .executeUpdate();
        antall += entityManager.createNativeQuery("UPDATE vedtak_ytelse SET AKTOER_ID = :gjeldende WHERE AKTOER_ID = :gammel")
            .setParameter(GJELDENDE, gjeldende)
            .setParameter(GAMMEL, gammel)
            .executeUpdate();
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
            // Jackson
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
