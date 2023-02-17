package no.nav.foreldrepenger.abakus.registerdata;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseBuilder;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.registerdata.infotrygd.InfotrygdgrunnlagYtelseMapper;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.arena.MeldekortUtbetalingsgrunnlagMeldekort;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.arena.MeldekortUtbetalingsgrunnlagSak;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.dto.InfotrygdYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;

public class YtelseRegisterInnhenting {
    private final InnhentingSamletTjeneste innhentingSamletTjeneste;
    private final VedtattYtelseInnhentingTjeneste vedtattYtelseInnhentingTjeneste;

    YtelseRegisterInnhenting(InnhentingSamletTjeneste innhentingSamletTjeneste, VedtattYtelseInnhentingTjeneste vedtattYtelseInnhentingTjeneste) {
        this.innhentingSamletTjeneste = innhentingSamletTjeneste;
        this.vedtattYtelseInnhentingTjeneste = vedtattYtelseInnhentingTjeneste;
    }

    void byggYtelser(Kobling behandling,
                     AktørId aktørId,
                     PersonIdent ident,
                     IntervallEntitet opplysningsPeriode,
                     InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder,
                     boolean medGrunnlag) {

        InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder = inntektArbeidYtelseAggregatBuilder.getAktørYtelseBuilder(aktørId);
        aktørYtelseBuilder.tilbakestillYtelser();

        vedtattYtelseInnhentingTjeneste.innhentFraYtelsesRegister(aktørId, behandling, aktørYtelseBuilder);

        if (!medGrunnlag) {
            // Ikke lenger relevant å hente eksternt for 2part eller engangsstønad
            inntektArbeidYtelseAggregatBuilder.leggTilAktørYtelse(aktørYtelseBuilder);
            return;
        }

        List<InfotrygdYtelseGrunnlag> alleGrunnlag = innhentingSamletTjeneste.innhentInfotrygdGrunnlag(ident, opplysningsPeriode);
        alleGrunnlag.forEach(grunnlag -> InfotrygdgrunnlagYtelseMapper.oversettInfotrygdYtelseGrunnlagTilYtelse(aktørYtelseBuilder, grunnlag));

        List<InfotrygdYtelseGrunnlag> ghosts = innhentingSamletTjeneste.innhentSpokelseGrunnlag(ident, opplysningsPeriode);
        ghosts.forEach(grunnlag -> oversettSpokelseYtelseGrunnlagTilYtelse(aktørYtelseBuilder, grunnlag));

        List<MeldekortUtbetalingsgrunnlagSak> arena = innhentingSamletTjeneste.hentDagpengerAAP(ident, opplysningsPeriode);
        for (MeldekortUtbetalingsgrunnlagSak sak : arena) {
            oversettMeldekortUtbetalingsgrunnlagTilYtelse(aktørYtelseBuilder, sak);
        }

        inntektArbeidYtelseAggregatBuilder.leggTilAktørYtelse(aktørYtelseBuilder);
    }

    private void oversettSpokelseYtelseGrunnlagTilYtelse(InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder,
                                                         InfotrygdYtelseGrunnlag grunnlag) {
        IntervallEntitet periode = utledPeriodeNårTomMuligFørFom(grunnlag.getVedtaksPeriodeFom(), grunnlag.getVedtaksPeriodeTom());
        var saksnummer = new Saksnummer(grunnlag.getVedtaksreferanse());
        YtelseBuilder ytelseBuilder = aktørYtelseBuilder.getYtelselseBuilderForType(Fagsystem.VLSP, grunnlag.getYtelseType(), saksnummer)
            .medBehandlingsTema(grunnlag.getTemaUnderkategori())
            .medVedtattTidspunkt(grunnlag.getVedtattTidspunkt())
            .medPeriode(periode)
            .medStatus(grunnlag.getYtelseStatus());
        grunnlag.getUtbetaltePerioder().forEach(vedtak -> {
            final IntervallEntitet intervall = utledPeriodeNårTomMuligFørFom(vedtak.getUtbetaltFom(), vedtak.getUtbetaltTom());
            ytelseBuilder.leggtilYtelseAnvist(
                ytelseBuilder.getAnvistBuilder().medAnvistPeriode(intervall).medUtbetalingsgradProsent(vedtak.getUtbetalingsgrad()).build());
        });
        aktørYtelseBuilder.leggTilYtelse(ytelseBuilder);
    }

