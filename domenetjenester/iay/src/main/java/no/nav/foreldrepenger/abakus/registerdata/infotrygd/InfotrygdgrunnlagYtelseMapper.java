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
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.dto.InfotrygdYtelseArbeid;
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
        grunnlag.getUtbetaltePerioder().forEach(vedtak -> {
            final IntervallEntitet intervall = utledPeriodeNårTomMuligFørFom(vedtak.getUtbetaltFom(), vedtak.getUtbetaltTom());
            var anvistBuilder = ytelseBuilder.getAnvistBuilder();
            if (skalMappeInfotrygdandeler(grunnlag)) {
                oversettYtelseArbeidTilAnvisteAndeler(
                    grunnlag.getArbeidsforhold(),
                    grunnlag.getKategori(),
                    intervall,
                    vedtak.getUtbetalingsgrad()).forEach(anvistBuilder::leggTilYtelseAnvistAndel);
            }
            ytelseBuilder.leggtilYtelseAnvist(anvistBuilder
                .medAnvistPeriode(intervall)
                .medUtbetalingsgradProsent(vedtak.getUtbetalingsgrad())
                .build());
        });
        ytelseBuilder.medYtelseGrunnlag(oversettYtelseArbeid(grunnlag, ytelseBuilder.getGrunnlagBuilder()));
        aktørYtelseBuilder.leggTilYtelse(ytelseBuilder);
    }


    /**
     * Næring og frilans uten forsikring har spesielle regler som gir reduksjon i netto dagsats som ikke reflekteres i utbetalingsgraden (se f.t.l. §§ 8-36 og 8-39)
     * <p>
     * Vi klarer ikke å representere dagsats nøyaktig for disse andelene
     *
     * @param grunnlag Beregningsgrunnlag fra infotrygd
     * @return Verdi som sier som vi skal mappe inn andeler fra grunnlaget
     */
    private static boolean skalMappeInfotrygdandeler(InfotrygdYtelseGrunnlag grunnlag) {
        var erToggletPå = !Environment.current().isProd();
        return erToggletPå && !grunnlag.isNæringEllerFrilansUtenForsikring() && !grunnlag.getKategori().equals(Arbeidskategori.UGYLDIG);
    }

    /** Mapper beregningsgrunnlagsandeler fra infotrygd til netto dagsats
     *
     * Netto dagsats = grunnlagsdasats * utbetalingsgrad
     *
     * @param arbeidsforhold Alle grunnlagsandeler fra infotrygd
     * @param kategori Arbeidskategori fra infotrygd
     * @param periode Periode for anvisning
     * @param utbetalingsgrad Utbetalingsgrad for periode
     * @return Liste med andeler
     */
    private static List<YtelseAnvistAndel> oversettYtelseArbeidTilAnvisteAndeler(List<InfotrygdYtelseArbeid> arbeidsforhold, Arbeidskategori kategori, IntervallEntitet periode, BigDecimal utbetalingsgrad) {
        var inntektskategorier = splittArbeidskategoriTilInntektskategorier(kategori);
        if (inntektskategorier.isEmpty()) {
            LOGGER.info("Kunne ikke mappe inntektskategori fra infotrygdgrunnlag. Mapper ingen andeler for anvisning.");
            return Collections.emptyList();
        }
        var andelBuildere = new ArrayList<>(finnArbeidstakerAndeler(arbeidsforhold, utbetalingsgrad, periode));
        finnIkkeArbeidstakerAndel(arbeidsforhold, utbetalingsgrad, inntektskategorier, kategori).ifPresent(andelBuildere::add);
        finnYtelseRapportertPåNødnummer(arbeidsforhold, utbetalingsgrad, inntektskategorier).ifPresent(andelBuildere::add);
        return andelBuildere;

    }

    private static Optional<YtelseAnvistAndel> finnIkkeArbeidstakerAndel(List<InfotrygdYtelseArbeid> arbeidsforhold, BigDecimal utbetalingsgrad, Set<Inntektskategori> inntektskategorier, Arbeidskategori kategori) {
        var ikkeArbeidstakerKategori = inntektskategorier.stream().filter(a -> !a.equals(Inntektskategori.ARBEIDSTAKER)).findFirst();
        return ikkeArbeidstakerKategori.map(i -> YtelseAnvistAndelBuilder.ny()
            .medUtbetalingsgrad(utbetalingsgrad)
            .medInntektskategori(i)
            .medRefusjonsgrad(BigDecimal.ZERO)
            .medDagsats(finnDagsatsIkkeArbeidstaker(arbeidsforhold, utbetalingsgrad, kategori))
            .build()
        );
    }

    /**
     * Nødnummer brukes i infotrygd til å legge betalinger som skal gå direkte til bruker for et arbeidsforhold som det ikke er søkt refusjon for
     *
     *
     * Dette brukes i situasjoner der det er kombinasjon med andre statuser.
     *
     * @param arbeidsforhold Infotrygdandeler
     * @param utbetalingsgrad Utbetalingsgrad
     * @param inntektskategorier Inntektskategorier på grunnlaget
     * @return Ytelseandel fra nødnummer dersom den finnes
     */
    private static Optional<YtelseAnvistAndel> finnYtelseRapportertPåNødnummer(List<InfotrygdYtelseArbeid> arbeidsforhold,
                                                                               BigDecimal utbetalingsgrad,
                                                                               Set<Inntektskategori> inntektskategorier) {
        var rapportertPåNødnummmer = arbeidsforhold.stream()
            .filter(arb -> arb.getOrgnr() != null && Arrays.stream(Nødnummer.values()).anyMatch(n -> n.getOrgnummer().equals(arb.getOrgnr())))
            .map(InfotrygdgrunnlagYtelseMapper::mapTilDagsats)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO)
            .multiply(utbetalingsgrad).divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);

        if (rapportertPåNødnummmer.compareTo(BigDecimal.ZERO) > 0) {
            var nødnummerYtelse = YtelseAnvistAndelBuilder.ny()
                .medDagsats(rapportertPåNødnummmer)
                .medRefusjonsgrad(BigDecimal.ZERO)
                .medUtbetalingsgrad(utbetalingsgrad);
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

    private static BigDecimal finnDagsatsIkkeArbeidstaker(List<InfotrygdYtelseArbeid> arbeidsforhold, BigDecimal utbetalingsgrad, Arbeidskategori kategori) {
        var erInaktiv = kategori.equals(Arbeidskategori.INAKTIV);
        return arbeidsforhold.stream().filter(arb -> !OrganisasjonsNummerValidator.erGyldig(arb.getOrgnr()))
            .map(InfotrygdgrunnlagYtelseMapper::mapTilDagsats)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO)
            .multiply(utbetalingsgrad)
            // Reduserer til 65% av grunnlaget dersom inaktiv
            .multiply(erInaktiv ? BigDecimal.valueOf(0.65) : BigDecimal.ONE)
            .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
    }

    private static List<YtelseAnvistAndel> finnArbeidstakerAndeler(List<InfotrygdYtelseArbeid> arbeidsforhold, BigDecimal utbetalingsgrad, IntervallEntitet periode) {
        var gruppertPrOrg = arbeidsforhold.stream()
            .filter(a -> a.getOrgnr() != null)
            .filter(a -> OrganisasjonsNummerValidator.erGyldig(a.getOrgnr()))
            .collect(Collectors.groupingBy(InfotrygdYtelseArbeid::getOrgnr));

        return gruppertPrOrg.entrySet().stream()
            .filter(e -> e.getValue().stream().map(InfotrygdYtelseArbeid::getInntekt).reduce(BigDecimal::add).orElse(BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0)
            .map(e -> mapGrunnlagsandelerTilAnvistandel(utbetalingsgrad, periode, new OrgNummer(e.getKey()), e.getValue())).toList();
    }

    private static YtelseAnvistAndel mapGrunnlagsandelerTilAnvistandel(BigDecimal utbetalingsgrad,
                                                                       IntervallEntitet periode,
                                                                       OrgNummer orgnummer,
                                                                       List<InfotrygdYtelseArbeid> grunnlagsandeler) {
        var refusjonTom = grunnlagsandeler.stream().flatMap(it -> it.getRefusjonTom().stream()).findFirst();
        refusjonTom.ifPresent(it -> validerRefusjonTom(periode, it));
        var refusjon = grunnlagsandeler.stream().filter(InfotrygdYtelseArbeid::getRefusjon)
            .map(InfotrygdgrunnlagYtelseMapper::mapTilDagsats).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        var direkteUtbetaling = grunnlagsandeler.stream().filter(a -> !a.getRefusjon()).map(InfotrygdgrunnlagYtelseMapper::mapTilDagsats).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        var total = refusjon.add(direkteUtbetaling);
        var refusjonsgrad = refusjon.divide(total, 10, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        // Mapper til netto dagsats for å være konskvent på tvers av kilder (fp-sak, k9-sak, infotrygd)
        var nettoDagsats = total.multiply(utbetalingsgrad).divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        return YtelseAnvistAndelBuilder.ny()
            .medDagsats(nettoDagsats)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medArbeidsgiver(Arbeidsgiver.virksomhet(orgnummer))
            .medRefusjonsgrad(refusjonsgrad)
            .medUtbetalingsgrad(utbetalingsgrad)
            .build();
    }

    private static void validerRefusjonTom(IntervallEntitet periode, LocalDate refusjonTom) {
        if (periode.inkluderer(refusjonTom) && !periode.getTomDato().equals(refusjonTom)) {
            LOGGER.warn(String.format("Fant periode i infotrygd der refusjonTom ikke ligger ved slutten av en utbetalingsperiode: periode: {%s}, refusjonTom: {%s}", periode, refusjonTom));
        }
    }

    private static BigDecimal mapTilDagsats(InfotrygdYtelseArbeid arbeid) {
        return switch (arbeid.getInntektperiode()) {
            case FASTSATT25PAVVIK, ÅRLIG -> arbeid.getInntekt().divide(BigDecimal.valueOf(260), 10, RoundingMode.HALF_UP);
            case MÅNEDLIG -> arbeid.getInntekt().multiply(BigDecimal.valueOf(12)).divide(BigDecimal.valueOf(260), 10, RoundingMode.HALF_UP);
            case DAGLIG -> arbeid.getInntekt();
            case UKENTLIG -> arbeid.getInntekt().multiply(BigDecimal.valueOf(52)).divide(BigDecimal.valueOf(260), 10, RoundingMode.HALF_UP);
            case BIUKENTLIG -> arbeid.getInntekt().multiply(BigDecimal.valueOf(26)).divide(BigDecimal.valueOf(260), 10, RoundingMode.HALF_UP);
            default -> throw new IllegalArgumentException("Ugyldig InntektPeriodeType" + arbeid.getInntektperiode());
        };
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
