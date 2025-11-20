package no.nav.foreldrepenger.abakus.rydding.grunnlag;

import static java.util.Collections.emptyList;
import static no.nav.foreldrepenger.abakus.rydding.grunnlag.FjernIayGrunnlagUtenReferanseTask.IAY_GRUNNLAG_BATCH_SIZE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.anyInt;
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
class FjernIayGrunnlagUtenReferanseTaskTest {

    @Mock
    private OppryddingIayAggregatRepository oppryddingIAYAggregatRepository;
    @Mock
    private OppryddingTjeneste oppryddingTjeneste;
    @Captor
    private ArgumentCaptor<Long> longCaptor;

    private FjernIayGrunnlagUtenReferanseTask task;

    @BeforeEach
    void setUp() {
        task = new FjernIayGrunnlagUtenReferanseTask(oppryddingIAYAggregatRepository, oppryddingTjeneste);
    }

    @Test
    void testDoTask_ok() {
        var iayIds = List.of(1L, 2L, 3L);
        when(oppryddingIAYAggregatRepository.hentIayAggregaterUtenReferanse(IAY_GRUNNLAG_BATCH_SIZE)).thenReturn(iayIds);

        // Act
        task.doTask(ProsessTaskData.forProsessTask(FjernIayGrunnlagUtenReferanseTask.class));

        // Assert
        verifyNoInteractions(oppryddingTjeneste);
        verify(oppryddingIAYAggregatRepository, times(1)).hentIayAggregaterUtenReferanse(anyInt());
        verify(oppryddingIAYAggregatRepository, times(3)).slettIayAggregat(longCaptor.capture());
        var capturedIds = List.copyOf(longCaptor.getAllValues());
        assertThat(capturedIds).isEqualTo(iayIds);
    }

    @Test
    void testDoTask_ikke_noe_til_å_slette() {
        when(oppryddingIAYAggregatRepository.hentIayAggregaterUtenReferanse(IAY_GRUNNLAG_BATCH_SIZE)).thenReturn(emptyList());

        // Act
        task.doTask(ProsessTaskData.forProsessTask(FjernIayGrunnlagUtenReferanseTask.class));

        // Assert
        verify(oppryddingIAYAggregatRepository, times(1)).hentIayAggregaterUtenReferanse(anyInt());
        verifyNoInteractions(oppryddingTjeneste);
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
        assertThat(capturedIds).isEqualTo(iayIds);
        verify(oppryddingTjeneste).opprettFjernIayInntektArbeidYtelseAggregatTask();
    }
}
