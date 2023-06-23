package no.nav.foreldrepenger.abakus.kobling;

import no.nav.foreldrepenger.abakus.kobling.repository.LåsRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.log.mdc.MdcExtendedLogContext;

public abstract class KoblingTask implements ProsessTaskHandler {

    private static final MdcExtendedLogContext LOG_CONTEXT = MdcExtendedLogContext.getContext("prosess");

    private LåsRepository låsRepository;

    public KoblingTask() {
    }

    public KoblingTask(LåsRepository låsRepository) {
        this.låsRepository = låsRepository;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        String nyKoblingId = prosessTaskData.getPropertyValue(TaskConstants.KOBLING_ID);
        Long koblingId = nyKoblingId != null ? Long.valueOf(nyKoblingId) : Long.valueOf(prosessTaskData.getBehandlingId());
        LOG_CONTEXT.add("koblingId", koblingId);

        KoblingLås koblingLås = låsRepository.taLås(koblingId);

        prosesser(prosessTaskData);

        låsRepository.oppdaterLåsVersjon(koblingLås);
    }

    protected abstract void prosesser(ProsessTaskData prosessTaskData);
}
