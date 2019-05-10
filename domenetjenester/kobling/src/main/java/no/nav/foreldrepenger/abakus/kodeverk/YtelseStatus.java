package no.nav.foreldrepenger.abakus.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "YtelseStatus")
@DiscriminatorValue(YtelseStatus.DISCRIMINATOR)
public class YtelseStatus extends Kodeliste {

    public static final String DISCRIMINATOR = "YTELSE_STATUS";
    public static final YtelseStatus OPPRETTET = new YtelseStatus("OPPR");
    public static final YtelseStatus UNDER_BEHANDLING = new YtelseStatus("UBEH");
    public static final YtelseStatus LØPENDE = new YtelseStatus("LOP");
    public static final YtelseStatus AVSLUTTET = new YtelseStatus("AVSLU");
    public static final YtelseStatus UDEFINERT = new YtelseStatus("-");

    YtelseStatus() {
        // Hibernate trenger den
    }

    private YtelseStatus(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
