package no.nav.foreldrepenger.abakus.domene.iay.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.abakus.kodeverk.Kodeliste;

@Entity(name = "PermisjonsbeskrivelseType")
@DiscriminatorValue(PermisjonsbeskrivelseType.DISCRIMINATOR)
public class PermisjonsbeskrivelseType extends Kodeliste{
    public static final String DISCRIMINATOR = "PERMISJONSBESKRIVELSE_TYPE"; //$NON-NLS-1$

    public static final PermisjonsbeskrivelseType UDEFINERT = new PermisjonsbeskrivelseType("-"); //$NON-NLS-1$
    public static final PermisjonsbeskrivelseType UTDANNINGSPERMISJON = new PermisjonsbeskrivelseType("UTDANNINGSPERMISJON"); //$NON-NLS-1$

    private PermisjonsbeskrivelseType(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public PermisjonsbeskrivelseType() {
        //hibernate
    }
}
