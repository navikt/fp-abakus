package no.nav.foreldrepenger.abakus.jetty;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PropertiesUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesUtils.class);

    private static String DEV_FILNAVN_LOCAL = "app-local.properties";
    private static String VTP_FILNAVN_LOCAL = "app-vtp.properties";

    private PropertiesUtils() {
    }

    static void initProperties() {
        loadPropertyFile(new File(DEV_FILNAVN_LOCAL));
        loadPropertyFile(new File(VTP_FILNAVN_LOCAL));
    }

    private static void loadPropertyFile(File devFil) {
        if (devFil.exists()) {
            Properties prop = new Properties();
            try (InputStream inputStream = new FileInputStream(devFil)) {
                prop.load(inputStream);
            } catch (IOException e) {
                LOGGER.error("Kunne ikke finne properties-fil", e);
            }
            System.getProperties().putAll(prop);
        } else {
            LOGGER.warn("Fant ikke [{}], laster ikke properites derfra ", devFil);
        }
    }

}

