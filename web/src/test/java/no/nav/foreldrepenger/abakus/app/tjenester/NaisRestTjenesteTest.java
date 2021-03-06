package no.nav.foreldrepenger.abakus.app.tjenester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.abakus.app.konfig.ApplicationServiceStarter;
import no.nav.foreldrepenger.abakus.app.selftest.NaisRestTjeneste;
import no.nav.foreldrepenger.abakus.app.selftest.checks.DatabaseHealthCheck;

@SuppressWarnings("resource")
public class NaisRestTjenesteTest {

    private NaisRestTjeneste restTjeneste;

    private ApplicationServiceStarter serviceStarterMock = mock(ApplicationServiceStarter.class);
    private DatabaseHealthCheck databaseHealthCheck = mock(DatabaseHealthCheck.class);

    @BeforeEach
    public void setup() {
        restTjeneste = new NaisRestTjeneste(serviceStarterMock, databaseHealthCheck);
    }

    @Test
    public void test_isAlive_skal_returnere_status_200() {
        when(serviceStarterMock.isKafkaAlive()).thenReturn(true);
        Response response = restTjeneste.isAlive();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void test_isReady_skal_returnere_service_unavailable_når_kritiske_selftester_feiler() {
        when(databaseHealthCheck.isReady()).thenReturn(false);

        Response response = restTjeneste.isReady();

        assertThat(response.getStatus()).isEqualTo(Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
    }

    @Test
    public void test_isReady_skal_returnere_status_ok_når_selftester_er_ok() {
        when(databaseHealthCheck.isReady()).thenReturn(true);

        Response response = restTjeneste.isReady();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void test_preStop_skal_kalle_stopServices_og_returnere_status_ok() {
        Response response = restTjeneste.preStop();

        verify(serviceStarterMock).stopServices();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }
}
