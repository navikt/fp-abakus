package no.nav.foreldrepenger.abakus.felles;

import no.nav.vedtak.sikkerhet.abac.AbacAttributtType;

/**
 * Lokal AbacAttributtType som er i bruk i FPABAKUS.
 */
public enum AppAbacAttributtType implements AbacAttributtType {

    KOBLING_REFERANSE;

    @Override
    public boolean getMaskerOutput() {
        return false;
    }
}
