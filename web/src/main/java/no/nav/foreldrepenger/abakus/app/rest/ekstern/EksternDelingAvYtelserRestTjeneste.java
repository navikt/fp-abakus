package no.nav.foreldrepenger.abakus.app.rest.ekstern;

import static no.nav.foreldrepenger.abakus.felles.sikkerhet.AbakusBeskyttetRessursAttributt.VEDTAK;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.abakus.iaygrunnlag.request.HentBrukersK9YtelserIPeriodeRequest;
import no.nav.abakus.iaygrunnlag.request.HentBrukersYtelserIPeriodeRequest;
import no.nav.abakus.vedtak.ytelse.Aktør;
import no.nav.abakus.vedtak.ytelse.Desimaltall;
import no.nav.abakus.vedtak.ytelse.Periode;
import no.nav.abakus.vedtak.ytelse.Ytelse;
import no.nav.abakus.vedtak.ytelse.Ytelser;
import no.nav.abakus.vedtak.ytelse.request.VedtakForPeriodeRequest;
import no.nav.abakus.vedtak.ytelse.v1.YtelseV1;
import no.nav.abakus.vedtak.ytelse.v1.anvisning.Anvisning;
import no.nav.abakus.vedtak.ytelse.v1.anvisning.AnvistAndel;
import no.nav.abakus.vedtak.ytelse.v1.anvisning.ArbeidsgiverIdent;
import no.nav.foreldrepenger.abakus.aktor.AktørTjeneste;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.registerdata.infotrygd.InfotrygdgrunnlagYtelseMapper;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.InnhentingInfotrygdTjeneste;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.foreldrepenger.abakus.typer.Stillingsprosent;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseRepository;
import no.nav.foreldrepenger.abakus.vedtak.extract.v1.ConvertToYtelseV1;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.AvailabilityType;

