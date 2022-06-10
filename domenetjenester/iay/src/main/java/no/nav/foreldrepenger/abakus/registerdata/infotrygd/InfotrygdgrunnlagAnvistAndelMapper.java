package no.nav.foreldrepenger.abakus.registerdata.infotrygd;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.abakus.iaygrunnlag.kodeverk.Arbeidskategori;
import no.nav.abakus.iaygrunnlag.kodeverk.Inntektskategori;
import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseAnvistAndel;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseAnvistAndelBuilder;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.dto.InfotrygdYtelseAnvist;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.dto.InfotrygdYtelseArbeid;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;
import no.nav.foreldrepenger.abakus.typer.OrganisasjonsNummerValidator;
import no.nav.foreldrepenger.abakus.typer.Stillingsprosent;

public class InfotrygdgrunnlagAnvistAndelMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfotrygdgrunnlagAnvistAndelMapper.class);
    public static final BigDecimal DIFF_SOM_LOGGES = BigDecimal.valueOf(2);
    public static final BigDecimal DIFF_SOM_AKSEPTERES_GRENSE = BigDecimal.valueOf(10);

    private InfotrygdgrunnlagAnvistAndelMapper() {
    }

    /**
     * Mapper utbetalinger fra infotrygd til anviste andeler
     * <p>
     * Utbetalinger fra infotrygd inneholder ikke alltid orgnr for arbeidsforholdet det er basert på og heller ingen inntektskategori.
     * For å kunne mappe utbetalinger til inntektskategori og orgnr bruker vi grunnlaget for å estimere hva som bør vere utbetalt dagsats for hver enkelt arbeidsforhold/aktivitet. Dette gjøres basert på kjente prioriteringsregler for refusjon i kap 8 i folketrygdloven.
     * Deretter finner vi den utbetalingen fra infotrygd som ligger nærmest denne estimerte dagsatsen.
     *
     * @param kategori       Arbeidskategori fra infotrygd
     * @param arbeidsforhold Beregningsgrunnlag fra infotrygd
     * @param utbetalinger   Vedtak/anvisning for periode
     * @return Liste med andeler
     */
    public static List<YtelseAnvistAndel> oversettYtelseArbeidTilAnvisteAndeler(Arbeidskategori kategori,
                                                                                List<InfotrygdYtelseArbeid> arbeidsforhold,
                                                                                List<InfotrygdYtelseAnvist> utbetalinger) {
        var inntektskategorier = splittArbeidskategoriTilInntektskategorier(kategori);
        if (inntektskategorier.isEmpty()) {
            LOGGER.info("Kunne ikke mappe inntektskategori fra infotrygdgrunnlag. Mapper ingen andeler for anvisning.");
            return Collections.emptyList();
        }
        var utbetalingerTilFordeling = mapTilMellomregninger(utbetalinger);
        var andeler = new ArrayList<YtelseAnvistAndel>();

        if (inntektskategorier.contains(Inntektskategori.ARBEIDSTAKER)) {
            // 1. Fordeler til arbeidstaker-andeler med refusjon
            andeler.addAll(finnArbeidstakerAndeler(arbeidsforhold, finnUtbetalingerMedRefusjon(utbetalingerTilFordeling), true));
            // 2. Fordeler til arbeidstaker-andeler uten refusjon
            andeler.addAll(finnArbeidstakerAndeler(arbeidsforhold, finnUtbetalingerUtenRefusjon(utbetalingerTilFordeling), false));
            // 3. Fordeler til andeler oppgitt på nødnummer (avsluttet arbeidsforhold)
            andeler.addAll(finnYtelseRapportertPåNødnummer(finnUtbetalingerUtenRefusjon(utbetalingerTilFordeling), inntektskategorier));
        }

        // Fordeler til andeler for kategori som ikke er arbeidstaker
        andeler.addAll(finnIkkeArbeidstakerAndel(finnUtbetalingerUtenRefusjon(utbetalingerTilFordeling), inntektskategorier));

        var sorterteDagsatserOutput = andeler.stream().map(YtelseAnvistAndel::getDagsats).map(Beløp::getVerdi).map(BigDecimal::intValue).sorted(Comparator.naturalOrder()).toList();
        var sorterteDagsatserInput = utbetalinger.stream().map(InfotrygdYtelseAnvist::getDagsats).map(BigDecimal::intValue).sorted(Comparator.naturalOrder()).toList();

        if (!sorterteDagsatserOutput.equals(sorterteDagsatserInput)) {
            LOGGER.info("Fant diff i fordeling fra infotrygd og mappet fordeling. " +
                "Input var " + utbetalinger + "" +
                "Output var " + andeler);
        }


        if (utbetalingerTilFordeling.stream().anyMatch(Mellomregninger::erIkkeFordelt)) {
            LOGGER.info("Fant utbetaling som ikke ble fordelt: " + utbetalingerTilFordeling.stream().filter(Mellomregninger::erIkkeFordelt).toList());
        }

        return andeler;

    }

    private static List<Mellomregninger> finnUtbetalingerMedRefusjon(List<Mellomregninger> utbetalingerTilFordeling) {
        return utbetalingerTilFordeling.stream().filter(Mellomregninger::getErRefusjon).toList();
    }

    private static List<Mellomregninger> finnUtbetalingerUtenRefusjon(List<Mellomregninger> utbetalingerTilFordeling) {
        return utbetalingerTilFordeling.stream().filter(m -> !m.getErRefusjon()).toList();
    }

    private static List<YtelseAnvistAndel> finnIkkeArbeidstakerAndel(List<Mellomregninger> utbetalinger,
                                                                     Set<Inntektskategori> inntektskategorier) {
        var ikkeArbeidstakerKategori = inntektskategorier.stream().filter(a -> !a.equals(Inntektskategori.ARBEIDSTAKER)).findFirst();
        if (ikkeArbeidstakerKategori.isEmpty()) {
            return Collections.emptyList();
        }

        var utbetalingerUtenOrgnr = utbetalinger.stream().filter(arb -> arb.getArbeidsgiver() == null)
            .filter(Mellomregninger::erIkkeFordelt)
            .toList();

        utbetalingerUtenOrgnr.forEach(u -> u.setErFordelt(true));

        return utbetalingerUtenOrgnr.stream().map(u ->
            YtelseAnvistAndelBuilder.ny()
                .medUtbetalingsgrad(u.getUtbetalingsgrad().getVerdi())
                .medInntektskategori(ikkeArbeidstakerKategori.get())
                .medRefusjonsgrad(BigDecimal.ZERO)
                .medDagsats(u.getDagsats().getVerdi())
                .build()
        ).toList();
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
    private static List<YtelseAnvistAndel> finnYtelseRapportertPåNødnummer(List<Mellomregninger> anvisninger,
                                                                           Set<Inntektskategori> inntektskategorier) {
        var anvisningerPåNødnummer = anvisninger.stream()
            .filter(Mellomregninger::getErUtbetalingPåNødnummer)
            .filter(Mellomregninger::erIkkeFordelt)
            .toList();

        if (anvisningerPåNødnummer.size() > 0) {
            var erArbeidstaker = inntektskategorier.stream().anyMatch(i -> i.equals(Inntektskategori.ARBEIDSTAKER));
            var inntektskategori = erArbeidstaker ? Inntektskategori.ARBEIDSTAKER : inntektskategorier.stream().filter(i -> !i.equals(Inntektskategori.ARBEIDSTAKER)).findFirst().orElse(Inntektskategori.ARBEIDSTAKER);
            anvisningerPåNødnummer.forEach(u -> u.setErFordelt(true));
            return anvisningerPåNødnummer.stream().map(a -> YtelseAnvistAndelBuilder.ny()
                .medInntektskategori(inntektskategori)
                .medDagsats(a.getDagsats().getVerdi())
                .medRefusjonsgrad(BigDecimal.ZERO)
                .medUtbetalingsgrad(a.getUtbetalingsgrad().getVerdi()).build()).toList();
        }

        return Collections.emptyList();
    }

    private static List<YtelseAnvistAndel> finnArbeidstakerAndeler(List<InfotrygdYtelseArbeid> arbeidsforhold,
                                                                   List<Mellomregninger> utbetalinger,
                                                                   boolean medRefusjon) {
        var gruppertPrOrg = arbeidsforhold.stream()
            .filter(a -> a.getOrgnr() != null)
            .filter(a -> medRefusjon ? a.getRefusjon() : a.getRefusjon() == null || !a.getRefusjon())
            .filter(a -> OrganisasjonsNummerValidator.erGyldig(a.getOrgnr()))
            .collect(Collectors.groupingBy(InfotrygdYtelseArbeid::getOrgnr));

        var sortertListe = gruppertPrOrg.entrySet().stream()
            .sorted(harAnvisningPåArbeidsgiverFørstComparator(utbetalinger))
            .toList();
        var resultatListe = new ArrayList<YtelseAnvistAndel>();
        var iterator = sortertListe.iterator();

        var restTilFordeling = utbetalinger.stream().filter(Mellomregninger::erIkkeFordelt)
            .map(Mellomregninger::getDagsats)
            .map(Beløp::getVerdi)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);

        while (utbetalinger.stream().anyMatch(Mellomregninger::erIkkeFordelt) && iterator.hasNext()) {
            var next = iterator.next();
            var ytelseAnvistAndel = mapGrunnlagsandelerTilAnvistandel(
                new OrgNummer(next.getKey()),
                next.getValue(),
                arbeidsforhold,
                utbetalinger,
                restTilFordeling,
                medRefusjon);
            resultatListe.add(ytelseAnvistAndel);
        }
        return resultatListe;
    }

    private static Comparator<Map.Entry<String, List<InfotrygdYtelseArbeid>>> harAnvisningPåArbeidsgiverFørstComparator(List<Mellomregninger> utbetalinger) {
        return (e1, e2) ->
        {
            var harMatch1 = utbetalinger.stream().anyMatch(u -> u.getArbeidsgiver() != null && u.getArbeidsgiver().getOrgnr().getId().equals(e1.getKey()));
            var harMatch2 = utbetalinger.stream().anyMatch(u -> u.getArbeidsgiver() != null && u.getArbeidsgiver().getOrgnr().getId().equals(e2.getKey()));
            if (harMatch1) {
                return harMatch2 ? 0 : -1;
            } else {
                return harMatch2 ? 1 : 0;
            }
        };
    }

    private static YtelseAnvistAndel beregnAndelFraGrunnlag(OrgNummer orgnummer,
                                                            List<InfotrygdYtelseArbeid> alleGrunnlagsandeler,
                                                            List<InfotrygdYtelseArbeid> grunnlagsandelerForArbeid,
                                                            List<Mellomregninger> anvisninger,
                                                            BigDecimal restTilFordeling,
                                                            boolean medRefusjon) {
        var gruppe = alleGrunnlagsandeler.stream()
            .filter(a -> OrganisasjonsNummerValidator.erGyldig(a.getOrgnr())).filter(a ->
                medRefusjon ? a.getRefusjon() : a.getRefusjon() == null || !a.getRefusjon()).toList();
        var utbetalingsgrad = finnUtbetalingsgrad(anvisninger);
        var grunnlagFraGruppe = gruppe.stream()
            .map(InfotrygdgrunnlagAnvistAndelMapper::mapTilDagsats)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);

        var gradertGrunnlagFraGruppe = grunnlagFraGruppe.multiply(utbetalingsgrad).divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);

        if (gradertGrunnlagFraGruppe.compareTo(restTilFordeling) > 0) {
            return beregnFraGrunnlagOgAnvisning(grunnlagFraGruppe,
                grunnlagsandelerForArbeid,
                restTilFordeling,
                anvisninger,
                Optional.of(orgnummer)
            ).orElse(nullAndel(orgnummer));
        } else {
            return beregnFraGrunnlagOgAnvisning(grunnlagFraGruppe,
                grunnlagsandelerForArbeid,
                grunnlagFraGruppe,
                anvisninger,
                Optional.of(orgnummer)
            ).orElse(nullAndel(orgnummer));
        }
    }

    private static YtelseAnvistAndel mapGrunnlagsandelerTilAnvistandel(OrgNummer orgnummer,
                                                                       List<InfotrygdYtelseArbeid> grunnlagsandelerForArbeid,
                                                                       List<InfotrygdYtelseArbeid> alleGrunnlagsandeler,
                                                                       List<Mellomregninger> anvisninger,
                                                                       BigDecimal restGrunnlagForGruppe,
                                                                       boolean medRefusjon) {
        // Dersom vi finner en utbetaling som har orgnr fra beregningsgrunnlag bruker vi denne
        if (anvisninger.stream().anyMatch(a -> a.getArbeidsgiver() != null && a.getArbeidsgiver().getOrgnr().getId().equals(orgnummer.getId()))) {
            return beregnFraAnvisning(orgnummer, anvisninger);
        } else {
            return beregnAndelFraGrunnlag(orgnummer, alleGrunnlagsandeler, grunnlagsandelerForArbeid, anvisninger, restGrunnlagForGruppe, medRefusjon);
        }
    }

    private static Optional<YtelseAnvistAndel> beregnFraGrunnlagOgAnvisning(BigDecimal totalGrunnlag,
                                                                            List<InfotrygdYtelseArbeid> grunnlagsandelerForAktivitet,
                                                                            BigDecimal grunnlagTilFordeling,
                                                                            List<Mellomregninger> anvisninger,
                                                                            Optional<OrgNummer> orgnummer) {
        var totalgrunnlagForAktivitet = grunnlagsandelerForAktivitet.stream()
            .map(InfotrygdgrunnlagAnvistAndelMapper::mapTilDagsats)
            .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);

        if (grunnlagTilFordeling.compareTo(BigDecimal.ZERO) == 0 || totalgrunnlagForAktivitet.compareTo(BigDecimal.ZERO) == 0) {
            return Optional.empty();
        }

        var utbetalingsgrad = finnUtbetalingsgrad(anvisninger);

        var fraksjonAvTotalUtbetaling = totalgrunnlagForAktivitet.divide(totalGrunnlag, 10, RoundingMode.HALF_UP);
        var andel = fraksjonAvTotalUtbetaling.multiply(grunnlagTilFordeling).multiply(utbetalingsgrad).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        var nærmesteUtbetaling = anvisninger.stream()
            .filter(Mellomregninger::erIkkeFordelt)
            .filter(a -> a.getArbeidsgiver() == null) // Filterer bort utbetalinger som spesifiserer arbeidsgiver. Disse mappes i et annet spor.
            .min(Comparator.comparing(u -> u.getDagsats().getVerdi().subtract(andel).abs()));

        BigDecimal refusjonsgrad = finnRefusjonsgrad(grunnlagsandelerForAktivitet, totalgrunnlagForAktivitet);


        var diff = nærmesteUtbetaling.map(u -> u.getDagsats().getVerdi().subtract(andel).abs());
        if (diff.isPresent() && diff.get().compareTo(DIFF_SOM_LOGGES) > 0) {
            var infotrygdYtelseArbeid = grunnlagsandelerForAktivitet.get(0);
            LOGGER.info("Estimert diff fra grunnlag og utbetaling fra infotrygd var " + diff.get() + " for andel " + infotrygdYtelseArbeid);
            if (diff.get().compareTo(DIFF_SOM_AKSEPTERES_GRENSE) > 0) {
                return Optional.empty();
            }
        }
        nærmesteUtbetaling.ifPresent(u -> u.setErFordelt(true));

        return nærmesteUtbetaling.map(utbetaling ->
            YtelseAnvistAndelBuilder.ny()
                .medDagsats(utbetaling.getDagsats().getVerdi())
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medArbeidsgiver(orgnummer.map(Arbeidsgiver::virksomhet).orElse(null))
                .medRefusjonsgrad(refusjonsgrad)
                .medUtbetalingsgrad(utbetaling.getUtbetalingsgrad().getVerdi())
                .build()
        );
    }

    private static BigDecimal finnRefusjonsgrad(List<InfotrygdYtelseArbeid> grunnlagsandelerForAktivitet, BigDecimal totalgrunnlagForAktivitet) {
        return grunnlagsandelerForAktivitet.stream()
            .filter(InfotrygdYtelseArbeid::getRefusjon)
            .map(InfotrygdgrunnlagAnvistAndelMapper::mapTilDagsats)
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

    private static YtelseAnvistAndel beregnFraAnvisning(OrgNummer orgnummer, List<Mellomregninger> anvisninger) {
        var anvisningerForOrgnr = anvisninger.stream()
            .filter(a -> a.getArbeidsgiver() != null && a.getArbeidsgiver().getOrgnr().getId().equals(orgnummer.getId())).toList();
        var refusjon = anvisningerForOrgnr.stream()
            .filter(Mellomregninger::getErRefusjon)
            .map(Mellomregninger::getDagsats)
            .map(Beløp::getVerdi)
            .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        var direkteUtbetaling = anvisningerForOrgnr.stream()
            .filter(a -> !a.getErRefusjon())
            .map(Mellomregninger::getDagsats)
            .map(Beløp::getVerdi)
            .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        var total = refusjon.add(direkteUtbetaling);
        var refusjonsgrad = refusjon.divide(total, 10, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        var utbetalingsgrad = finnUtbetalingsgrad(anvisningerForOrgnr);
        anvisningerForOrgnr.forEach(a -> a.setErFordelt(true));
        return YtelseAnvistAndelBuilder.ny()
            .medDagsats(total)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medArbeidsgiver(Arbeidsgiver.virksomhet(orgnummer))
            .medRefusjonsgrad(refusjonsgrad)
            .medUtbetalingsgrad(utbetalingsgrad)
            .build();
    }

    private static BigDecimal finnUtbetalingsgrad(List<Mellomregninger> anvisninger) {
        // Antar at disse anvisningene har samme utbetalingsgrad, velger tilfeldig
        return anvisninger.stream().map(Mellomregninger::getUtbetalingsgrad).map(Stillingsprosent::getVerdi).findFirst().orElse(BigDecimal.valueOf(100));
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

    private static List<Mellomregninger> mapTilMellomregninger(List<InfotrygdYtelseAnvist> utbetalinger) {
        return utbetalinger
            .stream()
            .map(u -> new Mellomregninger(OrganisasjonsNummerValidator.erGyldig(u.getOrgnr()) ? Arbeidsgiver.virksomhet(new OrgNummer(u.getOrgnr())) : null,
                new Beløp(u.getDagsats()),
                new Stillingsprosent(u.getUtbetalingsgrad()),
                u.getErRefusjon() != null && u.getErRefusjon(),
                false,
                u.getOrgnr() != null && Arrays.stream(Nødnummer.values()).anyMatch(n -> n.getOrgnummer().equals(u.getOrgnr()))))
            .toList();
    }


    public static class Mellomregninger {
        private final Arbeidsgiver arbeidsgiver;
        private final Beløp dagsats;
        private final Stillingsprosent utbetalingsgrad;
        private final boolean erRefusjon;
        private boolean erFordelt;
        private final boolean erUtbetalingPåNødnummer;


        public Mellomregninger(Arbeidsgiver arbeidsgiver,
                               Beløp dagsats,
                               Stillingsprosent utbetalingsgrad,
                               boolean erRefusjon,
                               boolean erFordelt,
                               boolean erUtbetalingPåNødnummer) {
            this.arbeidsgiver = arbeidsgiver;
            this.dagsats = dagsats;
            this.utbetalingsgrad = utbetalingsgrad;
            this.erRefusjon = erRefusjon;
            this.erFordelt = erFordelt;
            this.erUtbetalingPåNødnummer = erUtbetalingPåNødnummer;
        }

        public Arbeidsgiver getArbeidsgiver() {
            return arbeidsgiver;
        }

        public Beløp getDagsats() {
            return dagsats;
        }

        public Stillingsprosent getUtbetalingsgrad() {
            return utbetalingsgrad;
        }

        public boolean getErRefusjon() {
            return erRefusjon;
        }

        public boolean erIkkeFordelt() {
            return !erFordelt;
        }

        public void setErFordelt(boolean erFordelt) {
            this.erFordelt = erFordelt;
        }

        public boolean getErUtbetalingPåNødnummer() {
            return erUtbetalingPåNødnummer;
        }
    }


}
