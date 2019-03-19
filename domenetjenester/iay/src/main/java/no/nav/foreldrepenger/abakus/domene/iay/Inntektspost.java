package no.nav.foreldrepenger.abakus.domene.iay;

import java.time.LocalDate;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektspostType;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.SkatteOgAvgiftsregelType;
import no.nav.foreldrepenger.abakus.typer.Beløp;

public interface Inntektspost {

    /**
     * Underkategori av utbetaling
     * <p>
     * F.eks
     * <ul>
     * <li>Lønn</li>
     * <li>Ytelse</li>
     * <li>Næringsinntekt</li>
     * </ul>
     *
     * @return {@link InntektspostType}
     */
    InntektspostType getInntektspostType();

    /**
     * En kodeverksverdi som angir særskilt beskatningsregel.
     * Den er ikke alltid satt, og kommer fra inntektskomponenten
     *
     * @return {@link SkatteOgAvgiftsregelType}
     */
    SkatteOgAvgiftsregelType getSkatteOgAvgiftsregelType();

    /**
     * Periode start
     *
     * @return første dag i perioden
     */
    LocalDate getFraOgMed();

    /**
     * Periode slutt
     *
     * @return siste dag i perioden
     */
    LocalDate getTilOgMed();

    /**
     * Beløpet som har blitt utbetalt i perioden
     *
     * @return Beløpet
     */
    Beløp getBeløp();

    YtelseType getYtelseType();

}
