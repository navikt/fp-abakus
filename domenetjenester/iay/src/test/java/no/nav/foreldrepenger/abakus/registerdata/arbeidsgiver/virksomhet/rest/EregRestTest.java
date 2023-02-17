package no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.virksomhet.rest;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.abakus.iaygrunnlag.JsonObjectMapper;
import no.nav.vedtak.felles.integrasjon.organisasjon.JuridiskEnhetVirksomheter;
import no.nav.vedtak.felles.integrasjon.organisasjon.OrganisasjonEReg;
import no.nav.vedtak.felles.integrasjon.organisasjon.OrganisasjonstypeEReg;

public class EregRestTest {

    private static ObjectMapper mapper = JsonObjectMapper.getMapper();

    private static <T> T fromJson(String json, Class<T> clazz) throws IOException {
        return mapper.readerFor(clazz).readValue(json);
    }

    @Test
    public void mapping_dto_til_grunnlag_til_dto() throws IOException {
        // Arrange
        String json = """
            {
             "organisasjonsnummer":"999999999",
             "type":"Virksomhet",
             "navn":{"redigertnavn":"MIN BEDRIFT",
             "navnelinje1":"MIN BEDRIFT",
             "navnelinje2":"  ",
             "navnelinje3":"AVD DER JEG BOR"
            },
             "organisasjonDetaljer":null,
             "virksomhetDetaljer":null
            }""";

        var org = fromJson(json, OrganisasjonEReg.class);
        assertThat(org.getNavn()).isEqualTo("MIN BEDRIFT AVD DER JEG BOR");
        assertThat(org.organisasjonsnummer()).isEqualTo("999999999");
        assertThat(org.type()).isEqualTo(OrganisasjonstypeEReg.VIRKSOMHET);
    }

    @Test
    public void mapping_jurdisk_enhet() throws IOException {
        // Arrange
        String json = """
            {
              "organisasjonsnummer": "999999999",
              "type": "JuridiskEnhet",
              "organisasjonDetaljer": {
                "registreringsdato": "2007-01-23T00:00:00"
              },
              "driverVirksomheter": [
                {
                  "organisasjonsnummer": "999999997",
                  "bruksperiode": {
                    "fom": "2014-05-23T17:05:52.161"
                  },
                  "gyldighetsperiode": {
                    "fom": "2007-11-17"
                  }
                },
                {
                  "organisasjonsnummer": "999999998",
                  "bruksperiode": {
                    "fom": "2014-05-23T20:15:53.195"
                  },
                  "gyldighetsperiode": {
                    "fom": "2007-01-24",
                    "tom": "2007-11-16"
                  }
                }
              ]
            }""";

        var org = fromJson(json, JuridiskEnhetVirksomheter.class);
        assertThat(org.driverVirksomheter()).hasSize(2);
        assertThat(org.getEksaktVirksomhetForDato(LocalDate.now())).hasSize(1);
        assertThat(org.getEksaktVirksomhetForDato(LocalDate.now()).get(0)).isEqualTo("999999997");
        assertThat(org.organisasjonsnummer()).isEqualTo("999999999");
        assertThat(org.type()).isEqualTo(OrganisasjonstypeEReg.JURIDISK_ENHET);
    }
}
