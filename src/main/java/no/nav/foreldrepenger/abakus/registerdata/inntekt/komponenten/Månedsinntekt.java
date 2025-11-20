package no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten;

import java.math.BigDecimal;
import java.time.YearMonth;

public record Månedsinntekt(Inntektstype inntektstype, YearMonth måned, BigDecimal beløp, String beskrivelse,
                            String arbeidsgiver, String skatteOgAvgiftsregelType) {

    public YtelseNøkkel getNøkkel() {
        return new YtelseNøkkel(måned(), inntektstype(), isYtelse() ? beskrivelse() : null);
    }

    public boolean isYtelse() {
        return !Inntektstype.LØNN.equals(inntektstype());
    }

    public record YtelseNøkkel(YearMonth måned, Inntektstype type, String beskrivelse) { }
}