@OpenAPIDefinition(tags = @Tag(name = "ekstern"), servers = @Server())
@Path("/ytelse/v1")
@ApplicationScoped
@Transactional
public class EksternDelingAvYtelserRestTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(EksternDelingAvYtelserRestTjeneste.class);

    private static final Set<YtelseType> GYLDIGE_YTELSER = Set.of(YtelseType.PLEIEPENGER_NÆRSTÅENDE,
        YtelseType.FORELDREPENGER,
        YtelseType.OMSORGSPENGER,
        YtelseType.OPPLÆRINGSPENGER,
        YtelseType.FRISINN,
        YtelseType.SVANGERSKAPSPENGER,
        YtelseType.PLEIEPENGER_SYKT_BARN);

    // Kapittel 9 (ekskluderer FRISINN)
    private static final Set<YtelseType> K9_YTELSER = Set.of(YtelseType.PLEIEPENGER_NÆRSTÅENDE,
        YtelseType.OMSORGSPENGER,
        YtelseType.OPPLÆRINGSPENGER,
        YtelseType.PLEIEPENGER_SYKT_BARN);

    private VedtakYtelseRepository ytelseRepository;
    private AktørTjeneste aktørTjeneste;
    private InnhentingInfotrygdTjeneste innhentingInfotrygdTjeneste;

    public EksternDelingAvYtelserRestTjeneste() {
    } // CDI Ctor

    @Inject
    public EksternDelingAvYtelserRestTjeneste(VedtakYtelseRepository ytelseRepository, InnhentingInfotrygdTjeneste innhentingInfotrygdTjeneste, AktørTjeneste aktørTjeneste) {
        this.ytelseRepository = ytelseRepository;
        this.aktørTjeneste = aktørTjeneste;
        this.innhentingInfotrygdTjeneste = innhentingInfotrygdTjeneste;
    }

    @POST
    @Path("/hent-ytelse-vedtak")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Henter alle vedtak for en gitt person, evt med periode etter en fom", tags = "ytelse")
    @BeskyttetRessurs(actionType = ActionType.READ, resource = VEDTAK, availabilityType = AvailabilityType.ALL)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public List<Ytelse> hentVedtakYtelse(@NotNull @TilpassetAbacAttributt(supplierClass = EksternDelingAvYtelserRestTjeneste.VedtakForPeriodeRequestAbacDataSupplier.class) @Valid VedtakForPeriodeRequest request) {
        LOG.info("ABAKUS VEDTAK ekstern /hent-ytelse-vedtak for ytelser {}", request.getYtelser());

        if (request.getYtelser().isEmpty()) {
            return List.of();
        }

        Set<AktørId> aktørIder = utledAktørIdFraRequest(request.getIdent(), utledTemaFraYtelser(request.getYtelser()));
        var periode = IntervallEntitet.fraOgMedTilOgMed(request.getPeriode().getFom(), request.getPeriode().getTom());
        var ytelser = new ArrayList<Ytelse>();
        for (AktørId aktørId : aktørIder) {
            ytelser.addAll(ytelseRepository.hentYtelserForIPeriode(aktørId, periode)
                .stream()
                .filter(it -> request.getYtelser().contains(ConvertToYtelseV1.mapYtelser(it.getYtelseType())))
                .map(ConvertToYtelseV1::convert)
                .toList());
        }

        return ytelser;
    }

    private Set<AktørId> utledAktørIdFraRequest(Aktør aktør, YtelseType tema) {
        if (aktør.erAktørId()) {
            return Set.of(new AktørId(aktør.getVerdi()));
        }
        return aktørTjeneste.hentAktørIderForIdent(new PersonIdent(aktør.getVerdi()), tema);
    }

    private YtelseType utledTemaFraYtelser(Set<Ytelser> request) {
        if (request.contains(Ytelser.FORELDREPENGER)) {
            return YtelseType.FORELDREPENGER;
        }

        return YtelseType.OMSORGSPENGER;
    }

    @POST
    @Path("/hent-vedtatte/for-ident")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Henter alle vedtak for ytelser det blir etterspurt", tags = "ekstern")
    @BeskyttetRessurs(actionType = ActionType.READ, resource = VEDTAK, availabilityType = AvailabilityType.ALL)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public List<Ytelse> hentVedtakForPerson(@NotNull @TilpassetAbacAttributt(supplierClass = HentBrukersYtelserIPeriodeRequestAbacDataSupplier.class) @Valid HentBrukersYtelserIPeriodeRequest request) {

        LOG.info("ABAKUS VEDTAK ekstern /hent-vedtatte/for-ident for ytelser {}", request.getYtelser());

        var etterspurteYtelser = request.getYtelser()
            .stream()
            .filter(GYLDIGE_YTELSER::contains)
            .collect(Collectors.toSet());

        return hentUtYtelser(request, etterspurteYtelser, false);
    }


    @POST
    @Path("/hent-vedtatte/for-ident/k9")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Henter alle k9 vedtak for ytelser det blir etterspurt", tags = "ekstern")
    @BeskyttetRessurs(actionType = ActionType.READ, resource = VEDTAK, availabilityType = AvailabilityType.ALL)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public List<Ytelse> hentk9VedtakForPerson(@NotNull @TilpassetAbacAttributt(supplierClass = HentBrukersK9YtelserIPeriodeRequestAbacDataSupplier.class) @Valid HentBrukersK9YtelserIPeriodeRequest req) {
        LOG.info("ABAKUS VEDTAK ekstern /hent-vedtatte/for-ident/k9");
        var request = new HentBrukersYtelserIPeriodeRequest(req.getPersonident(), req.getPeriode(), K9_YTELSER);
        return hentUtYtelser(request, K9_YTELSER, false);
    }

    @POST
    @Path("/hent-vedtatte-og-historiske/for-ident/k9")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Henter alle k9 vedtak for ytelser det blir etterspurt og tar med historikk", tags = "ekstern")
    @BeskyttetRessurs(actionType = ActionType.READ, resource = VEDTAK, availabilityType = AvailabilityType.ALL)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public List<Ytelse> hentk9VedtakForPersonMedHistorikk(@NotNull @TilpassetAbacAttributt(supplierClass = HentBrukersK9YtelserIPeriodeRequestAbacDataSupplier.class) @Valid HentBrukersK9YtelserIPeriodeRequest req) {
        LOG.info("ABAKUS VEDTAK ekstern /hent-vedtatte-og-historiske/for-ident/k9");
        var request = new HentBrukersYtelserIPeriodeRequest(req.getPersonident(), req.getPeriode(), K9_YTELSER);
        return hentUtYtelser(request, K9_YTELSER, true);
    }

    private List<Ytelse> hentUtYtelser(HentBrukersYtelserIPeriodeRequest request, Set<YtelseType> etterspurteYtelser, boolean hentHistoriske) {
        if (etterspurteYtelser.isEmpty()) {
            return List.of();
        }

        var ytelser = new ArrayList<Ytelse>();
        var fnr = new PersonIdent(request.getPersonident().getIdent());
        var aktørIder = aktørTjeneste.hentAktørIderForIdent(fnr, utledTema(etterspurteYtelser));

        LocalDate fom = request.getPeriode().getFom();
        LocalDate tom = request.getPeriode().getTom();

        var periode = IntervallEntitet.fraOgMedTilOgMed(fom, tom);

        if (hentHistoriske) {
            var aktørId = aktørTjeneste.hentAktørForIdent(fnr, utledTema(etterspurteYtelser)).orElseThrow();
            var infotrygdYtelser = innhentingInfotrygdTjeneste.getInfotrygdYtelser(fnr, periode);
            ytelser.addAll(infotrygdYtelser.stream()
                .map(InfotrygdgrunnlagYtelseMapper::oversettInfotrygdYtelseGrunnlagTilYtelse)
                .filter(it -> etterspurteYtelser.contains(it.getRelatertYtelseType()))
                .map(it -> ytelseTilYtelse(aktørId, it))
                .toList());
        }
        for (AktørId aktørId : aktørIder) {
            ytelser.addAll(ytelseRepository.hentYtelserForIPeriode(aktørId, periode)
                .stream()
                .filter(it -> etterspurteYtelser.contains(it.getYtelseType()))
                .map(ConvertToYtelseV1::convert)
                .toList());
        }

        return ytelser;
    }

    private YtelseV1 ytelseTilYtelse(AktørId aktørId, no.nav.foreldrepenger.abakus.domene.iay.Ytelse vedtak) {
        var ytelse = new YtelseV1();
        var aktør = new Aktør();
        aktør.setVerdi(aktørId.getId());
        ytelse.setAktør(aktør);
        ytelse.setVedtattTidspunkt(vedtak.getVedtattTidspunkt());
        ytelse.setYtelse(ConvertToYtelseV1.mapYtelser(vedtak.getRelatertYtelseType()));
        ytelse.setSaksnummer(vedtak.getSaksreferanse().getVerdi());
        ytelse.setYtelseStatus(ConvertToYtelseV1.mapStatus(vedtak.getStatus()));
        ytelse.setKildesystem(ConvertToYtelseV1.mapKildesystem(vedtak.getKilde()));
        var periode = new Periode();
        periode.setFom(vedtak.getPeriode().getFomDato());
        periode.setTom(vedtak.getPeriode().getTomDato());
        ytelse.setPeriode(periode);
        var anvist = vedtak.getYtelseAnvist().stream().map(this::mapLagretInfotrygdAnvist).collect(Collectors.toList());
        ytelse.setAnvist(anvist);
        return ytelse;
    }

    private Anvisning mapLagretInfotrygdAnvist(no.nav.foreldrepenger.abakus.domene.iay.YtelseAnvist anvist) {
        var anvisning = new Anvisning();
        var periode = new Periode();
        periode.setFom(anvist.getAnvistFOM());
        periode.setTom(anvist.getAnvistTOM());
        anvisning.setPeriode(periode);
        anvist.getBeløp().map(Beløp::getVerdi).map(Desimaltall::new).ifPresent(anvisning::setBeløp);
        anvist.getDagsats().map(Beløp::getVerdi).map(Desimaltall::new).ifPresent(anvisning::setDagsats);
        anvist.getUtbetalingsgradProsent().map(Stillingsprosent::getVerdi).map(Desimaltall::new).ifPresent(anvisning::setUtbetalingsgrad);
        anvisning.setAndeler(mapInfotrygdAndeler(anvist));

        return anvisning;
    }

    private List<AnvistAndel> mapInfotrygdAndeler(no.nav.foreldrepenger.abakus.domene.iay.YtelseAnvist anvist) {
        return anvist.getYtelseAnvistAndeler().stream().map(a -> new AnvistAndel(
            a.getArbeidsgiver().map(EksternDelingAvYtelserRestTjeneste::mapArbeidsgiverIdent).orElse(null),
            a.getArbeidsforholdRef().getReferanse(),
            new Desimaltall(a.getDagsats().getVerdi()),
            a.getUtbetalingsgradProsent() == null ? null : new Desimaltall(a.getUtbetalingsgradProsent().getVerdi()),
            a.getRefusjonsgradProsent() == null ? null : new Desimaltall(a.getRefusjonsgradProsent().getVerdi()),
            ConvertToYtelseV1.fraInntektskategori(a.getInntektskategori())
        )).collect(Collectors.toList());
    }

    private static ArbeidsgiverIdent mapArbeidsgiverIdent(no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver arbeidsgiver) {
        if (arbeidsgiver == null) {
            return null;
        }
        return new ArbeidsgiverIdent(arbeidsgiver.getIdentifikator());
    }

    private YtelseType utledTema(Set<YtelseType> request) {
        if (request.contains(YtelseType.FORELDREPENGER)) {
            return YtelseType.FORELDREPENGER;
        }

        return YtelseType.OMSORGSPENGER;
    }


    public static class HentBrukersYtelserIPeriodeRequestAbacDataSupplier implements Function<Object, AbacDataAttributter> {

        public HentBrukersYtelserIPeriodeRequestAbacDataSupplier() {
        }

        @Override
        public AbacDataAttributter apply(Object obj) {
            HentBrukersYtelserIPeriodeRequest req = (HentBrukersYtelserIPeriodeRequest) obj;
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.FNR, req.getPersonident().getIdent());
        }
    }

    public static class HentBrukersK9YtelserIPeriodeRequestAbacDataSupplier implements Function<Object, AbacDataAttributter> {

        public HentBrukersK9YtelserIPeriodeRequestAbacDataSupplier() {
        }

        @Override
        public AbacDataAttributter apply(Object obj) {
            HentBrukersK9YtelserIPeriodeRequest req = (HentBrukersK9YtelserIPeriodeRequest) obj;
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.FNR, req.getPersonident().getIdent());
        }
    }

    public static class VedtakForPeriodeRequestAbacDataSupplier implements Function<Object, AbacDataAttributter> {

        public VedtakForPeriodeRequestAbacDataSupplier() {
        }

        @Override
        public AbacDataAttributter apply(Object obj) {
            var req = (VedtakForPeriodeRequest) obj;
            var attributeType = req.getIdent().erAktørId() ? StandardAbacAttributtType.AKTØR_ID : StandardAbacAttributtType.FNR;
            return AbacDataAttributter.opprett().leggTil(attributeType, req.getIdent().getVerdi());
        }
    }

}
