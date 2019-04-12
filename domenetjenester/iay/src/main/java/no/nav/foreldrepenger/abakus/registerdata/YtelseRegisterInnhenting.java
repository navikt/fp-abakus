package no.nav.foreldrepenger.abakus.registerdata;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.threeten.extra.Interval;

import no.nav.foreldrepenger.abakus.behandling.Fagsystem;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseGrunnlag;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseGrunnlagBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseStørrelseBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.virksomhet.VirksomhetTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.arena.MeldekortUtbetalingsgrunnlagMeldekort;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.arena.MeldekortUtbetalingsgrunnlagSak;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.beregningsgrunnlag.YtelseBeregningsgrunnlagArbeidsforhold;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.beregningsgrunnlag.YtelseBeregningsgrunnlagGrunnlag;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.beregningsgrunnlag.YtelseBeregningsgrunnlagVedtak;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.sak.InfotrygdSakOgGrunnlag;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class YtelseRegisterInnhenting {
    private final VirksomhetTjeneste virksomhetTjeneste;
    private final InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    private final InnhentingSamletTjeneste innhentingSamletTjeneste;

    YtelseRegisterInnhenting(VirksomhetTjeneste virksomhetTjeneste, InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste,
                             InnhentingSamletTjeneste innhentingSamletTjeneste) {
        this.virksomhetTjeneste = virksomhetTjeneste;
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
        this.innhentingSamletTjeneste = innhentingSamletTjeneste;
    }

    InntektArbeidYtelseAggregatBuilder innhentYtelserForInvolverteParter(Kobling behandling, Interval opplysningsPeriode, boolean medGrunnlag) {
        // For Søker
        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder = inntektArbeidYtelseTjeneste.opprettBuilderForRegister(behandling.getId());
        byggYtelser(behandling, behandling.getAktørId(), opplysningsPeriode, inntektArbeidYtelseAggregatBuilder, medGrunnlag);

        // For annen forelder
        final Optional<AktørId> annenPartAktørId = behandling.getAnnenPartAktørId();
        annenPartAktørId.ifPresent(aLong -> byggYtelser(behandling, aLong, opplysningsPeriode, inntektArbeidYtelseAggregatBuilder, false));

        return inntektArbeidYtelseAggregatBuilder;
    }

    void byggYtelser(Kobling behandling, AktørId aktørId, Interval opplysningsPeriode,
                             InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder, boolean medGrunnlag) {
        List<InfotrygdSakOgGrunnlag> sammenstilt = innhentingSamletTjeneste.getSammenstiltSakOgGrunnlag(behandling, aktørId, opplysningsPeriode, medGrunnlag);

        InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder = inntektArbeidYtelseAggregatBuilder.getAktørYtelseBuilder(aktørId);
        for (InfotrygdSakOgGrunnlag ytelse : sammenstilt) {
            RelatertYtelseType type = ytelse.getGrunnlag().map(YtelseBeregningsgrunnlagGrunnlag::getType).orElse(ytelse.getSak().getRelatertYtelseType());
            if (skalKopiereTilYtelse(behandling, aktørId, type)) {
                oversettSakGrunnlagTilYtelse(aktørYtelseBuilder, ytelse);
            }
        }

        if (medGrunnlag) {
            List<MeldekortUtbetalingsgrunnlagSak> arena = innhentingSamletTjeneste.hentYtelserTjenester(behandling, aktørId, opplysningsPeriode);
            for (MeldekortUtbetalingsgrunnlagSak sak : arena) {
                oversettMeldekortUtbetalingsgrunnlagTilYtelse(aktørYtelseBuilder, sak);
            }
        }

        innhentFraYtelsesRegister(aktørId, behandling, opplysningsPeriode, aktørYtelseBuilder);

        inntektArbeidYtelseAggregatBuilder.leggTilAktørYtelse(aktørYtelseBuilder);
    }

    private void innhentFraYtelsesRegister(AktørId aktørId, Kobling behandling, Interval opplysningsPeriode, InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder builder) {

    }

    /**
     * Bestemmer hvilke {@link RelatertYtelseType} som skal kopieres inn for søker og annenpart.
     */
    private boolean skalKopiereTilYtelse(Kobling behandling, AktørId aktørId, RelatertYtelseType relatertYtelseType) {
        List<RelatertYtelseType> ytelseTyperSomErRelevantForAnnenPart = Arrays.asList(RelatertYtelseType.FORELDREPENGER, RelatertYtelseType.ENGANGSSTØNAD);
        if (aktørId.equals(behandling.getAktørId())) {
            return true;
        }
        return !aktørId.equals(behandling.getAktørId()) && ytelseTyperSomErRelevantForAnnenPart.contains(relatertYtelseType);
    }

    private void oversettSakGrunnlagTilYtelse(InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder, InfotrygdSakOgGrunnlag ytelse) {
        YtelseBuilder ytelseBuilder = aktørYtelseBuilder.getYtelselseBuilderForType(Fagsystem.INFOTRYGD, ytelse.getSak().getRelatertYtelseType(), ytelse.getSak().getTemaUnderkategori(), ytelse.getPeriode())
            .medBehandlingsTema(ytelse.getSak().getTemaUnderkategori())
            .medSaksnummer(ytelse.getSaksnummer())
            .medStatus(ytelse.getSak().getRelatertYtelseTilstand())
            .medFagsystemUnderkategori(ytelse.getSak().getFagsystemUnderkategori());
        ytelseBuilder.tilbakestillAnvisteYtelser();
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

    private YtelseGrunnlag oversettYtelseGrunnlag(YtelseBeregningsgrunnlagGrunnlag grunnlag, YtelseGrunnlagBuilder grunnlagBuilder) {
        grunnlag.mapSpesialverdier(grunnlagBuilder);
        grunnlagBuilder.medArbeidskategori(grunnlag.getArbeidskategori());
        grunnlagBuilder.tilbakestillStørrelse();
        if (grunnlag.harArbeidsForhold()) {
            leggTilArbeidsforhold(grunnlagBuilder, grunnlag.getArbeidsforhold());
        }
        return grunnlagBuilder.build();
    }

    private void oversettMeldekortUtbetalingsgrunnlagTilYtelse(InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder, MeldekortUtbetalingsgrunnlagSak ytelse) {
        DatoIntervallEntitet periode = utledMeldekortVedtaksPeriode(ytelse);
        YtelseBuilder ytelseBuilder = aktørYtelseBuilder.getYtelselseBuilderForType(ytelse.getKilde(), ytelse.getYtelseType(), ytelse.getSaksnummer(), periode);
        ytelseBuilder
            .medPeriode(periode)
            .medStatus(ytelse.getYtelseTilstand())
            .medYtelseGrunnlag(ytelseBuilder.getGrunnlagBuilder().medOpprinneligIdentdato(ytelse.getKravMottattDato()).build());
        ytelseBuilder.tilbakestillAnvisteYtelser();
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

    private DatoIntervallEntitet utledMeldekortVedtaksPeriode(MeldekortUtbetalingsgrunnlagSak sak) {
        LocalDate fomFraSakMK = utledFomFraSakEllerMeldekortene(sak);
        if (sak.getVedtaksPeriodeTom() == null) {
            return DatoIntervallEntitet.fraOgMed(fomFraSakMK);
        }
        if (sak.getVedtaksPeriodeTom().isBefore(fomFraSakMK)) {
            return DatoIntervallEntitet.fraOgMedTilOgMed(fomFraSakMK, fomFraSakMK);
        }
        return DatoIntervallEntitet.fraOgMedTilOgMed(fomFraSakMK, sak.getVedtaksPeriodeTom());
    }

    private LocalDate utledFomFraSakEllerMeldekortene(MeldekortUtbetalingsgrunnlagSak sak) {
        if (sak.getVedtaksPeriodeFom() != null) {
            return sak.getVedtaksPeriodeFom();
        }

        Optional<LocalDate> minFom = sak.getMeldekortene().stream()
            .map(MeldekortUtbetalingsgrunnlagMeldekort::getMeldekortFom)
            .min(LocalDate::compareTo);
        return minFom.orElseGet(() -> sak.getVedtattDato() != null ? sak.getVedtattDato() : sak.getKravMottattDato());
    }

    private void leggTilArbeidsforhold(YtelseGrunnlagBuilder ygBuilder, List<YtelseBeregningsgrunnlagArbeidsforhold> arbeidsforhold) {
        if (arbeidsforhold != null)
            for (YtelseBeregningsgrunnlagArbeidsforhold arbeid : arbeidsforhold) {
                final YtelseStørrelseBuilder ysBuilder = ygBuilder.getStørrelseBuilder();
                ysBuilder.medBeløp(arbeid.getInntektForPerioden())
                    .medHyppighet(arbeid.getInntektPeriodeType());
                if (arbeid.harGyldigOrgnr()) {
                    ysBuilder.medVirksomhet(virksomhetTjeneste.hentOgLagreOrganisasjon(arbeid.getOrgnr()));
                }
                ygBuilder.medYtelseStørrelse(ysBuilder.build());
            }
    }

    /*
    // TODO : Må hentes fra FPSAK elns
    private void oversettRelaterteYtelserFraVedtaksløsning(final AktørId aktørId, Kobling behandling,
                                                           Interval opplysningsPeriode, InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder,
                                                           boolean medDenneFagsaken) {
        final List<Fagsak> fagsakListe = behandlingRepositoryProvider.getFagsakRepository().hentForBruker(aktørId);

        for (Fagsak fagsak : fagsakListe) {
            if (!medDenneFagsaken && fagsak.equals(behandling.getFagsak())) {
                continue;
            }
            // TODO (DIAMANT): Avklar hvilke(n) behandling man skal ta med. For FP siste revurdering. OBS: Ikke avsluttet behandling - for 5031!
            behandlingRepositoryProvider.getBehandlingRepository().finnSisteAvsluttedeIkkeHenlagteBehandling(fagsak.getId())
                .map(Behandling::getBehandlingsresultat)
                .map(Behandlingsresultat::getBehandlingVedtak)
                .filter(behandlingVedtak -> behandlingVedtak.getVedtakResultatType().equals(VedtakResultatType.INNVILGET))
                .map(behandlingVedtak -> mapFraFagsak(fagsak, aktørYtelseBuilder, behandlingVedtak.getBehandlingsresultat().getBehandling(), opplysningsPeriode))
                .ifPresent(aktørYtelseBuilder::leggTilYtelse);
        }
    }

    private void mapFraUttakTilYtelseAnvist(Kobling behandling, YtelseBuilder ytelseBuilder) {
        Optional<UttakResultatEntitet> uttakResultat = behandlingRepositoryProvider.getUttakRepository().hentUttakResultatHvisEksisterer(behandling);
        ytelseBuilder.tilbakestillAnvisteYtelser();
        if (uttakResultat.isPresent()) {
            List<YtelseAnvistBuilder> ytelseAnvistBuilderList = uttakResultat.get().getGjeldendePerioder().getPerioder().stream()
                .filter(p -> PeriodeResultatType.INNVILGET.equals(p.getPeriodeResultatType()))
                .map(periode -> ytelseBuilder.getAnvistBuilder().medAnvistPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(periode.getFom(), periode.getTom())))
                .collect(Collectors.toList());

            for (YtelseAnvistBuilder ytelseAnvistBuilder : ytelseAnvistBuilderList) {
                ytelseBuilder.medYtelseAnvist(ytelseAnvistBuilder.build());
            }
        }
    }

    private YtelseBuilder mapFraFagsak(Fagsak fagsak, InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder,
                                       Kobling behandling, Interval periodeFraRelaterteYtelserSøkesIVL) {

        YtelseBuilder ytelseBuilder = aktørYtelseBuilder.getYtelselseBuilderForType(Fagsystem.FPSAK, map(fagsak.getYtelseType()), fagsak.getSaksnummer())
            .medStatus(map(fagsak.getStatus()));

        Optional<UttakResultatEntitet> uttakResultat = behandlingRepositoryProvider.getUttakRepository().hentUttakResultatHvisEksisterer(behandling);
        if (uttakResultat.isPresent() && hentPeriodeFraUttak(uttakResultat.get()).isPresent()) {
            ytelseBuilder.medPeriode(hentPeriodeFraUttak(uttakResultat.get()).get());
        } else {
            ytelseBuilder.medPeriode(DatoIntervallEntitet.
                fraOgMedTilOgMed(behandling.getBehandlingsresultat().getBehandlingVedtak().getVedtaksdato(), behandling.getBehandlingsresultat().getBehandlingVedtak().getVedtaksdato()));
        }
        //Sjekker om perioden for uttak faktisk er utenfor innhentingsintervalet
        if (!periodeFraRelaterteYtelserSøkesIVL.overlaps(IntervallUtil.tilIntervall(ytelseBuilder.getPeriode().getTomDato()))) {
            return null;
        }
        mapFraUttakTilYtelseAnvist(behandling, ytelseBuilder);
        if (fagsak.getYtelseType().gjelderForeldrepenger()) {
            mapFraBeregning(behandling, ytelseBuilder);
        }
        return ytelseBuilder;
    }

    private void mapFraBeregning(Kobling behandling, YtelseBuilder ytelseBuilder) {
        BeregningsgrunnlagRepository beregningsgrunnlagRepository = behandlingRepositoryProvider.getBeregningsgrunnlagRepository();
        Optional<Beregningsgrunnlag> beregningsgrunnlag = beregningsgrunnlagRepository.hentBeregningsgrunnlagForBehandling(behandling.getId());

        if (beregningsgrunnlag.isPresent() && !beregningsgrunnlag.get().getBeregningsgrunnlagPerioder().isEmpty()) {
            BeregningsgrunnlagPeriode siste = beregningsgrunnlag.get().getBeregningsgrunnlagPerioder().get(0);
            for (BeregningsgrunnlagPeriode periode : beregningsgrunnlag.get().getBeregningsgrunnlagPerioder()) {
                if (siste.getBeregningsgrunnlagPeriodeFom().isBefore(periode.getBeregningsgrunnlagPeriodeFom())) {
                    siste = periode;
                }
            }
            siste.getBeregningsgrunnlagPrStatusOgAndelList().forEach(andel -> {
                YtelseGrunnlagBuilder grunnlagBuilder = ytelseBuilder.getGrunnlagBuilder();
                ytelseBuilder.medYtelseGrunnlag(grunnlagBuilder.medDekningsgradProsent(getDekningsgrad(behandling))
                    .medYtelseStørrelse(grunnlagBuilder.getStørrelseBuilder()
                        .medBeløp(andel.getBruttoPrÅr())
                        .medVirksomhet(andel.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getVirksomhet).orElse(null))
                        .medHyppighet(InntektPeriodeType.ÅRLIG)
                        .build())
                    .build());
            });
        }
    }

    private BigDecimal getDekningsgrad(Kobling behandling) {
        Dekningsgrad dekningsgrad = behandlingRepositoryProvider.getFagsakRelasjonRepository().finnRelasjonFor(behandling.getFagsak()).getDekningsgrad();
        return new BigDecimal(dekningsgrad.getVerdi());
    }

    RelatertYtelseType map(FagsakYtelseType type) {
        if (FagsakYtelseType.ENGANGSTØNAD.equals(type)) {
            return RelatertYtelseType.ENGANGSSTØNAD;
        } else if (FagsakYtelseType.FORELDREPENGER.equals(type)) {
            return RelatertYtelseType.FORELDREPENGER;
        }
        throw new IllegalStateException("Ukjent ytelsestype " + type);
    }

    RelatertYtelseTilstand map(FagsakStatus kode) {
        RelatertYtelseTilstand typeKode;
        switch (kode.getKode()) {
            case "OPPR":
                typeKode = RelatertYtelseTilstand.IKKE_STARTET;
                break;
            case "UBEH":
                typeKode = RelatertYtelseTilstand.ÅPEN;
                break;
            case "LOP":
                typeKode = RelatertYtelseTilstand.LØPENDE;
                break;
            case "AVSLU":
                typeKode = RelatertYtelseTilstand.AVSLUTTET;
                break;
            default:
                typeKode = RelatertYtelseTilstand.ÅPEN;
        }
        return typeKode;
    }

    private Optional<DatoIntervallEntitet> hentPeriodeFraUttak(UttakResultatEntitet uttakResultatPlan) {
        Optional<UttakResultatPeriodeEntitet> sisteInnvilgetUttaksperiode = uttakResultatPlan.getGjeldendePerioder().getPerioder()
            .stream()
            .filter(p -> PeriodeResultatType.INNVILGET.equals(p.getPeriodeResultatType()))
            .max(Comparator.comparing(UttakResultatPeriodeEntitet::getTom));
        if (sisteInnvilgetUttaksperiode.isPresent()) {
            final LocalDate tom = sisteInnvilgetUttaksperiode.get().getTom();
            return Optional.of(DatoIntervallEntitet.fraOgMedTilOgMed(sisteInnvilgetUttaksperiode.get().getFom(), tom));
        }
        return Optional.empty();
    }*/
}
