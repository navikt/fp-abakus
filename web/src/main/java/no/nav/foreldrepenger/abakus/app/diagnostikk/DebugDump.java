package no.nav.foreldrepenger.abakus.app.diagnostikk;

import java.util.List;

public interface DebugDump {

    List<DumpOutput> dump(DumpKontekst dumpKontekst);

}
