package no.nav.foreldrepenger.abakus.jetty;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vault.jdbc.hikaricp.HikariCPVaultUtil;
import no.nav.vault.jdbc.hikaricp.VaultError;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.jpa.flyway.FlywayUtil;
import no.nav.vedtak.felles.jpa.jdbc.DatasourceUtil;

class LocalDatasourceUtil {

    private static final Environment ENV = Environment.current();

    private LocalDatasourceUtil() {
    }

    static HikariDataSource createDatasource(int maxPoolSize) {
        var jdbcUrl = ENV.getRequiredProperty("defaultDS.url");
        if (ENV.isVTP() || ENV.isLocal()) {
            return DatasourceUtil.postgresDataSource(jdbcUrl, getUsername(), getUsername(), maxPoolSize);
        } else {
            var config = DatasourceUtil.postgresDataSourceConfig(jdbcUrl, null, null, maxPoolSize);
            return createVaultDatasource(config, mountPath(), getRole(DatasourceRole.USER));
        }
    }

    static HikariDataSource createMigrationDatasource() {
        var jdbcUrl = ENV.getRequiredProperty("defaultDS.url");
        if (ENV.isVTP() || ENV.isLocal()) {
            return FlywayUtil.createMigrationDataSource(jdbcUrl, getUsername(), getUsername());
        } else {
            var config = FlywayUtil.createMigrationDataSourceConfig(jdbcUrl, null, null);
            return createVaultDatasource(config, mountPath(), getRole(DatasourceRole.ADMIN));
        }
    }

    static String getRole(DatasourceRole role) {
        return String.format("%s-%s", getUsername(), role.name().toLowerCase());
    }

    private static String getUsername() {
        return ENV.getRequiredProperty("defaultDS.username");
    }

    private static HikariDataSource createVaultDatasource(HikariConfig config, String mountPath, String role) {
        try {
            return HikariCPVaultUtil.createHikariDataSourceWithVaultIntegration(config, mountPath, role);
        } catch (VaultError vaultError) {
            throw new TekniskException("VAULT-ERROR", "Vault feil ved opprettelse av databaseforbindelse", vaultError);
        }
    }

    private static String mountPath() {
        return "postgresql/" + (ENV.isProd() ? "prod-fss" : "preprod-fss");
    }
}
