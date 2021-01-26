package no.nav.foreldrepenger.abakus.app.tjenester;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Iterator;

import javax.enterprise.inject.Instance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.abakus.app.konfig.ApplicationServiceStarter;
import no.nav.foreldrepenger.abakus.felles.kafka.KafkaIntegration;
import no.nav.vedtak.felles.integrasjon.sensu.SensuKlient;
import no.nav.vedtak.felles.prosesstask.impl.BatchTaskScheduler;
import no.nav.vedtak.felles.prosesstask.impl.TaskManager;

public class ApplicationServiceStarterTest {

    private ApplicationServiceStarter serviceStarter;

    private KafkaIntegration serviceMock = mock(KafkaIntegration.class);
    private Instance<KafkaIntegration> testInstance = new UnitTestInstanceImpl<>(serviceMock);
    private Instance<KafkaIntegration> instanceSpied = spy(testInstance);

    @SuppressWarnings("unchecked")
    private Iterator<KafkaIntegration> iteratorMock = mock(Iterator.class);

    @BeforeEach
    public void setup() {
        when(iteratorMock.hasNext()).thenReturn(true, false);
        when(iteratorMock.next()).thenReturn(serviceMock);
        doReturn(iteratorMock).when(instanceSpied).iterator();

        serviceStarter = new ApplicationServiceStarter(instanceSpied, mock(SensuKlient.class), mock(TaskManager.class), mock(BatchTaskScheduler.class));
    }

    @Test
    public void test_skal_kalle_AppServiceHandler_start_og_stop() {
        serviceStarter.startServices();
        serviceStarter.stopServices();

        verify(serviceMock).start();
        verify(serviceMock).start();
    }
}
