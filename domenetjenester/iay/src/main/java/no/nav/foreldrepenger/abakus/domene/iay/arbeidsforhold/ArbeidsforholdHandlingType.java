package no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.abakus.kodeverk.Kodeliste;

/**
 * <h3>Internt kodeverk</h3>
 * Definerer typer av handlinger en saksbehandler kan gjøre vedrørende et arbeidsforhold
 * <p>
 */
@Entity(name = "ArbeidsforholdHandlingType")
@DiscriminatorValue(ArbeidsforholdHandlingType.DISCRIMINATOR)
public class ArbeidsforholdHandlingType extends Kodeliste {

    public static final String DISCRIMINATOR = "ARBEIDSFORHOLD_HANDLING_TYPE";

    public static final ArbeidsforholdHandlingType UDEFINERT = new ArbeidsforholdHandlingType("-"); //$NON-NLS-1$
    public static final ArbeidsforholdHandlingType BRUK = new ArbeidsforholdHandlingType("BRUK"); //$NON-NLS-1$
    public static final ArbeidsforholdHandlingType NYTT_ARBEIDSFORHOLD = new ArbeidsforholdHandlingType("NYTT_ARBEIDSFORHOLD"); //$NON-NLS-1$
    public static final ArbeidsforholdHandlingType BRUK_UTEN_INNTEKTSMELDING = new ArbeidsforholdHandlingType("BRUK_UTEN_INNTEKTSMELDING"); //$NON-NLS-1$
    public static final ArbeidsforholdHandlingType IKKE_BRUK = new ArbeidsforholdHandlingType("IKKE_BRUK"); //$NON-NLS-1$
    public static final ArbeidsforholdHandlingType SLÅTT_SAMMEN_MED_ANNET = new ArbeidsforholdHandlingType("SLÅTT_SAMMEN_MED_ANNET"); //$NON-NLS-1$
    public static final ArbeidsforholdHandlingType LAGT_TIL_AV_SAKSBEHANDLER = new ArbeidsforholdHandlingType("LAGT_TIL_AV_SAKSBEHANDLER"); //$NON-NLS-1$
    
    /**
     * @deprecated
     * TODO: Refaktorer til å kun bruke variabelen i ArbeidsforholdOverstyring (PFP-8119)
     */
    public static final ArbeidsforholdHandlingType BRUK_MED_OVERSTYRT_PERIODE = new ArbeidsforholdHandlingType("BRUK_MED_OVERSTYRT_PERIODE"); //$NON-NLS-1$

    
    /**
     * @deprecated
     * TODO: Refaktorer til variable i ArbeidsforholdOverstyring (PFP-8118)
     */
    public static final ArbeidsforholdHandlingType INNTEKT_IKKE_MED_I_BG = new ArbeidsforholdHandlingType("INNTEKT_IKKE_MED_I_BG"); //$NON-NLS-1$
    
    public ArbeidsforholdHandlingType() {
    }

    public ArbeidsforholdHandlingType(String kode) {
        super(kode, DISCRIMINATOR);
    }

}
