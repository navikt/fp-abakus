package no.nav.foreldrepenger.abakus.domene.iay;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import no.nav.abakus.iaygrunnlag.kodeverk.Landkode;
import no.nav.foreldrepenger.abakus.domene.iay.diff.TraverseEntityGraphFactory;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjeningBuilder.EgenNæringBuilder;
import no.nav.foreldrepenger.abakus.felles.diff.DiffEntity;

class DiffSjekkTest {

    @Test
    void skal_diffe_entitet_med_landkode() throws Exception {
        var traverser = TraverseEntityGraphFactory.build();

        var objDNK = EgenNæringBuilder.ny().medLandkode(Landkode.DNK).build();
        var objNOR = EgenNæringBuilder.ny().medLandkode(Landkode.NOR).build();

        var differ = new DiffEntity(traverser);

        assertThat(differ.areDifferent(objDNK, objNOR)).isTrue();
        assertThat(differ.areDifferent(objDNK, objDNK)).isFalse();

    }

    @Test
    void skal_diffe_landkode() throws Exception {
        var traverser = TraverseEntityGraphFactory.build();

        var objDNK = Landkode.DNK;
        var objNOR = Landkode.NOR;

        var differ = new DiffEntity(traverser);

        assertThat(differ.areDifferent(objDNK, objNOR)).isTrue();
        assertThat(differ.areDifferent(objDNK, objDNK)).isFalse();

    }
}
