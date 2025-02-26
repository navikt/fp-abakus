package no.nav.foreldrepenger.abakus.iay.tjeneste;


import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import no.nav.abakus.iaygrunnlag.AktørIdPersonident;
import no.nav.abakus.iaygrunnlag.FnrPersonident;
import no.nav.abakus.iaygrunnlag.Periode;
import no.nav.abakus.iaygrunnlag.PersonIdent;
import no.nav.abakus.iaygrunnlag.arbeidsforhold.v1.ArbeidsforholdInformasjon;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.abakus.iaygrunnlag.request.Dataset;
import no.nav.abakus.iaygrunnlag.request.InntektArbeidYtelseGrunnlagRequest;
import no.nav.abakus.iaygrunnlag.request.KopierGrunnlagRequest;
import no.nav.abakus.iaygrunnlag.v1.InntektArbeidYtelseAggregatOverstyrtDto;
import no.nav.abakus.iaygrunnlag.v1.InntektArbeidYtelseGrunnlagDto;
import no.nav.abakus.iaygrunnlag.v1.OverstyrtInntektArbeidYtelseDto;
import no.nav.foreldrepenger.abakus.domene.iay.GrunnlagReferanse;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlagBuilder;
import no.nav.foreldrepenger.abakus.felles.LoggUtil;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.IAYFraDtoMapper;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.IAYTilDtoMapper;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.kobling.KoblingTjeneste;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;

@OpenAPIDefinition(tags = {@Tag(name = "iay-grunnlag")})
@Path("/iay/grunnlag/v1")
@ApplicationScoped
@Transactional
public class GrunnlagRestTjeneste {

    private InntektArbeidYtelseTjeneste iayTjeneste;
    private KoblingTjeneste koblingTjeneste;

    public GrunnlagRestTjeneste() {
    } // CDI Ctor

    @Inject
    public GrunnlagRestTjeneste(InntektArbeidYtelseTjeneste iayTjeneste, KoblingTjeneste koblingTjeneste) {
        this.iayTjeneste = iayTjeneste;
        this.koblingTjeneste = koblingTjeneste;
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

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Hent ett enkelt IAY Grunnlag for angitt spesifikasjon. Spesifikasjonen kan angit hvilke data som ønskes", tags = "iay-grunnlag", responses = {@ApiResponse(description = "InntektArbeidYtelseGrunnlagDto", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InntektArbeidYtelseGrunnlagDto.class))), @ApiResponse(responseCode = "204", description = "Det finnes ikke et grunnlag for forespørselen"), @ApiResponse(responseCode = "304", description = "Grunnlaget har ikke endret seg i henhold til det fagsystemet allerede kjenner")})
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.FAGSAK)
    @SuppressWarnings({"findsecbugs:JAXRS_ENDPOINT", "resource"})
    public Response hentIayGrunnlag(@NotNull @Valid InntektArbeidYtelseGrunnlagRequestAbacDto spesifikasjon) {
        Response response;

        LoggUtil.setupLogMdc(spesifikasjon.getYtelseType(), spesifikasjon.getSaksnummer(), spesifikasjon.getKoblingReferanse());

        var aktørId = new AktørId(spesifikasjon.getPerson().getIdent());

        final var forespurtGrunnlagReferanse = spesifikasjon.getGrunnlagReferanse();
        var grunnlagReferanse = forespurtGrunnlagReferanse != null ? new GrunnlagReferanse(forespurtGrunnlagReferanse) : null;
        var koblingReferanse = getKoblingReferanse(aktørId, spesifikasjon.getKoblingReferanse(), spesifikasjon.getGrunnlagReferanse());

        final var sisteKjenteGrunnlagReferanse = utledSisteKjenteGrunnlagReferanseFraSpesifikasjon(spesifikasjon);
        final var sistKjenteErAktivt = sisteKjenteGrunnlagReferanse != null && iayTjeneste.erGrunnlagAktivt(sisteKjenteGrunnlagReferanse);

        if (sisteKjenteGrunnlagReferanse != null && sistKjenteErAktivt) {
            response = Response.notModified().build();
        } else {
            var grunnlag = getGrunnlag(spesifikasjon, grunnlagReferanse, koblingReferanse);
            if (grunnlag != null) {
                var dtoMapper = new IAYTilDtoMapper(aktørId, grunnlagReferanse, koblingReferanse);
                var dto = dtoMapper.mapTilDto(grunnlag, spesifikasjon.getYtelseType(), spesifikasjon.getDataset());
                response = Response.ok(dto).build();
            } else {
                response = Response.noContent().build();
            }
        }

        return response;
    }

