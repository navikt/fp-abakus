package no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.person;

import java.time.LocalDate;

import javax.enterprise.context.ApplicationScoped;

import no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.person.domene.Personinfo;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Aktoer;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Foedselsdato;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;

@ApplicationScoped
public class TpsOversetter {

    TpsOversetter() {
        // for CDI proxy
    }

    public Personinfo tilBrukerInfo(AktørId aktørId, Bruker bruker) { // NOSONAR - ingen forbedring å forkorte metoden her
        String navn = bruker.getPersonnavn().getSammensattNavn();

        LocalDate fødselsdato = finnFødselsdato(bruker);

        Aktoer aktoer = bruker.getAktoer();
        PersonIdent pi = (PersonIdent) aktoer;
        String ident = pi.getIdent().getIdent();


        return new Personinfo.Builder()
            .medAktørId(aktørId)
            .medPersonIdent(no.nav.foreldrepenger.abakus.typer.PersonIdent.fra(ident))
            .medNavn(navn)
            .medFødselsdato(fødselsdato)
            .build();
    }

    private LocalDate finnFødselsdato(Bruker person) {
        LocalDate fødselsdato = null;
        Foedselsdato fødselsdatoJaxb = person.getFoedselsdato();
        if (fødselsdatoJaxb != null) {
            fødselsdato = DateUtil.convertToLocalDate(fødselsdatoJaxb.getFoedselsdato());
        }
        return fødselsdato;
    }

}
