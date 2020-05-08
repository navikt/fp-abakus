package no.nav.foreldrepenger.abakus.iay.tjeneste;

import static no.nav.foreldrepenger.abakus.felles.sikkerhet.AbakusBeskyttetRessursAttributt.GRUNNLAG;
import static no.nav.foreldrepenger.abakus.felles.sikkerhet.AbakusBeskyttetRessursAttributt.SØKNAD;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.CREATE;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.UPDATE;

import java.time.Duration;
import java.time.Instant;
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
import no.nav.foreldrepenger.abakus.felles.FellesRestTjeneste;
import no.nav.foreldrepenger.abakus.felles.metrikker.MetrikkerTjeneste;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.iay.OppgittOpptjeningTjeneste;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.MapOppgittOpptjening;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.kobling.KoblingTjeneste;
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
public class OppgittOpptjeningRestTjeneste extends FellesRestTjeneste {

    private KoblingTjeneste koblingTjeneste;
    private OppgittOpptjeningTjeneste oppgittOpptjeningTjeneste;
    private InntektArbeidYtelseTjeneste iayTjeneste;

    public OppgittOpptjeningRestTjeneste() {} // RESTEASY ctor

    @Inject
    public OppgittOpptjeningRestTjeneste(KoblingTjeneste koblingTjeneste,
                                         OppgittOpptjeningTjeneste oppgittOpptjeningTjeneste,
                                         InntektArbeidYtelseTjeneste iayTjeneste, MetrikkerTjeneste metrikkerTjeneste) {
        super(metrikkerTjeneste);
        this.koblingTjeneste = koblingTjeneste;
        this.oppgittOpptjeningTjeneste = oppgittOpptjeningTjeneste;
        this.iayTjeneste = iayTjeneste;
    }

    @POST
    @Path("/motta")
    @Operation(description = "Lagrer ned mottatt oppgitt opptjening", tags = "oppgitt opptjening", responses = {
        @ApiResponse(description = "Oppdatert grunnlagreferanse",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = UuidDto.class)))
    })
    @BeskyttetRessurs(action = CREATE, resource = SØKNAD)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response lagreOppgittOpptjening(@NotNull @TilpassetAbacAttributt(supplierClass = AbacDataSupplier.class) @Valid OppgittOpptjeningMottattRequest mottattRequest) {
        var startTx = Instant.now();
        Response response;

        var koblingReferanse = new KoblingReferanse(mottattRequest.getKoblingReferanse());
        var koblingLås = koblingTjeneste.taSkrivesLås(koblingReferanse);
        var aktørId = new AktørId(mottattRequest.getAktør().getIdent());
        var kobling = koblingTjeneste.finnEllerOpprett(koblingReferanse, aktørId, new Saksnummer(mottattRequest.getSaksnummer()));

        OppgittOpptjeningBuilder builder = new MapOppgittOpptjening(iayTjeneste).mapFraDto(mottattRequest.getOppgittOpptjening());
        GrunnlagReferanse grunnlagReferanse = oppgittOpptjeningTjeneste.lagre(koblingReferanse, builder);

        koblingTjeneste.lagre(kobling);
        koblingTjeneste.oppdaterLåsVersjon(koblingLås);

        if (grunnlagReferanse != null) {
            response = Response.ok(new UuidDto(grunnlagReferanse.getReferanse())).build();
        } else {
            response = Response.noContent().build();
        }

        logMetrikk("/iay/oppgitt/v1/motta", Duration.between(startTx, Instant.now()));
        return response;
    }

    @POST
    @Path("/overstyr")
    @Operation(description = "Lagrer ned mottatt oppgitt opptjening", tags = "oppgitt opptjening", responses = {
            @ApiResponse(description = "Oppdatert grunnlagreferanse",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UuidDto.class)))
    })
    @BeskyttetRessurs(action = UPDATE, resource = GRUNNLAG)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response lagreOverstrytOppgittOpptjening(@NotNull @TilpassetAbacAttributt(supplierClass = AbacDataSupplier.class) @Valid OppgittOpptjeningMottattRequest mottattRequest) {
        var startTx = Instant.now();
        Response response;

        var koblingReferanse = new KoblingReferanse(mottattRequest.getKoblingReferanse());
        var koblingLås = koblingTjeneste.taSkrivesLås(koblingReferanse);
        var aktørId = new AktørId(mottattRequest.getAktør().getIdent());
        var kobling = koblingTjeneste.finnEllerOpprett(koblingReferanse, aktørId, new Saksnummer(mottattRequest.getSaksnummer()));

        OppgittOpptjeningBuilder builder = new MapOppgittOpptjening(iayTjeneste).mapFraDto(mottattRequest.getOppgittOpptjening());
        GrunnlagReferanse grunnlagReferanse = oppgittOpptjeningTjeneste.lagreOverstyring(koblingReferanse, builder);

        koblingTjeneste.lagre(kobling);
        koblingTjeneste.oppdaterLåsVersjon(koblingLås);

        if (grunnlagReferanse != null) {
            response = Response.ok(new UuidDto(grunnlagReferanse.getReferanse())).build();
        } else {
            response = Response.noContent().build();
        }

        logMetrikk("/iay/oppgitt/v1/overstyr", Duration.between(startTx, Instant.now()));
        return response;
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
