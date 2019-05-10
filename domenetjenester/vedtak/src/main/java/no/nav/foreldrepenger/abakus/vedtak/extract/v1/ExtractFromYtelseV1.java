package no.nav.foreldrepenger.abakus.vedtak.extract.v1;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.kodeverk.Kodeliste;
import no.nav.foreldrepenger.abakus.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseStatus;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Fagsystem;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseBuilder;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseRepository;
import no.nav.foreldrepenger.abakus.vedtak.domene.YtelseAnvistBuilder;
import no.nav.foreldrepenger.abakus.vedtak.extract.ExtractFromYtelse;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.ytelse.Periode;
import no.nav.vedtak.ytelse.v1.YtelseType;
import no.nav.vedtak.ytelse.v1.YtelseV1;
import no.nav.vedtak.ytelse.v1.anvisning.Anvisning;

@ApplicationScoped
public class ExtractFromYtelseV1 implements ExtractFromYtelse<YtelseV1> {

    private static final Map<String, Class<? extends Kodeliste>> kodeverkTilKodeliste = Map.of(Fagsystem.DISCRIMINATOR, Fagsystem.class,
        no.nav.foreldrepenger.abakus.kodeverk.YtelseType.DISCRIMINATOR, no.nav.foreldrepenger.abakus.kodeverk.YtelseType.class,
        YtelseStatus.DISCRIMINATOR, YtelseStatus.class);

    private VedtakYtelseRepository repository;
    private KodeverkRepository kodeverkRepository;

    ExtractFromYtelseV1() {
    }

    @Inject
    public ExtractFromYtelseV1(VedtakYtelseRepository repository, KodeverkRepository kodeverkRepository) {
        this.repository = repository;
        this.kodeverkRepository = kodeverkRepository;
    }

    @Override
    public VedtakYtelseBuilder extractFrom(YtelseV1 ytelse) {
        Fagsystem fagsystem = getFagsystem(ytelse.getFagsystem());
        no.nav.foreldrepenger.abakus.kodeverk.YtelseType ytelseType = getYtelseType(ytelse.getType());
        Saksnummer saksnummer = new Saksnummer(ytelse.getSaksnummer());
        AktørId aktørId = new AktørId(ytelse.getAktør().getVerdi());

        VedtakYtelseBuilder builder = repository.opprettBuilderFor(aktørId, saksnummer, fagsystem, ytelseType);
        builder.medAktør(aktørId)
            .medVedtattTidspunkt(ytelse.getVedtattTidspunkt())
            .medSaksnummer(saksnummer)
            .medKilde(fagsystem)
            .medYtelseType(ytelseType)
            .medPeriode(mapTilEntitet(ytelse.getPeriode()))
            .medStatus(getStatus(ytelse.getStatus()))
            .tilbakestillAnvisteYtelser();

        ytelse.getAnvist().forEach(anv -> mapAnvisning(builder, anv));

        return builder;
    }

    private YtelseStatus getStatus(no.nav.vedtak.ytelse.v1.YtelseStatus kodeverk) {
        return (YtelseStatus) kodeverkRepository.finn(kodeverkTilKodeliste.get(kodeverk.getKodeverk()), kodeverk.getKode());
    }

    private void mapAnvisning(VedtakYtelseBuilder builder, Anvisning anv) {
        YtelseAnvistBuilder anvistBuilder = builder.getAnvistBuilder();
        anvistBuilder.medAnvistPeriode(mapTilEntitet(anv.getPeriode()))
            .medBeløp(anv.getBeløp().getVerdi())
            .medDagsats(anv.getDagsats().getVerdi())
            .medUtbetalingsgradProsent(anv.getUtbetalingsgrad().getVerdi());
        builder.leggTil(anvistBuilder);
    }

    private DatoIntervallEntitet mapTilEntitet(Periode periode) {
        return DatoIntervallEntitet.fraOgMedTilOgMed(periode.getFom(), periode.getTom());
    }

    private no.nav.foreldrepenger.abakus.kodeverk.YtelseType getYtelseType(YtelseType kodeverk) {
        return (no.nav.foreldrepenger.abakus.kodeverk.YtelseType) kodeverkRepository.finn(kodeverkTilKodeliste.get(kodeverk.getKodeverk()), kodeverk.getKode());
    }

    private Fagsystem getFagsystem(no.nav.vedtak.ytelse.v1.Fagsystem kodeverk) {
        return (Fagsystem) kodeverkRepository.finn(kodeverkTilKodeliste.get(kodeverk.getKodeverk()), kodeverk.getKode());
    }
}
