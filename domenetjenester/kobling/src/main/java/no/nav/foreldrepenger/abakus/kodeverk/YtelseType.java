package no.nav.foreldrepenger.abakus.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "YtelseType")
@DiscriminatorValue(YtelseType.DISCRIMINATOR)
public class YtelseType extends Kodeliste {

    public static final String DISCRIMINATOR = "FAGSAK_YTELSE"; //$NON-NLS-1$
    public static final YtelseType ENGANGSTØNAD = new YtelseType("ES"); //$NON-NLS-1$
    public static final YtelseType FORELDREPENGER = new YtelseType("FP"); //$NON-NLS-1$

    public static final YtelseType SVANGERSKAPSPENGER = new YtelseType("SVP"); //$NON-NLS-1$
    public static final YtelseType UDEFINERT = new YtelseType("-"); //$NON-NLS-1$

    YtelseType() {
        // Hibernate trenger den
    }

    public YtelseType(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
