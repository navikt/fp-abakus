package no.nav.foreldrepenger.abakus.rydding.task;

import static java.util.Collections.emptySet;
import static no.nav.foreldrepenger.abakus.rydding.task.FjernIAYGrunnlagUtenReferanseTask.MAX_PARTITION_SIZE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.stream.LongStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.foreldrepenger.abakus.rydding.OppryddingIAYAggregatRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

@ExtendWith(MockitoExtension.class)
class FjernIAYGrunnlagUtenReferanseTaskTest {

    @Mock
    private OppryddingIAYAggregatRepository oppryddingIAYAggregatRepository;
    @Mock
    private ProsessTaskTjeneste prosessTaskTjeneste;
    @Captor
    private ArgumentCaptor<Long> longCaptor;
    @Captor ArgumentCaptor<ProsessTaskData> prosessTaskDataCaptor;

    private FjernIAYGrunnlagUtenReferanseTask task;

    @BeforeEach
    void setUp() {
        task = new FjernIAYGrunnlagUtenReferanseTask(oppryddingIAYAggregatRepository, prosessTaskTjeneste);
    }

    @Test
    void testDoTask_withValidPayload() {
        var iayIds = Set.of(1L, 2L, 3L);
        var payload = DefaultJsonMapper.toJson(iayIds);
        var prosessTaskData = ProsessTaskData.forProsessTask(FjernIAYGrunnlagUtenReferanseTask.class);
        prosessTaskData.setPayload(payload);

        task.doTask(prosessTaskData);

        verify(prosessTaskTjeneste, never()).lagre(any(ProsessTaskData.class));
        verify(oppryddingIAYAggregatRepository, times(3)).slettIayAggregat(longCaptor.capture());
        var capturedIds = Set.copyOf(longCaptor.getAllValues());
        assertEquals(iayIds, capturedIds);
    }

    @Test
    void testDoTask_withEmptyPayload() {
        Set<Long> iayIds = emptySet();
        var payload = DefaultJsonMapper.toJson(iayIds);
        var prosessTaskData = ProsessTaskData.forProsessTask(FjernIAYGrunnlagUtenReferanseTask.class);
        prosessTaskData.setPayload(payload);

        task.doTask(prosessTaskData);

        verify(prosessTaskTjeneste, never()).lagre(any(ProsessTaskData.class));
        verify(oppryddingIAYAggregatRepository, never()).slettIayAggregat(anyLong());
    }

    @Test
    void testDoTask_withValidPayload_over_max_partition_size() {
        var iayIds = LongStream.rangeClosed(1, MAX_PARTITION_SIZE).boxed().toList();
        var payload = DefaultJsonMapper.toJson(iayIds);
        var prosessTaskData = ProsessTaskData.forProsessTask(FjernIAYGrunnlagUtenReferanseTask.class);
        prosessTaskData.setPayload(payload);
        when(oppryddingIAYAggregatRepository.hentIayAggregaterUtenReferanse(MAX_PARTITION_SIZE)).thenReturn(iayIds.subList(0, MAX_PARTITION_SIZE - 5));

        task.doTask(prosessTaskData);

        verify(oppryddingIAYAggregatRepository, times(MAX_PARTITION_SIZE)).slettIayAggregat(longCaptor.capture());
        var capturedIds = List.copyOf(longCaptor.getAllValues());
        assertEquals(iayIds, capturedIds);
        verify(prosessTaskTjeneste).lagre(prosessTaskDataCaptor.capture());
        var nextProsesstaskPayload = DefaultJsonMapper.fromJson(prosessTaskDataCaptor.getValue().getPayloadAsString(), List.class);
        assertThat(nextProsesstaskPayload).hasSize(MAX_PARTITION_SIZE - 5);
    }
}
