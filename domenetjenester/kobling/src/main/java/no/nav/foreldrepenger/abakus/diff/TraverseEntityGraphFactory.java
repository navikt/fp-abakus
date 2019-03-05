package no.nav.foreldrepenger.abakus.diff;

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
        /* default oppsett for behandlingslager. */
        TraverseEntityGraph traverseEntityGraph = new TraverseEntityGraph(); // NOSONAR
        traverseEntityGraph.setIgnoreNulls(true);
        traverseEntityGraph.setOnlyCheckTrackedFields(medChangedTrackedOnly);
        traverseEntityGraph.addLeafClasses(KodeverkTabell.class);
        traverseEntityGraph.addLeafClasses(Kodeliste.class);
        traverseEntityGraph.addLeafClasses(DatoIntervallEntitet.class, ÅpenDatoIntervallEntitet.class);
        traverseEntityGraph.addRootClasses(Kobling.class);
        return traverseEntityGraph;
    }

    public static TraverseEntityGraph build() {
        return build(false);
    }
}
