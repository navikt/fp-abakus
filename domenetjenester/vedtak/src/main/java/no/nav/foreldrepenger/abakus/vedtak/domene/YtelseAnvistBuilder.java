package no.nav.foreldrepenger.abakus.vedtak.domene;

import java.math.BigDecimal;

import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.typer.Stillingsprosent;

public class YtelseAnvistBuilder {
    private final YtelseAnvistEntitet ytelseAnvistEntitet;

    YtelseAnvistBuilder(YtelseAnvistEntitet ytelseAnvistEntitet) {
        this.ytelseAnvistEntitet = ytelseAnvistEntitet;
    }

    public static YtelseAnvistBuilder ny() {
        return new YtelseAnvistBuilder(new YtelseAnvistEntitet());
    }

    public YtelseAnvistBuilder medBeløp(BigDecimal beløp) {
        if (beløp != null) {
            this.ytelseAnvistEntitet.setBeløp(new Beløp(beløp));
        }
        return this;
    }

    public YtelseAnvistBuilder medDagsats(BigDecimal dagsats) {
        if (dagsats != null) {
            this.ytelseAnvistEntitet.setDagsats(new Beløp(dagsats));
        }
        return this;
    }

    public YtelseAnvistBuilder medAnvistPeriode(IntervallEntitet intervallEntitet) {
        this.ytelseAnvistEntitet.setAnvistPeriode(intervallEntitet);
        return this;
    }

    public YtelseAnvistBuilder medUtbetalingsgradProsent(BigDecimal utbetalingsgradProsent) {
        if (utbetalingsgradProsent != null) {
            this.ytelseAnvistEntitet.setUtbetalingsgradProsent(new Stillingsprosent(utbetalingsgradProsent));
        }
        return this;
    }

    public YtelseAnvist build() {
        return ytelseAnvistEntitet;
    }

}
