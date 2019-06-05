package no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.domene.virksomhet.Virksomhet;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.person.TpsTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.person.domene.Personinfo;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.virksomhet.VirksomhetTjeneste;
import no.nav.vedtak.util.LRUCache;

@ApplicationScoped
public class ArbeidsgiverTjenesteImpl implements ArbeidsgiverTjeneste {

    private static final long CACHE_ELEMENT_LIVE_TIME_MS = TimeUnit.MILLISECONDS.convert(12, TimeUnit.HOURS);
    private TpsTjeneste tpsTjeneste;
    private VirksomhetTjeneste virksomhetTjeneste;
    private LRUCache<String, ArbeidsgiverOpplysninger> cache = new LRUCache<>(1000, CACHE_ELEMENT_LIVE_TIME_MS);

    ArbeidsgiverTjenesteImpl() {
        // CDI
    }

    @Inject
    public ArbeidsgiverTjenesteImpl(TpsTjeneste tpsTjeneste, VirksomhetTjeneste virksomhetTjeneste) {
        this.tpsTjeneste = tpsTjeneste;
        this.virksomhetTjeneste = virksomhetTjeneste;
    }

    @Override
    public ArbeidsgiverOpplysninger hent(Arbeidsgiver arbeidsgiver) {
        ArbeidsgiverOpplysninger arbeidsgiverOpplysninger = cache.get(arbeidsgiver.getIdentifikator());
        if (arbeidsgiverOpplysninger != null) {
            return arbeidsgiverOpplysninger;
        }
        if (arbeidsgiver.getErVirksomhet()) {
            Virksomhet virksomhet = virksomhetTjeneste.hentOgLagreOrganisasjon(arbeidsgiver.getIdentifikator());
            ArbeidsgiverOpplysninger opplysninger = new ArbeidsgiverOpplysninger(arbeidsgiver.getIdentifikator(), virksomhet.getNavn());
            cache.put(arbeidsgiver.getIdentifikator(), opplysninger);
            return opplysninger;
        } else if (arbeidsgiver.erAktørId()) {
            Optional<Personinfo> personinfo = tpsTjeneste.hentBrukerForAktør(arbeidsgiver.getAktørId());
            if (personinfo.isPresent()) {
                Personinfo info = personinfo.get();
                String fødselsdato = info.getFødselsdato().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                ArbeidsgiverOpplysninger nyOpplysninger = new ArbeidsgiverOpplysninger(fødselsdato, info.getNavn());
                cache.put(arbeidsgiver.getIdentifikator(), nyOpplysninger);
                return nyOpplysninger;
            } else {
                // Putter bevist ikke denne i cache da denne aktøren ikke er kjent
                return new ArbeidsgiverOpplysninger(arbeidsgiver.getIdentifikator(), "N/A");
            }
        }
        return null;
    }
}
