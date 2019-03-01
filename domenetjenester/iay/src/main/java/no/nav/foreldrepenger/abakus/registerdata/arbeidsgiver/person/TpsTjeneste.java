package no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.person;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.ws.soap.SOAPFaultException;

import no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.person.domene.Personinfo;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;

@ApplicationScoped
public class TpsTjeneste {

    private TpsAdapter tpsAdapter;

    public TpsTjeneste() {
        // for CDI proxy
    }

    @Inject
    public TpsTjeneste(TpsAdapter tpsAdapter) {
        this.tpsAdapter = tpsAdapter;
    }

    public Optional<Personinfo> hentBrukerForFnr(PersonIdent fnr) {
        if (fnr.erFdatNummer()) {
            return Optional.empty();
        }
        Optional<AktørId> aktørId = tpsAdapter.hentAktørIdForPersonIdent(fnr);
        if (!aktørId.isPresent()) {
            return Optional.empty();
        }
        try {
            Personinfo personinfo = tpsAdapter.hentKjerneinformasjon(fnr, aktørId.get());
            return Optional.ofNullable(personinfo);
        } catch (SOAPFaultException e) {
            if (e.getMessage().contains("status: S100008F")) {
                // Her sorterer vi ut dødfødte barn
                return Optional.empty();
            } else {
                throw e;
            }
        }
    }

    public PersonIdent hentFnrForAktør(AktørId aktørId) {
        Optional<PersonIdent> funnetFnr;
        funnetFnr = hentFnr(aktørId);
        if (funnetFnr.isPresent()) {
            return funnetFnr.get();
        }
        throw TpsFeilmeldinger.FACTORY.fantIkkePersonForAktørId().toException();
    }

    public Optional<AktørId> hentAktørForFnr(PersonIdent fnr) {
        return tpsAdapter.hentAktørIdForPersonIdent(fnr);
    }

    public Optional<PersonIdent> hentFnr(AktørId aktørId) {
        return tpsAdapter.hentIdentForAktørId(aktørId);
    }

    public Optional<Personinfo> hentBrukerForAktør(AktørId aktørId) {
        Optional<PersonIdent> funnetFnr = hentFnr(aktørId);
        return funnetFnr.map(fnr -> tpsAdapter.hentKjerneinformasjon(fnr, aktørId));
    }

}
