package no.nav.foreldrepenger.abakus.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "YtelseType")
@DiscriminatorValue(YtelseType.DISCRIMINATOR)
public class YtelseType extends Kodeliste {

    public static final String DISCRIMINATOR = "FAGSAK_YTELSE_TYPE"; //$NON-NLS-1$
    public static final YtelseType ENGANGSSTØNAD = new YtelseType("ES"); //$NON-NLS-1$
    public static final YtelseType FORELDREPENGER = new YtelseType("FP"); //$NON-NLS-1$

    public static final YtelseType SVANGERSKAPSPENGER = new YtelseType("SVP"); //$NON-NLS-1$
    public static final YtelseType UDEFINERT = new YtelseType("-"); //$NON-NLS-1$
    public static final YtelseType ARBEIDSAVKLARINGSPENGER = new YtelseType("AAP");
    public static final YtelseType DAGPENGER = new YtelseType("DAG");
    public static final YtelseType SYKEPENGER = new YtelseType("SP");
    public static final YtelseType PÅRØRENDESYKDOM = new YtelseType("PS");
    public static final YtelseType ENSLIG_FORSØRGER = new YtelseType("EF");

    YtelseType() {
        // Hibernate trenger den
    }

    public YtelseType(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
