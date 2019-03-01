package no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface Stilling {

    LocalDate getFraOgMed();

    LocalDate getTilOgMed();

    BigDecimal getFastStillingsprosent();

    Boolean getVariabelStrilling();

    Boolean getFastOgVariabel();

}
