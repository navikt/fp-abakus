package no.nav.foreldrepenger.abakus.iay.tjeneste;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.exceptions.UnsupportedOperationException;

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
import no.nav.abakus.iaygrunnlag.Aktør;
import no.nav.abakus.iaygrunnlag.AktørIdPersonident;
import no.nav.abakus.iaygrunnlag.FnrPersonident;
import no.nav.abakus.iaygrunnlag.Periode;
import no.nav.abakus.iaygrunnlag.PersonIdent;
import no.nav.abakus.iaygrunnlag.request.Dataset;
import no.nav.abakus.iaygrunnlag.request.InntektArbeidYtelseGrunnlagRequest;
import no.nav.abakus.iaygrunnlag.request.KopierGrunnlagRequest;
import no.nav.abakus.iaygrunnlag.v1.InntektArbeidYtelseGrunnlagDto;
import no.nav.abakus.iaygrunnlag.v1.InntektArbeidYtelseGrunnlagSakSnapshotDto;
import no.nav.foreldrepenger.abakus.domene.iay.GrunnlagReferanse;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlagBuilder;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.IAYFraDtoMapper;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.IAYTilDtoMapper;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.kobling.KoblingTjeneste;
import no.nav.foreldrepenger.abakus.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;

@OpenAPIDefinition(tags = {@Tag(name = "iay-grunnlag")})
@Path("/iay/grunnlag/v1")
@ApplicationScoped
@Transaction
public class GrunnlagRestTjeneste {

    private InntektArbeidYtelseTjeneste iayTjeneste;
    private KoblingTjeneste koblingTjeneste;
    private KodeverkRepository kodeverkRepository;

    public GrunnlagRestTjeneste() {
        // for CDI
    }

    @Inject
    public GrunnlagRestTjeneste(InntektArbeidYtelseTjeneste iayTjeneste,
                                KoblingTjeneste koblingTjeneste,
                                KodeverkRepository kodeverkRepository) {
        this.iayTjeneste = iayTjeneste;
        this.koblingTjeneste = koblingTjeneste;
        this.kodeverkRepository = kodeverkRepository;
    }

