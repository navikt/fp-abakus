package no.nav.foreldrepenger.abakus.vedtak.extract.v1;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.abakus.vedtak.ytelse.Periode;
import no.nav.abakus.vedtak.ytelse.v1.YtelseType;
import no.nav.abakus.vedtak.ytelse.v1.YtelseV1;
import no.nav.abakus.vedtak.ytelse.v1.anvisning.Anvisning;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseStatus;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Fagsystem;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseBuilder;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseRepository;
import no.nav.foreldrepenger.abakus.vedtak.domene.YtelseAnvistBuilder;
import no.nav.foreldrepenger.abakus.vedtak.extract.ExtractFromYtelse;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@ApplicationScoped
public class ExtractFromYtelseV1 implements ExtractFromYtelse<YtelseV1> {

    private VedtakYtelseRepository repository;

    ExtractFromYtelseV1() {
    }

    @Inject
    public ExtractFromYtelseV1(VedtakYtelseRepository repository) {
        this.repository = repository;
    }

    @Override
    public VedtakYtelseBuilder extractFrom(YtelseV1 ytelse) {
        Fagsystem fagsystem = getFagsystem(ytelse.getFagsystem());
        no.nav.foreldrepenger.abakus.kodeverk.YtelseType ytelseType = getYtelseType(ytelse.getType());
        Saksnummer saksnummer = new Saksnummer(ytelse.getSaksnummer());
        AktørId aktørId = new AktørId(ytelse.getAktør().getVerdi());

        VedtakYtelseBuilder builder = repository.opprettBuilderFor(aktørId, saksnummer, fagsystem, ytelseType);
        builder.medAktør(aktørId)
            .medVedtakReferanse(UUID.fromString(ytelse.getVedtakReferanse()))
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

    private YtelseStatus getStatus(no.nav.abakus.vedtak.ytelse.v1.YtelseStatus kodeverk) {
        return YtelseStatus.fraKode(kodeverk.getKode());
    }

    private void mapAnvisning(VedtakYtelseBuilder builder, Anvisning anv) {
        YtelseAnvistBuilder anvistBuilder = builder.getAnvistBuilder();
        anvistBuilder.medAnvistPeriode(mapTilEntitet(anv.getPeriode()))
            .medBeløp(anv.getBeløp() != null ? anv.getBeløp().getVerdi() : null)
            .medDagsats(anv.getDagsats() != null ? anv.getDagsats().getVerdi() : null)
            .medUtbetalingsgradProsent(anv.getUtbetalingsgrad() != null ? anv.getUtbetalingsgrad().getVerdi() : null);
        builder.leggTil(anvistBuilder);
    }

    private DatoIntervallEntitet mapTilEntitet(Periode periode) {
        return DatoIntervallEntitet.fraOgMedTilOgMed(periode.getFom(), periode.getTom());
    }

    private no.nav.foreldrepenger.abakus.kodeverk.YtelseType getYtelseType(YtelseType kodeverk) {
        return no.nav.foreldrepenger.abakus.kodeverk.YtelseType.fraKode(kodeverk.getKode());
    }

    private Fagsystem getFagsystem(no.nav.abakus.vedtak.ytelse.v1.Fagsystem kodeverk) {
        return Fagsystem.fraKode(kodeverk.getKode());
    }
}
