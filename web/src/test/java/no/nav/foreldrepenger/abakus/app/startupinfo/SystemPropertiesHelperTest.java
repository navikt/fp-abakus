package no.nav.foreldrepenger.abakus.app.startupinfo;

import java.util.HashMap;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class SystemPropertiesHelperTest {
    @Test
    public void skal_filtrere_bort_passord_fra_java_opts() {
        var input = new HashMap<String, String>() {{
            put("JAVA_OPTS", "-Djavax.net.ssl.trustStore=/foo/bar -Djavax.net.ssl.trustStorePassword=passord_i_klartekst  -javaagent:/foo/bar/javaagent.jar  -DapplicationName=dummy -");
        }};

        SystemPropertiesHelper.filter(input);

        Assertions.assertThat(input.get("JAVA_OPTS")).isEqualTo("-Djavax.net.ssl.trustStore=/foo/bar -Djavax.net.ssl.trustStorePassword=*****  -javaagent:/foo/bar/javaagent.jar  -DapplicationName=dummy -");
    }

    @Test
    public void skal_filtrere_bort_key_fra_appd() {
        var input = new HashMap<String, String>() {{
            put("APPDYNAMICS_AGENT_ACCOUNT_ACCESS_KEY", "key_i_kalrtekst");
        }};

        SystemPropertiesHelper.filter(input);

        Assertions.assertThat(input.get("APPDYNAMICS_AGENT_ACCOUNT_ACCESS_KEY")).isEqualTo("*****");
    }
}
