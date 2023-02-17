package no.nav.foreldrepenger.abakus.felles;

import java.util.UUID;

import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.vedtak.log.mdc.MdcExtendedLogContext;

public class LoggUtil {
    private static final MdcExtendedLogContext LOG_CONTEXT = MdcExtendedLogContext.getContext("prosess");

    public static void setupLogMdc(YtelseType ytelseType, String saksnummer) {
        setupLogMdc(ytelseType);
        LOG_CONTEXT.add("saksnummer", saksnummer);
    }

    public static void setupLogMdc(YtelseType ytelseType, String saksnummer, UUID koblingReferanse) {
        setupLogMdc(ytelseType);
        LOG_CONTEXT.add("saksnummer", saksnummer);
        LOG_CONTEXT.add("koblingReferanse", koblingReferanse);
    }

    public static void setupLogMdc(YtelseType ytelseType, String saksnummer, String koblingReferanse) {
        LOG_CONTEXT.add("saksnummer", saksnummer);
        LOG_CONTEXT.add("ytelseType", ytelseType.getKode());
        LOG_CONTEXT.add("koblingReferanse", koblingReferanse);
    }

    public static void setupLogMdc(YtelseType ytelse) {
        LOG_CONTEXT.add("ytelseType", ytelse.getKode());
    }
}
