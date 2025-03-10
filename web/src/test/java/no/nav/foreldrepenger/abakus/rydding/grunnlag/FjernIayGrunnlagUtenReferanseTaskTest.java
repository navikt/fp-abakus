package no.nav.foreldrepenger.abakus.rydding.grunnlag;

import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.LongStream;

import static java.util.Collections.emptyList;
import static no.nav.foreldrepenger.abakus.rydding.grunnlag.FjernIayGrunnlagUtenReferanseTask.IAY_GRUNNLAG_BATCH_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FjernIayGrunnlagUtenReferanseTaskTest {

    @Mock
    private OppryddingIayAggregatRepository oppryddingIAYAggregatRepository;
    @Mock
    private ProsessTaskTjeneste prosessTaskTjeneste;
    @Captor
    private ArgumentCaptor<Long> longCaptor;

    private FjernIayGrunnlagUtenReferanseTask task;

    @BeforeEach
    void setUp() {
        task = new FjernIayGrunnlagUtenReferanseTask(oppryddingIAYAggregatRepository, prosessTaskTjeneste);
    }

    @Test
    void testDoTask_ok() {
        var iayIds = List.of(1L, 2L, 3L);
        when(oppryddingIAYAggregatRepository.hentIayAggregaterUtenReferanse(IAY_GRUNNLAG_BATCH_SIZE)).thenReturn(iayIds);

        // Act
        task.doTask(ProsessTaskData.forProsessTask(FjernIayGrunnlagUtenReferanseTask.class));

        // Assert
        verify(prosessTaskTjeneste, never()).lagre(any(ProsessTaskData.class));
        verify(oppryddingIAYAggregatRepository, times(1)).hentIayAggregaterUtenReferanse(anyInt());
        verify(oppryddingIAYAggregatRepository, times(3)).slettIayAggregat(longCaptor.capture());
        var capturedIds = List.copyOf(longCaptor.getAllValues());
        assertEquals(iayIds, capturedIds);
    }

    @Test
    void testDoTask_ikke_noe_til_å_slette() {
        when(oppryddingIAYAggregatRepository.hentIayAggregaterUtenReferanse(IAY_GRUNNLAG_BATCH_SIZE)).thenReturn(emptyList());

        // Act
        task.doTask(ProsessTaskData.forProsessTask(FjernIayGrunnlagUtenReferanseTask.class));

        // Assert
        verify(oppryddingIAYAggregatRepository, times(1)).hentIayAggregaterUtenReferanse(anyInt());
        verifyNoInteractions(prosessTaskTjeneste);
        verify(oppryddingIAYAggregatRepository, never()).slettIayAggregat(anyLong());
    }

    @Test
    void testDoTask_ok_over_max_partition_size() {
        var iayIds = LongStream.rangeClosed(1, IAY_GRUNNLAG_BATCH_SIZE).boxed().toList();
        when(oppryddingIAYAggregatRepository.hentIayAggregaterUtenReferanse(IAY_GRUNNLAG_BATCH_SIZE)).thenReturn(iayIds);

        // Act
        task.doTask(ProsessTaskData.forProsessTask(FjernIayGrunnlagUtenReferanseTask.class));

        // Assert
        verify(oppryddingIAYAggregatRepository, times(IAY_GRUNNLAG_BATCH_SIZE)).slettIayAggregat(longCaptor.capture());
        var capturedIds = List.copyOf(longCaptor.getAllValues());
        assertEquals(iayIds, capturedIds);
        verify(prosessTaskTjeneste).lagre(any(ProsessTaskData.class));
    }
}
