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
import no.nav.foreldrepenger.abakus.typer.Beløp;
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
                oversettYtelseArbeidTilAnvisteAndeler(grunnlag.getKategori(),
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
        var erToggletPå = !Environment.current().isProd();
        var harDagsatsIListeMedUtbetalinger = grunnlag.getUtbetaltePerioder().stream().allMatch(p -> p.getDagsats() != null);
        return erToggletPå &&
            !grunnlag.getKategori().equals(Arbeidskategori.UGYLDIG)
            && harDagsatsIListeMedUtbetalinger;
    }

    /**
     * Mapper utbetalinger fra infotrygd til anviste andeler
     * <p>
     *
     * @param kategori       Arbeidskategori fra infotrygd
     * @param arbeidsforhold Beregningsgrunnlag fra infotrygd
     * @param utbetalinger   Vedtak/anvisning for periode
     * @return Liste med andeler
     */
    private static List<YtelseAnvistAndel> oversettYtelseArbeidTilAnvisteAndeler(Arbeidskategori kategori,
                                                                                 List<InfotrygdYtelseArbeid> arbeidsforhold, List<InfotrygdYtelseAnvist> utbetalinger) {
        var inntektskategorier = splittArbeidskategoriTilInntektskategorier(kategori);
        if (inntektskategorier.isEmpty()) {
            LOGGER.info("Kunne ikke mappe inntektskategori fra infotrygdgrunnlag. Mapper ingen andeler for anvisning.");
            return Collections.emptyList();
        }
        var totaltUtbetalt = utbetalinger.stream().map(InfotrygdYtelseAnvist::getDagsats).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        var andelerMedRefusjon = finnArbeidstakerAndeler(arbeidsforhold, utbetalinger, totaltUtbetalt, true);
        var andeler = new ArrayList<>(andelerMedRefusjon);
        var andelerUtenRefusjon = finnArbeidstakerAndeler(arbeidsforhold, utbetalinger, finnRestTilFordeling(utbetalinger, andelerMedRefusjon), false);
        andeler.addAll(andelerUtenRefusjon);
        finnYtelseRapportertPåNødnummer(utbetalinger, inntektskategorier).ifPresent(andeler::add);
        finnIkkeArbeidstakerAndel(utbetalinger, inntektskategorier, finnRestTilFordeling(utbetalinger, andeler)).ifPresent(andeler::add);
        return andeler;

    }

    private static BigDecimal finnRestTilFordeling(List<InfotrygdYtelseAnvist> utbetalinger, List<YtelseAnvistAndel> andeler) {
        var total = utbetalinger.stream().map(InfotrygdYtelseAnvist::getDagsats).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        var fordelt = andeler.stream().map(YtelseAnvistAndel::getDagsats)
            .map(Beløp::getVerdi)
            .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        return total.subtract(fordelt);
    }

    private static Optional<YtelseAnvistAndel> finnIkkeArbeidstakerAndel(List<InfotrygdYtelseAnvist> utbetalinger,
                                                                         Set<Inntektskategori> inntektskategorier,
                                                                         BigDecimal restTilFordeling) {
        var ikkeArbeidstakerKategori = inntektskategorier.stream().filter(a -> !a.equals(Inntektskategori.ARBEIDSTAKER)).findFirst();
        if (ikkeArbeidstakerKategori.isEmpty()) {
            return Optional.empty();
        }

        var utbetalingerUtenOrgnr = utbetalinger.stream().filter(arb -> !OrganisasjonsNummerValidator.erGyldig(arb.getOrgnr())).toList();

        if (utbetalingerUtenOrgnr.size() == 0) {
            return Optional.of(YtelseAnvistAndelBuilder.ny()
                .medUtbetalingsgrad(finnUtbetalingsgrad(utbetalingerUtenOrgnr))
                .medInntektskategori(ikkeArbeidstakerKategori.get())
                .medRefusjonsgrad(BigDecimal.ZERO)
                .medDagsats(BigDecimal.ZERO)
                .build()
            );
        }

        if (restTilFordeling.compareTo(BigDecimal.ZERO) == 0) {
            return Optional.of(YtelseAnvistAndelBuilder.ny()
                .medUtbetalingsgrad(finnUtbetalingsgrad(utbetalingerUtenOrgnr))
                .medInntektskategori(ikkeArbeidstakerKategori.get())
                .medRefusjonsgrad(BigDecimal.ZERO)
                .medDagsats(BigDecimal.ZERO)
                .build()
            );
        }

        return Optional.of(YtelseAnvistAndelBuilder.ny()
            .medUtbetalingsgrad(finnUtbetalingsgrad(utbetalingerUtenOrgnr))
            .medInntektskategori(ikkeArbeidstakerKategori.get())
            .medRefusjonsgrad(BigDecimal.ZERO)
            .medDagsats(restTilFordeling)
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

    private static List<YtelseAnvistAndel> finnArbeidstakerAndeler(List<InfotrygdYtelseArbeid> arbeidsforhold,
                                                                   List<InfotrygdYtelseAnvist> utbetalinger,
                                                                   BigDecimal restTilFordeling,
                                                                   boolean medRefusjon) {
        var gruppertPrOrg = arbeidsforhold.stream()
            .filter(a -> a.getOrgnr() != null)
            .filter(a -> medRefusjon ? a.getRefusjon() : a.getRefusjon() == null || !a.getRefusjon())
            .filter(a -> OrganisasjonsNummerValidator.erGyldig(a.getOrgnr()))
            .collect(Collectors.groupingBy(InfotrygdYtelseArbeid::getOrgnr));

        return gruppertPrOrg.entrySet().stream()
            .map(e -> mapGrunnlagsandelerTilAnvistandel(
                new OrgNummer(e.getKey()),
                e.getValue(),
                arbeidsforhold,
                utbetalinger,
                restTilFordeling, medRefusjon)).toList();
    }

    private static YtelseAnvistAndel beregnAndelFraGrunnlag(OrgNummer orgnummer,
                                                            List<InfotrygdYtelseArbeid> alleGrunnlagsandeler,
                                                            List<InfotrygdYtelseArbeid> grunnlagsandelerForArbeid,
                                                            List<InfotrygdYtelseAnvist> anvisninger,
                                                            BigDecimal tilFordeling,
                                                            boolean medRefusjon) {
        var gruppe = alleGrunnlagsandeler.stream().filter(a ->
            medRefusjon ? a.getRefusjon() : a.getRefusjon() == null || !a.getRefusjon()).toList();
        var grunnlagFraGruppe = gruppe.stream()
            .map(InfotrygdgrunnlagYtelseMapper::mapTilDagsats)
            .map(d -> finnUtbetalingsgrad(anvisninger).multiply(d))
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);

        if (grunnlagFraGruppe.compareTo(tilFordeling) > 0) {
            return beregnFraGrunnlagOgAnvisning(grunnlagFraGruppe,
                grunnlagsandelerForArbeid,
                tilFordeling,
                Optional.of(orgnummer),
                finnUtbetalingsgrad(anvisninger)).orElse(nullAndel(orgnummer));
        } else {
            return beregnFraGrunnlagOgAnvisning(grunnlagFraGruppe,
                grunnlagsandelerForArbeid,
                grunnlagFraGruppe,
                Optional.of(orgnummer),
                finnUtbetalingsgrad(anvisninger)).orElse(nullAndel(orgnummer));
        }
    }

    private static YtelseAnvistAndel mapGrunnlagsandelerTilAnvistandel(OrgNummer orgnummer,
                                                                       List<InfotrygdYtelseArbeid> grunnlagsandelerForArbeid,
                                                                       List<InfotrygdYtelseArbeid> alleGrunnlagsandeler,
                                                                       List<InfotrygdYtelseAnvist> anvisninger,
                                                                       BigDecimal tilFordeling,
                                                                       boolean medRefusjon) {
        // Dersom vi finner en utbetaling som har orgnr fra beregningsgrunnlag bruker vi denne
        if (anvisninger.stream().anyMatch(a -> a.getOrgnr().equals(orgnummer.getId()))) {
            return beregnFraAnvisning(orgnummer, anvisninger);
        } else {
            return beregnAndelFraGrunnlag(orgnummer, alleGrunnlagsandeler, grunnlagsandelerForArbeid, anvisninger, tilFordeling, medRefusjon);
        }
    }

    private static Optional<YtelseAnvistAndel> beregnFraGrunnlagOgAnvisning(BigDecimal totalGrunnlag,
                                                                            List<InfotrygdYtelseArbeid> grunnlagsandelerForAktivitet,
                                                                            BigDecimal tilFordeling,
                                                                            Optional<OrgNummer> orgnummer,
                                                                            BigDecimal utbetalingsgrad) {
        var totalgrunnlagForAktivitet = grunnlagsandelerForAktivitet.stream()
            .map(InfotrygdgrunnlagYtelseMapper::mapTilDagsats)
            .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);

        if (tilFordeling.compareTo(BigDecimal.ZERO) == 0) {
            return Optional.empty();
        }

        var fraksjonAvTotalUtbetaling = totalgrunnlagForAktivitet.divide(totalGrunnlag, RoundingMode.HALF_UP);
        var andel = fraksjonAvTotalUtbetaling.multiply(tilFordeling);

        BigDecimal refusjonsgrad = finnRefusjonsgrad(grunnlagsandelerForAktivitet, totalgrunnlagForAktivitet);
        return Optional.of(YtelseAnvistAndelBuilder.ny()
            .medDagsats(andel)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medArbeidsgiver(orgnummer.map(Arbeidsgiver::virksomhet).orElse(null))
            .medRefusjonsgrad(refusjonsgrad)
            .medUtbetalingsgrad(utbetalingsgrad)
            .build());
    }

    private static BigDecimal finnRefusjonsgrad(List<InfotrygdYtelseArbeid> grunnlagsandelerForAktivitet, BigDecimal totalgrunnlagForAktivitet) {
        return grunnlagsandelerForAktivitet.stream()
            .filter(InfotrygdYtelseArbeid::getRefusjon)
            .map(InfotrygdgrunnlagYtelseMapper::mapTilDagsats)
            .reduce(BigDecimal::add).orElse(BigDecimal.ZERO).divide(totalgrunnlagForAktivitet, RoundingMode.HALF_UP);
    }

    private static YtelseAnvistAndel nullAndel(OrgNummer orgnummer) {
        return YtelseAnvistAndelBuilder.ny()
            .medDagsats(BigDecimal.ZERO)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medArbeidsgiver(Arbeidsgiver.virksomhet(orgnummer))
            .medRefusjonsgrad(BigDecimal.ZERO)
            .medUtbetalingsgrad(BigDecimal.ZERO)
            .build();
    }

    private static YtelseAnvistAndel beregnFraAnvisning(OrgNummer orgnummer, List<InfotrygdYtelseAnvist> anvisninger) {
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
