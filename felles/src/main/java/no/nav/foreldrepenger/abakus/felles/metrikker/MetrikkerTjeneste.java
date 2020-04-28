package no.nav.foreldrepenger.abakus.felles.metrikker;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.integrasjon.sensu.SensuEvent;
import no.nav.vedtak.felles.integrasjon.sensu.SensuKlient;

@ApplicationScoped
public class MetrikkerTjeneste {
    private static final Logger LOG = LoggerFactory.getLogger(MetrikkerTjeneste.class);

    private SensuKlient sensuKlient;

    MetrikkerTjeneste() {} // WELD ctor

    @Inject
    public MetrikkerTjeneste(SensuKlient sensuKlient) {
        this.sensuKlient = sensuKlient;
    }

    public void logVedtakMottatRest(String vedtakType, String ytelseStatus, String fagsystem) {
        send(opprettVedtakEvent("antall_vedtakk_mottatt", "REST", vedtakType, ytelseStatus,fagsystem));
    }

    public void logVedtakMottatKafka(String vedtakType, String ytelseStatus, String fagsystem) {
        send(opprettVedtakEvent("antall_vedtakk_mottatt", "Kafka", vedtakType, ytelseStatus,fagsystem));
    }

    private SensuEvent opprettVedtakEvent(String metrikkNavn, String inputKilde, String vedtakType, String ytelseStatus, String fagsystem) {
        return SensuEvent.createSensuEvent(metrikkNavn,
                Map.of("input_kilde", inputKilde,
                        "vedtak_type", vedtakType,
                        "ytelse_status", ytelseStatus,
                        "fagsystem", fagsystem),
                Map.of("antall", 1));
    }

    public void logFeilProsessTask(String prosessTaskType, int antall) {
        send(opprettProsessTaskEvent("antall_feilende_prosesstask", prosessTaskType, antall));
    }

    private static SensuEvent opprettProsessTaskEvent(String metrikkNavn, String prosessTaskType, int antall) {
        return SensuEvent.createSensuEvent(metrikkNavn,
                Map.of("prosesstask_type", prosessTaskType),
                Map.of("antall", antall));
    }

    private void send(SensuEvent event) {
        try {
            sensuKlient.logMetrics(event);
        } catch (Exception ex) {
            LOG.info("Exception ved logging til sensu.");
        }
    }
}
