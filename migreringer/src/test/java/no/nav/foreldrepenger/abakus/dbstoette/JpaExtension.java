package no.nav.foreldrepenger.abakus.dbstoette;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.felles.testutilities.db.EntityManagerAwareExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JpaExtension extends EntityManagerAwareExtension {

    private static final Logger LOG = LoggerFactory.getLogger(JpaExtension.class);
    private static final boolean isNotRunningUnderMaven =
            Environment.current().getProperty("maven.cmd.line.args") == null;

    static {
        if (isNotRunningUnderMaven) {
            LOG.info("Kjører IKKE under maven");
            // prøver alltid migrering hvis endring, ellers funker det dårlig i IDE.
            Databaseskjemainitialisering.migrerUnittestSkjemaer();
        }
        Databaseskjemainitialisering.initUnitTestDataSource();
    }
}
