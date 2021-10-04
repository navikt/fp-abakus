package no.nav.foreldrepenger.abakus.app.selftest.checks;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import no.nav.foreldrepenger.abakus.dbstoette.JpaExtension;


public class DatabaseHealthCheckTest {

    @RegisterExtension
    public static JpaExtension jpaExtension = new JpaExtension();

    @Test
    public void test_working_query() {
        assertThat(new DatabaseHealthCheck().isReady()).isTrue();
    }


}
