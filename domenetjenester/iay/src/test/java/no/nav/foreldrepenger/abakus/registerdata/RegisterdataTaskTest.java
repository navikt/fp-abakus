package no.nav.foreldrepenger.abakus.registerdata;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import no.nav.abakus.iaygrunnlag.AktørIdPersonident;
import no.nav.abakus.iaygrunnlag.JsonObjectMapper;
import no.nav.abakus.iaygrunnlag.Periode;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.abakus.iaygrunnlag.request.InnhentRegisterdataRequest;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

class RegisterdataTaskTest {

    private static final String INPUT_UUID = "4b05205e-95f4-4b62-a7e8-5029a0423e76";

    private static final InnhentRegisterdataRequest REQUEST = new InnhentRegisterdataRequest("saksnummer",
        UUID.fromString(INPUT_UUID), YtelseType.FORELDREPENGER, new Periode(LocalDate.now().minusYears(2), LocalDate.now().plusYears(1)),
        new AktørIdPersonident("0000000000000"));

    @Test
    void roundtrip_payload() throws IOException {
        // Arrange
        var periodeRef = LocalDate.now();
        var innhentingTask = ProsessTaskData.forProsessTask(RegisterdataInnhentingTask.class);
        innhentingTask.setPayload(JsonObjectMapper.getMapper().writeValueAsString(REQUEST));

        var roundtripped = JsonObjectMapper.getMapper().readValue(innhentingTask.getPayloadAsString(), InnhentRegisterdataRequest.class);

        assertThat(roundtripped.getSaksnummer()).isEqualTo(REQUEST.getSaksnummer());
        assertThat(roundtripped.getOpplysningsperiode()).isEqualTo(REQUEST.getOpplysningsperiode());
        assertThat(roundtripped.getReferanse()).isEqualTo(REQUEST.getReferanse());
        assertThat(roundtripped.getAktør()).isEqualTo(REQUEST.getAktør());
        assertThat(roundtripped.getYtelseType()).isEqualTo(REQUEST.getYtelseType());
        assertThat(roundtripped.getElementer()).containsAll(REQUEST.getElementer());
    }

    private static final String LEGACY = """
        {
          "saksnummer" : "saksnummer",
          "referanse" : "4b05205e-95f4-4b62-a7e8-5029a0423e76",
          "ytelseType" : {
            "kode" : "FP",
            "kodeverk" : "FAGSAK_YTELSE_TYPE"
          },
          "opplysningsperiode" : {
            "fom" : "2020-08-11",
            "tom" : "2023-08-11"
          },
          "aktør" : {
            "identType" : "AKTØRID",
            "ident" : "0000000000000"
          },
          "elementer" : [ {
            "kode" : "YTELSE",
            "kodeverk" : "REGISTERDATA_TYPE"
          }, {
            "kode" : "INNTEKT_PENSJONSGIVENDE",
            "kodeverk" : "REGISTERDATA_TYPE"
          }, {
            "kode" : "ARBEIDSFORHOLD",
            "kodeverk" : "REGISTERDATA_TYPE"
          } ]
        }
        """;

    @Test
    void lese_legacy_payload() throws IOException {
        // Arrange
        var roundtripped = JsonObjectMapper.getMapper().readValue(LEGACY, InnhentRegisterdataRequest.class);

        assertThat(roundtripped.getSaksnummer()).isEqualTo(REQUEST.getSaksnummer());
        assertThat(roundtripped.getOpplysningsperiode()).isEqualTo(REQUEST.getOpplysningsperiode());
        assertThat(roundtripped.getReferanse()).isEqualTo(REQUEST.getReferanse());
        assertThat(roundtripped.getAktør()).isEqualTo(REQUEST.getAktør());
        assertThat(roundtripped.getYtelseType()).isEqualTo(REQUEST.getYtelseType());
        assertThat(roundtripped.getElementer()).containsAll(REQUEST.getElementer());
    }
}
