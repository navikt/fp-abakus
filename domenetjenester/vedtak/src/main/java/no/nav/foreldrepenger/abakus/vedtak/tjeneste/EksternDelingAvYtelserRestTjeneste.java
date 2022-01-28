package no.nav.foreldrepenger.abakus.vedtak.tjeneste;

import static no.nav.foreldrepenger.abakus.felles.sikkerhet.AbakusBeskyttetRessursAttributt.VEDTAK;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;

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

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import no.nav.abakus.iaygrunnlag.AktørIdPersonident;
import no.nav.abakus.iaygrunnlag.Organisasjon;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.abakus.iaygrunnlag.request.HentBrukersK9YtelserIPeriodeRequest;
import no.nav.abakus.iaygrunnlag.request.HentBrukersYtelserIPeriodeRequest;
import no.nav.abakus.vedtak.ytelse.Aktør;
import no.nav.abakus.vedtak.ytelse.Desimaltall;
import no.nav.abakus.vedtak.ytelse.Periode;
import no.nav.abakus.vedtak.ytelse.Ytelse;
import no.nav.abakus.vedtak.ytelse.v1.YtelseV1;
import no.nav.abakus.vedtak.ytelse.v1.anvisning.Anvisning;
import no.nav.abakus.vedtak.ytelse.v1.anvisning.AnvistAndel;
import no.nav.foreldrepenger.abakus.aktor.AktørTjeneste;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.foreldrepenger.abakus.typer.Stillingsprosent;
import no.nav.foreldrepenger.abakus.vedtak.domene.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelse;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseRepository;
import no.nav.foreldrepenger.abakus.vedtak.domene.YtelseAnvist;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;

@OpenAPIDefinition(tags = @Tag(name = "ekstern"), servers = @Server())
@Path("/ytelse/v1")
@ApplicationScoped
@Transactional
public class EksternDelingAvYtelserRestTjeneste {

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

    public EksternDelingAvYtelserRestTjeneste() {
    } // CDI Ctor

    @Inject
    public EksternDelingAvYtelserRestTjeneste(VedtakYtelseRepository ytelseRepository, AktørTjeneste aktørTjeneste) {
        this.ytelseRepository = ytelseRepository;
        this.aktørTjeneste = aktørTjeneste;
    }

    private static no.nav.abakus.iaygrunnlag.Aktør mapArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        if (arbeidsgiver == null) {
            return null;
        }
        return arbeidsgiver.getOrgnr() != null ?
            new Organisasjon(arbeidsgiver.getIdentifikator()) :
            new AktørIdPersonident(arbeidsgiver.getIdentifikator());
    }

    @POST
    @Path("/hent-vedtatte/for-ident")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Henter alle vedtak for ytelser det blir etterspurt", tags = "ekstern")
    @BeskyttetRessurs(action = READ, resource = VEDTAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public List<Ytelse> hentVedtakForPerson(@NotNull @TilpassetAbacAttributt(supplierClass = HentBrukersYtelserIPeriodeRequestAbacDataSupplier.class) @Valid HentBrukersYtelserIPeriodeRequest request) {

        var etterspurteYtelser = request.getYtelser()
            .stream()
            .filter(GYLDIGE_YTELSER::contains)
            .collect(Collectors.toSet());

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
                .map(this::mapLagretVedtakTilYtelse)
                .toList());
        }

        return ytelser;
    }

    @POST
    @Path("/hent-vedtatte/for-ident/k9")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Henter alle k9 vedtak for ytelser det blir etterspurt", tags = "ekstern")
    @BeskyttetRessurs(action = READ, resource = VEDTAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public List<Ytelse> hentk9VedtakForPerson(@NotNull @TilpassetAbacAttributt(supplierClass = HentBrukersK9YtelserIPeriodeRequestAbacDataSupplier.class) @Valid HentBrukersK9YtelserIPeriodeRequest request) {

        return hentVedtakForPerson(new HentBrukersYtelserIPeriodeRequest(request.getPersonident(), request.getPeriode(), K9_YTELSER));
    }

    private YtelseType utledTema(Set<YtelseType> request) {
        if (request.contains(YtelseType.FORELDREPENGER)) {
            return YtelseType.FORELDREPENGER;
        }

        return YtelseType.OMSORGSPENGER;
    }

    private Ytelse mapLagretVedtakTilYtelse(VedtakYtelse vedtak) {
        var ytelse = new YtelseV1();
        var aktør = new Aktør();
        aktør.setVerdi(vedtak.getAktør().getId());
        ytelse.setAktør(aktør);
        ytelse.setVedtattTidspunkt(vedtak.getVedtattTidspunkt());
        ytelse.setType(vedtak.getYtelseType());
        ytelse.setSaksnummer(vedtak.getSaksnummer().getVerdi());
        ytelse.setVedtakReferanse(vedtak.getVedtakReferanse().toString());
        ytelse.setStatus(vedtak.getStatus());
        ytelse.setFagsystem(vedtak.getKilde());
        ytelse.setTilleggsopplysninger(vedtak.getTilleggsopplysninger());
        var periode = new Periode();
        periode.setFom(vedtak.getPeriode().getFomDato());
        periode.setTom(vedtak.getPeriode().getTomDato());
        ytelse.setPeriode(periode);
        var anvist = vedtak.getYtelseAnvist().stream().map(this::mapLagretAnvist).collect(Collectors.toList());
        ytelse.setAnvist(anvist);
        return ytelse;
    }

    private Anvisning mapLagretAnvist(YtelseAnvist anvist) {
        var anvisning = new Anvisning();
        var periode = new Periode();
        periode.setFom(anvist.getAnvistFom());
        periode.setTom(anvist.getAnvistTom());
        anvisning.setPeriode(periode);
        anvist.getBeløp().map(Beløp::getVerdi).map(Desimaltall::new).ifPresent(anvisning::setBeløp);
        anvist.getDagsats().map(Beløp::getVerdi).map(Desimaltall::new).ifPresent(anvisning::setDagsats);
        anvist.getUtbetalingsgradProsent().map(Stillingsprosent::getVerdi).map(Desimaltall::new).ifPresent(anvisning::setUtbetalingsgrad);
        anvisning.setAndeler(mapAndeler(anvist));

        return anvisning;
    }

    private List<AnvistAndel> mapAndeler(YtelseAnvist anvist) {
        return anvist.getAndeler().stream().map(a -> new AnvistAndel(
            a.getArbeidsgiver().map(EksternDelingAvYtelserRestTjeneste::mapArbeidsgiver).orElse(null),
            a.getArbeidsforholdId(),
            new Desimaltall(a.getDagsats().getVerdi()),
            a.getUtbetalingsgradProsent() == null ? null : new Desimaltall(a.getUtbetalingsgradProsent().getVerdi()),
            a.getRefusjonsgradProsent() == null ? null : new Desimaltall(a.getRefusjonsgradProsent().getVerdi()),
            a.getInntektskategori()
        )).collect(Collectors.toList());
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


}
