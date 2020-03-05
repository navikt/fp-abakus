package no.nav.foreldrepenger.abakus.registerdata;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.kodemaps.YtelseTypeReverse;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.typer.Fagsystem;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;
import no.nav.foreldrepenger.abakus.typer.OrganisasjonsNummerValidator;
import no.nav.foreldrepenger.abakus.typer.Stillingsprosent;
import no.nav.foreldrepenger.abakus.vedtak.domene.TemaUnderkategori;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseRepository;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtattYtelse;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class YtelseRegisterInnhenting {
    private static final Logger LOGGER = LoggerFactory.getLogger(YtelseRegisterInnhenting.class);
    private final InnhentingSamletTjeneste innhentingSamletTjeneste;
    private final VedtakYtelseRepository vedtakYtelseRepository;

    YtelseRegisterInnhenting(InnhentingSamletTjeneste innhentingSamletTjeneste, VedtakYtelseRepository vedtakYtelseRepository) {
        this.innhentingSamletTjeneste = innhentingSamletTjeneste;
        this.vedtakYtelseRepository = vedtakYtelseRepository;
    }

    void byggYtelser(Kobling behandling, AktørId aktørId, Interval opplysningsPeriode,
                     InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder, boolean medGrunnlag) {

        InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder = inntektArbeidYtelseAggregatBuilder.getAktørYtelseBuilder(aktørId);
        ryddBortFeilaktigeInnhentedeYtelser(aktørYtelseBuilder);
        LOGGER.info("Ytelseaggregat før ytelser er lagt til : {}", aktørYtelseBuilder);

        innhentFraYtelsesRegister(aktørId, behandling, aktørYtelseBuilder);

        if (!medGrunnlag) {
            // Ikke lenger relevant å hente eksternt for 2part eller engangsstønad
            LOGGER.info("Ytelseaggregat etter at ytelser er lagt til : {}", aktørYtelseBuilder);
            inntektArbeidYtelseAggregatBuilder.leggTilAktørYtelse(aktørYtelseBuilder);
            return;
        }

        List<YtelseTypeReverse.InfotrygdYtelseGrunnlag> alleGrunnlag = innhentingSamletTjeneste.innhentInfotrygdGrunnlag(aktørId, opplysningsPeriode);
        alleGrunnlag.forEach(grunnlag -> oversettInfotrygdYtelseGrunnlagTilYtelse(aktørYtelseBuilder, grunnlag));

        List<MeldekortUtbetalingsgrunnlagSak> arena = innhentingSamletTjeneste.hentYtelserTjenester(aktørId, opplysningsPeriode);
        aktørYtelseBuilder.tilbakestillYtelserFraKildeBeholdAvsluttede(Fagsystem.ARENA);
        for (MeldekortUtbetalingsgrunnlagSak sak : arena) {
            oversettMeldekortUtbetalingsgrunnlagTilYtelse(aktørYtelseBuilder, sak);
        }

        LOGGER.info("Ytelseaggregat etter at ytelser er lagt til : {}", aktørYtelseBuilder);
        inntektArbeidYtelseAggregatBuilder.leggTilAktørYtelse(aktørYtelseBuilder);
    }

    void innhentFraYtelsesRegister(AktørId aktørId, Kobling kobling, InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder builder) {
        DatoIntervallEntitet opplysningsperiode = kobling.getOpplysningsperiode();
        List<VedtattYtelse> vedtatteYtelser = vedtakYtelseRepository.hentYtelserForIPeriode(aktørId, opplysningsperiode.getFomDato(), opplysningsperiode.getTomDato());

        for (VedtattYtelse vedtattYtelse : vedtatteYtelser) {
            YtelseBuilder ytelseBuilder = builder.getYtelselseBuilderForType(vedtattYtelse.getKilde(), vedtattYtelse.getYtelseType(), vedtattYtelse.getSaksnummer());
            ytelseBuilder.tilbakestillAnvisninger();
            ytelseBuilder.medPeriode(vedtattYtelse.getPeriode())
                .medStatus(vedtattYtelse.getStatus());

            mapAnvisninger(vedtattYtelse, ytelseBuilder);
            builder.leggTilYtelse(ytelseBuilder);
        }
    }

    private void mapAnvisninger(VedtattYtelse vedtattYtelse, YtelseBuilder ytelseBuilder) {
        vedtattYtelse.getYtelseAnvist().forEach(anvisning -> {
            YtelseAnvistBuilder anvistBuilder = ytelseBuilder.getAnvistBuilder();
            DatoIntervallEntitet periode = utledPeriodeNårTomMuligFørFom(anvisning.getAnvistFom(), anvisning.getAnvistTom());
            anvistBuilder.medAnvistPeriode(periode)
                .medBeløp(anvisning.getBeløp().map(Beløp::getVerdi).orElse(null))
                .medDagsats(anvisning.getDagsats().map(Beløp::getVerdi).orElse(null))
                .medUtbetalingsgradProsent(anvisning.getUtbetalingsgradProsent().map(Stillingsprosent::getVerdi).orElse(null));
            ytelseBuilder.leggtilYtelseAnvist(anvistBuilder.build());
        });
    }

    private void oversettInfotrygdYtelseGrunnlagTilYtelse(InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder, YtelseTypeReverse.InfotrygdYtelseGrunnlag grunnlag) {
        DatoIntervallEntitet periode = utledPeriodeNårTomMuligFørFom(grunnlag.getVedtaksPeriodeFom(), grunnlag.getVedtaksPeriodeTom());
        YtelseBuilder ytelseBuilder = aktørYtelseBuilder.getYtelselseBuilderForType(Fagsystem.INFOTRYGD, grunnlag.getYtelseType(),
            grunnlag.getTemaUnderkategori(), periode)
            .medBehandlingsTema(grunnlag.getTemaUnderkategori())
            .medStatus(grunnlag.getYtelseStatus());
        ytelseBuilder.tilbakestillAnvisninger();
        grunnlag.getUtbetaltePerioder().forEach(vedtak -> {
            final DatoIntervallEntitet intervall = utledPeriodeNårTomMuligFørFom(vedtak.getUtbetaltFom(), vedtak.getUtbetaltTom());
            ytelseBuilder.leggtilYtelseAnvist(ytelseBuilder.getAnvistBuilder()
                .medAnvistPeriode(intervall)
                .medUtbetalingsgradProsent(vedtak.getUtbetalingsgrad())
                .build());
        });
        ytelseBuilder.medYtelseGrunnlag(oversettYtelseArbeid(grunnlag, ytelseBuilder.getGrunnlagBuilder()));
        aktørYtelseBuilder.leggTilYtelse(ytelseBuilder);
    }

    private YtelseGrunnlag oversettYtelseArbeid(YtelseTypeReverse.InfotrygdYtelseGrunnlag grunnlag, YtelseGrunnlagBuilder grunnlagBuilder) {
        grunnlagBuilder.medDekningsgradProsent(grunnlag.getDekningsgrad());
        grunnlagBuilder.medGraderingProsent(grunnlag.getGradering());
        grunnlagBuilder.medOpprinneligIdentdato(grunnlag.getOpprinneligIdentdato());
        grunnlagBuilder.medArbeidskategori(grunnlag.getKategori());
        grunnlagBuilder.tilbakestillStørrelse();
        grunnlag.getArbeidsforhold().forEach(arbeid -> {
            final YtelseStørrelseBuilder ysBuilder = grunnlagBuilder.getStørrelseBuilder();
            ysBuilder.medBeløp(arbeid.getInntekt())
                .medHyppighet(arbeid.getInntektperiode());
            if (OrganisasjonsNummerValidator.erGyldig(arbeid.getOrgnr())) {
                ysBuilder.medVirksomhet(new OrgNummer(arbeid.getOrgnr()));
            }
            // Her er plass til bool refusjon
            grunnlagBuilder.medYtelseStørrelse(ysBuilder.build());
        });
        return grunnlagBuilder.build();
    }

    @Deprecated(forRemoval = true)
    private void ryddBortFeilaktigeInnhentedeYtelser(InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder) {
        aktørYtelseBuilder.tilbakestillYtelserFraKildeMedFeil(Fagsystem.INFOTRYGD, YtelseType.PÅRØRENDESYKDOM, TemaUnderkategori.UDEFINERT);
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
            ytelseBuilder.leggtilYtelseAnvist(ytelseBuilder.getAnvistBuilder()
                .medAnvistPeriode(utledPeriodeNårTomMuligFørFom(meldekort.getMeldekortFom(), meldekort.getMeldekortTom()))
                .medBeløp(meldekort.getBeløp())
                .medDagsats(meldekort.getDagsats())
                .medUtbetalingsgradProsent(meldekort.getUtbetalingsgrad())
                .build());
        }
        aktørYtelseBuilder.leggTilYtelse(ytelseBuilder);
    }

    private DatoIntervallEntitet utledMeldekortVedtaksPeriode(MeldekortUtbetalingsgrunnlagSak sak, Optional<LocalDate> førsteMeldekortFom) {
        LocalDate fomFraSakMK = utledFomFraSakEllerMeldekortene(sak, førsteMeldekortFom);
        return utledPeriodeNårTomMuligFørFom(fomFraSakMK, sak.getVedtaksPeriodeTom());
    }

    private LocalDate utledFomFraSakEllerMeldekortene(MeldekortUtbetalingsgrunnlagSak sak, Optional<LocalDate> førsteMeldekortFom) {
        if (sak.getVedtaksPeriodeFom() != null) {
            return sak.getVedtaksPeriodeFom();
        }
        return førsteMeldekortFom.orElseGet(() -> sak.getVedtattDato() != null ? sak.getVedtattDato() : sak.getKravMottattDato());
    }

    private DatoIntervallEntitet utledPeriodeNårTomMuligFørFom(LocalDate fom, LocalDate tom) {
        if (tom == null) {
            return DatoIntervallEntitet.fraOgMed(fom);
        }
        if (tom.isBefore(fom)) {
            return DatoIntervallEntitet.fraOgMedTilOgMed(fom, fom);
        }
        return DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom);
    }

    private Optional<LocalDate> finnFørsteMeldekortFom(MeldekortUtbetalingsgrunnlagSak sak) {
        return sak.getMeldekortene().stream()
            .map(MeldekortUtbetalingsgrunnlagMeldekort::getMeldekortFom)
            .min(LocalDate::compareTo);
    }

}
