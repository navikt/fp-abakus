package no.nav.foreldrepenger.abakus.iay.tjeneste;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import no.nav.abakus.iaygrunnlag.UuidDto;
import no.nav.abakus.iaygrunnlag.request.InntektsmeldingerMottattRequest;
import no.nav.abakus.iaygrunnlag.request.InntektsmeldingerRequest;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.abakus.felles.LoggUtil;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.iay.InntektsmeldingerTjeneste;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.MapInntektsmeldinger;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.kobling.KoblingTjeneste;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;

@Path("/iay/inntektsmeldinger/v1")
@RequestScoped
@Transactional
public class InntektsmeldingerRestTjeneste {

    private InntektsmeldingerTjeneste imTjeneste;
    private KoblingTjeneste koblingTjeneste;
    private InntektArbeidYtelseTjeneste iayTjeneste;

    public InntektsmeldingerRestTjeneste() {
    } // CDI Ctor

    @Inject
    public InntektsmeldingerRestTjeneste(InntektsmeldingerTjeneste imTjeneste,
                                         KoblingTjeneste koblingTjeneste,
                                         InntektArbeidYtelseTjeneste iayTjeneste) {
        this.imTjeneste = imTjeneste;
        this.koblingTjeneste = koblingTjeneste;
        this.iayTjeneste = iayTjeneste;
    }


    /**
     * Henter inntektsmeldinger for angitt aktørId og saksnummer
     * @param spesifikasjon InntektsmeldingerRequest
     * @return InntektsmeldingerDto
     */
    @POST
    @Path("/hentAlle")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.FAGSAK, sporingslogg = false)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentInntektsmeldingerForSak(@TilpassetAbacAttributt(supplierClass = InntektsmeldingerRequestAbacDataSupplier.class)
        @NotNull @Valid InntektsmeldingerRequest spesifikasjon) {
        LoggUtil.setupLogMdc(spesifikasjon.getYtelseType(), spesifikasjon.getSaksnummer(), "<alle>");

        var aktørId = new AktørId(spesifikasjon.getPerson().getIdent());
        var saksnummer = new Saksnummer(spesifikasjon.getSaksnummer());
        var ytelseType = spesifikasjon.getYtelseType();
        var inntektsmeldingerMap = iayTjeneste.hentArbeidsforholdinfoInntektsmeldingerMapFor(aktørId, saksnummer, ytelseType);
        return Response.ok(MapInntektsmeldinger.mapUnikeInntektsmeldingerFraGrunnlag(inntektsmeldingerMap)).build();
    }

    /**
     * Henter inntektsmeldinger for angitt aktørId og saksnummer
     * @param mottattRequest InntektsmeldingerRequest
     * @return UuidDto med Oppdatert grunnlagreferanse
     */
    @POST
    @Path("/motta")
    @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.FAGSAK, sporingslogg = true)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public UuidDto lagreInntektsmeldinger(@NotNull @TilpassetAbacAttributt(supplierClass = InntektsmeldingerMottattAbacDataSupplier.class) @Valid InntektsmeldingerMottattRequest mottattRequest) {
        UuidDto resultat = null;
        LoggUtil.setupLogMdc(mottattRequest.getYtelseType(), mottattRequest.getSaksnummer(), mottattRequest.getKoblingReferanse().toString());

        var aktørId = new AktørId(mottattRequest.getAktør().getIdent());

        var koblingReferanse = new KoblingReferanse(mottattRequest.getKoblingReferanse());
        var koblingLås = Optional.ofNullable(koblingTjeneste.taSkrivesLås(koblingReferanse));
        var kobling = koblingTjeneste.finnEllerOpprett(mottattRequest.getYtelseType(), koblingReferanse, aktørId,
            new Saksnummer(mottattRequest.getSaksnummer()));

        var informasjonBuilder = ArbeidsforholdInformasjonBuilder.oppdatere(imTjeneste.hentArbeidsforholdInformasjonForKobling(koblingReferanse));

        var inntektsmeldingerAggregat = new MapInntektsmeldinger.MapFraDto().map(informasjonBuilder, mottattRequest.getInntektsmeldinger());

        List<Inntektsmelding> inntektsmeldinger = inntektsmeldingerAggregat.getInntektsmeldinger();
        var grunnlagReferanse = imTjeneste.lagre(koblingReferanse, informasjonBuilder, inntektsmeldinger);

        koblingTjeneste.lagre(kobling);

        koblingLås.ifPresent(lås -> koblingTjeneste.oppdaterLåsVersjon(lås));

        if (grunnlagReferanse != null) {
            resultat = new UuidDto(grunnlagReferanse.getReferanse());
        }

        return resultat;
    }

    public static class InntektsmeldingerMottattAbacDataSupplier implements Function<Object, AbacDataAttributter> {

        @Override
        public AbacDataAttributter apply(Object obj) {
            var req = (InntektsmeldingerMottattRequest) obj;
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.SAKSNUMMER, req.getSaksnummer());
        }

    }

    public static class InntektsmeldingerRequestAbacDataSupplier implements Function<Object, AbacDataAttributter> {

        @Override
        public AbacDataAttributter apply(Object obj) {
            var req = (InntektsmeldingerRequest) obj;
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.SAKSNUMMER, req.getSaksnummer());
        }
    }
}
