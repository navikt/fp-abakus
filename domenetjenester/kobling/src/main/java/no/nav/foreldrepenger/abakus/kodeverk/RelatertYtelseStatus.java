package no.nav.foreldrepenger.abakus.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "RelatertYtelseStatus")
@DiscriminatorValue(RelatertYtelseStatus.DISCRIMINATOR)
public class RelatertYtelseStatus extends Kodeliste {

    public static final String DISCRIMINATOR = "RELATERT_YTELSE_STATUS"; //$NON-NLS-1$
    //Statuser fra Arena
    public static final RelatertYtelseStatus AVSLU = new RelatertYtelseStatus("AVSLU"); //$NON-NLS-1$
    public static final RelatertYtelseStatus GODKJ = new RelatertYtelseStatus("GODKJ"); //$NON-NLS-1$
    public static final RelatertYtelseStatus INNST = new RelatertYtelseStatus("INNST"); //$NON-NLS-1$
    public static final RelatertYtelseStatus IVERK = new RelatertYtelseStatus("IVERK"); //$NON-NLS-1$
    public static final RelatertYtelseStatus MOTAT = new RelatertYtelseStatus("MOTAT"); //$NON-NLS-1$
    public static final RelatertYtelseStatus OPPRE = new RelatertYtelseStatus("OPPRE"); //$NON-NLS-1$
    public static final RelatertYtelseStatus REGIS = new RelatertYtelseStatus("REGIS"); //$NON-NLS-1$

    //Statuser far Infotrygd
    public static final RelatertYtelseStatus IKKE_PÅBEGYNT = new RelatertYtelseStatus("IP"); //$NON-NLS-1$
    public static final RelatertYtelseStatus UNDER_BEHANDLING = new RelatertYtelseStatus("UB"); //$NON-NLS-1$
    public static final RelatertYtelseStatus SENDT_TIL_SAKSBEHANDLER = new RelatertYtelseStatus("SG"); //$NON-NLS-1$
    public static final RelatertYtelseStatus UNDERKJENT_AV_SAKSBEHANDLER = new RelatertYtelseStatus("UK"); //$NON-NLS-1$
    public static final RelatertYtelseStatus RETUNERT = new RelatertYtelseStatus("RT"); //$NON-NLS-1$
    public static final RelatertYtelseStatus SENDT = new RelatertYtelseStatus("ST"); //$NON-NLS-1$
    public static final RelatertYtelseStatus VIDERESENDT_DIREKTORATET = new RelatertYtelseStatus("VD"); //$NON-NLS-1$
    public static final RelatertYtelseStatus VENTER_IVERKSETTING = new RelatertYtelseStatus("VI"); //$NON-NLS-1$
    public static final RelatertYtelseStatus VIDERESENDT_TRYGDERETTEN = new RelatertYtelseStatus("VT"); //$NON-NLS-1$

    public static final RelatertYtelseStatus LØPENDE_VEDTAK = new RelatertYtelseStatus("L"); //$NON-NLS-1$
    public static final RelatertYtelseStatus IKKE_STARTET = new RelatertYtelseStatus("I"); //$NON-NLS-1$
    public static final RelatertYtelseStatus AVSLUTTET_IT = new RelatertYtelseStatus("A"); //$NON-NLS-1$

    RelatertYtelseStatus() {
        // Hibernate trenger den
    }

    private RelatertYtelseStatus(String kode) {
        super(kode, DISCRIMINATOR);
    }

}
