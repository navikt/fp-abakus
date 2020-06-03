package no.nav.foreldrepenger.abakus.vedtak.tjeneste;

import static no.nav.foreldrepenger.abakus.felles.sikkerhet.AbakusBeskyttetRessursAttributt.VEDTAK;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.CREATE;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
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
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import no.nav.abakus.vedtak.ytelse.Aktør;
import no.nav.abakus.vedtak.ytelse.Desimaltall;
import no.nav.abakus.vedtak.ytelse.Periode;
import no.nav.abakus.vedtak.ytelse.Ytelse;
import no.nav.abakus.vedtak.ytelse.v1.YtelseV1;
import no.nav.abakus.vedtak.ytelse.v1.anvisning.Anvisning;
import no.nav.foreldrepenger.abakus.felles.FellesRestTjeneste;
import no.nav.foreldrepenger.abakus.felles.metrikker.MetrikkerTjeneste;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.typer.Stillingsprosent;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelse;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseBuilder;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseRepository;
import no.nav.foreldrepenger.abakus.vedtak.domene.YtelseAnvist;
import no.nav.foreldrepenger.abakus.vedtak.extract.v1.ExtractFromYtelseV1;
import no.nav.vedtak.konfig.Tid;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;

@OpenAPIDefinition(tags = @Tag(name = "ytelse"))
@Path("/ytelse/v1")
@ApplicationScoped
@Transactional
public class YtelseRestTjeneste extends FellesRestTjeneste {

    private VedtakYtelseRepository ytelseRepository;
    private ExtractFromYtelseV1 extractor;

    public YtelseRestTjeneste() {} // RESTEASY ctor

    @Inject
    public YtelseRestTjeneste(VedtakYtelseRepository ytelseRepository,
                              ExtractFromYtelseV1 extractor,
                              MetrikkerTjeneste metrikkerTjeneste) {
        super(metrikkerTjeneste);
        this.ytelseRepository = ytelseRepository;
        this.extractor = extractor;
    }

    @POST
    @Path("/vedtatt")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Lagrer ytelse vedtak", tags = "ytelse")
    @BeskyttetRessurs(action = CREATE, resource = VEDTAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response lagreVedtakk(@NotNull @TilpassetAbacAttributt(supplierClass = AbacDataSupplier.class) @Valid Ytelse request) {
        var startTx = Instant.now();

        final YtelseV1 ytelseVedtak = (YtelseV1) request;
        VedtakYtelseBuilder builder = extractor.extractFrom(ytelseVedtak);

        ytelseRepository.lagre(builder);

        logMetrikk("/ytelse/v1/vedtatt", Duration.between(startTx, Instant.now()));

        getMetrikkTjeneste().logVedtakMottatRest(
                ytelseVedtak.getType().getKode(),
                ytelseVedtak.getStatus().getKode(),
                ytelseVedtak.getFagsystem().getKode());

        return Response.accepted().build();
    }

    @POST
    @Path("/vedtakene")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Henter alle vedtak for en gitt person, evt med periode etter en fom",
        tags = "ytelse")
    @BeskyttetRessurs(action = READ, resource = VEDTAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentVedtak(@NotNull @TilpassetAbacAttributt(supplierClass = AktørFomRequestAbacDataSupplier.class) @Valid AktørFomRequest request) {
        var startTx = Instant.now();

        AktørId aktørId = new AktørId(request.getAktør().getVerdi());
        LocalDate fom = request.getFomNonNull();
        LocalDate tom = Tid.TIDENES_ENDE;
        var ytelser = ytelseRepository.hentYtelserForIPeriode(aktørId, fom, tom).stream()
            .map(this::mapLagretVedtakTilYtelse)
            .collect(Collectors.toList());

        final Response response = Response.ok(ytelser).build();

        logMetrikk("/ytelse/v1/hentVedtak", Duration.between(startTx, Instant.now()));
        return response;
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
        return anvisning;
    }

    public static class AbacDataSupplier implements Function<Object, AbacDataAttributter> {
        @Override
        public AbacDataAttributter apply(Object obj) {
            Ytelse req = (Ytelse) obj;
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.AKTØR_ID, req.getAktør().getVerdi());
        }
    }

    public static class AktørFomRequestAbacDataSupplier implements Function<Object, AbacDataAttributter> {

        public AktørFomRequestAbacDataSupplier() {
        }

        @Override
        public AbacDataAttributter apply(Object obj) {
            AktørFomRequest req = (AktørFomRequest) obj;
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.AKTØR_ID, req.getAktør().getVerdi());
        }
    }
}
