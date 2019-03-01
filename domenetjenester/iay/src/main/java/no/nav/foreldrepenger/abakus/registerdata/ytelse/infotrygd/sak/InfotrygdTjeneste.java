package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.sak;

import java.time.LocalDate;
import java.util.List;

import no.nav.foreldrepenger.abakus.kobling.Kobling;

public interface InfotrygdTjeneste {
    List<InfotrygdSak> finnSakListe(Kobling behandling, String fnr, LocalDate fom);
}
