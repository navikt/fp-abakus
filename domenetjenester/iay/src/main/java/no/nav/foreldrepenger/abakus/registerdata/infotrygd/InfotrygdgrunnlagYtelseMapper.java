package no.nav.foreldrepenger.abakus.registerdata.infotrygd;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import no.nav.abakus.iaygrunnlag.kodeverk.Arbeidskategori;
import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.Ytelse;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseGrunnlag;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseGrunnlagBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseStørrelseBuilder;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.dto.InfotrygdYtelseAnvist;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.dto.InfotrygdYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;
import no.nav.foreldrepenger.abakus.typer.OrganisasjonsNummerValidator;

public class InfotrygdgrunnlagYtelseMapper {

    private InfotrygdgrunnlagYtelseMapper() {
    }

    public static void oversettInfotrygdYtelseGrunnlagTilYtelse(InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder, InfotrygdYtelseGrunnlag grunnlag) {
        IntervallEntitet periode = utledPeriodeNårTomMuligFørFom(grunnlag.getVedtaksPeriodeFom(), grunnlag.getVedtaksPeriodeTom());
        var tidligsteAnvist = grunnlag.getUtbetaltePerioder().stream().map(InfotrygdYtelseAnvist::getUtbetaltFom).min(Comparator.naturalOrder());
        YtelseBuilder ytelseBuilder = aktørYtelseBuilder.getYtelselseBuilderForType(Fagsystem.INFOTRYGD, grunnlag.getYtelseType(),
                grunnlag.getTemaUnderkategori(), periode, tidligsteAnvist)
            .medBehandlingsTema(grunnlag.getTemaUnderkategori())
            .medVedtattTidspunkt(grunnlag.getVedtattTidspunkt())
            .medStatus(grunnlag.getYtelseStatus());
        var unikePerioder = grunnlag.getUtbetaltePerioder().stream()
            .map(v -> utledPeriodeNårTomMuligFørFom(v.getUtbetaltFom(), v.getUtbetaltTom()))
            .distinct()
            .sorted()
            .toList();
        unikePerioder.forEach(intervall -> {
            var overlappendeUtbetalinger = grunnlag.getUtbetaltePerioder().stream().filter(v -> utledPeriodeNårTomMuligFørFom(v.getUtbetaltFom(), v.getUtbetaltTom()).overlapper(intervall)).toList();
            var anvistBuilder = ytelseBuilder.getAnvistBuilder();
            if (skalMappeInfotrygdandeler(grunnlag)) {
                InfotrygdgrunnlagAnvistAndelMapper.oversettYtelseArbeidTilAnvisteAndeler(grunnlag.getKategori(),
                    grunnlag.getArbeidsforhold(),
                    overlappendeUtbetalinger).forEach(anvistBuilder::leggTilYtelseAnvistAndel);
            }
            ytelseBuilder.leggtilYtelseAnvist(anvistBuilder
                .medAnvistPeriode(intervall)
                .medUtbetalingsgradProsent(finnUtbetalingsgrad(overlappendeUtbetalinger))
                .build());
        });
        ytelseBuilder.medYtelseGrunnlag(oversettYtelseArbeid(grunnlag, ytelseBuilder.getGrunnlagBuilder()));
        aktørYtelseBuilder.leggTilYtelse(ytelseBuilder);
    }

