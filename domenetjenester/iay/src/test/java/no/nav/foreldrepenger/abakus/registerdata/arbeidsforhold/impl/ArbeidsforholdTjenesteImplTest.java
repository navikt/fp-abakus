package no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.impl;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import no.nav.foreldrepenger.abakus.felles.jpa.IntervallUtil;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Arbeidsforhold;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.ArbeidsforholdIdentifikator;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.ArbeidsforholdTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Organisasjon;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.person.TpsTjeneste;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.FinnArbeidsforholdPrArbeidstakerUgyldigInput;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.feil.UgyldigInput;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.AnsettelsesPeriode;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforholdstyper;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Gyldighetsperiode;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.FinnArbeidsforholdPrArbeidstakerResponse;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.HentArbeidsforholdHistorikkResponse;
import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.felles.integrasjon.arbeidsforhold.ArbeidsforholdConsumer;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;

public class ArbeidsforholdTjenesteImplTest {
    private static final String FNR = "1234";
    private static final LocalDate FOM = LocalDate.now().minusYears(1L);
    private static final String ORGNR = "973093681";
    private static final LocalDate PERIODE_FOM = LocalDate.now().minusYears(3L);
    private static final String KODEVERKS_REF = "A_ORDNINGEN";
    private static final AktørId aktørId = new AktørId("1231231231223");
    @Test
    public void skal_kalle_consumer_og_oversette_response() throws Exception {
        // Arrange
        FinnArbeidsforholdPrArbeidstakerResponse response = opprettResponse();

        ArbeidsforholdConsumer arbeidsforholdConsumer = mock(ArbeidsforholdConsumer.class);
        when(arbeidsforholdConsumer.finnArbeidsforholdPrArbeidstaker(any())).thenReturn(response);
        HentArbeidsforholdHistorikkResponse response1 = new HentArbeidsforholdHistorikkResponse();
        response1.setArbeidsforhold(response.getArbeidsforhold().get(0));
        when(arbeidsforholdConsumer.hentArbeidsforholdHistorikk(any())).thenReturn(response1);

        ArbeidsforholdTjeneste arbeidsforholdTjeneste = new ArbeidsforholdTjeneste(arbeidsforholdConsumer, mock(TpsTjeneste.class), null);

        // Act
        Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> arbeidsforhold = arbeidsforholdTjeneste.finnArbeidsforholdForIdentIPerioden(new PersonIdent(FNR), aktørId, IntervallUtil.byggIntervall(FOM, LocalDate.now()));

        // Assert
        assertThat(((Organisasjon) arbeidsforhold.values().iterator().next().get(0).getArbeidsgiver()).getOrgNummer()).isEqualTo(ORGNR);
    }

    @Test
    public void skal_håndtere_exceptions_fra_consumer() throws Exception {
        // Arrange
        FinnArbeidsforholdPrArbeidstakerResponse response = opprettResponse();
        ArbeidsforholdConsumer arbeidsforholdConsumer = mock(ArbeidsforholdConsumer.class);
        when(arbeidsforholdConsumer.finnArbeidsforholdPrArbeidstaker(any())).thenReturn(response);

        doThrow(new FinnArbeidsforholdPrArbeidstakerUgyldigInput("Feil", new UgyldigInput())).when(arbeidsforholdConsumer).finnArbeidsforholdPrArbeidstaker(any());

        ArbeidsforholdTjeneste arbeidsforholdTjeneste = new ArbeidsforholdTjeneste(arbeidsforholdConsumer, mock(TpsTjeneste.class), null);

        try {
            // Act
            arbeidsforholdTjeneste.finnArbeidsforholdForIdentIPerioden(new PersonIdent(FNR), aktørId, IntervallUtil.byggIntervall(FOM, LocalDate.now()));
            fail("Forventet VLException");
        } catch (VLException e) {
            // Assert
            assertThat(e.getCause()).isInstanceOf(FinnArbeidsforholdPrArbeidstakerUgyldigInput.class);
        }
    }

    private FinnArbeidsforholdPrArbeidstakerResponse opprettResponse() throws Exception {
        FinnArbeidsforholdPrArbeidstakerResponse response = new FinnArbeidsforholdPrArbeidstakerResponse();
        no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold arbeidsforhold = new no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold();

        no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Organisasjon arbeidsgiver = new no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Organisasjon();

        arbeidsgiver.setOrgnummer(ORGNR);
        arbeidsforhold.setArbeidsgiver(arbeidsgiver);

        Arbeidsforholdstyper arbeidsforholdstyper = new Arbeidsforholdstyper();
        arbeidsforholdstyper.setKodeverksRef(KODEVERKS_REF);
        arbeidsforhold.setArbeidsforholdstype(arbeidsforholdstyper);

        AnsettelsesPeriode ansettelsesPeriode = new AnsettelsesPeriode();
        Gyldighetsperiode gyldighetsperiode = new Gyldighetsperiode();

        gyldighetsperiode.setFom(DateUtil.convertToXMLGregorianCalendar(PERIODE_FOM));
        ansettelsesPeriode.setPeriode(gyldighetsperiode);
        arbeidsforhold.setAnsettelsesPeriode(ansettelsesPeriode);

        response.getArbeidsforhold().add(arbeidsforhold);
        return response;
    }

}
