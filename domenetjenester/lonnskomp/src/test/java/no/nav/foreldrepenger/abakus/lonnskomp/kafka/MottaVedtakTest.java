package no.nav.foreldrepenger.abakus.lonnskomp.kafka;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import no.nav.foreldrepenger.abakus.aktor.AktørTjeneste;
import no.nav.foreldrepenger.abakus.lonnskomp.domene.LønnskompensasjonRepository;
import no.nav.foreldrepenger.abakus.lonnskomp.domene.LønnskompensasjonVedtak;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;

public class MottaVedtakTest {

    private static String FNR = "19575903667";
    private static String AKTØR = "1957590366776";

    private LønnskompensasjonRepository repository = mock(LønnskompensasjonRepository.class);
    private AktørTjeneste aktørTjeneste = mock(AktørTjeneste.class);
    private LonnskompHendelseHåndterer håndterer = new LonnskompHendelseHåndterer(aktørTjeneste, repository);

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

        ArgumentCaptor<LønnskompensasjonVedtak> vedtakCaptor = ArgumentCaptor.forClass(LønnskompensasjonVedtak.class);
        lenient().when(aktørTjeneste.hentAktørForIdent(eq(new PersonIdent(FNR)), any())).thenReturn(Optional.of(new AktørId(AKTØR)));
        håndterer.handleMessage("key", payload);
        verify(repository, atLeast(1)).lagre(vedtakCaptor.capture());
    }

    //    String dato;
    //    String dagsats;
    //    String permitteringsgrad;
    //    String overstiger6G;
    //    String refusjonssbeløp;
    //    String lønnskompensasjonsbeløp;
    //    String dagtype;

    @Test
    public void skal_motta_med_anvist() {
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
            "  \"forrigeVedtakDato\" : \"2020-08-01\"\n" +
            "}\n" +
            "";

        ArgumentCaptor<LønnskompensasjonVedtak> vedtakCaptor = ArgumentCaptor.forClass(LønnskompensasjonVedtak.class);
        lenient().when(aktørTjeneste.hentAktørForIdent(eq(new PersonIdent(FNR)), any())).thenReturn(Optional.of(new AktørId(AKTØR)));
        håndterer.handleMessage("key", payload);
        verify(repository, atLeast(1)).lagre(vedtakCaptor.capture());
    }
}
