package no.nav.foreldrepenger.abakus.jetty;

import no.nav.foreldrepenger.abakus.jetty.db.EnvironmentClass;

public final class EnvironmentUtil {
    private EnvironmentUtil() {
    }

    public static EnvironmentClass getEnvironmentClass() {
        String cluster = System.getProperty("nais.cluster.name", System.getenv("NAIS_CLUSTER_NAME"));
        if (cluster != null) {
            cluster = cluster.substring(0, cluster.indexOf("-")).toUpperCase();
            if ("DEV".equalsIgnoreCase(cluster)) {
                return EnvironmentClass.PREPROD;
            }
            return EnvironmentClass.valueOf(cluster);
        }
        return EnvironmentClass.PROD;
    }
}