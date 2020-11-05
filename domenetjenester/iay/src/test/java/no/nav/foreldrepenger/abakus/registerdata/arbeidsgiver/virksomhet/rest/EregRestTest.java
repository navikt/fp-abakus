package no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.virksomhet.rest;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class EregRestTest {

    private static ObjectMapper mapper;

    @BeforeAll
    public static void setup() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    public void mapping_dto_til_grunnlag_til_dto() throws IOException {
        // Arrange
        String json ="{\n \"organisasjonsnummer\":\"999999999\",\n" +
            " \"type\":\"Virksomhet\",\n" +
            " \"navn\":{\"redigertnavn\":\"MIN BEDRIFT\",\n" +
            " \"navnelinje1\":\"MIN BEDRIFT\",\n" +
            " \"navnelinje2\":\"  \",\n" +
            " \"navnelinje3\":\"AVD DER JEG BOR\"\n" +
            "},\n" +
            " \"organisasjonDetaljer\":null,\n" +
            " \"virksomhetDetaljer\":null\n" +
            "}";

        var org = fromJson(json, OrganisasjonEReg.class);
        assertThat(org.getNavn()).isEqualTo("MIN BEDRIFT AVD DER JEG BOR");
        assertThat(org.getOrganisasjonsnummer()).isEqualTo("999999999");
        assertThat(org.getType()).isEqualTo(OrganisasjonstypeEReg.VIRKSOMHET);
    }

    @Test
    public void mapping_jurdisk_enhet() throws IOException {
        // Arrange
        String json ="{\n" +
            "  \"organisasjonsnummer\": \"999999999\",\n" +
            "  \"type\": \"JuridiskEnhet\",\n" +
            "  \"organisasjonDetaljer\": {\n" +
            "    \"registreringsdato\": \"2007-01-23T00:00:00\"\n" +
            "  },\n" +
            "  \"driverVirksomheter\": [\n" +
            "    {\n" +
            "      \"organisasjonsnummer\": \"999999997\",\n" +
            "      \"bruksperiode\": {\n" +
            "        \"fom\": \"2014-05-23T17:05:52.161\"\n" +
            "      },\n" +
            "      \"gyldighetsperiode\": {\n" +
            "        \"fom\": \"2007-11-17\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"organisasjonsnummer\": \"999999998\",\n" +
            "      \"bruksperiode\": {\n" +
            "        \"fom\": \"2014-05-23T20:15:53.195\"\n" +
            "      },\n" +
            "      \"gyldighetsperiode\": {\n" +
            "        \"fom\": \"2007-01-24\",\n" +
            "        \"tom\": \"2007-11-16\"\n" +
            "      }\n" +
            "    }\n" +
            "  ]\n" +
            "}";

        var org = fromJson(json, JuridiskEnhetVirksomheter.class);
        assertThat(org.getDriverVirksomheter()).hasSize(2);
        assertThat(org.getEksaktVirksomhetForDato(LocalDate.now())).hasSize(1);
        assertThat(org.getEksaktVirksomhetForDato(LocalDate.now()).get(0)).isEqualTo("999999997");
        assertThat(org.getOrganisasjonsnummer()).isEqualTo("999999999");
        assertThat(org.getType()).isEqualTo(OrganisasjonstypeEReg.JURIDISK_ENHET);
    }

    private static <T> T fromJson(String json, Class<T> clazz) throws IOException {
        return mapper.readerFor(clazz).readValue(json);
    }
}
