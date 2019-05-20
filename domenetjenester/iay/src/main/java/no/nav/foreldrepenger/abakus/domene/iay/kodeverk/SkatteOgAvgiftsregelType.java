package no.nav.foreldrepenger.abakus.domene.iay.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.abakus.kodeverk.Kodeliste;

@Entity(name = "SkatteOgAvgiftsregelType")
@DiscriminatorValue(SkatteOgAvgiftsregelType.DISCRIMINATOR)
public class SkatteOgAvgiftsregelType extends Kodeliste {
    public static final String DISCRIMINATOR = "SKATTE_OG_AVGIFTSREGEL"; //$NON-NLS-1$

    public static final SkatteOgAvgiftsregelType UDEFINERT = new SkatteOgAvgiftsregelType("-"); //$NON-NLS-1$
    public static final SkatteOgAvgiftsregelType NETTOLOENN_FOR_SJOEFOLK = new SkatteOgAvgiftsregelType("NETTOLØNN_FOR_SJØFOLK"); //$NON-NLS-1$
    public static final SkatteOgAvgiftsregelType SAERSKILT_FRADRAG_FOR_SJOEFOLK = new SkatteOgAvgiftsregelType("SÆRSKILT_FRADRAG_FOR_SJØFOLK"); //$NON-NLS-1$

    public SkatteOgAvgiftsregelType(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public SkatteOgAvgiftsregelType() {
        //hibernate
    }

}
