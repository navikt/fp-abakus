package no.nav.foreldrepenger.abakus.iay.tjeneste;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.CREATE;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.foreldrepenger.abakus.iay.InntektsmeldingerTjeneste;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.MapInntektsmeldinger;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.kobling.KoblingTjeneste;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.UuidDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.request.InntektsmeldingerMottattRequest;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
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

    public InntektsmeldingerRestTjeneste() {
        // for CDI
    }

    @Inject
    public InntektsmeldingerRestTjeneste(InntektsmeldingerTjeneste imTjeneste,
                                         KoblingTjeneste koblingTjeneste) {
        this.imTjeneste = imTjeneste;
        this.koblingTjeneste = koblingTjeneste;
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
}
