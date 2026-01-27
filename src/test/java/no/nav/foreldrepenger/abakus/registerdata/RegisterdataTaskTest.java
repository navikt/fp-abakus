package no.nav.foreldrepenger.abakus.registerdata;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import no.nav.abakus.iaygrunnlag.AktørIdPersonident;
import no.nav.abakus.iaygrunnlag.Periode;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.abakus.iaygrunnlag.request.InnhentRegisterdataRequest;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

class RegisterdataTaskTest {

    private static final String INPUT_UUID = "4b05205e-95f4-4b62-a7e8-5029a0423e76";

    private static final LocalDate PERIODE_BASE = LocalDate.of(2022, 8, 11);

    private static final InnhentRegisterdataRequest REQUEST = new InnhentRegisterdataRequest("saksnummer", UUID.fromString(INPUT_UUID),
        YtelseType.FORELDREPENGER, new Periode(PERIODE_BASE.minusYears(2), PERIODE_BASE.plusYears(1)), new AktørIdPersonident("0000000000000"));

    @Test
    void roundtrip_payload() throws IOException {
        // Arrange

        var innhentingTask = ProsessTaskData.forProsessTask(RegisterdataInnhentingTask.class);
        innhentingTask.setPayload(DefaultJsonMapper.toJson(REQUEST));

        var roundtripped = DefaultJsonMapper.fromJson(innhentingTask.getPayloadAsString(), InnhentRegisterdataRequest.class);

        assertThat(roundtripped.getSaksnummer()).isEqualTo(REQUEST.getSaksnummer());
        assertThat(roundtripped.getOpplysningsperiode()).isEqualTo(REQUEST.getOpplysningsperiode());
        assertThat(roundtripped.getReferanse()).isEqualTo(REQUEST.getReferanse());
        assertThat(roundtripped.getAktør()).isEqualTo(REQUEST.getAktør());
        assertThat(roundtripped.getYtelseType()).isEqualTo(REQUEST.getYtelseType());
        assertThat(roundtripped.getElementer()).containsAll(REQUEST.getElementer());
    }


}
