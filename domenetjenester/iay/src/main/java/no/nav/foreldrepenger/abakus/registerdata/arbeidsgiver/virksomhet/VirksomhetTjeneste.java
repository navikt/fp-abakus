package no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.virksomhet;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.abakus.domene.virksomhet.Organisasjonstype;
import no.nav.foreldrepenger.abakus.domene.virksomhet.Virksomhet;
import no.nav.foreldrepenger.abakus.domene.virksomhet.VirksomhetAlleredeLagretException;
import no.nav.foreldrepenger.abakus.domene.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.abakus.domene.virksomhet.VirksomhetRepository;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonOrganisasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonUgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.JuridiskEnhet;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Orgledd;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.OrgnrForOrganisasjon;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.UnntakForOrgnr;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.UstrukturertNavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonResponse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentVirksomhetsOrgnrForJuridiskOrgnrBolkResponse;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.integrasjon.organisasjon.OrganisasjonConsumer;
import no.nav.vedtak.felles.integrasjon.organisasjon.hent.HentOrganisasjonForJuridiskRequest;
import no.nav.vedtak.felles.integrasjon.organisasjon.hent.HentOrganisasjonRequest;


@ApplicationScoped
public class VirksomhetTjeneste {

    private static final String TJENESTE = "Organisasjon";
    private static final Logger LOGGER = LoggerFactory.getLogger(VirksomhetTjeneste.class);
    private OrganisasjonConsumer organisasjonConsumer;
    private VirksomhetRepository virksomhetRepository;

    public VirksomhetTjeneste() {
        // CDI
    }

    @Inject
    public VirksomhetTjeneste(OrganisasjonConsumer organisasjonConsumer,
                              VirksomhetRepository virksomhetRepository) {
        this.organisasjonConsumer = organisasjonConsumer;
        this.virksomhetRepository = virksomhetRepository;
    }

    /**
     * Henter informasjon fra Enhetsregisteret hvis applikasjonen ikke kjenner til orgnr eller har data som er eldre enn 24 timer.
     *
     * @param orgNummer orgnummeret
     * @return relevant informasjon om virksomheten.
     * @throws IllegalArgumentException ved forespørsel om orgnr som ikke finnes i enhetsreg
     */
    public Virksomhet hentOgLagreOrganisasjon(String orgNummer) {
        final Optional<Virksomhet> virksomhetOptional = virksomhetRepository.hent(orgNummer);
        if (virksomhetOptional.isEmpty() || virksomhetOptional.get().skalRehentes()) {
            HentOrganisasjonResponse response = hentOrganisasjon(orgNummer);
            return lagreVirksomhet(virksomhetOptional, response);
        }
        return virksomhetOptional.orElseThrow(IllegalArgumentException::new);
    }


    /**
     * Henter informasjon fra Enhetsregisteret
     *
     * @param orgNummer orgnummeret
     * @return true (når virksomheten er orgledd)
     */
    public boolean sjekkOmVirksomhetErOrgledd(String orgNummer) {
        Organisasjon organisasjon = hentOrganisasjon(orgNummer).getOrganisasjon();
        return organisasjon instanceof Orgledd;
    }

    /**
     * Henter informasjon fra Enhetsregisteret hvis applikasjonen ikke kjenner til orgnr eller har data som er eldre enn 24 timer.
     * Prøver og utlede faktisk virksomhet hvis oppgitt orgNummer refererer til et juridiskOrgnummer (gjelder 1 til 1 forhold)
     * Vil lagre ned virksomheten med juridiskOrgNummer hvis det ikke er mulig og utlede faktisk orgNummer (gjelder 1 til mange forhold)
     *
     * @param orgNummer orgnummeret
     * @return relevant informasjon om virksomheten.
     * @throws IllegalArgumentException ved forespørsel om orgnr som ikke finnes i enhetsreg
     */
    public Virksomhet hentOgLagreOrganisasjonMedHensynTilJuridisk(String orgNummer, LocalDate hentedato) {
        final Optional<Virksomhet> virksomhetOptional = virksomhetRepository.hent(orgNummer);
        if (virksomhetOptional.isEmpty() || virksomhetOptional.get().skalRehentes()) {
            HentOrganisasjonResponse response = hentOrganisasjon(orgNummer);
            Organisasjon organisasjon = response.getOrganisasjon();
            if (organisasjon instanceof JuridiskEnhet) {
                Optional<String> virksomhetsOrgNrOpt = hentOrgnummerKnyttetTilJuridiskOrgNr(orgNummer, hentedato);
                if (virksomhetsOrgNrOpt.isPresent()) {
                    // fant rikitg virksomhet
                    String virksomhetsOrgNr = virksomhetsOrgNrOpt.get();
                    HentOrganisasjonResponse hentOrganisasjonResponse = hentOrganisasjon(virksomhetsOrgNr);
                    final Optional<Virksomhet> virksomhetsMedOrgNrOpt = virksomhetRepository.hent(virksomhetsOrgNr);
                    LOGGER.info("Lagret inntektene på virksomhet({}), var opprinnelig registret på juridisk enhet({})", virksomhetsOrgNr, orgNummer);
                    return lagreVirksomhet(virksomhetsMedOrgNrOpt, hentOrganisasjonResponse);
                } else {
                    // må lage virksomhet med juridiskOrgNr
                    LOGGER.info("Lagret inntektene på juridisk enhet({})", orgNummer);
                    return lagreVirksomhet(virksomhetOptional, response);
                }
            } else if (organisasjon instanceof no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Virksomhet) {
                return lagreVirksomhet(virksomhetOptional, response);
            } else {
                // støtter ikke orgledd
                throw OrganisasjonTjenesteFeil.FACTORY.organisasjonErOrgledd(TJENESTE).toException();
            }
        }
        return virksomhetOptional.orElseThrow(IllegalArgumentException::new);
    }

