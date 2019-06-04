package no.nav.foreldrepenger.abakus.domene.iay;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "OffentligYtelseType")
@DiscriminatorValue(OffentligYtelseType.DISCRIMINATOR)
public class OffentligYtelseType extends YtelseType {

    public static final String DISCRIMINATOR = "YTELSE_FRA_OFFENTLIGE"; //$NON-NLS-1$
    public static final OffentligYtelseType UDEFINERT = new OffentligYtelseType("-"); //$NON-NLS-1$
    public static final OffentligYtelseType FORELDREPENGER = new OffentligYtelseType("FORELDREPENGER"); //$NON-NLS-1$

    public OffentligYtelseType(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public OffentligYtelseType() {
        //hibernate
    }
}
