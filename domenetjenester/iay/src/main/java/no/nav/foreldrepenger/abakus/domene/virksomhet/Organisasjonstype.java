package no.nav.foreldrepenger.abakus.domene.virksomhet;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.abakus.kodeverk.Kodeliste;

@Entity(name = "OrganisasjonsType")
@DiscriminatorValue(Organisasjonstype.DISCRIMINATOR)
public class Organisasjonstype extends Kodeliste {

    public static final String DISCRIMINATOR = "ORGANISASJONSTYPE";

    public static final Organisasjonstype JURIDISK_ENHET = new Organisasjonstype("JURIDISK_ENHET"); //$NON-NLS-1$
    public static final Organisasjonstype VIRKSOMHET = new Organisasjonstype("VIRKSOMHET"); //$NON-NLS-1$
    public static final Organisasjonstype KUNSTIG = new Organisasjonstype("KUNSTIG"); //$NON-NLS-1$

    public static final Organisasjonstype UDEFINERT = new Organisasjonstype("-"); //$NON-NLS-1$

    /** Orgnr for KUNSTIG organisasjoner. Går sammen med Organisasjonstype#KUNSTIG. (p.t. kun en kunstig organisasjon som holder på arbeidsforhold lagt til av saksbehandler.)*/
    public static final String KUNSTIG_ORG = "342352362";

    
    @SuppressWarnings("unused")
    private Organisasjonstype() {
        // Hibernate
    }

    public Organisasjonstype(String kode) {
        super(kode, DISCRIMINATOR);
    }
    
    public static boolean erKunstig(String orgNr) {
        return KUNSTIG_ORG.equals(orgNr);
    }
}
