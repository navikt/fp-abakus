package no.nav.foreldrepenger.abakus.rydding.task;

import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.foreldrepenger.abakus.rydding.OppryddingIAYAggregatRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

@ExtendWith(MockitoExtension.class)
class FjernIAYGrunnlagUtenReferanseTaskTest {

    @Mock
    private OppryddingIAYAggregatRepository oppryddingIAYAggregatRepository;
    @Captor
    private ArgumentCaptor<Long> longCaptor;

    private FjernIAYGrunnlagUtenReferanseTask task;

    @BeforeEach
    void setUp() {
        task = new FjernIAYGrunnlagUtenReferanseTask(oppryddingIAYAggregatRepository);
    }

    @Test
    void testDoTask_withValidPayload() {
        var iayIds = Set.of(1L, 2L, 3L);
        var payload = DefaultJsonMapper.toJson(iayIds);
        var prosessTaskData = ProsessTaskData.forProsessTask(FjernIAYGrunnlagUtenReferanseTask.class);
        prosessTaskData.setPayload(payload);

        task.doTask(prosessTaskData);

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

        verify(oppryddingIAYAggregatRepository, never()).slettIayAggregat(anyLong());
    }
}
