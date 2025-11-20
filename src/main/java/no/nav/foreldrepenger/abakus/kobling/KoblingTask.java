package no.nav.foreldrepenger.abakus.kobling;

import jakarta.persistence.NoResultException;
import no.nav.foreldrepenger.abakus.kobling.repository.LåsRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.log.mdc.MdcExtendedLogContext;

public abstract class KoblingTask implements ProsessTaskHandler {

    private static final MdcExtendedLogContext LOG_CONTEXT = MdcExtendedLogContext.getContext("prosess");

    private LåsRepository låsRepository;

    protected KoblingTask() {
        // CDI proxy
    }

    protected KoblingTask(LåsRepository låsRepository) {
        this.låsRepository = låsRepository;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        var koblingId = Long.valueOf(prosessTaskData.getPropertyValue(TaskConstants.KOBLING_ID));
        LOG_CONTEXT.add("koblingId", koblingId);

        try {
            var koblingLås = låsRepository.taLås(koblingId);

            prosesser(prosessTaskData);

            låsRepository.oppdaterLåsVersjon(koblingLås);
        } catch (NoResultException _) {
            // NOOP - oppstår dersom det ikke lenger finnes en kobling som er aktiv, dvs behandlingen er avsluttet
        }

    }

    protected abstract void prosesser(ProsessTaskData prosessTaskData);
}
