package no.nav.foreldrepenger.abakus.jetty;

import no.nav.foreldrepenger.abakus.jetty.db.EnvironmentClass;
import no.nav.vedtak.konfig.PropertyUtil;

public final class EnvironmentUtil {
    private EnvironmentUtil() {
    }

    public static EnvironmentClass getEnvironmentClass() {
        String cluster = PropertyUtil.getProperty("nais.cluster.name");
        if (cluster != null) {
            cluster = cluster.substring(0, cluster.indexOf("-")).toUpperCase();
            return EnvironmentClass.valueOf(cluster);
        }
        return EnvironmentClass.PROD;
    }
}
