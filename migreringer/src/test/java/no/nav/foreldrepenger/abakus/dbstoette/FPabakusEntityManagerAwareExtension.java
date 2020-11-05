package no.nav.foreldrepenger.abakus.dbstoette;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.testutilities.db.EntityManagerAwareExtension;
import no.nav.vedtak.util.env.Environment;

public class FPabakusEntityManagerAwareExtension extends EntityManagerAwareExtension {

    private static final Logger LOG = LoggerFactory.getLogger(FPabakusEntityManagerAwareExtension.class);
    private static final boolean isNotRunningUnderMaven = Environment.current().getProperty("maven.cmd.line.args") == null;

    static {
        if (isNotRunningUnderMaven) {
            LOG.info("Kjører IKKE under maven");
            // prøver alltid migrering hvis endring, ellers funker det dårlig i IDE.
            Databaseskjemainitialisering.migrerUnittestSkjemaer();
        }
        Databaseskjemainitialisering.settPlaceholdereOgJdniOppslag();
    }

}
