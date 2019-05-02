package no.nav.foreldrepenger.abakus.domene.iay;

import java.math.BigDecimal;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektPeriodeType;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.domene.virksomhet.Virksomhet;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;

public class YtelseStørrelseBuilder {
    private final YtelseStørrelseEntitet ytelseStørrelseEntitet;

    YtelseStørrelseBuilder(YtelseStørrelseEntitet ytelseStørrelseEntitet) {
        this.ytelseStørrelseEntitet = ytelseStørrelseEntitet;
    }

    public static YtelseStørrelseBuilder ny() {
        return new YtelseStørrelseBuilder(new YtelseStørrelseEntitet());
    }

    public YtelseStørrelseBuilder medVirksomhet(OrgNummer orgNummer) {
        this.ytelseStørrelseEntitet.setVirksomhet(orgNummer);
        return this;
    }

    public YtelseStørrelseBuilder medBeløp(BigDecimal verdi) {
        BigDecimal verdiNotNull = verdi != null ? verdi : new BigDecimal(0);
        this.ytelseStørrelseEntitet.setBeløp(new Beløp(verdiNotNull));
        return this;
    }

    public YtelseStørrelseBuilder medHyppighet(InntektPeriodeType frekvens) {
        this.ytelseStørrelseEntitet.setHyppighet(frekvens);
        return this;
    }

    public YtelseStørrelse build() {
        if (ytelseStørrelseEntitet.hasValues()) {
            return ytelseStørrelseEntitet;
        }
        throw new IllegalStateException();
    }

}
