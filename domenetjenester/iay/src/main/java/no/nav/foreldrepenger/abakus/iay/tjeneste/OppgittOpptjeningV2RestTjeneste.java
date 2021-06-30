package no.nav.foreldrepenger.abakus.iay.tjeneste;

import static no.nav.foreldrepenger.abakus.felles.sikkerhet.AbakusBeskyttetRessursAttributt.SØKNAD;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.CREATE;

import java.util.Optional;
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
import no.nav.foreldrepenger.abakus.felles.LoggUtil;
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
@Path("/iay/oppgitt/v2")
@ApplicationScoped
@Transactional
public class OppgittOpptjeningV2RestTjeneste {
    private KoblingTjeneste koblingTjeneste;
    private OppgittOpptjeningTjeneste oppgittOpptjeningTjeneste;

    public OppgittOpptjeningV2RestTjeneste() {
        // RESTEASY ctor
    }

    @Inject
    public OppgittOpptjeningV2RestTjeneste(KoblingTjeneste koblingTjeneste,
                                           OppgittOpptjeningTjeneste oppgittOpptjeningTjeneste) {
        this.koblingTjeneste = koblingTjeneste;
        this.oppgittOpptjeningTjeneste = oppgittOpptjeningTjeneste;
    }

    @POST
    @Path("/motta")
    @Operation(description = "Lagrer ned mottatt oppgitt opptjening (versjon 2: støtter oppgitt opptjening pr journalpost)", tags = "oppgitt opptjening", responses = {
        @ApiResponse(description = "Oppdatert grunnlagreferanse", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UuidDto.class)))
    })
    @BeskyttetRessurs(action = CREATE, resource = SØKNAD)
    @SuppressWarnings({"findsecbugs:JAXRS_ENDPOINT"})
    public Response lagreOppgittOpptjeningV2(@NotNull @TilpassetAbacAttributt(supplierClass = AbacDataSupplier.class) @Valid OppgittOpptjeningMottattRequest mottattRequest) {
        LoggUtil.setupLogMdc(mottattRequest.getYtelseType(), mottattRequest.getSaksnummer(), mottattRequest.getKoblingReferanse());
        if (!mottattRequest.harOppgittJournalpostId() || !mottattRequest.harOppgittInnsendingstidspunkt()) {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), "v2/motta krever at journalpostId og innsendingstidspunkt er satt på oppgitt opptjening").build();
        }

        var koblingReferanse = new KoblingReferanse(mottattRequest.getKoblingReferanse());
        var koblingLås = Optional.ofNullable(koblingTjeneste.taSkrivesLås(koblingReferanse));
        var aktørId = new AktørId(mottattRequest.getAktør().getIdent());
        var kobling = koblingTjeneste.finnEllerOpprett(mottattRequest.getYtelseType(), koblingReferanse, aktørId, new Saksnummer(mottattRequest.getSaksnummer()));

        OppgittOpptjeningBuilder builder = new MapOppgittOpptjening().mapFraDto(mottattRequest.getOppgittOpptjening());
        GrunnlagReferanse grunnlagReferanse = oppgittOpptjeningTjeneste.lagrePrJournalpostId(koblingReferanse, builder);

        koblingTjeneste.lagre(kobling);
        koblingLås.ifPresent(lås -> koblingTjeneste.oppdaterLåsVersjon(lås));

        if (grunnlagReferanse != null) {
            return Response.ok(new UuidDto(grunnlagReferanse.getReferanse())).build();
        } else {
            return Response.noContent().build();
        }
    }
    
    public static class AbacDataSupplier implements Function<Object, AbacDataAttributter> {

        @Override
        public AbacDataAttributter apply(Object o) {
            var req = (OppgittOpptjeningMottattRequest) o;
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.AKTØR_ID, req.getAktør().getIdent());
        }
    }
}
