package no.nav.foreldrepenger.abakus.app.vedlikehold;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.foreldrepenger.abakus.felles.sikkerhet.AbakusBeskyttetRessursAttributt.DRIFT;
import static no.nav.foreldrepenger.abakus.felles.sikkerhet.AbakusBeskyttetRessursAttributt.GRUNNLAG;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.CREATE;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.UPDATE;

import java.util.List;
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
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjening;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.felles.InfotrygdGrunnlagAggregator;
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
    @Path("/toggleTokenDebug")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(description = "Slår debugging av T-rex på/av",
        tags = "FORVALTNING",
        responses = {
            @ApiResponse(responseCode = "200", description = "Ny verdi for toggle.")
        })
    @BeskyttetRessurs(action = CREATE, resource = DRIFT)
    public Response toggleTokenDebug() {
        boolean toggle = InfotrygdGrunnlagAggregator.toggleDebugToken();
        return Response.ok(toggle).build();
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
