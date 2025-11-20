package no.nav.foreldrepenger.abakus.rydding.inntektsmelding;

import static java.util.Collections.emptyList;
import static no.nav.foreldrepenger.abakus.rydding.inntektsmelding.FjernIayInntektsmeldingerUtenReferanseTask.IAY_INNTEKTSMELDING_BATCH_SIZE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.LongStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.foreldrepenger.abakus.rydding.OppryddingTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

@ExtendWith(MockitoExtension.class)
class FjernIayInntektsmeldingerUtenReferanseTaskTest {

    @Mock
    private OppryddingIayInntektsmeldingerRepository oppryddingIayInntektsmeldingerRepository;
    @Mock
    private OppryddingTjeneste oppryddingTjeneste;
    @Captor
    private ArgumentCaptor<Long> longCaptor;

    private FjernIayInntektsmeldingerUtenReferanseTask task;

    @BeforeEach
    void setUp() {
        task = new FjernIayInntektsmeldingerUtenReferanseTask(oppryddingIayInntektsmeldingerRepository, oppryddingTjeneste);
    }

    @Test
    void testDoTask_ok() {
        var iayIds = List.of(1L, 2L, 3L);
        when(oppryddingIayInntektsmeldingerRepository.hentIayInntektsmeldingerUtenReferanse(IAY_INNTEKTSMELDING_BATCH_SIZE)).thenReturn(iayIds);

        // Act
        task.doTask(ProsessTaskData.forProsessTask(FjernIayInntektsmeldingerUtenReferanseTask.class));

        // Assert
        verifyNoInteractions(oppryddingTjeneste);
        verify(oppryddingIayInntektsmeldingerRepository, times(1)).hentIayInntektsmeldingerUtenReferanse(anyInt());
        verify(oppryddingIayInntektsmeldingerRepository, times(3)).slettIayInntektsmeldinger(longCaptor.capture());
        var capturedIds = List.copyOf(longCaptor.getAllValues());
        assertThat(capturedIds).isEqualTo(iayIds);
    }

    @Test
    void testDoTask_ikke_noe_til_å_slette() {
        when(oppryddingIayInntektsmeldingerRepository.hentIayInntektsmeldingerUtenReferanse(IAY_INNTEKTSMELDING_BATCH_SIZE)).thenReturn(emptyList());

        // Act
        task.doTask(ProsessTaskData.forProsessTask(FjernIayInntektsmeldingerUtenReferanseTask.class));

        // Assert
        verify(oppryddingIayInntektsmeldingerRepository, times(1)).hentIayInntektsmeldingerUtenReferanse(anyInt());
        verifyNoInteractions(oppryddingTjeneste);
        verify(oppryddingIayInntektsmeldingerRepository, never()).slettIayInntektsmeldinger(anyLong());
    }

    @Test
    void testDoTask_withValidPayload_over_max_partition_size() {
        var iayIds = LongStream.rangeClosed(1, IAY_INNTEKTSMELDING_BATCH_SIZE).boxed().toList();
        when(oppryddingIayInntektsmeldingerRepository.hentIayInntektsmeldingerUtenReferanse(IAY_INNTEKTSMELDING_BATCH_SIZE)).thenReturn(
            iayIds);

        // Act
        task.doTask(ProsessTaskData.forProsessTask(FjernIayInntektsmeldingerUtenReferanseTask.class));

        // Assert
        verify(oppryddingIayInntektsmeldingerRepository, times(IAY_INNTEKTSMELDING_BATCH_SIZE)).slettIayInntektsmeldinger(longCaptor.capture());
        var capturedIds = List.copyOf(longCaptor.getAllValues());
        assertThat(capturedIds).isEqualTo(iayIds);
        verify(oppryddingTjeneste).opprettFjernIayInntektsmeldingerTask();
    }
}
