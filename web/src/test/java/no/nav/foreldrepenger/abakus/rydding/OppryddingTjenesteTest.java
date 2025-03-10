package no.nav.foreldrepenger.abakus.rydding;

import no.nav.foreldrepenger.abakus.rydding.task.FjernIAYGrunnlagUtenReferanseTask;
import no.nav.foreldrepenger.abakus.rydding.task.FjernIayInformasjonUtenReferanseTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.felles.prosesstask.api.TaskType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OppryddingTjenesteTest {

    @Mock
    private OppryddingIAYAggregatRepository oppryddingIAYAggregatRepository;
    @Mock
    private OppryddingIayInformasjonRepository oppryddingIayInformasjonRepository;
    @Mock
    private ProsessTaskTjeneste prosessTaskTjeneste;
    @Captor
    private ArgumentCaptor<ProsessTaskData> prosessTaskDataCaptor;

    private OppryddingTjeneste oppryddingTjeneste;

    @BeforeEach
    void setUp() {
        oppryddingTjeneste = new OppryddingTjeneste(oppryddingIAYAggregatRepository, oppryddingIayInformasjonRepository, prosessTaskTjeneste);
    }

    @Test
    void testFjernAlleIayAggregatUtenReferanse_noAggregates_ok() {
        when(oppryddingIAYAggregatRepository.hentIayAggregaterUtenReferanse(anyInt())).thenReturn(List.of());

        // Act
        oppryddingTjeneste.fjernAlleIayAggregatUtenReferanse();

        // Assert
        verify(oppryddingIAYAggregatRepository, times(1)).hentIayAggregaterUtenReferanse(anyInt());
        verifyNoInteractions(prosessTaskTjeneste);
        verifyNoInteractions(oppryddingIayInformasjonRepository);
    }

    @Test
    void testFjernAlleIayAggregatUtenReferanse_withAggregates_ok() {
        var aggregates = List.of(1L, 2L, 3L);
        when(oppryddingIAYAggregatRepository.hentIayAggregaterUtenReferanse(anyInt())).thenReturn(aggregates);

        // Act
        oppryddingTjeneste.fjernAlleIayAggregatUtenReferanse();

        // Assert
        verify(oppryddingIAYAggregatRepository).hentIayAggregaterUtenReferanse(anyInt());
        verify(prosessTaskTjeneste, times(1)).lagre(prosessTaskDataCaptor.capture());
        var prosessTaskData = prosessTaskDataCaptor.getValue();
        assertThat(prosessTaskData.taskType()).isEqualTo(TaskType.forProsessTask(FjernIAYGrunnlagUtenReferanseTask.class));
        verifyNoInteractions(oppryddingIayInformasjonRepository);
    }

    @Test
    void testFjernAlleInformasjonAggregatUtenReferanse_noAggregates_ok() {
        when(oppryddingIayInformasjonRepository.hentIayInformasjonUtenReferanse(anyInt())).thenReturn(List.of());

        // Act
        oppryddingTjeneste.fjernAlleIayInformasjontUtenReferanse();

        // Assert
        verify(oppryddingIayInformasjonRepository, times(1)).hentIayInformasjonUtenReferanse(anyInt());
        verifyNoInteractions(prosessTaskTjeneste);
        verifyNoInteractions(oppryddingIAYAggregatRepository);
    }

    @Test
    void testFjernAlleInformasjonAggregatUtenReferanse_withAggregates_ok() {
        var aggregates = List.of(1L, 2L, 3L);
        when(oppryddingIayInformasjonRepository.hentIayInformasjonUtenReferanse(anyInt())).thenReturn(aggregates);

        // Act
        oppryddingTjeneste.fjernAlleIayInformasjontUtenReferanse();

        // Assert
        verify(oppryddingIayInformasjonRepository).hentIayInformasjonUtenReferanse(anyInt());
        verify(prosessTaskTjeneste, times(1)).lagre(prosessTaskDataCaptor.capture());
        var prosessTaskData = prosessTaskDataCaptor.getValue();
        assertThat(prosessTaskData.taskType()).isEqualTo(TaskType.forProsessTask(FjernIayInformasjonUtenReferanseTask.class));
        verifyNoInteractions(oppryddingIAYAggregatRepository);
    }
}
