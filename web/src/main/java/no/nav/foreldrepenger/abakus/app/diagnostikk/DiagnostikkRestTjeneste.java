package no.nav.foreldrepenger.abakus.app.diagnostikk;

import com.fasterxml.jackson.annotation.JsonValue;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.aktor.AktørTjeneste;
import no.nav.foreldrepenger.abakus.felles.sikkerhet.AbakusBeskyttetRessursAttributt;
import no.nav.foreldrepenger.abakus.kobling.repository.KoblingRepository;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.function.Function;

@Path("/diagnostikk")
@ApplicationScoped
@Transactional
public class DiagnostikkRestTjeneste {

    private KoblingRepository koblingRepository;
    private DebugDumpsters dumpsters;
    private EntityManager entityManager;
    private AktørTjeneste aktørTjeneste;

    public DiagnostikkRestTjeneste() {
        // for proxy
    }

    @Inject
    public DiagnostikkRestTjeneste(AktørTjeneste aktørTjeneste,
                                   KoblingRepository fagsakRepository,
                                   EntityManager entityManager,
                                   DebugDumpsters dumpsters) {
        this.aktørTjeneste = aktørTjeneste;
        this.koblingRepository = fagsakRepository;
        this.entityManager = entityManager;
        this.dumpsters = dumpsters;
    }

    @POST
    @Path("/grunnlag")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Operation(description = "Henter en dump av info for debugging og analyse av en sak. Logger hvem som har hatt innsyn", summary = ("Henter en dump av info for debugging og analyse av en sak"), tags = "forvaltning")
    @BeskyttetRessurs(actionType = ActionType.READ, resource = AbakusBeskyttetRessursAttributt.DRIFT)
    public Response dumpSak(@NotNull @QueryParam("saksnummer") @Parameter(description = "saksnummer") @Valid @TilpassetAbacAttributt(supplierClass = AbacNoopSupplier.class) SaksnummerDto saksnummerDto,
                            @NotNull @QueryParam("aktørId") @Parameter(description = "aktørId") @Valid @TilpassetAbacAttributt(supplierClass = AbacAktørIdSupplier.class) AktørId aktørId,
                            @NotNull @QueryParam("ytelseType") @Parameter(description = "ytelseType") @Valid @TilpassetAbacAttributt(supplierClass = AbacNoopSupplier.class) YtelseType ytelseType) {

        var saksnummer = new Saksnummer(saksnummerDto.getVerdi());
        var kobling = koblingRepository.hentSisteKoblingReferanseFor(aktørId, saksnummer, ytelseType)
            .orElseThrow(
                () -> new IllegalArgumentException("Fant ikke kobling for saksnummer=" + saksnummer + ", aktørId og ytelseType=" + ytelseType));
        var ident = aktørTjeneste.hentIdentForAktør(aktørId, ytelseType).orElseThrow(); // skal ikke komme hit, bør feile forrige linje

        /*
         * logg tilgang til tabell - må gjøres før dumps (siden StreamingOutput ikke kjører i scope av denne metoden på stacken,
         * og derfor ikke har nytte av @Transactional.
         */
        entityManager.persist(new DiagnostikkLogg(kobling.getSaksnummer()));
        entityManager.flush();

        var streamingOutput = dumpsters.dumper(new DumpKontekst(kobling, ident));

        return Response.ok(streamingOutput)
            .type(MediaType.APPLICATION_OCTET_STREAM)
            .header("Content-Disposition",
                String.format("attachment; filename=\"abakus-%s-%s-v%s.zip\"", kobling.getYtelseType(), saksnummer.getVerdi(), kobling.getVersjon()))
            .build();
    }

    public static class AbacAktørIdSupplier implements Function<Object, AbacDataAttributter> {
        @Override
        public AbacDataAttributter apply(Object aktørId) {
            var id = (AktørId) aktørId;
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.AKTØR_ID, id.getId());
        }
    }

    public static class AbacNoopSupplier implements Function<Object, AbacDataAttributter> {
        @Override
        public AbacDataAttributter apply(Object saksnummer) {
            // sjekker p.t. ikke på saksnummer, kun aktørId siden vi uansett gjør oppslag her for å matche
            return AbacDataAttributter.opprett();
        }
    }

    public static class SaksnummerDto {

        @JsonValue
        @jakarta.validation.constraints.NotNull
        @Pattern(regexp = "^[0-9a-zA-Z:\\-]+$", message = "[${validatedValue}] matcher ikke tillatt pattern [{regexp}]")
        private final String verdi;

        public SaksnummerDto(String verdi) {
            this.verdi = verdi;
        }

        String getVerdi() {
            return this.verdi;
        }

    }

}
