package no.nav.foreldrepenger.abakus.felles.sikkerhet;

import no.nav.vedtak.sikkerhet.abac.pdp.RessursDataKey;

enum DuploDataKeys implements RessursDataKey {

    BEHANDLING_STATUS("no.nav.abac.attributter.resource.duplo.behandlingsstatus"),
    FAGSAK_STATUS("no.nav.abac.attributter.resource.duplo.saksstatus"),
    ;

    private final String key;

    DuploDataKeys(String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }
}
