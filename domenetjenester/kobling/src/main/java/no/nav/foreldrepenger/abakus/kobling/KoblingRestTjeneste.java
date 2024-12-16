package no.nav.foreldrepenger.abakus.kobling;

import java.net.HttpURLConnection;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.abakus.iaygrunnlag.request.AvsluttGrunnlagRequest;
import no.nav.foreldrepenger.abakus.felles.LoggUtil;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestScoped
@Transactional
@Path("/kobling")
public class KoblingRestTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(KoblingRestTjeneste.class);

    private KoblingTjeneste koblingTjeneste;

    KoblingRestTjeneste() {
        // CDI proxy
    }

    @Inject
    public KoblingRestTjeneste(KoblingTjeneste koblingTjeneste) {
        this.koblingTjeneste = koblingTjeneste;
    }

    @POST
    @Path("/avslutt")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @BeskyttetRessurs(actionType = ActionType.UPDATE, resourceType = ResourceType.FAGSAK)
    public Response deaktiverKobling(@Valid AvsluttGrunnlagRequest dto) {
        LoggUtil.setupLogMdc(dto.getYtelseType(), dto.getSaksnummer());
        if (!YtelseType.abakusYtelser().contains(dto.getYtelseType())) {
            return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).build();
        }
        var koblingReferanse = new KoblingReferanse(dto.getReferanse());
        var kobling = koblingTjeneste.hentFor(koblingReferanse).orElseThrow(() -> new IllegalStateException("Kobling som skal deaktiveres finnes ikke."));

        if (dto.getSaksnummer().equals(kobling.getSaksnummer().getVerdi())) {
            LOG.warn("Prøver å avslutte kobling på feil saksnummer {}", dto.getSaksnummer());
            throw new IllegalStateException("Prøver å avslutte kobling på feil saksnummer");
        }

        if (dto.getYtelseType().equals(kobling.getYtelseType())) {
            LOG.warn("Prøver å avslutte kobling på feil ytelsetype {}", dto.getYtelseType());
            throw new IllegalStateException("Prøver å avslutte kobling på feil ytelsetype");
        }

        if (dto.getAktør().getIdent().equals(kobling.getAktørId().getId())) {
            LOG.warn("Prøver å avslutte kobling på feil aktør {}", dto.getAktør().getIdent());
            throw new IllegalStateException("Prøver å avslutte kobling på feil aktør");
        }

        koblingTjeneste.deaktiver(koblingReferanse);

        return Response.ok().build();
    }
}
