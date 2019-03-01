package no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.virksomhet;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.foreldrepenger.abakus.domene.virksomhet.Virksomhet;

public interface VirksomhetTjeneste {

    /**
     * Henter informasjon fra Enhetsregisteret hvis applikasjonen ikke kjenner til orgnr eller har data som er eldre enn 24 timer.
     *
     * @param orgNummer orgnummeret
     * @return relevant informasjon om virksomheten.
     * @throws IllegalArgumentException ved forespørsel om orgnr som ikke finnes i enhetsreg
     */
    Virksomhet hentOgLagreOrganisasjon(String orgNummer);


    /**
     * Henter informasjon fra databasen til VL.
     * Benyttes til DTO-tjenester.
     *
     * @param orgNummer orgnummeret
     * @return relevant informasjon om virksomheten.
     */
    Optional<Virksomhet> finnOrganisasjon(String orgNummer);


    /**
     * Henter informasjon fra Enhetsregisteret
     *
     * @param orgNummer orgnummeret
     * @return true (når virksomheten er orgledd)
     */
    boolean sjekkOmVirksomhetErOrgledd(String orgNummer);

    /**
     * Henter informasjon fra Enhetsregisteret hvis applikasjonen ikke kjenner til orgnr eller har data som er eldre enn 24 timer.
     * Prøver og utlede faktisk virksomhet hvis oppgitt orgNummer refererer til et juridiskOrgnummer (gjelder 1 til 1 forhold)
     * Vil lagre ned virksomheten med juridiskOrgNummer hvis det ikke er mulig og utlede faktisk orgNummer (gjelder 1 til mange forhold)
     *
     * @param orgNummer orgnummeret
     * @return relevant informasjon om virksomheten.
     * @throws IllegalArgumentException ved forespørsel om orgnr som ikke finnes i enhetsreg
     */
    Virksomhet hentOgLagreOrganisasjonMedHensynTilJuridisk(String orgNummer, LocalDate hentedato);
}
