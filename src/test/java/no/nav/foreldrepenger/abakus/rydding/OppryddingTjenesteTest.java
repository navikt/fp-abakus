package no.nav.foreldrepenger.abakus.rydding;

import no.nav.foreldrepenger.abakus.rydding.arbeidsforhold.FjernIayInformasjonUtenReferanseTask;
import no.nav.foreldrepenger.abakus.rydding.grunnlag.FjernIayGrunnlagUtenReferanseTask;
import no.nav.foreldrepenger.abakus.rydding.inntektsmelding.FjernIayInntektsmeldingerUtenReferanseTask;
import no.nav.foreldrepenger.abakus.rydding.opptjening.FjernIayOppgittOpptjeningUtenReferanseTask;
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
        oppryddingTjeneste.fjernAlleInaktiveAggregaterUtenReferanse();

        // Assert
        verify(prosessTaskTjeneste, times(4)).lagre(prosessTaskDataCaptor.capture());
        verifyNoMoreInteractions(prosessTaskTjeneste);
        var prosessTaskData = prosessTaskDataCaptor.getAllValues();

        var forventetTaskOpprettet = List.of(
                TaskType.forProsessTask(FjernIayGrunnlagUtenReferanseTask.class),
                TaskType.forProsessTask(FjernIayInformasjonUtenReferanseTask.class),
                TaskType.forProsessTask(FjernIayInntektsmeldingerUtenReferanseTask.class),
                TaskType.forProsessTask(FjernIayOppgittOpptjeningUtenReferanseTask.class)
        );
        assertThat(prosessTaskData.stream().map(ProsessTaskData::taskType).toList()).isEqualTo(forventetTaskOpprettet);
    }

    @Test
    void testFjernAlleIayGrunnlagAggregatUtenReferanse_ok() {
        // Act
        oppryddingTjeneste.opprettFjernIayInntektArbeidYtelseAggregatTask();

        // Assert
        verify(prosessTaskTjeneste, times(1)).lagre(prosessTaskDataCaptor.capture());
        verifyNoMoreInteractions(prosessTaskTjeneste);
        var prosessTaskData = prosessTaskDataCaptor.getValue();
        assertThat(prosessTaskData.taskType()).isEqualTo(TaskType.forProsessTask(FjernIayGrunnlagUtenReferanseTask.class));
    }

    @Test
    void testFjernAlleInformasjonAggregatUtenReferanse_ok() {
        // Act
        oppryddingTjeneste.opprettFjernIayInformasjonTask();

        // Assert
        verify(prosessTaskTjeneste, times(1)).lagre(prosessTaskDataCaptor.capture());
        verifyNoMoreInteractions(prosessTaskTjeneste);
        var prosessTaskData = prosessTaskDataCaptor.getValue();
        assertThat(prosessTaskData.taskType()).isEqualTo(TaskType.forProsessTask(FjernIayInformasjonUtenReferanseTask.class));
    }

    @Test
    void testFjernAlleInntektsmeldingerUtenReferanse_ok() {
        // Act
        oppryddingTjeneste.opprettFjernIayInntektsmeldingerTask();

        // Assert
        verify(prosessTaskTjeneste, times(1)).lagre(prosessTaskDataCaptor.capture());
        verifyNoMoreInteractions(prosessTaskTjeneste);
        var prosessTaskData = prosessTaskDataCaptor.getValue();
        assertThat(prosessTaskData.taskType()).isEqualTo(TaskType.forProsessTask(FjernIayInntektsmeldingerUtenReferanseTask.class));
    }

    @Test
    void testFjernAlleOppgittOpptjeningUtenReferanse_ok() {
        // Act
        oppryddingTjeneste.opprettFjernIayOppgittOpptjeningTask();

        // Assert
        verify(prosessTaskTjeneste, times(1)).lagre(prosessTaskDataCaptor.capture());
        verifyNoMoreInteractions(prosessTaskTjeneste);
        var prosessTaskData = prosessTaskDataCaptor.getValue();
        assertThat(prosessTaskData.taskType()).isEqualTo(TaskType.forProsessTask(FjernIayOppgittOpptjeningUtenReferanseTask.class));
    }
}
