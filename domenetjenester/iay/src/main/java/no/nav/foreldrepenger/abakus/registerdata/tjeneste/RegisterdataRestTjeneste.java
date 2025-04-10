package no.nav.foreldrepenger.abakus.registerdata.tjeneste;

import java.net.HttpURLConnection;
import java.util.function.Function;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.abakus.iaygrunnlag.request.InnhentRegisterdataRequest;
import no.nav.foreldrepenger.abakus.felles.LoggUtil;
import no.nav.foreldrepenger.abakus.felles.sikkerhet.IdentDataAttributter;
import no.nav.foreldrepenger.abakus.registerdata.tjeneste.dto.TaskResponsDto;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;

@OpenAPIDefinition(tags = @Tag(name = "registerinnhenting"))
@Path("/registerdata/v1")
@ApplicationScoped
@Transactional
public class RegisterdataRestTjeneste {

    private InnhentRegisterdataTjeneste innhentTjeneste;

    public RegisterdataRestTjeneste() {
    } // CDI ctor

    @Inject
    public RegisterdataRestTjeneste(InnhentRegisterdataTjeneste innhentTjeneste) {
        this.innhentTjeneste = innhentTjeneste;
    }

    @POST
    @Path("/innhent/async")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Trigger registerinnhenting for en gitt id", tags = "registerinnhenting")
    @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.APPLIKASJON, sporingslogg = true)
    @SuppressWarnings({"findsecbugs:JAXRS_ENDPOINT", "resource"})
    public Response innhentOgLagreRegisterdataAsync(@Parameter(name = "innhent") @Valid @TilpassetAbacAttributt(supplierClass = InnhentSupplier.class) InnhentRegisterdataRequest dto) {
        Response response;
        if (!YtelseType.abakusYtelser().contains(dto.getYtelseType())) {
            return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).build();
        }
        LoggUtil.setupLogMdc(dto.getYtelseType(), dto.getSaksnummer());
        String taskGruppe = innhentTjeneste.triggAsyncInnhent(dto);
        if (taskGruppe != null) {
            response = Response.accepted(new TaskResponsDto(taskGruppe)).build();
        } else {
            response = Response.noContent().build();
        }
        return response;
    }

    public static class InnhentSupplier implements Function<Object, AbacDataAttributter> {
        @Override
        public AbacDataAttributter apply(Object obj) {
            var req = (InnhentRegisterdataRequest) obj;
            return AbacDataAttributter.opprett()
                .leggTil(IdentDataAttributter.abacAttributterForPersonIdent(req.getAktør()))
                .leggTil(StandardAbacAttributtType.SAKSNUMMER, req.getSaksnummer());
        }
    }
}
