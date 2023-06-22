package no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Year;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunConsumer;

class SigrunTjenesteTest {

    private static final Long AKTØR_ID = 200000000001L;

    private Year fjoråret = Year.now().minusYears(1);

    private SigrunConsumer sigrunConsumer = Mockito.mock(SigrunConsumer.class);

    private SigrunTjeneste tjeneste = new SigrunTjeneste(sigrunConsumer);

    @Test
    void skal_utvide_opplysningsperioden_med_ett_år_når_siste_år_ikke_er_ferdiglignet() {
        IntervallEntitet opplysningsperiode = IntervallEntitet.fraOgMedTilOgMed(fjoråret.atDay(30).minusYears(2), fjoråret.atDay(150));
        Mockito.when(sigrunConsumer.erÅretFerdiglignet(AKTØR_ID, fjoråret)).thenReturn(false);

        IntervallEntitet justert = tjeneste.justerOpplysningsperiodeNårSisteÅrIkkeErFerdiglignet(AKTØR_ID, opplysningsperiode);
        Assertions.assertThat(justert).isEqualTo(IntervallEntitet.fraOgMedTilOgMed(opplysningsperiode.getFomDato().minusYears(1), opplysningsperiode.getTomDato()));
    }

    @Test
    void skal_ikke_utvide_opplysningsperioden_med_ett_år_når_siste_år_er_ferdiglignet() {
        IntervallEntitet opplysningsperiode = IntervallEntitet.fraOgMedTilOgMed(fjoråret.atDay(30).minusYears(2), fjoråret.atDay(150));
        Mockito.when(sigrunConsumer.erÅretFerdiglignet(AKTØR_ID, fjoråret)).thenReturn(true);

        IntervallEntitet justert = tjeneste.justerOpplysningsperiodeNårSisteÅrIkkeErFerdiglignet(AKTØR_ID, opplysningsperiode);
        Assertions.assertThat(justert).isEqualTo(opplysningsperiode);
    }

    @Test
    void skal_ikke_utvide_opplysningsperioden_med_ett_år_når_opplysningsperioden_er_over_3_kalenderår() {
        IntervallEntitet opplysningsperiode = IntervallEntitet.fraOgMedTilOgMed(fjoråret.atDay(30).minusYears(3), fjoråret.atDay(150));
        Mockito.when(sigrunConsumer.erÅretFerdiglignet(AKTØR_ID, fjoråret)).thenReturn(false);

        IntervallEntitet justert = tjeneste.justerOpplysningsperiodeNårSisteÅrIkkeErFerdiglignet(AKTØR_ID, opplysningsperiode);
        Assertions.assertThat(justert).isEqualTo(opplysningsperiode);
    }
}
