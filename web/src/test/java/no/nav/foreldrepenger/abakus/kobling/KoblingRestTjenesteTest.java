package no.nav.foreldrepenger.abakus.kobling;

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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.abakus.iaygrunnlag.AktørIdPersonident;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;

@ExtendWith(MockitoExtension.class)
class KoblingRestTjenesteTest {

    @Mock
    private KoblingTjeneste koblingTjeneste;

    @Mock
    private AvsluttKoblingTjeneste avsluttKobling;

    private KoblingRestTjeneste koblingRestTjeneste;

    @BeforeEach
    void setUp() {
        koblingRestTjeneste = new KoblingRestTjeneste(koblingTjeneste, avsluttKobling);
    }

    @Test
    void sletting_av_kobling_ok() {
        var referanse = new KoblingReferanse(UUID.randomUUID());
        var saksnummer = "23234234";
        var aktørId = AktørId.dummy();
        var ytelseType = YtelseType.FORELDREPENGER;

        when(koblingTjeneste.hentFor(referanse)).thenReturn(Optional.of(opprettKobling(saksnummer, aktørId, ytelseType, referanse)));

        // Act
        var request = new KoblingRestTjeneste.AvsluttKoblingRequestAbacDto(saksnummer, referanse.getReferanse(), ytelseType, new AktørIdPersonident(aktørId.getId()));

        var response = koblingRestTjeneste.deaktiverKobling(request);

        // Assert
        assertThat(response.getStatus()).isEqualTo(HttpURLConnection.HTTP_OK);
        verify(avsluttKobling).avsluttKobling(referanse, ytelseType);
    }

    @Test
    void sletting_av_kobling_allerede_deaktivert_ok() {
        var referanse = new KoblingReferanse(UUID.randomUUID());
        var saksnummer = "23234234";
        var aktørId = AktørId.dummy();
        var ytelseType = YtelseType.FORELDREPENGER;

        var kobling = opprettKobling(saksnummer, aktørId, ytelseType, referanse);
        kobling.setAktiv(false);
        when(koblingTjeneste.hentFor(referanse)).thenReturn(Optional.of(kobling));

        // Act
        var request = new KoblingRestTjeneste.AvsluttKoblingRequestAbacDto(saksnummer, referanse.getReferanse(), ytelseType, new AktørIdPersonident(aktørId.getId()));

        var response = koblingRestTjeneste.deaktiverKobling(request);

        // Assert
        assertThat(response.getStatus()).isEqualTo(HttpURLConnection.HTTP_NO_CONTENT);
        verify(avsluttKobling, never()).avsluttKobling(referanse, ytelseType);
    }

    @Test
    void sletting_av_kobling_feil_ytesletype_bad_request_nok() {
        var referanse = new KoblingReferanse(UUID.randomUUID());
        var saksnummer = "23234234";
        var aktørId = AktørId.dummy();

        // Act
        var request = new KoblingRestTjeneste.AvsluttKoblingRequestAbacDto(saksnummer, referanse.getReferanse(), YtelseType.DAGPENGER, new AktørIdPersonident(aktørId.getId()));

        var response = koblingRestTjeneste.deaktiverKobling(request);

        // Assert
        assertThat(response.getStatus()).isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
        verify(avsluttKobling, never()).avsluttKobling(any(), any());
    }

    @Test
    void sletting_av_kobling_finner_ikke_kobling_ok() {
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
    void sletting_av_kobling_saksnummer_matcher_ikke_nok() {
        var referanse = new KoblingReferanse(UUID.randomUUID());
        var saksnummer = "23234234";
        var aktørId = AktørId.dummy();
        var ytelseType = YtelseType.FORELDREPENGER;

        when(koblingTjeneste.hentFor(referanse)).thenReturn(Optional.of(opprettKobling(saksnummer, aktørId, ytelseType, referanse)));

        // Act
        var request = new KoblingRestTjeneste.AvsluttKoblingRequestAbacDto("354645634", referanse.getReferanse(), ytelseType, new AktørIdPersonident(aktørId.getId()));

        var ex = assertThrows(IllegalArgumentException.class, () -> koblingRestTjeneste.deaktiverKobling(request));

        // Assert
        assertThat(ex.getMessage()).contains("Prøver å avslutte kobling på feil saksnummer");
        verify(avsluttKobling, never()).avsluttKobling(any(), any());
    }

    @Test
    void sletting_av_kobling_ytelse_type_matcher_ikke_nok() {
        var referanse = new KoblingReferanse(UUID.randomUUID());
        var saksnummer = "23234234";
        var aktørId = AktørId.dummy();
        var ytelseType = YtelseType.FORELDREPENGER;

        when(koblingTjeneste.hentFor(referanse)).thenReturn(Optional.of(opprettKobling(saksnummer, aktørId, ytelseType, referanse)));

        // Act
        var request = new KoblingRestTjeneste.AvsluttKoblingRequestAbacDto(saksnummer, referanse.getReferanse(), YtelseType.SVANGERSKAPSPENGER, new AktørIdPersonident(aktørId.getId()));

        var ex = assertThrows(IllegalArgumentException.class, () -> koblingRestTjeneste.deaktiverKobling(request));

        // Assert
        assertThat(ex.getMessage()).contains("Prøver å avslutte kobling på feil ytelsetype");
        verify(avsluttKobling, never()).avsluttKobling(any(), any());
    }

    @Test
    void sletting_av_kobling_aktør_id_matcher_ikke_nok() {
        var referanse = new KoblingReferanse(UUID.randomUUID());
        var saksnummer = "23234234";
        var aktørId = AktørId.dummy();
        var ytelseType = YtelseType.FORELDREPENGER;

        when(koblingTjeneste.hentFor(referanse)).thenReturn(Optional.of(opprettKobling(saksnummer, aktørId, ytelseType, referanse)));

        // Act
        var request = new KoblingRestTjeneste.AvsluttKoblingRequestAbacDto(saksnummer, referanse.getReferanse(), ytelseType, new AktørIdPersonident(AktørId.dummy().getId()));

        var ex = assertThrows(IllegalArgumentException.class, () -> koblingRestTjeneste.deaktiverKobling(request));

        // Assert
        assertThat(ex.getMessage()).contains("Prøver å avslutte kobling på feil aktør");
        verify(avsluttKobling, never()).avsluttKobling(any(), any());
    }

    private Kobling opprettKobling(String saksnummer, AktørId aktørId, YtelseType ytelseType, KoblingReferanse referanse) {
        return new Kobling(ytelseType, new Saksnummer(saksnummer), referanse, aktørId);
    }
}
