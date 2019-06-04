package no.nav.foreldrepenger.abakus.domene.iay;

import java.io.Serializable;

import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;

public interface Arbeidsgiver extends Serializable {

    /**
     * Virksomhets orgnr. Leser bør ta høyde for at dette kan være juridisk orgnr (istdf. virksomhets orgnr).
     */
    OrgNummer getOrgnr();

    /**
     * Hvis arbeidsgiver er en privatperson, returner aktørId for person.
     */
    AktørId getAktørId();

    /**
     * Returneer ident for arbeidsgiver. Kan være Org nummer eller Aktør id (dersom arbeidsgiver er en enkelt person -
     * f.eks. for Frilans el.)
     */
    String getIdentifikator();

    /**
     * Return true hvis arbeidsgiver er en {@link Virksomhet}, false hvis en Person.
     */
    boolean getErVirksomhet();

    /**
     * Return true hvis arbeidsgiver er en {@link AktørId}, ellers false.
     */
    boolean erAktørId();
}
