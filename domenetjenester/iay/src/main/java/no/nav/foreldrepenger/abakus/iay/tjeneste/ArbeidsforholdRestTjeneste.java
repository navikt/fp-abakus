package no.nav.foreldrepenger.abakus.iay.tjeneste;

import java.util.Objects;
import java.util.function.Function;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
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
import jakarta.ws.rs.core.Response;
import no.nav.abakus.iaygrunnlag.request.AktørDatoRequest;
import no.nav.foreldrepenger.abakus.felles.sikkerhet.IdentDataAttributter;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.arbeidsforhold.ArbeidsforholdDtoTjeneste;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.vedtak.log.mdc.MdcExtendedLogContext;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;

@OpenAPIDefinition(tags = @Tag(name = "arbeidsforhold"))
@Path("/arbeidsforhold/v1")
@ApplicationScoped
@Transactional
public class ArbeidsforholdRestTjeneste {
    private static final MdcExtendedLogContext LOG_CONTEXT = MdcExtendedLogContext.getContext("prosess");

    private ArbeidsforholdDtoTjeneste dtoTjeneste;

    ArbeidsforholdRestTjeneste() {
    } // CDI Ctor

    @Inject
    public ArbeidsforholdRestTjeneste(ArbeidsforholdDtoTjeneste dtoTjeneste) {
        this.dtoTjeneste = dtoTjeneste;
    }

    @POST
    @Path("/arbeidstakerMedPermisjoner")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Gir ut alle arbeidsforhold og permisjoner i en gitt periode/dato for en gitt aktør. NB! Proxyer direkte til aa-registeret / ingen bruk av sak/kobling i abakus", tags = "arbeidsforhold")
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.FAGSAK, sporingslogg = true)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentArbeidsforholdOgPermisjonerForEnPeriode(@NotNull @TilpassetAbacAttributt(supplierClass = AktørDatoRequestAbacDataSupplier.class) @Valid AktørDatoRequest request) {
        var aktørId = new AktørId(request.getAktør().getIdent());
        var periode = request.getPeriode();
        LOG_CONTEXT.add("ytelseType", request.getYtelse().getKode());
        LOG_CONTEXT.add("periode", periode);

        var fom = periode.getFom();
        var tom = Objects.equals(fom, periode.getTom()) ? fom.plusDays(1) // enkel dato søk
            : periode.getTom(); // periode søk
        var arbeidstakersArbeidsforhold = dtoTjeneste.mapArbForholdOgPermisjoner(aktørId, fom, tom);
        return Response.ok(arbeidstakersArbeidsforhold).build();
    }

    public static class AktørDatoRequestAbacDataSupplier implements Function<Object, AbacDataAttributter> {

        public AktørDatoRequestAbacDataSupplier() {
        }

        @Override
        public AbacDataAttributter apply(Object obj) {
            AktørDatoRequest req = (AktørDatoRequest) obj;
            return IdentDataAttributter.abacAttributterForPersonIdent(req.getAktør());
        }
    }
}
