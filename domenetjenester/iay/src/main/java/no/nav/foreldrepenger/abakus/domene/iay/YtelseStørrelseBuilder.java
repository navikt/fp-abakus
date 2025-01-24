package no.nav.foreldrepenger.abakus.domene.iay;

import java.math.BigDecimal;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektPeriodeType;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;

public class YtelseStørrelseBuilder {
    private final YtelseStørrelse ytelseStørrelse;

    YtelseStørrelseBuilder(YtelseStørrelse ytelseStørrelse) {
        this.ytelseStørrelse = ytelseStørrelse;
    }

    public static YtelseStørrelseBuilder ny() {
        return new YtelseStørrelseBuilder(new YtelseStørrelse());
    }

    public YtelseStørrelseBuilder medVirksomhet(OrgNummer orgNummer) {
        this.ytelseStørrelse.setVirksomhet(orgNummer);
        return this;
    }

    public YtelseStørrelseBuilder medBeløp(BigDecimal verdi) {
        BigDecimal verdiNotNull = verdi != null ? verdi : new BigDecimal(0);
        this.ytelseStørrelse.setBeløp(new Beløp(verdiNotNull));
        return this;
    }

    public YtelseStørrelseBuilder medHyppighet(InntektPeriodeType frekvens) {
        this.ytelseStørrelse.setHyppighet(frekvens);
        return this;
    }

    public YtelseStørrelseBuilder medErRefusjon(Boolean erRefusjon) {
        this.ytelseStørrelse.setErRefusjon(erRefusjon);
        return this;
    }

    public YtelseStørrelse build() {
        if (ytelseStørrelse.hasValues()) {
            return ytelseStørrelse;
        }
        throw new IllegalStateException();
    }
}
