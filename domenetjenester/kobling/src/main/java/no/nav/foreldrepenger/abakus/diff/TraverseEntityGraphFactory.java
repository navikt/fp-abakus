package no.nav.foreldrepenger.abakus.diff;

import java.util.function.Function;

import no.nav.foreldrepenger.abakus.felles.diff.TraverseGraph;
import no.nav.foreldrepenger.abakus.felles.diff.TraverseGraphConfig;
import no.nav.foreldrepenger.abakus.felles.diff.TraverseJpaEntityGraphConfig;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.kodeverk.Kodeverdi;

/*
 * Legger denne sammen med RootClass
 */
public final class TraverseEntityGraphFactory {
    private TraverseEntityGraphFactory() {
    }

    public static TraverseGraph build(boolean medChangedTrackedOnly) {
        return build(medChangedTrackedOnly, TraverseGraphConfig.NO_FILTER);
    }
    
    public static TraverseGraph build(boolean medChangedTrackedOnly, Function<Object, Boolean> inclusionFilter) {
        /* default oppsett for behandlingslager. */
        var config = new TraverseJpaEntityGraphConfig(); // NOSONAR
        config.setIgnoreNulls(true);
        config.setOnlyCheckTrackedFields(medChangedTrackedOnly);
        
        config.addLeafClasses(Kodeverdi.class);
        config.addLeafClasses(IntervallEntitet.class);
        
        config.addRootClasses(Kobling.class);
        config.setInclusionFilter(inclusionFilter);
        return new TraverseGraph(config);
    }

    public static TraverseGraph build() {
        return build(false);
    }
}
