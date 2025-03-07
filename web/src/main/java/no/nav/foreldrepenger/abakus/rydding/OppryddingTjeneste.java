package no.nav.foreldrepenger.abakus.rydding;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.abakus.rydding.task.FjernIAYGrunnlagUtenReferanseTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;


@ApplicationScoped
public class OppryddingTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(OppryddingTjeneste.class);
    protected static final int MAX_PARTITION_SIZE = 250;
    private OppryddingIAYAggregatRepository iayOppryddingRepository;
    private ProsessTaskTjeneste taskTjeneste;

    OppryddingTjeneste() {
        // CDI proxy
    }

    @Inject
    public OppryddingTjeneste(OppryddingIAYAggregatRepository iayOppryddingRepository, ProsessTaskTjeneste taskTjeneste) {
        this.iayOppryddingRepository = iayOppryddingRepository;
        this.taskTjeneste = taskTjeneste;
    }

    public void fjernAlleIayAggregatUtenReferanse() {
        var iayAggregatUtenReferanse = iayOppryddingRepository.hentIayAggregaterUtenReferanse(MAX_PARTITION_SIZE);
        if (iayAggregatUtenReferanse.isEmpty()) {
            LOG.info("Ingen IAY aggregat for sletting.");
            return;}
        LOG.info("Fjerner {} IAY-aggregater uten referanse.", iayAggregatUtenReferanse.size());
        opprettFjernIayAggregatTask(iayAggregatUtenReferanse);
    }

    private void opprettFjernIayAggregatTask(List<Long> iayIdsList) {
        LOG.info("Oppretter task for å fjerne {} IAY-aggregater uten referanse.", iayIdsList.size());
        var prosessTaskData = ProsessTaskData.forProsessTask(FjernIAYGrunnlagUtenReferanseTask.class);
        prosessTaskData.setPayload(DefaultJsonMapper.toJson(iayIdsList));
        taskTjeneste.lagre(prosessTaskData);
    }
}
