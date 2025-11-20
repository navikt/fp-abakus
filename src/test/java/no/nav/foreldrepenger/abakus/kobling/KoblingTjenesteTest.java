package no.nav.foreldrepenger.abakus.kobling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.kobling.repository.KoblingRepository;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;

@ExtendWith(MockitoExtension.class)
class KoblingTjenesteTest {

    @Mock
    private KoblingRepository koblingRepository;

    private KoblingTjeneste koblingTjeneste;

    @BeforeEach
    void setUp() {
        koblingTjeneste = new KoblingTjeneste(koblingRepository, null);
    }

    @Test
    void deaktiver() {
        // Prepare
        UUID referanse = UUID.randomUUID();
        var koblingReferanse = new KoblingReferanse(referanse);
        var aktivKobling = lagreKobling(koblingReferanse);

        when(koblingRepository.hentForKoblingReferanse(koblingReferanse, true)).thenReturn(Optional.of(aktivKobling));

        // Run
        koblingTjeneste.deaktiver(koblingReferanse);

        // Assert
        var captor = ArgumentCaptor.forClass(Kobling.class);
        verify(koblingRepository).hentForKoblingReferanse(koblingReferanse, true);
        verify(koblingRepository).lagre(captor.capture());
        verifyNoMoreInteractions(koblingRepository);

        var kobling = captor.getValue();
        assertThat(kobling.erAktiv()).isFalse();
        assertThat(kobling.getKoblingReferanse()).isEqualTo(koblingReferanse);
    }

    private Kobling lagreKobling(KoblingReferanse koblingReferanse) {
        return new Kobling(YtelseType.FORELDREPENGER, new Saksnummer("21223423"), koblingReferanse, new AktørId(1231234234122L));
    }
}
