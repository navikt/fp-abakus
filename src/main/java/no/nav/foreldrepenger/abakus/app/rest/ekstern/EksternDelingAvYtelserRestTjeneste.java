package no.nav.foreldrepenger.abakus.app.rest.ekstern;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import no.nav.abakus.vedtak.ytelse.Aktør;
import no.nav.abakus.vedtak.ytelse.Ytelse;
import no.nav.abakus.vedtak.ytelse.request.VedtakForPeriodeRequest;
import no.nav.abakus.vedtak.ytelse.v1.YtelseV1;
import no.nav.foreldrepenger.abakus.aktor.AktørTjeneste;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseRepository;
import no.nav.foreldrepenger.abakus.vedtak.extract.v1.ConvertToYtelseV1;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.AvailabilityType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;

@OpenAPIDefinition(tags = @Tag(name = "ytelse"), servers = @Server())
@Path("/ytelse/v1")
@ApplicationScoped
@Transactional
public class EksternDelingAvYtelserRestTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(EksternDelingAvYtelserRestTjeneste.class);

    private VedtakYtelseRepository ytelseRepository;
    private AktørTjeneste aktørTjeneste;

    public EksternDelingAvYtelserRestTjeneste() {
    } // CDI Ctor

    @Inject
    public EksternDelingAvYtelserRestTjeneste(VedtakYtelseRepository ytelseRepository,
                                              AktørTjeneste aktørTjeneste) {
        this.ytelseRepository = ytelseRepository;
        this.aktørTjeneste = aktørTjeneste;
    }

    @POST
    @Path("/hent-ytelse-vedtak")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(tags = "ytelse",
        description = "Henter alle vedtak for en gitt person, evt med periode etter en fom"
    )
    @RequestBody(required = true, description = "Vi godkjenner både aktørid og fnr som gyldig ident.", content = @Content(schema = @Schema(implementation = VedtakForPeriodeRequest.class)))
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Liste med vedtak som matcher kriteriene.",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = YtelseV1.class))))}
    )
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.APPLIKASJON, availabilityType = AvailabilityType.ALL, sporingslogg = true)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public List<Ytelse> hentVedtakYtelse(@NotNull @TilpassetAbacAttributt(supplierClass = EksternDelingAvYtelserRestTjeneste.VedtakForPeriodeRequestAbacDataSupplier.class) @Valid VedtakForPeriodeRequest request) {
        LOG.info("ABAKUS VEDTAK ekstern /hent-ytelse-vedtak for ytelser {}", request.getYtelser());

        if (request.getYtelser().isEmpty()) {
            return List.of();
        }

        Set<AktørId> aktørIder = utledAktørIdFraRequest(request.getIdent());
        var periode = IntervallEntitet.fraOgMedTilOgMed(request.getPeriode().getFom(), request.getPeriode().getTom());
        var ytelser = new ArrayList<Ytelse>();
        for (AktørId aktørId : aktørIder) {
            ytelser.addAll(ytelseRepository.hentYtelserForIPeriode(aktørId, periode)
                .stream()
                .filter(it -> request.getYtelser().contains(ConvertToYtelseV1.mapYtelser(it.getYtelseType())))
                .map(ConvertToYtelseV1::convert)
                .toList());
        }
        return ytelser;
    }


    private Set<AktørId> utledAktørIdFraRequest(Aktør aktør) {
        if (aktør.erAktørId()) {
            return Set.of(new AktørId(aktør.getVerdi()));
        }
        return aktørTjeneste.hentAktørIderForIdent(new PersonIdent(aktør.getVerdi()));
    }

    public static class VedtakForPeriodeRequestAbacDataSupplier implements Function<Object, AbacDataAttributter> {

        public VedtakForPeriodeRequestAbacDataSupplier() {
            // Jackson
        }

        @Override
        public AbacDataAttributter apply(Object obj) {
            var req = (VedtakForPeriodeRequest) obj;
            var attributeType = req.getIdent().erAktørId() ? StandardAbacAttributtType.AKTØR_ID : StandardAbacAttributtType.FNR;
            return AbacDataAttributter.opprett().leggTil(attributeType, req.getIdent().getVerdi());
        }
    }

}
