package no.nav.foreldrepenger.abakus.iay.tjeneste;

import static no.nav.foreldrepenger.abakus.felles.sikkerhet.AbakusBeskyttetRessursAttributt.INNTEKSTMELDING;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.CREATE;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import no.nav.abakus.iaygrunnlag.AktørIdPersonident;
import no.nav.abakus.iaygrunnlag.FnrPersonident;
import no.nav.abakus.iaygrunnlag.PersonIdent;
import no.nav.abakus.iaygrunnlag.UuidDto;
import no.nav.abakus.iaygrunnlag.inntektsmelding.v1.InntektsmeldingerDto;
import no.nav.abakus.iaygrunnlag.inntektsmelding.v1.RefusjonskravDatoerDto;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.abakus.iaygrunnlag.request.InntektsmeldingDiffRequest;
import no.nav.abakus.iaygrunnlag.request.InntektsmeldingerMottattRequest;
import no.nav.abakus.iaygrunnlag.request.InntektsmeldingerRequest;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.abakus.felles.FellesRestTjeneste;
import no.nav.foreldrepenger.abakus.felles.metrikker.MetrikkerTjeneste;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.iay.InntektsmeldingerTjeneste;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.MapInntektsmeldinger;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.kobling.KoblingTjeneste;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;

@OpenAPIDefinition(tags = @Tag(name = "inntektsmelding"))
@Path("/iay/inntektsmeldinger/v1")
@ApplicationScoped
@Transactional
public class InntektsmeldingerRestTjeneste extends FellesRestTjeneste {

    private InntektsmeldingerTjeneste imTjeneste;
    private KoblingTjeneste koblingTjeneste;
    private InntektArbeidYtelseTjeneste iayTjeneste;
    private static final Logger LOGGER = LoggerFactory.getLogger(InntektsmeldingerRestTjeneste.class);

    public InntektsmeldingerRestTjeneste() {
    } // RESTEASY ctor

    @Inject
    public InntektsmeldingerRestTjeneste(InntektsmeldingerTjeneste imTjeneste,
                                         KoblingTjeneste koblingTjeneste, InntektArbeidYtelseTjeneste iayTjeneste, MetrikkerTjeneste metrikkerTjeneste) {
        super(metrikkerTjeneste);
        this.imTjeneste = imTjeneste;
        this.koblingTjeneste = koblingTjeneste;
        this.iayTjeneste = iayTjeneste;
    }

    @POST
    @Path("/hentAlle")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Hent inntektsmeldinger for angitt søke spesifikasjon", tags = "inntektsmelding")
    @BeskyttetRessurs(action = READ, resource = INNTEKSTMELDING)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentInntektsmeldingerForSak(@NotNull @Valid InntektsmeldingerRequestAbacDto spesifikasjon) {
        var startTx = Instant.now();

        var aktørId = new AktørId(spesifikasjon.getPerson().getIdent());
        var saksnummer = new Saksnummer(spesifikasjon.getSaksnummer());
        var ytelseType = YtelseType.fraKode(spesifikasjon.getYtelseType().getKode());
        var inntektsmeldingerMap = iayTjeneste.hentArbeidsforholdinfoInntektsmeldingerMapFor(aktørId, saksnummer, YtelseType.fraKode(ytelseType.getKode()));
        InntektsmeldingerDto inntektsmeldingerDto = MapInntektsmeldinger.mapUnikeInntektsmeldingerFraGrunnlag(inntektsmeldingerMap);
        final Response build = Response.ok(inntektsmeldingerDto).build();

        logMetrikk("/iay/inntektsmeldinger/v1/hentAlle", Duration.between(startTx, Instant.now()));
        return build;
    }

    @POST
    @Path("/hentRefusjonskravDatoer")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Hent refusjonskrav fra inntektsmeldinger for angitt søke spesifikasjon", tags = "inntektsmelding")
    @BeskyttetRessurs(action = READ, resource = INNTEKSTMELDING)
    @SuppressWarnings({ "findsecbugs:JAXRS_ENDPOINT", "resource" })
    public Response hentRefusjonskravDatoForSak(@NotNull @Valid InntektsmeldingerRequestAbacDto spesifikasjon) {
        var startTx = Instant.now();
        Response response;

        var aktørId = new AktørId(spesifikasjon.getPerson().getIdent());
        var saksnummer = new Saksnummer(spesifikasjon.getSaksnummer());
        var ytelseType = YtelseType.fraKode(spesifikasjon.getYtelseType().getKode());
        var inntektsmeldinger = iayTjeneste.hentAlleInntektsmeldingerFor(aktørId, saksnummer, YtelseType.fraKode(ytelseType.getKode()));
        var kobling = koblingTjeneste.hentSisteFor(aktørId, saksnummer, ytelseType);
        if (kobling.isEmpty()) {
            response = Response.ok(new InntektsmeldingerDto().medInntektsmeldinger(Collections.emptyList())).build();
        } else {
            InntektArbeidYtelseGrunnlag nyesteGrunnlag = iayTjeneste.hentAggregat(kobling.get().getKoblingReferanse());
            RefusjonskravDatoerDto refusjonskravDatoerDto = MapInntektsmeldinger.mapRefusjonskravdatoer(inntektsmeldinger, nyesteGrunnlag);
            LOGGER.info("RefusjonskravDtoer for saksnummer ({}) er ({})", spesifikasjon.getSaksnummer(), refusjonskravDatoerDto);
            response = Response.ok(refusjonskravDatoerDto).build();
        }
        logMetrikk("/iay/inntektsmeldinger/v1/hentRefusjonskravDatoer", Duration.between(startTx, Instant.now()));
        return response;
    }

