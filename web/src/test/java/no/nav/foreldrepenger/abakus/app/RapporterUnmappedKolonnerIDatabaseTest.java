package no.nav.foreldrepenger.abakus.app;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.boot.model.relational.Namespace;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.jpa.boot.spi.IntegratorProvider;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import no.nav.foreldrepenger.abakus.dbstoette.Databaseskjemainitialisering;

/**
 * Denne testen rapporterer kun tabeller og kolonner som ikke er mappet i hibernate. Det kan være gyldige grunner til det (f.eks. dersom det
 * kun aksesseres gjennom native sql), men p.t. høyst sannsynlig ikke.
 * Bør gjennomgås jevnlig for å luke manglende contract av db skjema.
 */
class RapporterUnmappedKolonnerIDatabaseTest {
    private static final Logger LOG = LoggerFactory.getLogger(RapporterUnmappedKolonnerIDatabaseTest.class);

    private static EntityManagerFactory entityManagerFactory;

    private static List<Pattern> WHITELIST = List.of(Pattern.compile("^PROSESS_TASK.*$", Pattern.CASE_INSENSITIVE),
        Pattern.compile("^.*SCHEMA_VERSION.*$", Pattern.CASE_INSENSITIVE),
        Pattern.compile("^BEHANDLING#SIST_OPPDATERT_TIDSPUNKT.*$", Pattern.CASE_INSENSITIVE));

    private RapporterUnmappedKolonnerIDatabaseTest() {
    }

    @BeforeAll
    static void setup() {
        // Kan ikke skrus på nå - trigger på CHAR kolonner som kunne vært VARCHAR. Må fikses først
        // System.setProperty("hibernate.hbm2ddl.auto", "validate");
        Databaseskjemainitialisering.initUnitTestDataSource();
        Map<String, Object> configuration = new HashMap<>();

        configuration.put("hibernate.integrator_provider",
            (IntegratorProvider) () -> Collections.singletonList(MetadataExtractorIntegrator.INSTANCE));

        entityManagerFactory = Persistence.createEntityManagerFactory("pu-default", configuration);
    }

    @AfterAll
    static void teardown() throws Exception {
        entityManagerFactory.close();
    }

    private NavigableMap<String, Set<String>> getColumns(@SuppressWarnings("unused") String namespace) {
        var groupingBy = Collectors.groupingBy((Object[] cols) -> ((String) cols[0]).toUpperCase(), TreeMap::new,
            Collectors.mapping((Object[] cols) -> ((String) cols[1]).toUpperCase(), Collectors.toCollection(TreeSet::new)));

        var em = entityManagerFactory.createEntityManager();
        try {
            @SuppressWarnings({"unchecked"}) var result = (NavigableMap<String, Set<String>>) em.createNativeQuery(
                "select table_name, column_name\n" + "     from information_schema.columns\n" + "     where table_schema in ('public')\n"
                    + "     order by 1,2").getResultStream().collect(groupingBy);

            var filtered = new TreeMap<String, Set<String>>();
            for (var entry : result.entrySet()) {
                if (!whitelistTable(entry.getKey())) {
                    filtered.put(entry.getKey(), entry.getValue());
                }
            }
            return filtered;
        } finally {
            em.close();
        }
    }

    private boolean whitelistTable(String tableName) {
        return WHITELIST.stream().anyMatch(p -> p.matcher(tableName).matches());
    }

    private Set<String> whitelistColumns(String table, Set<String> columns) {
        var cols = columns.stream().filter(c -> !WHITELIST.stream().anyMatch(p -> p.matcher(table + "#" + c).matches())).collect(Collectors.toSet());

        return cols;
    }

    @SuppressWarnings("java:S2699")
    @Test
    void sjekk_unmapped() throws Exception {
        sjekk_alle_tabeller_mappet();
        sjekk_alle_kolonner_mappet();
    }

    private void sjekk_alle_kolonner_mappet() {
        for (var namespace : MetadataExtractorIntegrator.INSTANCE.getDatabase().getNamespaces()) {
            var namespaceName = getSchemaName(namespace);
            var dbColumns = getColumns(namespaceName);

            for (var table : namespace.getTables()) {

                var tableName = table.getName().toUpperCase();
                if (whitelistTable(tableName)) {
                    continue;
                }

                var columnNames = table.getColumns().stream().map(c -> c.getName().toUpperCase()).collect(Collectors.toCollection(TreeSet::new));
                if (dbColumns.containsKey(tableName)) {
                    var unmapped = new TreeSet<>(whitelistColumns(tableName, dbColumns.get(tableName)));
                    unmapped.removeAll(columnNames);
                    if (!unmapped.isEmpty()) {
                        LOG.warn("Table {} has unmapped columns: {}", table.getName(), unmapped);
                    }
                } else {
                    LOG.warn("Table {} not in database schema {}", tableName, namespaceName);
                }
            }
        }

    }

    private void sjekk_alle_tabeller_mappet() throws Exception {
        for (var namespace : MetadataExtractorIntegrator.INSTANCE.getDatabase().getNamespaces()) {
            var namespaceName = getSchemaName(namespace);
            var dbColumns = getColumns(namespaceName);
            var dbTables = dbColumns.keySet();
            for (var table : namespace.getTables()) {
                var tableName = table.getName().toUpperCase();
                dbTables.remove(tableName);
            }
            dbTables.forEach(t -> LOG.warn("Table not mapped in hibernate{}: {}", namespaceName, t));
        }

    }

    private String getSchemaName(Namespace namespace) {
        var schema = namespace.getName().getSchema();
        return schema == null ? null : schema.getCanonicalName().toUpperCase();
    }

    static class MetadataExtractorIntegrator implements org.hibernate.integrator.spi.Integrator {

        static final MetadataExtractorIntegrator INSTANCE = new MetadataExtractorIntegrator();

        private Database database;

        Database getDatabase() {
            return database;
        }

        @Override
        public void integrate(Metadata metadata, SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
            database = metadata.getDatabase();
        }

        @Override
        public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
        }
    }

}