    private Virksomhet lagreVirksomhet(Optional<Virksomhet> virksomhetOptional, HentOrganisasjonResponse response) {
        final Virksomhet virksomhet = mapOrganisasjonResponseToOrganisasjon(response.getOrganisasjon(), virksomhetOptional);
        try {
            virksomhetRepository.lagre(virksomhet);
            return virksomhet;
        } catch (VirksomhetAlleredeLagretException exception) {
            return virksomhet;
        }
    }

    private HentOrganisasjonResponse hentOrganisasjon(String orgNummer) {
        HentOrganisasjonRequest request = new HentOrganisasjonRequest(orgNummer);
        try {
            return organisasjonConsumer.hentOrganisasjon(request);
        } catch (HentOrganisasjonOrganisasjonIkkeFunnet e) {
            throw OrganisasjonTjenesteFeil.FACTORY.organisasjonIkkeFunnet(orgNummer, e).toException();
        } catch (HentOrganisasjonUgyldigInput e) {
            throw OrganisasjonTjenesteFeil.FACTORY.ugyldigInput(TJENESTE, orgNummer, e).toException();
        }
    }

    private Optional<String> hentOrgnummerKnyttetTilJuridiskOrgNr(String orgNummer, LocalDate hentedato) {
        HentOrganisasjonForJuridiskRequest request = new HentOrganisasjonForJuridiskRequest(orgNummer, hentedato);

        HentVirksomhetsOrgnrForJuridiskOrgnrBolkResponse response = organisasjonConsumer.hentOrganisajonerForJuridiskOrgnr(request);
        List<UnntakForOrgnr> unntakForOrgnrListe = response.getUnntakForOrgnrListe();

        Optional<UnntakForOrgnr> unntakOpt = unntakForOrgnrListe.stream().findAny();
        if (unntakOpt.isPresent()) {
            LOGGER.info("Unntaksmelding fra hentOrganisajonerForJuridiskOrgnr: {}", unntakOpt.get().getUnntaksmelding());
            return Optional.empty();
        }
        //kan gjøre det sånn da FPSAK bare spør på ett orgNummer
        return response.getOrgnrForOrganisasjonListe()
            .stream()
            .findFirst()
            .map(OrgnrForOrganisasjon::getOrganisasjonsnummer);
    }

    private Virksomhet mapOrganisasjonResponseToOrganisasjon(Organisasjon responsOrganisasjon, Optional<Virksomhet> virksomhetOptional) {
        final VirksomhetEntitet.Builder builder = getBuilder(virksomhetOptional)
            .medNavn(((UstrukturertNavn) responsOrganisasjon.getNavn()).getNavnelinje().stream().filter(it -> !it.isEmpty())
                .reduce("", (a, b) -> a + " " + b).trim())
            .medRegistrert(DateUtil.convertToLocalDate(responsOrganisasjon.getOrganisasjonDetaljer().getRegistreringsDato()));
        if (!virksomhetOptional.isPresent()) {
            builder.medOrgnr(responsOrganisasjon.getOrgnummer());
        }
        if (responsOrganisasjon instanceof no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Virksomhet) {
            no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Virksomhet virksomhet = (no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Virksomhet) responsOrganisasjon;

            if (virksomhet.getVirksomhetDetaljer().getOppstartsdato() != null) {
                builder.medOppstart(DateUtil.convertToLocalDate(virksomhet.getVirksomhetDetaljer().getOppstartsdato()));
            }
            if (virksomhet.getVirksomhetDetaljer().getNedleggelsesdato() != null) {
                builder.medAvsluttet(DateUtil.convertToLocalDate(virksomhet.getVirksomhetDetaljer().getNedleggelsesdato()));
            }
            builder.medOrganisasjonstype(Organisasjonstype.VIRKSOMHET);
        } else if (responsOrganisasjon instanceof JuridiskEnhet) {
            builder.medOrganisasjonstype(Organisasjonstype.JURIDISK_ENHET);
        }
        return builder.oppdatertOpplysningerNå().build();
    }

    private VirksomhetEntitet.Builder getBuilder(Optional<Virksomhet> virksomhetOptional) {
        return virksomhetOptional.map(VirksomhetEntitet.Builder::new).orElseGet(VirksomhetEntitet.Builder::new);
    }
}
