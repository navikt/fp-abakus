package no.nav.foreldrepenger.abakus.rydding.task;

import no.nav.foreldrepenger.abakus.rydding.OppryddingTjeneste;

import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FjernAlleGrunnlagUtenReferanseBatchTaskTest {

    @Mock
    private OppryddingTjeneste oppryddingTjeneste;

    private FjernAlleGrunnlagUtenReferanseBatchTask task;

    @BeforeEach
    void setUp() {
        task = new FjernAlleGrunnlagUtenReferanseBatchTask(oppryddingTjeneste);
    }

    @Test
    void doTask() {
        task.doTask(ProsessTaskData.forProsessTask(FjernAlleGrunnlagUtenReferanseBatchTask.class));
        verify(oppryddingTjeneste).fjernAlleIayAggregatUtenReferanse();
    }
}
