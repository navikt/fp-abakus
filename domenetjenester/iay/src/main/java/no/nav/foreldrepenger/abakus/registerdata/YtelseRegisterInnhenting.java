package no.nav.foreldrepenger.abakus.registerdata;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseAnvistBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseGrunnlag;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseGrunnlagBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseStørrelseBuilder;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.arena.MeldekortUtbetalingsgrunnlagMeldekort;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.arena.MeldekortUtbetalingsgrunnlagSak;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.dto.InfotrygdYtelseAnvist;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.dto.InfotrygdYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;
import no.nav.foreldrepenger.abakus.typer.OrganisasjonsNummerValidator;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.foreldrepenger.abakus.typer.Stillingsprosent;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelse;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseRepository;

public class YtelseRegisterInnhenting {
    private final InnhentingSamletTjeneste innhentingSamletTjeneste;
    private final VedtakYtelseRepository vedtakYtelseRepository;

    YtelseRegisterInnhenting(InnhentingSamletTjeneste innhentingSamletTjeneste, VedtakYtelseRepository vedtakYtelseRepository) {
        this.innhentingSamletTjeneste = innhentingSamletTjeneste;
        this.vedtakYtelseRepository = vedtakYtelseRepository;
    }

    void byggYtelser(Kobling behandling, AktørId aktørId, PersonIdent ident, IntervallEntitet opplysningsPeriode,
                     InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder, boolean medGrunnlag) {

        InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder = inntektArbeidYtelseAggregatBuilder.getAktørYtelseBuilder(aktørId);
        aktørYtelseBuilder.tilbakestillYtelser();

        innhentFraYtelsesRegister(aktørId, behandling, aktørYtelseBuilder);

        if (!medGrunnlag) {
            // Ikke lenger relevant å hente eksternt for 2part eller engangsstønad
            inntektArbeidYtelseAggregatBuilder.leggTilAktørYtelse(aktørYtelseBuilder);
            return;
        }

        List<InfotrygdYtelseGrunnlag> alleGrunnlag = innhentingSamletTjeneste.innhentInfotrygdGrunnlag(ident, opplysningsPeriode);
        alleGrunnlag.forEach(grunnlag -> oversettInfotrygdYtelseGrunnlagTilYtelse(aktørYtelseBuilder, grunnlag));

        List<InfotrygdYtelseGrunnlag> ghosts = innhentingSamletTjeneste.innhentSpokelseGrunnlag(ident, opplysningsPeriode);
        ghosts.forEach(grunnlag -> oversettSpokelseYtelseGrunnlagTilYtelse(aktørYtelseBuilder, grunnlag));

        List<MeldekortUtbetalingsgrunnlagSak> arena = innhentingSamletTjeneste.hentDagpengerAAP(ident, opplysningsPeriode);
        for (MeldekortUtbetalingsgrunnlagSak sak : arena) {
            oversettMeldekortUtbetalingsgrunnlagTilYtelse(aktørYtelseBuilder, sak);
        }

        inntektArbeidYtelseAggregatBuilder.leggTilAktørYtelse(aktørYtelseBuilder);
    }

    void innhentFraYtelsesRegister(AktørId aktørId, Kobling kobling, InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder builder) {
        IntervallEntitet opplysningsperiode = kobling.getOpplysningsperiode();
        List<VedtakYtelse> vedtatteYtelser = vedtakYtelseRepository.hentYtelserForIPeriode(aktørId, opplysningsperiode.getFomDato(), opplysningsperiode.getTomDato());

        for (var vedtattYtelse : vedtatteYtelser) {
            YtelseBuilder ytelseBuilder = builder.getYtelselseBuilderForType(vedtattYtelse.getKilde(), vedtattYtelse.getYtelseType(), vedtattYtelse.getSaksnummer());
            ytelseBuilder.medPeriode(vedtattYtelse.getPeriode())
                .medStatus(vedtattYtelse.getStatus())
                .medVedtattTidspunkt(vedtattYtelse.getVedtattTidspunkt());

            mapAnvisninger(vedtattYtelse, ytelseBuilder);
            builder.leggTilYtelse(ytelseBuilder);
        }
    }

    private void mapAnvisninger(VedtakYtelse vedtattYtelse, YtelseBuilder ytelseBuilder) {
        vedtattYtelse.getYtelseAnvist().forEach(anvisning -> {
            YtelseAnvistBuilder anvistBuilder = ytelseBuilder.getAnvistBuilder();
            IntervallEntitet periode = utledPeriodeNårTomMuligFørFom(anvisning.getAnvistFom(), anvisning.getAnvistTom());
            anvistBuilder.medAnvistPeriode(periode)
                .medBeløp(anvisning.getBeløp().map(Beløp::getVerdi).orElse(null))
                .medDagsats(anvisning.getDagsats().map(Beløp::getVerdi).orElse(null))
                .medUtbetalingsgradProsent(anvisning.getUtbetalingsgradProsent().map(Stillingsprosent::getVerdi).orElse(null));
            ytelseBuilder.leggtilYtelseAnvist(anvistBuilder.build());
        });
    }

