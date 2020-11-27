package no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Objects;

public class Månedsinntekt {

    private BigDecimal beløp;
    private YearMonth måned;
    private String arbeidsgiver;
    private String arbeidsforholdRef;
    private String ytelseKode;
    private String pensjonKode;
    private String skatteOgAvgiftsregelType;
    private String næringsinntektKode;
    private boolean ytelse;
    private boolean etterbetaling;

    private Månedsinntekt(BigDecimal beløp, YearMonth måned, String arbeidsgiver, String arbeidsforholdRef, String skatteOgAvgiftsregelType) {
        this.beløp = beløp;
        this.måned = måned;
        this.arbeidsgiver = arbeidsgiver;
        this.arbeidsforholdRef = arbeidsforholdRef;
        this.skatteOgAvgiftsregelType = skatteOgAvgiftsregelType;
        this.ytelse = false;
    }

    public String getYtelseKode() {
        return ytelseKode;
    }

    public String getPensjonKode() {
        return pensjonKode;
    }

    public BigDecimal getBeløp() {
        return beløp;
    }

    public YearMonth getMåned() {
        return måned;
    }

    public String getArbeidsgiver() {
        return arbeidsgiver;
    }

    public String getArbeidsforholdRef() {
        return arbeidsforholdRef;
    }

    public String getSkatteOgAvgiftsregelType() {
        return skatteOgAvgiftsregelType;
    }

    public YtelseNøkkel getNøkkel() {
        return new YtelseNøkkel(måned, ytelseKode, pensjonKode, næringsinntektKode);
    }

    public boolean isYtelse() {
        return ytelse;
    }

    public boolean isEtterbetaling() {
        return etterbetaling;
    }

    public String getNæringsinntektKode() {
        return næringsinntektKode;
    }

    public static class Builder {
        private BigDecimal beløp;
        private YearMonth måned;
        private String utbetaler;
        private String arbeidsforholdRef;
        private String ytelseKode;
        private String pensjonKode;
        private String næringsinntektKode;
        private boolean ytelse;
        private boolean etterbetaling;
        private String skatteOgAvgiftsregelType;

        public Builder medBeløp(BigDecimal beløp) {
            this.beløp = beløp;
            return this;
        }

        public Builder medMåned(YearMonth måned) {
            this.måned = måned;
            return this;
        }

        public Builder medArbeidsgiver(String arbeidsgiver) {
            this.utbetaler = arbeidsgiver;
            return this;
        }

        public Builder medPensjonEllerTrygdKode(String kode) {
            this.pensjonKode = kode;
            return this;
        }

        public Builder medYtelseKode(String kode) {
            this.ytelseKode = kode;
            return this;
        }

        public Builder medNæringsinntektKode(String kode) {
            this.næringsinntektKode = kode;
            return this;
        }

        public Builder medArbeidsforholdRef(String arbeidsforholdRef) {
            this.arbeidsforholdRef = arbeidsforholdRef;
            return this;
        }

        public Builder medYtelse(boolean ytelse) {
            this.ytelse = ytelse;
            return this;
        }

        public Builder medEtterbetaling(boolean etterbetaling) {
            this.etterbetaling = etterbetaling;
            return this;
        }


        public Builder medSkatteOgAvgiftsregelType(String skatteOgAvgiftsregelType) {
            this.skatteOgAvgiftsregelType = skatteOgAvgiftsregelType;
            return this;
        }

        public Månedsinntekt build() {
            final Månedsinntekt månedsinntekt = new Månedsinntekt(beløp, måned, utbetaler, arbeidsforholdRef, skatteOgAvgiftsregelType);
            månedsinntekt.ytelse = ytelse;
            månedsinntekt.etterbetaling = etterbetaling;
            månedsinntekt.pensjonKode = pensjonKode;
            månedsinntekt.ytelseKode = ytelseKode;
            månedsinntekt.næringsinntektKode = næringsinntektKode;
            return månedsinntekt;
        }
    }

    class YtelseNøkkel {
        private YearMonth måned;
        private String ytelseKode;
        private String pensjonKode;
        private String næringsinntektKode;

        private YtelseNøkkel(YearMonth måned, String ytelseKode, String pensjonKode, String næringsinntektKode) {
            this.måned = måned;
            this.ytelseKode = ytelseKode;
            this.pensjonKode = pensjonKode;
            this.næringsinntektKode = næringsinntektKode;
        }

        public YearMonth getMåned() {
            return måned;
        }

        public String getYtelseKode() {
            return ytelseKode;
        }

        public String getPensjonKode() {
            return pensjonKode;
        }

        public String getNæringsinntektKode() {
            return næringsinntektKode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            YtelseNøkkel nøkkel = (YtelseNøkkel) o;
            return Objects.equals(måned, nøkkel.måned) &&
                Objects.equals(ytelseKode, nøkkel.ytelseKode) &&
                Objects.equals(pensjonKode, nøkkel.pensjonKode) &&
                Objects.equals(næringsinntektKode, nøkkel.næringsinntektKode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(måned, ytelseKode, pensjonKode, næringsinntektKode);
        }
    }
}
