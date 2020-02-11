package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.kodemaps;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.Arbeidskategori;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektPeriodeType;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseStatus;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.vedtak.domene.TemaUnderkategori;

public class YtelseTypeReverse {

    private static final Map<TemaUnderkategori, YtelseType> YTELSE_TYPE_MAP = Map.ofEntries(
        Map.entry(TemaUnderkategori.FORELDREPENGER_FODSEL, YtelseType.FORELDREPENGER),
        Map.entry(TemaUnderkategori.FORELDREPENGER_ADOPSJON, YtelseType.FORELDREPENGER),
        Map.entry(TemaUnderkategori.FORELDREPENGER_FODSEL_UTLAND, YtelseType.FORELDREPENGER),
        Map.entry(TemaUnderkategori.FORELDREPENGER_SVANGERSKAPSPENGER, YtelseType.SVANGERSKAPSPENGER),

        Map.entry(TemaUnderkategori.SYKEPENGER_SYKEPENGER, YtelseType.SYKEPENGER),
        Map.entry(TemaUnderkategori.SYKEPENGER_FORSIKRINGSRISIKO, YtelseType.SYKEPENGER),
        Map.entry(TemaUnderkategori.SYKEPENGER_REISETILSKUDD, YtelseType.SYKEPENGER),
        Map.entry(TemaUnderkategori.SYKEPENGER_UTENLANDSOPPHOLD, YtelseType.SYKEPENGER),

        Map.entry(TemaUnderkategori.PÅRØRENDE_OMSORGSPENGER, YtelseType.PÅRØRENDESYKDOM),
        Map.entry(TemaUnderkategori.PÅRØRENDE_OPPLÆRINGSPENGER, YtelseType.PÅRØRENDESYKDOM),
        Map.entry(TemaUnderkategori.PÅRØRENDE_PLEIETRENGENDE_SYKT_BARN, YtelseType.PÅRØRENDESYKDOM),
        Map.entry(TemaUnderkategori.PÅRØRENDE_PLEIETRENGENDE, YtelseType.PÅRØRENDESYKDOM),
        Map.entry(TemaUnderkategori.PÅRØRENDE_PLEIETRENGENDE_PÅRØRENDE, YtelseType.PÅRØRENDESYKDOM),
        Map.entry(TemaUnderkategori.PÅRØRENDE_PLEIEPENGER, YtelseType.PÅRØRENDESYKDOM)
    );


    public static YtelseType reverseMap(TemaUnderkategori tuk, Logger logger) {
        if (YTELSE_TYPE_MAP.get(tuk) == null) {
            logger.warn("Infotrygd ga ukjent kode for stønadskategori 2 {}", tuk.getKode());
        }
        return YTELSE_TYPE_MAP.getOrDefault(tuk, YtelseType.UDEFINERT);
    }

    public static class InfotrygdYtelseAnvist {

        private LocalDate utbetaltFom;
        private LocalDate utbetaltTom;
        private BigDecimal utbetalingsgrad;

        public InfotrygdYtelseAnvist(LocalDate utbetaltFom, LocalDate utbetaltTom, BigDecimal utbetalingsgrad) {
            this.utbetaltFom = utbetaltFom;
            this.utbetaltTom = utbetaltTom;
            this.utbetalingsgrad = utbetalingsgrad;
        }

        public InfotrygdYtelseAnvist(LocalDate utbetaltFom, LocalDate utbetaltTom, int utbetalingsgrad) {
            this.utbetaltFom = utbetaltFom;
            this.utbetaltTom = utbetaltTom;
            this.utbetalingsgrad = new BigDecimal(utbetalingsgrad);
        }

        public LocalDate getUtbetaltFom() {
            return utbetaltFom;
        }

        public LocalDate getUtbetaltTom() {
            return utbetaltTom;
        }

        public BigDecimal getUtbetalingsgrad() {
            return utbetalingsgrad;
        }

        public static Builder getBuilder() {
            return new Builder();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            InfotrygdYtelseAnvist that = (InfotrygdYtelseAnvist) o;
            return Objects.equals(utbetaltFom, that.utbetaltFom) &&
                Objects.equals(utbetaltTom, that.utbetaltTom) &&
                Objects.equals(utbetalingsgrad, that.utbetalingsgrad);
        }

        @Override
        public int hashCode() {
            return Objects.hash(utbetaltFom, utbetaltTom, utbetalingsgrad);
        }

        @Override
        public String toString() {
            return "InfotrygdYtelseAnvist{" +
                "utbetaltFom=" + utbetaltFom +
                ", utbetaltTom=" + utbetaltTom +
                ", utbetalingsgrad=" + utbetalingsgrad +
                '}';
        }

        public static class Builder {
            private InfotrygdYtelseAnvist grunnlag;


            public Builder medUtbetaltFom(LocalDate fom) {
                grunnlag.utbetaltFom = fom;
                return this;
            }

            public Builder medUtbetaltTom(LocalDate tom) {
                grunnlag.utbetaltTom = tom;
                return this;
            }

