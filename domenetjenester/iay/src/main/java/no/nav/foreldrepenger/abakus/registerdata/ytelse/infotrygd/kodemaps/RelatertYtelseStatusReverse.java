package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.kodemaps;

import java.util.Map;

import org.slf4j.Logger;

import no.nav.foreldrepenger.abakus.kodeverk.RelatertYtelseStatus;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseStatus;

public class RelatertYtelseStatusReverse {


    private static final Map<String, YtelseStatus> YTELSE_STATUS_MAP = Map.ofEntries(
        Map.entry(RelatertYtelseStatus.AVSLUTTET_IT.getKode(), YtelseStatus.AVSLUTTET),
        Map.entry(RelatertYtelseStatus.LØPENDE_VEDTAK.getKode(), YtelseStatus.LØPENDE),
        Map.entry(RelatertYtelseStatus.IKKE_STARTET.getKode(), YtelseStatus.UNDER_BEHANDLING),
        Map.entry(RelatertYtelseStatus.AVSLU.getKode(), YtelseStatus.AVSLUTTET),
        Map.entry("INAKT", YtelseStatus.AVSLUTTET), // Sak, ingen vedtak
        Map.entry(RelatertYtelseStatus.IVERK.getKode(), YtelseStatus.LØPENDE),
        // Resterende koder Arena
        Map.entry("AKTIV", YtelseStatus.UNDER_BEHANDLING), // Sak, ingen vedtak
        Map.entry(RelatertYtelseStatus.GODKJ.getKode(), YtelseStatus.UNDER_BEHANDLING),
        Map.entry(RelatertYtelseStatus.INNST.getKode(), YtelseStatus.UNDER_BEHANDLING),
        Map.entry(RelatertYtelseStatus.MOTAT.getKode(), YtelseStatus.UNDER_BEHANDLING),
        Map.entry(RelatertYtelseStatus.OPPRE.getKode(), YtelseStatus.UNDER_BEHANDLING),
        Map.entry(RelatertYtelseStatus.REGIS.getKode(), YtelseStatus.UNDER_BEHANDLING)
    );

    public static YtelseStatus reverseMap(String kode, Logger logger) {
        if (YTELSE_STATUS_MAP.get(kode) == null) {
            logger.warn("Infotrygd ga ukjent kode for relatert ytelse status {}", kode);
        }
        return YTELSE_STATUS_MAP.getOrDefault(kode, YtelseStatus.UNDER_BEHANDLING);
    }
}