    @PUT
    @Path("/overstyrt")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Lagrer siste versjon", tags = "iay-grunnlag", responses = {@ApiResponse(responseCode = "200", description = "Mottatt grunnlaget")})
    @BeskyttetRessurs(actionType = ActionType.UPDATE, resourceType = ResourceType.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response oppdaterOgLagreOverstyring(@NotNull @Valid OverstyrtInntektArbeidYtelseAbacDto dto) {

        var aktørId = new AktørId(dto.getPerson().getIdent());
        var koblingReferanse = getKoblingReferanse(aktørId, dto.getKoblingReferanse(), dto.getGrunnlagReferanse());

        setupLogMdcFraKoblingReferanse(koblingReferanse);

        var nyttGrunnlagBuilder = InntektArbeidYtelseGrunnlagBuilder.oppdatere(iayTjeneste.hentGrunnlagFor(koblingReferanse));

        new IAYFraDtoMapper(iayTjeneste, aktørId, koblingReferanse).mapOverstyringerTilGrunnlagBuilder(dto.getOverstyrt(),
            dto.getArbeidsforholdInformasjon(), nyttGrunnlagBuilder);

        iayTjeneste.lagre(koblingReferanse, nyttGrunnlagBuilder);

        return Response.ok().build();
    }

    @POST
    @Path("/kopier")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Kopier grunnlag", tags = "iay-grunnlag")
    @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response kopierOgLagreGrunnlagUtenInntektsmeldinger(@NotNull @Valid KopierGrunnlagRequestAbac request) {
        kopierOgLagreGrunnlag(request, false);
        return Response.ok().build();
    }

    @POST
    @Path("/kopier-behold-im")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Kopier grunnlag behold opprinnelige inntektsmeldinger", tags = "iay-grunnlag")
    @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response kopierOgLagreGrunnlagBeholdIM(@NotNull @Valid KopierGrunnlagRequestAbac request) {
        kopierOgLagreGrunnlag(request, true);
        return Response.ok().build();
    }

    private void kopierOgLagreGrunnlag(KopierGrunnlagRequestAbac request, boolean beholdInntektsmeldinger) {
        var koblingReferanse = new KoblingReferanse(request.getNyReferanse());
        var koblingLås = Optional.ofNullable(koblingTjeneste.taSkrivesLås(koblingReferanse));

        setupLogMdcFraKoblingReferanse(koblingReferanse);

        var kobling = oppdaterKobling(request);

        iayTjeneste.kopierGrunnlagFraEksisterendeBehandling(kobling.getYtelseType(), kobling.getAktørId(), new Saksnummer(request.getSaksnummer()),
            new KoblingReferanse(request.getGammelReferanse()), new KoblingReferanse(request.getNyReferanse()), request.getDataset(), beholdInntektsmeldinger);

        koblingLås.ifPresent(lås -> koblingTjeneste.oppdaterLåsVersjon(lås));
    }

    private UUID utledSisteKjenteGrunnlagReferanseFraSpesifikasjon(InntektArbeidYtelseGrunnlagRequestAbacDto spesifikasjon) {
        final var sisteKjenteGrunnlagReferanse = spesifikasjon.getSisteKjenteGrunnlagReferanse();
        final var forespurtGrunnlagReferanse = spesifikasjon.getGrunnlagReferanse();

        return forespurtGrunnlagReferanse == null || forespurtGrunnlagReferanse.equals(sisteKjenteGrunnlagReferanse) ? sisteKjenteGrunnlagReferanse : null;
    }

    private Kobling oppdaterKobling(@NotNull @Valid KopierGrunnlagRequest dto) {
        KoblingReferanse referanse = new KoblingReferanse(dto.getNyReferanse());
        Optional<Kobling> koblingOpt = koblingTjeneste.hentFor(referanse);
        Kobling kobling;
        if (koblingOpt.isEmpty()) {
            // Lagre kobling
            AktørId aktørId = new AktørId(dto.getAktør().getIdent());
            kobling = new Kobling(dto.getYtelseType(), new Saksnummer(dto.getSaksnummer()), referanse, aktørId);
        } else {
            kobling = koblingOpt.get();
            if (YtelseType.UDEFINERT.equals(kobling.getYtelseType())) {
                var ytelseType = dto.getYtelseType();
                if (ytelseType != null) {
                    kobling.setYtelseType(ytelseType);
                }
            }
        }
        // Oppdater kobling med perioder
        mapPeriodeTilIntervall(dto.getOpplysningsperiode()).ifPresent(kobling::setOpplysningsperiode);
        mapPeriodeTilIntervall(dto.getOpplysningsperiodeSkattegrunnlag()).ifPresent(kobling::setOpplysningsperiodeSkattegrunnlag);
        mapPeriodeTilIntervall(dto.getOpptjeningsperiode()).ifPresent(kobling::setOpptjeningsperiode);

        // Diff & log endringer
        koblingTjeneste.lagre(kobling);
        return kobling;
    }

    private Optional<IntervallEntitet> mapPeriodeTilIntervall(Periode periode) {
        return Optional.ofNullable(periode == null ? null : IntervallEntitet.fraOgMedTilOgMed(periode.getFom(), periode.getTom()));
    }

    private KoblingReferanse getKoblingReferanse(AktørId aktørId, UUID koblingRef, UUID grunnlagRef) {
        KoblingReferanse koblingReferanse;
        if (koblingRef != null) {
            koblingReferanse = new KoblingReferanse(koblingRef);
        } else {
            var grunnlagReferanse = new GrunnlagReferanse(grunnlagRef);
            koblingReferanse = iayTjeneste.hentKoblingReferanse(grunnlagReferanse);
        }

        Optional<Kobling> koblingOpt = koblingTjeneste.hentFor(koblingReferanse);
        if (koblingOpt.isEmpty()) {
            // Har ikke fått denne opprettet enda
            return koblingReferanse;
        }
        var kobling = koblingOpt.get();

        if (!Objects.equals(kobling.getAktørId(), aktørId)) {
            // post-condition sjekk at angitt person stemmer med koblingens aktør
            throw new IllegalArgumentException("Kobling ikke knyttet til angitt aktør: " + aktørId);
        }
        return koblingReferanse;
    }

    private InntektArbeidYtelseGrunnlag getGrunnlag(@SuppressWarnings("unused") InntektArbeidYtelseGrunnlagRequest spesifikasjon,  // NOSONAR
                                                    GrunnlagReferanse grunnlagReferanse,
                                                    KoblingReferanse koblingReferanse) {
        if (grunnlagReferanse != null) {
            var grunnlag = iayTjeneste.hentAggregat(grunnlagReferanse);
            if (koblingReferanse != null) {
                var grunnlagsKoblingReferanse = iayTjeneste.hentKoblingReferanse(grunnlagReferanse);
                if (!Objects.equals(koblingReferanse, grunnlagsKoblingReferanse)) {
                    // returner kun angitt koblingReferanse i feilmelding, ikke den som er på grunnlag (sikkerhet).
                    throw new IllegalStateException("Angitt koblingreferanse matcher ikke grunnlag: " + koblingReferanse);
                }
            }
            return grunnlag;
        } else if (koblingReferanse != null) {
            final var grunnlagOpt = iayTjeneste.hentGrunnlagFor(koblingReferanse);

            return grunnlagOpt.orElse(null);
        }

        throw new UnsupportedOperationException("Må ha grunnlagReferanse eller koblingReferanse");
    }

    private void setupLogMdcFraKoblingReferanse(KoblingReferanse koblingReferanse) {
        var kobling = koblingTjeneste.hentFor(koblingReferanse);
        kobling.filter(k -> k.getSaksnummer() != null)
            .ifPresent(k -> LoggUtil.setupLogMdc(k.getYtelseType(), kobling.get().getSaksnummer().getVerdi(),
                koblingReferanse.getReferanse())); // legger til saksnummer i MDC
    }

    /**
     * Json bean med Abac.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    public static class InntektArbeidYtelseGrunnlagRequestAbacDto extends InntektArbeidYtelseGrunnlagRequest implements AbacDto {

        @JsonCreator
        public InntektArbeidYtelseGrunnlagRequestAbacDto(@JsonProperty(value = "personIdent", required = true) @Valid @NotNull PersonIdent person) {
            super(person);
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            return lagAbacAttributter(getPerson());
        }

    }

    /**
     * Json bean med Abac.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    public static class KopierGrunnlagRequestAbac extends KopierGrunnlagRequest implements AbacDto {

        @JsonCreator
        public KopierGrunnlagRequestAbac(@JsonProperty(value = "saksnummer", required = true) @Valid @NotNull String saksnummer,
                                         @JsonProperty(value = "nyReferanse", required = true) @Valid @NotNull UUID nyReferanse,
                                         @JsonProperty(value = "gammelReferanse", required = true) @Valid @NotNull UUID gammelReferanse,
                                         @JsonProperty(value = "ytelseType", required = true) @Valid @NotNull no.nav.abakus.iaygrunnlag.kodeverk.YtelseType ytelseType,
                                         @JsonProperty(value = "aktør", required = true) @NotNull @Valid PersonIdent aktør,
                                         @JsonProperty(value = "dataset", required = false) @Valid Set<Dataset> dataset) {

            super(saksnummer, nyReferanse, gammelReferanse, ytelseType, aktør, dataset == null ? EnumSet.allOf(Dataset.class) : dataset);
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            return lagAbacAttributter(getAktør());
        }
    }

    /**
     * Json bean med Abac.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class OverstyrtInntektArbeidYtelseAbacDto extends OverstyrtInntektArbeidYtelseDto implements AbacDto {

        @JsonCreator
        public OverstyrtInntektArbeidYtelseAbacDto(@JsonProperty(value = "personIdent", required = true) PersonIdent person,
                                                   @JsonProperty(value = "grunnlagReferanse") @Valid @NotNull UUID grunnlagReferanse,
                                                   @JsonProperty(value = "koblingReferanse") @Valid @NotNull UUID koblingReferanse,
                                                   @JsonProperty(value = "ytelseType") YtelseType ytelseType,
                                                   @JsonProperty(value = "arbeidsforholdInformasjon") ArbeidsforholdInformasjon arbeidsforholdInformasjon,
                                                   @JsonProperty(value = "overstyrt") InntektArbeidYtelseAggregatOverstyrtDto overstyrt) {
            super(person, grunnlagReferanse, koblingReferanse, ytelseType, arbeidsforholdInformasjon, overstyrt);
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            return lagAbacAttributter(getPerson());
        }
    }
}
