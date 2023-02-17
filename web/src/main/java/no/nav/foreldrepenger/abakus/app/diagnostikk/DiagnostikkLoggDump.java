package no.nav.foreldrepenger.abakus.app.diagnostikk;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Tuple;

import no.nav.foreldrepenger.abakus.kobling.kontroll.YtelseTypeRef;

/**
 * Logger tilgang til fagsak for diagnostikk dumps til en egen tabell, inkluderer logg som del av output.
 */
@ApplicationScoped
@YtelseTypeRef
public class DiagnostikkLoggDump implements DebugDump {

    private EntityManager entityManager;

    DiagnostikkLoggDump() {
        // for proxy
    }

    @Inject
    DiagnostikkLoggDump(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<DumpOutput> dump(DumpKontekst dumpKontekst) {
        var sql = "select" + "   d.saksnummer" + " , replace(cast(d.opprettet_tid as varchar), ' ', 'T') opprettet_tid" + " , d.opprettet_av"
            + " from diagnostikk_logg d " + " where d.saksnummer=:saksnummer " + " order by d.opprettet_tid desc";

        var query = entityManager.createNativeQuery(sql, Tuple.class).setParameter("saksnummer", dumpKontekst.getSaksnummer().getVerdi());
        String path = "diagnostikk-logg.csv";

        @SuppressWarnings("unchecked") List<Tuple> results = query.getResultList();

        if (results.isEmpty()) {
            return List.of();
        }

        return CsvOutput.dumpResultSetToCsv(path, results).map(v -> List.of(v)).orElse(List.of());
    }

}
