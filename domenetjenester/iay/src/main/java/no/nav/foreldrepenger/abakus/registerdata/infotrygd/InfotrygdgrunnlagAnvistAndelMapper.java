package no.nav.foreldrepenger.abakus.registerdata.infotrygd;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.abakus.iaygrunnlag.kodeverk.Arbeidskategori;
import no.nav.abakus.iaygrunnlag.kodeverk.Inntektskategori;
import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseAnvistAndel;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseAnvistAndelBuilder;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.dto.InfotrygdYtelseAnvist;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;
import no.nav.foreldrepenger.abakus.typer.OrganisasjonsNummerValidator;
import no.nav.foreldrepenger.abakus.typer.Stillingsprosent;

public class InfotrygdgrunnlagAnvistAndelMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfotrygdgrunnlagAnvistAndelMapper.class);

    private InfotrygdgrunnlagAnvistAndelMapper() {
    }

    /**
     * Mapper utbetalinger fra infotrygd til anviste andeler
     * <p>
     *
     * @param kategori     Arbeidskategori fra infotrygd
     * @param utbetalinger Vedtak/anvisning for periode
     * @return Liste med andeler
     */
    public static List<YtelseAnvistAndel> oversettYtelseArbeidTilAnvisteAndeler(Arbeidskategori kategori,
                                                                                List<InfotrygdYtelseAnvist> utbetalinger) {
        LOGGER.info("Mapper utbetalinger fra infotrygd:  " + utbetalinger);
        var inntektskategorier = splittArbeidskategoriTilInntektskategorier(kategori);
        if (inntektskategorier.isEmpty()) {
            LOGGER.info("Kunne ikke mappe inntektskategori fra infotrygdgrunnlag. Mapper ingen andeler for anvisning.");
            return Collections.emptyList();
        }
        var utbetalingerTilFordeling = mapTilMellomregninger(utbetalinger);
        var andeler = new ArrayList<YtelseAnvistAndel>();

        // 1. Mapper først kategori som ikke er ARBEIDTAKER ved kombinasjon
        andeler.addAll(finnIkkeArbeidstakerAndel(finnUtbetalingerUtenRefusjon(utbetalingerTilFordeling), inntektskategorier));

        if (inntektskategorier.contains(Inntektskategori.ARBEIDSTAKER)) {
            // Mapper alle med orgnr satt
            utbetalingerTilFordeling.stream()
                .filter(Mellomregninger::erIkkeFordelt)
                .filter(u -> !u.getErUtbetalingPåNødnummer())
                .map(Mellomregninger::getArbeidsgiver)
                .filter(Objects::nonNull)
                .map(Arbeidsgiver::getOrgnr)
                .distinct()
                .map(orgnr -> beregnFraAnvisning(orgnr, utbetalingerTilFordeling))
                .forEach(andeler::add);

            // Mapper andeler oppgitt på nødnummer (avsluttet arbeidsforhold)
            andeler.addAll(finnYtelseRapportertPåNødnummer(finnUtbetalingerUtenRefusjon(utbetalingerTilFordeling), inntektskategorier));

            // Mapper resterende
            utbetalingerTilFordeling.stream().filter(Mellomregninger::erIkkeFordelt).forEach(u -> {
                    u.setErFordelt(true);
                    andeler.add(YtelseAnvistAndelBuilder.ny()
                        .medArbeidsgiver(u.getArbeidsgiver())
                        .medUtbetalingsgrad(u.getUtbetalingsgrad().getVerdi())
                        .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                        .medRefusjonsgrad(u.getErRefusjon() ? BigDecimal.valueOf(100) : BigDecimal.ZERO)
                        .medDagsats(u.getDagsats().getVerdi())
                        .build());
                }
            );
        }


        var sorterteDagsatserOutput = andeler.stream().map(YtelseAnvistAndel::getDagsats).map(Beløp::getVerdi).map(BigDecimal::intValue).sorted(Comparator.naturalOrder()).toList();
        var sorterteDagsatserInput = utbetalinger.stream().map(InfotrygdYtelseAnvist::getDagsats).map(BigDecimal::intValue).sorted(Comparator.naturalOrder()).toList();

        if (!sorterteDagsatserOutput.equals(sorterteDagsatserInput)) {
            LOGGER.info("Fant diff i fordeling fra infotrygd og mappet fordeling. " +
                "Input var " + utbetalinger + "" +
                "Output var " + andeler);
        }

        return andeler;

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

        @Override
        public String toString() {
            return "Mellomregninger{" +
                "arbeidsgiver=" + arbeidsgiver +
                ", dagsats=" + dagsats +
                ", utbetalingsgrad=" + utbetalingsgrad +
                ", erRefusjon=" + erRefusjon +
                ", erFordelt=" + erFordelt +
                ", erUtbetalingPåNødnummer=" + erUtbetalingPåNødnummer +
                '}';
        }
    }


}
