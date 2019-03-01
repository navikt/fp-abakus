package no.nav.foreldrepenger.abakus.domene.virksomhet;

import java.util.Optional;

public interface VirksomhetRepository {

    Optional<Virksomhet> hent(String orgnr);

    void lagre(Virksomhet virksomhet);
}
