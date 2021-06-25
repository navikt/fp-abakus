package no.nav.foreldrepenger.abakus.app.diagnostikk.rapportering;

import java.util.List;

import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.app.diagnostikk.DumpOutput;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;

public interface RapportGenerator {

    List<DumpOutput> generer(YtelseType ytelseType, IntervallEntitet periode);
}
