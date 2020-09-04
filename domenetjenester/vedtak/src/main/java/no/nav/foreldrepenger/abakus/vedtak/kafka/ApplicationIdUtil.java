package no.nav.foreldrepenger.abakus.vedtak.kafka;

public final class ApplicationIdUtil {

    private ApplicationIdUtil() {
    }

    public static String get() {
        return System.getProperty("nais.app.name", "fpabakus");
    }
}
