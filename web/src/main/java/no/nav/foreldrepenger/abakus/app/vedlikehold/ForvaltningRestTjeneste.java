package no.nav.foreldrepenger.abakus.app.vedlikehold;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.List;
import java.util.function.Function;

import no.nav.foreldrepenger.abakus.rydding.OppryddingTjeneste;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlagBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektsmeldingAggregat;
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
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;

@Path("/forvaltning")
@ApplicationScoped
@Transactional
public class ForvaltningRestTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(ForvaltningRestTjeneste.class);

    private static final String GAMMEL = "gammel";
    private static final String GJELDENDE = "gjeldende";

    private InntektArbeidYtelseTjeneste iayTjeneste;
    private OppryddingTjeneste oppryddingTjeneste;

    private EntityManager entityManager;

    public ForvaltningRestTjeneste() {
        // For CDI
    }

    @Inject
    public ForvaltningRestTjeneste(EntityManager entityManager,
                                   InntektArbeidYtelseTjeneste iayTjeneste,
                                   OppryddingTjeneste oppryddingTjeneste) {
        this.entityManager = entityManager;
        this.iayTjeneste = iayTjeneste;
        this.oppryddingTjeneste = oppryddingTjeneste;
    }

    @POST
    @Path("/settVarigEndring")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(description = "Setter oppgitt opptjening til å være varig endring", tags = "FORVALTNING", responses = {@ApiResponse(responseCode = "200", description = "Forekomster av utgått aktørid erstattet.")})
    @BeskyttetRessurs(actionType = ActionType.UPDATE, resourceType = ResourceType.FAGSAK, sporingslogg = true)
    public Response setVarigEndring(@TilpassetAbacAttributt(supplierClass = ForvaltningRestTjeneste.AbacDataSupplier.class) @NotNull @Valid VarigEndringRequest request) {
        var oppgittOpptjeningEksternReferanse = request.getEksternReferanse().toUuidReferanse();
        LOG.info("FORVALTNING ABAKUS settVarigEndring for {}", oppgittOpptjeningEksternReferanse);
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
    @BeskyttetRessurs(actionType = ActionType.UPDATE, resourceType = ResourceType.FAGSAK, sporingslogg = true)
    public Response eliminerInntektsmelding(@TilpassetAbacAttributt(supplierClass = ForvaltningRestTjeneste.AbacDataSupplier.class) @NotNull @Valid EliminerInntektsmeldingRequest request) {
        var koblingReferanse = new KoblingReferanse(request.getEksternReferanse().toUuidReferanse());
        LOG.info("FORVALTNING ABAKUS eliminerInntektsmelding for {}", koblingReferanse);
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
    @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.DRIFT, sporingslogg = true)
    public Response oppdaterAktoerId(@TilpassetAbacAttributt(supplierClass = ForvaltningRestTjeneste.AktørRequestAbacDataSupplier.class) @NotNull @Valid ByttAktørRequest request) {
        LOG.info("FORVALTNING ABAKUS oppdaterAktoerId");
        int antall = oppdaterAktørIdFor(request.getUtgåttAktør().getVerdi(), request.getGyldigAktør().getVerdi());
        return Response.ok(antall).build();
    }

    @POST
    @Path("/ryddOppGrunnlagUtenReferanse")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(description = "Trigger ryddejobben som fjerner alle inaktive grunnlag uten referanse", tags = "FORVALTNING", responses = {@ApiResponse(responseCode = "200", description = "Taskene blir opprettet og grunnlag blir slettet asynkront.")})
    @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.DRIFT, sporingslogg = true)
    public Response ryddOppGrunnlagUtenReferanse() {
        LOG.info("FORVALTNING ABAKUS ryddOppGrunnlagUtenReferanse");
        oppryddingTjeneste.fjernAlleIayAggregatUtenReferanse();
        oppryddingTjeneste.fjernAlleIayInformasjontUtenReferanse();
        return Response.ok().build();
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
