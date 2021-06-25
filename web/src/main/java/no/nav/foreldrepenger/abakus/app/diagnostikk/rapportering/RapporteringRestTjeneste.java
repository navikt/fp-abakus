package no.nav.foreldrepenger.abakus.app.diagnostikk.rapportering;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import no.nav.abakus.iaygrunnlag.Periode;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.app.diagnostikk.DumpOutput;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.felles.sikkerhet.AbakusBeskyttetRessursAttributt;
import no.nav.vedtak.felles.prosesstask.rest.AbacEmptySupplier;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;

@OpenAPIDefinition(tags = @Tag(name = "rapportering"))
@Path(RapporteringRestTjeneste.BASE_PATH)
@ApplicationScoped
@Transactional
public class RapporteringRestTjeneste {

    private static final Logger log = LoggerFactory.getLogger(RapporteringRestTjeneste.class);

    private static final DateTimeFormatter DT_FORMAT = new DateTimeFormatterBuilder()
        .append(DateTimeFormatter.ISO_LOCAL_DATE)
        .appendLiteral('T')
        .appendPattern("HHmmss")
        .toFormatter();

    static final String BASE_PATH = "/rapportering";

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
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.READ, resource = AbakusBeskyttetRessursAttributt.DRIFT)
    public Response genererRapportForYtelse(@NotNull @FormParam("ytelseType") @Parameter(description = "ytelseType", required = true) @Valid @TilpassetAbacAttributt(supplierClass = AbacEmptySupplier.class) YtelseTypeKode ytelseTypeKode,
                                            @NotNull @FormParam("rapport") @Parameter(description = "rapport", required = true) @Valid @TilpassetAbacAttributt(supplierClass = AbacEmptySupplier.class) RapportType rapportType,
                                            @NotNull @FormParam("periode") @Parameter(description = "periode", required = true, example = "2020-01-01/2020-12-31") @Valid @TilpassetAbacAttributt(supplierClass = AbacEmptySupplier.class) Periode periode) {

        var ytelseType = YtelseType.fraKode(ytelseTypeKode.name());
        rapportType.valider(ytelseType);

        var generators = RapportTypeRef.Lookup.list(RapportGenerator.class, rapportGenerators, rapportType);

        List<DumpOutput> outputListe = new ArrayList<>();
        for (var generator : generators) {
            RapportGenerator g = generator.get();
            log.info("RapportGenerator [{}]({}), ytelse: {}", g.getClass().getName(), rapportType, ytelseType);
            var output = g.generer(ytelseType, IntervallEntitet.fra(periode));
            outputListe.addAll(output);
        }

        var streamingOutput = new ZipOutput().dump(outputListe);

        return Response.ok(streamingOutput)
            .type(MediaType.APPLICATION_OCTET_STREAM)
            .header("Content-Disposition", String.format("attachment; filename=\"%s-%s-%s.zip\"", rapportType.name(), ytelseType.getKode(), LocalDateTime.now().format(DT_FORMAT)))
            .build();

    }

}
