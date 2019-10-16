package no.nav.foreldrepenger.abakus.domene.iay.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.abakus.kodeverk.Kodeliste;

@Entity(name = "RelatertYtelseTema")
@DiscriminatorValue(RelatertYtelseTema.DISCRIMINATOR)
public class RelatertYtelseTema extends Kodeliste {

    public static final String DISCRIMINATOR = "RELATERT_YTELSE_TEMA"; //$NON-NLS-1$

    public static final RelatertYtelseTema FORELDREPENGER_TEMA = new RelatertYtelseTema("FA"); //$NON-NLS-1$
    public static final RelatertYtelseTema ENSLIG_FORSORGER_TEMA = new RelatertYtelseTema("EF"); //$NON-NLS-1$
    public static final RelatertYtelseTema SYKEPENGER_TEMA = new RelatertYtelseTema("SP"); //$NON-NLS-1$
    public static final RelatertYtelseTema PÅRØRENDE_SYKDOM_TEMA = new RelatertYtelseTema("BS"); //$NON-NLS-1$

    RelatertYtelseTema() {
        // Hibernate trenger den
    }

    private RelatertYtelseTema(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