    private static Periode mapPeriode(IntervallEntitet datoIntervall) {
        if (datoIntervall == null) {
            return new Periode(LocalDate.now(), LocalDate.now());
        }
        return new Periode(datoIntervall.getFomDato(), datoIntervall.getTomDato());
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Hent ett enkelt IAY Grunnlag for angitt spesifikasjon. Spesifikasjonen kan angit hvilke data som ønskes",
        tags = "iay-grunnlag",
        responses = {
            @ApiResponse(description = "Grunnlaget",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = InntektArbeidYtelseGrunnlagDto.class))),
            @ApiResponse(responseCode = "204", description = "Det finnes ikke et grunnlag for forespørselen"),
            @ApiResponse(responseCode = "304", description = "Grunnlaget har ikke endret seg i henhold til det fagsystemet allerede kjenner")
        })
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentIayGrunnlag(@NotNull @Valid InntektArbeidYtelseGrunnlagRequestAbacDto spesifikasjon) {

        var aktørId = new AktørId(spesifikasjon.getPerson().getIdent());

        final var forespurtGrunnlagReferanse = spesifikasjon.getGrunnlagReferanse();
        var grunnlagReferanse = forespurtGrunnlagReferanse != null ? new GrunnlagReferanse(forespurtGrunnlagReferanse) : null;
        var koblingReferanse = getKoblingReferanse(aktørId, spesifikasjon);

        final var sisteKjenteGrunnlagReferanse = utledSisteKjenteGrunnlagReferanse(spesifikasjon);
        final var sistKjenteErAktivt = sisteKjenteGrunnlagReferanse != null && iayTjeneste.erGrunnlagAktivt(sisteKjenteGrunnlagReferanse);

        if (sisteKjenteGrunnlagReferanse != null && sistKjenteErAktivt) {
            return Response.notModified().build();
        }

        var grunnlag = getGrunnlag(spesifikasjon, grunnlagReferanse, koblingReferanse);
        if (grunnlag != null) {
            var dtoMapper = new IAYTilDtoMapper(aktørId, grunnlagReferanse, koblingReferanse);

            return Response.ok(dtoMapper.mapTilDto(grunnlag, spesifikasjon)).build();
        } else {
            return Response.noContent().build();
        }
    }

    private UUID utledSisteKjenteGrunnlagReferanse(InntektArbeidYtelseGrunnlagRequestAbacDto spesifikasjon) {
        final var sisteKjenteGrunnlagReferanse = spesifikasjon.getSisteKjenteGrunnlagReferanse();
        final var forespurtGrunnlagReferanse = spesifikasjon.getGrunnlagReferanse();

        if (forespurtGrunnlagReferanse != null && forespurtGrunnlagReferanse.equals(sisteKjenteGrunnlagReferanse)) {
            if (forespurtGrunnlagReferanse.equals(sisteKjenteGrunnlagReferanse)) {
                return sisteKjenteGrunnlagReferanse;
            }
        } else {
            return sisteKjenteGrunnlagReferanse;
        }
        return null;
    }

    @PUT
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Lagrer siste versjon",
        tags = "iay-grunnlag",
        responses = {
            @ApiResponse(responseCode = "200", description = "Mottatt grunnlaget")
        })
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response oppdaterGrunnlag(@NotNull @Valid InntektArbeidYtelseGrunnlagAbacDto dto) {

        var aktørId = new AktørId(dto.getPerson().getIdent());

        var koblingReferanse = getKoblingReferanse(aktørId, dto);

        var dtoMapper = new IAYFraDtoMapper(iayTjeneste, kodeverkRepository, aktørId, koblingReferanse);
        final var builder = InntektArbeidYtelseGrunnlagBuilder.oppdatere(iayTjeneste.hentGrunnlagFor(koblingReferanse));
        iayTjeneste.lagre(koblingReferanse, InntektArbeidYtelseGrunnlagBuilder.oppdatere(dtoMapper.mapOverstyringerTilGrunnlag(dto, builder)));

        return Response.ok().build();
    }

    @POST
    @Path("/snapshot")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Hent IAY Grunnlag for angitt søke spesifikasjon",
        tags = "iay-grunnlag",
        responses = {
            @ApiResponse(description = "Grunnlaget for saken",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = InntektArbeidYtelseGrunnlagSakSnapshotDto.class)))
        })
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentSnapshotIayGrunnlag(@NotNull @Valid InntektArbeidYtelseGrunnlagRequestAbacDto spesifikasjon) {

        var aktørId = new AktørId(spesifikasjon.getPerson().getIdent());

        var saksnummer = Objects.requireNonNull(spesifikasjon.getSaksnummer(), "saksnummer");
        var ytelseType = Objects.requireNonNull(spesifikasjon.getYtelseType(), "ytelseType");

        var snapshot = new InntektArbeidYtelseGrunnlagSakSnapshotDto(saksnummer, ytelseType, spesifikasjon.getPerson());

        var grunnlagEtterspurt = iayTjeneste.hentGrunnlagEtterspurtFor(aktørId,
            new Saksnummer(saksnummer),
            YtelseType.fraKode(ytelseType.getKode()),
            spesifikasjon.getGrunnlagVersjon());

        grunnlagEtterspurt.forEach(g -> {
            var kobling = koblingTjeneste.hent(g.getKoblingId());

            var dtoMapper = new IAYTilDtoMapper(aktørId, g.getGrunnlagReferanse(), kobling.getKoblingReferanse());
            var dto = dtoMapper.mapTilDto(g, spesifikasjon);

            snapshot.leggTil(dto, g.isAktiv(), mapPeriode(kobling.getOpplysningsperiode()), mapPeriode(kobling.getOpptjeningsperiode()));
        });

        return Response.ok(snapshot).build();
    }

    @POST
    @Path("/kopier")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Kopier grunnlag",
        tags = "iay-grunnlag")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response kopierGrunnlag(@NotNull @Valid KopierGrunnlagRequestAbac request) {
        var ref = new KoblingReferanse(request.getNyReferanse());
        var koblingLås = koblingTjeneste.taSkrivesLås(ref); // alltid ta lås før skrive operasjoner

        var kobling = oppdaterKobling(request);

        iayTjeneste.kopierGrunnlagFraEksisterendeBehandling(kobling.getYtelseType(), kobling.getAktørId(),
            new Saksnummer(request.getSaksnummer()),
            new KoblingReferanse(request.getGammelReferanse()),
            new KoblingReferanse(request.getNyReferanse()),
            request.getDataset());

        koblingTjeneste.oppdaterLåsVersjon(koblingLås);
        return Response.ok().build();
    }

    private Kobling oppdaterKobling(@NotNull @Valid KopierGrunnlagRequest dto) {
        KoblingReferanse referanse = new KoblingReferanse(dto.getNyReferanse());
        Optional<Kobling> koblingOpt = koblingTjeneste.hentFor(referanse);
        Kobling kobling;
        if (koblingOpt.isEmpty()) {
            // Lagre kobling
            AktørId aktørId = new AktørId(dto.getAktør().getIdent());
            kobling = new Kobling(new Saksnummer(dto.getSaksnummer()), referanse, aktørId);
        } else {
            kobling = koblingOpt.get();
        }

        if (YtelseType.UDEFINERT.equals(kobling.getYtelseType())) {
            var ytelseType = YtelseType.fraKode(dto.getYtelseType().getKode());
            if (ytelseType != null) {
                kobling.setYtelseType(ytelseType);
            }
        }
        // Oppdater kobling
        Aktør annenPartAktør = dto.getAnnenPartAktør();
        if (annenPartAktør != null) {
            kobling.setAnnenPartAktørId(new AktørId(annenPartAktør.getIdent()));
        }
        Periode opplysningsperiode = dto.getOpplysningsperiode();
        if (opplysningsperiode != null) {
            kobling.setOpplysningsperiode(IntervallEntitet.fraOgMedTilOgMed(opplysningsperiode.getFom(), opplysningsperiode.getTom()));
        }
        Periode opptjeningsperiode = dto.getOpptjeningsperiode();
        if (opptjeningsperiode != null) {
            kobling.setOpptjeningsperiode(IntervallEntitet.fraOgMedTilOgMed(opptjeningsperiode.getFom(), opptjeningsperiode.getTom()));
        }
        // Diff & log endringer
        koblingTjeneste.lagre(kobling);
        return kobling;
    }

    private KoblingReferanse getKoblingReferanse(AktørId aktørId, InntektArbeidYtelseGrunnlagRequest spesifikasjon) {
        KoblingReferanse koblingReferanse;
        if (spesifikasjon.getKoblingReferanse() != null) {
            koblingReferanse = new KoblingReferanse(spesifikasjon.getKoblingReferanse());
        } else {
            var grunnlagReferanse = new GrunnlagReferanse(spesifikasjon.getGrunnlagReferanse());
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

    private KoblingReferanse getKoblingReferanse(AktørId aktørId, InntektArbeidYtelseGrunnlagDto spesifikasjon) {
        KoblingReferanse koblingReferanse;
        if (spesifikasjon.getKoblingReferanse() != null) {
            koblingReferanse = new KoblingReferanse(spesifikasjon.getKoblingReferanse());
        } else {
            var grunnlagReferanse = new GrunnlagReferanse(spesifikasjon.getGrunnlagReferanse());
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

    private InntektArbeidYtelseGrunnlag getGrunnlag(@SuppressWarnings("unused") InntektArbeidYtelseGrunnlagRequest spesifikasjon,
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
            final var abacDataAttributter = AbacDataAttributter.opprett();
            if (FnrPersonident.IDENT_TYPE.equals(getPerson().getIdentType())) {
                return abacDataAttributter.leggTil(StandardAbacAttributtType.FNR, getPerson().getIdent());
            } else if (AktørIdPersonident.IDENT_TYPE.equals(getPerson().getIdentType())) {
                return abacDataAttributter.leggTil(StandardAbacAttributtType.AKTØR_ID, getPerson().getIdent());
            }
            throw new java.lang.IllegalStateException("Ukjent identtype");
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
                                         @JsonProperty(value = "dataset", required = false) @NotNull @Valid Set<Dataset> dataset) {

            super(saksnummer, nyReferanse, gammelReferanse, ytelseType, aktør, dataset == null ? EnumSet.allOf(Dataset.class) : dataset);
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            final var abacDataAttributter = AbacDataAttributter.opprett();
            if (FnrPersonident.IDENT_TYPE.equals(getAktør().getIdentType())) {
                return abacDataAttributter.leggTil(StandardAbacAttributtType.FNR, getAktør().getIdent());
            } else if (AktørIdPersonident.IDENT_TYPE.equals(getAktør().getIdentType())) {
                return abacDataAttributter.leggTil(StandardAbacAttributtType.AKTØR_ID, getAktør().getIdent());
            }
            throw new java.lang.IllegalStateException("Ukjent identtype");
        }
    }

    /**
     * Json bean med Abac.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class InntektArbeidYtelseGrunnlagAbacDto extends InntektArbeidYtelseGrunnlagDto implements AbacDto {

        @JsonCreator
        public InntektArbeidYtelseGrunnlagAbacDto(@JsonProperty(value = "person", required = true) @Valid @NotNull PersonIdent person,
                                                  @JsonProperty(value = "grunnlagTidspunkt", required = true) @Valid @NotNull OffsetDateTime grunnlagTidspunkt,
                                                  @JsonProperty(value = "grunnlagReferanse", required = true) @Valid @NotNull UUID grunnlagReferanse,
                                                  @JsonProperty(value = "koblingReferanse", required = true) @Valid @NotNull UUID koblingReferanse) {
            super(person, grunnlagTidspunkt, grunnlagReferanse, koblingReferanse);
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            final var abacDataAttributter = AbacDataAttributter.opprett();
            if (FnrPersonident.IDENT_TYPE.equals(getPerson().getIdentType())) {
                return abacDataAttributter.leggTil(StandardAbacAttributtType.FNR, getPerson().getIdent());
            } else if (AktørIdPersonident.IDENT_TYPE.equals(getPerson().getIdentType())) {
                return abacDataAttributter.leggTil(StandardAbacAttributtType.AKTØR_ID, getPerson().getIdent());
            }
            throw new java.lang.IllegalStateException("Ukjent identtype");
        }
    }
}
