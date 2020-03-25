package no.nav.foreldrepenger.abakus.kobling;

import no.nav.foreldrepenger.abakus.kobling.repository.LåsRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

public abstract class KoblingTask implements ProsessTaskHandler {

    private LåsRepository låsRepository;

    public KoblingTask() {
    }

    public KoblingTask(LåsRepository låsRepository) {
        this.låsRepository = låsRepository;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long koblingId = Long.valueOf(prosessTaskData.getBehandlingId()); // koblingId
        KoblingLås koblingLås = låsRepository.taLås(koblingId);

        prosesser(prosessTaskData);

        låsRepository.oppdaterLåsVersjon(koblingLås);
    }

    protected abstract void prosesser(ProsessTaskData prosessTaskData);
}
