package no.nav.abakus.vedtak.ytelse;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Desimaltall {

    @JsonProperty("verdi")
    private BigDecimal verdi;

    public Desimaltall() {
    }

    public Desimaltall(BigDecimal verdi) {
        setVerdi(verdi);
    }

    public BigDecimal getVerdi() {
        return verdi;
    }

    public void setVerdi(BigDecimal verdi) {

        if (verdi != null) {
            this.verdi = verdi.setScale(2, RoundingMode.HALF_UP);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[verdi=" + verdi + "]";
    }
}
