package no.nav.foreldrepenger.abakus.registerdata.tjeneste;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import no.nav.foreldrepenger.abakus.domene.iay.GrunnlagReferanse;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.registerdata.tjeneste.dto.TaskResponsDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.AktørIdPersonident;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.FnrPersonident;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.Periode;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.PersonIdent;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.UuidDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.request.InnhentRegisterdataRequest;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.request.SjekkStatusRequest;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;

@OpenAPIDefinition(tags = @Tag(name = "registerinnhenting"))
@Path("/registerdata/v1")
@ApplicationScoped
@Transaction
public class RegisterdataRestTjeneste {

    private InnhentRegisterdataTjeneste innhentTjeneste;

    public RegisterdataRestTjeneste() {
    }

    @Inject
    public RegisterdataRestTjeneste(InnhentRegisterdataTjeneste innhentTjeneste) {
        this.innhentTjeneste = innhentTjeneste;
    }

    @POST
    @Path("/innhent/sync")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Trigger registerinnhenting for en gitt id", tags = "registerinnhenting")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response innhentRegisterdata(@Parameter(name = "innhent") @Valid InnhentRegisterdataAbacDto dto) {
        Optional<GrunnlagReferanse> innhent = innhentTjeneste.innhent(dto);
        if (innhent.isPresent()) {
            return Response.ok(new UuidDto(innhent.get().getReferanse().toString())).build();
        }
        return Response.noContent().build();
    }

    @POST
    @Path("/innhent/async")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Trigger registerinnhenting for en gitt id", tags = "registerinnhenting")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response innhentAsyncRegisterdata(@Parameter(name = "innhent") @Valid InnhentRegisterdataAbacDto dto) {
        String taskGruppe = innhentTjeneste.triggAsyncInnhent(dto);
        if (taskGruppe != null) {
            return Response.accepted(new TaskResponsDto(taskGruppe)).build();
        }
        return Response.noContent().build();
    }

    @POST
    @Path("/innhent/status")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Sjekker innhentingFerdig på async innhenting og gir siste referanseid på grunnlaget når tasken er ferdig. " +
        "Hvis ikke innhentingFerdig", tags = "registerinnhenting")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response innhentAsyncStatus(@Parameter(name = "status") @Valid SjekkStatusAbacDto dto) {
        if (innhentTjeneste.innhentingFerdig(dto.getTaskReferanse())) {
            Optional<GrunnlagReferanse> grunnlagReferanse = innhentTjeneste.hentSisteReferanseFor(new KoblingReferanse(dto.getReferanse().getReferanse()));
            if (grunnlagReferanse.isEmpty()) {
                return Response.noContent().build();
            }
            return Response.ok(new UuidDto(grunnlagReferanse.get().toString())).build();
        }
        return Response.status(425).build();
    }

    /**
     * Json bean med Abac.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
    public static class InnhentRegisterdataAbacDto extends InnhentRegisterdataRequest implements AbacDto {

        @JsonCreator
        public InnhentRegisterdataAbacDto(@JsonProperty(value = "saksnummer", required = true) @Valid @NotNull String saksnummer,
                                          @JsonProperty(value = "referanse", required = true) @Valid @NotNull UUID referanse,
                                          @JsonProperty(value = "ytelseType", required = true) @Valid @NotNull YtelseType ytelseType,
                                          @JsonProperty(value = "opplysningsperiode", required = true) @NotNull @Valid Periode opplysningsperiode,
                                          @JsonProperty(value = "aktør", required = true) @NotNull @Valid PersonIdent aktør) {
            super(saksnummer, referanse, ytelseType, opplysningsperiode, aktør);
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            AbacDataAttributter opprett = AbacDataAttributter.opprett();
            if (getAnnenPartAktør() != null) {
                leggTil(opprett, getAnnenPartAktør());
            }
            leggTil(opprett, getAktør());
            return opprett;
        }

        private void leggTil(AbacDataAttributter abac, PersonIdent person) {
            if (person != null) {
                if (FnrPersonident.IDENT_TYPE.equals(person.getIdentType())) {
                    abac.leggTil(StandardAbacAttributtType.FNR, person.getIdent());
                } else if (AktørIdPersonident.IDENT_TYPE.equals(person.getIdentType())) {
                    abac.leggTil(StandardAbacAttributtType.AKTØR_ID, person.getIdent());
                }
            }
        }

    }

    /**
     * Json bean med Abac.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
    public static class SjekkStatusAbacDto extends SjekkStatusRequest implements AbacDto {

        @JsonCreator
        public SjekkStatusAbacDto(@JsonProperty(value = "referanse", required = true) @Valid @NotNull UuidDto referanse,
                                  @JsonProperty(value = "taskReferanse", required = true) @NotNull @Pattern(regexp = "\\d+") String taskReferanse) {
            super(referanse, taskReferanse);
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            return AbacDataAttributter.opprett();
        }
    }


}
