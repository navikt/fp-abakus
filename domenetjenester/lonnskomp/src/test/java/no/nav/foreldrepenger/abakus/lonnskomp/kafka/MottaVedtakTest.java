package no.nav.foreldrepenger.abakus.lonnskomp.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import no.nav.foreldrepenger.abakus.lonnskomp.domene.LønnskompensasjonRepository;
import no.nav.foreldrepenger.abakus.lonnskomp.domene.LønnskompensasjonVedtak;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

public class MottaVedtakTest {

    private static String FNR = "19575903667";
    private static String AKTØR = "1957590366776";

    private LønnskompensasjonRepository repository = mock(LønnskompensasjonRepository.class);
    private ProsessTaskTjeneste taskTjeneste = mock(ProsessTaskTjeneste.class);
    private LonnskompHendelseHåndterer håndterer = new LonnskompHendelseHåndterer(repository, taskTjeneste);

    @Test
    public void skal_motta_uten_anvist() {
        String payload = "{\n" +
            "  \"id\": \"1.0\",\n" +
            "  \"fnr\": \"" + FNR + "\",\n" +
            "  \"totalKompensasjon\": \"12345.67\",\n" +
            "  \"bedriftNr\": \"999999999\",\n" +
            "  \"sakId\": \"3028155d-c556-4a8a-a38d-a526b1129bf2\",\n" +
            "  \"fom\": \"2020-04-20\",\n" +
            "  \"tom\": \"2020-05-15\",\n" +
            "  \"dagBeregninger\": [],\n" +
            "  \"forrigeVedtakDato\" : null\n" +
            "}\n" +
            "";
        when(repository.skalLagreVedtak(any(), any())).thenReturn(true);
        ArgumentCaptor<LønnskompensasjonVedtak> vedtakCaptor = ArgumentCaptor.forClass(LønnskompensasjonVedtak.class);
        håndterer.handleMessage("key", payload);
        verify(repository, atLeast(1)).lagre(vedtakCaptor.capture());
        assertThat(vedtakCaptor.getValue().getSakId()).isEqualTo("3028155d-c556-4a8a-a38d-a526b1129bf2");
    }

    @Test
    public void skal_motta_med_anvist() {
        var forrigeVedtakDato = LocalDate.of(2020,8,15);
        String payload = "{\n" +
            "  \"id\": \"1.0\",\n" +
            "  \"fnr\": \"" + FNR + "\",\n" +
            "  \"totalKompensasjon\": \"18000\",\n" +
            "  \"bedriftNr\": \"999999999\",\n" +
            "  \"sakId\": \"3028155d-c556-4a8a-a38d-a526b1129bf2\",\n" +
            "  \"fom\": \"2020-04-20\",\n" +
            "  \"tom\": \"2020-05-15\",\n" +
            "  \"dagBeregninger\": [{\n" +
            "     \"dato\": \"2020-04-20\",\n" +
            "     \"dagsats\": \"1000\",\n" +
            "     \"refusjonssbeløp\": \"0\",\n" +
            "     \"lønnskompensasjonsbeløp\": \"1000\"\n" +
            "    },{\n" +
            "     \"dato\": \"2020-04-21\",\n" +
            "     \"lønnskompensasjonsbeløp\": \"1000\"\n" +
            "    },{\n" +
            "     \"dato\": \"2020-05-10\",\n" +
            "     \"lønnskompensasjonsbeløp\": \"1000\"\n" +
            "    },{\n" +
            "     \"dato\": \"2020-05-15\",\n" +
            "     \"lønnskompensasjonsbeløp\": \"1000\"\n" +
            "  }],\n" +
            "  \"forrigeVedtakDato\" : \"" + forrigeVedtakDato + "\"\n" +
            "}\n";

        when(repository.skalLagreVedtak(any(), any())).thenReturn(true);
        ArgumentCaptor<LønnskompensasjonVedtak> vedtakCaptor = ArgumentCaptor.forClass(LønnskompensasjonVedtak.class);
        håndterer.handleMessage("key", payload);
        verify(repository, atLeast(1)).lagre(vedtakCaptor.capture());
        assertThat(vedtakCaptor.getValue().getSakId()).isEqualTo("3028155d-c556-4a8a-a38d-a526b1129bf2");
    }
}