    public static Ytelse oversettInfotrygdYtelseGrunnlagTilYtelse(InfotrygdYtelseGrunnlag grunnlag) {
        IntervallEntitet periode = utledPeriodeNårTomMuligFørFom(grunnlag.getVedtaksPeriodeFom(), grunnlag.getVedtaksPeriodeTom());
        var tidligsteAnvist = grunnlag.getUtbetaltePerioder().stream().map(InfotrygdYtelseAnvist::getUtbetaltFom).min(Comparator.naturalOrder());
        YtelseBuilder ytelseBuilder = InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder.oppdatere(Optional.empty()).getYtelselseBuilderForType(Fagsystem.INFOTRYGD, grunnlag.getYtelseType(),
                grunnlag.getTemaUnderkategori(), periode, tidligsteAnvist)
            .medBehandlingsTema(grunnlag.getTemaUnderkategori())
            .medVedtattTidspunkt(grunnlag.getVedtattTidspunkt())
            .medStatus(grunnlag.getYtelseStatus());
        var unikePerioder = grunnlag.getUtbetaltePerioder().stream()
            .map(v -> utledPeriodeNårTomMuligFørFom(v.getUtbetaltFom(), v.getUtbetaltTom()))
            .distinct()
            .sorted()
            .toList();
        unikePerioder.forEach(intervall -> {
            var overlappendeUtbetalinger = grunnlag.getUtbetaltePerioder().stream().filter(v -> utledPeriodeNårTomMuligFørFom(v.getUtbetaltFom(), v.getUtbetaltTom()).overlapper(intervall)).toList();
            var anvistBuilder = ytelseBuilder.getAnvistBuilder();
            if (skalMappeInfotrygdandeler(grunnlag)) {
                InfotrygdgrunnlagAnvistAndelMapper.oversettYtelseArbeidTilAnvisteAndeler(grunnlag.getKategori(),
                    grunnlag.getArbeidsforhold(),
                    overlappendeUtbetalinger).forEach(anvistBuilder::leggTilYtelseAnvistAndel);
            }
            ytelseBuilder.leggtilYtelseAnvist(anvistBuilder
                .medAnvistPeriode(intervall)
                .medUtbetalingsgradProsent(finnUtbetalingsgrad(overlappendeUtbetalinger))
                .build());
        });
        ytelseBuilder.medYtelseGrunnlag(oversettYtelseArbeid(grunnlag, ytelseBuilder.getGrunnlagBuilder()));
        return ytelseBuilder.build();
    }


    /**
     * For å kunne mappe utbetalinger fra infotrygd er vi avhengig av å få data som på korrekt måte kan brukes i automatisk saksbehandling.
     * Etter et omfattende arbeid på dette området vinteren 2022 er det konkludert med at enkelte tilfeller må ekskluderes
     * fordi datakvaliteten ikke er god nok til å kunne brukes til automatisk saksbehandling.
     * Disse tilfellene er:
     * - Vedtak der det mangler dagsats
     * - Vedtak som er basert på beregningsgrunnlag uten arbeidskategori
     *
     * @param grunnlag Beregningsgrunnlag fra infotrygd
     * @return Verdi som sier som vi skal mappe inn andeler fra grunnlaget
     */
    private static boolean skalMappeInfotrygdandeler(InfotrygdYtelseGrunnlag grunnlag) {
        var harDagsatsIListeMedUtbetalinger = grunnlag.getUtbetaltePerioder().stream().allMatch(p -> p.getDagsats() != null);
        return !grunnlag.getKategori().equals(Arbeidskategori.UGYLDIG)
            && harDagsatsIListeMedUtbetalinger;
    }

    private static BigDecimal finnUtbetalingsgrad(List<InfotrygdYtelseAnvist> anvisninger) {
        // Antar at disse anvisningene har samme utbetalingsgrad, velger tilfeldig
        return anvisninger.stream().map(InfotrygdYtelseAnvist::getUtbetalingsgrad).findFirst().orElse(BigDecimal.valueOf(100));
    }

    private static YtelseGrunnlag oversettYtelseArbeid(InfotrygdYtelseGrunnlag grunnlag, YtelseGrunnlagBuilder
        grunnlagBuilder) {
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

    private static IntervallEntitet utledPeriodeNårTomMuligFørFom(LocalDate fom, LocalDate tom) {
        if (tom == null) {
            return IntervallEntitet.fraOgMed(fom);
        }
        if (tom.isBefore(fom)) {
            return IntervallEntitet.fraOgMedTilOgMed(fom, fom);
        }
        return IntervallEntitet.fraOgMedTilOgMed(fom, tom);
    }


}
