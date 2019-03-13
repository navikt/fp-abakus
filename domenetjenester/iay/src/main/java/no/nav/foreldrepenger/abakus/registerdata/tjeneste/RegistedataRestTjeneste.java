package no.nav.foreldrepenger.abakus.registerdata.tjeneste;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import no.nav.foreldrepenger.abakus.registerdata.tjeneste.dto.InnhentRegisterdataDto;
import no.nav.foreldrepenger.abakus.registerdata.tjeneste.dto.ReferanseDto;
import no.nav.foreldrepenger.abakus.registerdata.tjeneste.dto.TaskResponsDto;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Api(tags = "registerdata")
@Path("/registerdata")
@ApplicationScoped
@Transaction
public class RegistedataRestTjeneste {

    private InnhentRegisterdataTjeneste innhentTjeneste;

    public RegistedataRestTjeneste() {
    }

    @Inject
    public RegistedataRestTjeneste(InnhentRegisterdataTjeneste innhentTjeneste) {
        this.innhentTjeneste = innhentTjeneste;
    }

    @POST
    @Path("/innhent/sync")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Trigger registerinnhenting for en gitt id")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response innhentRegisterdata(@ApiParam("innhent") @Valid InnhentRegisterdataDto dto) {
        Optional<UUID> innhent = innhentTjeneste.innhent(dto);
        if (innhent.isPresent()) {
            return Response.ok(new ReferanseDto(innhent.get().toString())).build();
        }
        return Response.noContent().build();
    }

    @POST
    @Path("/innhent/async")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Trigger registerinnhenting for en gitt id")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response innhentAsyncRegisterdata(@ApiParam("innhent") @Valid InnhentRegisterdataDto dto) {
        String taskGruppe = innhentTjeneste.triggAsyncInnhent(dto);
        if (taskGruppe != null) {
            return Response.accepted(new TaskResponsDto(taskGruppe)).build();
        }
        return Response.noContent().build();
    }

}
