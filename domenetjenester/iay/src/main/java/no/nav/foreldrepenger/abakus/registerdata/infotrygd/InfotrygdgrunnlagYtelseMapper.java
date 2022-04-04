package no.nav.foreldrepenger.abakus.registerdata.infotrygd;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.abakus.iaygrunnlag.kodeverk.Arbeidskategori;
import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.abakus.iaygrunnlag.kodeverk.Inntektskategori;
import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseAnvistAndel;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseAnvistAndelBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseGrunnlag;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseGrunnlagBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseStørrelseBuilder;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.dto.InfotrygdYtelseAnvist;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.dto.InfotrygdYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;
import no.nav.foreldrepenger.abakus.typer.OrganisasjonsNummerValidator;
import no.nav.foreldrepenger.konfig.Environment;

public class InfotrygdgrunnlagYtelseMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfotrygdgrunnlagYtelseMapper.class);

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
                oversettYtelseArbeidTilAnvisteAndeler(grunnlag.getKategori(), overlappendeUtbetalinger).forEach(anvistBuilder::leggTilYtelseAnvistAndel);
            }
            ytelseBuilder.leggtilYtelseAnvist(anvistBuilder
                .medAnvistPeriode(intervall)
                .medUtbetalingsgradProsent(finnUtbetalingsgrad(overlappendeUtbetalinger))
                .build());
        });
        ytelseBuilder.medYtelseGrunnlag(oversettYtelseArbeid(grunnlag, ytelseBuilder.getGrunnlagBuilder()));
        aktørYtelseBuilder.leggTilYtelse(ytelseBuilder);
    }


    /**
     * Mapper ikke grunnlag uten arbeidskategori (UGYLDIG)
     *
     * @param grunnlag Beregningsgrunnlag fra infotrygd
     * @return Verdi som sier som vi skal mappe inn andeler fra grunnlaget
     */
    private static boolean skalMappeInfotrygdandeler(InfotrygdYtelseGrunnlag grunnlag) {
        var erToggletPå = !Environment.current().isProd();
        var harDagsatsIListeMedUtbetalinger = grunnlag.getUtbetaltePerioder().stream().allMatch(p -> p.getDagsats() != null);
        return erToggletPå && !grunnlag.getKategori().equals(Arbeidskategori.UGYLDIG) && harDagsatsIListeMedUtbetalinger;
    }

    /**
     * Mapper utbetalinger fra infotrygd til anviste andeler
     * <p>
     *
     * @param kategori     Arbeidskategori fra infotrygd
     * @param utbetalinger Vedtak/anvisning for periode
     * @return Liste med andeler
     */
    private static List<YtelseAnvistAndel> oversettYtelseArbeidTilAnvisteAndeler(Arbeidskategori kategori,
                                                                                 List<InfotrygdYtelseAnvist> utbetalinger) {
        var inntektskategorier = splittArbeidskategoriTilInntektskategorier(kategori);
        if (inntektskategorier.isEmpty()) {
            LOGGER.info("Kunne ikke mappe inntektskategori fra infotrygdgrunnlag. Mapper ingen andeler for anvisning.");
            return Collections.emptyList();
        }
        var andelBuildere = new ArrayList<>(finnArbeidstakerAndeler(utbetalinger));
        finnIkkeArbeidstakerAndel(utbetalinger, inntektskategorier).ifPresent(andelBuildere::add);
        finnYtelseRapportertPåNødnummer(utbetalinger, inntektskategorier).ifPresent(andelBuildere::add);
        return andelBuildere;

    }

    private static Optional<YtelseAnvistAndel> finnIkkeArbeidstakerAndel(List<InfotrygdYtelseAnvist> utbetalinger, Set<Inntektskategori> inntektskategorier) {
        var ikkeArbeidstakerKategori = inntektskategorier.stream().filter(a -> !a.equals(Inntektskategori.ARBEIDSTAKER)).findFirst();
        var utbetalingerUtenOrgnr = utbetalinger.stream().filter(arb -> !OrganisasjonsNummerValidator.erGyldig(arb.getOrgnr())).toList();
        return ikkeArbeidstakerKategori.map(i -> YtelseAnvistAndelBuilder.ny()
            .medUtbetalingsgrad(finnUtbetalingsgrad(utbetalingerUtenOrgnr))
            .medInntektskategori(i)
            .medRefusjonsgrad(BigDecimal.ZERO)
            .medDagsats(finnDagsatsIkkeArbeidstaker(utbetalingerUtenOrgnr))
            .build()
        );
    }

    /**
     * Nødnummer brukes i infotrygd til å legge betalinger som skal gå direkte til bruker for et arbeidsforhold som det ikke er søkt refusjon for
     * <p>
     * <p>
     * Dette brukes i situasjoner der det er kombinasjon med andre statuser.
     *
     * @param anvisninger        Infotrygdandeler
     * @param inntektskategorier Inntektskategorier på grunnlaget
     * @return Ytelseandel fra nødnummer dersom den finnes
     */
    private static Optional<YtelseAnvistAndel> finnYtelseRapportertPåNødnummer(List<InfotrygdYtelseAnvist> anvisninger,
                                                                               Set<Inntektskategori> inntektskategorier) {
        var anvisningerPåNødnummer = anvisninger.stream()
            .filter(arb -> arb.getOrgnr() != null && Arrays.stream(Nødnummer.values()).anyMatch(n -> n.getOrgnummer().equals(arb.getOrgnr())))
            .toList();
        var rapportertPåNødnummmer = anvisningerPåNødnummer.stream()
            .map(InfotrygdYtelseAnvist::getDagsats)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);

        if (rapportertPåNødnummmer.compareTo(BigDecimal.ZERO) > 0) {
            var nødnummerYtelse = YtelseAnvistAndelBuilder.ny()
                .medDagsats(rapportertPåNødnummmer)
                .medRefusjonsgrad(BigDecimal.ZERO)
                .medUtbetalingsgrad(finnUtbetalingsgrad(anvisningerPåNødnummer));
            var erArbeidstaker = inntektskategorier.stream().anyMatch(i -> i.equals(Inntektskategori.ARBEIDSTAKER));
            if (erArbeidstaker) {
                nødnummerYtelse.medInntektskategori(Inntektskategori.ARBEIDSTAKER);
            } else {
                inntektskategorier.stream().filter(i -> !i.equals(Inntektskategori.ARBEIDSTAKER)).findFirst().ifPresent(nødnummerYtelse::medInntektskategori);
            }
            return Optional.of(nødnummerYtelse.build());
        }
        return Optional.empty();
    }

    private static BigDecimal finnDagsatsIkkeArbeidstaker(List<InfotrygdYtelseAnvist> utbetalinger) {
        return utbetalinger.stream()
            .map(InfotrygdYtelseAnvist::getDagsats)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);
    }

    private static List<YtelseAnvistAndel> finnArbeidstakerAndeler(List<InfotrygdYtelseAnvist> utbetalinger) {
        var gruppertPrOrg = utbetalinger.stream()
            .filter(a -> a.getOrgnr() != null)
            .filter(a -> OrganisasjonsNummerValidator.erGyldig(a.getOrgnr()))
            .collect(Collectors.groupingBy(InfotrygdYtelseAnvist::getOrgnr));

        return gruppertPrOrg.entrySet().stream()
            .filter(e -> e.getValue().stream().map(InfotrygdYtelseAnvist::getDagsats).reduce(BigDecimal::add).orElse(BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0)
            .map(e -> mapGrunnlagsandelerTilAnvistandel(new OrgNummer(e.getKey()), e.getValue())).toList();
    }

    private static YtelseAnvistAndel mapGrunnlagsandelerTilAnvistandel(OrgNummer orgnummer,
                                                                       List<InfotrygdYtelseAnvist> anvisninger) {

        var refusjon = anvisninger.stream().filter(InfotrygdYtelseAnvist::getErRefusjon)
            .map(InfotrygdYtelseAnvist::getDagsats)
            .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        var direkteUtbetaling = anvisninger.stream().filter(a -> !a.getErRefusjon()).map(InfotrygdYtelseAnvist::getDagsats).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        var total = refusjon.add(direkteUtbetaling);
        var refusjonsgrad = refusjon.divide(total, 10, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        var utbetalingsgrad = finnUtbetalingsgrad(anvisninger);
        return YtelseAnvistAndelBuilder.ny()
            .medDagsats(total)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medArbeidsgiver(Arbeidsgiver.virksomhet(orgnummer))
            .medRefusjonsgrad(refusjonsgrad)
            .medUtbetalingsgrad(utbetalingsgrad)
            .build();
    }

    private static BigDecimal finnUtbetalingsgrad(List<InfotrygdYtelseAnvist> anvisninger) {
        // Antar at disse anvisningene har samme utbetalingsgrad, velger tilfeldig
        return anvisninger.stream().map(InfotrygdYtelseAnvist::getUtbetalingsgrad).findFirst().orElse(BigDecimal.valueOf(100));
    }

    /**
     * Splitter kombinasjonsstatuser fra infotrygd og mapper til inntektskategori
     *
     * @param kategori Arbeidskategori fra infotrygd
     * @return Set av Inntektskategori
     */
    private static Set<Inntektskategori> splittArbeidskategoriTilInntektskategorier(Arbeidskategori kategori) {
        return switch (kategori) {
            case FISKER -> Set.of(Inntektskategori.FISKER);
            case ARBEIDSTAKER -> Set.of(Inntektskategori.ARBEIDSTAKER);
            case SELVSTENDIG_NÆRINGSDRIVENDE -> Set.of(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE);
            case KOMBINASJON_ARBEIDSTAKER_OG_SELVSTENDIG_NÆRINGSDRIVENDE -> Set.of(Inntektskategori.ARBEIDSTAKER, Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE);
            case SJØMANN -> Set.of(Inntektskategori.SJØMANN);
            case JORDBRUKER -> Set.of(Inntektskategori.JORDBRUKER);
            case DAGPENGER -> Set.of(Inntektskategori.DAGPENGER);
            case INAKTIV -> Set.of(Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER);
            case KOMBINASJON_ARBEIDSTAKER_OG_JORDBRUKER -> Set.of(Inntektskategori.ARBEIDSTAKER, Inntektskategori.JORDBRUKER);
            case KOMBINASJON_ARBEIDSTAKER_OG_FISKER -> Set.of(Inntektskategori.ARBEIDSTAKER, Inntektskategori.FISKER);
            case FRILANSER -> Set.of(Inntektskategori.FRILANSER);
            case KOMBINASJON_ARBEIDSTAKER_OG_FRILANSER -> Set.of(Inntektskategori.ARBEIDSTAKER, Inntektskategori.FRILANSER);
            case KOMBINASJON_ARBEIDSTAKER_OG_DAGPENGER -> Set.of(Inntektskategori.ARBEIDSTAKER, Inntektskategori.DAGPENGER);
            case DAGMAMMA -> Set.of(Inntektskategori.DAGMAMMA);
            default -> Set.of();
        };
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
