package no.nav.foreldrepenger.abakus.kobling;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.abakus.kodeverk.Kodeliste;

@Entity(name = "KoblingYtelseType")
@DiscriminatorValue(KoblingYtelseType.DISCRIMINATOR)
public class KoblingYtelseType extends Kodeliste {
    public static final String DISCRIMINATOR = "KOBLING_YTELSE_TYPE";

    public static final KoblingYtelseType UDEFINERT = new KoblingYtelseType("-"); //$NON-NLS-1$
    public static final KoblingYtelseType FORELDREPENGER = new KoblingYtelseType("FP"); //$NON-NLS-1$
    public static final KoblingYtelseType ENGANGSTØNAD = new KoblingYtelseType("ES"); //$NON-NLS-1$
    public static final KoblingYtelseType SVANGERSKAPSPENGER = new KoblingYtelseType("SVP"); //$NON-NLS-1$

    public KoblingYtelseType() {
    }

    private KoblingYtelseType(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
