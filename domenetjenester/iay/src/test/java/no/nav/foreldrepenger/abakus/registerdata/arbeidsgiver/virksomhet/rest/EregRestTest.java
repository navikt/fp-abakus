package no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.virksomhet.rest;

import org.junit.Test;

public class EregRestTest {

    @Test
    public void roundtrip_mapping_dto_til_grunnlag_til_dto() {
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

        var org = JsonMapper.fromJson(json, Organisasjon.class);
        var navn = org.getNavn();
        var orgnr = org.getOrganisasjonsnummer();
    }

    @Test
    public void roundtrip_mapping_jurdisk_enhet() {
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

        var org = JsonMapper.fromJson(json, JuridiskEnhetVirksomheter.class);
        var virksomheter = org.getDriverVirksomheter();
        var orgnr = org.getOrganisasjonsnummer();
    }
}