    private void oversettInfotrygdYtelseGrunnlagTilYtelse(InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder, InfotrygdYtelseGrunnlag grunnlag) {
        IntervallEntitet periode = utledPeriodeNårTomMuligFørFom(grunnlag.getVedtaksPeriodeFom(), grunnlag.getVedtaksPeriodeTom());
        var tidligsteAnvist = grunnlag.getUtbetaltePerioder().stream().map(InfotrygdYtelseAnvist::getUtbetaltFom).min(Comparator.naturalOrder());
        YtelseBuilder ytelseBuilder = aktørYtelseBuilder.getYtelselseBuilderForType(Fagsystem.INFOTRYGD, grunnlag.getYtelseType(),
            grunnlag.getTemaUnderkategori(), periode, tidligsteAnvist)
            .medBehandlingsTema(grunnlag.getTemaUnderkategori())
            .medVedtattTidspunkt(grunnlag.getVedtattTidspunkt())
            .medStatus(grunnlag.getYtelseStatus());
        grunnlag.getUtbetaltePerioder().forEach(vedtak -> {
            final IntervallEntitet intervall = utledPeriodeNårTomMuligFørFom(vedtak.getUtbetaltFom(), vedtak.getUtbetaltTom());
            ytelseBuilder.leggtilYtelseAnvist(ytelseBuilder.getAnvistBuilder()
                .medAnvistPeriode(intervall)
                .medUtbetalingsgradProsent(vedtak.getUtbetalingsgrad())
                .build());
        });
        ytelseBuilder.medYtelseGrunnlag(oversettYtelseArbeid(grunnlag, ytelseBuilder.getGrunnlagBuilder()));
        aktørYtelseBuilder.leggTilYtelse(ytelseBuilder);
    }

    private void oversettSpokelseYtelseGrunnlagTilYtelse(InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder, InfotrygdYtelseGrunnlag grunnlag) {
        IntervallEntitet periode = utledPeriodeNårTomMuligFørFom(grunnlag.getVedtaksPeriodeFom(), grunnlag.getVedtaksPeriodeTom());
        var saksnummer = new Saksnummer(grunnlag.getVedtaksreferanse());
        YtelseBuilder ytelseBuilder = aktørYtelseBuilder.getYtelselseBuilderForType(Fagsystem.VLSP, grunnlag.getYtelseType(), saksnummer)
            .medBehandlingsTema(grunnlag.getTemaUnderkategori())
            .medVedtattTidspunkt(grunnlag.getVedtattTidspunkt())
            .medPeriode(periode)
            .medStatus(grunnlag.getYtelseStatus());
        grunnlag.getUtbetaltePerioder().forEach(vedtak -> {
            final IntervallEntitet intervall = utledPeriodeNårTomMuligFørFom(vedtak.getUtbetaltFom(), vedtak.getUtbetaltTom());
            ytelseBuilder.leggtilYtelseAnvist(ytelseBuilder.getAnvistBuilder()
                .medAnvistPeriode(intervall)
                .medUtbetalingsgradProsent(vedtak.getUtbetalingsgrad())
                .build());
        });
        aktørYtelseBuilder.leggTilYtelse(ytelseBuilder);
    }

    private YtelseGrunnlag oversettYtelseArbeid(InfotrygdYtelseGrunnlag grunnlag, YtelseGrunnlagBuilder grunnlagBuilder) {
        grunnlagBuilder.medDekningsgradProsent(grunnlag.getDekningsgrad());
        grunnlagBuilder.medGraderingProsent(grunnlag.getGradering());
        grunnlagBuilder.medOpprinneligIdentdato(grunnlag.getOpprinneligIdentdato());
        grunnlagBuilder.medArbeidskategori(grunnlag.getKategori());
        grunnlagBuilder.tilbakestillStørrelse();
        grunnlag.getArbeidsforhold().forEach(arbeid -> {
            final YtelseStørrelseBuilder ysBuilder = grunnlagBuilder.getStørrelseBuilder();
            ysBuilder.medBeløp(arbeid.getInntekt())
                .medHyppighet(arbeid.getInntektperiode())
                .medErRefusjon(arbeid.getRefusjon());
            if (OrganisasjonsNummerValidator.erGyldig(arbeid.getOrgnr())) {
                ysBuilder.medVirksomhet(new OrgNummer(arbeid.getOrgnr()));
            }
            // Her er plass til bool refusjon
            grunnlagBuilder.medYtelseStørrelse(ysBuilder.build());
        });
        return grunnlagBuilder.build();
    }

    private void oversettMeldekortUtbetalingsgrunnlagTilYtelse(InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder,
                                                               MeldekortUtbetalingsgrunnlagSak ytelse) {
        Optional<LocalDate> førsteMeldekortFom = finnFørsteMeldekortFom(ytelse);
        IntervallEntitet periode = utledMeldekortVedtaksPeriode(ytelse, førsteMeldekortFom);
        YtelseBuilder ytelseBuilder = aktørYtelseBuilder.getYtelselseBuilderForType(ytelse.getKilde(), ytelse.getYtelseType(), ytelse.getSaksnummer(), periode, førsteMeldekortFom);
        ytelseBuilder
            .medPeriode(periode)
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
        return sak.getMeldekortene().stream()
            .map(MeldekortUtbetalingsgrunnlagMeldekort::getMeldekortFom)
            .min(LocalDate::compareTo);
    }

}
