package no.nav.foreldrepenger.abakus.domene.iay.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.abakus.kodeverk.Kodeliste;

@Entity(name = "InntektsKilde")
@DiscriminatorValue(InntektsKilde.DISCRIMINATOR)
public class InntektsKilde extends Kodeliste {

    public static final String DISCRIMINATOR = "INNTEKTS_KILDE"; //$NON-NLS-1$

    public static final InntektsKilde UDEFINERT = new InntektsKilde("-"); //$NON-NLS-1$
    public static final InntektsKilde INNTEKT_OPPTJENING = new InntektsKilde("INNTEKT_OPPTJENING"); //$NON-NLS-1$
    public static final InntektsKilde INNTEKT_BEREGNING = new InntektsKilde("INNTEKT_BEREGNING"); //$NON-NLS-1$
    public static final InntektsKilde INNTEKT_SAMMENLIGNING = new InntektsKilde("INNTEKT_SAMMENLIGNING"); //$NON-NLS-1$
    public static final InntektsKilde SIGRUN = new InntektsKilde("SIGRUN"); //$NON-NLS-1$

    public InntektsKilde(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public InntektsKilde() {
        //hibernate
    }
}
