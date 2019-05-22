package no.nav.foreldrepenger.abakus.vedtak.kafka;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.vedtak.LagreVedtakTask;
import no.nav.vedtak.felles.AktiverContextOgTransaksjon;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ApplicationScoped
@AktiverContextOgTransaksjon
public class VedtaksHendelseHåndterer {
    private ProsessTaskRepository taskRepository;

    public VedtaksHendelseHåndterer() {
    }

    @Inject
    public VedtaksHendelseHåndterer(ProsessTaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    void handleMessage(String key, String payload) {
        ProsessTaskData data = new ProsessTaskData(LagreVedtakTask.TASKTYPE);
        data.setProperty(LagreVedtakTask.KEY, key);
        data.setPayload(payload);

        taskRepository.lagre(data);
    }
}
