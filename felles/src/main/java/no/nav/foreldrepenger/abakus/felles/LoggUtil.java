package no.nav.foreldrepenger.abakus.felles;

import java.util.UUID;

import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.vedtak.log.mdc.MdcExtendedLogContext;

public class LoggUtil {
    private static final MdcExtendedLogContext LOG_CONTEXT = MdcExtendedLogContext.getContext("prosess");
    protected static final String SAKSNUMMER = "saksnummer";
    protected static final String KOBLING_REFERANSE = "koblingReferanse";
    protected static final String YTELSE_TYPE = "ytelseType";

    private LoggUtil() {
        // Skjuler konstruktør
    }

    public static void setupLogMdc(YtelseType ytelseType, String saksnummer) {
        setupLogMdc(ytelseType);
        LOG_CONTEXT.add(SAKSNUMMER, saksnummer);
    }

    public static void setupLogMdc(YtelseType ytelseType, String saksnummer, UUID koblingReferanse) {
        setupLogMdc(ytelseType);
        LOG_CONTEXT.add(SAKSNUMMER, saksnummer);
        LOG_CONTEXT.add(KOBLING_REFERANSE, koblingReferanse);
    }

    public static void setupLogMdc(YtelseType ytelseType, String saksnummer, String koblingReferanse) {
        LOG_CONTEXT.add(SAKSNUMMER, saksnummer);
        LOG_CONTEXT.add(YTELSE_TYPE, ytelseType.getKode());
        LOG_CONTEXT.add(KOBLING_REFERANSE, koblingReferanse);
    }

    public static void setupLogMdc(YtelseType ytelse) {
        LOG_CONTEXT.add(YTELSE_TYPE, ytelse.getKode());
    }
}
