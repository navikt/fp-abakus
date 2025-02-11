package no.nav.foreldrepenger.abakus.app.vedlikehold;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class BegrunnelseVaskerTest {

    @Test
    void skal_vaske_begrunnelse() {

        var uvasket =
            "Gått fra \n" + "usammenhengende vikariater med varierende arbeidsomfang,til fast kontrakt med mer stabil og \n" + "høyere inntekt.";


        var forventet =
            "Gått fra " + "usammenhengende vikariater med varierende arbeidsomfang,til fast kontrakt med mer stabil og " + "høyere inntekt.";

        var vasket = BegrunnelseVasker.vask(uvasket);

        assertThat(vasket).isEqualTo(forventet);
    }
}
