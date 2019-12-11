package no.nav.foreldrepenger.abakus.domene.iay;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektspostType;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.SkatteOgAvgiftsregelType;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

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
     * Beløpet som har blitt utbetalt i perioden
     *
     * @return Beløpet
     */
    Beløp getBeløp();

    YtelseInntektspostType getYtelseType();
    
    /** Periode inntektsposten gjelder. */
    DatoIntervallEntitet getPeriode();

}
