package no.nav.foreldrepenger.abakus.kobling;

import static no.nav.foreldrepenger.abakus.kobling.TaskConstants.KOBLING_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.HttpURLConnection;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.abakus.iaygrunnlag.AktørIdPersonident;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@ExtendWith(MockitoExtension.class)
class KoblingRestTjenesteTest {

    @Mock
    private KoblingTjeneste koblingTjeneste;

    @Mock
    private ProsessTaskTjeneste prosessTaskTjeneste;

    private KoblingRestTjeneste koblingRestTjeneste;

    @BeforeEach
    void setUp() {
        koblingRestTjeneste = new KoblingRestTjeneste(koblingTjeneste, prosessTaskTjeneste);
    }

    @Test
    void opprett_sletting_av_kobling_task_og_verifiser_task_parameterne_ok() {
        var taskDataArgumentCaptor = ArgumentCaptor.forClass(ProsessTaskData.class);

        var referanse = new KoblingReferanse(UUID.randomUUID());
        var saksnummer = "23234234";
        var aktørId = AktørId.dummy();
        var ytelseType = YtelseType.FORELDREPENGER;
        var koblingId = 1L;

        when(koblingTjeneste.hentFor(referanse)).thenReturn(Optional.of(opprettKobling(koblingId, saksnummer, aktørId, ytelseType, referanse)));

        // Act
        var request = new KoblingRestTjeneste.AvsluttKoblingRequestAbacDto(saksnummer, referanse.getReferanse(), ytelseType, new AktørIdPersonident(aktørId.getId()));

        var response = koblingRestTjeneste.deaktiverKobling(request);

        // Assert
        assertThat(response.getStatus()).isEqualTo(HttpURLConnection.HTTP_OK);
        verify(prosessTaskTjeneste).lagre(taskDataArgumentCaptor.capture());
        var taskData = taskDataArgumentCaptor.getValue();
        assertThat(taskData.getPropertyValue(KOBLING_ID)).isEqualTo(String.valueOf(koblingId));
        assertThat(taskData.getSaksnummer()).isEqualTo(saksnummer);
        assertThat(taskData.getBehandlingUuid()).isEqualTo(referanse.getReferanse());
    }

    @Test
    void opprett_sletting_av_kobling_task_feil_ytesletype_bad_request_nok() {
        var referanse = new KoblingReferanse(UUID.randomUUID());
        var saksnummer = "23234234";
        var aktørId = AktørId.dummy();

        // Act
        var request = new KoblingRestTjeneste.AvsluttKoblingRequestAbacDto(saksnummer, referanse.getReferanse(), YtelseType.DAGPENGER, new AktørIdPersonident(aktørId.getId()));

        var response = koblingRestTjeneste.deaktiverKobling(request);

        // Assert
        assertThat(response.getStatus()).isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
        verify(prosessTaskTjeneste, never()).lagre(any(ProsessTaskData.class));
    }

    @Test
    void opprett_sletting_av_kobling_finner_ikke_kobling_ok() {
        var referanse = new KoblingReferanse(UUID.randomUUID());
        var saksnummer = "23234234";
        var aktørId = AktørId.dummy();
        var ytelseType = YtelseType.FORELDREPENGER;

        when(koblingTjeneste.hentFor(referanse)).thenReturn(Optional.empty());

        // Act
        var request = new KoblingRestTjeneste.AvsluttKoblingRequestAbacDto(saksnummer, referanse.getReferanse(), ytelseType, new AktørIdPersonident(aktørId.getId()));
        var response = koblingRestTjeneste.deaktiverKobling(request);

        // Assert
        assertThat(response.getStatus()).isEqualTo(HttpURLConnection.HTTP_NO_CONTENT);
    }

    @Test
    void opprett_sletting_av_kobling_saksnummer_matcher_ikke_nok() {
        var referanse = new KoblingReferanse(UUID.randomUUID());
        var saksnummer = "23234234";
        var aktørId = AktørId.dummy();
        var ytelseType = YtelseType.FORELDREPENGER;
        var koblingId = 1L;

        when(koblingTjeneste.hentFor(referanse)).thenReturn(Optional.of(opprettKobling(koblingId, saksnummer, aktørId, ytelseType, referanse)));

        // Act
        var request = new KoblingRestTjeneste.AvsluttKoblingRequestAbacDto("354645634", referanse.getReferanse(), ytelseType, new AktørIdPersonident(aktørId.getId()));

        var ex = assertThrows(IllegalArgumentException.class, () -> koblingRestTjeneste.deaktiverKobling(request));

        // Assert
        assertThat(ex.getMessage()).contains("Prøver å avslutte kobling på feil saksnummer");
        verify(prosessTaskTjeneste, never()).lagre(any(ProsessTaskData.class));
    }

    @Test
    void opprett_sletting_av_kobling_ytelse_type_matcher_ikke_nok() {
        var referanse = new KoblingReferanse(UUID.randomUUID());
        var saksnummer = "23234234";
        var aktørId = AktørId.dummy();
        var ytelseType = YtelseType.FORELDREPENGER;
        var koblingId = 1L;

        when(koblingTjeneste.hentFor(referanse)).thenReturn(Optional.of(opprettKobling(koblingId, saksnummer, aktørId, ytelseType, referanse)));

        // Act
        var request = new KoblingRestTjeneste.AvsluttKoblingRequestAbacDto(saksnummer, referanse.getReferanse(), YtelseType.SVANGERSKAPSPENGER, new AktørIdPersonident(aktørId.getId()));

        var ex = assertThrows(IllegalArgumentException.class, () -> koblingRestTjeneste.deaktiverKobling(request));

        // Assert
        assertThat(ex.getMessage()).contains("Prøver å avslutte kobling på feil ytelsetype");
        verify(prosessTaskTjeneste, never()).lagre(any(ProsessTaskData.class));
    }

    @Test
    void opprett_sletting_av_kobling_aktør_id_matcher_ikke_nok() {
        var referanse = new KoblingReferanse(UUID.randomUUID());
        var saksnummer = "23234234";
        var aktørId = AktørId.dummy();
        var ytelseType = YtelseType.FORELDREPENGER;
        var koblingId = 1L;

        when(koblingTjeneste.hentFor(referanse)).thenReturn(Optional.of(opprettKobling(koblingId, saksnummer, aktørId, ytelseType, referanse)));

        // Act
        var request = new KoblingRestTjeneste.AvsluttKoblingRequestAbacDto(saksnummer, referanse.getReferanse(), ytelseType, new AktørIdPersonident(AktørId.dummy().getId()));

        var ex = assertThrows(IllegalArgumentException.class, () -> koblingRestTjeneste.deaktiverKobling(request));

        // Assert
        assertThat(ex.getMessage()).contains("Prøver å avslutte kobling på feil aktør");
        verify(prosessTaskTjeneste, never()).lagre(any(ProsessTaskData.class));
    }

    private Kobling opprettKobling(Long id, String saksnummer, AktørId aktørId, YtelseType ytelseType, KoblingReferanse referanse) {
        var kobling = new Kobling(ytelseType, new Saksnummer(saksnummer), referanse, aktørId);
        kobling.setId(id);
        return kobling;
    }
}
