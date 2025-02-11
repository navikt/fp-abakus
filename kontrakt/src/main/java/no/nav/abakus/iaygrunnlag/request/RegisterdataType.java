package no.nav.abakus.iaygrunnlag.request;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import no.nav.abakus.iaygrunnlag.kodeverk.Kodeverdi;

@JsonAutoDetect(
        getterVisibility = Visibility.NONE,
        setterVisibility = Visibility.NONE,
        fieldVisibility = Visibility.ANY)
public enum RegisterdataType implements Kodeverdi {
    ARBEIDSFORHOLD,
    LIGNET_NÆRING,
    INNTEKT_PENSJONSGIVENDE,
    INNTEKT_BEREGNINGSGRUNNLAG,
    INNTEKT_SAMMENLIGNINGSGRUNNLAG,
    YTELSE,
    ;

    public static RegisterdataType fraKode(String kode) {
        return kode != null ? RegisterdataType.valueOf(kode) : null;
    }

    @Override
    public String getKode() {
        return name();
    }
}
