package no.nav.foreldrepenger.abakus.rydding;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

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
        verify(oppryddingTjeneste).fjernAlleIayOppgittOpptjeningUtenReferanse();
        verify(oppryddingTjeneste).fjernAlleIayInformasjontUtenReferanse();
        verify(oppryddingTjeneste).fjernAlleIayInntektsmeldingerUtenReferanse();
    }
}
