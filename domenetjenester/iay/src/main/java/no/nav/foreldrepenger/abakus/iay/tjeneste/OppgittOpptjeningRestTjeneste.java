package no.nav.foreldrepenger.abakus.iay.tjeneste;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import no.nav.abakus.iaygrunnlag.UuidDto;
import no.nav.abakus.iaygrunnlag.request.OppgittOpptjeningMottattRequest;
import no.nav.foreldrepenger.abakus.domene.iay.GrunnlagReferanse;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjeningBuilder;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.iay.OppgittOpptjeningTjeneste;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.MapOppgittOpptjening;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.kobling.KoblingTjeneste;
import no.nav.foreldrepenger.abakus.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;

@OpenAPIDefinition(tags = @Tag(name = "oppgitt opptjening"))
@Path("/iay/oppgitt/v1")
@ApplicationScoped
@Transactional
public class OppgittOpptjeningRestTjeneste {

    private KoblingTjeneste koblingTjeneste;
    private OppgittOpptjeningTjeneste oppgittOpptjeningTjeneste;
    private InntektArbeidYtelseTjeneste iayTjeneste;
    private KodeverkRepository kodeverkRepository;

    public OppgittOpptjeningRestTjeneste() {
    }

    @Inject
    public OppgittOpptjeningRestTjeneste(KoblingTjeneste koblingTjeneste,
                                         OppgittOpptjeningTjeneste oppgittOpptjeningTjeneste,
                                         InntektArbeidYtelseTjeneste iayTjeneste,
                                         KodeverkRepository kodeverkRepository) {
        this.koblingTjeneste = koblingTjeneste;
        this.oppgittOpptjeningTjeneste = oppgittOpptjeningTjeneste;
        this.iayTjeneste = iayTjeneste;
        this.kodeverkRepository = kodeverkRepository;
    }

    @POST
    @Path("/motta")
    @Operation(description = "Lagrer ned mottatt oppgitt opptjening", tags = "oppgitt opptjening", responses = {
        @ApiResponse(description = "Oppdatert grunnlagreferanse",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = UuidDto.class)))
    })
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentIayGrunnlag(@NotNull @TilpassetAbacAttributt(supplierClass = AbacDataSupplier.class) @Valid OppgittOpptjeningMottattRequest mottattRequest) {

        var koblingReferanse = new KoblingReferanse(mottattRequest.getKoblingReferanse());
        var koblingLås = koblingTjeneste.taSkrivesLås(koblingReferanse);
        var aktørId = new AktørId(mottattRequest.getAktør().getIdent());
        var kobling = koblingTjeneste.finnEllerOpprett(koblingReferanse, aktørId, new Saksnummer(mottattRequest.getSaksnummer()));

        OppgittOpptjeningBuilder builder = new MapOppgittOpptjening(iayTjeneste, kodeverkRepository).mapFraDto(mottattRequest.getOppgittOpptjening());
        GrunnlagReferanse grunnlagReferanse = oppgittOpptjeningTjeneste.lagre(koblingReferanse, builder);

        koblingTjeneste.lagre(kobling);
        koblingTjeneste.oppdaterLåsVersjon(koblingLås);

        if (grunnlagReferanse != null) {
            return Response.ok(new UuidDto(grunnlagReferanse.getReferanse())).build();
        }
        return Response.noContent().build();
    }

    public static class AbacDataSupplier implements Function<Object, AbacDataAttributter> {

        public AbacDataSupplier() {
        }

        @Override
        public AbacDataAttributter apply(Object o) {
            var req = (OppgittOpptjeningMottattRequest) o;
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.AKTØR_ID, req.getAktør().getIdent());
        }
    }
}
