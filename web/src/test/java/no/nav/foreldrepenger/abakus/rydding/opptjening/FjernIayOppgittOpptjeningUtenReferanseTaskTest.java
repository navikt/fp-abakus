package no.nav.foreldrepenger.abakus.rydding.opptjening;

import static java.util.Collections.emptyList;
import static no.nav.foreldrepenger.abakus.rydding.opptjening.FjernIayOppgittOpptjeningUtenReferanseTask.IAY_OPPGITT_OPPTJENING_BATCH_SIZE;
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
import no.nav.foreldrepenger.abakus.rydding.inntektsmelding.FjernIayInntektsmeldingerUtenReferanseTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

@ExtendWith(MockitoExtension.class)
class FjernIayOppgittOpptjeningUtenReferanseTaskTest {

    @Mock
    private OppryddingIayOppgittOpptjeningRepository opptjeningRepository;
    @Mock
    private OppryddingTjeneste oppryddingTjeneste;
    @Captor
    private ArgumentCaptor<Long> longCaptor;

    private FjernIayOppgittOpptjeningUtenReferanseTask task;

    @BeforeEach
    void setUp() {
        task = new FjernIayOppgittOpptjeningUtenReferanseTask(opptjeningRepository, oppryddingTjeneste);
    }

    @Test
    void testDoTask_ok() {
        var iayIds = List.of(1L, 2L, 3L);
        when(opptjeningRepository.hentIayOppgittOpptjeningUtenReferanse(IAY_OPPGITT_OPPTJENING_BATCH_SIZE)).thenReturn(iayIds);

        // Act
        task.doTask(ProsessTaskData.forProsessTask(FjernIayInntektsmeldingerUtenReferanseTask.class));

        // Assert
        verifyNoInteractions(oppryddingTjeneste);
        verify(opptjeningRepository, times(1)).hentIayOppgittOpptjeningUtenReferanse(anyInt());
        verify(opptjeningRepository, times(3)).slettIayOppgittOpptjening(longCaptor.capture());
        var capturedIds = List.copyOf(longCaptor.getAllValues());
        assertEquals(iayIds, capturedIds);
    }

    @Test
    void testDoTask_ikke_noe_til_å_slette() {
        when(opptjeningRepository.hentIayOppgittOpptjeningUtenReferanse(IAY_OPPGITT_OPPTJENING_BATCH_SIZE)).thenReturn(emptyList());

        // Act
        task.doTask(ProsessTaskData.forProsessTask(FjernIayInntektsmeldingerUtenReferanseTask.class));

        // Assert
        verify(opptjeningRepository, times(1)).hentIayOppgittOpptjeningUtenReferanse(anyInt());
        verifyNoInteractions(oppryddingTjeneste);
        verify(opptjeningRepository, never()).slettIayOppgittOpptjening(anyLong());
    }

    @Test
    void testDoTask_withValidPayload_over_max_partition_size() {
        var iayIds = LongStream.rangeClosed(1, IAY_OPPGITT_OPPTJENING_BATCH_SIZE).boxed().toList();
        when(opptjeningRepository.hentIayOppgittOpptjeningUtenReferanse(IAY_OPPGITT_OPPTJENING_BATCH_SIZE)).thenReturn(iayIds);

        // Act
        task.doTask(ProsessTaskData.forProsessTask(FjernIayInntektsmeldingerUtenReferanseTask.class));

        // Assert
        verify(opptjeningRepository, times(IAY_OPPGITT_OPPTJENING_BATCH_SIZE)).slettIayOppgittOpptjening(longCaptor.capture());
        var capturedIds = List.copyOf(longCaptor.getAllValues());
        assertEquals(iayIds, capturedIds);
        verify(oppryddingTjeneste).opprettFjernIayOppgittOpptjeningTask();
    }
}
