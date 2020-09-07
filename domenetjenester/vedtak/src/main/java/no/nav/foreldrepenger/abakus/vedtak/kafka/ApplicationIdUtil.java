package no.nav.foreldrepenger.abakus.vedtak.kafka;

import no.nav.vedtak.util.env.Environment;

public final class ApplicationIdUtil {

    private static final Environment ENV = Environment.current();

    private ApplicationIdUtil() {
    }

    public static String get() {
        String prefix = ENV.getProperty("nais.app.name", "fpabakus");
        if (ENV.isProd()) {
            return prefix + "-default";
        }
        return prefix + "-" + ENV.namespace();
    }
}
