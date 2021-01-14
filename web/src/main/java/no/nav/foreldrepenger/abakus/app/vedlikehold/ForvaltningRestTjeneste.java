package no.nav.foreldrepenger.abakus.app.vedlikehold;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.foreldrepenger.abakus.felles.sikkerhet.AbakusBeskyttetRessursAttributt.DRIFT;
import static no.nav.foreldrepenger.abakus.felles.sikkerhet.AbakusBeskyttetRessursAttributt.GRUNNLAG;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.CREATE;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.UPDATE;

import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektskildeType;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjening;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.lonnskomp.domene.LønnskompensasjonRepository;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
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
    private LønnskompensasjonRepository lønnskompensasjonRepository;

    public ForvaltningRestTjeneste() {
        // For CDI
    }

    @Inject
    public ForvaltningRestTjeneste(EntityManager entityManager,
                                   InntektArbeidYtelseTjeneste iayTjeneste) {
        this.entityManager = entityManager;
        this.iayTjeneste = iayTjeneste;
        this.lønnskompensasjonRepository = new LønnskompensasjonRepository(entityManager);
    }

    @POST
    @Path("/lonnskomp-sammenligning")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(description = "Vil innhente data fra lønnskompensasjon for sak i inntekt/sammenligning",
        tags = "FORVALTNING",
        responses = {
            @ApiResponse(responseCode = "200", description = "Oppdatert."),
            @ApiResponse(responseCode = "500", description = "Feilet pga ukjent feil.")
        })
    @BeskyttetRessurs(action = UPDATE, resource = GRUNNLAG)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response enableLønnskompSammenligningFor(@TilpassetAbacAttributt(supplierClass = ForvaltningRestTjeneste.SaksnummerAbacDto.class) @NotNull @Valid SaksnummerAbacDto request) {
        lønnskompensasjonRepository.lagreFilter(new Saksnummer(request.getSaksnummer()), InntektskildeType.INNTEKT_SAMMENLIGNING);
        return Response.ok().build();
    }

    @POST
    @Path("/lonnskomp-beregning")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(description = "Vil innhente data fra lønnskompensasjon for sak i inntekt/beregning",
        tags = "FORVALTNING",
        responses = {
            @ApiResponse(responseCode = "200", description = "Oppdatert."),
            @ApiResponse(responseCode = "500", description = "Feilet pga ukjent feil.")
        })
    @BeskyttetRessurs(action = UPDATE, resource = GRUNNLAG)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response enableLønnskompBeregningFor(@TilpassetAbacAttributt(supplierClass = ForvaltningRestTjeneste.SaksnummerAbacDto.class) @NotNull @Valid SaksnummerAbacDto request) {
        lønnskompensasjonRepository.lagreFilter(new Saksnummer(request.getSaksnummer()), InntektskildeType.INNTEKT_BEREGNING);
        return Response.ok().build();
    }

    @POST
    @Path("/lonnskomp-fjern")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(description = "Stans innhenting av data fra lønnskompensasjon for sak",
        tags = "FORVALTNING",
        responses = {
            @ApiResponse(responseCode = "200", description = "Oppdatert."),
            @ApiResponse(responseCode = "500", description = "Feilet pga ukjent feil.")
        })
    @BeskyttetRessurs(action = UPDATE, resource = GRUNNLAG)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response disableLønnskompFor(@TilpassetAbacAttributt(supplierClass = ForvaltningRestTjeneste.SaksnummerAbacDto.class) @NotNull @Valid SaksnummerAbacDto request) {
        int antall = entityManager.createNativeQuery("DELETE FROM lonnskomp_filter WHERE saksnummer = :saksnummer")
            .setParameter("saksnummer", request.getSaksnummer()).executeUpdate();
        return Response.ok(antall).build();
    }

    // TODO: FJERNE denne hvis behovet ikke reoppstår
    @POST
    @Path("/sett-egenn-virksomhet-type")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(description = "Oppdaterer manglende data i egen næring",
        tags = "FORVALTNING",
        responses = {
            @ApiResponse(responseCode = "200", description = "Oppdatert."),
            @ApiResponse(responseCode = "500", description = "Feilet pga ukjent feil.")
        })
    @BeskyttetRessurs(action = UPDATE, resource = GRUNNLAG)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response fiksVirksomhetTypeEgenN() {
        int antall = entityManager.createNativeQuery("UPDATE iay_egen_naering SET virksomhet_type = 'ANNEN' WHERE virksomhet_type is null")
            .executeUpdate();
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
            ByttAktørRequest req = (ByttAktørRequest) obj;
            return AbacDataAttributter.opprett()
                .leggTil(StandardAbacAttributtType.AKTØR_ID, req.getUtgåttAktør().getVerdi())
                .leggTil(StandardAbacAttributtType.AKTØR_ID, req.getGyldigAktør().getVerdi());
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class SaksnummerAbacDto implements Function<Object, AbacDataAttributter> {

        @JsonProperty(value = "saksnummer", required = true)
        @NotNull
        @Pattern(regexp = "^[A-Za-z0-9_\\.\\-]+$", message = "[${validatedValue}] matcher ikke tillatt pattern '{value}'")
        @Valid
        private String saksnummer;

        public SaksnummerAbacDto() {
            // NOSONAR
        }

        @JsonCreator
        public SaksnummerAbacDto(@JsonProperty(value = "saksnummer", required = true) @Valid @NotNull String saksnummer) {
            this.saksnummer = saksnummer;
        }

        public String getSaksnummer() {
            return saksnummer;
        }

        @Override
        public AbacDataAttributter apply(Object obj) {
            return AbacDataAttributter.opprett();
        }
    }
}
