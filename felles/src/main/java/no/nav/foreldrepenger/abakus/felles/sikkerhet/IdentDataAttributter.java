package no.nav.foreldrepenger.abakus.felles.sikkerhet;

import no.nav.abakus.iaygrunnlag.AktørIdPersonident;
import no.nav.abakus.iaygrunnlag.FnrPersonident;
import no.nav.abakus.iaygrunnlag.PersonIdent;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;

public final class IdentDataAttributter {

    private IdentDataAttributter() {
        // Skal ikke instansieres
    }

    public static AbacDataAttributter abacAttributterForPersonIdent(PersonIdent person) {
        if (person != null && FnrPersonident.IDENT_TYPE.equals(person.getIdentType())) {
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.FNR, person.getIdent());
        } else if (person != null && AktørIdPersonident.IDENT_TYPE.equals(person.getIdentType())) {
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.AKTØR_ID, person.getIdent());
        } else {
            return AbacDataAttributter.opprett();
        }
    }


}
