package no.nav.foreldrepenger.abakus.app.diagnostikk.rapportering;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.app.diagnostikk.DumpOutput;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.vedtak.felles.prosesstask.rest.AbacEmptySupplier;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;

@OpenAPIDefinition(tags = @Tag(name = "rapportering"))
@Path(RapporteringRestTjeneste.BASE_PATH)
@ApplicationScoped
@Transactional
public class RapporteringRestTjeneste {

    static final String BASE_PATH = "/rapportering";
    private static final Logger LOG = LoggerFactory.getLogger(RapporteringRestTjeneste.class);
    private static final DateTimeFormatter DT_FORMAT = new DateTimeFormatterBuilder().append(DateTimeFormatter.ISO_LOCAL_DATE)
        .appendLiteral('T')
        .appendPattern("HHmmss")
        .toFormatter();
    private Instance<RapportGenerator> rapportGenerators;

    public RapporteringRestTjeneste() {
        // for proxy
    }

    @Inject
    public RapporteringRestTjeneste(@Any Instance<RapportGenerator> rapportGenerators) {
        this.rapportGenerators = rapportGenerators;
    }

    @POST
    @Path("/generer")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(description = "Dumper en rapport av data", summary = ("Henter en dump av info for debugging og analyse av en sak"), tags = "rapportering")
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.DRIFT)
    public Response genererRapportForYtelse(@NotNull @FormParam("ytelseType") @Parameter(description = "ytelseType", required = true) @Valid @TilpassetAbacAttributt(supplierClass = AbacEmptySupplier.class) YtelseTypeKode ytelseTypeKode,
                                            @NotNull @FormParam("rapport") @Parameter(description = "rapport", required = true) @Valid @TilpassetAbacAttributt(supplierClass = AbacEmptySupplier.class) RapportType rapportType,
                                            @NotNull @FormParam("periode") @Parameter(description = "periode", required = true, example = "2020-01-01/2020-12-31") @Valid @TilpassetAbacAttributt(supplierClass = AbacEmptySupplier.class) IsoPeriode periode) {

        var ytelseType = YtelseType.fraKode(ytelseTypeKode.name());
        rapportType.valider(ytelseType);

        var generators = RapportTypeRef.Lookup.list(RapportGenerator.class, rapportGenerators, rapportType);

        List<DumpOutput> outputListe = new ArrayList<>();
        for (var generator : generators) {
            RapportGenerator g = generator.get();
            LOG.info("RapportGenerator [{}]({}), ytelse: {}", g.getClass().getName(), rapportType, ytelseType);
            var output = g.generer(ytelseType, IntervallEntitet.fra(periode.fom, periode.tom));
            outputListe.addAll(output);
        }

        var streamingOutput = new ZipOutput().dump(outputListe);

        return Response.ok(streamingOutput)
            .type(MediaType.APPLICATION_OCTET_STREAM)
            .header("Content-Disposition", String.format("attachment; filename=\"%s-%s-%s.zip\"", rapportType.name(), ytelseType.getKode(),
                LocalDateTime.now().format(DT_FORMAT)))
            .build();

    }

    public static class IsoPeriode {
        LocalDate fom;
        LocalDate tom;

        public IsoPeriode(String iso8601Periode) {
            String[] split = iso8601Periode.split("/");
            this.fom = LocalDate.parse(split[0]);
            this.tom = LocalDate.parse(split[1]);
        }
    }
}