    private void oversettMeldekortUtbetalingsgrunnlagTilYtelse(InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder,
                                                               MeldekortUtbetalingsgrunnlagSak ytelse) {
        Optional<LocalDate> førsteMeldekortFom = finnFørsteMeldekortFom(ytelse);
        IntervallEntitet periode = utledMeldekortVedtaksPeriode(ytelse, førsteMeldekortFom);
        YtelseBuilder ytelseBuilder = aktørYtelseBuilder.getYtelselseBuilderForType(ytelse.getKilde(), ytelse.getYtelseType(), ytelse.getSaksnummer(),
            periode, førsteMeldekortFom);
        ytelseBuilder.medPeriode(periode)
            .medStatus(ytelse.getYtelseTilstand())
            .medVedtattTidspunkt(ytelse.getVedtattDato().atStartOfDay())
            .medYtelseGrunnlag(ytelseBuilder.getGrunnlagBuilder()
                .medOpprinneligIdentdato(ytelse.getKravMottattDato())
                .medVedtaksDagsats(ytelse.getVedtaksDagsats())
                .build());
        for (MeldekortUtbetalingsgrunnlagMeldekort meldekort : ytelse.getMeldekortene()) {
            ytelseBuilder.leggtilYtelseAnvist(ytelseBuilder.getAnvistBuilder()
                .medAnvistPeriode(utledPeriodeNårTomMuligFørFom(meldekort.getMeldekortFom(), meldekort.getMeldekortTom()))
                .medBeløp(meldekort.getBeløp())
                .medDagsats(meldekort.getDagsats())
                .medUtbetalingsgradProsent(meldekort.getUtbetalingsgrad())
                .build());
        }
        aktørYtelseBuilder.leggTilYtelse(ytelseBuilder);
    }

    private IntervallEntitet utledMeldekortVedtaksPeriode(MeldekortUtbetalingsgrunnlagSak sak, Optional<LocalDate> førsteMeldekortFom) {
        LocalDate fomFraSakMK = utledFomFraSakEllerMeldekortene(sak, førsteMeldekortFom);
        return utledPeriodeNårTomMuligFørFom(fomFraSakMK, sak.getVedtaksPeriodeTom());
    }

    private LocalDate utledFomFraSakEllerMeldekortene(MeldekortUtbetalingsgrunnlagSak sak, Optional<LocalDate> førsteMeldekortFom) {
        if (sak.getVedtaksPeriodeFom() != null) {
            return sak.getVedtaksPeriodeFom();
        }
        return førsteMeldekortFom.orElseGet(() -> sak.getVedtattDato() != null ? sak.getVedtattDato() : sak.getKravMottattDato());
    }

    private IntervallEntitet utledPeriodeNårTomMuligFørFom(LocalDate fom, LocalDate tom) {
        if (tom == null) {
            return IntervallEntitet.fraOgMed(fom);
        }
        if (tom.isBefore(fom)) {
            return IntervallEntitet.fraOgMedTilOgMed(fom, fom);
        }
        return IntervallEntitet.fraOgMedTilOgMed(fom, tom);
    }

    private Optional<LocalDate> finnFørsteMeldekortFom(MeldekortUtbetalingsgrunnlagSak sak) {
        return sak.getMeldekortene().stream().map(MeldekortUtbetalingsgrunnlagMeldekort::getMeldekortFom).min(LocalDate::compareTo);
    }

}
