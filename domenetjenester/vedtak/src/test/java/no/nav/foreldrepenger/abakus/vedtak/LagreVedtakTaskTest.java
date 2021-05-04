package no.nav.foreldrepenger.abakus.vedtak;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseRepository;
import no.nav.foreldrepenger.abakus.vedtak.extract.v1.ExtractFromYtelseV1;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

public class LagreVedtakTaskTest {


    private VedtakYtelseRepository repository = mock(VedtakYtelseRepository.class);
    private ExtractFromYtelseV1 extractor = new ExtractFromYtelseV1(repository);
    private LagreVedtakTask task = new LagreVedtakTask(repository, extractor);

    @Test
    public void skal_feile_ved_valideringsfeil() {
        String payload = "{\n" +
            "  \"version\": \"1.0\",\n" +
            "  \"aktør\": {\n" +
            "    \"verdi\": \"1957590366736\"\n" +
            "  },\n" +
            "  \"vedtattTidspunkt\": \"2019-06-11T00:00:00\",\n" +
            "  \"type\": {\n" +
            "    \"kodeverk\": \"FAGSAK_YTELSE_TYPE\",\n" +
            "    \"kode\": \"SVP\"\n" +
            "  },\n" +
            "  \"saksnummer\": \"139015437\",\n" +
            "  \"vedtakReferanse\": \"3028155d-c556-4a8a-a38d-a526b1129bf2\",\n" +
            "  \"status\": {\n" +
            "    \"kodeverk\": \"YTELSE_STATUS\",\n" +
            "    \"kode\": \"UBEH\"\n" +
            "  },\n" +
            "  \"fagsystem\": {\n" +
            "    \"kodeverk\": \"FAGSYSTEM\",\n" +
            "    \"kode\": \"FPSAK\"\n" +
            "  },\n" +
            "  \"periode\": {\n" +
            "    \"fom\": null,\n" +
            "    \"tom\": null\n" +
            "  },\n" +
            "  \"anvist\": []\n" +
            "}\n" +
            "";

        ProsessTaskData data = new ProsessTaskData(LagreVedtakTask.TASKTYPE);
        data.setPayload(payload);
        data.setProperty(LagreVedtakTask.KEY, UUID.randomUUID().toString());

        assertThrows(IllegalArgumentException.class, () ->task.doTask(data));
    }
}
