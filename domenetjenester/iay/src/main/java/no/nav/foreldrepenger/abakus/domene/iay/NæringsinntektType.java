package no.nav.foreldrepenger.abakus.domene.iay;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "NæringsinntektType")
@DiscriminatorValue(NæringsinntektType.DISCRIMINATOR)
public class NæringsinntektType extends YtelseType {

    public static final String DISCRIMINATOR = "NÆRINGSINNTEKT_TYPE"; //$NON-NLS-1$
    public static final NæringsinntektType UDEFINERT = new NæringsinntektType("-"); //$NON-NLS-1$

    public NæringsinntektType(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public NæringsinntektType() {
        //hibernate
    }
}
