package no.nav.foreldrepenger.abakus.rydding;

import no.nav.foreldrepenger.abakus.rydding.task.FjernIayGrunnlagUtenReferanseTask;
import no.nav.foreldrepenger.abakus.rydding.task.FjernIayInformasjonUtenReferanseTask;
import no.nav.foreldrepenger.abakus.rydding.task.FjernIayInntektsmeldingerUtenReferanseTask;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OppryddingTjenesteTest {

    @Mock
    private ProsessTaskTjeneste prosessTaskTjeneste;
    @Captor
    private ArgumentCaptor<ProsessTaskData> prosessTaskDataCaptor;

    private OppryddingTjeneste oppryddingTjeneste;

    @BeforeEach
    void setUp() {
        oppryddingTjeneste = new OppryddingTjeneste(prosessTaskTjeneste);
    }


    @Test
    void testFjernAlleIayAggregatUtenReferanse_ok() {
        // Act
        oppryddingTjeneste.fjernAlleIayAggregatUtenReferanse();

        // Assert
        verify(prosessTaskTjeneste, times(1)).lagre(prosessTaskDataCaptor.capture());
        verifyNoMoreInteractions(prosessTaskTjeneste);
        var prosessTaskData = prosessTaskDataCaptor.getValue();
        assertThat(prosessTaskData.taskType()).isEqualTo(TaskType.forProsessTask(FjernIayGrunnlagUtenReferanseTask.class));
    }

    @Test
    void testFjernAlleInformasjonAggregatUtenReferanse_ok() {
        // Act
        oppryddingTjeneste.fjernAlleIayInformasjontUtenReferanse();

        // Assert
        verify(prosessTaskTjeneste, times(1)).lagre(prosessTaskDataCaptor.capture());
        verifyNoMoreInteractions(prosessTaskTjeneste);
        var prosessTaskData = prosessTaskDataCaptor.getValue();
        assertThat(prosessTaskData.taskType()).isEqualTo(TaskType.forProsessTask(FjernIayInformasjonUtenReferanseTask.class));
    }

    @Test
    void testFjernAlleInntektsmeldingerUtenReferanse_ok() {
        // Act
        oppryddingTjeneste.fjernAlleIayInntektsmeldingerUtenReferanse();

        // Assert
        verify(prosessTaskTjeneste, times(1)).lagre(prosessTaskDataCaptor.capture());
        verifyNoMoreInteractions(prosessTaskTjeneste);
        var prosessTaskData = prosessTaskDataCaptor.getValue();
        assertThat(prosessTaskData.taskType()).isEqualTo(TaskType.forProsessTask(FjernIayInntektsmeldingerUtenReferanseTask.class));
    }
}
