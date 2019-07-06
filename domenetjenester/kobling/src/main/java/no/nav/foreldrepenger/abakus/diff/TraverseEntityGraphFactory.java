package no.nav.foreldrepenger.abakus.diff;

import java.util.function.Function;

import no.nav.foreldrepenger.abakus.felles.diff.TraverseEntityGraph;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.kodeverk.Kodeliste;
import no.nav.foreldrepenger.abakus.kodeverk.KodeverkTabell;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.jpa.tid.ÅpenDatoIntervallEntitet;

/*
 * Legger denne sammen med RootClass
 */
public final class TraverseEntityGraphFactory {
    private TraverseEntityGraphFactory() {
    }

    public static TraverseEntityGraph build(boolean medChangedTrackedOnly) {
        return build(medChangedTrackedOnly, TraverseEntityGraph.NO_FILTER);
    }
    
    public static TraverseEntityGraph build(boolean medChangedTrackedOnly, Function<Object, Boolean> inclusionFilter) {
        /* default oppsett for behandlingslager. */
        TraverseEntityGraph traverseEntityGraph = new TraverseEntityGraph(); // NOSONAR
        traverseEntityGraph.setIgnoreNulls(true);
        traverseEntityGraph.setOnlyCheckTrackedFields(medChangedTrackedOnly);
        traverseEntityGraph.addLeafClasses(KodeverkTabell.class);
        traverseEntityGraph.addLeafClasses(Kodeliste.class);
        traverseEntityGraph.addLeafClasses(DatoIntervallEntitet.class, ÅpenDatoIntervallEntitet.class);
        traverseEntityGraph.addRootClasses(Kobling.class);
        traverseEntityGraph.setInclusionFilter(inclusionFilter);
        return traverseEntityGraph;
    }

    public static TraverseEntityGraph build() {
        return build(false);
    }
}
