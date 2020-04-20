package no.nav.foreldrepenger.abakus.registerdata.fagsakytelser.omp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import no.nav.foreldrepenger.abakus.registerdata.IAYRegisterInnhentingTjeneste;

public class OmsorgspengerIAYRegisterInnhentingTjenesteImplTest {

    private IAYRegisterInnhentingTjeneste tjeneste = new OmsorgspengerIAYRegisterInnhentingTjenesteImpl();

    @Test
    public void skal_hente_inn_næringsinntekter() {
        assertThat(tjeneste.skalInnhenteNæringsInntekterFor(null)).isTrue();
    }
}