            public Builder medUtbetalingsgrad(int utbetalingsgrad) {
                grunnlag.utbetalingsgrad = new BigDecimal(utbetalingsgrad);
                return this;
            }

            public Builder medUtbetalingsgrad(BigDecimal utbetalingsgrad) {
                grunnlag.utbetalingsgrad = utbetalingsgrad;
                return this;
            }

            public InfotrygdYtelseAnvist build() {
                Objects.requireNonNull(grunnlag.utbetaltFom);
                Objects.requireNonNull(grunnlag.utbetaltTom);
                return grunnlag;
            }
        }
    }

    public static class InfotrygdYtelseArbeid {

        private String orgnr;
        private BigDecimal inntekt;
        private InntektPeriodeType inntektperiode;
        private Boolean refusjon;

        public InfotrygdYtelseArbeid(String orgnr, BigDecimal inntekt, InntektPeriodeType inntektperiode, Boolean refusjon) {
            this.orgnr = orgnr;
            this.inntekt = inntekt;
            this.inntektperiode = inntektperiode;
            this.refusjon = refusjon;
        }

        public InfotrygdYtelseArbeid(String orgnr, int inntekt, InntektPeriodeType inntektperiode, Boolean refusjon) {
            this.orgnr = orgnr;
            this.inntekt = new BigDecimal(inntekt);
            this.inntektperiode = inntektperiode;
            this.refusjon = refusjon;
        }

        public String getOrgnr() {
            return orgnr;
        }

        public BigDecimal getInntekt() {
            return inntekt;
        }

        public InntektPeriodeType getInntektperiode() {
            return inntektperiode;
        }

        public Boolean getRefusjon() {
            return refusjon;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            InfotrygdYtelseArbeid that = (InfotrygdYtelseArbeid) o;
            return Objects.equals(orgnr, that.orgnr) &&
                Objects.equals(inntekt, that.inntekt) &&
                Objects.equals(inntektperiode, that.inntektperiode) &&
                Objects.equals(refusjon, that.refusjon);
        }

        @Override
        public int hashCode() {
            return Objects.hash(orgnr, inntekt, inntektperiode, refusjon);
        }

        @Override
        public String toString() {
            return "InfotrygdYtelseArbeid{" +
                "orgnr='" + orgnr + '\'' +
                ", inntekt=" + inntekt +
                ", inntektperiode=" + inntektperiode +
                ", refusjon=" + refusjon +
                '}';
        }
    }

    public static class InfotrygdYtelseGrunnlag {

        private LocalDate identdato;
        private YtelseType ytelseType = YtelseType.UDEFINERT;
        private TemaUnderkategori temaUnderkategori = TemaUnderkategori.UDEFINERT;
        private YtelseStatus ytelseStatus = YtelseStatus.AVSLUTTET;
        private List<InfotrygdYtelseAnvist> utbetaltePerioder;
        private LocalDate vedtaksPeriodeFom;
        private LocalDate vedtaksPeriodeTom;

        private Arbeidskategori kategori;
        private List<InfotrygdYtelseArbeid> arbeidsforhold;

        private BigDecimal dekningsgrad;
        private BigDecimal gradering;
        private LocalDate fødselsdatoBarn;
        private LocalDate opprinneligIdentdato;

        public LocalDate getIdentdato() {
            return identdato;
        }

        public YtelseType getYtelseType() {
            return ytelseType;
        }

        public TemaUnderkategori getTemaUnderkategori() {
            return temaUnderkategori;
        }

        public YtelseStatus getYtelseStatus() {
            return ytelseStatus;
        }

        public List<InfotrygdYtelseAnvist> getUtbetaltePerioder() {
            return utbetaltePerioder;
        }

        public LocalDate getVedtaksPeriodeFom() {
            return vedtaksPeriodeFom;
        }

        public LocalDate getVedtaksPeriodeTom() {
            return vedtaksPeriodeTom;
        }

        public Arbeidskategori getKategori() {
            return kategori;
        }

        public List<InfotrygdYtelseArbeid> getArbeidsforhold() {
            return arbeidsforhold;
        }

        public BigDecimal getDekningsgrad() {
            return dekningsgrad;
        }

        public BigDecimal getGradering() {
            return gradering;
        }

        public LocalDate getFødselsdatoBarn() {
            return fødselsdatoBarn;
        }

        public LocalDate getOpprinneligIdentdato() {
            return opprinneligIdentdato;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            InfotrygdYtelseGrunnlag that = (InfotrygdYtelseGrunnlag) o;
            return Objects.equals(identdato, that.identdato) &&
                Objects.equals(ytelseType, that.ytelseType) &&
                Objects.equals(temaUnderkategori, that.temaUnderkategori) &&
                Objects.equals(ytelseStatus, that.ytelseStatus) &&
                Objects.equals(utbetaltePerioder, that.utbetaltePerioder) &&
                Objects.equals(vedtaksPeriodeFom, that.vedtaksPeriodeFom) &&
                Objects.equals(vedtaksPeriodeTom, that.vedtaksPeriodeTom) &&
                Objects.equals(kategori, that.kategori) &&
                Objects.equals(arbeidsforhold, that.arbeidsforhold) &&
                Objects.equals(dekningsgrad, that.dekningsgrad) &&
                Objects.equals(gradering, that.gradering) &&
                Objects.equals(fødselsdatoBarn, that.fødselsdatoBarn) &&
                Objects.equals(opprinneligIdentdato, that.opprinneligIdentdato);
        }

