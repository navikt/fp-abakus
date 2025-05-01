package no.nav.foreldrepenger.abakus.kobling;

import java.net.HttpURLConnection;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.RequestScoped;
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
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.abakus.iaygrunnlag.request.AvsluttKoblingRequest;
import no.nav.foreldrepenger.abakus.felles.LoggUtil;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;

@RequestScoped
@Transactional
@Path("/kobling/v1")
public class KoblingRestTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(KoblingRestTjeneste.class);

    private AvsluttKoblingTjeneste avsluttKoblingTjeneste;
    private KoblingTjeneste koblingTjeneste;

    KoblingRestTjeneste() {
        // CDI proxy
    }

    @Inject
    public KoblingRestTjeneste(KoblingTjeneste koblingTjeneste, AvsluttKoblingTjeneste avsluttKoblingTjeneste) {
        this.koblingTjeneste = koblingTjeneste;
        this.avsluttKoblingTjeneste = avsluttKoblingTjeneste;
    }

    @POST
    @Path("/avslutt")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @BeskyttetRessurs(actionType = ActionType.UPDATE, resourceType = ResourceType.FAGSAK, sporingslogg = true)
    public Response deaktiverKobling(@TilpassetAbacAttributt(supplierClass = AvsluttKoblingRequestAbacDataSupplier.class)
        @Valid @NotNull AvsluttKoblingRequest request) {
        LoggUtil.setupLogMdc(request.getYtelseType(), request.getSaksnummer(), request.getReferanse());
        if (!YtelseType.abakusYtelser().contains(request.getYtelseType())) {
            LOG.warn("Ugyldig ytelseType: {}", request.getYtelseType());
            return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).build();
        }
        var koblingReferanse = new KoblingReferanse(request.getReferanse());
        var koblingOptional = koblingTjeneste.hentFor(koblingReferanse);

        if (koblingOptional.isPresent()) {
            var kobling = koblingOptional.get();
            if (kobling.erAktiv()) {
                validerRequest(request, kobling);
                avsluttKoblingTjeneste.avsluttKobling(kobling.getKoblingReferanse());
                return Response.ok().build();
            } else {
                LOG.info("KOBLING. Kobling er allerede avsluttet for referanse: {}", request.getReferanse());
                return Response.noContent().build();
            }
        } else {
            LOG.info("KOBLING. Fant ikke kobling for referanse: {}", request.getReferanse());
            return Response.noContent().build();
        }
    }

    private static void validerRequest(AvsluttKoblingRequest request, Kobling kobling) {
        if (!request.getSaksnummer().equals(kobling.getSaksnummer().getVerdi())) {
            throw new IllegalArgumentException("Prøver å avslutte kobling på feil saksnummer");
        }

        if (!request.getYtelseType().equals(kobling.getYtelseType())) {
            throw new IllegalArgumentException("Prøver å avslutte kobling på feil ytelsetype");
        }

        if (!request.getAktør().getIdent().equals(kobling.getAktørId().getId())) {
            throw new IllegalArgumentException("Prøver å avslutte kobling på feil aktør");
        }
    }

    public static class AvsluttKoblingRequestAbacDataSupplier implements Function<Object, AbacDataAttributter> {

        @Override
        public AbacDataAttributter apply(Object obj) {
            var req = (AvsluttKoblingRequest) obj;
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.SAKSNUMMER, req.getSaksnummer());
        }
    }
}
