package no.nav.foreldrepenger.abakus.kobling;

import java.net.HttpURLConnection;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

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
import no.nav.abakus.iaygrunnlag.AktørIdPersonident;
import no.nav.abakus.iaygrunnlag.FnrPersonident;
import no.nav.abakus.iaygrunnlag.PersonIdent;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.abakus.iaygrunnlag.request.AvsluttKoblingRequest;
import no.nav.foreldrepenger.abakus.felles.LoggUtil;
import no.nav.foreldrepenger.abakus.kobling.task.AvsluttKoblingTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;

@RequestScoped
@Transactional
@Path("/kobling/v1")
public class KoblingRestTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(KoblingRestTjeneste.class);

    private KoblingTjeneste koblingTjeneste;
    private ProsessTaskTjeneste prosessTaskTjeneste;

    KoblingRestTjeneste() {
        // CDI proxy
    }

    @Inject
    public KoblingRestTjeneste(KoblingTjeneste koblingTjeneste, ProsessTaskTjeneste prosessTaskTjeneste) {
        this.koblingTjeneste = koblingTjeneste;
        this.prosessTaskTjeneste = prosessTaskTjeneste;
    }

    @POST
    @Path("/avslutt")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @BeskyttetRessurs(actionType = ActionType.UPDATE, resourceType = ResourceType.FAGSAK)
    public Response deaktiverKobling(@Valid @NotNull KoblingRestTjeneste.AvsluttKoblingRequestAbacDto request) {
        LoggUtil.setupLogMdc(request.getYtelseType(), request.getSaksnummer());
        if (!YtelseType.abakusYtelser().contains(request.getYtelseType())) {
            LOG.warn("Ugyldig ytelseType: {}", request.getYtelseType());
            return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).build();
        }
        var kobling = hentKoblingOgValiderRequest(request);

        var avsluttKoblingTask = ProsessTaskData.forProsessTask(AvsluttKoblingTask.class);
        avsluttKoblingTask.setProperty(TaskConstants.KOBLING_ID, kobling.getId().toString());
        avsluttKoblingTask.setSaksnummer(kobling.getSaksnummer().getVerdi()); // Kun for logging
        avsluttKoblingTask.setBehandlingUUid(kobling.getKoblingReferanse().getReferanse()); // kun for logging

        prosessTaskTjeneste.lagre(avsluttKoblingTask);

        return Response.ok().build();
    }

    private Kobling hentKoblingOgValiderRequest(AvsluttKoblingRequestAbacDto request) {
        var koblingReferanse = new KoblingReferanse(request.getReferanse());
        var kobling = koblingTjeneste.hentFor(koblingReferanse)
            .orElseThrow(() -> new IllegalArgumentException("Kobling som skal deaktiveres finnes ikke."));

        if (!request.getSaksnummer().equals(kobling.getSaksnummer().getVerdi())) {
            throw new IllegalArgumentException("Prøver å avslutte kobling på feil saksnummer");
        }

        if (!request.getYtelseType().equals(kobling.getYtelseType())) {
            throw new IllegalArgumentException("Prøver å avslutte kobling på feil ytelsetype");
        }

        if (!request.getAktør().getIdent().equals(kobling.getAktørId().getId())) {
            throw new IllegalArgumentException("Prøver å avslutte kobling på feil aktør");
        }
        return kobling;
    }

    /**
     * Json bean med Abac.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class AvsluttKoblingRequestAbacDto extends AvsluttKoblingRequest implements AbacDto {

        @JsonCreator
        public AvsluttKoblingRequestAbacDto(@JsonProperty(value = "saksnummer", required = true) @Valid @NotNull String saksnummer,
                                            @JsonProperty(value = "referanse", required = true) @Valid @NotNull UUID referanse,
                                            @JsonProperty(value = "ytelseType", required = true) @NotNull YtelseType ytelseType,
                                            @JsonProperty(value = "aktør", required = true) @NotNull @Valid PersonIdent aktør) {
            super(saksnummer, referanse, ytelseType, aktør);
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            return lagAbacAttributter(this);
        }
    }

    private static AbacDataAttributter lagAbacAttributter(AvsluttKoblingRequestAbacDto dto) {
        var abacDataAttributter = AbacDataAttributter.opprett();

        abacDataAttributter.leggTil(StandardAbacAttributtType.SAKSNUMMER, dto.getSaksnummer());
        abacDataAttributter.leggTil(StandardAbacAttributtType.BEHANDLING_UUID, dto.getReferanse());

        String ident = dto.getAktør().getIdent();
        String identType = dto.getAktør().getIdentType();
        if (FnrPersonident.IDENT_TYPE.equals(identType)) {
            return abacDataAttributter.leggTil(StandardAbacAttributtType.FNR, ident);
        } else if (AktørIdPersonident.IDENT_TYPE.equals(identType)) {
            return abacDataAttributter.leggTil(StandardAbacAttributtType.AKTØR_ID, ident);
        }
        throw new java.lang.IllegalStateException("Ukjent identtype" + identType);
    }
}