        @Override
        public int hashCode() {
            return Objects.hash(identdato, ytelseType, temaUnderkategori, ytelseStatus, utbetaltePerioder, vedtaksPeriodeFom, vedtaksPeriodeTom, kategori, arbeidsforhold, dekningsgrad, gradering, fødselsdatoBarn, opprinneligIdentdato);
        }

        @Override
        public String toString() {
            return "InfotrygdYtelseGrunnlag{" +
                "identdato=" + identdato +
                ", ytelseType=" + ytelseType +
                ", temaUnderkategori=" + temaUnderkategori +
                ", ytelseStatus=" + ytelseStatus +
                ", utbetaltePerioder=" + utbetaltePerioder +
                ", vedtaksPeriodeFom=" + vedtaksPeriodeFom +
                ", vedtaksPeriodeTom=" + vedtaksPeriodeTom +
                ", kategori=" + kategori +
                ", arbeidsforhold=" + arbeidsforhold +
                ", dekningsgrad=" + dekningsgrad +
                ", gradering=" + gradering +
                ", fødselsdatoBarn=" + fødselsdatoBarn +
                ", opprinneligIdentdato=" + opprinneligIdentdato +
                '}';
        }

        public static Builder getBuilder() {
            return new Builder();
        }

        public static class Builder {
            private InfotrygdYtelseGrunnlag grunnlag;

            Builder() {
                grunnlag = new InfotrygdYtelseGrunnlag();
                grunnlag.arbeidsforhold = new ArrayList<>();
                grunnlag.utbetaltePerioder = new ArrayList<>();
            }

            public Builder medIdentdato(LocalDate identdato) {
                grunnlag.identdato = identdato;
                return this;
            }

            public Builder medYtelseType(YtelseType ytelseType) {
                grunnlag.ytelseType = ytelseType;
                return this;
            }

            public Builder medTemaUnderkategori(TemaUnderkategori temaUnderkategori) {
                grunnlag.temaUnderkategori = temaUnderkategori;
                return this;
            }

            public Builder medYtelseStatus(YtelseStatus ytelseStatus) {
                grunnlag.ytelseStatus = ytelseStatus;
                return this;
            }

            public Builder medVedtaksPeriodeFom(LocalDate vedtaksPeriodeFom) {
                grunnlag.vedtaksPeriodeFom = vedtaksPeriodeFom;
                return this;
            }

            public Builder medVedtaksPeriodeTom(LocalDate vedtaksPeriodeTom) {
                grunnlag.vedtaksPeriodeTom = vedtaksPeriodeTom;
                return this;
            }

            public Builder medArbeidskategori(Arbeidskategori arbeidskategori) {
                grunnlag.kategori = arbeidskategori;
                return this;
            }

            public Builder leggTillAnvistPerioder(InfotrygdYtelseAnvist anvistPeriode) {
                grunnlag.utbetaltePerioder.add(anvistPeriode);
                return this;
            }

            public Builder leggTilArbeidsforhold(InfotrygdYtelseArbeid arbeidsforhold) {
                grunnlag.arbeidsforhold.add(arbeidsforhold);
                return this;
            }

            public Builder medDekningsgrad(Integer dekningsgrad) {
                grunnlag.dekningsgrad = dekningsgrad == null ? null : new BigDecimal(dekningsgrad);
                return this;
            }
            public Builder medDekningsgrad(BigDecimal dekningsgrad) {
                grunnlag.dekningsgrad = dekningsgrad;
                return this;
            }

            public Builder medGradering(Integer gradering) {
                grunnlag.gradering = gradering == null ? null : new BigDecimal(gradering);
                return this;
            }

            public Builder medGradering(BigDecimal gradering) {
                grunnlag.gradering = gradering;
                return this;
            }

            public Builder medFødselsdatoBarn(LocalDate fødselsdatoBarn) {
                grunnlag.fødselsdatoBarn = fødselsdatoBarn;
                return this;
            }

            public Builder medOpprinneligIdentdato(LocalDate opprinneligIdentdato) {
                grunnlag.opprinneligIdentdato = opprinneligIdentdato;
                return this;
            }

            public InfotrygdYtelseGrunnlag build() {
                Objects.requireNonNull(grunnlag.ytelseType);
                Objects.requireNonNull(grunnlag.temaUnderkategori);
                Objects.requireNonNull(grunnlag.identdato);
                Objects.requireNonNull(grunnlag.vedtaksPeriodeFom);
                return grunnlag;
            }
        }
    }
}
