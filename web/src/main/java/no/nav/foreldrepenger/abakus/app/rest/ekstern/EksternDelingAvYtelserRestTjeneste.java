package no.nav.foreldrepenger.abakus.app.rest.ekstern;

import static no.nav.foreldrepenger.abakus.felles.sikkerhet.AbakusBeskyttetRessursAttributt.VEDTAK;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
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
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.ps.InfotrygdPSGrunnlag;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.ps.PS;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.foreldrepenger.abakus.typer.Stillingsprosent;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseRepository;
import no.nav.foreldrepenger.abakus.vedtak.extract.v1.ConvertToYtelseV1;
import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.GrunnlagRequest;
import no.nav.vedtak.konfig.Tid;
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

    private static final Set<Ytelser> K9_INFOTRYGD_YTELSER = Set.of(Ytelser.PLEIEPENGER_NÆRSTÅENDE, Ytelser.OPPLÆRINGSPENGER, Ytelser.PLEIEPENGER_SYKT_BARN);

    private VedtakYtelseRepository ytelseRepository;
    private AktørTjeneste aktørTjeneste;
    private InfotrygdPSGrunnlag infotrygdPSGrunnlag;

    public EksternDelingAvYtelserRestTjeneste() {
    } // CDI Ctor

    @Inject
    public EksternDelingAvYtelserRestTjeneste(VedtakYtelseRepository ytelseRepository,
                                              @PS InfotrygdPSGrunnlag infotrygdPSGrunnlag,
                                              AktørTjeneste aktørTjeneste) {
        this.ytelseRepository = ytelseRepository;
        this.aktørTjeneste = aktørTjeneste;
        this.infotrygdPSGrunnlag = infotrygdPSGrunnlag;
    }

    private static ArbeidsgiverIdent mapArbeidsgiverIdent(no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver arbeidsgiver) {
        if (arbeidsgiver == null) {
            return null;
        }
        return new ArbeidsgiverIdent(arbeidsgiver.getIdentifikator());
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
        ytelser.addAll(hentVedtakYtelseInfotrygdK9Intern(request));

        return ytelser;
    }


    public List<Ytelse> hentVedtakYtelseInfotrygdK9Intern(VedtakForPeriodeRequest request) {
        if (request.getYtelser().isEmpty() || K9_INFOTRYGD_YTELSER.stream().noneMatch(y -> request.getYtelser().contains(y))) {
            return List.of();
        }

        var aktørIdOpt = utledEnkeltAktørIdFraRequest(request.getIdent(), utledTemaFraYtelser(request.getYtelser()));
        if (aktørIdOpt.isEmpty()) {
            return List.of();
        }

        var aktørId = aktørIdOpt.orElseThrow();
        var identer = utledPersonIdentFraRequest(request.getIdent(), utledTemaFraYtelser(request.getYtelser()));
        var periode = IntervallEntitet.fraOgMedTilOgMed(request.getPeriode().getFom(), request.getPeriode().getTom());
        var fnr = identer.stream().map(PersonIdent::getIdent).toList();
        var inforequest = new GrunnlagRequest(fnr, Tid.fomEllerBegynnelse(periode.getFomDato()), Tid.tomEllerEndetid(periode.getTomDato()));
        var infotrygdYtelser = infotrygdPSGrunnlag.hentGrunnlagFailSoft(inforequest);
        var mappedYtelser =  InnhentingInfotrygdTjeneste.mapTilInfotrygdYtelseGrunnlag(infotrygdYtelser, periode.getFomDato()).stream()
            .map(InfotrygdgrunnlagYtelseMapper::oversettInfotrygdYtelseGrunnlagTilYtelse)
            .map(it -> ytelseTilYtelse(aktørId, it))
            .filter(it -> request.getYtelser().contains(it.getYtelse()))
            .toList();
        var ytelser = new ArrayList<Ytelse>(mappedYtelser);
        return ytelser;
    }

    private Set<AktørId> utledAktørIdFraRequest(Aktør aktør, YtelseType tema) {
        if (aktør.erAktørId()) {
            return Set.of(new AktørId(aktør.getVerdi()));
        }
        return aktørTjeneste.hentAktørIderForIdent(new PersonIdent(aktør.getVerdi()), tema);
    }

    private Optional<AktørId> utledEnkeltAktørIdFraRequest(Aktør aktør, YtelseType tema) {
        if (aktør.erAktørId()) {
            return Optional.of(new AktørId(aktør.getVerdi()));
        }
        return aktørTjeneste.hentAktørForIdent(new PersonIdent(aktør.getVerdi()), tema);
    }

    private Set<PersonIdent> utledPersonIdentFraRequest(Aktør aktør, YtelseType tema) {
        if (aktør.erAktørId()) {
            return aktørTjeneste.hentPersonIdenterForAktør(new AktørId(aktør.getVerdi()), tema);

        }
        return Set.of(new PersonIdent(aktør.getVerdi()));
    }

    private YtelseType utledTemaFraYtelser(Set<Ytelser> request) {
        if (request.contains(Ytelser.FORELDREPENGER)) {
            return YtelseType.FORELDREPENGER;
        }

        return YtelseType.OMSORGSPENGER;
    }

    private YtelseV1 ytelseTilYtelse(AktørId aktørId, no.nav.foreldrepenger.abakus.domene.iay.Ytelse vedtak) {
        var ytelse = new YtelseV1();
        var aktør = new Aktør();
        aktør.setVerdi(aktørId.getId());
        ytelse.setAktør(aktør);
        ytelse.setVedtattTidspunkt(Optional.ofNullable(vedtak.getVedtattTidspunkt()).orElseGet(LocalDateTime::now));
        ytelse.setYtelse(ConvertToYtelseV1.mapYtelser(vedtak.getRelatertYtelseType()));
        Optional.ofNullable(vedtak.getSaksreferanse()).map(Saksnummer::getVerdi).ifPresent(ytelse::setSaksnummer);
        ytelse.setYtelseStatus(ConvertToYtelseV1.mapStatus(vedtak.getStatus()));
        ytelse.setKildesystem(ConvertToYtelseV1.mapKildesystem(vedtak.getKilde()));
        ytelse.setVedtakReferanse(UUID.randomUUID().toString()); // NotNull i kontrakt
        var periode = new Periode();
        periode.setFom(Optional.ofNullable(vedtak.getPeriode()).map(IntervallEntitet::getFomDato).orElseGet(LocalDate::now));
        periode.setTom(Optional.ofNullable(vedtak.getPeriode()).map(IntervallEntitet::getTomDato).orElseGet(LocalDate::now));
        ytelse.setPeriode(periode);
        var anvist = vedtak.getYtelseAnvist().stream().map(this::mapLagretInfotrygdAnvist).toList();
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
        return anvist.getYtelseAnvistAndeler()
            .stream()
            .map(a -> new AnvistAndel(a.getArbeidsgiver().map(EksternDelingAvYtelserRestTjeneste::mapArbeidsgiverIdent).orElse(null),
                a.getArbeidsforholdRef().getReferanse(), new Desimaltall(a.getDagsats().getVerdi()),
                a.getUtbetalingsgradProsent() == null ? null : new Desimaltall(a.getUtbetalingsgradProsent().getVerdi()),
                a.getRefusjonsgradProsent() == null ? null : new Desimaltall(a.getRefusjonsgradProsent().getVerdi()),
                ConvertToYtelseV1.fraInntektskategori(a.getInntektskategori())))
            .toList();
    }

    public static class VedtakForPeriodeRequestAbacDataSupplier implements Function<Object, AbacDataAttributter> {

        public VedtakForPeriodeRequestAbacDataSupplier() {
            // Jackson
        }

        @Override
        public AbacDataAttributter apply(Object obj) {
            var req = (VedtakForPeriodeRequest) obj;
            var attributeType = req.getIdent().erAktørId() ? StandardAbacAttributtType.AKTØR_ID : StandardAbacAttributtType.FNR;
            return AbacDataAttributter.opprett().leggTil(attributeType, req.getIdent().getVerdi());
        }
    }

}
