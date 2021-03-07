package no.nav.foreldrepenger.abakus.registerdata.ytelse.arena.ws;

import javax.xml.ws.soap.SOAPFaultException;

import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.binding.FinnMeldekortUtbetalingsgrunnlagListeAktoerIkkeFunnet;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.binding.FinnMeldekortUtbetalingsgrunnlagListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.binding.FinnMeldekortUtbetalingsgrunnlagListeUgyldigInput;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.binding.MeldekortUtbetalingsgrunnlagV1;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.meldinger.FinnMeldekortUtbetalingsgrunnlagListeRequest;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.meldinger.FinnMeldekortUtbetalingsgrunnlagListeResponse;
import no.nav.vedtak.exception.IntegrasjonException;

public class MeldekortUtbetalingsgrunnlagConsumerImpl implements MeldekortUtbetalingsgrunnlagConsumer {

    private MeldekortUtbetalingsgrunnlagV1 port;

    public MeldekortUtbetalingsgrunnlagConsumerImpl(MeldekortUtbetalingsgrunnlagV1 port) {
        this.port = port;
    }

    @Override
    public FinnMeldekortUtbetalingsgrunnlagListeResponse finnMeldekortUtbetalingsgrunnlagListe(FinnMeldekortUtbetalingsgrunnlagListeRequest request) throws FinnMeldekortUtbetalingsgrunnlagListeSikkerhetsbegrensning, FinnMeldekortUtbetalingsgrunnlagListeAktoerIkkeFunnet, FinnMeldekortUtbetalingsgrunnlagListeUgyldigInput {
        try {
            return port.finnMeldekortUtbetalingsgrunnlagListe(request);
        } catch (SOAPFaultException e) { // NOSONAR
            throw new IntegrasjonException("F-942048", "SOAP tjenesten [ MeldekortUtbetalingsgrunnlagV1 ] returnerte en SOAP Fault:", e);
        }
    }
}
