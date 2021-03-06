package no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import no.nav.foreldrepenger.abakus.felles.jpa.IntervallUtil;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Arbeidsavtale;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Arbeidsforhold;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.ArbeidsforholdIdentifikator;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.ArbeidsforholdTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Organisasjon;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.rest.AaregRestKlient;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.rest.ArbeidsforholdRS;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;

public class ArbeidsforholdTjenesteMedRestTest {

    private static final String ORGNR = "973093681";
    private static final String ULL = "8629102";
    private static final AktørId AKTØR_ID = new AktørId("1231231231223");
    private static final PersonIdent FNR = new PersonIdent("12312312312");
    private static final LocalDate FOM = LocalDate.now().minusYears(1L);

    private static ObjectMapper mapper;

    private static final String json = "{\n" +
        "  \"arbeidsforholdId\": \"990983666\",\n" +
        "  \"navArbeidsforholdId\": \"1234565\",\n" +
        "  \"type\": \"ordinaertArbeidsforhold\",\n" +
        "  \"registrert\": \"2020-02-23\",\n" +
        "  \"ansettelsesperiode\": {\n" +
        "    \"periode\": {\n" +
        "      \"fom\": \"2018-02-23\",\n" +
        "      \"tom\": \"2029-02-23\"\n" +
        "    }\n" +
        "  },\n" +
        "  \"arbeidsgiver\": {\n" +
        "    \"type\": \"Organisasjon\",\n" +
        "    \"organisasjonsnummer\": \"" + ORGNR +"\"\n" +
        "  },\n" +
        "  \"arbeidsavtaler\": [\n" +
        "      {\n" +
        "        \"stillingsprosent\": \"100.0\",\n" +
        "        \"antallTimerPrUke\": \"37.5\",\n" +
        "        \"beregnetAntallTimerPrUke\": \"37.5\",\n" +
        "        \"yrke\": \"" + ULL + "\",\n" +
        "        \"gyldighetsperiode\": {\n" +
            "      \"fom\": \"2018-02-23\",\n" +
            "      \"tom\": \"2029-02-23\"\n" +
            "    }\n" +
        "      }\n" +
        "    ]\n" +
        "}";

    @BeforeAll
    public static void setup() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    public void mapping_organisasjon() throws IOException {
        var arbeidsforhold = fromJson(json, ArbeidsforholdRS.class);

        assertThat(arbeidsforhold.getArbeidsgiver().getOrganisasjonsnummer()).isEqualTo(ORGNR);
        assertThat(arbeidsforhold.getArbeidsavtaler().get(0).getAntallTimerPrUke()).isEqualTo(new BigDecimal("37.5"));
    }

    @Test
    public void skal_kalle_consumer_og_oversette_response() throws Exception {
        // Arrange
        AaregRestKlient aaregRestKlient = mock(AaregRestKlient.class);
        when(aaregRestKlient.finnArbeidsforholdForArbeidstaker(any(), any(), any())).thenReturn(List.of(fromJson(json, ArbeidsforholdRS.class)));
        ArbeidsforholdTjeneste arbeidsforholdTjeneste = new ArbeidsforholdTjeneste(aaregRestKlient);

        // Act
        Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> arbeidsforhold = arbeidsforholdTjeneste.finnArbeidsforholdForIdentIPerioden(FNR, AKTØR_ID, IntervallUtil.byggIntervall(FOM, LocalDate.now()));

        // Assert
        assertThat(((Organisasjon) arbeidsforhold.values().iterator().next().get(0).getArbeidsgiver()).getOrgNummer()).isEqualTo(ORGNR);
        assertAktivitetsavtaler(arbeidsforhold.values().iterator().next().get(0).getArbeidsavtaler());
    }

    private void assertAktivitetsavtaler(List<Arbeidsavtale> arbeidsavtaler) {
        arbeidsavtaler.forEach(avtale -> {
            assertThat(avtale.getArbeidsavtaleFom()).isNotNull();
            if (!avtale.getErAnsettelsesPerioden()) {
                assertThat(avtale.getAvtaltArbeidstimerPerUke()).isNotNull();
                assertThat(avtale.getBeregnetAntallTimerPrUke()).isNotNull();
                assertThat(avtale.getStillingsprosent()).isNotNull();
            }
        });

    }


    private static <T> T fromJson(String json, Class<T> clazz) throws IOException {
        return mapper.readerFor(clazz).readValue(json);
    }
}
