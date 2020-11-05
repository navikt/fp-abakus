package no.nav.foreldrepenger.abakus.registerdata.ytelse.arena.ws;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.binding.MeldekortUtbetalingsgrunnlagV1;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.meldinger.FinnMeldekortUtbetalingsgrunnlagListeRequest;
import no.nav.vedtak.exception.IntegrasjonException;

public class MeldekortUtbetalingsgrunnlagConsumerTest {

    private MeldekortUtbetalingsgrunnlagConsumer consumer;


    private MeldekortUtbetalingsgrunnlagV1 mockWebservice = mock(MeldekortUtbetalingsgrunnlagV1.class);

    @BeforeEach
    public void setUp() {
        consumer = new MeldekortUtbetalingsgrunnlagConsumerImpl(mockWebservice);
    }

    @Test
    public void skalKasteIntegrasjonsfeilNårWebserviceSenderSoapFault() throws Exception {
        when(mockWebservice.finnMeldekortUtbetalingsgrunnlagListe(any(FinnMeldekortUtbetalingsgrunnlagListeRequest.class))).thenThrow(opprettSOAPFaultException("feil"));

        assertThrows(IntegrasjonException.class, () -> consumer.finnMeldekortUtbetalingsgrunnlagListe(mock(FinnMeldekortUtbetalingsgrunnlagListeRequest.class)));
    }

    private SOAPFaultException opprettSOAPFaultException(String faultString) throws SOAPException {
        SOAPFault fault = SOAPFactory.newInstance().createFault();
        fault.setFaultString(faultString);
        fault.setFaultCode(new QName("local"));
        return new SOAPFaultException(fault);
    }
}
