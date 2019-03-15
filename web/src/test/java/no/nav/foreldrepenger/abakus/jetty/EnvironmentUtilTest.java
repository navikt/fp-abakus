package no.nav.foreldrepenger.abakus.jetty;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import no.nav.foreldrepenger.abakus.jetty.db.EnvironmentClass;

public class EnvironmentUtilTest {

    @Test
    public void skal_finne_environment_basert_på_nais_cluster() {
        System.setProperty("nais.cluster.name", "prod-fss");

        EnvironmentClass environment = EnvironmentUtil.getEnvironmentClass();
        assertThat(environment).isEqualByComparingTo(EnvironmentClass.PROD);
        System.setProperty("nais.cluster.name", "preprod-fss");
        environment = EnvironmentUtil.getEnvironmentClass();
        assertThat(environment).isEqualByComparingTo(EnvironmentClass.PREPROD);

        System.clearProperty("nais.cluster.name");
    }
}
