package no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.virksomhet;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.abakus.iaygrunnlag.kodeverk.OrganisasjonType;
import no.nav.foreldrepenger.abakus.domene.virksomhet.Virksomhet;
import no.nav.foreldrepenger.abakus.domene.virksomhet.VirksomhetAlleredeLagretException;
import no.nav.foreldrepenger.abakus.domene.virksomhet.VirksomhetRepository;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.virksomhet.rest.EregOrganisasjonRestKlient;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.virksomhet.rest.JuridiskEnhetVirksomheter;
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
import no.nav.vedtak.util.env.Environment;


@ApplicationScoped
public class VirksomhetTjeneste {

    private static final String TJENESTE = "Organisasjon";
    private static final Logger LOGGER = LoggerFactory.getLogger(VirksomhetTjeneste.class);
    private OrganisasjonConsumer organisasjonConsumer;
    private VirksomhetRepository virksomhetRepository;
    private EregOrganisasjonRestKlient eregRestKlient;
    private boolean isProd;

    public VirksomhetTjeneste() {
        // CDI
    }

    @Inject
    public VirksomhetTjeneste(OrganisasjonConsumer organisasjonConsumer,
                              EregOrganisasjonRestKlient eregRestKlient,
                              VirksomhetRepository virksomhetRepository) {
        this.organisasjonConsumer = organisasjonConsumer;
        this.eregRestKlient = eregRestKlient;
        this.virksomhetRepository = virksomhetRepository;
        isProd = Environment.current().isProd();
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
        sammenlignLoggRestVirksomhet(virksomhet.getOrgnr(), virksomhet);
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

        restSammenlignLoggJE(orgNummer, hentedato, response);

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
        final Virksomhet.Builder builder = getBuilder(virksomhetOptional)
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
            builder.medOrganisasjonstype(OrganisasjonType.VIRKSOMHET);
        } else if (responsOrganisasjon instanceof JuridiskEnhet) {
            builder.medOrganisasjonstype(OrganisasjonType.VIRKSOMHET);
        }
        return builder.oppdatertOpplysningerNå().build();
    }

    private Virksomhet.Builder getBuilder(Optional<Virksomhet> virksomhetOptional) {
        return virksomhetOptional.map(Virksomhet.Builder::new).orElseGet(Virksomhet.Builder::new);
    }

    private void sammenlignLoggRestVirksomhet(String orgNummer, Virksomhet virksomhet) {
        if (!isProd)
            return;
        try {
            var org = eregRestKlient.hentOrganisasjon(orgNummer);
            var builder = getBuilder(Optional.empty())
                .medNavn(org.getNavn())
                .medRegistrert(org.getRegistreringsdato())
                .medOrgnr(org.getOrganisasjonsnummer());
            if ("Virksomhet".equalsIgnoreCase(org.getType())) {
                builder.medOrganisasjonstype(OrganisasjonType.VIRKSOMHET)
                    .medOppstart(org.getOppstartsdato())
                    .medAvsluttet(org.getNedleggelsesdato());
            } else if ("JuridiskEnhet".equalsIgnoreCase(org.getType())) {
                builder.medOrganisasjonstype(OrganisasjonType.VIRKSOMHET);
            }
            var rest = builder.build();
            if (erLik(virksomhet, rest)) {
                LOGGER.info("FPSAK EREG REST likt svar");
            } else {
                LOGGER.info("FPSAK EREG REST avvik WS {} RS {}", virksomhet.getOrgnr(), rest.getOrgnr());
            }
        } catch (Exception e) {
            LOGGER.info("FPSAK EREG REST noe gikk feil", e);
        }
    }

    private void restSammenlignLoggJE(String orgNummer, LocalDate hentedato, HentVirksomhetsOrgnrForJuridiskOrgnrBolkResponse ws) {
        if (!isProd)
            return;
        try {
            var jenhet = eregRestKlient.hentJurdiskEnhetVirksomheter(orgNummer);
            List<String> aktiveOrgnr = jenhet.getDriverVirksomheter().stream()
                .filter(v -> v.getGyldighetsperiode().getFom().isBefore(hentedato) && v.getGyldighetsperiode().getTom().isAfter(hentedato))
                .map(JuridiskEnhetVirksomheter.DriverVirksomhet::getOrganisasjonsnummer)
                .collect(Collectors.toList());
            int antallUnntak = ws.getUnntakForOrgnrListe().size();
            int antallVirksomhet = ws.getOrgnrForOrganisasjonListe().size();
            Optional<String> wsOrgnummer = ws.getOrgnrForOrganisasjonListe().stream().map(OrgnrForOrganisasjon::getOrganisasjonsnummer).findFirst();
            if (aktiveOrgnr.size() == 1) {
                if (antallVirksomhet == 1 && antallUnntak == 0 && wsOrgnummer.map(aktiveOrgnr.get(0)::equals).orElse(false))
                    LOGGER.info("ABAKUS EREG JENHET samme virksomhet {} for juridisk {}", aktiveOrgnr.get(0), orgNummer);
                else
                    LOGGER.info("ABAKUS EREG JENHET avvik virksomhet WS {} RS {} for juridisk {}", wsOrgnummer, aktiveOrgnr.get(0), orgNummer);
            } else {
                if (antallUnntak > 0)
                    LOGGER.info("ABAKUS EREG JENHET samme unntak for juridisk {}", orgNummer);
                else
                    LOGGER.info("ABAKUS EREG JENHET avvik unntak for juridisk {}", orgNummer);
            }
        } catch (Exception e) {
            LOGGER.info("ABAKUS EREG JENHET noe gikk galt", e);
        }
    }

    public boolean erLik(Virksomhet en, Virksomhet to) {
        if (en == to)
            return true;
        if (to == null)
            return false;
        return Objects.equals(en.getOrgnr(), to.getOrgnr())
            && Objects.equals(en.getNavn(), to.getNavn())
            && Objects.equals(en.getRegistrert(), to.getRegistrert())
            && Objects.equals(en.getAvslutt(), to.getAvslutt())
            && Objects.equals(en.getOppstart(), to.getOppstart())
            && Objects.equals(en.getOrganisasjonstype(), to.getOrganisasjonstype());
    }
}
