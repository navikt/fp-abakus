package no.nav.foreldrepenger.abakus.iay.tjeneste;


import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

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
import no.nav.abakus.iaygrunnlag.Periode;
import no.nav.abakus.iaygrunnlag.request.InntektArbeidYtelseGrunnlagRequest;
import no.nav.abakus.iaygrunnlag.request.KopierGrunnlagRequest;
import no.nav.abakus.iaygrunnlag.request.OverstyrGrunnlagRequest;
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
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;

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

    /**
     * Hent ett enkelt IAY Grunnlag for angitt spesifikasjon. Spesifikasjonen kan angit hvilke data som ønskes
     * @param spesifikasjon InntektArbeidYtelseGrunnlagRequestAbacDto
     * @return 200 med InntektArbeidYtelseGrunnlagDto
     *        204 hvis det ikke finnes noe grunnlag
     *        304 hvis grunnlaget ikke har endret seg i henhold til det fagsystemet allerede kjenner
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.FAGSAK, sporingslogg = false)
    @SuppressWarnings({"findsecbugs:JAXRS_ENDPOINT", "resource"})
    public Response hentIayGrunnlag(@TilpassetAbacAttributt(supplierClass = InntektArbeidYtelseGrunnlagRequestAbacDataSupplier.class)
        @NotNull @Valid InntektArbeidYtelseGrunnlagRequest spesifikasjon) {
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

    /**
     * Lagrer siste versjon
     * @param dto OverstyrtInntektArbeidYtelseDto
     * @return 200 ok
     */
    @PUT
    @Path("/overstyr-grunnlag")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @BeskyttetRessurs(actionType = ActionType.UPDATE, resourceType = ResourceType.FAGSAK, sporingslogg = true)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response overstyrGrunnlag(@TilpassetAbacAttributt(supplierClass = OverstyrGrunnlagRequestAbacDataSupplier.class)
                                               @NotNull @Valid OverstyrGrunnlagRequest dto) {

        var aktørId = new AktørId(dto.getAktør().getIdent());
        var koblingReferanse = getKoblingReferanse(aktørId, dto.getKoblingReferanse(), dto.getGrunnlagReferanse());

        setupLogMdcFraKoblingReferanse(koblingReferanse);

        var nyttGrunnlagBuilder = InntektArbeidYtelseGrunnlagBuilder.oppdatere(iayTjeneste.hentGrunnlagFor(koblingReferanse));

        new IAYFraDtoMapper(iayTjeneste, aktørId, koblingReferanse).mapOverstyringerTilGrunnlagBuilder(dto.getOverstyrt(),
            dto.getArbeidsforholdInformasjon(), nyttGrunnlagBuilder);

        iayTjeneste.lagre(koblingReferanse, nyttGrunnlagBuilder);

        return Response.ok().build();
    }

    /**
     * Koperer grunnlaget
     * @param request KopierGrunnlagRequest
     * @return 200 ok
     */
    @POST
    @Path("/kopier")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.FAGSAK, sporingslogg = true)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response kopierOgLagreGrunnlagUtenInntektsmeldinger(@TilpassetAbacAttributt(supplierClass = KopierGrunnlagRequestAbacDataSupplier.class)
        @NotNull @Valid KopierGrunnlagRequest request) {
        kopierOgLagreGrunnlag(request, false);
        return Response.ok().build();
    }

    /**
     * Kopier grunnlag behold opprinnelige inntektsmeldinger
     * @param request KopierGrunnlagRequest
     * @return 200 ok
     */
    @POST
    @Path("/kopier-behold-im")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.FAGSAK, sporingslogg = true)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response kopierOgLagreGrunnlagBeholdIM(@TilpassetAbacAttributt(supplierClass = KopierGrunnlagRequestAbacDataSupplier.class)
        @NotNull @Valid KopierGrunnlagRequest request) {
        kopierOgLagreGrunnlag(request, true);
        return Response.ok().build();
    }

    private void kopierOgLagreGrunnlag(KopierGrunnlagRequest request, boolean beholdInntektsmeldinger) {
        var koblingReferanse = new KoblingReferanse(request.getNyReferanse());
        var koblingLås = Optional.ofNullable(koblingTjeneste.taSkrivesLås(koblingReferanse));

        setupLogMdcFraKoblingReferanse(koblingReferanse);

        var kobling = oppdaterKobling(request);

        iayTjeneste.kopierGrunnlagFraEksisterendeBehandling(kobling.getYtelseType(), kobling.getAktørId(), new Saksnummer(request.getSaksnummer()),
            new KoblingReferanse(request.getGammelReferanse()), new KoblingReferanse(request.getNyReferanse()), request.getDataset(), beholdInntektsmeldinger);

        koblingLås.ifPresent(lås -> koblingTjeneste.oppdaterLåsVersjon(lås));
    }

    private UUID utledSisteKjenteGrunnlagReferanseFraSpesifikasjon(InntektArbeidYtelseGrunnlagRequest spesifikasjon) {
        final var sisteKjenteGrunnlagReferanse = spesifikasjon.getSisteKjenteGrunnlagReferanse();
        final var forespurtGrunnlagReferanse = spesifikasjon.getGrunnlagReferanse();

        return forespurtGrunnlagReferanse == null || forespurtGrunnlagReferanse.equals(sisteKjenteGrunnlagReferanse) ? sisteKjenteGrunnlagReferanse : null;
    }

    private Kobling oppdaterKobling(@NotNull @Valid KopierGrunnlagRequest dto) {
        var referanse = new KoblingReferanse(dto.getNyReferanse());
        var kobling = koblingTjeneste.hentFor(referanse)
            .orElse(new Kobling(dto.getYtelseType(), new Saksnummer(dto.getSaksnummer()), referanse, new AktørId((dto.getAktør().getIdent()))));

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

    public static class InntektArbeidYtelseGrunnlagRequestAbacDataSupplier implements Function<Object, AbacDataAttributter> {

        @Override
        public AbacDataAttributter apply(Object obj) {
            var req = (InntektArbeidYtelseGrunnlagRequest) obj;
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.SAKSNUMMER, req.getSaksnummer());
        }
    }

    public static class KopierGrunnlagRequestAbacDataSupplier implements Function<Object, AbacDataAttributter> {

        @Override
        public AbacDataAttributter apply(Object obj) {
            var req = (KopierGrunnlagRequest) obj;
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.SAKSNUMMER, req.getSaksnummer());
        }
    }

    public static class OverstyrGrunnlagRequestAbacDataSupplier implements Function<Object, AbacDataAttributter> {

        @Override
        public AbacDataAttributter apply(Object obj) {
            var req = (OverstyrGrunnlagRequest) obj;
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.SAKSNUMMER, req.getSaksnummer());
        }
    }
}
