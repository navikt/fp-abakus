package no.nav.abakus.prosesstask.batch;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@ProsessTask(value = "partition.cleanBucket", cronExpression = "0 0 7 1 * *", maxFailedRuns = 1)
public class CleanNextBucketBatchTask implements ProsessTaskHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CleanNextBucketBatchTask.class);
    private ProsessTaskTjeneste taskTjeneste;

    @Inject
    public CleanNextBucketBatchTask(ProsessTaskTjeneste taskTjeneste) {
        this.taskTjeneste = taskTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        int antallSlettet = taskTjeneste.tømNestePartisjon();
        LOG.info("Tømmer neste partisjon med ferdige tasks, slettet {}", antallSlettet);
    }
}
