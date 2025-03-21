package no.nav.foreldrepenger.abakus.rydding.arbeidsforhold;

import static java.util.Collections.emptyList;
import static no.nav.foreldrepenger.abakus.rydding.arbeidsforhold.FjernIayInformasjonUtenReferanseTask.IAY_ARBEIDSFORHOLD_INFORMASJON_BATCH_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
class FjernIayInformasjonUtenReferanseTaskTest {

    @Mock
    private OppryddingIayInformasjonRepository oppryddingIayInformasjonRepository;
    @Mock
    private OppryddingTjeneste oppryddingTjeneste;
    @Captor
    private ArgumentCaptor<Long> longCaptor;

    private FjernIayInformasjonUtenReferanseTask task;

    @BeforeEach
    void setUp() {
        task = new FjernIayInformasjonUtenReferanseTask(oppryddingIayInformasjonRepository, oppryddingTjeneste);
    }

    @Test
    void testDoTask_ok() {
        var iayIds = List.of(1L, 2L, 3L);
        when(oppryddingIayInformasjonRepository.hentIayInformasjonUtenReferanse(IAY_ARBEIDSFORHOLD_INFORMASJON_BATCH_SIZE)).thenReturn(iayIds);

        // Act
        task.doTask(ProsessTaskData.forProsessTask(FjernIayInformasjonUtenReferanseTask.class));

        // Assert
        verifyNoInteractions(oppryddingTjeneste);
        verify(oppryddingIayInformasjonRepository, times(1)).hentIayInformasjonUtenReferanse(anyInt());
        verify(oppryddingIayInformasjonRepository, times(3)).slettIayInformasjon(longCaptor.capture());
        var capturedIds = List.copyOf(longCaptor.getAllValues());
        assertEquals(iayIds, capturedIds);
    }

    @Test
    void testDoTask_ikke_noe_til_å_slette() {
        when(oppryddingIayInformasjonRepository.hentIayInformasjonUtenReferanse(IAY_ARBEIDSFORHOLD_INFORMASJON_BATCH_SIZE)).thenReturn(emptyList());

        // Act
        task.doTask(ProsessTaskData.forProsessTask(FjernIayInformasjonUtenReferanseTask.class));

        // Assert
        verify(oppryddingIayInformasjonRepository, times(1)).hentIayInformasjonUtenReferanse(anyInt());
        verifyNoInteractions(oppryddingTjeneste);
        verify(oppryddingIayInformasjonRepository, never()).slettIayInformasjon(anyLong());
    }

    @Test
    void testDoTask_withValidPayload_over_max_partition_size() {
        var iayIds = LongStream.rangeClosed(1, IAY_ARBEIDSFORHOLD_INFORMASJON_BATCH_SIZE).boxed().toList();
        when(oppryddingIayInformasjonRepository.hentIayInformasjonUtenReferanse(IAY_ARBEIDSFORHOLD_INFORMASJON_BATCH_SIZE)).thenReturn(
                iayIds);

        // Act
        task.doTask(ProsessTaskData.forProsessTask(FjernIayInformasjonUtenReferanseTask.class));

        // Assert
        verify(oppryddingIayInformasjonRepository, times(IAY_ARBEIDSFORHOLD_INFORMASJON_BATCH_SIZE)).slettIayInformasjon(longCaptor.capture());
        var capturedIds = List.copyOf(longCaptor.getAllValues());
        assertEquals(iayIds, capturedIds);
        verify(oppryddingTjeneste).opprettFjernIayInformasjonTask();
    }
}
