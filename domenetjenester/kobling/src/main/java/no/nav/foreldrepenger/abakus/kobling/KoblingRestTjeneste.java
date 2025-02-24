package no.nav.foreldrepenger.abakus.kobling;

import java.net.HttpURLConnection;
import java.util.Optional;
import java.util.UUID;

import no.nav.vedtak.exception.TekniskException;

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
import no.nav.abakus.iaygrunnlag.request.AvsluttGrunnlagRequest;
import no.nav.foreldrepenger.abakus.felles.LoggUtil;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;

import static no.nav.foreldrepenger.abakus.kobling.utils.KoblingUtil.validerIkkeAvsluttet;

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
    public Response deaktiverKobling(@Valid AvsluttGrunnlagRequestAbacDto dto) {
        LoggUtil.setupLogMdc(dto.getYtelseType(), dto.getSaksnummer());
        if (!YtelseType.abakusYtelser().contains(dto.getYtelseType())) {
            return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).build();
        }
        var koblingReferanse = new KoblingReferanse(dto.getReferanse());
        var koblingLås = koblingTjeneste.taSkrivesLås(koblingReferanse);
        var kobling = koblingTjeneste.hentFor(koblingReferanse)
            .orElseThrow(() -> new IllegalStateException("Kobling som skal deaktiveres finnes ikke."));
        validerIkkeAvsluttet(kobling);

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
        koblingTjeneste.oppdaterLåsVersjon(koblingLås);

        return Response.ok().build();
    }

    /**
     * Json bean med Abac.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class AvsluttGrunnlagRequestAbacDto extends AvsluttGrunnlagRequest implements AbacDto {

        @JsonCreator
        public AvsluttGrunnlagRequestAbacDto(@JsonProperty(value = "saksnummer", required = true) @Valid @NotNull String saksnummer,
                                             @JsonProperty(value = "referanse", required = true) @Valid @NotNull UUID referanse,
                                             @JsonProperty(value = "ytelseType", required = true) @NotNull YtelseType ytelseType,
                                             @JsonProperty(value = "aktør", required = true) @NotNull @Valid PersonIdent aktør) {
            super(saksnummer, referanse, ytelseType, aktør);
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            return lagAbacAttributter(getAktør());
        }
    }

    private static AbacDataAttributter lagAbacAttributter(PersonIdent person) {
        var abacDataAttributter = AbacDataAttributter.opprett();
        String ident = person.getIdent();
        String identType = person.getIdentType();
        if (FnrPersonident.IDENT_TYPE.equals(identType)) {
            return abacDataAttributter.leggTil(StandardAbacAttributtType.FNR, ident);
        } else if (AktørIdPersonident.IDENT_TYPE.equals(identType)) {
            return abacDataAttributter.leggTil(StandardAbacAttributtType.AKTØR_ID, ident);
        }
        throw new java.lang.IllegalStateException("Ukjent identtype" + identType);
    }
}
