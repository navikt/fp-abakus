package no.nav.foreldrepenger.abakus.vedtak.tjeneste;

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
import io.swagger.v3.oas.annotations.tags.Tag;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.abakus.iaygrunnlag.request.AktørDatoRequest;
import no.nav.abakus.iaygrunnlag.request.HentBrukersYtelserIPeriodeRequest;
import no.nav.abakus.vedtak.ytelse.Aktør;
import no.nav.abakus.vedtak.ytelse.Ytelse;
import no.nav.abakus.vedtak.ytelse.Ytelser;
import no.nav.abakus.vedtak.ytelse.request.VedtakForPeriodeRequest;
import no.nav.foreldrepenger.abakus.aktor.AktørTjeneste;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseRepository;
import no.nav.foreldrepenger.abakus.vedtak.extract.v1.ConvertToYtelseV1;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;

@OpenAPIDefinition(tags = @Tag(name = "ytelse"))
@Path("/ytelse/v1")
@ApplicationScoped
@Transactional
public class YtelseRestTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(YtelseRestTjeneste.class);

    private static final Set<YtelseType> GYLDIGE_YTELSER = Set.of(YtelseType.PLEIEPENGER_NÆRSTÅENDE, YtelseType.FORELDREPENGER,
        YtelseType.OMSORGSPENGER, YtelseType.OPPLÆRINGSPENGER, YtelseType.FRISINN, YtelseType.SVANGERSKAPSPENGER, YtelseType.PLEIEPENGER_SYKT_BARN);

    private VedtakYtelseRepository ytelseRepository;
    private AktørTjeneste aktørTjeneste;

    public YtelseRestTjeneste() {
    } // CDI Ctor

    @Inject
    public YtelseRestTjeneste(VedtakYtelseRepository ytelseRepository, AktørTjeneste aktørTjeneste) {
        this.ytelseRepository = ytelseRepository;
        this.aktørTjeneste = aktørTjeneste;
    }

    @POST
    @Path("/hent-vedtak-ytelse")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Henter alle vedtak for en gitt person, evt med periode etter en fom", tags = "ytelse")
    @BeskyttetRessurs(actionType = ActionType.READ, resource = VEDTAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public List<Ytelse> hentVedtakYtelse(@NotNull @TilpassetAbacAttributt(supplierClass = VedtakForPeriodeRequestAbacDataSupplier.class) @Valid VedtakForPeriodeRequest request) {

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

    @POST
    @Path("/hent-vedtatte")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Henter alle vedtak for en gitt person, evt med periode etter en fom", tags = "ytelse")
    @BeskyttetRessurs(actionType = ActionType.READ, resource = VEDTAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public List<Ytelse> hentVedtakForPerson(@NotNull @TilpassetAbacAttributt(supplierClass = HentBrukersYtelserIPeriodeRequestAbacDataSupplier.class) @Valid HentBrukersYtelserIPeriodeRequest request) {

        LOG.info("ABAKUS VEDTAK intern /hent-vedtak-ytelse for ytelser {}", request.getYtelser());

        var etterspurteYtelser = request.getYtelser().stream().filter(GYLDIGE_YTELSER::contains).collect(Collectors.toSet());

        if (etterspurteYtelser.isEmpty()) {
            return List.of();
        }

        var ytelser = new ArrayList<Ytelse>();
        var aktørIder = aktørTjeneste.hentAktørIderForIdent(new PersonIdent(request.getPersonident().getIdent()), utledTema(etterspurteYtelser));

        LocalDate fom = request.getPeriode().getFom();
        LocalDate tom = request.getPeriode().getTom();

        var periode = IntervallEntitet.fraOgMedTilOgMed(fom, tom);

        for (AktørId aktørId : aktørIder) {
            ytelser.addAll(ytelseRepository.hentYtelserForIPeriode(aktørId, periode)
                .stream()
                .filter(it -> etterspurteYtelser.contains(it.getYtelseType()))
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

    private YtelseType utledTema(Set<YtelseType> request) {
        if (request.contains(YtelseType.FORELDREPENGER)) {
            return YtelseType.FORELDREPENGER;
        }

        return YtelseType.OMSORGSPENGER;
    }

    private YtelseType utledTemaFraYtelser(Set<Ytelser> request) {
        if (request.contains(Ytelser.FORELDREPENGER)) {
            return YtelseType.FORELDREPENGER;
        }

        return YtelseType.OMSORGSPENGER;
    }

    public static class AktørDatoRequestAbacDataSupplier implements Function<Object, AbacDataAttributter> {

        public AktørDatoRequestAbacDataSupplier() {
        }

        @Override
        public AbacDataAttributter apply(Object obj) {
            AktørDatoRequest req = (AktørDatoRequest) obj;
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.AKTØR_ID, req.getAktør().getIdent());
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

    public static class HentBrukersYtelserIPeriodeRequestAbacDataSupplier implements Function<Object, AbacDataAttributter> {

        public HentBrukersYtelserIPeriodeRequestAbacDataSupplier() {
        }

        @Override
        public AbacDataAttributter apply(Object obj) {
            HentBrukersYtelserIPeriodeRequest req = (HentBrukersYtelserIPeriodeRequest) obj;
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.FNR, req.getPersonident().getIdent());
        }
    }


}
