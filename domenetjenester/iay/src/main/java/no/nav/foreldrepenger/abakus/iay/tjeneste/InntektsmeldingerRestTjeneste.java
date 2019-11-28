package no.nav.foreldrepenger.abakus.iay.tjeneste;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.CREATE;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.iay.InntektsmeldingerTjeneste;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.MapInntektsmeldinger;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.kobling.KoblingTjeneste;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.AktørIdPersonident;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.FnrPersonident;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.PersonIdent;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.UuidDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.inntektsmelding.v1.InntektsmeldingerDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.request.InntektsmeldingerMottattRequest;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.request.InntektsmeldingerRequest;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;

@OpenAPIDefinition(tags = @Tag(name = "inntektsmelding"))
@Path("/iay/inntektsmeldinger/v1")
@ApplicationScoped
@Transaction
public class InntektsmeldingerRestTjeneste {

    private InntektsmeldingerTjeneste imTjeneste;
    private KoblingTjeneste koblingTjeneste;
    private InntektArbeidYtelseTjeneste iayTjeneste;

    public InntektsmeldingerRestTjeneste() {
        // for CDI
    }

    @Inject
    public InntektsmeldingerRestTjeneste(InntektsmeldingerTjeneste imTjeneste,
                                         KoblingTjeneste koblingTjeneste, InntektArbeidYtelseTjeneste iayTjeneste) {
        this.imTjeneste = imTjeneste;
        this.koblingTjeneste = koblingTjeneste;
        this.iayTjeneste = iayTjeneste;
    }

    @POST
    @Path("/hentAlle")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Hent inntektsmeldinger for angitt søke spesifikasjon", tags = "inntektsmelding")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentInntektsmeldingerForSak(@NotNull @Valid InntektsmeldingerRequestAbacDto spesifikasjon) {
        var aktørId = new AktørId(spesifikasjon.getPerson().getIdent());
        var saksnummer = new Saksnummer(spesifikasjon.getSaksnummer());
        var ytelseType = new YtelseType(spesifikasjon.getYtelseType().getKode());
        var inntektsmeldinger = iayTjeneste.hentAlleInntektsmeldingerFor(aktørId, saksnummer, new YtelseType(ytelseType.getKode()));
        var kobling = koblingTjeneste.hentSisteFor(aktørId, saksnummer, ytelseType);
        if (kobling.isEmpty()) {
            return Response.ok(new InntektsmeldingerDto().medInntektsmeldinger(Collections.emptyList())).build();
        }
        InntektArbeidYtelseGrunnlag nyesteGrunnlag = iayTjeneste.hentAggregat(kobling.get().getKoblingReferanse());
        InntektsmeldingerDto inntektsmeldingerDto = MapInntektsmeldinger.mapUnikeInntektsmeldingerFraGrunnlag(inntektsmeldinger, nyesteGrunnlag);
        return Response.ok(inntektsmeldingerDto).build();
    }

    @POST
    @Path("/motta")
    @Operation(description = "Motta inntektsmelding(er)", tags = "inntektsmelding",
        responses = {
            @ApiResponse(description = "Oppdatert grunnlagreferanse",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = UuidDto.class)))
        })
    @BeskyttetRessurs(action = CREATE, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public UuidDto mottaInntektsmeldinger(@NotNull @TilpassetAbacAttributt(supplierClass = AbacDataSupplier.class) @Valid InntektsmeldingerMottattRequest mottattRequest) {

        var aktørId = new AktørId(mottattRequest.getAktør().getIdent());

        var koblingReferanse = new KoblingReferanse(mottattRequest.getKoblingReferanse());
        var koblingLås = koblingTjeneste.taSkrivesLås(koblingReferanse);
        var kobling = koblingTjeneste.finnEllerOpprett(koblingReferanse, aktørId, new Saksnummer(mottattRequest.getSaksnummer()));

        var informasjonBuilder = ArbeidsforholdInformasjonBuilder.oppdatere(imTjeneste.hentArbeidsforholdInformasjonForKobling(koblingReferanse));

        var inntektsmeldingerAggregat = new MapInntektsmeldinger.MapFraDto().map(informasjonBuilder, mottattRequest.getInntektsmeldinger());

        var grunnlagReferanse = imTjeneste.lagre(koblingReferanse, informasjonBuilder, inntektsmeldingerAggregat.getAlleInntektsmeldinger());

        koblingTjeneste.lagre(kobling);
        koblingTjeneste.oppdaterLåsVersjon(koblingLås);

        if (grunnlagReferanse != null) {
            return new UuidDto(grunnlagReferanse.getReferanse());
        }
        return null;
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
            } else if(AktørIdPersonident.IDENT_TYPE.equals(getPerson().getIdentType())) {
                return abacDataAttributter.leggTil(StandardAbacAttributtType.AKTØR_ID, getPerson().getIdent());
            }
            throw new java.lang.IllegalArgumentException("Ukjent identtype: " + getPerson().getIdentType());
        }

    }

}
