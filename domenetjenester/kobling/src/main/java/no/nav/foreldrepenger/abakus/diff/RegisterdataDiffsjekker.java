package no.nav.foreldrepenger.abakus.diff;

import no.nav.foreldrepenger.abakus.felles.diff.DiffEntity;
import no.nav.foreldrepenger.abakus.felles.diff.TraverseGraph;

public class RegisterdataDiffsjekker {
    private DiffEntity diffEntity;
    private TraverseGraph traverseEntityGraph;

    public RegisterdataDiffsjekker() {
        this(true);
    }

    public RegisterdataDiffsjekker(boolean onlyCheckTrackedFields) {
        traverseEntityGraph = TraverseEntityGraphFactory.build(onlyCheckTrackedFields);
        diffEntity = new DiffEntity(traverseEntityGraph);
    }

    public DiffEntity getDiffEntity() {
        return diffEntity;
    }
}
