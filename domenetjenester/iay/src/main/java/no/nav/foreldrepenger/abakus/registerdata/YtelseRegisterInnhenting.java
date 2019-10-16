package no.nav.foreldrepenger.abakus.registerdata;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.threeten.extra.Interval;

import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseAnvistBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseGrunnlag;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseGrunnlagBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseStørrelseBuilder;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.arena.MeldekortUtbetalingsgrunnlagMeldekort;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.arena.MeldekortUtbetalingsgrunnlagSak;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.beregningsgrunnlag.YtelseBeregningsgrunnlag;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.beregningsgrunnlag.YtelseBeregningsgrunnlagArbeidsforhold;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.beregningsgrunnlag.YtelseBeregningsgrunnlagVedtak;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.sak.InfotrygdSakOgGrunnlag;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.typer.Fagsystem;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;
import no.nav.foreldrepenger.abakus.typer.Stillingsprosent;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseRepository;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtattYtelse;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class YtelseRegisterInnhenting {
    private final InnhentingSamletTjeneste innhentingSamletTjeneste;
    private final VedtakYtelseRepository vedtakYtelseRepository;

    YtelseRegisterInnhenting(InnhentingSamletTjeneste innhentingSamletTjeneste, VedtakYtelseRepository vedtakYtelseRepository) {
        this.innhentingSamletTjeneste = innhentingSamletTjeneste;
        this.vedtakYtelseRepository = vedtakYtelseRepository;
    }

    void byggYtelser(Kobling behandling, AktørId aktørId, Interval opplysningsPeriode,
                     InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder, boolean medGrunnlag) {
        List<InfotrygdSakOgGrunnlag> sammenstilt = innhentingSamletTjeneste.getSammenstiltSakOgGrunnlag(aktørId, opplysningsPeriode, medGrunnlag);

        InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder = inntektArbeidYtelseAggregatBuilder.getAktørYtelseBuilder(aktørId);
        for (InfotrygdSakOgGrunnlag ytelse : sammenstilt) {
            YtelseType type = ytelse.getGrunnlag().map(YtelseBeregningsgrunnlag::getType).orElse(ytelse.getSak().getYtelseType());
            if (skalKopiereTilYtelse(behandling, aktørId, type)) {
                oversettSakGrunnlagTilYtelse(aktørYtelseBuilder, ytelse);
            }
        }

        if (medGrunnlag) {
            List<MeldekortUtbetalingsgrunnlagSak> arena = innhentingSamletTjeneste.hentYtelserTjenester(aktørId, opplysningsPeriode);
            aktørYtelseBuilder.tilbakestillYtelserFraKildeBeholdAvsluttede(Fagsystem.ARENA);
            for (MeldekortUtbetalingsgrunnlagSak sak : arena) {
                oversettMeldekortUtbetalingsgrunnlagTilYtelse(aktørYtelseBuilder, sak);
            }
        }

        innhentFraYtelsesRegister(aktørId, behandling, aktørYtelseBuilder);

        inntektArbeidYtelseAggregatBuilder.leggTilAktørYtelse(aktørYtelseBuilder);
    }

    private void innhentFraYtelsesRegister(AktørId aktørId, Kobling kobling, InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder builder) {
        DatoIntervallEntitet opplysningsperiode = kobling.getOpplysningsperiode();
        List<VedtattYtelse> vedtatteYtelser = vedtakYtelseRepository.hentYtelserForIPeriode(aktørId, opplysningsperiode.getFomDato(), opplysningsperiode.getTomDato());

        for (VedtattYtelse vedtattYtelse : vedtatteYtelser) {
            YtelseBuilder ytelseBuilder = builder.getYtelselseBuilderForType(vedtattYtelse.getKilde(), vedtattYtelse.getYtelseType(), vedtattYtelse.getSaksnummer());
            ytelseBuilder.tilbakestillAnvisninger();
            ytelseBuilder.medPeriode(vedtattYtelse.getPeriode())
                .medStatus(vedtattYtelse.getStatus());

            mapAnvisninger(vedtattYtelse, ytelseBuilder);
        }
    }

    private void mapAnvisninger(VedtattYtelse vedtattYtelse, YtelseBuilder ytelseBuilder) {
        vedtattYtelse.getYtelseAnvist().forEach(anvisning -> {
            YtelseAnvistBuilder anvistBuilder = ytelseBuilder.getAnvistBuilder();
            DatoIntervallEntitet periode = DatoIntervallEntitet.fraOgMedTilOgMed(anvisning.getAnvistFom(), anvisning.getAnvistTom());
            anvistBuilder.medAnvistPeriode(periode)
                .medBeløp(anvisning.getBeløp().map(Beløp::getVerdi).orElse(null))
                .medDagsats(anvisning.getDagsats().map(Beløp::getVerdi).orElse(null))
                .medUtbetalingsgradProsent(anvisning.getUtbetalingsgradProsent().map(Stillingsprosent::getVerdi).orElse(null));
        });
    }

    /**
     * Bestemmer hvilke {@link YtelseType} som skal kopieres inn for søker og annenpart.
     */
    private boolean skalKopiereTilYtelse(Kobling behandling, AktørId aktørId, YtelseType relatertYtelseType) {
        if (aktørId.equals(behandling.getAktørId())) {
            return true;
        }
        return !aktørId.equals(behandling.getAktørId()) && List.of(YtelseType.FORELDREPENGER, YtelseType.ENGANGSSTØNAD).contains(relatertYtelseType);
    }

    private void oversettSakGrunnlagTilYtelse(InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder, InfotrygdSakOgGrunnlag ytelse) {
        YtelseBuilder ytelseBuilder = aktørYtelseBuilder.getYtelselseBuilderForType(Fagsystem.INFOTRYGD, ytelse.getSak().getYtelseType(),
            ytelse.getSak().getTemaUnderkategori(), ytelse.getPeriode())
            .medBehandlingsTema(ytelse.getSak().getTemaUnderkategori())
            .medStatus(ytelse.getSak().getYtelseStatus());
        ytelseBuilder.tilbakestillAnvisninger();
        ytelse.getGrunnlag().ifPresent(grunnlag -> {
            for (YtelseBeregningsgrunnlagVedtak vedtak : grunnlag.getVedtak()) {
                final DatoIntervallEntitet intervall = vedtak.getTom() == null ? DatoIntervallEntitet.fraOgMed(vedtak.getFom()) :
                    DatoIntervallEntitet.fraOgMedTilOgMed(vedtak.getFom(), vedtak.getTom());
                ytelseBuilder.medYtelseAnvist(ytelseBuilder.getAnvistBuilder()
                    .medAnvistPeriode(intervall)
                    .medUtbetalingsgradProsent(vedtak.getUtbetalingsgrad() != null ? new BigDecimal(vedtak.getUtbetalingsgrad()) : null)
                    .build());
            }
            ytelseBuilder.medYtelseGrunnlag(oversettYtelseGrunnlag(grunnlag, ytelseBuilder.getGrunnlagBuilder()));
        });
        aktørYtelseBuilder.leggTilYtelse(ytelseBuilder);
    }

    private YtelseGrunnlag oversettYtelseGrunnlag(YtelseBeregningsgrunnlag grunnlag, YtelseGrunnlagBuilder grunnlagBuilder) {
        grunnlag.mapSpesialverdier(grunnlagBuilder);
        grunnlagBuilder.medArbeidskategori(grunnlag.getArbeidskategori());
        grunnlagBuilder.tilbakestillStørrelse();
        if (grunnlag.harArbeidsForhold()) {
            leggTilArbeidsforhold(grunnlagBuilder, grunnlag.getArbeidsforhold());
        }
        return grunnlagBuilder.build();
    }

    private void oversettMeldekortUtbetalingsgrunnlagTilYtelse(InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder,
                                                               MeldekortUtbetalingsgrunnlagSak ytelse) {
        Optional<LocalDate> førsteMeldekortFom = finnFørsteMeldekortFom(ytelse);
        DatoIntervallEntitet periode = utledMeldekortVedtaksPeriode(ytelse, førsteMeldekortFom);
        YtelseBuilder ytelseBuilder = aktørYtelseBuilder.getYtelselseBuilderForType(ytelse.getKilde(), ytelse.getYtelseType(), ytelse.getSaksnummer(), periode, førsteMeldekortFom);
        ytelseBuilder
            .medPeriode(periode)
            .medStatus(ytelse.getYtelseTilstand())
            .medYtelseGrunnlag(ytelseBuilder.getGrunnlagBuilder()
                .medOpprinneligIdentdato(ytelse.getKravMottattDato())
                .medVedtaksDagsats(ytelse.getVedtaksDagsats())
                .build())
            .tilbakestillAnvisninger();
        for (MeldekortUtbetalingsgrunnlagMeldekort meldekort : ytelse.getMeldekortene()) {
            ytelseBuilder.medYtelseAnvist(ytelseBuilder.getAnvistBuilder()
                .medAnvistPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(meldekort.getMeldekortFom(), meldekort.getMeldekortTom()))
                .medBeløp(meldekort.getBeløp())
                .medDagsats(meldekort.getDagsats())
                .medUtbetalingsgradProsent(meldekort.getUtbetalingsgrad())
                .build());
        }
        aktørYtelseBuilder.leggTilYtelse(ytelseBuilder);
    }

    private DatoIntervallEntitet utledMeldekortVedtaksPeriode(MeldekortUtbetalingsgrunnlagSak sak, Optional<LocalDate> førsteMeldekortFom) {
        LocalDate fomFraSakMK = utledFomFraSakEllerMeldekortene(sak, førsteMeldekortFom);
        if (sak.getVedtaksPeriodeTom() == null) {
            return DatoIntervallEntitet.fraOgMed(fomFraSakMK);
        }
        if (sak.getVedtaksPeriodeTom().isBefore(fomFraSakMK)) {
            return DatoIntervallEntitet.fraOgMedTilOgMed(fomFraSakMK, fomFraSakMK);
        }
        return DatoIntervallEntitet.fraOgMedTilOgMed(fomFraSakMK, sak.getVedtaksPeriodeTom());
    }

    private LocalDate utledFomFraSakEllerMeldekortene(MeldekortUtbetalingsgrunnlagSak sak, Optional<LocalDate> førsteMeldekortFom) {
        if (sak.getVedtaksPeriodeFom() != null) {
            return sak.getVedtaksPeriodeFom();
        }
        return førsteMeldekortFom.orElseGet(() -> sak.getVedtattDato() != null ? sak.getVedtattDato() : sak.getKravMottattDato());
    }

    private Optional<LocalDate> finnFørsteMeldekortFom(MeldekortUtbetalingsgrunnlagSak sak) {
        return sak.getMeldekortene().stream()
            .map(MeldekortUtbetalingsgrunnlagMeldekort::getMeldekortFom)
            .min(LocalDate::compareTo);
    }

    private void leggTilArbeidsforhold(YtelseGrunnlagBuilder ygBuilder, List<YtelseBeregningsgrunnlagArbeidsforhold> arbeidsforhold) {
        if (arbeidsforhold != null)
            for (YtelseBeregningsgrunnlagArbeidsforhold arbeid : arbeidsforhold) {
                final YtelseStørrelseBuilder ysBuilder = ygBuilder.getStørrelseBuilder();
                ysBuilder.medBeløp(arbeid.getInntektForPerioden())
                    .medHyppighet(arbeid.getInntektPeriodeType());
                if (arbeid.harGyldigOrgnr()) {
                    ysBuilder.medVirksomhet(new OrgNummer(arbeid.getOrgnr()));
                }
                ygBuilder.medYtelseStørrelse(ysBuilder.build());
            }
    }
}
