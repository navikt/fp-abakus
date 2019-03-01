package no.nav.foreldrepenger.abakus.domene.virksomhet;

import java.time.LocalDate;

public interface Virksomhet {

    String getOrgnr();

    String getNavn();

    LocalDate getRegistrert();

    LocalDate getOppstart();

    LocalDate getAvslutt();

    boolean skalRehentes();

    Organisasjonstype getOrganisasjonstype();

}
