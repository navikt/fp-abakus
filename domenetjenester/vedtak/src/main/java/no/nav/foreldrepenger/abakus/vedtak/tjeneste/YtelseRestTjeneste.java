package no.nav.foreldrepenger.abakus.vedtak.tjeneste;

import static no.nav.foreldrepenger.abakus.felles.sikkerhet.AbakusBeskyttetRessursAttributt.VEDTAK;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.CREATE;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;

import java.time.LocalDate;
import java.util.List;
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
import no.nav.abakus.iaygrunnlag.AktørIdPersonident;
import no.nav.abakus.iaygrunnlag.Organisasjon;
import no.nav.abakus.iaygrunnlag.request.AktørDatoRequest;
import no.nav.abakus.vedtak.ytelse.Aktør;
import no.nav.abakus.vedtak.ytelse.Desimaltall;
import no.nav.abakus.vedtak.ytelse.Periode;
import no.nav.abakus.vedtak.ytelse.Ytelse;
import no.nav.abakus.vedtak.ytelse.v1.YtelseV1;
import no.nav.abakus.vedtak.ytelse.v1.anvisning.Anvisning;
import no.nav.abakus.vedtak.ytelse.v1.anvisning.AnvistAndel;
import no.nav.foreldrepenger.abakus.felles.LoggUtil;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.typer.Stillingsprosent;
import no.nav.foreldrepenger.abakus.vedtak.domene.Arbeidsgiver;
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
public class YtelseRestTjeneste {
    private VedtakYtelseRepository ytelseRepository;
    private ExtractFromYtelseV1 extractor;

    public YtelseRestTjeneste() {} // CDI Ctor

    @Inject
    public YtelseRestTjeneste(VedtakYtelseRepository ytelseRepository,
                              ExtractFromYtelseV1 extractor) {
        this.ytelseRepository = ytelseRepository;
        this.extractor = extractor;
    }

    @POST
    @Path("/vedtatt")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Lagrer ytelse vedtak", tags = "ytelse")
    @BeskyttetRessurs(action = CREATE, resource = VEDTAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response lagreVedtak(@NotNull @TilpassetAbacAttributt(supplierClass = AbacDataSupplier.class) @Valid Ytelse request) {
        final YtelseV1 ytelseVedtak = (YtelseV1) request;
        LoggUtil.setupLogMdc(ytelseVedtak.getType(), ytelseVedtak.getSaksnummer());

        VedtakYtelseBuilder builder = extractor.extractFrom(ytelseVedtak);

        ytelseRepository.lagre(builder);

        return Response.ok().build();
    }

    @POST
    @Path("/hentVedtakForAktoer")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Henter alle vedtak for en gitt person, evt med periode etter en fom", tags = "ytelse")
    @BeskyttetRessurs(action = READ, resource = VEDTAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public List<Ytelse> hentVedtak(@NotNull @TilpassetAbacAttributt(supplierClass = AktørDatoRequestAbacDataSupplier.class) @Valid AktørDatoRequest request) {
        LoggUtil.setupLogMdc(request.getYtelse());

        AktørId aktørId = new AktørId(request.getAktør().getIdent());
        LocalDate fom = request.getDato();
        LocalDate tom = Tid.TIDENES_ENDE;
        var ytelser = ytelseRepository.hentYtelserForIPeriode(aktørId, fom, tom).stream()
            .map(this::mapLagretVedtakTilYtelse)
            .collect(Collectors.toList());

        return ytelser;
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
            a.getArbeidsgiver().map(YtelseRestTjeneste::mapArbeidsgiver).orElse(null),
            a.getArbeidsforholdId(),
            new Desimaltall(a.getDagsats().getVerdi()),
            a.getUtbetalingsgradProsent() == null ? null : new Desimaltall(a.getUtbetalingsgradProsent().getVerdi()),
            a.getRefusjonsgradProsent() == null ? null : new Desimaltall(a.getRefusjonsgradProsent().getVerdi()),
            a.getInntektskategori()
            )).collect(Collectors.toList());
    }

    private static no.nav.abakus.iaygrunnlag.Aktør mapArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        if (arbeidsgiver == null) {
            return null;
        }
        return arbeidsgiver.getOrgnr() != null ?
            new Organisasjon(arbeidsgiver.getIdentifikator()) :
            new AktørIdPersonident(arbeidsgiver.getIdentifikator());
    }

    public static class AbacDataSupplier implements Function<Object, AbacDataAttributter> {
        @Override
        public AbacDataAttributter apply(Object obj) {
            Ytelse req = (Ytelse) obj;
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.AKTØR_ID, req.getAktør().getVerdi());
        }
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


}
