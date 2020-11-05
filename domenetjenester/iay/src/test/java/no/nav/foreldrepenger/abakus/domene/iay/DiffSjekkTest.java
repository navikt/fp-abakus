package no.nav.foreldrepenger.abakus.domene.iay;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import no.nav.abakus.iaygrunnlag.kodeverk.Landkode;
import no.nav.foreldrepenger.abakus.domene.iay.diff.TraverseEntityGraphFactory;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjeningBuilder.EgenNæringBuilder;
import no.nav.foreldrepenger.abakus.felles.diff.DiffEntity;

public class DiffSjekkTest {

    @Test
    public void skal_diffe_entitet_med_landkode() throws Exception {
        var traverser = TraverseEntityGraphFactory.build();

        var objCAN = EgenNæringBuilder.ny().medLandkode(Landkode.CAN).build();
        var objNOR = EgenNæringBuilder.ny().medLandkode(Landkode.NOR).build();

        var differ = new DiffEntity(traverser);

        assertThat(differ.areDifferent(objCAN, objNOR)).isTrue();
        assertThat(differ.areDifferent(objCAN, objCAN)).isFalse();

    }

    @Test
    public void skal_diffe_landkode() throws Exception {
        var traverser = TraverseEntityGraphFactory.build();

        var objCAN = Landkode.CAN;
        var objNOR = Landkode.NOR;

        var differ = new DiffEntity(traverser);

        assertThat(differ.areDifferent(objCAN, objNOR)).isTrue();
        assertThat(differ.areDifferent(objCAN, objCAN)).isFalse();

    }
}
