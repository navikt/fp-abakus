package no.nav.foreldrepenger.abakus.kobling;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;

@ExtendWith(MockitoExtension.class)
class AvsluttKoblingTjenesteTest {

    @Mock
    private KoblingTjeneste koblingTjeneste;

    @Mock
    private InntektArbeidYtelseTjeneste iayTjeneste;

    @Mock
    private Kobling kobling;

    private AvsluttKoblingTjeneste avsluttKoblingTjeneste;

    @BeforeEach
    void setUp() {
        avsluttKoblingTjeneste = new AvsluttKoblingTjeneste(koblingTjeneste, iayTjeneste);
    }

    @Test
    void avslutt_grunnlag_ok() {
        var referanse = new KoblingReferanse(UUID.randomUUID());
        when(kobling.getKoblingReferanse()).thenReturn(referanse);
        when(kobling.getYtelseType()).thenReturn(YtelseType.FORELDREPENGER);

        when(koblingTjeneste.hentFor(referanse)).thenReturn(Optional.of(kobling));

        // Act
        avsluttKoblingTjeneste.avsluttKobling(referanse);

        // Verify
        verify(iayTjeneste).slettInaktiveGrunnlagFor(referanse);
        verify(koblingTjeneste).deaktiver(referanse);
    }

    @Test
    void avslutt_grunnlag_exception_ingen_kobling_nok() {
        var referanse = new KoblingReferanse(UUID.randomUUID());

        when(koblingTjeneste.hentFor(referanse)).thenReturn(Optional.empty());

        // Act
        assertThrows(NoSuchElementException.class, () -> avsluttKoblingTjeneste.avsluttKobling(referanse));

        // Verify
        verify(iayTjeneste, never()).slettInaktiveGrunnlagFor(referanse);
        verify(koblingTjeneste, never()).deaktiver(referanse);
    }
}
