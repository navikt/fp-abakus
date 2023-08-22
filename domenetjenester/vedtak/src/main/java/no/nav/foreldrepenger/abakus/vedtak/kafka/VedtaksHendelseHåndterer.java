package no.nav.foreldrepenger.abakus.vedtak.kafka;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.abakus.vedtak.LagreVedtakTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskDataBuilder;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@ApplicationScoped
@ActivateRequestContext
@Transactional
public class VedtaksHendelseHåndterer {

    private static final Logger LOG = LoggerFactory.getLogger(VedtaksHendelseHåndterer.class);
    private ProsessTaskTjeneste taskTjeneste;

    public VedtaksHendelseHåndterer() {
        // CDI
    }

    @Inject
    public VedtaksHendelseHåndterer(ProsessTaskTjeneste taskTjeneste) {
        this.taskTjeneste = taskTjeneste;
    }

    void handleMessage(String key, String payload) {
        LOG.debug("Mottatt ytelse-vedtatt hendelse med key='{}', payload={}", key, payload);
        var data = ProsessTaskDataBuilder.forProsessTask(LagreVedtakTask.class).medProperty(LagreVedtakTask.KEY, key).medPayload(payload);

        taskTjeneste.lagre(data.build());
    }
}
