package no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.person.TpsTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.person.domene.Personinfo;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.virksomhet.VirksomhetTjeneste;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;
import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.util.LRUCache;

@ApplicationScoped
public class ArbeidsgiverTjeneste {

    private static final long CACHE_ELEMENT_LIVE_TIME_MS = TimeUnit.MILLISECONDS.convert(12, TimeUnit.HOURS);
    private static final long SHORT_CACHE_ELEMENT_LIVE_TIME_MS = TimeUnit.MILLISECONDS.convert(10, TimeUnit.MINUTES);
    private TpsTjeneste tpsTjeneste;
    private VirksomhetTjeneste virksomhetTjeneste;
    private LRUCache<String, ArbeidsgiverOpplysninger> cache = new LRUCache<>(1000, CACHE_ELEMENT_LIVE_TIME_MS);
    private LRUCache<String, ArbeidsgiverOpplysninger> failBackoffCache = new LRUCache<>(100, SHORT_CACHE_ELEMENT_LIVE_TIME_MS);

    ArbeidsgiverTjeneste() {
        // CDI
    }

    @Inject
    public ArbeidsgiverTjeneste(TpsTjeneste tpsTjeneste, VirksomhetTjeneste virksomhetTjeneste) {
        this.tpsTjeneste = tpsTjeneste;
        this.virksomhetTjeneste = virksomhetTjeneste;
    }

    public ArbeidsgiverOpplysninger hent(Arbeidsgiver arbeidsgiver) {
        if (arbeidsgiver == null) {
            return null;
        }
        ArbeidsgiverOpplysninger arbeidsgiverOpplysninger = cache.get(arbeidsgiver.getIdentifikator());
        if (arbeidsgiverOpplysninger != null) {
            return arbeidsgiverOpplysninger;
        }
        arbeidsgiverOpplysninger = failBackoffCache.get(arbeidsgiver.getIdentifikator());
        if (arbeidsgiverOpplysninger != null) {
            return arbeidsgiverOpplysninger;
        }
        if (arbeidsgiver.getErVirksomhet() && !OrgNummer.erKunstig(arbeidsgiver.getOrgnr())) {
            String orgnr = arbeidsgiver.getOrgnr().getId();
            var virksomhet = virksomhetTjeneste.hentOrganisasjon(orgnr);
            ArbeidsgiverOpplysninger nyOpplysninger = new ArbeidsgiverOpplysninger(orgnr, virksomhet.getNavn());
            cache.put(arbeidsgiver.getIdentifikator(), nyOpplysninger);
            return nyOpplysninger;
        } else if (arbeidsgiver.getErVirksomhet() && OrgNummer.erKunstig(arbeidsgiver.getOrgnr())) {
            return new ArbeidsgiverOpplysninger(OrgNummer.KUNSTIG_ORG, "Kunstig(Lagt til av saksbehandling)");
        } else if (arbeidsgiver.erAktørId()) {
            Optional<Personinfo> personinfo = hentInformasjonFraTps(arbeidsgiver);
            if (personinfo.isPresent()) {
                Personinfo info = personinfo.get();
                String fødselsdato = info.getFødselsdato().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                ArbeidsgiverOpplysninger nyOpplysninger = new ArbeidsgiverOpplysninger(fødselsdato, info.getNavn(), info.getFødselsdato());
                cache.put(arbeidsgiver.getIdentifikator(), nyOpplysninger);
                return nyOpplysninger;
            } else {
                // Putter bevist ikke denne i cache da denne aktøren ikke er kjent, men legger denne i en backoff cache som benyttes for at vi ikke skal hamre på tps ved sikkerhetsbegrensning
                ArbeidsgiverOpplysninger opplysninger = new ArbeidsgiverOpplysninger(arbeidsgiver.getIdentifikator(), "N/A");
                failBackoffCache.put(arbeidsgiver.getIdentifikator(), opplysninger);
                return opplysninger;
            }
        }
        return null;
    }

    private Optional<Personinfo> hentInformasjonFraTps(Arbeidsgiver arbeidsgiver) {
        try {
            return tpsTjeneste.hentBrukerForAktør(arbeidsgiver.getAktørId());
        } catch (VLException feil) {
            // Ønsker ikke å gi GUI problemer ved å eksponere exceptions
            return Optional.empty();
        }
    }
}
