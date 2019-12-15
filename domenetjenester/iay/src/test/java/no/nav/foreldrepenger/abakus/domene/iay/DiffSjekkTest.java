package no.nav.foreldrepenger.abakus.domene.iay;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import no.nav.foreldrepenger.abakus.diff.TraverseEntityGraphFactory;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjeningBuilder.EgenNæringBuilder;
import no.nav.foreldrepenger.abakus.felles.diff.DiffEntity;
import no.nav.foreldrepenger.abakus.kodeverk.Landkoder;

public class DiffSjekkTest {

    @Test
    public void skal_diffe_entitet_med_landkode() throws Exception {
        var traverser = TraverseEntityGraphFactory.build();
        
        var objCAN = EgenNæringBuilder.ny().medLandkode(Landkoder.CAN).build();
        var objNOR = EgenNæringBuilder.ny().medLandkode(Landkoder.NOR).build();
        
        var differ = new DiffEntity(traverser);
        
        assertThat(differ.areDifferent(objCAN, objNOR)).isTrue();
        assertThat(differ.areDifferent(objCAN, objCAN)).isFalse();
        
    }
    
    @Test
    public void skal_diffe_landkode() throws Exception {
        var traverser = TraverseEntityGraphFactory.build();
        
        var objCAN = Landkoder.CAN;
        var objNOR = Landkoder.NOR;
        
        var differ = new DiffEntity(traverser);
        
        assertThat(differ.areDifferent(objCAN, objNOR)).isTrue();
        assertThat(differ.areDifferent(objCAN, objCAN)).isFalse();
        
    }
}
