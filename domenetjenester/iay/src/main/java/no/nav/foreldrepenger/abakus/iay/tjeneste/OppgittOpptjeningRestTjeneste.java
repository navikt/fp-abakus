package no.nav.foreldrepenger.abakus.iay.tjeneste;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.UuidDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.request.OppgittOpptjeningMottattRequest;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;

@Api(tags = "iay")
@Path("/iay/oppgitt/v1")
@ApplicationScoped
@Transaction
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
    @ApiOperation(value = "Lagrer ned mottatt ", response = UuidDto.class)
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
