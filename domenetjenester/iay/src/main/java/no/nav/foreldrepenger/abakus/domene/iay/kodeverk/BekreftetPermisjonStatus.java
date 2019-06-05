package no.nav.foreldrepenger.abakus.domene.iay.kodeverk;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.abakus.kodeverk.Kodeliste;

/**
 * <p>
 * Definerer statuser for bekreftet permisjoner
 * </p>
 */
@Entity(name = "BekreftetPermisjonStatus")
@DiscriminatorValue(BekreftetPermisjonStatus.DISCRIMINATOR)
public class BekreftetPermisjonStatus extends Kodeliste {

    public static final String DISCRIMINATOR = "BEKREFTET_PERMISJON_STATUS";
    public static final BekreftetPermisjonStatus UDEFINERT = new BekreftetPermisjonStatus("-"); //$NON-NLS-1$
    public static final BekreftetPermisjonStatus BRUK_PERMISJON = new BekreftetPermisjonStatus("BRUK_PERMISJON");
    public static final BekreftetPermisjonStatus IKKE_BRUK_PERMISJON = new BekreftetPermisjonStatus("IKKE_BRUK_PERMISJON");
    public static final BekreftetPermisjonStatus UGYLDIGE_PERIODER = new BekreftetPermisjonStatus("UGYLDIGE_PERIODER");

    public BekreftetPermisjonStatus() {}

    public BekreftetPermisjonStatus(String kode) {
        super(kode, DISCRIMINATOR);
    }

}