    @POST
    @Path("/motta")
    @Operation(description = "Motta og lagre inntektsmelding(er)", tags = "inntektsmelding", responses = {
            @ApiResponse(description = "Oppdatert grunnlagreferanse", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UuidDto.class)))
    })
    @BeskyttetRessurs(action = CREATE, resource = INNTEKSTMELDING)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public UuidDto lagreInntektsmeldinger(@NotNull @TilpassetAbacAttributt(supplierClass = AbacDataSupplier.class) @Valid InntektsmeldingerMottattRequest mottattRequest) {
        var startTx = Instant.now();
        UuidDto resultat = null;

        var aktørId = new AktørId(mottattRequest.getAktør().getIdent());

        var koblingReferanse = new KoblingReferanse(mottattRequest.getKoblingReferanse());
        var koblingLås = koblingTjeneste.taSkrivesLås(koblingReferanse);
        var kobling = koblingTjeneste.finnEllerOpprett(koblingReferanse, aktørId, new Saksnummer(mottattRequest.getSaksnummer()));

        var informasjonBuilder = ArbeidsforholdInformasjonBuilder.oppdatere(imTjeneste.hentArbeidsforholdInformasjonForKobling(koblingReferanse));

        var inntektsmeldingerAggregat = new MapInntektsmeldinger.MapFraDto().map(informasjonBuilder, mottattRequest.getInntektsmeldinger());

        List<Inntektsmelding> inntektsmeldinger = inntektsmeldingerAggregat.getInntektsmeldinger();
        valider(kobling.getYtelseType(), inntektsmeldinger);
        var grunnlagReferanse = imTjeneste.lagre(koblingReferanse, informasjonBuilder, inntektsmeldinger);

        koblingTjeneste.lagre(kobling);
        koblingTjeneste.oppdaterLåsVersjon(koblingLås);

        if (grunnlagReferanse != null) {
            resultat = new UuidDto(grunnlagReferanse.getReferanse());
        }

        logMetrikk("/iay/inntektsmeldinger/v1/motta", Duration.between(startTx, Instant.now()));
        return resultat;
    }

    private void valider(YtelseType ytelseType, List<Inntektsmelding> inntektsmeldinger) {
        switch (ytelseType) {
            case FORELDREPENGER:
            case SVANGERSKAPSPENGER:
            case UDEFINERT:
                // har ikke validering på Kapittel 14 ytelser her ennå pga feil i Gosys kopiering ved journalføring på annen sak.
                return;
            default:
                var feil = inntektsmeldinger.stream().filter(im -> im.getKanalreferanse() == null).findFirst();
                if (feil.isPresent()) {
                    throw new IllegalArgumentException("Inntektsmelding mangler kanalreferanse: " + feil);
                }
        }
    }

    @POST
    @Path("/hentDiff")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Hent inntektsmeldinger for angitt søke spesifikasjon", tags = "inntektsmelding")
    @BeskyttetRessurs(action = READ, resource = INNTEKSTMELDING)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentDifferanseMellomToReferanserPåSak(@NotNull @Valid InntektsmeldingDiffRequestAbacDto spesifikasjon) {
        var startTx = Instant.now();

        var aktørId = new AktørId(spesifikasjon.getPerson().getIdent());
        var saksnummer = new Saksnummer(spesifikasjon.getSaksnummer());
        var ytelseType = YtelseType.fraKode(spesifikasjon.getYtelseType().getKode());
        Map<Inntektsmelding, ArbeidsforholdInformasjon> førsteMap = iayTjeneste.hentAlleInntektsmeldingerForEksternRef(aktørId, saksnummer, new KoblingReferanse(spesifikasjon.getEksternRefEn()), YtelseType.fraKode(ytelseType.getKode()));
        Map<Inntektsmelding, ArbeidsforholdInformasjon> andreMap = iayTjeneste.hentAlleInntektsmeldingerForEksternRef(aktørId, saksnummer, new KoblingReferanse(spesifikasjon.getEksternRefTo()), YtelseType.fraKode(ytelseType.getKode()));

        var diffMap = iayTjeneste.utledInntektsmeldingDiff(førsteMap, andreMap);
        InntektsmeldingerDto imDiffListe = MapInntektsmeldinger.mapUnikeInntektsmeldingerFraGrunnlag(diffMap);
        final Response response = Response.ok(imDiffListe).build();

        logMetrikk("/iay/inntektsmeldinger/v1/hentDiff", Duration.between(startTx, Instant.now()));
        return response;
    }

    public static class AbacDataSupplier implements Function<Object, AbacDataAttributter> {

        public AbacDataSupplier() {
        }

        @Override
        public AbacDataAttributter apply(Object obj) {
            var req = (InntektsmeldingerMottattRequest) obj;
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.AKTØR_ID, req.getAktør().getIdent());
        }

    }

    /**
     * Json bean med Abac.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    public static class InntektsmeldingerRequestAbacDto extends InntektsmeldingerRequest implements AbacDto {

        @JsonCreator
        public InntektsmeldingerRequestAbacDto(@JsonProperty(value = "personIdent", required = true) @Valid @NotNull PersonIdent person) {
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
            throw new java.lang.IllegalArgumentException("Ukjent identtype: " + getPerson().getIdentType());
        }

    }

    /**
     * Json bean med Abac.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    public static class InntektsmeldingDiffRequestAbacDto extends InntektsmeldingDiffRequest implements AbacDto {

        @JsonCreator
        public InntektsmeldingDiffRequestAbacDto(@JsonProperty(value = "personIdent", required = true) @Valid @NotNull PersonIdent person) {
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
            throw new java.lang.IllegalArgumentException("Ukjent identtype: " + getPerson().getIdentType());
        }

    }

}
